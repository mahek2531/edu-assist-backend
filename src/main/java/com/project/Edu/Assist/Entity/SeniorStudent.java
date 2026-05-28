package com.project.Edu.Assist.Entity;

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
public class SeniorStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String rollNumber;

    @CreationTimestamp
    private LocalDateTime registeredAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean verified = false;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean autoVerified = false;

    private Long solvedCount = 0L;
    private String photo;

    @Column(name = "expertise_subjects", length = 500)
    private String expertiseSubjects;
}
