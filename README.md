# Java Student Management System

A desktop-based Student Management System developed using Java Swing, JDBC, and MySQL.

## Features
- Add student records
- View all students
- Update student details
- Delete student records
- Search students by roll number
- Store records in a MySQL database

## Technologies Used
- Java
- Java Swing
- JDBC
- MySQL
- Visual Studio Code

## Database Setup
1. Install MySQL Server and MySQL Workbench.
2. Create a database named `student_db`.
3. Create a table named `students` with columns `id`, `roll_no`, `name`, and `course`.
4. Configure the MySQL connection details in the Java application.

## Run the Project
1. Install the JDK and MySQL Server.
2. Download MySQL Connector/J and place its JAR file in the `lib` folder.
3. Configure your local MySQL credentials.
4. Compile:

   `javac -cp ".;lib/*" StudentManagementApp.java`

5. Run:

   `java -cp ".;lib/*" StudentManagementApp`
