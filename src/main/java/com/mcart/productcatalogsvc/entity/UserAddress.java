package com.mcart.productcatalogsvc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_address")
public class UserAddress {

    @Id
    @Column("address_id")
    private UUID addressId;

    @Column("user_id")
    private UUID userId;

    private String type;

    // JSONB mapped as String
    @Column("address_json")
    private Map<String, Object> addressJson;

    @Column("is_default")
    private Boolean isDefault;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
