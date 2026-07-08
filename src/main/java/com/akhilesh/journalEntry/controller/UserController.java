package com.akhilesh.journalEntry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        if (user.getRoles() != null && user.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.badRequest().body("Validation Error: Cannot create an ADMIN user via this endpoint.");
        }
        userService.saveNewUser(user);
        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        if (user.getRoles() != null && user.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.badRequest().body("Validation Error: Cannot update role to ADMIN.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User userInDb = userService.findByUserName(username);
        
        if (userInDb != null) {
            userInDb.setUsername(user.getUsername());
            userInDb.setPassword(user.getPassword());
            userInDb.setEmail(user.getEmail());
            
            // Note: saveNewUser sets the role back to "USER". 
            // If an Admin updates their profile here, they might lose admin rights unless handled in UserService!
            userService.saveNewUser(userInDb);
            return ResponseEntity.ok("User updated successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
