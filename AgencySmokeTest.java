package CarProjDS2;

import java.time.LocalDate;

public class AgencySmokeTest {
    public static void main(String[] args) {
        CarAgency agency = new CarAgency();
        agency.loadDemoData();
        String yesterday = LocalDate.now().minusDays(1).toString();

        Vehicle vehicle60 = agency.findVehicle(60);
        require(vehicle60 != null, "Vehicle 60 should be found in the AVL tree.");
        agency.changeVehicleStatus(60, Vehicle.IN_SERVICE);
        require(agency.findVehicle(60) != null, "Vehicle 60 should remain in AVL after status change.");
        agency.changeVehicleStatus(60, Vehicle.AVAILABLE);

        agency.addWaitingCustomer(1);
        agency.addWaitingCustomer(2);
        agency.addWaitingCustomer(3);
        require(agency.serveWaitingCustomer().getCustomerId() == 1, "Waiting queue must serve Ahmed first.");
        agency.undo();
        require(agency.waitingCustomers()[0].getCustomerId() == 1, "Undo serve should return Ahmed to the waiting queue.");
        agency.redo();
        require(agency.waitingCustomers()[0].getCustomerId() == 2, "Redo serve should remove Ahmed again.");
        require(agency.waitingCustomers()[0].getCustomerId() == 2, "Sara should be next in waiting queue.");
        require(agency.waitingCustomers()[1].getCustomerId() == 3, "Omar should be third in waiting queue.");

        agency.receiveVehicle(80, LocalDate.now().toString(), LocalDate.now().plusDays(2).toString());
        require(agency.findVehicle(80).getStatus().equals(Vehicle.RECEIVED), "Received vehicle should have RECEIVED status.");
        agency.updateVehicle(new Vehicle(80, 2, "Toyota", "Rav4", 2026, 23000, "green", Vehicle.AVAILABLE));
        require(agency.findVehicle(80).getStatus().equals(Vehicle.RECEIVED), "Vehicle update should not change status.");
        agency.endReceiving(80);
        require(agency.findVehicle(80).getStatus().equals(Vehicle.AVAILABLE), "End receiving should make the vehicle available.");
        agency.markSold(80);
        boolean soldReceiveRejected = false;
        try {
            agency.receiveVehicle(80, LocalDate.now().plusDays(3).toString(), LocalDate.now().plusDays(4).toString());
        } catch (IllegalArgumentException ex) {
            soldReceiveRejected = true;
        }
        require(soldReceiveRejected, "Sold vehicles cannot be received.");

        agency.createServiceRequest(new ServiceRequest(101, 50, 1, "Oil Change", yesterday, "PENDING"));
        boolean duplicateRejected = false;
        try {
            agency.createServiceRequest(new ServiceRequest(50, 1, "Oil Change", yesterday, "PENDING"));
        } catch (IllegalArgumentException ex) {
            duplicateRejected = true;
        }
        require(duplicateRejected, "Duplicate maintenance request for the same customer, vehicle, and service type should be rejected.");
        agency.createServiceRequest(new ServiceRequest(102, 30, 2, "Brake Service", yesterday, "PENDING"));
        agency.createServiceRequest(new ServiceRequest(103, 70, 3, "Tire Replacement", yesterday, "PENDING"));
        require(agency.processNextServiceRequest().getRequestId() == 101, "Maintenance request 101 should be processed first.");
        agency.undo();
        require(agency.maintenanceRequests()[0].getRequestId() == 101, "Undo process should return request 101 to the maintenance table.");
        require(agency.processNextServiceRequest().getRequestId() == 101, "Redo after undo process should allow request 101 to process first again.");
        agency.completeCurrentService(120);
        agency.undo();
        require(agency.maintenanceRequests()[0].getRequestId() == 101, "Undo complete should return request 101 to the maintenance table.");
        require(agency.serviceHistoryNewestFirst().length == 0, "Undo complete should remove the completed service from service history.");
        require(agency.processNextServiceRequest().getRequestId() == 101, "Request 101 should process again after undo complete.");
        agency.completeCurrentService(120);
        require(agency.processNextServiceRequest().getRequestId() == 102, "Maintenance request 102 should be processed second.");
        agency.completeCurrentService(250);
        require(agency.processNextServiceRequest().getRequestId() == 103, "Maintenance request 103 should be processed third.");
        agency.completeCurrentService(90);

        agency.createTransaction(new Transaction(1, 0, 1000, "DEPOSIT", "2026-09-05"));
        int generatedTransactionId = agency.transactions()[0].getTransactionId();
        agency.applyDiscount(generatedTransactionId, 100);
        require(agency.transactions()[0].getAmount() == 900, "Discount should reduce transaction amount.");
        agency.undo();
        require(agency.transactions()[0].getAmount() == 1000, "Undo should reverse the discount first.");
        agency.redo();
        require(agency.transactions()[0].getAmount() == 900, "Redo should reapply the discount.");

        agency.createServiceRequest(new ServiceRequest(160, 60, 1, "Integrated Workflow Service", yesterday, "PENDING"));
        require(agency.processNextServiceRequest().getVehicleId() == 60, "Integrated workflow should process vehicle 60.");
        require(agency.findVehicle(60).getStatus().equals(Vehicle.IN_SERVICE), "Vehicle 60 should be in service.");
        agency.completeCurrentService(300);
        require(agency.findVehicle(60).getStatus().equals(Vehicle.AVAILABLE), "Vehicle 60 should return to available.");
        require(agency.serviceHistoryNewestFirst()[0].getVehicleId() == 60, "Latest service should be on top of history stack.");

        System.out.println("Smoke test passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
