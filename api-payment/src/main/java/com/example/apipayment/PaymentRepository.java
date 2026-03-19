package com.example.apipayment;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<LocalPayment, Long> {
    Optional<LocalPayment> findByOrdersIdx(Long ordersIdx);
}

