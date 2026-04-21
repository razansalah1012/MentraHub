package com.mentra.controller;

import com.mentra.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private SessionFactory sessionFactory;

    @GetMapping
    public String settingsPage(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try (Session session = sessionFactory.openSession()) {
            User freshUser = session.get(User.class, user.getUserId());
            if (freshUser != null) {
                user = freshUser;
                httpSession.setAttribute("user", user);
            }

            Query<?> screeningQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM screening_attempts WHERE user_id = :userId")
                    .setParameter("userId", user.getUserId());
            Object sResult = screeningQuery.uniqueResult();
            int screeningsCount = sResult != null ? ((Number) sResult).intValue() : 0;

            Query<?> feedbackQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM feedback WHERE user_id = :userId")
                    .setParameter("userId", user.getUserId());
            Object fResult = feedbackQuery.uniqueResult();
            int feedbackCount = fResult != null ? ((Number) fResult).intValue() : 0;

            long daysActive = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()) + 1;

            model.addAttribute("user", user);
            model.addAttribute("screeningsCount", screeningsCount);
            model.addAttribute("quizzesCount", 0);
            model.addAttribute("chatSessionsCount", 0);
            model.addAttribute("feedbackCount", feedbackCount);
            model.addAttribute("daysActive", daysActive);
        }

        return "settings";
    }

    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveSettings(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false, defaultValue = "false") boolean darkMode,
            @RequestParam(required = false, defaultValue = "medium") String fontSize,
            @RequestParam(required = false, defaultValue = "en") String language,
            HttpSession httpSession) {

        Map<String, Object> response = new HashMap<>();
        User user = (User) httpSession.getAttribute("user");

        if (user == null) {
            response.put("success", false);
            response.put("message", "Session expired. Please login again.");
            return response;
        }

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            User dbUser = session.get(User.class, user.getUserId());
            if (dbUser != null) {
                if (fullName != null && !fullName.trim().isEmpty()) {
                    dbUser.setFullName(fullName.trim());
                }
                dbUser.setDarkMode(darkMode);
                dbUser.setFontSize(fontSize);
                dbUser.setLanguage(language);

                session.update(dbUser);
                session.getTransaction().commit();

                httpSession.setAttribute("user", dbUser);

                response.put("success", true);
                response.put("message", "Settings saved successfully!");
            } else {
                response.put("success", false);
                response.put("message", "User not found.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error saving settings: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/preferences")
    @ResponseBody
    public Map<String, Object> getPreferences(HttpSession httpSession) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) httpSession.getAttribute("user");

        if (user == null) {
            response.put("success", false);
            return response;
        }

        try (Session session = sessionFactory.openSession()) {
            User freshUser = session.get(User.class, user.getUserId());
            if (freshUser != null) {
                response.put("success", true);
                response.put("darkMode", freshUser.isDarkMode());
                response.put("fontSize", freshUser.getFontSize());
                response.put("language", freshUser.getLanguage());
            } else {
                response.put("success", false);
            }
        }

        return response;
    }
}
