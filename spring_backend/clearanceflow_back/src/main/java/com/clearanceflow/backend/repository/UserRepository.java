package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
 //When someone attempts to log in, their email might not exist in the database.
 //Optional<User> lets us handle both situations without assuming a user was found.

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}