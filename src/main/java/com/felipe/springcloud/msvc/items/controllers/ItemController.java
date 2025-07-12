package com.felipe.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.felipe.springcloud.msvc.items.models.Item;
import com.felipe.libs.msvc.commons.entities.Product;
import com.felipe.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RefreshScope
@RestController
public class ItemController {
    private final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService service;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    private final Environment env;

    @Value("${config.text}")
    private String text;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService service,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory, Environment env) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.service = service;
        this.env = env;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> getConfigs(@Value("${server.port}") String port) {
        Map<String, String> json = new HashMap<>();
        json.put("text", this.text);
        json.put("port", port);
        logger.info("port: " + port);
        logger.info("text: " + text);
        if (env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev")) {
            json.put("author.name", env.getProperty("config.author.name"));
            json.put("author.email", env.getProperty("config.author.email"));
            json.put("env", env.getActiveProfiles()[0]);
        }
        return ResponseEntity.ok(json);
    }

    @GetMapping()
    public List<Item> list(@RequestHeader(name = "token-request", required = false) String token,
            @RequestParam(name = "name", required = false) String name) {
        logger.info("Request Parameter: {}", name);
        logger.info("Token: {}", token);
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

    @CircuitBreaker(name = "items", fallbackMethod = "getFallBackMethodProduct2")
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

    public CompletableFuture<?> getFallBackMethodProduct2(Throwable e) {
        return CompletableFuture.supplyAsync(() -> {
            logger.error(e.getMessage());
            return ResponseEntity.ok(new Item(new Product(1L, "Default", 100D, LocalDate.now(), 80), 10));
        });
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product product) {
        logger.info("Creating product: {}", product);
        return service.save(product);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        logger.info("Updating product: {}", product);
        return service.update(product, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        logger.info("Deleting product: {}", id);
        service.delete(id);
    }

}
