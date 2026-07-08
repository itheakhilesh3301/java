package com.akhilesh.journalEntry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.service.UserService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUserName(auth.getName());
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<User> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUserName(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id).orElse(null);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        if (user.getRoles() != null && user.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.badRequest().body("Validation Error: Cannot create an ADMIN user via this endpoint.");
        }
        userService.saveNewUser(user);
        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        if (user.getRoles() != null && user.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.badRequest().body("Validation Error: Cannot update role to ADMIN.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User userInDb = userService.findByUserName(username);
        
        if (userInDb == null || !userInDb.getId().equals(id)) {
             return ResponseEntity.badRequest().body("Validation Error: You can only update your own profile.");
        }
            // Validations for same existing values
            if (user.getUsername() != null && user.getUsername().equals(userInDb.getUsername())) {
                return ResponseEntity.badRequest().body("Validation Error: The new username matches your current username.");
            }
            if (user.getEmail() != null && user.getEmail().equals(userInDb.getEmail())) {
                return ResponseEntity.badRequest().body("Validation Error: The new email matches your current email.");
            }
            if (user.getPassword() != null && passwordEncoder.matches(user.getPassword(), userInDb.getPassword())) {
                return ResponseEntity.badRequest().body("Validation Error: The new password cannot be the same as your current password.");
            }

            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                userInDb.setUsername(user.getUsername());
            }
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                userInDb.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                userInDb.setEmail(user.getEmail());
            }
            userService.saveUser(userInDb);
            return ResponseEntity.ok("User updated successfully");
    }
}
