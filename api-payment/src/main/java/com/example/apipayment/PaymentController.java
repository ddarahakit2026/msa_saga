package com.example.apipayment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.portone.sdk.server.payment.CancelPaymentResponse;
import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.Payment;
import io.portone.sdk.server.payment.PaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CrossOrigin(originPatterns = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    private final OrdersFeignClient ordersFeignClient;
    private final ProductFeignClient productFeignClient;
    private final PaymentClient pg;
    private final KafkaTemplate<Long, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = "product-stock-reduce-failed", groupId = "payment-group-1"
    ,properties = "spring.json.value.default.type:com.example.apipayment.PaymentDto.OrdersRes")
    @Transactional
    public void productStockReduceFailedConsume(
            @Header(KafkaHeaders.RECEIVED_KEY) Long key,
            @Payload PaymentDto.OrdersRes dto
    ) {
        LocalPayment localPayment = paymentRepository.findByOrdersIdx(dto.getIdx()).orElseThrow();
        localPayment.setStatus("CANCELLED");

        try {
            CompletableFuture<CancelPaymentResponse> future = pg.cancelPayment(
                    localPayment.getPgPaymentId(), null, null, null,
                    "product-stock-reduce-failed", null, null, null, null, null, null
            );
            future.join();
        } catch (Exception e) {
            System.out.println("실패");
        }

        kafkaTemplate.send("payment-canceled", localPayment.getOrdersIdx(), localPayment);

    }

    @PostMapping("/verify")
    public ResponseEntity verify(@RequestBody PaymentDto.VerifyReq dto) throws JsonProcessingException {
        // 트랜잭션 시작
        System.out.println(dto.getPaymentId());
        System.out.println(dto.getOrdersIdx());

        // api-orders 에서 주문 정보 조회
        PaymentDto.OrdersRes ordersRes = ordersFeignClient.getOrders(dto.getOrdersIdx());
        // PG사에 결제 정보 요청
        CompletableFuture<Payment> completableFuture = pg.getPayment(dto.getPaymentId());
        Payment payment = completableFuture.join();

        if (payment instanceof PaidPayment paidPayment) {
            Map<String, Object> customData = new ObjectMapper().readValue(
                    paidPayment.getCustomData(), Map.class
            );

            List<Integer> list = (List<Integer>) customData.get("productIdxList");

            List<PaymentDto.ProductRes> productList = productFeignClient.list();
            Map<Long, Integer> productPriceMap = productList.stream()
                    .collect(Collectors.toMap(
                            PaymentDto.ProductRes::getIdx,
                            PaymentDto.ProductRes::getPrice
                    ));
            System.out.println(productList.toArray());

            int totalOrderedPrice = 0;
            for (PaymentDto.OrdersItemRes ordersItem : ordersRes.getOrdersItems()) {
                int quantity = ordersItem.getQuantity();
                Long productIdx = ordersItem.getProductIdx();
                int price = productPriceMap.get(productIdx);
                totalOrderedPrice = totalOrderedPrice + quantity * price;
            }

            // 검증 완료 후 결제 정보 저장
            if (totalOrderedPrice == paidPayment.getAmount().getTotal()) {
                LocalPayment localPayment = com.example.apipayment.LocalPayment.builder()
                        .ordersIdx(dto.getOrdersIdx())
                        .pgPaymentId(dto.getPaymentId())
                        .amount(totalOrderedPrice)
                        .status("PAID")
                        .build();
                paymentRepository.save(localPayment);
                // 결제 완료 이벤트 발행  (payment-completed 토픽, 상품 idx 및 재고 정보를 페이로드로 전송)
                kafkaTemplate.send("payment-completed", ordersRes.getIdx(), ordersRes);
                System.out.println("성공");
            } else {
                // 결제 취소 및 취소 이벤트 발행
                System.out.println("실패");
            }

        }


        return ResponseEntity.ok("성공");
    }
}