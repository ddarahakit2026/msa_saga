package com.example.apipayment;

import lombok.*;

import java.util.List;

public class PaymentDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyReq {
        private String paymentId;
        private Long ordersIdx;
    }

    @Builder
    @Getter
    public static class OrdersItemRes {
        private Long idx;
        private Long productIdx;
        private int quantity;
    }


    @Builder
    @Getter
    public static class OrdersRes {
        private Long idx;
        private Integer paymentPrice;
        private String status;
        private List<OrdersItemRes> ordersItems;
    }

    @Builder
    @Getter
    public static class ProductRes {
            private Long idx;
            private String name;
            private int price;

    }
}
