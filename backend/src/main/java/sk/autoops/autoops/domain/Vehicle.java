package sk.autoops.autoops.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Vehicle {
    public UUID id;
    public String vin;
    public String licensePlate;
    public String brand;
    public String model;
    public int year;
    public int mileage;
    public UUID customerId;
    public List<UUID> repairOrderIds = new ArrayList<>();

    public Vehicle() {
    }

    public Vehicle(UUID id, String vin, String licensePlate, String brand, String model, int year, int mileage, UUID customerId) {
        this.id = id;
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.customerId = customerId;
    }
}
