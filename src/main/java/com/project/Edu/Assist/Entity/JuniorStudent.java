package com.project.Edu.Assist.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "juniorstudent")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JuniorStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String rollNumber;

    @CreationTimestamp
    private LocalDateTime registeredAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean verified = false;

    @Builder.Default
    @Column(nullable = false)
    private Long doubtCount = 0L;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean autoVerified = false;

    @Builder.Default
    private Long pending = 0L;

    @Builder.Default
    private Long solvedCount = 0L;

    private String photo;

    public Long getDoubtCount() {
        return doubtCount;
    }

    public void setDoubtCount(Long doubtCount) {
        this.doubtCount = doubtCount;
    }
}
