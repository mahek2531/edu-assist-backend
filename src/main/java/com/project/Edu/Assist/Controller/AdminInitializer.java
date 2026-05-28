package com.project.Edu.Assist.Controller;

import com.project.Edu.Assist.Entity.Admin;
import com.project.Edu.Assist.Repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (adminRepository.findByEmailIgnoreCase("admin@gmail.com").isEmpty()) {

            Admin admin = new Admin();
            admin.setName("ADMIN");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("Admin123"));
            admin.setRole("ADMIN");
            admin.setLoggedIn(false);

            adminRepository.save(admin);
            System.out.println("Default Admin created: admin@gmail.com / Admin123");
        } else {
            System.out.println("Admin user already exists. Skipping creation.");
        }
    }
}