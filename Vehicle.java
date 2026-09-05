package com.example.carprojds2;

class Vehicle implements Comparable<Vehicle> {
    static final String AVAILABLE = "AVAILABLE";
    static final String SOLD = "SOLD";
    static final String IN_SERVICE = "IN_SERVICE";
    static final String RESERVED = "RESERVED";
    static final String RECEIVED = "RECEIVED";
    static final String[] STATUSES = {AVAILABLE, SOLD, IN_SERVICE, RESERVED, RECEIVED};

    private static int nextVehicleId = 1;

    private int vehicleId;
    private int ownerCustomerId;
    private String make;
    private String model;
    private int year;
    private double price;
    private String color;
    private String status;
    private String receiveStartDate;
    private String receiveEndDate;

    static Vehicle create(int ownerCustomerId, String make, String model, int year, double price, String color, String status) {
        return new Vehicle(nextVehicleId++, ownerCustomerId, make, model, year, price, color, status);
    }

    Vehicle(int vehicleId, String make, String model, int year, double price, String color, String status) {
        this(vehicleId, 0, make, model, year, price, color, status);
    }

    Vehicle(int vehicleId, int ownerCustomerId, String make, String model, int year, double price, String color, String status) {
        this(vehicleId, ownerCustomerId, make, model, year, price, color, status, "", "");
    }

    Vehicle(int vehicleId, int ownerCustomerId, String make, String model, int year, double price, String color,
            String status, String receiveStartDate, String receiveEndDate) {
        this.vehicleId = vehicleId;
        this.ownerCustomerId = ownerCustomerId;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.color = color;
        this.status = normalizeStatus(status);
        this.receiveStartDate = receiveStartDate == null ? "" : receiveStartDate;
        this.receiveEndDate = receiveEndDate == null ? "" : receiveEndDate;
        updateNextId(vehicleId);
    }

    int getVehicleId() {
        return vehicleId;
    }

    int getOwnerCustomerId() {
        return ownerCustomerId;
    }

    void setOwnerCustomerId(int ownerCustomerId) {
        this.ownerCustomerId = ownerCustomerId;
    }

    String getMake() {
        return make;
    }

    void setMake(String make) {
        this.make = make;
    }

    String getModel() {
        return model;
    }

    void setModel(String model) {
        this.model = model;
    }

    int getYear() {
        return year;
    }

    void setYear(int year) {
        this.year = year;
    }

    double getPrice() {
        return price;
    }

    void setPrice(double price) {
        this.price = price;
    }

    String getColor() {
        return color;
    }

    void setColor(String color) {
        this.color = color;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    String getReceiveStartDate() {
        return receiveStartDate;
    }

    void setReceiveStartDate(String receiveStartDate) {
        this.receiveStartDate = receiveStartDate == null ? "" : receiveStartDate;
    }

    String getReceiveEndDate() {
        return receiveEndDate;
    }

    void setReceiveEndDate(String receiveEndDate) {
        this.receiveEndDate = receiveEndDate == null ? "" : receiveEndDate;
    }

    Vehicle copy() {
        return new Vehicle(vehicleId, ownerCustomerId, make, model, year, price, color, status, receiveStartDate, receiveEndDate);
    }

    void copyFrom(Vehicle other) {
        ownerCustomerId = other.ownerCustomerId;
        make = other.make;
        model = other.model;
        year = other.year;
        price = other.price;
        color = other.color;
        status = other.status;
        receiveStartDate = other.receiveStartDate;
        receiveEndDate = other.receiveEndDate;
    }

    @Override
    public int compareTo(Vehicle other) {
        return Integer.compare(vehicleId, other.vehicleId);
    }

    private static void updateNextId(int usedId) {
        if (usedId >= nextVehicleId) {
            nextVehicleId = usedId + 1;
        }
    }

    static String normalizeStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Vehicle status is required.");
        }
        String normalized = status.trim().toUpperCase();
        for (String allowed : STATUSES) {
            if (allowed.equals(normalized)) {
                return normalized;
            }
        }
        throw new IllegalArgumentException("Vehicle status must be AVAILABLE, SOLD, IN_SERVICE, RESERVED, or RECEIVED.");
    }
}
