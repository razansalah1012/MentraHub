package com.mentra.controller;

import com.mentra.dao.AppointmentDao;
import com.mentra.dao.UserDao;
import com.mentra.model.Appointment;
import com.mentra.model.TimeSlot;
import com.mentra.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentDao appointmentDao;
    private final UserDao userDao;

    @Autowired
    public AppointmentController(AppointmentDao appointmentDao, UserDao userDao) {
        this.appointmentDao = appointmentDao;
        this.userDao = userDao;
    }

    @GetMapping("")
    public String showAppointmentsPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Get all counselors
        List<User> counselors = appointmentDao.getAllCounselors();
        model.addAttribute("counselors", counselors);

        // Get time slots
        List<TimeSlot> timeSlots = appointmentDao.getAllActiveTimeSlots();
        model.addAttribute("timeSlots", timeSlots);

        // Get user's upcoming appointments
        List<Appointment> upcomingAppointments = appointmentDao.getUpcomingAppointmentsByStudent(user.getUserId());
        model.addAttribute("upcomingAppointments", upcomingAppointments);

        // Get user's past appointments
        List<Appointment> allAppointments = appointmentDao.getAppointmentsByStudent(user.getUserId());
        List<Appointment> pastAppointments = allAppointments.stream()
                .filter(a -> !a.isUpcoming() || a.isCancelled() || a.isCompleted())
                .collect(Collectors.toList());
        model.addAttribute("pastAppointments", pastAppointments);

        // Add today's date for the date picker minimum
        model.addAttribute("minDate", LocalDate.now().toString());

        model.addAttribute("user", user);
        return "appointments";
    }

    @PostMapping("/book")
    @ResponseBody
    public Map<String, Object> bookAppointment(@RequestBody Map<String, Object> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Please log in to book an appointment");
            return response;
        }

        try {
            Long counselorId = Long.parseLong(request.get("counselorId").toString());
            String dateStr = request.get("date").toString();
            Long slotId = Long.parseLong(request.get("slotId").toString());
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";

            // Parse date
            LocalDate appointmentDate = LocalDate.parse(dateStr);

            // Get time slot
            TimeSlot timeSlot = appointmentDao.getTimeSlotById(slotId);
            if (timeSlot == null) {
                response.put("success", false);
                response.put("message", "Invalid time slot selected");
                return response;
            }

            // Check if slot is available
            if (!appointmentDao.isSlotAvailable(counselorId, appointmentDate,
                    timeSlot.getStartTime(), timeSlot.getEndTime())) {
                response.put("success", false);
                response.put("message", "This time slot is no longer available. Please choose another slot.");
                return response;
            }

            // Get counselor
            User counselor = userDao.findById(counselorId);
            if (counselor == null || !counselor.hasRole("ROLE_COUNSELOR")) {
                response.put("success", false);
                response.put("message", "Invalid counselor selected");
                return response;
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setStudent(user);
            appointment.setCounselor(counselor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStartTime(timeSlot.getStartTime());
            appointment.setEndTime(timeSlot.getEndTime());
            appointment.setReason(reason);
            appointment.setStatus(Appointment.Status.PENDING);

            appointmentDao.createAppointment(appointment);

            response.put("success", true);
            response.put("message", "Appointment booked successfully! You will receive a confirmation soon.");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/available-slots")
    @ResponseBody
    public Map<String, Object> getAvailableSlots(@RequestParam Long counselorId,
            @RequestParam String date) {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate appointmentDate = LocalDate.parse(date);
            List<TimeSlot> allSlots = appointmentDao.getAllActiveTimeSlots();

            // Filter out booked slots
            List<Map<String, Object>> availableSlots = allSlots.stream()
                    .filter(slot -> appointmentDao.isSlotAvailable(counselorId, appointmentDate,
                            slot.getStartTime(), slot.getEndTime()))
                    .map(slot -> {
                        Map<String, Object> slotMap = new HashMap<>();
                        slotMap.put("slotId", slot.getSlotId());
                        slotMap.put("label", slot.getSlotLabel());
                        slotMap.put("startTime", slot.getStartTime().toString());
                        slotMap.put("endTime", slot.getEndTime().toString());
                        return slotMap;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("slots", availableSlots);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching available slots");
        }

        return response;
    }

    @PostMapping("/cancel/{id}")
    @ResponseBody
    public Map<String, Object> cancelAppointment(@PathVariable("id") Long appointmentId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Please log in");
            return response;
        }

        Appointment appointment = appointmentDao.getAppointmentById(appointmentId);
        if (appointment == null) {
            response.put("success", false);
            response.put("message", "Appointment not found");
            return response;
        }

        // Check if user owns this appointment
        if (!appointment.getStudent().getUserId().equals(user.getUserId())) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        appointmentDao.updateAppointmentStatus(appointmentId, Appointment.Status.CANCELLED);

        response.put("success", true);
        response.put("message", "Appointment cancelled successfully");

        return response;
    }
}
