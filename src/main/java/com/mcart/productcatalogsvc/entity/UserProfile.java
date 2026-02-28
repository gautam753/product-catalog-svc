package com.mcart.productcatalogsvc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_profile")
public class UserProfile {

    @Id
    private UUID userId;

    private String firstName;

    private String lastName;

    private LocalDate dob;

    private String gender;

    // JSONB mapped as String
    private String preferences;

    private LocalDateTime updatedAt;
}
