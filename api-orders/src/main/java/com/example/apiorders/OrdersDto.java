package com.example.apiorders;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class OrdersDto {
    @Builder
    @Getter
    public static class OrdersItemReq {
        private Long productIdx;
        private int quantity;

        public OrdersItem toEntity(Orders orders) {
            return OrdersItem.builder()
                    .productIdx(this.productIdx)
                    .quantity(this.quantity)
                    .orders(orders)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class OrdersItemRes {
        private Long idx;
        private Long productIdx;
        private int quantity;

        public static OrdersItemRes from(OrdersItem entity) {
            return OrdersItemRes.builder()
                    .idx(entity.getIdx())
                    .productIdx(entity.getProductIdx())
                    .quantity(entity.getQuantity())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class OrdersReq {
        private Integer paymentPrice;
        private List<OrdersItemReq> ordersItems;

        public Orders toEntity() {
            Orders orders = Orders.builder()
                    .status("PENDING")
                    .paymentPrice(this.paymentPrice)
                    .build();

            if (this.ordersItems != null) {
                this.ordersItems.forEach(item -> {
                    orders.getOrdersItems().add(OrdersItem.builder()
                            .productIdx(item.getProductIdx())
                            .quantity(item.getQuantity())
                            .orders(orders)
                            .build());
                });
            }

            return orders;
        }
    }


    @Builder
    @Getter
    public static class OrdersRes {
        private Long idx;
        private Integer paymentPrice;
        private String status;
        private List<OrdersItemRes> ordersItems;

        public static OrdersRes from(Orders entity) {
            return OrdersRes.builder()
                    .idx(entity.getIdx())
                    .status(entity.getStatus())
                    .paymentPrice(entity.getPaymentPrice())
                    .ordersItems(entity.getOrdersItems().stream().map(OrdersItemRes::from).toList())
                    .build();
        }
    }
}
