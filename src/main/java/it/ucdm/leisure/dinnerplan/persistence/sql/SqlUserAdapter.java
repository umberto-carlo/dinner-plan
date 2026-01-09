package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.UserRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.UserSqlEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("sql")
public class SqlUserAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    public SqlUserAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User save(User user) {
        UserSqlEntity entity = UserSqlEntity.fromDomain(user);
        return jpaUserRepository.save(entity).toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id).map(UserSqlEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        // Handle admin specifically if needed or rely on DB
        return jpaUserRepository.findByUsername(username).map(UserSqlEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll().stream()
                .map(UserSqlEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.findByUsername(username).isPresent();
    }
}
