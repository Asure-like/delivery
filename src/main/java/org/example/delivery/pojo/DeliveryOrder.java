package org.example.delivery.pojo;

import lombok.Data;

import java.util.List;

@Data
public class DeliveryOrder {
    private String companyName;
    private String issueDate;
    private String orderNumber;
    private List<ProductDetail> productDetails;
    private String issuer;
    private String receiver;
    private String deliveryAddress;
}
