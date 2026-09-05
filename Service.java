package com.example.carprojds2;

class Service implements Comparable<Service> {
    private static int nextServiceId = 1;

    private int serviceId;
    private int vehicleId;
    private int customerId;
    private String serviceType;
    private String date;
    private double cost;
    private String status;

    Service(int vehicleId, int customerId, String serviceType, String date, double cost, String status) {
        this(nextServiceId++, vehicleId, customerId, serviceType, date, cost, status);
    }

    Service(int serviceId, int vehicleId, int customerId, String serviceType, String date, double cost, String status) {
        this.serviceId = serviceId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.date = date;
        this.cost = cost;
        this.status = status;
        updateNextId(serviceId);
    }

    int getServiceId() {
        return serviceId;
    }

    int getVehicleId() {
        return vehicleId;
    }

    int getCustomerId() {
        return customerId;
    }

    String getServiceType() {
        return serviceType;
    }

    String getDate() {
        return date;
    }

    double getCost() {
        return cost;
    }

    String getStatus() {
        return status;
    }

    Service copy() {
        return new Service(serviceId, vehicleId, customerId, serviceType, date, cost, status);
    }

    @Override
    public int compareTo(Service other) {
        return Integer.compare(serviceId, other.serviceId);
    }

    private static void updateNextId(int usedId) {
        if (usedId >= nextServiceId) {
            nextServiceId = usedId + 1;
        }
    }
}
