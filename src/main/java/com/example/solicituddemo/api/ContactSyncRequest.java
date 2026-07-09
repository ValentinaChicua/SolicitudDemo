package com.example.solicituddemo.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactSyncRequest(
        @NotBlank String firstname,
        @NotBlank String lastname,
        @Email @NotBlank String email,
        @NotBlank String company
) {
}