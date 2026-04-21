package com.mentra.controller;

import com.mentra.model.ScreeningAttempt;
import com.mentra.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/screening")
public class ScreeningController {

    @Autowired
    private SessionFactory sessionFactory;

    @GetMapping
    public String screeningPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", user);
        return "screening";
    }

    @GetMapping("/quiz/{type}")
    public String quizPage(@PathVariable String type, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (!type.equals("dass21") && !type.equals("phq9")) {
            return "redirect:/screening";
        }

        model.addAttribute("user", user);
        model.addAttribute("quizType", type);
        return "screening-quiz";
    }

    @PostMapping(value = "/submit", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitScreening(
            @RequestParam String screeningType,
            @RequestParam int score,
            @RequestParam String category,
            HttpSession httpSession) {

        Map<String, Object> response = new HashMap<>();
        User user = (User) httpSession.getAttribute("user");

        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login");
            return ResponseEntity.status(401).body(response);
        }

        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            ScreeningAttempt attempt = new ScreeningAttempt();
            attempt.setUser(user);
            attempt.setScreeningType(screeningType);
            attempt.setFinalScore(score);
            attempt.setCategory(category);

            session.save(attempt);
            session.getTransaction().commit();

            response.put("success", true);
            response.put("message", "Screening saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @GetMapping("/result")
    public String resultPage(@RequestParam String type, @RequestParam int score, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("quizType", type);
        model.addAttribute("score", score);
        return "screening-result";
    }
}
