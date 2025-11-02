# ReviewAnalytics

An analytical application for automating the calculation of ratings and analysis of product reviews. BSUIR course project.

## ✨ Features

- **Automated Rating Calculation:** Implements algorithms to calculate weighted integral ratings for each review and average ratings for products.
- **Interactive Dashboard:** Visually presents Key Performance Indicators (KPIs), top/worst-rated products, rating dynamics over time, and rating distribution by criteria.
- **Detailed Product Analytics:** Allows users to drill down into in-depth metrics, view a criteria profile (strengths/weaknesses), and see a complete list of reviews for a single product.
- **Comparative Analysis:** Provides tools to directly compare multiple products from the same category based on their average ratings across various criteria.
- **PDF Report Export:** Enables the generation and saving of comprehensive, multi-page PDF reports for the main dashboard, detailed product view, and comparative analysis pages.
- **Role-Based Access Control (RBAC):** Secure access for Analyst (can view analytics) and Administrator (full access, including user and review management) roles.
- **Review Moderation:** Tools for administrators to manage reviews (approve, reject), ensuring high data quality.
- **REST API:**  A well-defined API for robust client-server communication.

## 🛠️ Technology Stack

- **Backend:**
    - Java 17, Spring Boot, Spring Security, Spring Data JPA (Hibernate)
    - JWT (JSON Web Tokens) for authentication
    - Lombok to reduce boilerplate code
    - Apache PDFBox for server-side PDF generation
- **Frontend:**
    - JavaFX (FXML for layout, CSS for styling)
    - OkHttp for handling HTTP requests to the server
    - Jackson for JSON parsing
- **Database:** MySQL
- **Build Tool:** Maven (multi-module project)

## 🚀 Getting Started

This guide will help you set up and run the project locally for development and testing purposes.

### Prerequisites

Before you begin, ensure you have the following installed and configured:

- **JDK 17** (a distribution that includes JavaFX, such as BellSoft Liberica "Full JDK", is recommended).
- **Apache Maven** 3.6+ to build the project.
- **MySQL Server** 8.0+ (running locally or remotely).
- An **IDE** like IntelliJ IDEA for a smooth development experience.

### Installation & Run

#### 1. Clone the Repository
`git clone [YOUR_REPOSITORY_URL]
cd ReviewAnalytics`

#### 2. Configure the Database
Ensure your MySQL server instance is running. The database schema (`review_analytics`) will be created automatically on the first server run, thanks to the `createDatabaseIfNotExist=true` setting.

#### 3. Initial Data Seeding
The project includes a `data.sql` file located in `review-analytics-server/src/main/resources/`. This script populates the database with essential seed data, including default product categories (e.g., Laptops, Smartphones) and evaluation criteria (e.g., Design, Battery Life, Sound Quality).

Thanks to the Spring Boot configuration (`spring.sql.init.mode=always`), this script is **executed automatically** when the server starts. No manual action is required. This ensures that the application is immediately functional with a pre-configured set of data for testing and demonstration.

#### 4. Configure the Backend Server
The server requires environment variables for database connection and security settings. The easiest way to set them in IntelliJ IDEA is:

1.  Navigate to `Run -> Edit Configurations...`.
2.  Find or create a run configuration for `ReviewAnalyticsApplication`.
3.  In the **Environment variables** field, add the following, replacing the values with your own:
    - `DB_URL`=`jdbc:mysql://localhost:3306/review_analytics`
    - `DB_USERNAME`=`root`
    - `DB_PASSWORD`=`your_mysql_password`
    - `JWT_SECRET`=`this-is-a-very-long-and-secure-secret-key-for-jwt`
    - `JWT_EXPIRATION`=`86400000` (24 hours in milliseconds)

    > *Tip: For local development, you can skip this step and use the default values in `application.properties` by modifying them directly if needed.*

#### 5. Configure the Frontend (JavaFX)
If you are running the client application from within IntelliJ IDEA, you need to configure the VM options to include the JavaFX modules.

1.  Navigate to `Run -> Edit Configurations...`.
2.  Find or create a run configuration for `ClientApplication`.
3.  In the **VM options** field, add the following line. **Make sure to replace `path\to\your\javafx-sdk` with the actual path to your JavaFX SDK `lib` folder.**

    ```
    --module-path "path\to\your\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing
    ```
    *Example for Windows:* `--module-path "C:\javafx-sdk-17.0.1\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing`

#### 6. Run the Backend
Open the `review-analytics-server/src/main/java/com/github/stasangelov/reviewanalytics/ReviewAnalyticsApplication.java` class and run its `main()` method.
The server will start on port `8080`.

#### 7. Run the Frontend
Open the `desktop-client/src/main/java/com/github/stasangelov/reviewanalytics/client/ClientApplication.java` class and run its `main()` method.
The application login window will appear.

#### 8. Log In
On the first launch, the application creates two default users to facilitate testing:

- **Administrator:**
    - **Email:** `admin@admin`
    - **Password:** `admin`
- **Analyst:**
    - **Email:** `user@user`
    - **Password:** `user`

Use these credentials to log in and explore the application's features.