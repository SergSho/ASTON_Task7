package ru.shokhinsergey.springproject.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.shokhinsergey.springproject.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
}
