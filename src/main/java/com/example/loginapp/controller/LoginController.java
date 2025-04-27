package com.example.loginapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.loginapp.model.User;
import com.example.loginapp.service.UserService;

import jakarta.validation.Valid;

@Controller
public class LoginController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }
    
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("user") User user, 
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "login";
        }
        
        if (userService.authenticate(user)) {
            model.addAttribute("message", "Login successful!");
            return "login";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }
}
