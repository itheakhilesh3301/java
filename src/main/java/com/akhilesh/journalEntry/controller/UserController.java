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
import com.akhilesh.journalEntry.entity.AuditLog;
import com.akhilesh.journalEntry.repository.AuditLogRepository;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogRepository auditLogRepository;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User userInDb = userService.findByUserName(username);
        
        if (userInDb == null || !userInDb.getId().equals(id)) {
             return ResponseEntity.badRequest().body("Validation Error: You can only delete your own profile.");
        }
        
        userService.deleteById(id);

        // Log the self-delete action
        AuditLog log = AuditLog.builder()
                .action("USER_SELF_DELETE")
                .adminUsername(username)
                .targetUsername(username)
                .details("User deleted their own profile")
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);

        return ResponseEntity.ok("User deleted successfully");
    }
}
