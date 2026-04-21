package com.mentra.controller;

import com.mentra.model.CounsellingRequest;
import com.mentra.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/referral")
public class ReferralController {

    @Autowired
    private SessionFactory sessionFactory;

    @GetMapping
    public String referralPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", user);
        return "referral";
    }

    @PostMapping(value = "/counseling/request", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitCounselingRequest(
            @RequestParam String preferredDate,
            @RequestParam String preferredTime,
            @RequestParam String contactMethod,
            @RequestParam(required = false) String reason,
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
            request.setReason(reason != null ? reason : "");
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
            response.put("message", "Error submitting request: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
