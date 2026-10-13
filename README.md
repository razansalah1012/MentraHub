
# MentraHub — Mental Health Support Platform

MentraHub is a web-based mental health support platform developed to improve access to mental health resources and counseling services. The application provides a centralized environment for users to explore mental health support options, manage appointments, and interact with platform features based on their assigned roles.

## Key Features

- User registration and authentication
- Role-based access to application functionality
- Counseling appointment scheduling and management
- Mental health screening features
- Chatbot functionality for user interaction
- User and administrative workflows
- Database-driven information management

## Technology Stack

| Category | Technologies |
|---|---|
| Backend | Java 17, Spring MVC |
| Frontend | HTML5, CSS3, JavaScript |
| Database | MySQL |
| Build Tool | Apache Maven |
| Architecture | Model-View-Controller (MVC), Data Access Object (DAO) |
| Version Control | Git, GitHub |

## System Architecture

MentraHub follows the MVC architectural pattern to separate application responsibilities.

- **Model:** Represents application entities and business data.
- **View:** Provides user-facing interfaces.
- **Controller:** Handles incoming requests and coordinates application operations.
- **DAO:** Manages database access and persistence operations.

This separation supports maintainability, organization, and future development.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17
- Apache Maven
- MySQL Server
- A compatible Java web application environment

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/razansalah1012/MentraHub.git
   ```

2. Open the project in your preferred Java IDE.
3. Configure the MySQL database and application connection settings.
4. Install project dependencies using Maven:

   ```bash
   mvn clean install
   ```

5. Configure and run the application using the project's supported web application environment.

> Database credentials and environment-specific settings must be configured locally.

## Learning Outcomes

Through this project, the development team gained practical experience in:

- Building full-stack web applications with Java
- Applying MVC and DAO design patterns
- Integrating relational databases
- Implementing authentication and role-based functionality
- Organizing a maintainable backend codebase
- Collaborating on a multi-feature software project

## Future Improvements

- Expand automated testing
- Improve deployment documentation
- Strengthen application security validation
- Enhance accessibility and responsive design
- Introduce additional analytics and reporting capabilities

## Project Purpose

MentraHub demonstrates the development of a database-driven web application that combines backend architecture, user management, and interactive features to address a real-world problem.

