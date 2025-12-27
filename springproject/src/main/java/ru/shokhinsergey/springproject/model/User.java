package ru.shokhinsergey.springproject.model;


import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table (name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 25)
    private String name;


    @Column(nullable = false, unique = true, length = 50)
    private String email;


    @Column(nullable = false)
    private int age;


    @Column(nullable = false)
    @ColumnDefault("current_date")
    private LocalDate created_At;

    @PrePersist
    protected void createDate() {
        created_At = LocalDate.now();
    }

    public User(String name, String email, int age) {
        this.name = name;
        if (email != null && !email.isEmpty()) email = email.toLowerCase();
        this.email = email;
        this.age = age;
    }

    public User(Integer id, String name, String email, int age) {
        this(name, email, age);
        this.id = id;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", created_at=" + created_At +
                '}';
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && Objects.equals(name, user.name) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, age);
    }
}

