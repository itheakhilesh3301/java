package com.akhilesh.journalEntry.config;

import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.repository.UserRepo;
import com.akhilesh.journalEntry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Check if an admin user already exists
        if (userRepo.findByUsername("admin") == null) {
            log.info("No 'admin' user found. Creating default admin...");
            
            User admin = User.builder()
                    .username("admin")
                    .password("admin") // Make sure to change this later!
                    .email("admin@example.com")
                    .build();
            
            userService.saveAdmin(admin);
            log.info("Default admin user created successfully! Username: 'admin', Password: 'admin'");
        }
    }
}
