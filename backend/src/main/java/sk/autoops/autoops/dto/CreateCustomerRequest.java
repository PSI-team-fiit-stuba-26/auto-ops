package sk.autoops.autoops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = "must not be blank")
        @Size(min = 2, max = 120, message = "must be between 2 and 120 characters")
        String name,

        @NotBlank(message = "must not be blank")
        @Email(message = "must be a valid email address")
        @Size(max = 254, message = "must not exceed 254 characters")
        String email,

        @Pattern(regexp = "^\\+?[0-9 ()\\-]{6,20}$", message = "must be a valid phone number")
        String phone
) {
}
