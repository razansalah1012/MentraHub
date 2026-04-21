package com.mentra.controller;

import com.mentra.dao.AppointmentDao;
import com.mentra.model.Appointment;
import com.mentra.model.CounselorAvailability;
import com.mentra.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    private final AppointmentDao appointmentDao;

    @Autowired
    public CounselorController(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    @GetMapping("/dashboard")
    public String counselorDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/auth/login";
        }

        // Check if user is a counselor
        if (!user.hasRole("ROLE_COUNSELOR")) {
            return "redirect:/student/dashboard";
        }

        // Get statistics
        long pendingCount = appointmentDao.countPendingAppointmentsByCounselor(user.getUserId());
        long completedCount = appointmentDao.countCompletedAppointmentsByCounselor(user.getUserId());
        long totalCount = appointmentDao.countTotalAppointmentsByCounselor(user.getUserId());

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCount", totalCount);

        // Get appointments lists
        List<Appointment> pendingAppointments = appointmentDao.getPendingAppointmentsByCounselor(user.getUserId());
        model.addAttribute("pendingAppointments", pendingAppointments);

        List<Appointment> allAppointments = appointmentDao.getAppointmentsByCounselor(user.getUserId());
        model.addAttribute("allAppointments", allAppointments);

        // Get availability
        List<CounselorAvailability> availability = appointmentDao.getAvailabilityByCounselor(user.getUserId());
        model.addAttribute("availability", availability);

        model.addAttribute("user", user);
        return "counselor/dashboard";
    }

    @PostMapping("/appointments/{id}/confirm")
    @ResponseBody
    public Map<String, Object> confirmAppointment(@PathVariable("id") Long appointmentId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.hasRole("ROLE_COUNSELOR")) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        Appointment appointment = appointmentDao.getAppointmentById(appointmentId);
        if (appointment == null || !appointment.getCounselor().getUserId().equals(user.getUserId())) {
            response.put("success", false);
            response.put("message", "Appointment not found");
            return response;
        }

        appointment.setStatus(Appointment.Status.CONFIRMED);
        appointmentDao.updateAppointment(appointment);

        response.put("success", true);
        response.put("message", "Appointment confirmed successfully");
        return response;
    }

    @PostMapping("/appointments/{id}/cancel")
    @ResponseBody
    public Map<String, Object> cancelAppointment(@PathVariable("id") Long appointmentId,
            @RequestBody(required = false) Map<String, Object> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.hasRole("ROLE_COUNSELOR")) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        Appointment appointment = appointmentDao.getAppointmentById(appointmentId);
        if (appointment == null || !appointment.getCounselor().getUserId().equals(user.getUserId())) {
            response.put("success", false);
            response.put("message", "Appointment not found");
            return response;
        }

        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : "";
        if (!reason.isEmpty()) {
            appointment.setNotes("Cancelled by counselor: " + reason);
        }

        appointment.setStatus(Appointment.Status.CANCELLED);
        appointmentDao.updateAppointment(appointment);

        response.put("success", true);
        response.put("message", "Appointment cancelled");
        return response;
    }

    @PostMapping("/appointments/{id}/complete")
    @ResponseBody
    public Map<String, Object> completeAppointment(@PathVariable("id") Long appointmentId,
            @RequestBody(required = false) Map<String, Object> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.hasRole("ROLE_COUNSELOR")) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        Appointment appointment = appointmentDao.getAppointmentById(appointmentId);
        if (appointment == null || !appointment.getCounselor().getUserId().equals(user.getUserId())) {
            response.put("success", false);
            response.put("message", "Appointment not found");
            return response;
        }

        String notes = body != null && body.get("notes") != null ? body.get("notes").toString() : "";
        if (!notes.isEmpty()) {
            appointment.setNotes(notes);
        }

        appointment.setStatus(Appointment.Status.COMPLETED);
        appointmentDao.updateAppointment(appointment);

        response.put("success", true);
        response.put("message", "Appointment marked as completed");
        return response;
    }

    @PostMapping("/appointments/{id}/noshow")
    @ResponseBody
    public Map<String, Object> markNoShow(@PathVariable("id") Long appointmentId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.hasRole("ROLE_COUNSELOR")) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        Appointment appointment = appointmentDao.getAppointmentById(appointmentId);
        if (appointment == null || !appointment.getCounselor().getUserId().equals(user.getUserId())) {
            response.put("success", false);
            response.put("message", "Appointment not found");
            return response;
        }

        appointment.setStatus(Appointment.Status.NO_SHOW);
        appointmentDao.updateAppointment(appointment);

        response.put("success", true);
        response.put("message", "Appointment marked as no-show");
        return response;
    }

    @PostMapping("/availability/update")
    @ResponseBody
    public Map<String, Object> updateAvailability(@RequestBody Map<String, Object> request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.hasRole("ROLE_COUNSELOR")) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            String dayStr = request.get("day").toString().toUpperCase();
            CounselorAvailability.DayOfWeek day = CounselorAvailability.DayOfWeek.valueOf(dayStr);
            boolean isAvailable = Boolean.parseBoolean(request.get("isAvailable").toString());

            CounselorAvailability availability = appointmentDao.getAvailabilityByDay(user.getUserId(), day);

            if (availability == null) {
                availability = new CounselorAvailability();
                availability.setCounselor(user);
                availability.setDayOfWeek(day);
                availability.setStartTime(LocalTime.of(9, 0));
                availability.setEndTime(LocalTime.of(17, 0));
            }

            availability.setIsAvailable(isAvailable);

            if (request.containsKey("startTime") && isAvailable) {
                availability.setStartTime(LocalTime.parse(request.get("startTime").toString()));
            }
            if (request.containsKey("endTime") && isAvailable) {
                availability.setEndTime(LocalTime.parse(request.get("endTime").toString()));
            }

            appointmentDao.saveOrUpdateAvailability(availability);

            response.put("success", true);
            response.put("message", "Availability updated");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating availability: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/appointments/history")
    @ResponseBody
    public Map<String, Object> getAppointmentHistory(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.hasRole("ROLE_COUNSELOR")) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        List<Appointment> appointments = appointmentDao.getAppointmentsByCounselor(user.getUserId());
        response.put("success", true);
        response.put("appointments", appointments);
        return response;
    }
}
