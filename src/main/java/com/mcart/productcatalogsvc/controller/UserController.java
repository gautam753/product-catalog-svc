package com.mcart.productcatalogsvc.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcart.productcatalogsvc.entity.UserAccount;
import com.mcart.productcatalogsvc.entity.UserAddress;
import com.mcart.productcatalogsvc.entity.UserProfile;
import com.mcart.productcatalogsvc.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =========================
    // ACCOUNT
    // =========================

    @GetMapping("/me")
    public Mono<UserAccount> getMyAccount(@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId) {
    	log.info("authoeized-user-id: {}", authorizedUserId);
    	UUID userId = UUID.fromString(authorizedUserId);
        return userService.getAccount(userId);
    }

    @PutMapping("/me")
    public Mono<UserAccount> updateMyAccount(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @RequestBody UserAccount account) {

        UUID userId = UUID.fromString(authorizedUserId);
        return userService.updateAccount(userId, account);
    }

    // =========================
    // PROFILE
    // =========================

    @GetMapping("/me/profile")
    public Mono<UserProfile> getProfile(@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId) {
        UUID userId = UUID.fromString(authorizedUserId);
        return userService.getProfile(userId);
    }

    @PutMapping("/me/profile")
    public Mono<UserProfile> saveProfile(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @RequestBody UserProfile profile) {

        UUID userId = UUID.fromString(authorizedUserId);
        return userService.saveOrUpdateProfile(userId, profile);
    }

    @DeleteMapping("/me/profile")
    public Mono<Void> deleteProfile(@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId) {
        UUID userId = UUID.fromString(authorizedUserId);
        return userService.deleteProfile(userId);
    }

    // =========================
    // ADDRESS
    // =========================

    @GetMapping("/me/addresses")
    public Flux<UserAddress> getAddresses(@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId) {
        UUID userId = UUID.fromString(authorizedUserId);
        return userService.getAddresses(userId);
    }

    @PostMapping("/me/addresses")
    public Mono<UserAddress> addAddress(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @RequestBody UserAddress address) {

        UUID userId = UUID.fromString(authorizedUserId);
        return userService.addAddress(userId, address);
    }

    @PutMapping("/me/addresses/{addressId}")
    public Mono<UserAddress> updateAddress(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @PathVariable UUID addressId,
            @RequestBody UserAddress address) {

        UUID userId = UUID.fromString(authorizedUserId);
        return userService.updateAddress(userId, addressId, address);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public Mono<Void> deleteAddress(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @PathVariable UUID addressId) {

        UUID userId = UUID.fromString(authorizedUserId);
        return userService.deleteAddress(userId, addressId);
    }

    @PutMapping("/me/addresses/{addressId}/default")
    public Mono<Void> setDefaultAddress(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @PathVariable UUID addressId) {

        UUID userId = UUID.fromString(authorizedUserId);
        return userService.setDefaultAddress(userId, addressId);
    }
}
