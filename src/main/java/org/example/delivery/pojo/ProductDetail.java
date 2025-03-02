package org.example.delivery.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDetail {
    private String productName;
    private String specification;
    private String material;
    private Double pieceWeightTon;
    private Integer quantity;
    private Double totalWeightTon;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String remark;
}
