package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomOrder;
import com.zjxy.intangible_heritage.entity.CustomPayment;
import com.zjxy.intangible_heritage.entity.WalletAccount;
import com.zjxy.intangible_heritage.entity.WalletTransaction;
import com.zjxy.intangible_heritage.repository.CustomOrderRepository;
import com.zjxy.intangible_heritage.repository.WalletAccountRepository;
import com.zjxy.intangible_heritage.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletAccountRepository walletAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CustomOrderRepository customOrderRepository;
    private final PaymentService paymentService;

    @Override
    public WalletAccount getOrCreateAccount(Long userId) {
        return walletAccountRepository.findByUserId(userId).orElseGet(() -> {
            WalletAccount a = new WalletAccount();
            a.setUserId(userId);
            a.setBalance(BigDecimal.ZERO);
            return walletAccountRepository.save(a);
        });
    }

    @Override
    public List<WalletTransaction> listTransactions(Long userId) {
        return walletTransactionRepository.findByUserIdOrderByCreateTimeDescIdDesc(userId);
    }

    @Override
    @Transactional
    public WalletTransaction applyRecharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("充值金额无效");
        }
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalStateException("单次充值不能超过 100000 元");
        }
        getOrCreateAccount(userId);
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setTxType("RECHARGE");
        tx.setAmount(amount);
        tx.setStatus(0);
        tx.setRemark("充值申请，等待管理员审核");
        return walletTransactionRepository.save(tx);
    }

    @Override
    @Transactional
    public WalletTransaction auditRecharge(Long txId, Long adminId, boolean approved, String rejectReason) {
        WalletTransaction tx = walletTransactionRepository.findById(txId)
                .orElseThrow(() -> new IllegalStateException("充值记录不存在"));
        if (!"RECHARGE".equals(tx.getTxType()) || tx.getStatus() == null || tx.getStatus() != 0) {
            throw new IllegalStateException("该记录不可审核");
        }
        tx.setAuditorId(adminId);
        tx.setAuditTime(LocalDateTime.now());
        if (approved) {
            getOrCreateAccount(tx.getUserId());
            walletAccountRepository.credit(tx.getUserId(), tx.getAmount());
            BigDecimal balanceAfter = walletAccountRepository.findByUserId(tx.getUserId())
                    .map(WalletAccount::getBalance).orElse(BigDecimal.ZERO);
            tx.setStatus(1);
            tx.setBalanceAfter(balanceAfter);
            tx.setRemark("充值审核通过，已到账");
        } else {
            tx.setStatus(2);
            tx.setRemark(rejectReason == null || rejectReason.isBlank() ? "审核未通过" : rejectReason);
        }
        return walletTransactionRepository.save(tx);
    }

    @Override
    public List<WalletTransaction> pendingRecharges() {
        return walletTransactionRepository
                .findByTxTypeAndStatusOrderByCreateTimeDescIdDesc("RECHARGE", 0);
    }

    @Override
    @Transactional
    public void payByWallet(String outTradeNo, Long payerId) {
        // 1. 校验支付流水与付款人
        CustomPayment payment = paymentService.getByOutTradeNo(outTradeNo);
        if (!payment.getPayerId().equals(payerId)) {
            throw new IllegalStateException("无权支付该笔订单");
        }
        if (payment.getStatus() != 0) {
            throw new IllegalStateException("该笔支付已处理，请勿重复支付");
        }
        CustomOrder order = customOrderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalStateException("关联订单不存在"));
        Long craftsmanId = order.getCraftsmanId();
        BigDecimal amount = payment.getAmount();

        // 2. 原子扣款（余额不足时更新 0 行），失败则整个事务回滚
        getOrCreateAccount(payerId);
        getOrCreateAccount(craftsmanId);
        int rows = walletAccountRepository.deduct(payerId, amount);
        if (rows == 0) {
            throw new IllegalStateException("钱包余额不足，请先充值");
        }
        // 3. 匠人加款
        int credited = walletAccountRepository.credit(craftsmanId, amount);
        if (credited == 0) {
            throw new IllegalStateException("匠人账户入账失败");
        }
        BigDecimal payerBalance = walletAccountRepository.findByUserId(payerId)
                .map(WalletAccount::getBalance).orElse(BigDecimal.ZERO);
        BigDecimal craftsmanBalance = walletAccountRepository.findByUserId(craftsmanId)
                .map(WalletAccount::getBalance).orElse(BigDecimal.ZERO);

        // 4. 双边流水
        String typeText = payment.getPayType() == 1 ? "定金" : "尾款";
        WalletTransaction out = new WalletTransaction();
        out.setUserId(payerId);
        out.setTxType("PAY_OUT");
        out.setAmount(amount);
        out.setBalanceAfter(payerBalance);
        out.setStatus(1);
        out.setRefType("CUSTOM_PAY");
        out.setRefId(payment.getId());
        out.setRemark("支付定制订单#" + order.getId() + typeText);
        walletTransactionRepository.save(out);

        WalletTransaction in = new WalletTransaction();
        in.setUserId(craftsmanId);
        in.setTxType("PAY_IN");
        in.setAmount(amount);
        in.setBalanceAfter(craftsmanBalance);
        in.setStatus(1);
        in.setRefType("CUSTOM_PAY");
        in.setRefId(payment.getId());
        in.setRemark("收到定制订单#" + order.getId() + typeText);
        walletTransactionRepository.save(in);

        // 5. 支付流水置已付并推进订单状态（同事务；定金 4→5，尾款 6→7）
        paymentService.markPaid(outTradeNo, "WALLET-" + System.currentTimeMillis(), "WALLET");
    }
}
