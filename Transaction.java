package CarProjDS2;

class Transaction implements Comparable<Transaction> {
    private static int nextTransactionId = 1;

    private int transactionId;
    private int customerId;
    private int vehicleId;
    private double amount;
    private String transactionType;
    private String date;

    Transaction(int customerId, int vehicleId, double amount, String transactionType, String date) {
        this(nextTransactionId++, customerId, vehicleId, amount, transactionType, date);
    }

    Transaction(int transactionId, int customerId, int vehicleId, double amount, String transactionType, String date) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.date = date;
        updateNextId(transactionId);
    }

    int getTransactionId() {
        return transactionId;
    }

    int getCustomerId() {
        return customerId;
    }

    int getVehicleId() {
        return vehicleId;
    }

    double getAmount() {
        return amount;
    }

    void setAmount(double amount) {
        this.amount = amount;
    }

    String getTransactionType() {
        return transactionType;
    }

    String getDate() {
        return date;
    }

    Transaction copy() {
        return new Transaction(transactionId, customerId, vehicleId, amount, transactionType, date);
    }

    @Override
    public int compareTo(Transaction other) {
        return Integer.compare(transactionId, other.transactionId);
    }

    private static void updateNextId(int usedId) {
        if (usedId >= nextTransactionId) {
            nextTransactionId = usedId + 1;
        }
    }
}
