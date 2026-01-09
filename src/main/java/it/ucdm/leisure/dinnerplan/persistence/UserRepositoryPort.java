package it.ucdm.leisure.dinnerplan.persistence;

import it.ucdm.leisure.dinnerplan.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    void deleteById(Long id);

    boolean existsByUsername(String username);
}
