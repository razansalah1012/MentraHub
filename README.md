MentraHub — Student Mental Health & Wellness Platform
A full-stack academic web application designed to help students access mental wellness resources, screenings, and counseling appointments.
Features
- Student, counselor, and administrator roles
- Registration, login, and role-based access
- Counseling appointment scheduling and availability management
- Mental health screening and self-assessment interfaces
- Educational resources, breathing exercises, feedback, and chatbot interface
Tech stack
Java 17 · Spring MVC 5.3 · Maven · MySQL · HTML · CSS · JavaScript · MVC/DAO architecture
Repository structure
- src/main/java/com/mentra/controller/ — request handlers
- src/main/java/com/mentra/dao/ — data access
- src/main/java/com/mentra/model/ — domain models
- src/main/webapp/ — web views and assets
- database/ — SQL scripts
Getting started
1. Install Java 17, Maven, MySQL, and a compatible servlet container.
2. Review the SQL files in database/ and configure a local database.
3. Review src/main/resources/application.properties and Spring XML configuration for local settings. Do not commit real credentials.
4. Build with mvn clean package and deploy the generated WAR to the configured servlet container.
Exact database initialization and deployment settings depend on your local environment; verify them before publishing step-by-step instructions.

