package com.example.carprojds2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Function;

public class Main extends Application {
    private final CarAgency agency = new CarAgency();

    private final TableView<Customer> customerTable = new TableView<>();
    private final TableView<Vehicle> vehicleTable = new TableView<>();
    private final TableView<ServiceRequest> maintenanceTable = new TableView<>();
    private final TableView<Service> serviceTable = new TableView<>();
    private final TableView<Customer> waitingTable = new TableView<>();
    private final TableView<ReservationRequest> reservationTable = new TableView<>();
    private final TableView<Transaction> transactionTable = new TableView<>();
    private final TableView<String> undoTable = new TableView<>();
    private final TableView<String> redoTable = new TableView<>();

    private final TextArea log = new TextArea();
    private final TextArea reportArea = new TextArea();
    private final Label heightLabel = new Label("Height: -1");
    private final Label waitingCountLabel = new Label("Waiting: 0");
    private final Label currentServiceLabel = new Label("Current service: none");

    private int selectedCustomerId = -1;
    private int selectedVehicleId = -1;
    private int selectedTransactionId = -1;

    private TextField customerSearchIdField;
    private TextField customerNameField;
    private TextField customerPhoneField;
    private TextField customerAddressField;

    private TextField vehicleSearchIdField;
    private TextField ownerCustomerIdField;
    private TextField makeField;
    private TextField modelField;
    private TextField yearField;
    private TextField priceField;
    private TextField colorField;
    private ComboBox<String> statusBox;

    private TextField maintenanceSearchIdField;
    private TextField serviceVehicleIdField;
    private TextField serviceCustomerIdField;
    private TextField serviceTypeField;
    private DatePicker serviceDatePicker;
    private TextField serviceCostField;

    private TextField waitingCustomerIdField;

    private TextField reservationSearchIdField;
    private TextField reservationVehicleIdField;
    private TextField reservationCustomerIdField;
    private DatePicker reservationStartDatePicker;
    private DatePicker reservationEndDatePicker;
    private TextField reservationPriceField;
    private TextField reservationDiscountField;

