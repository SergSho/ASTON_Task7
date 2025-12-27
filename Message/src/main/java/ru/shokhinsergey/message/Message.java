package ru.shokhinsergey.message;

import java.util.Objects;

public class Message {

    private static final String CREATE ="create";
    private static final String DELETE ="delete";

    private String operation;
    private String email;

    public static Message instanceOfMessageOnCreate(String email){
        return new Message(CREATE, email);
    }

    public static Message instanceOfMessageOnDelete(String email){
        return new Message(DELETE, email);
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    private Message(String operation, String email) {
        this.operation = operation;
        this.email = email;
    }
    public Message() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return operation.equals(message.operation) && email.equals(message.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, email);
    }
}
