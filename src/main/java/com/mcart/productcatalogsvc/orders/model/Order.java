package com.mcart.productcatalogsvc.orders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.List;

// Fix: With @DynamoDbBean, DynamoDB annotations must be on EXPLICIT getter methods.
// Lombok @Data generates getters but DynamoDB Enhanced client looks for annotated getters.
// Having both @Data and annotated getters on separate methods causes duplicate getter conflict.
// Solution: Remove @Data, use explicit getters/setters only for annotated fields,
// and keep @Data fields unannotated by overriding only the needed getters below.
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Order {

    private String orderId;
    private String userId;
    private String status;
    private String paymentMethod;
    private String addressId;
    private AddressSnapshot addressSnapshot;
    private List<OrderItem> items;
    private Integer totalItems;
    private Double totalAmount;
    private String currency;
    private String notes;
    private String createdAt;
    private String updatedAt;

    // ─── DynamoDB annotated getters (explicit — no Lombok conflict) ───────────

    @DynamoDbPartitionKey
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @DynamoDbSecondaryPartitionKey(indexNames = {
            "userId-createdAt-index",
            "userId-status-index"
    })
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSecondarySortKey(indexNames = "userId-createdAt-index")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbSecondarySortKey(indexNames = "userId-status-index")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ─── Regular getters/setters for remaining fields ────────────────────────

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public AddressSnapshot getAddressSnapshot() { return addressSnapshot; }
    public void setAddressSnapshot(AddressSnapshot addressSnapshot) { this.addressSnapshot = addressSnapshot; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}