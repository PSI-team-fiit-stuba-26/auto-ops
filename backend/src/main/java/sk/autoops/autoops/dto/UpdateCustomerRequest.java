package sk.autoops.autoops.dto;

public record UpdateCustomerRequest(
        String name,
        String email,
        String phone
) {
}
