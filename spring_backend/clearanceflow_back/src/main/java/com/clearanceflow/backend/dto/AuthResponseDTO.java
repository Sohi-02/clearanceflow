//This defines what the backend will return after successful login.

package com.clearanceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String tokenType;
}