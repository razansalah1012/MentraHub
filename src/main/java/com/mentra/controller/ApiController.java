package com.mentra.controller;

import com.mentra.model.CounsellingRequest;
import com.mentra.model.Feedback;
import com.mentra.model.ScreeningAttempt;
import com.mentra.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private SessionFactory sessionFactory;
    @PostMapping(value = "/counseling/request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitCounselingRequest(
            @RequestParam String preferredDate,
            @RequestParam String preferredTime,
            @RequestParam String contactMethod,
            @RequestParam(required = false, defaultValue = "") String reason,
            HttpSession httpSession) {

        Map<String, Object> response = new HashMap<>();

        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login to submit a request");
            return ResponseEntity.status(401).body(response);
        }

        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            CounsellingRequest request = new CounsellingRequest();
            request.setUser(user);
            request.setPreferredDate(LocalDate.parse(preferredDate));
            request.setPreferredTime(preferredTime);
            request.setContactMethod(contactMethod);
            request.setReason(reason);
            request.setStatus("PENDING");

            session.save(request);
            session.getTransaction().commit();

            response.put("success", true);
            response.put("message", "Request submitted successfully");
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
    @PostMapping(value = "/feedback/submit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestParam int rating,
            @RequestParam String feedbackText,
            @RequestParam(required = false, defaultValue = "Help & Support") String moduleName,
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
    @PostMapping(value = "/screening/submit", produces = MediaType.APPLICATION_JSON_VALUE)
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
}
