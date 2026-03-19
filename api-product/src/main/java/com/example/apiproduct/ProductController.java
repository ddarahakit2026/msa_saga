package com.example.apiproduct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(originPatterns = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductRepository productRepository;
    private final KafkaTemplate<Long, Long> kafkaTemplate;


    // payment-completed 이벤트를 받았을 때 재고 차감 로직 구현
    //      이벤트로 받은 상품IDX 별 재고를 차감
    //      재고 변경 완료 이벤트 발행 (product-stock-reduced)

    @KafkaListener(topics = "payment-completed", groupId = "abcd-group-1",
            properties = "spring.json.value.default.type:com.example.apiproduct.ProductDto.OrdersRes")
    @Transactional
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) Long key,
            @Payload ProductDto.OrdersRes dto
    ) {
        try {
            for (ProductDto.OrdersItemRes item : dto.getOrdersItems()) {
                Product product = productRepository.findById(item.getProductIdx())
                        .orElseThrow();

                product.reduceStock(item.getQuantity());
            }
            kafkaTemplate.send("product-stock-reduced", dto.getIdx(), dto.getIdx());
        } catch (Exception e) {
            kafkaTemplate.send("orders-failed", dto.getIdx(), dto.getIdx());
        }
    }

    @GetMapping("/list")
    public ResponseEntity list() {
        List<Product> productList = productRepository.findAll();
        return ResponseEntity.ok(productList);
    }
}
