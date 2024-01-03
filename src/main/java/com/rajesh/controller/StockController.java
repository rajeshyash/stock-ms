package com.rajesh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajesh.dto.CustomerOrder;
import com.rajesh.dto.DeliveryEvent;
import com.rajesh.dto.PaymentEvent;
import com.rajesh.dto.Stock;
import com.rajesh.entity.WareHouse;
import com.rajesh.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StockController {

    @Autowired
    private StockRepository repository;

    @Autowired
    private KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaPaymentTemplate;

    @KafkaListener(topics = "new-payments", groupId = "payments-group")
    public void updateStock(PaymentEvent paymentEvent) {
        System.out.println("Inside update inventory for order "+paymentEvent);
        DeliveryEvent event = new DeliveryEvent();
        CustomerOrder order = paymentEvent.getOrder();
        try {
            List<WareHouse> inventories = repository.findByItem(order.getItem());
            if (inventories.isEmpty()) {
                System.out.println("Stock not exist so reverting the order");
                throw new Exception("Stock not available");
            }
            inventories.forEach(i -> {
                if(order.getQuantity() > i.getQuantity()){
                    System.out.println("Stock not exist so reverting the order");
                    try {
                        throw new Exception("Stock not available");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                i.setQuantity(i.getQuantity() - order.getQuantity());

                repository.save(i);
            });

            event.setType("STOCK_UPDATED");
            event.setOrder(paymentEvent.getOrder());
            kafkaTemplate.send("new-stock", event);
        } catch (Exception e) {
            PaymentEvent pe = new PaymentEvent();
            pe.setOrder(order);
            pe.setType("PAYMENT_REVERSED");
            kafkaPaymentTemplate.send("reversed-payments", pe);
            System.out.println("PAYMENT_REVERSED");
        }
    }


    @PostMapping("/addItems")
    public void addItems(@RequestBody Stock stock) {
        List<WareHouse> items = repository.findByItem(stock.getItem());
        if (items.size() > 0) {
            items.forEach(i -> {
                i.setQuantity(stock.getQuantity() + i.getQuantity());
                repository.save(i);
            });
        } else {
            WareHouse i = new WareHouse();
            i.setItem(stock.getItem());
            i.setQuantity(stock.getQuantity());
            repository.save(i);
        }
    }
}
