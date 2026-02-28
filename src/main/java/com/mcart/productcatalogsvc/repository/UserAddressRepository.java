package com.mcart.productcatalogsvc.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mcart.productcatalogsvc.entity.UserAddress;

import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UserAddressRepository
        extends ReactiveCrudRepository<UserAddress, UUID> {

    Flux<UserAddress> findByUserId(UUID userId);

    Flux<UserAddress> findByUserIdAndType(UUID userId, String type);
}
