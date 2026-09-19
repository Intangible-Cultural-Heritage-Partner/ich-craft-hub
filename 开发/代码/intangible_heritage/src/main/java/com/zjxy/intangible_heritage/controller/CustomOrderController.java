package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.CustomMessage;
import com.zjxy.intangible_heritage.entity.CustomOrder;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.CustomMessageService;
import com.zjxy.intangible_heritage.service.CustomOrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定制对接模块：
 *   GET  /custom/apply        申请表单（USER）
 *   POST /custom/apply        提交申请（USER）
 *   GET  /custom/myApply      我发起的（USER）
 *   GET  /custom/myCraft      发给我的（CRAFTSMAN）
 *   POST /custom/accept/{id}  接受
 *   POST /custom/reject/{id}  拒绝
 *   POST /custom/finish/{id}  完结
 *   GET  /custom/chat/{id}    聊天页
 *   POST /custom/chat/{id}    发消息
 *   GET  /custom/messages/{id} AJAX 拉取消息（轮询）
 */
@Controller
@RequiredArgsConstructor
public class CustomOrderController {

    private final CustomOrderService customOrderService;
    private final CustomMessageService customMessageService;
    private final UserRepository userRepository;

    // ---------- 申请表单 ----------
    @GetMapping("/custom/apply")
    public String applyForm(Model model) {
        // 列出所有匠人供用户选择
        List<User> craftsmen = userRepository.findByRole("1");
        model.addAttribute("craftsmen", craftsmen);
        model.addAttribute("order", new CustomOrder());
        return "custom/apply";
    }

    @PostMapping("/custom/apply")
    public String submitApply(@RequestParam Long craftsmanId,
                              @RequestParam String workDesc,
                              @RequestParam(required = false) String materialRequire,
                              @RequestParam(required = false) String budget,
                              @RequestParam(required = false) String expectFinishTime,
                              @RequestParam(required = false) String remark,
                              HttpSession session,
                              RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        CustomOrder order = new CustomOrder();
        order.setApplyUserId(loginUser.getId());
        order.setCraftsmanId(craftsmanId);
        order.setWorkDesc(workDesc);
        order.setMaterialRequire(materialRequire);
        order.setBudget(budget);
        order.setExpectFinishTime(expectFinishTime);
        order.setRemark(remark);
        customOrderService.apply(order);
        ra.addFlashAttribute("msg", "申请已提交，等待匠人回应");
        return "redirect:/custom/myApply";
    }

    // ---------- 用户：我发起的 ----------
    @GetMapping("/custom/myApply")
    public String myApply(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<CustomOrder> orders = customOrderService.myApply(loginUser.getId());
        // 构造匠人id -> 昵称映射，供页面显示
        Map<Long, String> craftsmanNames = new HashMap<>();
        for (CustomOrder o : orders) {
            craftsmanNames.computeIfAbsent(o.getCraftsmanId(),
                    id -> userRepository.findById(id).map(User::getUsername).orElse("未知匠人"));
        }
        model.addAttribute("orders", orders);
        model.addAttribute("partyNames", craftsmanNames);
        return "custom/myApply";
    }

    // ---------- 匠人：发给我的 ----------
    @GetMapping("/custom/myCraft")
    public String myCraft(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<CustomOrder> orders = customOrderService.myCraft(loginUser.getId());
        Map<Long, String> applyUserNames = new HashMap<>();
        for (CustomOrder o : orders) {
            applyUserNames.computeIfAbsent(o.getApplyUserId(),
                    id -> userRepository.findById(id).map(User::getUsername).orElse("未知用户"));
        }
        model.addAttribute("orders", orders);
        model.addAttribute("partyNames", applyUserNames);
        return "custom/myCraft";
    }

    // ---------- 状态流转 ----------
    @PostMapping("/custom/accept/{id}")
    public String accept(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        try {
            customOrderService.accept(id, loginUser.getId());
            ra.addFlashAttribute("msg", "已接受，进入沟通");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "操作失败：" + e.getMessage());
        }
        return "redirect:/custom/myCraft";
    }

    @PostMapping("/custom/reject/{id}")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String refuseReason,
                         HttpSession session,
                         RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        try {
            customOrderService.reject(id, loginUser.getId(), refuseReason);
            ra.addFlashAttribute("msg", "已拒绝");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "操作失败：" + e.getMessage());
        }
        return "redirect:/custom/myCraft";
    }

    @PostMapping("/custom/finish/{id}")
    public String finish(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        try {
            customOrderService.finish(id, loginUser.getId());
            ra.addFlashAttribute("msg", "已完结");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "操作失败：" + e.getMessage());
        }
        return "redirect:/custom/chat/" + id;
    }

    // ---------- 聊天页 ----------
    @GetMapping("/custom/chat/{id}")
    public String chatPage(@PathVariable Long id, HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        CustomOrder order = customOrderService.getForUserOrCraftsman(id, loginUser.getId());
        // 对方信息
        Long otherId = loginUser.getId().equals(order.getApplyUserId())
                ? order.getCraftsmanId()
                : order.getApplyUserId();
        User other = userRepository.findById(otherId).orElse(null);
        List<CustomMessage> messages = customMessageService.listByOrder(id);

        model.addAttribute("order", order);
        model.addAttribute("other", other);
        model.addAttribute("messages", messages);
        model.addAttribute("loginUserId", loginUser.getId());
        return "custom/chat";
    }

    @PostMapping("/custom/chat/{id}")
    public String postMessage(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        try {
            customMessageService.send(id, loginUser.getId(), content);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "发送失败：" + e.getMessage());
        }
        return "redirect:/custom/chat/" + id;
    }

    // ---------- AJAX 轮询拉消息 ----------
    @GetMapping("/custom/messages/{id}")
    @ResponseBody
    public List<CustomMessage> messages(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        // 必须是当事人
        customOrderService.getForUserOrCraftsman(id, loginUser.getId());
        return customMessageService.listByOrder(id);
    }
}
