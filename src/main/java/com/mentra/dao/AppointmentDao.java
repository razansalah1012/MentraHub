package com.mentra.dao;

import com.mentra.model.Appointment;
import com.mentra.model.CounselorAvailability;
import com.mentra.model.TimeSlot;
import com.mentra.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
@Transactional
public class AppointmentDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public AppointmentDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    // ==================== Appointment Methods ====================

    public void createAppointment(Appointment appointment) {
        getCurrentSession().save(appointment);
    }

    public Appointment getAppointmentById(Long appointmentId) {
        return getCurrentSession().get(Appointment.class, appointmentId);
    }

    public void updateAppointment(Appointment appointment) {
        getCurrentSession().update(appointment);
    }

    public void updateAppointmentStatus(Long appointmentId, Appointment.Status status) {
        Appointment appointment = getCurrentSession().get(Appointment.class, appointmentId);
        if (appointment != null) {
            appointment.setStatus(status);
            getCurrentSession().update(appointment);
        }
    }

    public List<Appointment> getAppointmentsByStudent(Long studentId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.student.userId = :studentId ORDER BY a.appointmentDate DESC, a.startTime DESC",
                Appointment.class)
                .setParameter("studentId", studentId)
                .list();
    }

    public List<Appointment> getUpcomingAppointmentsByStudent(Long studentId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.student.userId = :studentId " +
                        "AND a.appointmentDate >= :today " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY a.appointmentDate ASC, a.startTime ASC",
                Appointment.class)
                .setParameter("studentId", studentId)
                .setParameter("today", LocalDate.now())
                .list();
    }

    public List<Appointment> getAppointmentsByCounselor(Long counselorId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.counselor.userId = :counselorId ORDER BY a.appointmentDate DESC, a.startTime DESC",
                Appointment.class)
                .setParameter("counselorId", counselorId)
                .list();
    }

    public List<Appointment> getUpcomingAppointmentsByCounselor(Long counselorId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.counselor.userId = :counselorId " +
                        "AND a.appointmentDate >= :today " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY a.appointmentDate ASC, a.startTime ASC",
                Appointment.class)
                .setParameter("counselorId", counselorId)
                .setParameter("today", LocalDate.now())
                .list();
    }

    public List<Appointment> getTodaysAppointmentsByCounselor(Long counselorId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.counselor.userId = :counselorId " +
                        "AND a.appointmentDate = :today " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY a.startTime ASC",
                Appointment.class)
                .setParameter("counselorId", counselorId)
                .setParameter("today", LocalDate.now())
                .list();
    }

    public List<Appointment> getPendingAppointmentsByCounselor(Long counselorId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.counselor.userId = :counselorId " +
                        "AND a.status = 'PENDING' " +
                        "ORDER BY a.appointmentDate ASC, a.startTime ASC",
                Appointment.class)
                .setParameter("counselorId", counselorId)
                .list();
    }

    public List<Appointment> getAppointmentsByDateAndCounselor(LocalDate date, Long counselorId) {
        return getCurrentSession().createQuery(
                "FROM Appointment a WHERE a.counselor.userId = :counselorId " +
                        "AND a.appointmentDate = :date " +
                        "AND a.status NOT IN ('CANCELLED') " +
                        "ORDER BY a.startTime ASC",
                Appointment.class)
                .setParameter("counselorId", counselorId)
                .setParameter("date", date)
                .list();
    }

    public boolean isSlotAvailable(Long counselorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Long count = getCurrentSession().createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.counselor.userId = :counselorId " +
                        "AND a.appointmentDate = :date " +
                        "AND a.status NOT IN ('CANCELLED') " +
                        "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
                        "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
                        "OR (a.startTime >= :startTime AND a.endTime <= :endTime))",
                Long.class)
                .setParameter("counselorId", counselorId)
                .setParameter("date", date)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .uniqueResult();
        return count == null || count == 0;
    }

    // ==================== Counselor Availability Methods ====================

    public List<CounselorAvailability> getAvailabilityByCounselor(Long counselorId) {
        return getCurrentSession().createQuery(
                "FROM CounselorAvailability ca WHERE ca.counselor.userId = :counselorId " +
                        "ORDER BY ca.dayOfWeek",
                CounselorAvailability.class)
                .setParameter("counselorId", counselorId)
                .list();
    }

    public CounselorAvailability getAvailabilityByDay(Long counselorId, CounselorAvailability.DayOfWeek day) {
        return getCurrentSession().createQuery(
                "FROM CounselorAvailability ca WHERE ca.counselor.userId = :counselorId " +
                        "AND ca.dayOfWeek = :day",
                CounselorAvailability.class)
                .setParameter("counselorId", counselorId)
                .setParameter("day", day)
                .uniqueResult();
    }

    public void saveOrUpdateAvailability(CounselorAvailability availability) {
        getCurrentSession().saveOrUpdate(availability);
    }

    // ==================== Time Slots Methods ====================

    public List<TimeSlot> getAllActiveTimeSlots() {
        return getCurrentSession().createQuery(
                "FROM TimeSlot ts WHERE ts.isActive = true ORDER BY ts.startTime",
                TimeSlot.class)
                .list();
    }

    public TimeSlot getTimeSlotById(Long slotId) {
        return getCurrentSession().get(TimeSlot.class, slotId);
    }

    // ==================== Counselor Methods ====================

    public List<User> getAllCounselors() {
        return getCurrentSession().createQuery(
                "SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.roleName = 'ROLE_COUNSELOR' ORDER BY u.fullName",
                User.class)
                .list();
    }

    // ==================== Statistics Methods ====================

    public long countTotalAppointmentsByCounselor(Long counselorId) {
        Long count = getCurrentSession().createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.counselor.userId = :counselorId",
                Long.class)
                .setParameter("counselorId", counselorId)
                .uniqueResult();
        return count != null ? count : 0;
    }

    public long countPendingAppointmentsByCounselor(Long counselorId) {
        Long count = getCurrentSession().createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.counselor.userId = :counselorId AND a.status = 'PENDING'",
                Long.class)
                .setParameter("counselorId", counselorId)
                .uniqueResult();
        return count != null ? count : 0;
    }

    public long countCompletedAppointmentsByCounselor(Long counselorId) {
        Long count = getCurrentSession().createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.counselor.userId = :counselorId AND a.status = 'COMPLETED'",
                Long.class)
                .setParameter("counselorId", counselorId)
                .uniqueResult();
        return count != null ? count : 0;
    }

    public long countTodaysAppointmentsByCounselor(Long counselorId) {
        Long count = getCurrentSession().createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.counselor.userId = :counselorId " +
                        "AND a.appointmentDate = :today AND a.status IN ('PENDING', 'CONFIRMED')",
                Long.class)
                .setParameter("counselorId", counselorId)
                .setParameter("today", LocalDate.now())
                .uniqueResult();
        return count != null ? count : 0;
    }
}
