package sk.autoops.autoops.dto;

public record UpdateVehicleRequest(
        String licensePlate,
        String brand,
        String model,
        Integer year,
        Integer mileage
) {
}
