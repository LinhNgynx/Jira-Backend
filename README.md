# Task Management System (Jira Clone)

A robust, full-stack Task Management application designed to replicate core functionalities of Jira. This project demonstrates the implementation of **Agile/Scrum methodologies**, **Data-Driven Architecture**, and **Enterprise-grade Database Design**.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

---

##  Key Features

### 1. Agile & Scrum Support
* **Sprints:** Create, start, and complete sprints.
* **Backlog Management:** Organize Epics, Stories, and Tasks.
* **Reporting:** Velocity Charts & Burndown Charts logic (Calculated from DB).

### 2. Dynamic Workflow Engine (Data-Driven)
* **Custom Workflows:** Projects are not bound to hard-coded statuses.
* **Flexible Steps:** Workflows are defined in the database (`workflows`, `workflow_steps`), allowing dynamic rendering of Kanban Boards.

### 3. Robust Task Management
* **Hierarchy:** Epic -> User Story -> Sub-task structure.
* **Rich Context:** Assignees, Priority, Story Points, Due Dates.
* **Tracking:** Activity Logs (Audit Trail) and Comments for every task.

### 4. Security & Identity
* **RBAC (Role-Based Access Control):** Separation between System Roles (Admin) and Project Roles (PO, Scrum Master, Dev).
* **Authentication:** Secure Login/Register with JWT (Coming soon).

---

## 🏗️ System Architecture

### Database Schema (17 Tables)
The database is normalized and designed for scalability, divided into 4 main clusters:

1.  **Lookup & Configuration:** `project_roles`, `issue_types`, `workflow_statuses`.
2.  **Workflow Engine:** `workflows`, `workflow_steps`, `status_transitions`.
3.  **Core Entities:** `users`, `projects`, `sprints`, `tasks`.
4.  **Tracking & Joins:** `project_members`, `task_assignees`, `activity_logs`, `notifications`, `comments`.

### Tech Stack
* **Backend:** Java 21, Spring Boot 3.3+
* **Database:** MySQL 8.0
* **ORM:** Spring Data JPA / Hibernate
* **Security:** Spring Security (Configured)
* **Tools:** Maven, Docker, Lombok

