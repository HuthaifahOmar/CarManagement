package com.example.carprojds2;

class ReservationRequest implements Comparable<ReservationRequest> {
    private static int nextReservationId = 1;

    private int reservationId;
    private int vehicleId;
    private int customerId;
    private String startDate;
    private String endDate;
    private double price;
    private double discount;
    private String status;

    ReservationRequest(int vehicleId, int customerId, String startDate, String endDate, String status) {
        this(nextReservationId++, vehicleId, customerId, startDate, endDate, 0, 0, status);
    }

    ReservationRequest(int vehicleId, int customerId, String startDate, String endDate, double price, double discount, String status) {
        this(nextReservationId++, vehicleId, customerId, startDate, endDate, price, discount, status);
    }

    ReservationRequest(int reservationId, int vehicleId, int customerId, String date, String status) {
        this(reservationId, vehicleId, customerId, date, date, 0, 0, status);
    }

    ReservationRequest(int reservationId, int vehicleId, int customerId, String startDate, String endDate, String status) {
        this(reservationId, vehicleId, customerId, startDate, endDate, 0, 0, status);
    }

    ReservationRequest(int reservationId, int vehicleId, int customerId, String startDate, String endDate,
                       double price, double discount, String status) {
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.discount = discount;
        this.status = status;
        updateNextId(reservationId);
    }

    int getReservationId() {
        return reservationId;
    }

    int getVehicleId() {
        return vehicleId;
    }

    int getCustomerId() {
        return customerId;
    }

    String getDate() {
        return startDate;
    }

    String getStartDate() {
        return startDate;
    }

    String getEndDate() {
        return endDate;
    }

    double getPrice() {
        return price;
    }

    double getDiscount() {
        return discount;
    }

    double getPaidAmount() {
        return price - discount;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(ReservationRequest other) {
        return Integer.compare(reservationId, other.reservationId);
    }

    private static void updateNextId(int usedId) {
        if (usedId >= nextReservationId) {
            nextReservationId = usedId + 1;
        }
    }
}
