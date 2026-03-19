package com.example.apiorders;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {
    private final OrdersRepository ordersRepository;

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
