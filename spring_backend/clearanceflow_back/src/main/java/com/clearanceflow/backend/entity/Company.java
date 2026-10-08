
package com.clearanceflow.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity //Tells JPA that this java class represents a database entity
@Table(name = "companies") //Maps the entity to a PostgreSQL table named companies
@Getter //Lombok generates getter and setter methods for the fields.
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sector;

    private String location;

    private Integer employeeCount;
}
