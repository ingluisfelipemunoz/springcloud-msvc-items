package com.felipe.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.felipe.springcloud.msvc.items.models.Item;
import com.felipe.springcloud.msvc.items.models.Product;
import com.felipe.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ItemController {
    private final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService service;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService service,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.service = service;
    }

    @GetMapping()
    public List<Item> list(@RequestHeader(name = "token-request", required = false) String header,
            @RequestParam(name = "name", required = false) String name) {
        System.out.println("Name: " + name + " Token: " + header);
        return service.findAll();
    }

    @CircuitBreaker(name = "items", fallbackMethod = "getFallBackMethodProduct")
    @GetMapping("/details/{id}")
    public ResponseEntity<?> detailsCb(@PathVariable() Long id) {
        Optional<Item> itemOptional = service.findById(id);
        if (itemOptional.isPresent()) {
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
                .body(Collections
                        .singletonMap("message", "Product not found in product microservice"));
    }

    @TimeLimiter(name = "items")
    @GetMapping("/details2/{id}")
    public CompletableFuture<?> detailsCbRateLimiter(@PathVariable() Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Item> itemOptional = service.findById(id);
            if (itemOptional.isPresent()) {
                return ResponseEntity.ok(itemOptional.get());
            }
            return ResponseEntity.status(404)
                    .body(Collections
                            .singletonMap("message", "Product not found in product microservice"));
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable() Long id) {
        Optional<Item> itemOptional = circuitBreakerFactory.create("items").run(() -> service.findById(id), e -> {
            logger.error(e.getMessage());
            return Optional.of(new Item(new Product(1L, "Default", 100D, LocalDate.now(), 80), 10));
        });// service.findById(id);
        if (itemOptional.isPresent()) {
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
                .body(Collections
                        .singletonMap("message", "Product not found in product microservice"));
    }

    public ResponseEntity<?> getFallBackMethodProduct(Throwable e) {
        logger.error(e.getMessage());
        return ResponseEntity.ok(new Item(new Product(1L, "Default", 100D, LocalDate.now(), 80), 10));
    }
}
