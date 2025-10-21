# ReviewAnalytics

An analytical application for automating the calculation of ratings and analysis of product reviews. BSUIR course project.

## ✨ Features

- **Automated Rating Calculation:** Implements algorithms for calculating weighted product ratings.
- **Interactive Dashboard:** Visualizes analytics, metrics, and trends.
- **Role-Based Access Control:** Secure access for Analysts and Administrators.
- **Review Moderation:** Tools for administrators to manage and ensure data quality.
- **REST API:** A well-defined API for client-server communication.

## 🛠️ Technology Stack

- **Backend:** Java, Spring Boot, Spring Security, JPA (Hibernate), Lombok
- **Database:** MySQL
- **Build Tool:** Maven

## 🚀 Getting Started

*(This section will be filled in later as write the code)*

### Prerequisites

Before running the project, you need to set the following environment variables:

**Database:**
- DB_URL: The JDBC URL of your MySQL database
  (e.g., jdbc:mysql://localhost:3306/review_analytics).
- DB_USERNAME: The username for your database.
- DB_PASSWORD: The password for your database.

**JWT:**

- JWT_SECRET: A long, secret string for signing JWT tokens.
- JWT_EXPIRATION: Token expiration time in milliseconds (e.g., 86400000 for 24 hours).