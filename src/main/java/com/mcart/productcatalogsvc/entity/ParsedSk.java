package com.mcart.productcatalogsvc.entity;

//ParsedSk.java — decodes "ITEM#<productId>#<variantId>"
public record ParsedSk(String productId, String variantId) {

 public static ParsedSk from(String sk) {
     // SK format: ITEM#<productId>#<variantId>
     String[] parts = sk.split("#", 3);
     if (parts.length != 3 || !"ITEM".equals(parts[0])) {
         throw new IllegalArgumentException("Unknown SK format: " + sk);
     }
     return new ParsedSk(parts[1], parts[2]);
 }
}