    private TextField transactionSearchIdField;
    private TextField transactionCustomerIdField;
    private TextField transactionVehicleIdField;
    private TextField amountField;
    private TextField transactionTypeField;
    private DatePicker transactionDatePicker;
    private TextField discountField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Platform.runLater(() -> showExceptionPopup("Unexpected error", throwable)));
        log.setEditable(false);
        log.setPrefRowCount(5);

        TabPane tabs = new TabPane(
                tab("Customers", customerTab()),
                tab("Vehicles", vehicleTab()),
                tab("Maintenance", maintenanceTab()),
                tab("Waiting Queue", waitingTab()),
                tab("Reservations", reservationsTab()),
                tab("Transactions", transactionsTab()),
                tab("Undo / Redo", undoRedoTab()),
                tab("Report", reportTab(stage)),
                tab("Files / Demo", filesTab(stage))
        );
        configureTables();
        configureSelectionHandlers();

        BorderPane root = new BorderPane(tabs);
        root.setStyle("-fx-font-family: 'Segoe UI', Arial; -fx-font-size: 13px; -fx-base: #f7f8fb;");
        root.setBottom(log);
        BorderPane.setMargin(log, new Insets(8));
        Scene scene = new Scene(root, 1120, 760);
        stage.setTitle("Car Agency Management System");
        stage.setScene(scene);
        stage.show();
        refreshAll();
    }

    private Tab tab(String title, VBox content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private VBox customerTab() {
        customerSearchIdField = text("example: 1");
        customerNameField = text("example: Ahmad");
        customerPhoneField = text("example: 0591234567");
        customerAddressField = text("example: Ramallah");

        GridPane form = form(
                item("Search by ID", customerSearchIdField), item("Name", customerNameField),
                item("Phone", customerPhoneField), item("Address", customerAddressField)
        );
        HBox buttons = buttons(
                action("Add", () -> agency.addCustomer(readNewCustomer())),
                action("Search", this::searchCustomer),
                action("Update", () -> agency.updateCustomer(readSelectedCustomer())),
                action("Delete", () -> agency.deleteCustomer(requireSelectedCustomerId())),
                action("Clear", this::clearCustomerForm),
                undoButton(),
                redoButton()
        );
        return pane(form, buttons, customerTable);
    }

    private VBox vehicleTab() {
        vehicleSearchIdField = text("example: 50");
        ownerCustomerIdField = text("example: 1");
        makeField = text("example: Toyota");
        modelField = text("example: Corolla");
        yearField = text("example: 2022");
        priceField = text("example: 15000");
        colorField = text("example: green");
        statusBox = new ComboBox<>(FXCollections.observableArrayList(Vehicle.STATUSES));
        statusBox.setValue(Vehicle.AVAILABLE);
        statusBox.setDisable(true);

        GridPane form = form(
                item("Search by ID", vehicleSearchIdField), item("Owner Customer ID", ownerCustomerIdField),
                item("Make", makeField), item("Model", modelField),
                item("Year", yearField), item("Price", priceField), item("Color", colorField),
                item("Status", statusBox)
        );
        HBox buttons = buttons(
                action("Add", () -> agency.addVehicle(readNewVehicle())),
                action("Search", this::searchVehicle),
                action("Update", () -> agency.updateVehicle(readSelectedVehicle())),
                action("Delete", () -> agency.deleteVehicle(requireSelectedVehicleId())),
                action("Clear", this::clearVehicleForm),
                undoButton(),
                redoButton()
        );
        HBox status = new HBox(12, heightLabel);
        status.setAlignment(Pos.CENTER_LEFT);
        return pane(form, buttons, status, vehicleTable);
    }

    private VBox maintenanceTab() {
        maintenanceSearchIdField = text("example: 101");
        serviceVehicleIdField = text("example: 50");
        serviceCustomerIdField = text("example: 1");
        serviceTypeField = text("example: Oil Change");
        serviceDatePicker = new DatePicker(LocalDate.now().minusDays(1));
        serviceCostField = text("example: 120");

        GridPane form = form(
                item("Search by ID", maintenanceSearchIdField), item("Vehicle ID", serviceVehicleIdField),
                item("Customer ID", serviceCustomerIdField), item("Type", serviceTypeField),
                item("Date", serviceDatePicker), item("Cost", serviceCostField)
        );
        HBox buttons = buttons(
                action("Create Request", () -> agency.createServiceRequest(readServiceRequest())),
                action("Search", this::searchMaintenance),
                action("Process Next", this::processService),
                action("Complete", () -> agency.completeCurrentService(parseDouble(serviceCostField, "Service cost"))),
                action("Clear", this::clearMaintenanceForm),
                undoButton(),
                redoButton()
        );
        VBox tables = splitTables(maintenanceTable, serviceTable);
        return pane(form, buttons, currentServiceLabel, tables);
    }

    private VBox waitingTab() {
        waitingCustomerIdField = text("example: 1");
        GridPane form = form(item("Customer ID", waitingCustomerIdField));
        HBox buttons = buttons(
                action("Enqueue", () -> agency.addWaitingCustomer(parseInt(waitingCustomerIdField, "Customer ID"))),
                action("Serve Next", this::serveWaiting),
                action("Clear", this::clearWaitingForm),
                undoButton(),
                redoButton()
        );
        return pane(form, buttons, waitingCountLabel, waitingTable);
    }

    private VBox reservationsTab() {
        reservationSearchIdField = text("example: 1");
        reservationVehicleIdField = text("example: 50");
        reservationCustomerIdField = text("example: 1");
        reservationStartDatePicker = new DatePicker(LocalDate.now());
        reservationEndDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        reservationPriceField = text("example: 300");
        reservationDiscountField = text("example: 25");

        GridPane form = form(
                item("Search by ID", reservationSearchIdField), item("Vehicle ID", reservationVehicleIdField),
                item("Customer ID", reservationCustomerIdField), item("Start Date", reservationStartDatePicker),
                item("End Date", reservationEndDatePicker), item("Reservation Price", reservationPriceField),
                item("Discount", reservationDiscountField)
        );
        HBox buttons = buttons(
                action("Add Reservation", () -> agency.addReservation(readReservation())),
                action("Search", this::searchReservation),
                action("Process For Vehicle", this::processReservation),
                action("Clear", this::clearReservationForm),
                undoButton(),
                redoButton()
        );
        return pane(form, buttons, reservationTable);
    }

    private VBox transactionsTab() {
        transactionSearchIdField = text("example: 1");
        transactionCustomerIdField = text("example: 1");
        transactionVehicleIdField = text("example: 50");
        amountField = text("example: 1000");
        transactionTypeField = text("example: SALE");
        transactionTypeField.setText("SALE");
        transactionDatePicker = new DatePicker(LocalDate.now());
        discountField = text("example: 100");

        GridPane form = form(
                item("Search by ID", transactionSearchIdField), item("Customer ID", transactionCustomerIdField),
                item("Vehicle ID", transactionVehicleIdField), item("Amount", amountField),
                item("Type", transactionTypeField), item("Date", transactionDatePicker),
                item("Discount", discountField)
        );
        HBox buttons = buttons(
                action("Create", () -> agency.createTransaction(readTransaction())),
                action("Sale", () -> setAndCreateTransaction("SALE")),
                action("Service Payment", () -> setAndCreateTransaction("SERVICE_PAYMENT")),
                action("Deposit", () -> setAndCreateTransaction("DEPOSIT")),
                action("Refund", () -> setAndCreateTransaction("REFUND")),
                action("Search", this::searchTransaction),
                action("Apply Discount", () -> agency.applyDiscount(requireSelectedTransactionId(),
                        parseDouble(discountField, "Discount"))),
                action("Clear", this::clearTransactionForm),
                undoButton(),
                redoButton()
        );
        return pane(form, buttons, transactionTable);
    }

    private VBox undoRedoTab() {
        HBox buttons = buttons(
                action("Undo", () -> append(agency.undo())),
                action("Redo", () -> append(agency.redo()))
        );
        VBox tables = splitTables(undoTable, redoTable);
        return pane(buttons, tables);
    }

    private VBox filesTab(Stage stage) {
        HBox row1 = buttons(
                fileButton(stage, "Load Customers", path -> agency.loadCustomers(path)),
                fileButton(stage, "Load Vehicles", path -> agency.loadVehicles(path)),
                fileButton(stage, "Load Requests", path -> agency.loadServiceRequests(path)),
                fileButton(stage, "Load Reservations", path -> agency.loadReservations(path)),
                fileButton(stage, "Load Services", path -> agency.loadServices(path)),
                fileButton(stage, "Load Transactions", path -> agency.loadTransactions(path))
        );
        HBox row2 = buttons(action("Load Demo Data", agency::loadDemoData), undoButton(), redoButton());
        return pane(row1, row2);
    }

    private VBox reportTab(Stage stage) {
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        HBox buttons = buttons(action("Refresh Report", () -> reportArea.setText(agency.report())),
                saveReportButton(stage), undoButton(), redoButton());
        return pane(buttons, reportArea);
    }

    private void configureTables() {
        customerTable.getColumns().addAll(
                column("Customer ID", c -> text(c.getCustomerId())),
                column("Customer Name", Customer::getName),
                column("Phone Number", Customer::getPhone),
                column("Address", Customer::getAddress)
        );
        vehicleTable.getColumns().addAll(
                column("Vehicle ID", v -> text(v.getVehicleId())),
                column("Owner Customer ID", v -> text(v.getOwnerCustomerId())),
                column("Make", Vehicle::getMake),
                column("Model", Vehicle::getModel),
                column("Year", v -> text(v.getYear())),
                column("Price", v -> money(v.getPrice())),
                column("Color", Vehicle::getColor),
                column("Status", Vehicle::getStatus)
        );
        maintenanceTable.getColumns().addAll(
                column("Request ID", r -> text(r.getRequestId())),
                column("Vehicle ID", r -> text(r.getVehicleId())),
                column("Customer ID", r -> text(r.getCustomerId())),
                column("Service Type", ServiceRequest::getServiceType),
                column("Date", ServiceRequest::getDate),
                column("Status", ServiceRequest::getStatus)
        );
        serviceTable.getColumns().addAll(
                column("Service ID", s -> text(s.getServiceId())),
                column("Vehicle ID", s -> text(s.getVehicleId())),
                column("Customer ID", s -> text(s.getCustomerId())),
                column("Service Type", Service::getServiceType),
                column("Date", Service::getDate),
                column("Cost", s -> money(s.getCost())),
                column("Status", Service::getStatus)
        );
        waitingTable.getColumns().addAll(
                column("Customer ID", c -> text(c.getCustomerId())),
                column("Customer Name", Customer::getName),
                column("Phone Number", Customer::getPhone)
        );
        reservationTable.getColumns().addAll(
                column("Reservation ID", r -> text(r.getReservationId())),
                column("Vehicle ID", r -> text(r.getVehicleId())),
                column("Customer ID", r -> text(r.getCustomerId())),
                column("Start Date", ReservationRequest::getStartDate),
                column("End Date", ReservationRequest::getEndDate),
                column("Price", r -> money(r.getPrice())),
                column("Discount", r -> money(r.getDiscount())),
                column("Paid Amount", r -> money(r.getPaidAmount())),
                column("Status", ReservationRequest::getStatus)
        );
        transactionTable.getColumns().addAll(
                column("Transaction ID", t -> text(t.getTransactionId())),
                column("Customer ID", t -> text(t.getCustomerId())),
                column("Vehicle ID", t -> text(t.getVehicleId())),
                column("Amount", t -> money(t.getAmount())),
                column("Transaction Type", Transaction::getTransactionType),
                column("Date", Transaction::getDate)
        );
        undoTable.getColumns().add(column("Undo Stack", value -> value));
        redoTable.getColumns().add(column("Redo Stack", value -> value));
        TableView<?>[] tables = {customerTable, vehicleTable, maintenanceTable, serviceTable,
                waitingTable, reservationTable, transactionTable, undoTable, redoTable};
        for (TableView<?> table : tables) {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }
    }

    private void configureSelectionHandlers() {
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, customer) -> {
            if (customer != null) {
                fillCustomer(customer);
            }
        });
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, vehicle) -> {
            if (vehicle != null) {
                fillVehicle(vehicle);
            }
        });
        maintenanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, request) -> {
            if (request != null) {
                maintenanceSearchIdField.setText(text(request.getRequestId()));
                serviceVehicleIdField.setText(text(request.getVehicleId()));
                serviceCustomerIdField.setText(text(request.getCustomerId()));
                serviceTypeField.setText(request.getServiceType());
                serviceDatePicker.setValue(LocalDate.parse(request.getDate()));
            }
        });
        serviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, service) -> {
            if (service != null) {
                serviceVehicleIdField.setText(text(service.getVehicleId()));
                serviceCustomerIdField.setText(text(service.getCustomerId()));
                serviceTypeField.setText(service.getServiceType());
                serviceDatePicker.setValue(LocalDate.parse(service.getDate()));
                serviceCostField.setText(text(service.getCost()));
            }
        });
        waitingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, customer) -> {
            if (customer != null) {
                waitingCustomerIdField.setText(text(customer.getCustomerId()));
            }
        });
        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, reservation) -> {
            if (reservation != null) {
                reservationSearchIdField.setText(text(reservation.getReservationId()));
                reservationVehicleIdField.setText(text(reservation.getVehicleId()));
                reservationCustomerIdField.setText(text(reservation.getCustomerId()));
                reservationStartDatePicker.setValue(LocalDate.parse(reservation.getStartDate()));
                reservationEndDatePicker.setValue(LocalDate.parse(reservation.getEndDate()));
                reservationPriceField.setText(text(reservation.getPrice()));
                reservationDiscountField.setText(text(reservation.getDiscount()));
            }
        });
        transactionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, transaction) -> {
            if (transaction != null) {
                selectedTransactionId = transaction.getTransactionId();
                transactionSearchIdField.setText(text(transaction.getTransactionId()));
                transactionCustomerIdField.setText(text(transaction.getCustomerId()));
                transactionVehicleIdField.setText(text(transaction.getVehicleId()));
                amountField.setText(text(transaction.getAmount()));
                transactionTypeField.setText(transaction.getTransactionType());
                transactionDatePicker.setValue(LocalDate.parse(transaction.getDate()));
            }
        });
    }

    private Customer readNewCustomer() {
        return new Customer(customerNameField.getText().trim(),
                customerPhoneField.getText().trim(), customerAddressField.getText().trim());
    }

    private Customer readSelectedCustomer() {
        return new Customer(requireSelectedCustomerId(), customerNameField.getText().trim(),
                customerPhoneField.getText().trim(), customerAddressField.getText().trim());
    }

    private Vehicle readNewVehicle() {
        return Vehicle.create(parseInt(ownerCustomerIdField, "Owner Customer ID"), makeField.getText().trim(), modelField.getText().trim(),
                parseInt(yearField, "Year"), parseDouble(priceField, "Price"), colorField.getText().trim(),
                Vehicle.AVAILABLE);
    }

    private Vehicle readSelectedVehicle() {
        return new Vehicle(requireSelectedVehicleId(), parseInt(ownerCustomerIdField, "Owner Customer ID"),
                makeField.getText().trim(), modelField.getText().trim(),
                parseInt(yearField, "Year"), parseDouble(priceField, "Price"), colorField.getText().trim(),
                agency.findVehicle(requireSelectedVehicleId()).getStatus());
    }

    private ServiceRequest readServiceRequest() {
        return new ServiceRequest(parseInt(serviceVehicleIdField, "Vehicle ID"),
                parseInt(serviceCustomerIdField, "Customer ID"), serviceTypeField.getText().trim(),
                dateValue(serviceDatePicker, "Maintenance date"), "PENDING");
    }

    private ReservationRequest readReservation() {
        return new ReservationRequest(parseInt(reservationVehicleIdField, "Vehicle ID"),
                parseInt(reservationCustomerIdField, "Customer ID"),
                dateValue(reservationStartDatePicker, "Reservation start date"),
                dateValue(reservationEndDatePicker, "Reservation end date"),
                parseDouble(reservationPriceField, "Reservation price"),
                parseDouble(reservationDiscountField, "Reservation discount"), "PENDING");
    }

    private Transaction readTransaction() {
        return new Transaction(parseInt(transactionCustomerIdField, "Customer ID"),
                parseInt(transactionVehicleIdField, "Vehicle ID"),
                parseDouble(amountField, "Amount"),
                transactionTypeField.getText().trim().toUpperCase(),
                dateValue(transactionDatePicker, "Transaction date"));
    }

    private void searchCustomer() {
        Customer customer = agency.findCustomer(parseInt(customerSearchIdField, "Customer ID"));
        if (customer == null) {
            append("Customer not found.");
            return;
        }
        customerTable.getSelectionModel().select(customer);
        fillCustomer(customer);
        append("Customer found: " + customer.getName());
    }

    private void fillCustomer(Customer customer) {
        selectedCustomerId = customer.getCustomerId();
        customerSearchIdField.setText(text(customer.getCustomerId()));
        customerNameField.setText(customer.getName());
        customerPhoneField.setText(customer.getPhone());
        customerAddressField.setText(customer.getAddress());
    }

    private void searchVehicle() {
        Vehicle vehicle = agency.findVehicle(parseInt(vehicleSearchIdField, "Vehicle ID"));
        if (vehicle == null) {
            append("Vehicle not found.");
            return;
        }
        vehicleTable.getSelectionModel().select(vehicle);
        fillVehicle(vehicle);
        append("Vehicle found: " + vehicle.getMake() + " " + vehicle.getModel());
    }

    private void fillVehicle(Vehicle vehicle) {
        selectedVehicleId = vehicle.getVehicleId();
        vehicleSearchIdField.setText(text(vehicle.getVehicleId()));
        ownerCustomerIdField.setText(text(vehicle.getOwnerCustomerId()));
        makeField.setText(vehicle.getMake());
        modelField.setText(vehicle.getModel());
        yearField.setText(text(vehicle.getYear()));
        priceField.setText(text(vehicle.getPrice()));
        colorField.setText(vehicle.getColor());
        statusBox.setValue(vehicle.getStatus());
    }

    private void processService() {
        ServiceRequest request = agency.processNextServiceRequest();
        currentServiceLabel.setText("Current service: request " + request.getRequestId()
                + " for vehicle " + request.getVehicleId());
        append("Processing service request " + request.getRequestId() + ".");
    }

    private void serveWaiting() {
        Customer customer = agency.serveWaitingCustomer();
        append("Served waiting customer: " + customer.getName() + ".");
    }

    private void processReservation() {
        ReservationRequest request = agency.processReservationForVehicle(parseInt(reservationVehicleIdField, "Vehicle ID"));
        append("Reserved vehicle " + request.getVehicleId() + " for customer " + request.getCustomerId() + ".");
    }

    private void searchMaintenance() {
        int requestId = parseInt(maintenanceSearchIdField, "Request ID");
        for (ServiceRequest request : agency.maintenanceRequests()) {
            if (request.getRequestId() == requestId) {
                maintenanceTable.getSelectionModel().select(request);
                append("Maintenance request found: " + requestId);
                return;
            }
        }
        append("Maintenance request not found.");
    }

    private void searchReservation() {
        int reservationId = parseInt(reservationSearchIdField, "Reservation ID");
        for (ReservationRequest reservation : agency.reservations()) {
            if (reservation.getReservationId() == reservationId) {
                reservationTable.getSelectionModel().select(reservation);
                append("Reservation found: " + reservationId);
                return;
            }
        }
        append("Reservation not found.");
    }

    private void searchTransaction() {
        int transactionId = parseInt(transactionSearchIdField, "Transaction ID");
        for (Transaction transaction : agency.transactions()) {
            if (transaction.getTransactionId() == transactionId) {
                transactionTable.getSelectionModel().select(transaction);
                selectedTransactionId = transactionId;
                append("Transaction found: " + transactionId);
                return;
            }
        }
        append("Transaction not found.");
    }

    private void setAndCreateTransaction(String type) {
        transactionTypeField.setText(type);
        agency.createTransaction(readTransaction());
    }

    private void clearCustomerForm() {
        selectedCustomerId = -1;
        customerTable.getSelectionModel().clearSelection();
        customerSearchIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
    }

    private void clearVehicleForm() {
        selectedVehicleId = -1;
        vehicleTable.getSelectionModel().clearSelection();
        vehicleSearchIdField.clear();
        ownerCustomerIdField.clear();
        makeField.clear();
        modelField.clear();
        yearField.clear();
        priceField.clear();
        colorField.clear();
        statusBox.setValue(Vehicle.AVAILABLE);
    }

    private void clearMaintenanceForm() {
        maintenanceTable.getSelectionModel().clearSelection();
        serviceTable.getSelectionModel().clearSelection();
        maintenanceSearchIdField.clear();
        serviceVehicleIdField.clear();
        serviceCustomerIdField.clear();
        serviceTypeField.clear();
        serviceDatePicker.setValue(LocalDate.now().minusDays(1));
        serviceCostField.clear();
    }

    private void clearWaitingForm() {
        waitingTable.getSelectionModel().clearSelection();
        waitingCustomerIdField.clear();
    }

    private void clearReservationForm() {
        reservationTable.getSelectionModel().clearSelection();
        reservationSearchIdField.clear();
        reservationVehicleIdField.clear();
        reservationCustomerIdField.clear();
        reservationStartDatePicker.setValue(LocalDate.now());
        reservationEndDatePicker.setValue(LocalDate.now().plusDays(1));
        reservationPriceField.clear();
        reservationDiscountField.clear();
    }

    private void clearTransactionForm() {
        selectedTransactionId = -1;
        transactionTable.getSelectionModel().clearSelection();
        transactionSearchIdField.clear();
        transactionCustomerIdField.clear();
        transactionVehicleIdField.clear();
        amountField.clear();
        transactionTypeField.setText("SALE");
        transactionDatePicker.setValue(LocalDate.now());
        discountField.clear();
    }

    private int requireSelectedCustomerId() {
        if (selectedCustomerId < 1) {
            throw new IllegalArgumentException("Select a customer from the table first.");
        }
        return selectedCustomerId;
    }

    private int requireSelectedVehicleId() {
        if (selectedVehicleId < 1) {
            throw new IllegalArgumentException("Select a vehicle from the table first.");
        }
        return selectedVehicleId;
    }

    private int requireSelectedTransactionId() {
        if (selectedTransactionId < 1) {
            throw new IllegalArgumentException("Select a transaction from the table first.");
        }
        return selectedTransactionId;
    }

    private Button action(String title, ThrowingAction action) {
        Button button = new Button(title);
        button.setOnAction(event -> run(title, action));
        return button;
    }

    private Button undoButton() {
        return action("Undo", () -> append(agency.undo()));
    }

    private Button redoButton() {
        return action("Redo", () -> append(agency.redo()));
    }

    private Button fileButton(Stage stage, String title, FileAction action) {
        Button button = new Button(title);
        button.setOnAction(event -> {
            try {
                FileChooser chooser = new FileChooser();
                chooser.setTitle(title);
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv", "*.txt"));
                File file = chooser.showOpenDialog(stage);
                if (file != null) {
                    run(title, () -> action.run(file.toPath()));
                }
            } catch (Throwable throwable) {
                showExceptionPopup(title, throwable);
            }
        });
        return button;
    }

    private Button saveReportButton(Stage stage) {
        Button button = new Button("Save Report");
        button.setOnAction(event -> {
            try {
                reportArea.setText(agency.report());
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save Report");
                chooser.setInitialFileName("car-agency-report.txt");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
                File file = chooser.showSaveDialog(stage);
                if (file != null) {
                    Files.writeString(file.toPath(), reportArea.getText(), StandardCharsets.UTF_8);
                    append("Report saved to " + file.getAbsolutePath());
                }
            } catch (Throwable throwable) {
                showExceptionPopup("Save Report", throwable);
            }
        });
        return button;
    }

    private void run(String title, ThrowingAction action) {
        try {
            action.run();
            refreshAll();
            append(title + " completed.");
        } catch (Throwable throwable) {
            showExceptionPopup(title, throwable);
        }
    }

    private void showExceptionPopup(String title, Throwable throwable) {
        String message = exceptionMessage(throwable);
        append(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Operation failed");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String exceptionMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error.";
        }
        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }

    private void refreshAll() {
        refreshCustomers();
        refreshVehicles(true);
        refreshMaintenance();
        refreshWaiting();
        refreshReservations();
        refreshTransactions();
        refreshUndoRedo();
        refreshReport();
    }

    private void refreshCustomers() {
        customerTable.setItems(list(agency.customers()));
        customerTable.refresh();
    }

    private void refreshVehicles(boolean ascending) {
        vehicleTable.setItems(list(ascending ? agency.vehiclesAscending() : agency.vehiclesDescending()));
        vehicleTable.refresh();
        heightLabel.setText("Height: " + agency.vehicleTreeHeight());
    }

    private void refreshMaintenance() {
        maintenanceTable.setItems(list(agency.maintenanceRequests()));
        serviceTable.setItems(list(agency.serviceHistoryNewestFirst()));
        maintenanceTable.refresh();
        serviceTable.refresh();
        ServiceRequest current = agency.currentServiceRequest();
        currentServiceLabel.setText(current == null ? "Current service: none"
                : "Current service: request " + current.getRequestId() + " for vehicle " + current.getVehicleId());
    }

    private void refreshWaiting() {
        waitingTable.setItems(list(agency.waitingCustomers()));
        waitingTable.refresh();
        waitingCountLabel.setText("Waiting: " + agency.waitingCount());
    }

    private void refreshReservations() {
        reservationTable.setItems(list(agency.reservations()));
        reservationTable.refresh();
    }

    private void refreshTransactions() {
        transactionTable.setItems(list(agency.transactions()));
        transactionTable.refresh();
    }

    private void refreshUndoRedo() {
        undoTable.setItems(list(agency.undoNames()));
        redoTable.setItems(list(agency.redoNames()));
        undoTable.refresh();
        redoTable.refresh();
    }

    private void refreshReport() {
        reportArea.setText(agency.report());
    }

    private <T> ObservableList<T> list(T[] values) {
        ObservableList<T> result = FXCollections.observableArrayList();
        for (T value : values) {
            result.add(value);
        }
        return result;
    }

    private <T> TableColumn<T, String> column(String title, Function<T, String> extractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(extractor.apply(data.getValue())));
        return column;
    }

    private GridPane form(VBox... items) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        for (int i = 0; i < items.length; i++) {
            grid.add(items[i], i % 4, i / 4);
        }
        return grid;
    }

    private VBox item(String label, javafx.scene.Node input) {
        Label title = new Label(label);
        VBox box = new VBox(3, title, input);
        box.setMinWidth(140);
        return box;
    }

    private TextField text(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private HBox buttons(Button... buttons) {
        HBox box = new HBox(8, buttons);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 10, 8, 10));
        return box;
    }

    private VBox pane(javafx.scene.Node... nodes) {
        VBox box = new VBox(8, nodes);
        box.setPadding(new Insets(8));
        for (javafx.scene.Node node : nodes) {
            if (node instanceof TableView<?> || node instanceof VBox || node instanceof TextArea) {
                VBox.setVgrow(node, Priority.ALWAYS);
            }
        }
        return box;
    }

    private VBox splitTables(TableView<?> first, TableView<?> second) {
        VBox box = new VBox(8, first, second);
        VBox.setVgrow(first, Priority.ALWAYS);
        VBox.setVgrow(second, Priority.ALWAYS);
        return box;
    }

    private int parseInt(TextField field, String name) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a number.");
        }
    }

    private double parseDouble(TextField field, String name) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a number.");
        }
    }

    private String dateValue(DatePicker picker, String name) {
        if (picker.getValue() == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return picker.getValue().toString();
    }

    private void append(String message) {
        log.appendText(message + System.lineSeparator());
    }

    private String text(int value) {
        return String.valueOf(value);
    }

    private String text(double value) {
        return String.valueOf(value);
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }

    private String complexityText() {
        return """
                Time Complexity Analysis

                AVL vehicle search, insert, and delete: O(log n) average and worst case because the tree rebalances after updates.
                AVL ascending and descending traversal: O(n), visiting each vehicle once.
                Customer array search, update, and delete: O(n), scanning by customerId.
                Queue enqueue and dequeue: O(1), using the student's array queue with circular front and rear indexes.
                Stack push and pop: O(1), using the student's array stack top index.
                Displaying a queue, stack, transaction list, or service list: O(n), copying each item into the JavaFX table view.
                Reservation lookup by vehicleId: O(v) for the number of reservation queues, then O(1) to enqueue or dequeue that vehicle's queue.
                """;
    }

    private interface ThrowingAction {
        void run() throws Exception;
    }

    private interface FileAction {
        void run(Path path) throws Exception;
    }
}

