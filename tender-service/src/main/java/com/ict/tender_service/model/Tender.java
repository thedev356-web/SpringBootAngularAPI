//package com.ict.tender_service.model;

package com.ict.tender_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tenders")
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String organization;

    private String district;

    private String category;

    private String tenderNumber;

    private Double estimatedValue;

    private LocalDate publishedDate;

    private LocalDate deadline;

    private String documentUrl;

    private String sourceUrl;

    private String source;

    @Enumerated(EnumType.STRING)
    private TenderStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TenderStatus.ACTIVE;
    }

    public enum TenderStatus {
        ACTIVE, EXPIRED, CANCELLED
    }
}