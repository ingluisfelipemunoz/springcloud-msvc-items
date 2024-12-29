package com.felipe.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.felipe.springcloud.msvc.items.clients.ProductFeignClient;
import com.felipe.springcloud.msvc.items.models.Item;
import com.felipe.springcloud.msvc.items.models.Product;

@Service
public class ItemServiceFeign implements ItemService {
    private final ProductFeignClient client;

    public ItemServiceFeign(ProductFeignClient client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return client.findAll().stream().map(product -> {
            Random random = new Random();
            return new Item(product, random.nextInt(10) + 1);
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Product product = client.details(id);
        if (product == null) {
            return Optional.empty();
        }
        return Optional.of(new Item(product, new Random().nextInt(10) + 1));
    }

}
