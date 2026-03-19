package com.example.apiorders;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Setter
    private String status;

    @Setter
    private String pgPaymentId;

    private int paymentPrice;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrdersItem> ordersItems = new ArrayList<>();
}
