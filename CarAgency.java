package com.example.carprojds2;



import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CarAgency {
    private static final int STRUCTURE_CAPACITY = 1000;

    private Customer[] customers = new Customer[32];
    private int customerCount;

    private final AVL<Vehicle> vehicles = new AVL<>();
    private int vehicleCount;

    private final QueueCAraay<ServiceRequest> maintenanceQueue = new QueueCAraay<>(STRUCTURE_CAPACITY);
    private ServiceRequest currentServiceRequest;
    private Service[] services = new Service[32];
    private int serviceCount;
    private final StackArray<Service> serviceHistory = new StackArray<>(STRUCTURE_CAPACITY);

    private final QueueCAraay<Customer> waitingQueue = new QueueCAraay<>(STRUCTURE_CAPACITY);

    private ReservationBucket[] reservationBuckets = new ReservationBucket[16];
    private int reservationBucketCount;

    private Transaction[] transactions = new Transaction[32];
    private int transactionCount;

    private final StackArray<UndoableOperation> undoStack = new StackArray<>(STRUCTURE_CAPACITY);
    private final StackArray<UndoableOperation> redoStack = new StackArray<>(STRUCTURE_CAPACITY);

    public void addCustomer(Customer customer) {
        requirePositive(customer.getCustomerId(), "Customer ID");
        if (findCustomer(customer.getCustomerId()) != null) {
            throw new IllegalArgumentException("Duplicate customer ID.");
        }
        Customer copy = customer.copy();
        addCustomerInternal(copy);
        record("Add Customer " + copy.getCustomerId(),
                () -> removeCustomerInternal(copy.getCustomerId()),
                () -> addCustomerInternal(copy.copy()));
    }

    public Customer findCustomer(int customerId) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getCustomerId() == customerId) {
                return customers[i];
            }
        }
        return null;
    }

    public void updateCustomer(Customer updated) {
        Customer customer = requireCustomer(updated.getCustomerId());
        Customer before = customer.copy();
        Customer after = updated.copy();
        customer.copyFrom(after);
        record("Update Customer " + after.getCustomerId(),
                () -> requireCustomer(before.getCustomerId()).copyFrom(before),
                () -> requireCustomer(after.getCustomerId()).copyFrom(after));
    }

    public void deleteCustomer(int customerId) {
        Customer removed = removeCustomerInternal(customerId);
        if (removed == null) {
            throw new IllegalArgumentException("Customer not found.");
        }
        Customer copy = removed.copy();
        record("Delete Customer " + customerId,
                () -> addCustomerInternal(copy.copy()),
                () -> removeCustomerInternal(customerId));
    }

    public Customer[] customers() {
        Customer[] result = new Customer[customerCount];
        for (int i = 0; i < customerCount; i++) {
            result[i] = customers[i];
        }
        return result;
    }

    public void addVehicle(Vehicle vehicle) {
        validateVehicle(vehicle);
        validateVehicleOwner(vehicle.getOwnerCustomerId());
        if (findVehicle(vehicle.getVehicleId()) != null) {
            throw new IllegalArgumentException("Duplicate vehicle ID.");
        }
        Vehicle copy = vehicle.copy();
        addVehicleInternal(copy);
        record("Add Vehicle " + copy.getVehicleId(),
                () -> removeVehicleInternal(copy.getVehicleId()),
                () -> addVehicleInternal(copy.copy()));
    }

    public Vehicle findVehicle(int vehicleId) {
        return findVehicle(vehicles.getRoot(), vehicleId);
    }

    public void updateVehicle(Vehicle updated) {
        validateVehicle(updated);
        validateVehicleOwner(updated.getOwnerCustomerId());
        Vehicle vehicle = requireVehicle(updated.getVehicleId());
        Vehicle before = vehicle.copy();
        Vehicle after = updated.copy();
        after.setStatus(vehicle.getStatus());
        after.setReceiveStartDate(vehicle.getReceiveStartDate());
        after.setReceiveEndDate(vehicle.getReceiveEndDate());
        vehicle.copyFrom(after);
        record("Update Vehicle " + after.getVehicleId(),
                () -> requireVehicle(before.getVehicleId()).copyFrom(before),
                () -> requireVehicle(after.getVehicleId()).copyFrom(after));
    }

    public void changeVehicleStatus(int vehicleId, String status) {
        Vehicle vehicle = requireVehicle(vehicleId);
        String before = vehicle.getStatus();
        vehicle.setStatus(status);
        record("Change Vehicle Status " + vehicleId,
                () -> requireVehicle(vehicleId).setStatus(before),
                () -> requireVehicle(vehicleId).setStatus(status));
    }

    public void markSold(int vehicleId) {
        Vehicle vehicle = requireVehicle(vehicleId);
        if (vehicle.getStatus().equals(Vehicle.SOLD)) {
            throw new IllegalArgumentException("Vehicle is already sold.");
        }
        changeVehicleStatus(vehicleId, Vehicle.SOLD);
    }

    public void receiveVehicle(int vehicleId, String startDateValue, String endDateValue) {
        Vehicle vehicle = requireVehicle(vehicleId);
        if (vehicle.getStatus().equals(Vehicle.SOLD)) {
            throw new IllegalArgumentException("Sold cars cannot be received.");
        }
        LocalDate startDate = parseDate(startDateValue, "Receive start date");
        LocalDate endDate = parseDate(endDateValue, "Receive end date");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Receive end date must be after or equal to receive start date.");
        }
        if (!vehicle.getReceiveEndDate().isEmpty()) {
            LocalDate previousEndDate = parseDate(vehicle.getReceiveEndDate(), "Previous receive end date");
            if (!startDate.isAfter(previousEndDate)) {
                throw new IllegalArgumentException("New receive start date must be after the previous receive end date.");
            }
        }
        Vehicle before = vehicle.copy();
        vehicle.setReceiveStartDate(startDate.toString());
        vehicle.setReceiveEndDate(endDate.toString());
        vehicle.setStatus(Vehicle.RECEIVED);
        Vehicle after = vehicle.copy();
        record("Receive Vehicle " + vehicleId,
                () -> requireVehicle(vehicleId).copyFrom(before),
                () -> requireVehicle(vehicleId).copyFrom(after));
    }

    public void endReceiving(int vehicleId) {
        Vehicle vehicle = requireVehicle(vehicleId);
        if (!vehicle.getStatus().equals(Vehicle.RECEIVED)) {
            throw new IllegalArgumentException("Vehicle is not currently received.");
        }
        Vehicle before = vehicle.copy();
        vehicle.setStatus(Vehicle.AVAILABLE);
        Vehicle after = vehicle.copy();
        record("End Receiving Vehicle " + vehicleId,
                () -> requireVehicle(vehicleId).copyFrom(before),
                () -> requireVehicle(vehicleId).copyFrom(after));
    }

    public void deleteVehicle(int vehicleId) {
        Vehicle removed = removeVehicleInternal(vehicleId);
        if (removed == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        Vehicle copy = removed.copy();
        record("Delete Vehicle " + vehicleId,
                () -> addVehicleInternal(copy.copy()),
                () -> removeVehicleInternal(vehicleId));
    }

    public Vehicle[] vehiclesAscending() {
        Vehicle[] result = new Vehicle[vehicleCount];
        fillVehiclesAscending(vehicles.getRoot(), result, new int[]{0});
        return result;
    }

    public Vehicle[] vehiclesDescending() {
        Vehicle[] result = new Vehicle[vehicleCount];
        fillVehiclesDescending(vehicles.getRoot(), result, new int[]{0});
        return result;
    }

    public int vehicleTreeHeight() {
        return vehicles.height();
    }

    public void createServiceRequest(ServiceRequest request) {
        requireCustomer(request.getCustomerId());
        requireVehicle(request.getVehicleId());
        validatePastDate(request.getDate(), "Maintenance date");
        if (findPendingRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Duplicate service request ID.");
        }
        if (findDuplicatePendingRequest(request.getVehicleId(), request.getCustomerId(), request.getServiceType()) != null) {
            throw new IllegalArgumentException("This maintenance request already exists for the same customer, vehicle, and service type.");
        }
        enqueueOrFail(maintenanceQueue, request.copy(), "Maintenance queue is full.");
    }

    public ServiceRequest processNextServiceRequest() {
        if (currentServiceRequest != null) {
            throw new IllegalStateException("Complete the current service request first.");
        }
        ServiceRequest request = maintenanceQueue.dequeue();
        if (request == null) {
            throw new IllegalStateException("Maintenance queue is empty.");
        }
        Vehicle vehicle = requireVehicle(request.getVehicleId());
        String beforeStatus = vehicle.getStatus();
        if (!vehicle.getStatus().equals(Vehicle.AVAILABLE)) {
            enqueueOrFail(maintenanceQueue, request, "Maintenance queue is full.");
            throw new IllegalStateException("Vehicle must be AVAILABLE before service can start.");
        }
        vehicle.setStatus(Vehicle.IN_SERVICE);
        request.setStatus("PROCESSING");
        currentServiceRequest = request;
        ServiceRequest savedRequest = request.copy();
        record("Process Service Request " + savedRequest.getRequestId(),
                () -> {
                    if (currentServiceRequest != null
                            && currentServiceRequest.getRequestId() == savedRequest.getRequestId()) {
                        currentServiceRequest = null;
                    }
                    ServiceRequest restored = savedRequest.copy();
                    restored.setStatus("PENDING");
                    enqueueFront(maintenanceQueue, restored, "Maintenance queue is full.");
                    requireVehicle(savedRequest.getVehicleId()).setStatus(beforeStatus);
                },
                () -> {
                    ServiceRequest next = removeRequestFromQueue(savedRequest.getRequestId());
                    if (next == null) {
                        next = savedRequest.copy();
                    }
                    next.setStatus("PROCESSING");
                    requireVehicle(next.getVehicleId()).setStatus(Vehicle.IN_SERVICE);
                    currentServiceRequest = next;
                });
        return request;
    }

    public Service completeCurrentService(double cost) {
        validateNonNegative(cost, "Service cost");
        if (currentServiceRequest == null) {
            throw new IllegalStateException("No service request is currently being processed.");
        }
        ServiceRequest request = currentServiceRequest;
        Vehicle vehicle = requireVehicle(request.getVehicleId());
        Service service = new Service(request.getVehicleId(), request.getCustomerId(),
                request.getServiceType(), request.getDate(), cost, "COMPLETED");
        addServiceInternal(service);
        serviceHistory.push(service);
        Transaction payment = new Transaction(request.getCustomerId(), request.getVehicleId(), cost,
                "SERVICE_PAYMENT", LocalDate.now().toString());
        addTransactionInternal(payment);
        vehicle.setStatus(Vehicle.AVAILABLE);
        currentServiceRequest = null;
        Service savedService = service.copy();
        ServiceRequest savedRequest = request.copy();
        Transaction savedPayment = payment.copy();
        record("Complete Service " + savedService.getServiceId(),
                () -> {
                    removeServiceInternal(savedService.getServiceId());
                    removeTransactionInternal(savedPayment.getTransactionId());
                    removeServiceFromHistory(savedService.getServiceId());
                    ServiceRequest restored = savedRequest.copy();
                    restored.setStatus("PENDING");
                    enqueueFront(maintenanceQueue, restored, "Maintenance queue is full.");
                    requireVehicle(savedRequest.getVehicleId()).setStatus(Vehicle.AVAILABLE);
                    currentServiceRequest = null;
                },
                () -> {
                    removeRequestFromQueue(savedRequest.getRequestId());
                    addServiceInternal(savedService.copy());
                    serviceHistory.push(savedService.copy());
                    addTransactionInternal(savedPayment.copy());
                    requireVehicle(savedRequest.getVehicleId()).setStatus(Vehicle.AVAILABLE);
                    currentServiceRequest = null;
                });
        return service;
    }

    public ServiceRequest currentServiceRequest() {
        return currentServiceRequest;
    }

    public ServiceRequest[] maintenanceRequests() {
        Object[] values = queueToArray(maintenanceQueue);
        ServiceRequest[] result = new ServiceRequest[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (ServiceRequest) values[i];
        }
        return result;
    }

    public Service[] services() {
        Service[] result = new Service[serviceCount];
        for (int i = 0; i < serviceCount; i++) {
            result[i] = services[i];
        }
        return result;
    }

    public Service[] serviceHistoryNewestFirst() {
        Object[] values = stackToArrayTopFirst(serviceHistory);
        Service[] result = new Service[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (Service) values[i];
        }
        return result;
    }

    public void addWaitingCustomer(int customerId) {
        Customer customer = requireCustomer(customerId);
        enqueueOrFail(waitingQueue, customer, "Customer waiting queue is full.");
        record("Add Waiting Customer " + customerId,
                () -> removeCustomerFromWaitingQueue(customerId),
                () -> enqueueOrFail(waitingQueue, customer, "Customer waiting queue is full."));
    }

    public Customer serveWaitingCustomer() {
        Customer customer = waitingQueue.dequeue();
        if (customer == null) {
            throw new IllegalStateException("Customer waiting queue is empty.");
        }
        record("Serve Waiting Customer " + customer.getCustomerId(),
                () -> enqueueFront(waitingQueue, customer, "Customer waiting queue is full."),
                () -> {
                    Customer servedAgain = waitingQueue.dequeue();
                    if (servedAgain == null) {
                        throw new IllegalStateException("Customer waiting queue is empty.");
                    }
                });
        return customer;
    }

    public int waitingCount() {
        return waitingQueue.getSize();
    }

    public Customer[] waitingCustomers() {
        Object[] values = queueToArray(waitingQueue);
        Customer[] result = new Customer[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (Customer) values[i];
        }
        return result;
    }

    public void addReservation(ReservationRequest request) {
        Vehicle vehicle = requireVehicle(request.getVehicleId());
        if (vehicle.getStatus().equals(Vehicle.SOLD)) {
            throw new IllegalArgumentException("Sold cars cannot be reserved.");
        }
        if (!vehicle.getStatus().equals(Vehicle.AVAILABLE)) {
            throw new IllegalArgumentException("Reservations are only for cars that are  available.");
        }
        Customer nextCustomer = waitingQueue.peek();
        if (nextCustomer == null) {
            throw new IllegalArgumentException("Add the customer to the waiting queue first.");
        }
        if (nextCustomer.getCustomerId() != request.getCustomerId()) {
            throw new IllegalArgumentException("The first customer in the waiting queue must be served before the next customer.");
        }
        LocalDate startDate = parseDate(request.getStartDate(), "Reservation start date");
        LocalDate endDate = parseDate(request.getEndDate(), "Reservation end date");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Reservation end date must be after or equal to reservation start date.");
        }
        if (hasReservationOverlap(request.getVehicleId(), startDate, endDate)) {
            throw new IllegalArgumentException("This car already has a reservation during these dates.");
        }
        validateNonNegative(request.getPrice(), "Reservation price");
        validateNonNegative(request.getDiscount(), "Reservation discount");
        if (request.getDiscount() > request.getPrice()) {
            throw new IllegalArgumentException("Reservation discount cannot be larger than the reservation price.");
        }
        if (request.getPaidAmount() <= 0) {
            throw new IllegalArgumentException("Customer must pay before reservation can leave pending status.");
        }
        request.setStatus("PAID");
        Transaction payment = new Transaction(request.getCustomerId(), request.getVehicleId(), request.getPaidAmount(),
                "RESERVATION_PAYMENT", LocalDate.now().toString());
        Customer servedCustomer = waitingQueue.dequeue();
        ReservationBucket bucket = bucketFor(request.getVehicleId(), true);
        enqueueOrFail(bucket.queue, request, "Reservation queue is full.");
        addTransactionInternal(payment);
        ReservationRequest savedRequest = request;
        Transaction savedPayment = payment.copy();
        record("Add Reservation " + request.getReservationId(),
                () -> {
                    removeReservationFromQueue(savedRequest.getReservationId());
                    removeTransactionInternal(savedPayment.getTransactionId());
                    enqueueFront(waitingQueue, servedCustomer, "Customer waiting queue is full.");
                },
                () -> {
                    Customer next = waitingQueue.dequeue();
                    if (next == null || next.getCustomerId() != savedRequest.getCustomerId()) {
                        throw new IllegalStateException("Waiting queue order changed; cannot redo reservation.");
                    }
                    enqueueOrFail(bucketFor(savedRequest.getVehicleId(), true).queue, savedRequest, "Reservation queue is full.");
                    addTransactionInternal(savedPayment.copy());
                });
    }

    public ReservationRequest processReservationForVehicle(int vehicleId) {
        Vehicle vehicle = requireVehicle(vehicleId);
        if (vehicle.getStatus().equals(Vehicle.SOLD)) {
            throw new IllegalStateException("Sold cars cannot be reserved.");
        }
        if (!vehicle.getStatus().equals(Vehicle.AVAILABLE)) {
            throw new IllegalStateException("Vehicle must be AVAILABLE before processing a reservation.");
        }
        ReservationBucket bucket = bucketFor(vehicleId, false);
        if (bucket == null || bucket.queue.isEmpty()) {
            throw new IllegalStateException("No reservation queue exists for this vehicle.");
        }
        ReservationRequest request = bucket.queue.dequeue();
        if (!request.getStatus().equals("PAID")) {
            enqueueFront(bucket.queue, request, "Reservation queue is full.");
            throw new IllegalStateException("Customer must pay before the reservation can be processed.");
        }
        String beforeStatus = vehicle.getStatus();
        request.setStatus("RESERVED");
        vehicle.setStatus(Vehicle.RESERVED);
        ReservationRequest savedRequest = request;
        record("Process Reservation " + savedRequest.getReservationId(),
                () -> {
                    savedRequest.setStatus("PAID");
                    requireVehicle(vehicleId).setStatus(beforeStatus);
                    enqueueFront(bucketFor(vehicleId, true).queue, savedRequest, "Reservation queue is full.");
                },
                () -> {
                    ReservationRequest next = removeReservationFromQueue(savedRequest.getReservationId());
                    if (next == null) {
                        next = savedRequest;
                    }
                    next.setStatus("RESERVED");
                    requireVehicle(vehicleId).setStatus(Vehicle.RESERVED);
                });
        return request;
    }

    public ReservationRequest[] reservations() {
        int total = 0;
        for (int i = 0; i < reservationBucketCount; i++) {
            total += reservationBuckets[i].queue.getSize();
        }
        ReservationRequest[] result = new ReservationRequest[total];
        int index = 0;
        for (int i = 0; i < reservationBucketCount; i++) {
            Object[] values = queueToArray(reservationBuckets[i].queue);
            for (Object value : values) {
                result[index++] = (ReservationRequest) value;
            }
        }
        return result;
    }

    public void createTransaction(Transaction transaction) {
        validateTransaction(transaction);
        if (findTransaction(transaction.getTransactionId()) != null) {
            throw new IllegalArgumentException("Duplicate transaction ID.");
        }
        Transaction copy = transaction.copy();
        Vehicle vehicle = findVehicle(copy.getVehicleId());
        String beforeStatus = vehicle == null ? null : vehicle.getStatus();
        int beforeOwnerCustomerId = vehicle == null ? 0 : vehicle.getOwnerCustomerId();
        if (copy.getTransactionType().equalsIgnoreCase("SALE") && vehicle != null
                && vehicle.getStatus().equals(Vehicle.SOLD)) {
            throw new IllegalArgumentException("Vehicle is already sold.");
        }
        addTransactionInternal(copy);
        if (copy.getTransactionType().equalsIgnoreCase("SALE") && vehicle != null) {
            vehicle.setStatus(Vehicle.SOLD);
            vehicle.setOwnerCustomerId(copy.getCustomerId());
        }
        record("Create Transaction " + copy.getTransactionId(),
                () -> {
                    removeTransactionInternal(copy.getTransactionId());
                    if (vehicle != null && beforeStatus != null) {
                        Vehicle restoredVehicle = requireVehicle(copy.getVehicleId());
                        restoredVehicle.setStatus(beforeStatus);
                        restoredVehicle.setOwnerCustomerId(beforeOwnerCustomerId);
                    }
                },
                () -> {
                    addTransactionInternal(copy.copy());
                    if (copy.getTransactionType().equalsIgnoreCase("SALE") && findVehicle(copy.getVehicleId()) != null) {
                        requireVehicle(copy.getVehicleId()).setStatus(Vehicle.SOLD);
                        requireVehicle(copy.getVehicleId()).setOwnerCustomerId(copy.getCustomerId());
                    }
                });
    }

    public void applyDiscount(int transactionId, double discount) {
        validateNonNegative(discount, "Discount");
        Transaction transaction = requireTransaction(transactionId);
        if (discount > transaction.getAmount()) {
            throw new IllegalArgumentException("Discount cannot be larger than the transaction amount.");
        }
        double before = transaction.getAmount();
        double after = before - discount;
        transaction.setAmount(after);
        record("Apply Discount " + transactionId,
                () -> requireTransaction(transactionId).setAmount(before),
                () -> requireTransaction(transactionId).setAmount(after));
    }

    public Transaction[] transactions() {
        Transaction[] result = new Transaction[transactionCount];
        for (int i = 0; i < transactionCount; i++) {
            result[i] = transactions[i];
        }
        return result;
    }

    public String undo() {
        UndoableOperation operation = undoStack.pop();
        if (operation == null) {
            return "Undo stack is empty.";
        }
        operation.undo();
        redoStack.push(operation);
        return "Undone: " + operation.getName();
    }

    public String redo() {
        UndoableOperation operation = redoStack.pop();
        if (operation == null) {
            return "Redo stack is empty.";
        }
        operation.redo();
        undoStack.push(operation);
        return "Redone: " + operation.getName();
    }

    public String[] undoNames() {
        return operationNames(undoStack);
    }

    public String[] redoNames() {
        return operationNames(redoStack);
    }

    public String report() {
        int available = 0;
        int sold = 0;
        int inService = 0;
        int reserved = 0;
        int received = 0;
        Vehicle[] vehicleList = vehiclesAscending();
        for (Vehicle vehicle : vehicleList) {
            if (vehicle.getStatus().equals(Vehicle.AVAILABLE)) {
                available++;
            } else if (vehicle.getStatus().equals(Vehicle.SOLD)) {
                sold++;
            } else if (vehicle.getStatus().equals(Vehicle.IN_SERVICE)) {
                inService++;
            } else if (vehicle.getStatus().equals(Vehicle.RESERVED)) {
                reserved++;
            } else if (vehicle.getStatus().equals(Vehicle.RECEIVED)) {
                received++;
            }
        }

        double totalTransactions = 0;
        double netProfit = 0;
        for (int i = 0; i < transactionCount; i++) {
            totalTransactions += transactions[i].getAmount();
            if (transactions[i].getTransactionType().equalsIgnoreCase("REFUND")) {
                netProfit -= Math.abs(transactions[i].getAmount());
            } else if (!transactions[i].getTransactionType().equalsIgnoreCase("SALE")) {
                netProfit += transactions[i].getAmount();
            }
        }
        for (Vehicle vehicle : vehicleList) {
            if (vehicle.getStatus().equals(Vehicle.SOLD)) {
                netProfit += vehicle.getPrice();
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Car Agency Report\n\n");
        builder.append("Customers: ").append(customerCount).append('\n');
        builder.append("Vehicles: ").append(vehicleCount).append('\n');
        builder.append("AVL height: ").append(vehicleTreeHeight()).append('\n');
        builder.append("Available vehicles: ").append(available).append('\n');
        builder.append("Sold vehicles: ").append(sold).append('\n');
        builder.append("In service vehicles: ").append(inService).append('\n');
        builder.append("Reserved vehicles: ").append(reserved).append('\n');
        builder.append("Received vehicles: ").append(received).append('\n');
        builder.append("Pending maintenance requests: ").append(maintenanceQueue.getSize()).append('\n');
        builder.append("Waiting customers: ").append(waitingQueue.getSize()).append('\n');
        builder.append("Reservations: ").append(reservations().length).append('\n');
        builder.append("Completed services: ").append(serviceCount).append('\n');
        builder.append("Transactions: ").append(transactionCount).append('\n');
        builder.append("Total transaction amount: ").append(String.format("%.2f", totalTransactions)).append("\n\n");
        builder.append("Net profit: ").append(String.format("%.2f", netProfit)).append("\n\n");
        builder.append("Cars by customer\n");
        for (int i = 0; i < customerCount; i++) {
            Customer customer = customers[i];
            builder.append(customer.getCustomerId()).append(" - ").append(customer.getName()).append(": ");
            int owned = 0;
            for (Vehicle vehicle : vehicleList) {
                if (vehicle.getOwnerCustomerId() == customer.getCustomerId()) {
                    if (owned > 0) {
                        builder.append(", ");
                    }
                    builder.append(vehicle.getVehicleId()).append(' ').append(vehicle.getMake()).append(' ').append(vehicle.getModel());
                    owned++;
                }
            }
            if (owned == 0) {
                builder.append("No cars");
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    public void loadCustomers(Path path) throws IOException {
        readCsv(path, fields -> addCustomer(new Customer(parseInt(fields, 0), fields[1], fields[2], fields[3])), 4);
    }

    public void loadVehicles(Path path) throws IOException {
        readCsv(path, fields -> {
            if (fields.length >= 8) {
                addVehicle(new Vehicle(parseInt(fields, 0), parseInt(fields, 1), fields[2], fields[3],
                        parseInt(fields, 4), parseDouble(fields, 5), fields[6], parseStatus(fields[7])));
            } else {
                addVehicle(new Vehicle(parseInt(fields, 0), fields[1], fields[2],
                        parseInt(fields, 3), parseDouble(fields, 4), fields[5], parseStatus(fields[6])));
            }
        }, 7);
    }

    public void loadServiceRequests(Path path) throws IOException {
        readCsv(path, fields -> createServiceRequest(new ServiceRequest(parseInt(fields, 0), parseInt(fields, 1),
                parseInt(fields, 2), fields[3], fields[4], valueOrDefault(fields, 5, "PENDING"))), 5);
    }

    public void loadReservations(Path path) throws IOException {
        readCsv(path, fields -> {
            if (fields.length >= 6) {
                if (fields.length >= 8) {
                    addReservation(new ReservationRequest(parseInt(fields, 0), parseInt(fields, 1), parseInt(fields, 2),
                            fields[3], fields[4], parseDouble(fields, 5), parseDouble(fields, 6), fields[7]));
                } else {
                    addReservation(new ReservationRequest(parseInt(fields, 0), parseInt(fields, 1), parseInt(fields, 2),
                            fields[3], fields[4], fields[5]));
                }
            } else {
                addReservation(new ReservationRequest(parseInt(fields, 0), parseInt(fields, 1), parseInt(fields, 2),
                        fields[3], fields[3], valueOrDefault(fields, 4, "PENDING")));
            }
        }, 4);
    }

    public void loadServices(Path path) throws IOException {
        readCsv(path, fields -> {
            Service service = new Service(parseInt(fields, 0), parseInt(fields, 1), parseInt(fields, 2),
                    fields[3], fields[4], parseDouble(fields, 5), valueOrDefault(fields, 6, "COMPLETED"));
            addServiceInternal(service);
            serviceHistory.push(service);
        }, 6);
    }

    public void loadTransactions(Path path) throws IOException {
        readCsv(path, fields -> createTransaction(new Transaction(parseInt(fields, 0), parseInt(fields, 1),
                parseInt(fields, 2), parseDouble(fields, 3), fields[4], fields[5])), 6);
    }

    public void loadDemoData() {
        int[] ids = {50, 30, 70, 20, 40, 60, 80};
        String[] models = {"Corolla", "Civic", "Sportage", "Camry", "Accord", "Elantra", "Rav4"};
        String[] makes = {"Toyota", "Honda", "Kia", "Toyota", "Honda", "Hyundai", "Toyota"};
        if (findCustomer(1) == null) {
            addCustomer(new Customer(1, "Ahmed", "0590000001", "Ramallah"));
        }
        if (findCustomer(2) == null) {
            addCustomer(new Customer(2, "Sara", "0590000002", "Nablus"));
        }
        if (findCustomer(3) == null) {
            addCustomer(new Customer(3, "Omar", "0590000003", "Hebron"));
        }
        for (int i = 0; i < ids.length; i++) {
            if (findVehicle(ids[i]) == null) {
                int ownerId = (i % 3) + 1;
                addVehicle(new Vehicle(ids[i], ownerId, makes[i], models[i], 2020 + i, 12000 + (i * 1500), "White", Vehicle.AVAILABLE));
            }
        }
    }

    private void addCustomerInternal(Customer customer) {
        ensureCustomerCapacity();
        customers[customerCount++] = customer;
    }

    private Customer removeCustomerInternal(int customerId) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getCustomerId() == customerId) {
                Customer removed = customers[i];
                for (int j = i; j < customerCount - 1; j++) {
                    customers[j] = customers[j + 1];
                }
                customers[--customerCount] = null;
                return removed;
            }
        }
        return null;
    }

    private Customer requireCustomer(int customerId) {
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found.");
        }
        return customer;
    }

    private void addVehicleInternal(Vehicle vehicle) {
        vehicles.insert(vehicle);
        vehicleCount++;
    }

    private Vehicle removeVehicleInternal(int vehicleId) {
        Vehicle vehicle = findVehicle(vehicleId);
        if (vehicle != null) {
            vehicles.delete(new Vehicle(vehicleId, "", "", 0, 0, "", Vehicle.AVAILABLE));
            vehicleCount--;
        }
        return vehicle;
    }

    private Vehicle requireVehicle(int vehicleId) {
        Vehicle vehicle = findVehicle(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        return vehicle;
    }

    private void addServiceInternal(Service service) {
        ensureServiceCapacity();
        services[serviceCount++] = service;
    }

    private Service removeServiceInternal(int serviceId) {
        for (int i = 0; i < serviceCount; i++) {
            if (services[i].getServiceId() == serviceId) {
                Service removed = services[i];
                for (int j = i; j < serviceCount - 1; j++) {
                    services[j] = services[j + 1];
                }
                services[--serviceCount] = null;
                return removed;
            }
        }
        return null;
    }

    private void addTransactionInternal(Transaction transaction) {
        ensureTransactionCapacity();
        transactions[transactionCount++] = transaction;
    }

    private Transaction removeTransactionInternal(int transactionId) {
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getTransactionId() == transactionId) {
                Transaction removed = transactions[i];
                for (int j = i; j < transactionCount - 1; j++) {
                    transactions[j] = transactions[j + 1];
                }
                transactions[--transactionCount] = null;
                return removed;
            }
        }
        return null;
    }

    private Transaction findTransaction(int transactionId) {
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getTransactionId() == transactionId) {
                return transactions[i];
            }
        }
        return null;
    }

    private Transaction requireTransaction(int transactionId) {
        Transaction transaction = findTransaction(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found.");
        }
        return transaction;
    }

    private ServiceRequest findPendingRequest(int requestId) {
        Object[] requests = queueToArray(maintenanceQueue);
        for (Object request : requests) {
            ServiceRequest serviceRequest = (ServiceRequest) request;
            if (serviceRequest.getRequestId() == requestId) {
                return serviceRequest;
            }
        }
        return currentServiceRequest != null && currentServiceRequest.getRequestId() == requestId ? currentServiceRequest : null;
    }

    private ServiceRequest findDuplicatePendingRequest(int vehicleId, int customerId, String serviceType) {
        Object[] requests = queueToArray(maintenanceQueue);
        for (Object request : requests) {
            ServiceRequest serviceRequest = (ServiceRequest) request;
            if (isSameMaintenance(serviceRequest, vehicleId, customerId, serviceType)) {
                return serviceRequest;
            }
        }
        if (currentServiceRequest != null && isSameMaintenance(currentServiceRequest, vehicleId, customerId, serviceType)) {
            return currentServiceRequest;
        }
        return null;
    }

    private boolean isSameMaintenance(ServiceRequest request, int vehicleId, int customerId, String serviceType) {
        return request.getVehicleId() == vehicleId
                && request.getCustomerId() == customerId
                && request.getServiceType().equalsIgnoreCase(serviceType.trim());
    }

    private Customer removeCustomerFromWaitingQueue(int customerId) {
        Object[] values = queueToArray(waitingQueue);
        waitingQueue.clear();
        Customer removed = null;
        for (Object value : values) {
            Customer customer = (Customer) value;
            if (customer.getCustomerId() == customerId && removed == null) {
                removed = customer;
            } else {
                enqueueOrFail(waitingQueue, customer, "Customer waiting queue is full.");
            }
        }
        return removed;
    }

    private boolean hasReservationOverlap(int vehicleId, LocalDate startDate, LocalDate endDate) {
        ReservationBucket bucket = bucketFor(vehicleId, false);
        if (bucket == null) {
            return false;
        }
        Object[] values = queueToArray(bucket.queue);
        for (Object value : values) {
            ReservationRequest reservation = (ReservationRequest) value;
            LocalDate existingStart = parseDate(reservation.getStartDate(), "Reservation start date");
            LocalDate existingEnd = parseDate(reservation.getEndDate(), "Reservation end date");
            if (!endDate.isBefore(existingStart) && !startDate.isAfter(existingEnd)) {
                return true;
            }
        }
        return false;
    }

    private ReservationRequest removeReservationFromQueue(int reservationId) {
        ReservationRequest removed = null;
        for (int i = 0; i < reservationBucketCount; i++) {
            Object[] values = queueToArray(reservationBuckets[i].queue);
            reservationBuckets[i].queue.clear();
            for (Object value : values) {
                ReservationRequest reservation = (ReservationRequest) value;
                if (reservation.getReservationId() == reservationId && removed == null) {
                    removed = reservation;
                } else {
                    enqueueOrFail(reservationBuckets[i].queue, reservation, "Reservation queue is full.");
                }
            }
        }
        return removed;
    }

    private ServiceRequest removeRequestFromQueue(int requestId) {
        Object[] values = queueToArray(maintenanceQueue);
        maintenanceQueue.clear();
        ServiceRequest removed = null;
        for (Object value : values) {
            ServiceRequest request = (ServiceRequest) value;
            if (request.getRequestId() == requestId && removed == null) {
                removed = request;
            } else {
                enqueueOrFail(maintenanceQueue, request, "Maintenance queue is full.");
            }
        }
        return removed;
    }

    private void removeServiceFromHistory(int serviceId) {
        Object[] values = stackToArrayTopFirst(serviceHistory);
        clearStack(serviceHistory);
        for (int i = values.length - 1; i >= 0; i--) {
            Service service = (Service) values[i];
            if (service.getServiceId() != serviceId) {
                serviceHistory.push(service);
            }
        }
    }

    private void record(String name, Runnable undo, Runnable redo) {
        undoStack.push(new UndoableOperation(name, undo, redo));
        clearStack(redoStack);
    }

    private String[] operationNames(StackArray<UndoableOperation> stack) {
        Object[] values = stackToArrayTopFirst(stack);
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = ((UndoableOperation) values[i]).getName();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Vehicle findVehicle(BST<Vehicle>.TNode<Vehicle> node, int vehicleId) {
        if (node == null) {
            return null;
        }
        Vehicle vehicle = node.data;
        if (vehicle.getVehicleId() == vehicleId) {
            return vehicle;
        }
        if (vehicleId < vehicle.getVehicleId()) {
            return findVehicle((BST<Vehicle>.TNode<Vehicle>) node.left, vehicleId);
        }
        return findVehicle((BST<Vehicle>.TNode<Vehicle>) node.right, vehicleId);
    }

    @SuppressWarnings("unchecked")
    private void fillVehiclesAscending(BST<Vehicle>.TNode<Vehicle> node, Vehicle[] result, int[] index) {
        if (node == null) {
            return;
        }
        fillVehiclesAscending((BST<Vehicle>.TNode<Vehicle>) node.left, result, index);
        result[index[0]++] = node.data;
        fillVehiclesAscending((BST<Vehicle>.TNode<Vehicle>) node.right, result, index);
    }

    @SuppressWarnings("unchecked")
    private void fillVehiclesDescending(BST<Vehicle>.TNode<Vehicle> node, Vehicle[] result, int[] index) {
        if (node == null) {
            return;
        }
        fillVehiclesDescending((BST<Vehicle>.TNode<Vehicle>) node.right, result, index);
        result[index[0]++] = node.data;
        fillVehiclesDescending((BST<Vehicle>.TNode<Vehicle>) node.left, result, index);
    }

    private ReservationBucket bucketFor(int vehicleId, boolean create) {
        for (int i = 0; i < reservationBucketCount; i++) {
            if (reservationBuckets[i].vehicleId == vehicleId) {
                return reservationBuckets[i];
            }
        }
        if (!create) {
            return null;
        }
        ensureReservationCapacity();
        ReservationBucket bucket = new ReservationBucket(vehicleId);
        reservationBuckets[reservationBucketCount++] = bucket;
        return bucket;
    }

    private void ensureCustomerCapacity() {
        if (customerCount == customers.length) {
            Customer[] expanded = new Customer[customers.length * 2];
            for (int i = 0; i < customers.length; i++) {
                expanded[i] = customers[i];
            }
            customers = expanded;
        }
    }

    private void ensureServiceCapacity() {
        if (serviceCount == services.length) {
            Service[] expanded = new Service[services.length * 2];
            for (int i = 0; i < services.length; i++) {
                expanded[i] = services[i];
            }
            services = expanded;
        }
    }

    private void ensureReservationCapacity() {
        if (reservationBucketCount == reservationBuckets.length) {
            ReservationBucket[] expanded = new ReservationBucket[reservationBuckets.length * 2];
            for (int i = 0; i < reservationBuckets.length; i++) {
                expanded[i] = reservationBuckets[i];
            }
            reservationBuckets = expanded;
        }
    }

    private void ensureTransactionCapacity() {
        if (transactionCount == transactions.length) {
            Transaction[] expanded = new Transaction[transactions.length * 2];
            for (int i = 0; i < transactions.length; i++) {
                expanded[i] = transactions[i];
            }
            transactions = expanded;
        }
    }

    private void validateVehicle(Vehicle vehicle) {
        requirePositive(vehicle.getVehicleId(), "Vehicle ID");
        if (vehicle.getYear() < 1886 || vehicle.getYear()>2026) {
            throw new IllegalArgumentException("Vehicle year is invalid.");
        }
        validateNonNegative(vehicle.getPrice(), "Vehicle price");
    }

    private void validatePastDate(String value, String name) {
        try {
            LocalDate date = parseDate(value, name);
            if (!date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(name + " must be before the current date.");
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(name + " must use yyyy-MM-dd format.");
        }
    }

    private LocalDate parseDate(String value, String name) {
        try {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(name + " is required.");
            }
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(name + " must use yyyy-MM-dd format.");
        }
    }

    private void validateVehicleOwner(int ownerCustomerId) {
        if (ownerCustomerId > 0) {
            requireCustomer(ownerCustomerId);
        }
    }

    private void validateTransaction(Transaction transaction) {
        requirePositive(transaction.getTransactionId(), "Transaction ID");
        String type = transaction.getTransactionType().toUpperCase();
        boolean canBeNegative = type.equals("REFUND") || type.equals("ADJUSTMENT");
        if (!canBeNegative) {
            validateNonNegative(transaction.getAmount(), "Transaction amount");
        }
        if (type.equals("SALE")) {
            requireCustomer(transaction.getCustomerId());
            requireVehicle(transaction.getVehicleId());
        }
    }

    private void validateNonNegative(double value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative.");
        }
    }

    private void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive.");
        }
    }

    private void readCsv(Path path, CsvConsumer consumer, int minimumFields) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] fields = splitCsv(line);
                if (fields.length < minimumFields || looksLikeHeader(fields[0])) {
                    continue;
                }
                consumer.accept(fields);
            }
        }
    }

    private String[] splitCsv(String line) {
        String[] fields = line.split(",", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }

    private boolean looksLikeHeader(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i)) && value.charAt(i) != '-') {
                return true;
            }
        }
        return false;
    }

    private int parseInt(String[] fields, int index) {
        return Integer.parseInt(fields[index].trim());
    }

    private double parseDouble(String[] fields, int index) {
        return Double.parseDouble(fields[index].trim());
    }

    private String valueOrDefault(String[] fields, int index, String defaultValue) {
        if (fields.length <= index || fields[index].trim().isEmpty()) {
            return defaultValue;
        }
        return fields[index].trim();
    }

    private String parseStatus(String value) {
        return Vehicle.normalizeStatus(value);
    }

    private interface CsvConsumer {
        void accept(String[] fields);
    }

    private static class ReservationBucket {
        int vehicleId;
        QueueCAraay<ReservationRequest> queue = new QueueCAraay<>(STRUCTURE_CAPACITY);

        ReservationBucket(int vehicleId) {
            this.vehicleId = vehicleId;
        }
    }

    private <T extends Comparable<T>> void enqueueOrFail(QueueCAraay<T> queue, T value, String message) {
        if (!queue.enqueue(value)) {
            throw new IllegalStateException(message);
        }
    }

    private <T extends Comparable<T>> void enqueueFront(QueueCAraay<T> queue, T value, String message) {
        Object[] values = queueToArray(queue);
        queue.clear();
        enqueueOrFail(queue, value, message);
        for (Object oldValue : values) {
            enqueueOrFail(queue, (T) oldValue, message);
        }
    }

    private <T extends Comparable<T>> Object[] queueToArray(QueueCAraay<T> queue) {
        Object[] result = new Object[queue.getSize()];
        QueueCAraay<T> temp = new QueueCAraay<>(Math.max(queue.getSize(), 1));
        int index = 0;
        while (!queue.isEmpty()) {
            T value = queue.dequeue();
            result[index++] = value;
            temp.enqueue(value);
        }
        while (!temp.isEmpty()) {
            queue.enqueue(temp.dequeue());
        }
        return result;
    }

    private <T extends Comparable<T>> Object[] stackToArrayTopFirst(StackArray<T> stack) {
        Object[] result = new Object[stack.getSize()];
        StackArray<T> temp = new StackArray<>(Math.max(stack.getSize(), 1));
        int index = 0;
        while (!stack.isEmpty()) {
            T value = stack.pop();
            result[index++] = value;
            temp.push(value);
        }
        while (!temp.isEmpty()) {
            stack.push(temp.pop());
        }
        return result;
    }

    private <T extends Comparable<T>> void clearStack(StackArray<T> stack) {
        while (!stack.isEmpty()) {
            stack.pop();
        }
    }
}
