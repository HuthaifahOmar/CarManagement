package CarProjDS2;

class ReservationRequest implements Comparable<ReservationRequest> {
    private static int nextReservationId = 1;

    private int reservationId;
    private int vehicleId;
    private int customerId;
    private String date;
    private String status;

    ReservationRequest(int vehicleId, int customerId, String date, String status) {
        this(nextReservationId++, vehicleId, customerId, date, status);
    }

    ReservationRequest(int reservationId, int vehicleId, int customerId, String date, String status) {
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.date = date;
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
        return date;
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
