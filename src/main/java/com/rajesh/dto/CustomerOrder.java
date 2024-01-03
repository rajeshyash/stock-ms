package com.rajesh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrder {
    private String item;
    private int quantity;
    private double amount;
    private String paymentMode;
    private long orderId;
    private String address;

}
