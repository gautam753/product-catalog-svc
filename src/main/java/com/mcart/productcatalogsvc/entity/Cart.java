// entity/Cart.java
package com.mcart.productcatalogsvc.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("carts")
public class Cart {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;          // populated if ownerType == USER

    @Column("guest_token")
    private String guestToken;      // populated if ownerType == GUEST

    @Column("owner_type")
    private OwnerType ownerType;

    @Column("expires_at")
    private Instant expiresAt;      // only set for GUEST carts

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    public enum OwnerType {
        USER, GUEST
    }
}
