# FitPulse — Fitness Tracking System

FitPulse is a Java desktop application for tracking workouts, fitness goals, and nutrition. It uses a Swing interface, JDBC persistence, and a MySQL database. If MySQL is unavailable, the application can use its embedded H2 database fallback.

## Features

- Sign in as a member or trainer and view a personalized dashboard.
- Record cardio and strength workouts, review history, and delete entries.
- Set and track fitness goals, including calorie and workout targets.
- Log meals and review calories and macronutrients.
- Explore a concurrency demo that runs workers against synchronized shared metrics.
- Persist records through DAO classes and JDBC; use transactions for workout, goal-progress, and audit updates.

## Technology

- Java 17
- Java Swing with FlatLaf
- JDBC with MySQL Connector/J 8.3.0
- MySQL 5.7+ or MySQL 8.0+ (optional embedded H2 fallback)
- Maven project metadata is included in `pom.xml`; Windows batch scripts are also provided.

## Project layout

```text
src/com/fitpulse/       Application, UI, models, services, DAOs, utilities
src/resources/          Database configuration
sql/fitpulse_schema.sql MySQL schema and demo seed data
lib/                    Bundled JDBC and UI dependency JARs
```

## Requirements

- Windows for the included `.bat` launchers
- JDK 17 installed and available on `PATH` (or set `JAVA_HOME` to a JDK 17 installation)
- MySQL server for MySQL-backed persistence; otherwise, the app falls back to H2

## Run on Windows

1. Install JDK 17 and ensure `java` and `javac` are available in `PATH`, or set `JAVA_HOME`.
2. In the project folder, run `compile.bat`.
3. Run `run.bat` to start FitPulse, or `run_tests.bat` to launch the bundled rubric verification suite.

The launchers use the JARs in `lib/`. The test suite can include checks that connect to a database; start/configure MySQL first when you want to verify the MySQL path.

## MySQL setup

1. Start your MySQL service.
2. For a **new or disposable database**, execute the schema script from the project folder:

   ```bash
   mysql -u root -p < sql/fitpulse_schema.sql
   ```

   You can also open `sql/fitpulse_schema.sql` in MySQL Workbench and run it. **The script drops and recreates the FitPulse tables**, so do not run it against a database whose data you need to keep.
3. Review `src/resources/db.properties` and set `db.url`, `db.user`, and `db.password` for your local MySQL setup. The checked-in values are local development defaults (`root` with a blank password); replace them as needed and never commit a real password.
4. Compile and run using the steps above. The app tries MySQL first and uses H2 if MySQL is unreachable.

The schema script inserts sample accounts on each fresh schema creation. Use `password123` for the sample users:

| Role | Email |
| --- | --- |
| Member | `sarah@fitpulse.com` |
| Member | `john@fitpulse.com` |
| Trainer | `alex@fitpulse.com` |

With the H2 fallback, the application initializes a local database under the user's home directory and seeds it when it is empty.

## Architecture notes

- `model/` contains user, workout, goal, and meal domain classes, including the `User` and `Workout` hierarchies.
- `ui/` contains the Swing window, navigation panels, and dialogs.
- `service/` contains application workflows and domain operations.
- `dao/` and `dao/impl/` isolate persistence and SQL operations.
- `concurrency/` contains background workers, thread-pool helpers, and synchronized shared metrics.
- `util/` contains database configuration, validation, password hashing, and collection helpers.

## Team

**Section 25**

| Name | Student ID |
| --- | --- |
| Adarsh | `26SCSE1010899` |
| Zoha Fatima | `26SCSE1010875` |
| Sneha Agrawal | `26SCSE1010310` |
| Abhinav Singh | `26SCSE1010880` |

## Repository notes

Generated output (`bin/`, `out/`, and `target/`) should not be committed. Keep source, SQL, configuration defaults, and the required dependency JARs available in the repository so the project can be built from a fresh checkout.
