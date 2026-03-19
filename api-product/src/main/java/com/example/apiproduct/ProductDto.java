package com.example.apiproduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ProductDto {
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    public static class OrdersItemRes {
        private Long idx;
        private Long productIdx;
        private int quantity;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    public static class OrdersRes {
        private Long idx;
        private Integer paymentPrice;
        private String status;
        private List<OrdersItemRes> ordersItems;
    }
}
