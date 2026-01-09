package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.persistence.sql.entity.UserSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserSqlEntity, Long> {
    Optional<UserSqlEntity> findByUsername(String username);

    java.util.List<UserSqlEntity> findByRoleNot(it.ucdm.leisure.dinnerplan.features.user.Role role);
}
