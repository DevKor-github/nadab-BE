package com.devkor.ifive.nadab.domain.admin.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/admin/tabs/app-versions";
    }

    @GetMapping("/admin/tabs/app-versions")
    public String adminVersionPage() {
        return "admin/version";
    }
}
