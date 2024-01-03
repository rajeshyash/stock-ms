package com.rajesh.repository;

import com.rajesh.entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface StockRepository extends JpaRepository<WareHouse,Long> {
    List<WareHouse> findByItem(String item);
}
