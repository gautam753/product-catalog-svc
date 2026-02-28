package com.mcart.productcatalogsvc.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mcart.productcatalogsvc.entity.UserAccount;
import com.mcart.productcatalogsvc.entity.UserAddress;
import com.mcart.productcatalogsvc.entity.UserProfile;
import com.mcart.productcatalogsvc.repository.UserAccountRepository;
import com.mcart.productcatalogsvc.repository.UserAddressRepository;
import com.mcart.productcatalogsvc.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository accountRepo;
    private final UserProfileRepository profileRepo;
    private final UserAddressRepository addressRepo;

    // ===============================
    // ACCOUNT
    // ===============================

    public Mono<UserAccount> getAccount(UUID userId) {
        return accountRepo.findById(userId);
    }

    public Mono<UserAccount> updateAccount(UUID userId, UserAccount updated) {
        return accountRepo.findById(userId)
                .flatMap(existing -> {
                    existing.setName(updated.getName());
                    existing.setPhone(updated.getPhone());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return accountRepo.save(existing);
                });
    }

    // ===============================
    // PROFILE
    // ===============================

    public Mono<UserProfile> getProfile(UUID userId) {
        return profileRepo.findById(userId);
    }

    public Mono<UserProfile> saveOrUpdateProfile(UUID userId, UserProfile profile) {
        profile.setUserId(userId);
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepo.save(profile);
    }

    public Mono<Void> deleteProfile(UUID userId) {
        return profileRepo.deleteById(userId);
    }

    // ===============================
    // ADDRESS
    // ===============================

    public Flux<UserAddress> getAddresses(UUID userId) {
        return addressRepo.findByUserId(userId);
    }

    public Mono<UserAddress> addAddress(UUID userId, UserAddress address) {
        address.setAddressId(UUID.randomUUID());
        address.setUserId(userId);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        return addressRepo.save(address);
    }

    public Mono<UserAddress> updateAddress(UUID userId, UUID addressId, UserAddress updated) {
        return addressRepo.findById(addressId)
                .filter(addr -> addr.getUserId().equals(userId))
                .flatMap(existing -> {
                    existing.setType(updated.getType());
                    existing.setAddressJson(updated.getAddressJson());
                    existing.setIsDefault(updated.getIsDefault());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return addressRepo.save(existing);
                });
    }

    public Mono<Void> deleteAddress(UUID userId, UUID addressId) {
        return addressRepo.findById(addressId)
                .filter(addr -> addr.getUserId().equals(userId))
                .flatMap(addressRepo::delete);
    }

    public Mono<Void> setDefaultAddress(UUID userId, UUID addressId) {
        return addressRepo.findByUserId(userId)
                .flatMap(addr -> {
                    addr.setIsDefault(addr.getAddressId().equals(addressId));
                    return addressRepo.save(addr);
                })
                .then();
    }
}
