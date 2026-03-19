package com.example.apipayment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    @PostMapping("/verify")
    public ResponseEntity verify(@RequestBody PaymentDto.VerifyReq dto) {
        // 트랜잭션 시작
        System.out.println(dto.getPaymentId());
        System.out.println(dto.getOrdersIdx());

        return ResponseEntity.ok("성공");
    }
}