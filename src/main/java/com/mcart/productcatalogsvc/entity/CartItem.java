// entity/CartItem.java
package com.mcart.productcatalogsvc.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Table("cart_items")
public class CartItem {

    @Id
    private Long id;

    @Column("cart_id")
    private Long cartId;

    @Column("product_id")
    private String productId;

    @Column("variant_id")
    private String variantId;

    @Column("quantity")
    private Integer quantity;

    @Column("price_at_addition")
    private BigDecimal priceAtAddition;

    @CreatedDate
    @Column("added_at")
    private Instant addedAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    // --- Transient: not persisted, set by service before calling repository ---
    @Transient
    private Cart.OwnerType ownerType;   // USER or GUEST

    @Transient
    private String ownerId;             // userId or guestToken
}