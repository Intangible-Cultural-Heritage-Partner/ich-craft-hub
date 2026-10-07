package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.repository.CustomMessageRepository;
import com.zjxy.intangible_heritage.repository.CustomOrderRepository;
import com.zjxy.intangible_heritage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员：平台数据概览（简化实现）
 */
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final CustomOrderRepository customOrderRepository;
    private final CustomMessageRepository customMessageRepository;

    @GetMapping("/admin/stat")
    public String stat(Model model) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("userCount", userRepository.count());
        stat.put("craftsmanCount", userRepository.findByRole("CRAFTSMAN").size());
        stat.put("orderCount", customOrderRepository.count());
        stat.put("messageCount", customMessageRepository.count());

        // 各状态定制单数量（内存遍历，简化实现）
        Map<String, Long> orderStatus = new HashMap<>();
        orderStatus.put("新建", 0L);
        orderStatus.put("已拒绝", 0L);
        orderStatus.put("沟通中", 0L);
        orderStatus.put("需求完结", 0L);
        customOrderRepository.findAll().forEach(o -> {
            String key = switch (o.getOrderStatus()) {
                case 0 -> "新建";
                case 1 -> "已拒绝";
                case 2 -> "沟通中";
                case 3 -> "需求完结";
                default -> "未知";
            };
            orderStatus.merge(key, 1L, Long::sum);
        });
        stat.put("orderStatusStat", orderStatus);

        model.addAttribute("stat", stat);
        return "admin/stat";
    }
}
