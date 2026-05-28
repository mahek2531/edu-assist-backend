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
public class JuniorStudent {

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

    // FIXED: renamed from 'isVerified' → 'verified'
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean verified = false;

    @Column(nullable = false)
    private Long doubtCount = 0L;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean autoVerified = false;



    private Long pending = 0L;
    private Long solvedCount = 0L;

    private String photo;

    // Optional: keeping explicit getter/setter (not required due to Lombok, but safe)
    public Long getDoubtCount() {
        return doubtCount;
    }

    public void setDoubtCount(Long doubtCount) {
        this.doubtCount = doubtCount;
    }
}