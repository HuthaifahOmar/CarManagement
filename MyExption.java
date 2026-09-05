package com.example.carprojds2;

public class MyExption extends Exception {
    public MyExption() {
        super();
    }
    public MyExption(String message) {
        super(message);
    }

    public static class InvalidValueException extends MyExption {
        public InvalidValueException(String message) {
            super(message);
        }
    }

    public static class DuplicateIdException extends MyExption {
        public DuplicateIdException(String message) {
            super(message);
        }
    }

    public static class NotFoundException extends MyExption {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class EmptyStructureException extends MyExption {
        public EmptyStructureException(String message) {
            super(message);
        }
    }

    public static class FullStructureException extends MyExption {
        public FullStructureException(String message) {
            super(message);
        }
    }

    public static class InvalidStatusException extends MyExption {
        public InvalidStatusException(String message) {
            super(message);
        }
    }
}
