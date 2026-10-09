
package com.clearanceflow.backend.entity;
import com.clearanceflow.backend.enums.CompanySize;
import com.clearanceflow.backend.enums.Industry;
import com.clearanceflow.backend.enums.ProjectType;

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

    @Enumerated(EnumType.STRING) //tells JPA to store enum names in PostgreSQL, such as MANUFACTURING, instead of integer positions
    private Industry industry;


    private String state;

    @Enumerated(EnumType.STRING)
    private CompanySize companySize;

    @Enumerated(EnumType.STRING)
    private ProjectType projectType;
}
