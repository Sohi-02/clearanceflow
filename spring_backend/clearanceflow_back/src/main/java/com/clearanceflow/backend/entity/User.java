package com.clearanceflow.backend.entity;

import com.clearanceflow.backend.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.APPLICANT;

    @ManyToOne(fetch = FetchType.LAZY) //This creates a relationship where multiple users can belong to one company
    @JoinColumn(name = "company_id") //many-to-one relationship. PostgreSQL will store company_id as a foreign key in the users table.
    private Company company;
}