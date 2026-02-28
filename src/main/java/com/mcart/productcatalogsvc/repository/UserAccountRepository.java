package com.mcart.productcatalogsvc.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.mcart.productcatalogsvc.entity.UserAccount;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface UserAccountRepository
        extends ReactiveCrudRepository<UserAccount, UUID> {

    Mono<UserAccount> findByEmail(String email);

    Mono<UserAccount> findByPhone(String phone);
}
