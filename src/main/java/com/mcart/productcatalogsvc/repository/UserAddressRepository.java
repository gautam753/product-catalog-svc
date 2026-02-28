package com.mcart.productcatalogsvc.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.mcart.productcatalogsvc.entity.UserAddress;

import reactor.core.publisher.Flux;

public interface UserAddressRepository
        extends R2dbcRepository<UserAddress, UUID> {

    Flux<UserAddress> findByUserId(UUID userId);

    Flux<UserAddress> findByUserIdAndType(UUID userId, String type);
}
