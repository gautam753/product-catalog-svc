package com.mcart.productcatalogsvc.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mcart.productcatalogsvc.entity.UserProfile;

import java.util.UUID;

public interface UserProfileRepository
        extends ReactiveCrudRepository<UserProfile, UUID> {
}
