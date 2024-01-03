package com.rajesh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "WARE_HOUSE_TBL")
public class WareHouse {
    @Id
    @GeneratedValue
    private long id;
    @Column
    private int quantity;
    @Column
    private String item;
}
