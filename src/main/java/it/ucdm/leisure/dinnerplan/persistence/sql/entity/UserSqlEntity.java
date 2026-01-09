package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class UserSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public UserSqlEntity() {
    }

    public UserSqlEntity(Long id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User toDomain() {
        return new User(this.id, this.username, this.password, this.role);
    }

    public static UserSqlEntity fromDomain(User user) {
        return new UserSqlEntity(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private Role role;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserSqlEntity build() {
            return new UserSqlEntity(id, username, password, role);
        }
    }
}
