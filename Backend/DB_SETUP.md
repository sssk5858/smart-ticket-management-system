# Smart Ticket Management - Database Setup

## Why tables are getting created automatically

Current backend config uses:

- H2 in-memory database:
  - `spring.datasource.url=jdbc:h2:mem:smart_ticket_db...`
- Hibernate auto schema update:
  - `spring.jpa.hibernate.ddl-auto=update`
- Seed data loader:
  - `DataInitializer` inserts default users, SLA rules, and category mappings at startup.

So even if you do not manually create tables, Hibernate creates them automatically at runtime for development.

---

## SQL Script (MySQL) - Manual Table Creation

Use this when you want DB-controlled schema creation.

```sql
CREATE DATABASE IF NOT EXISTS smart_ticket_db;
USE smart_ticket_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN', 'SUPER_ADMIN') NOT NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS sla_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') UNIQUE,
    duration_in_hours INT
);

CREATE TABLE IF NOT EXISTS category_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255),
    category VARCHAR(255),
    assigned_team VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    status ENUM('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'OVERDUE'),
    priority ENUM('HIGH', 'MEDIUM', 'LOW'),
    category VARCHAR(255),
    assigned_team VARCHAR(255),
    resolution_notes VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    due_date DATETIME(6),
    created_by BIGINT,
    assigned_to BIGINT,
    CONSTRAINT fk_ticket_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_ticket_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT,
    action VARCHAR(255),
    updated_by VARCHAR(255),
    timestamp DATETIME(6)
);
```

---

## Optional: Switch from auto-create to manual DB control

If you want to stop Hibernate from creating/updating tables automatically:

1. Change datasource to MySQL in `src/main/resources/application.properties`
2. Set:

```properties
spring.jpa.hibernate.ddl-auto=none
```

Keep this only when your schema is already created using the SQL script above.

---

## Notes

- This document does **not** change any existing functionality.
- Current app behavior remains the same (H2 + auto-create + seed data) until you change properties explicitly.
