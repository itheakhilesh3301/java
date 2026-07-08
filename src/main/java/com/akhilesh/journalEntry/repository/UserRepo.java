package com.akhilesh.journalEntry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.akhilesh.journalEntry.entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    
    User findByUsername(String username);
    
    void deleteByUsername(String username);
}
