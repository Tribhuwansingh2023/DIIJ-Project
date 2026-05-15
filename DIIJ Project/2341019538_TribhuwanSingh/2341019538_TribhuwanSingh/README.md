# Library Loan Management System with Transaction Management and Performance Evaluation

## Project Overview

This is a complete console-based Java JDBC mini project that uses Apache Derby Embedded Database. It manages library members, books, loan transactions, returns, overdue-book reporting, and performance benchmarking.

Project folder name:

```text
2341019538_TribhuwanSingh
```

Java root package:

```text
tribhuwansingh_2341019538
```

The package name is different from the folder name because Java package identifiers cannot start with numbers.

## Technology Stack

- Java 17 or newer
- JDBC
- Apache Derby Embedded Database
- Console-based CLI
- No Maven
- No Gradle
- No external framework

## Features

- Embedded Derby database using `jdbc:derby:libraryDB;create=true`
- Normalized tables: `Members`, `Books`, and `Loans`
- Primary keys, unique constraints, foreign keys, and indexes
- CRUD operations through DAO classes
- PreparedStatement usage for database operations
- Explicit transaction handling with `setAutoCommit(false)`, `commit()`, and `rollback()`
- Savepoint usage during loan processing
- Rollback demonstration for transaction integrity
- Menu-driven console interface
- Edge case validation and clean error messages
- Performance benchmarks with warm-up phase
- CSV benchmark report generation
- Separate model, DAO, service, utility, and database layers

## Folder Structure

```text
2341019538_TribhuwanSingh/
|
+-- lib/
|   +-- derby.jar
|   +-- derbyshared.jar
|   +-- derbytools.jar
|
+-- src/
|   +-- TribhuwanSingh_2341019538/
|       +-- MainApp.java
|       |
|       +-- db/
|       |   +-- ConnectionManager.java
|       |   +-- DatabaseSetup.java
|       |
|       +-- dao/
|       |   +-- MemberDAO.java
|       |   +-- BookDAO.java
|       |   +-- LoanDAO.java
|       |
|       +-- model/
|       |   +-- Member.java
|       |   +-- Book.java
|       |   +-- Loan.java
|       |
|       +-- service/
|       |   +-- LibraryService.java
|       |   +-- TransactionService.java
|       |   +-- PerformanceEvaluator.java
|       |
|       +-- util/
|           +-- BenchmarkUtil.java
|           +-- ReportGenerator.java
|
+-- README.md
+-- performance_report.csv
+-- analysis_report.md
```

## Database Design

Database URL:

```text
jdbc:derby:libraryDB;create=true
```

Tables created automatically on startup:

- `Members`
  - `member_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY`
  - `name VARCHAR(100) NOT NULL`
  - `email VARCHAR(100) UNIQUE`
  - `active_loans INT DEFAULT 0`

- `Books`
  - `book_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY`
  - `title VARCHAR(200) NOT NULL`
  - `author VARCHAR(100) NOT NULL`
  - `isbn VARCHAR(50) UNIQUE`
  - `available BOOLEAN DEFAULT TRUE`

- `Loans`
  - `loan_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY`
  - `member_id INT NOT NULL`
  - `book_id INT NOT NULL`
  - `loan_date DATE NOT NULL`
  - `return_date DATE`
  - foreign keys to `Members(member_id)` and `Books(book_id)`

Indexes:

- `idx_books_isbn` on `Books(isbn)`
- `idx_loans_member_id` on `Loans(member_id)`
- `idx_loans_return_date` on `Loans(return_date)`

## Dependency Setup

The required Derby jars are stored in `lib`.

This project includes:

```text
lib/derby.jar
lib/derbyshared.jar
lib/derbytools.jar
```

All three jars should be included in the runtime classpath using `lib/*`.

## Build Instructions

Open PowerShell in the project folder:

```powershell
cd "C:\Users\Tribhu\Downloads\DIIJ Project\2341019538_TribhuwanSingh"
```

Create the output folder:

```powershell
New-Item -ItemType Directory -Force -Path out
```

Compile all source files:

```powershell
$sources = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
javac -d out $sources
```

Note: the source code imports only standard Java APIs at compile time. Derby jars are required at runtime.

## Run Instructions

Run the console application:

```powershell
java -cp "out;lib/*" TribhuwanSingh_2341019538.MainApp
```

On Linux or macOS, use `:` instead of `;` in the classpath:

```bash
java -cp "out:lib/*" TribhuwanSingh_2341019538.MainApp
```

The first run creates the `libraryDB` Derby database directory automatically.

## CLI Menu

The application provides these options:

```text
1. Register Member
2. Add Book
3. Process Loan
4. Return Book
5. View Members
6. View Books
7. View Active Loans
8. View Overdue Books
9. Run Benchmarks
10. Exit
```

## Sample Manual Test

Start the app:

```powershell
java -cp "out;lib/*" TribhuwanSingh_2341019538.MainApp
```

Example session:

```text
1. Register Member
Name: TribhuwanSingh
Email: TribhuwanSingh@example.com
Member registered with ID: 1

2. Add Book
Title: Database Systems
Author: C. J. Date
ISBN: DB-001
Book added with ID: 1

3. Process Loan
Member ID: 1
Book ID: 1
Loan processed successfully. Loan ID: 1

7. View Active Loans

4. Return Book
Loan ID: 1
Book returned successfully.
```

## Automated Self-Test

The application includes a non-interactive self-test mode:

```powershell
java -cp "out;lib/*" TribhuwanSingh_2341019538.MainApp --self-test
```

This performs:

- database initialization
- sample member insertion
- sample book insertion
- rollback demonstration
- benchmark execution
- `performance_report.csv` generation

## Edge Cases to Test

Use the CLI to verify these scenarios:

- Register a member with an email that already exists
- Add a book with a duplicate ISBN
- Process a loan with an invalid member ID
- Process a loan with an invalid book ID
- Process a loan for a book that is already unavailable
- Return an invalid loan ID
- Return the same loan twice
- View active loans after loan creation and after return
- Run benchmarks from menu option `9`

## Benchmark Details

Benchmarking uses `System.nanoTime()` and runs a warm-up phase before measurements. Each benchmark runs 5 times and writes averaged results to:

```text
performance_report.csv
```

CSV columns:

```text
Operation,Records,ExecutionTime(ms),Throughput
```

Benchmarks included:

- Insert strategy: `executeUpdate()` versus `executeBatch()`
- Query strategy: full table scan versus indexed lookup
- Statement type: `Statement` versus `PreparedStatement`
- Transaction granularity: per-operation commit versus batched commit

## Clean Repository Instructions

Generated files can be removed safely:

```powershell
Remove-Item -Recurse -Force out, libraryDB -ErrorAction SilentlyContinue
Remove-Item -Force derby.log -ErrorAction SilentlyContinue
Get-ChildItem -Recurse -Filter *.class src | Remove-Item -Force
```

Keep these files and folders for submission:

```text
lib/
src/
README.md
analysis_report.md
performance_report.csv
```

## Project Reports

- `analysis_report.md` explains transaction integrity, ACID behavior, PreparedStatement usage, indexing, benchmark observations, and performance tradeoffs.
- `performance_report.csv` contains benchmark output generated by the application.

## Screenshots to Add in Report

Suggested screenshots:

- Main menu
- Successful member registration
- Successful book insertion
- Successful loan transaction
- Failed duplicate ISBN case
- Active loans view
- Benchmark CSV output
