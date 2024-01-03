package com.rajesh.service;

import com.rajesh.dto.DeliveryEvent;
import com.rajesh.dto.PaymentEvent;
import com.rajesh.entity.WareHouse;
import com.rajesh.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReversesStock {
    @Autowired
    private StockRepository repository;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = "reversed-stock", groupId = "stock-group")
    public void reverseStock(DeliveryEvent deliveryEvent) {
        System.out.println("Inside reverse stock-ms for order "+deliveryEvent);
        try {
            List<WareHouse> wareHousesList = this.repository.findByItem(deliveryEvent.getOrder().getItem());

            wareHousesList.forEach(i -> {
                i.setQuantity(i.getQuantity() + deliveryEvent.getOrder().getQuantity());
                repository.save(i);
            });

            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrder(deliveryEvent.getOrder());
            paymentEvent.setType("PAYMENT_REVERSED");
            kafkaTemplate.send("reversed-payments", paymentEvent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
