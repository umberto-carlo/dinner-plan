package it.ucdm.leisure.dinnerplan.features.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DietaryPreference dietaryPreference = DietaryPreference.OMNIVORE;

    public User() {
    }

    public User(Long id, String username, String email, String password, Role role, String address, Double latitude, Double longitude, DietaryPreference dietaryPreference) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dietaryPreference = dietaryPreference != null ? dietaryPreference : DietaryPreference.OMNIVORE;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public DietaryPreference getDietaryPreference() {
        return dietaryPreference;
    }

    public void setDietaryPreference(DietaryPreference dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private Role role;
        private String address;
        private Double latitude;
        private Double longitude;
        private DietaryPreference dietaryPreference;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
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

        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }

        public UserBuilder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public UserBuilder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public UserBuilder dietaryPreference(DietaryPreference dietaryPreference) {
            this.dietaryPreference = dietaryPreference;
            return this;
        }

        public User build() {
            return new User(id, username, email, password, role, address, latitude, longitude, dietaryPreference);
        }
    }
}
