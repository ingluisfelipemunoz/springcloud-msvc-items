package com.felipe.springcloud.msvc.items.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import com.felipe.springcloud.msvc.items.models.Item;

@Service
public class ItemServiceWebClient implements ItemService {
    private final WebClient.Builder client;

    public ItemServiceWebClient(Builder client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return this.client.build()
                .get().uri("http://msvc-products")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Item.class)
                .collectList().block();
    }

    @Override
    public Optional<Item> findById(Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);
        return Optional.ofNullable(client.build()
                .get()
                .uri("http://msvc-products/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Item.class)
                .block());
    }

}
