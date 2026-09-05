package com.example.carprojds2;

class Customer implements Comparable<Customer> {
    private static int nextCustomerId = 1;

    private int customerId;
    private String name;
    private String phone;
    private String address;

    Customer(String name, String phone, String address) {
        this(nextCustomerId++, name, phone, address);
    }

    Customer(int customerId, String name, String phone, String address) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        updateNextId(customerId);
    }

    int getCustomerId() {
        return customerId;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getPhone() {
        return phone;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    String getAddress() {
        return address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    Customer copy() {
        return new Customer(customerId, name, phone, address);
    }

    void copyFrom(Customer other) {
        name = other.name;
        phone = other.phone;
        address = other.address;
    }

    @Override
    public int compareTo(Customer other) {
        return Integer.compare(customerId, other.customerId);
    }

    private static void updateNextId(int usedId) {
        if (usedId >= nextCustomerId) {
            nextCustomerId = usedId + 1;
        }
    }
}
