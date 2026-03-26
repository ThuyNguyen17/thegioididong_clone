package com.example.demo1.controller;

import com.example.demo1.model.User;
import com.example.demo1.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller // Đánh dấu lớp này là một Controller trong Spring MVC.
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "users/login";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User()); // Thêm một đối tượng User mới vàomodel
        return "users/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           @NotNull BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "users/register";
        }
        
        try {
            userService.save(user);
            userService.setDefaultRole(user.getUsername());
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            System.err.println("Registration error message: " + e.getMessage());
            
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate entry") || msg.contains("constraintviolationexception")) {
                if (msg.contains("username")) {
                    model.addAttribute("errors", new String[]{"Tên đăng nhập này đã có người sử dụng. Vui lòng chọn tên khác."});
                } else if (msg.contains("email")) {
                    model.addAttribute("errors", new String[]{"Email này đã được đăng ký. Vui lòng sử dụng email khác."});
                } else if (msg.contains("phone")) {
                    model.addAttribute("errors", new String[]{"Số điện thoại này đã được đăng ký. Vui lòng sử dụng số khác."});
                } else {
                    model.addAttribute("errors", new String[]{"Lỗi: Thông tin bạn nhập đã tồn tại trong hệ thống (Email/SĐT/Username)."});
                }
            } else {
                model.addAttribute("errors", new String[]{"Lỗi máy chủ: " + e.getMessage()});
            }
            return "users/register";
        }
    }
}