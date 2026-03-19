package com.example.apiorders;

import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {
    private final OrdersRepository ordersRepository;

    @KafkaListener(topics = "product-stock-reduced", groupId = "orders-group-1")
    @Transactional
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) Long key,
            @Payload Long ordersIdx
    ) {
        Orders orders = ordersRepository.findById(ordersIdx).orElseThrow();
        orders.setStatus("COMPLETE");
    }

    @PostMapping("/create")
    public ResponseEntity create(
            @RequestBody OrdersDto.OrdersReq dto) {
       Orders result =  ordersRepository.save(dto.toEntity());

        return ResponseEntity.ok(OrdersDto.OrdersRes.from(result));
    }

    @GetMapping("/{ordersIdx}")
    public ResponseEntity get(@PathVariable Long ordersIdx) {
        Orders result =  ordersRepository.findById(ordersIdx).orElseThrow();

        return ResponseEntity.ok(OrdersDto.OrdersRes.from(result));
    }


}
