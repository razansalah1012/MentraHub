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
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SessionFactory sessionFactory;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null || !user.hasRole("ROLE_ADMIN")) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);

        try (Session session = sessionFactory.openSession()) {
            Query<Long> userCountQuery = session.createQuery(
                    "SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.roleName = 'ROLE_STUDENT'", Long.class);
            Long totalUsers = userCountQuery.uniqueResult();
            model.addAttribute("totalUsers", totalUsers != null ? totalUsers : 0);

            Query<Long> screeningCountQuery = session.createNativeQuery("SELECT COUNT(*) FROM screening_attempts");
            Object screeningResult = screeningCountQuery.uniqueResult();
            Long totalScreenings = screeningResult != null ? ((Number) screeningResult).longValue() : 0L;
            model.addAttribute("totalScreenings", totalScreenings);

            Query<Long> feedbackCountQuery = session.createNativeQuery("SELECT COUNT(*) FROM feedback");
            Object feedbackResult = feedbackCountQuery.uniqueResult();
            Long totalFeedback = feedbackResult != null ? ((Number) feedbackResult).longValue() : 0L;
            model.addAttribute("totalFeedback", totalFeedback);

            Query<Long> counsellingCountQuery = session
                    .createNativeQuery("SELECT COUNT(*) FROM counselling_requests WHERE status = 'PENDING'");
            Object counsellingResult = counsellingCountQuery.uniqueResult();
            Long pendingCounselling = counsellingResult != null ? ((Number) counsellingResult).longValue() : 0L;
            model.addAttribute("pendingCounselling", pendingCounselling);

            List<Object[]> categoryResults = session.createNativeQuery(
                    "SELECT category, COUNT(*) as count FROM screening_attempts GROUP BY category").list();

            Map<String, Long> screeningDistribution = new HashMap<>();
            long totalForPercentage = 0;
            for (Object[] row : categoryResults) {
                String category = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                screeningDistribution.put(category, count);
                totalForPercentage += count;
            }

            Map<String, Integer> distributionPercent = new HashMap<>();
            if (totalForPercentage > 0) {
                for (Map.Entry<String, Long> entry : screeningDistribution.entrySet()) {
                    int percent = (int) Math.round((entry.getValue() * 100.0) / totalForPercentage);
                    distributionPercent.put(entry.getKey(), percent);
                }
            }

            if (distributionPercent.isEmpty()) {
                distributionPercent.put("Normal", 0);
                distributionPercent.put("Mild", 0);
                distributionPercent.put("Moderate", 0);
                distributionPercent.put("Severe", 0);
            }
            model.addAttribute("distributionPercent", distributionPercent);
            model.addAttribute("distributionCounts", screeningDistribution);

            model.addAttribute("weeklyData", getWeeklyActivityData(session));

            Query<Double> avgRatingQuery = session.createNativeQuery("SELECT AVG(rating) FROM feedback");
            Object avgResult = avgRatingQuery.uniqueResult();
            Double avgRating = avgResult != null ? ((Number) avgResult).doubleValue() : 0.0;
            model.addAttribute("avgRating", String.format("%.1f", avgRating));

            List<Object[]> recentFeedback = session.createNativeQuery(
                    "SELECT f.module_name, f.rating, f.comment, f.created_at, u.full_name " +
                            "FROM feedback f JOIN users u ON f.user_id = u.user_id " +
                            "ORDER BY f.created_at DESC LIMIT 5")
                    .list();
            model.addAttribute("recentFeedback", recentFeedback);

            List<Object[]> recentCounselling = session.createNativeQuery(
                    "SELECT c.preferred_date, c.preferred_time, c.contact_method, c.status, u.full_name " +
                            "FROM counselling_requests c JOIN users u ON c.user_id = u.user_id " +
                            "ORDER BY c.created_at DESC LIMIT 5")
                    .list();
            model.addAttribute("recentCounselling", recentCounselling);
        }

        return "admin/dashboard";
    }

    private Map<String, int[]> getWeeklyActivityData(Session session) {
        Map<String, int[]> data = new HashMap<>();

        int[] screenings = new int[7];
        int[] feedback = new int[7];
        int[] counselling = new int[7];

        LocalDate today = LocalDate.now();
        String[] dayLabels = new String[7];

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int index = 6 - i;
            dayLabels[index] = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            Query<?> screeningQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM screening_attempts WHERE DATE(created_at) = :date")
                    .setParameter("date", date.toString());
            Object sResult = screeningQuery.uniqueResult();
            screenings[index] = sResult != null ? ((Number) sResult).intValue() : 0;

            Query<?> feedbackQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM feedback WHERE DATE(created_at) = :date")
                    .setParameter("date", date.toString());
            Object fResult = feedbackQuery.uniqueResult();
            feedback[index] = fResult != null ? ((Number) fResult).intValue() : 0;

            Query<?> counsellingQuery = session.createNativeQuery(
                    "SELECT COUNT(*) FROM counselling_requests WHERE DATE(created_at) = :date")
                    .setParameter("date", date.toString());
            Object cResult = counsellingQuery.uniqueResult();
            counselling[index] = cResult != null ? ((Number) cResult).intValue() : 0;
        }

        data.put("screenings", screenings);
        data.put("feedback", feedback);
        data.put("counselling", counselling);
        data.put("labels", Arrays.stream(dayLabels).mapToInt(s -> 0).toArray());

        return data;
    }
}
