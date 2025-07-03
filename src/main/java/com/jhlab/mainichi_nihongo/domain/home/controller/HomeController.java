package com.jhlab.mainichi_nihongo.domain.home.controller;

import com.jhlab.mainichi_nihongo.domain.subscribe.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final SubscribeService subscribeService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("subscriberCount", subscribeService.getSubscribersCount());
        return "index";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            subscribeService.subscribe(email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "구독이 완료되었습니다! 매일 아침 새로운 일본어와 함께하세요. 🌸");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/unsubscribe")
    public String unsubscribePage(Model model) {
        model.addAttribute("subscriberCount", subscribeService.getSubscribersCount());
        return "unsubscribe";
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String email, Model model) {
        try {
            subscribeService.unsubscribe(email);
            model.addAttribute("successMessage",
                    "구독이 취소되었습니다. 그동안 이용해 주셔서 감사합니다. 🙏");
        } catch (Exception e) {
            model.addAttribute("subscriberCount", subscribeService.getSubscribersCount());
        }
        return "unsubscribe";
    }
}
