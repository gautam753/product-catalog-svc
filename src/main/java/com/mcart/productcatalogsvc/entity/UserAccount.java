package com.mcart.productcatalogsvc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_account")
public class UserAccount {

    @Id
    @Column("user_id")
    private UUID userId;

    private String name;

    private String email;

    private String phone;

    @Column("email_verified")
    private Boolean emailVerified;

    @Column("phone_verified")
    private Boolean phoneVerified;

    @Column("hashed_password")
    private String hashedPassword;

    private String status;

    @Column("account_type")
    private String accountType;

    @Column("mfa_setting")
    private String mfaSetting;

    @Column("mfa_methods")
    private String mfaMethods;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
