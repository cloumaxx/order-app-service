package com.uni.sabana.order_app.web.controller;

import com.uni.sabana.order_app.domain.models.Order;
import com.uni.sabana.order_app.infrastructure.repostory.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderRepository orderRepository;

    @GetMapping
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public Order findOne(@PathVariable Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Order save(@RequestBody Order order) {
        return orderRepository.save(order);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from CI/CD demo!";
    }
}
