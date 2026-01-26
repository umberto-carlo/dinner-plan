# Dinner Plan 🍽️

> **Verdict:** "Ambitious project. Not just a tutorial copy. Solid foundations." — *Code Audit 2026*

Dinner Plan is a Spring Boot application designed to simplify the organization of group dinners. It handles event creation, proposal voting, and real-time communication between participants.

## 🌟 Key Features

*   **Dinner Organization**: Create events, set deadlines, and manage guest lists.
*   **Proposals & Voting**: Participants can propose locations/menus and vote.
*   **Geocoding & Maps**: Automatic address resolution for proposals.
*   **Real-time Chat**: Integrated WebSocket chat for every dinner event.
*   **Email Notifications**: Automatic invitations and reminders via email.
*   **Calendar Export**: Add confirmed events to your personal calendar (.ics).
*   **Role-Based Access**: Granular permissions for Admin, Organizer, and Participant.
*   **Mobile-First UI**: Responsive design optimized for mobile usage.

## 🏗️ Architecture

The project follows a standard Spring Boot layered architecture with some advanced features:

*   **Backend**: Java 25, Spring Boot 4.0.1
*   **Security**: Spring Security (BCrypt, Custom UserDetails, Role-based auth)
*   **Database**: Apache Derby (Embedded) / H2 ready
*   **Frontend**: Thymeleaf + Tailwind CSS (Runtime) + Vanilla CSS
*   **Real-time**: Spring WebSocket (STOMP)
*   **Internationalization**: Multi-language support (IT, EN, SV)
*   **API Documentation**: OpenAPI 3 (Swagger UI)
*   **External Services**: Geocoding (Photon/ArcGIS), SMTP Mail

### Project Structure (Current)
```
src/main/java/it/ucdm/leisure/dinnerplan/
├── config/       # Security, WebSocket & App configuration
├── features/     # Feature-based modules (User, Event, Proposal, Geocode)
├── service/      # Shared Services (Email, Calendar)
├── controller/   # Global Controllers
├── dto/          # Data Transfer Objects
├── utils/        # Utility classes
└── exception/    # Error Handling
```

## 🚀 Getting Started

### Prerequisites
*   JDK 25
*   Maven 3.8+

### Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/your-repo/dinner-plan.git
    cd dinner-plan
    ```

2.  Build the project:
    ```bash
    mvn clean install
    ```

3.  Run the application:
    ```bash
    mvn spring-boot:run
    ```

4.  Access the application:
    *   Web Interface: `http://localhost:8080`
    *   Swagger UI: `http://localhost:8080/swagger-ui.html`

### Default Users
*(If enabled in `AdminInitializer`)*
*   **Admin**: `admin` / `password`
*   **User**: `user` / `password`

## ⚙️ Configuration

### Proxy Support
The application supports running behind an HTTP proxy. Configure the following properties in `application.properties`:

```properties
# Proxy Host and Port
proxy.host=your-proxy-host
proxy.port=8080

# Optional Proxy Authentication
proxy.username=user
proxy.password=pass
```

## 🛠️ Development

### Profiles
The project uses Maven profiles to manage environments:
*   `dev` (Default): Uses embedded database, enables debug logs, `spring-boot-devtools` active.
*   `prod`: Optimized for production usage.

 Run with a specific profile:
```bash
mvn spring-boot:run -Pprod
```

## 🧪 Testing

Run unit and integration tests:
```bash
mvn test
```

---
*Developed with ❤️ by [Your Name]*
