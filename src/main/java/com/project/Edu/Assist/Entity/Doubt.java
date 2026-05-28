package com.project.Edu.Assist.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Doubt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String doubtPic;
    private String description;

    @CreationTimestamp
    private LocalDateTime doubtRaisedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 50)
    private DoubtStatus status = DoubtStatus.PENDING;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isVerified = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "junior_id", nullable = false)
    private JuniorStudent asker;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_id")
    private SeniorStudent solver;

    private String solutionText;
    private String solutionPic;
    private String comments;
    private Double rating;

    @UpdateTimestamp
    private LocalDateTime solutionSubmittedAt;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean chatEnabled = false;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean chatClosed = false;

    private LocalDateTime chatClosedAt;
}