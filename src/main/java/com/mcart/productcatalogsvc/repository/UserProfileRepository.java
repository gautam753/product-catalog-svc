package com.mcart.productcatalogsvc.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.mcart.productcatalogsvc.entity.UserProfile;

public interface UserProfileRepository
        extends R2dbcRepository<UserProfile, UUID> {
}
