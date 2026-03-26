package com.example.demo1.service;

import com.example.demo1.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IRoleRepository roleRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) throws Exception {
        // Ensure Role ADMIN (ID: 1) exists
        roleRepository.insertRoleWithId(1L, "ADMIN", "Administrator role with full access");

        // Ensure Role USER (ID: 2) exists
        roleRepository.insertRoleWithId(2L, "USER", "Standard user role with limited access");

        // Ensure Role MANAGER (ID: 3) exists
        roleRepository.insertRoleWithId(3L, "MANAGER", "Manager role with product management access");
    }
}
