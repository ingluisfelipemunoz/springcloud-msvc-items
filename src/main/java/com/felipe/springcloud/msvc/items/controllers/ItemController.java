package com.felipe.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.felipe.springcloud.msvc.items.models.Item;
import com.felipe.springcloud.msvc.items.services.ItemService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ItemController {
    private final ItemService service;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService service) {
        this.service = service;
    }

    @GetMapping()
    public List<Item> list(@RequestHeader(name = "token-request", required = false) String header,
            @RequestParam(name = "name", required = false) String name) {
        System.out.println("Name: " + name + " Token: " + header);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable() Long id) {
        Optional<Item> itemOptional = service.findById(id);
        if (itemOptional.isPresent()) {
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
                .body(Collections
                        .singletonMap("message", "Product not found in product microservice"));
    }
}
