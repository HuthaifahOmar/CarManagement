package CarProjDS2;

class ServiceRequest implements Comparable<ServiceRequest> {
    private static int nextRequestId = 1;

    private int requestId;
    private int vehicleId;
    private int customerId;
    private String serviceType;
    private String date;
    private String status;

    ServiceRequest(int vehicleId, int customerId, String serviceType, String date, String status) {
        this(nextRequestId++, vehicleId, customerId, serviceType, date, status);
    }

    ServiceRequest(int requestId, int vehicleId, int customerId, String serviceType, String date, String status) {
        this.requestId = requestId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.date = date;
        this.status = status;
        updateNextId(requestId);
    }

    int getRequestId() {
        return requestId;
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

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    ServiceRequest copy() {
        return new ServiceRequest(requestId, vehicleId, customerId, serviceType, date, status);
    }

    @Override
    public int compareTo(ServiceRequest other) {
        return Integer.compare(requestId, other.requestId);
    }

    private static void updateNextId(int usedId) {
        if (usedId >= nextRequestId) {
            nextRequestId = usedId + 1;
        }
    }
}
