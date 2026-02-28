package com.mcart.productcatalogsvc.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.mcart.productcatalogsvc.entity.UserAccount;

import reactor.core.publisher.Mono;

public interface UserAccountRepository
        extends R2dbcRepository<UserAccount, UUID> {

    Mono<UserAccount> findByEmail(String email);

    Mono<UserAccount> findByPhone(String phone);
}
