package com.mentra.controller;

import com.mentra.dao.UserDao;
import com.mentra.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserDao userDao;

    @Autowired
    public AuthController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        User user = userDao.login(email, password);

        if (user == null) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }

        session.setAttribute("user", user);
        if (user.hasRole("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (user.hasRole("ROLE_COUNSELOR")) {
            return "redirect:/counselor/dashboard";
        } else {
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String matricNo,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/register";
        }
        if (userDao.emailExists(email)) {
            model.addAttribute("error", "Email already registered");
            return "auth/register";
        }
        if (userDao.matricNoExists(matricNo)) {
            model.addAttribute("error", "Matric number already registered");
            return "auth/register";
        }

        userDao.register(matricNo, fullName, email, password);
        model.addAttribute("success", "Registration successful! Please login.");
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
