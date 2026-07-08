package com.akhilesh.journalEntry.controller;

import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        userService.saveNewUser(user);
        return ResponseEntity.ok("User created successfully");
    }
}
