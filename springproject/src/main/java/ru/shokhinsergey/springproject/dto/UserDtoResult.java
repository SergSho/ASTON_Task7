package ru.shokhinsergey.springproject.dto;

import java.time.LocalDate;

public class UserDtoResult {

    private UserDtoResult(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
        this.created_At = builder.created_At;
    }

    private final Integer id;

    private final String name;

    private final String email;

    private final int age;

    private final LocalDate created_At;

    public static class Builder {
        private Integer id;
        private String name;
        private String email;
        private int age;
        private LocalDate created_At;

        public static Builder builder() {
            return new Builder();
        }

        public UserDtoResult build() {
            return new UserDtoResult(this);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setAge(int age) {
            this.age = age;
            return this;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setCreated_At(LocalDate created_At) {
            this.created_At = created_At;
            return this;
        }
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public LocalDate getCreated_At() {
        return created_At;
    }
}
