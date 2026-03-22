// entity/WishlistItem.java
package com.mcart.productcatalogsvc.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wishlist_items")
public class WishlistItem {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;          // replaces PK = "USER#<userId>"

    @Column("product_id")
    private String productId;       // replaces SK = "ITEM#<productId>#<variantId>"

    @Column("variant_id")
    private String variantId;       // nullable — replaces optional SK segment

    @Column("priority")
    private String priority;        // low / medium / high

    @Column("notes")
    private String notes;

    @CreatedDate
    @Column("added_at")
    private Instant addedAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}