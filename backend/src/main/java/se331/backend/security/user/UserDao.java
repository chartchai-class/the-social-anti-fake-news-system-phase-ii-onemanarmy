package se331.backend.security.user;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User findByUsername(String username);

    User save(User user);
    List<User> findAll();
    Optional<User> findById(Integer id);
}