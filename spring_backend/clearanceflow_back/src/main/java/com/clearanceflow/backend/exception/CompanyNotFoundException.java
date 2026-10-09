package com.clearanceflow.backend.exception;

public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(Long id) {
        super("Company not found with ID: " + id);
    }
}