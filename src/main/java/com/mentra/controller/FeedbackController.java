package com.mentra.controller;

import com.mentra.model.Feedback;
import com.mentra.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private SessionFactory sessionFactory;

    @PostMapping(value = "/submit", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestParam int rating,
            @RequestParam String feedbackText,
            @RequestParam(defaultValue = "Help & Support") String moduleName,
            HttpSession httpSession) {

        Map<String, Object> response = new HashMap<>();
        User user = (User) httpSession.getAttribute("user");

        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login to submit feedback");
            return ResponseEntity.status(401).body(response);
        }

        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            Feedback feedback = new Feedback();
            feedback.setUser(user);
            feedback.setModuleName(moduleName);
            feedback.setRating(rating);
            feedback.setComment(feedbackText);

            session.save(feedback);
            session.getTransaction().commit();

            response.put("success", true);
            response.put("message", "Feedback submitted successfully");
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
}
