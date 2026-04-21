package com.mentra.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "screening_attempts")
public class ScreeningAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screening_id")
    private Long screeningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "screening_type", nullable = false, length = 30)
    private String screeningType;

    @Column(name = "final_score", nullable = false)
    private Integer finalScore;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    public ScreeningAttempt() {
    }

    public ScreeningAttempt(User user, String screeningType, Integer finalScore, String category) {
        this.user = user;
        this.screeningType = screeningType;
        this.finalScore = finalScore;
        this.category = category;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    public Long getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getScreeningType() {
        return screeningType;
    }

    public void setScreeningType(String screeningType) {
        this.screeningType = screeningType;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
