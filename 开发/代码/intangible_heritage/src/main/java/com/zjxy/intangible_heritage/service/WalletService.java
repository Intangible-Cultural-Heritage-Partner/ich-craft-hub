package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.WalletAccount;
import com.zjxy.intangible_heritage.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    /** 获取（不存在则创建）钱包账户 */
    WalletAccount getOrCreateAccount(Long userId);

    /** 某用户的钱包流水（时间倒序） */
    List<WalletTransaction> listTransactions(Long userId);

    /** 用户提交充值申请，生成待审核充值流水 */
    WalletTransaction applyRecharge(Long userId, BigDecimal amount);

    /** 管理员审核充值：true 通过并加余额，false 驳回 */
    WalletTransaction auditRecharge(Long txId, Long adminId, boolean approved, String rejectReason);

    /** 待审核充值列表 */
    List<WalletTransaction> pendingRecharges();

    List<WalletTransaction> auditedRecharges();

    /**
     * 钱包支付（同一事务）：
     * 校验余额 → 原子扣减买家 → 加款匠人 → 写双边流水 → 支付流水置已付并推进订单状态。
     * 余额不足抛 IllegalStateException
     */
    void payByWallet(String outTradeNo, Long payerId);
}
