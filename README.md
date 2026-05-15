# Library Loan Management System

## Overview

This is a comprehensive Java-based Library Loan Management System developed as part of the Database Implementation and Integrity (DIIJ) project. The system provides a console-based interface for managing library operations including member registration, book cataloging, loan processing, and return handling. It emphasizes transaction integrity, ACID properties, and performance optimization.

**Student ID:** 2341019538  
**Author:** Tribhuwan Singh  
**Date:** May 15, 2026

## Features

- **Member Management**: Register library members with unique email validation
- **Book Cataloging**: Add books with title, author, and ISBN tracking
- **Loan Processing**: Issue loans with automatic availability checking and due date calculation (14-day loan period)
- **Return Handling**: Process book returns with transaction safety
- **Reporting**: View active loans, overdue books, member lists, and book inventories
- **Transaction Integrity**: ACID-compliant operations with rollback capabilities
- **Performance Benchmarking**: Automated performance testing and CSV report generation
- **Self-Test Mode**: Built-in testing functionality for validation

## Architecture

The system follows a layered architecture:

```
src/
├── MainApp.java                 # Main application entry point
├── dao/                         # Data Access Objects
│   ├── BookDAO.java
│   ├── LoanDAO.java
│   └── MemberDAO.java
├── db/                          # Database layer
│   ├── ConnectionManager.java
│   └── DatabaseSetup.java
├── model/                       # Data models
│   ├── Book.java
│   ├── Loan.java
│   └── Member.java
├── service/                     # Business logic
│   ├── LibraryService.java
│   ├── PerformanceEvaluator.java
│   └── TransactionService.java
└── util/                        # Utilities
    ├── BenchmarkUtil.java
    └── ReportGenerator.java
```

### Database Schema

The system uses H2 (Derby) embedded database with the following tables:

- **Members**: Stores member information (ID, name, email, active loans count)
- **Books**: Stores book details (ID, title, author, ISBN, availability status)
- **Loans**: Tracks loan transactions (ID, member ID, book ID, loan/return dates)

Indexes are created on:
- Books(isbn) for duplicate detection and lookups
- Loans(member_id) for active loan queries
- Loans(return_date) for overdue loan reporting

## Technologies Used

- **Java**: Core programming language
- **H2 Database**: Embedded relational database (Derby engine)
- **JDBC**: Database connectivity
- **Maven**: Build and dependency management (assumed based on project structure)

## Setup Instructions

### Prerequisites

- Java 11 or higher
- Maven (for dependency management)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Tribhuwansingh2023/DIIJ-Project.git
   cd DIIJ-Project
   ```

2. Navigate to the project directory:
   ```bash
   cd "DIIJ Project/2341019538_TribhuwanSingh/2341019538_TribhuwanSingh"
   ```

3. Compile the project:
   ```bash
   javac -cp "lib/*" src/Tribhuwan_2341019538/*.java src/Tribhuwan_2341019538/*/*.java
   ```

4. Run the application:
   ```bash
   java -cp "lib/*:src" tribhuwansingh_2341019538.MainApp
   ```

### Self-Test Mode

To run the built-in self-test and generate performance reports:
```bash
java -cp "lib/*:src" tribhuwansingh_2341019538.MainApp --self-test
```

## Usage

The application provides a console-based menu with the following options:

1. **Register Member**: Add a new library member
2. **Add Book**: Add a new book to the catalog
3. **Process Loan**: Issue a book loan to a member
4. **Return Book**: Process a book return
5. **View Members**: Display all registered members
6. **View Books**: Display all books in catalog
7. **View Active Loans**: Show current outstanding loans
8. **View Overdue Books**: Show loans past the 14-day due date
9. **Run Benchmarks**: Execute performance tests and generate CSV report
10. **Exit**: Close the application

### Sample Operations

- Register a member: Enter name and email
- Add a book: Enter title, author, and ISBN
- Process loan: Enter member ID and book ID
- Return book: Enter loan ID

## Transaction Integrity and ACID Properties

The system ensures data integrity through:

- **Atomicity**: All loan/return operations are treated as single transactions
- **Consistency**: Foreign key constraints and business rules maintain data validity
- **Isolation**: Database transactions prevent concurrent modification issues
- **Durability**: Changes are persisted to the embedded database

Rollback demonstrations are included to show transaction safety.

## Performance Analysis

The system includes comprehensive benchmarking that measures:

- Insert performance (executeUpdate vs. batch operations)
- Query performance (table scans vs. indexed lookups)
- Statement vs. PreparedStatement efficiency
- Commit strategies (per-operation vs. batched)

Benchmark results are saved to `performance_report.csv` with metrics including execution time and throughput.

### Sample Benchmark Results

| Operation | Records | Execution Time (ms) | Throughput |
|-----------|---------|---------------------|------------|
| Insert executeUpdate | 100 | 27.311 | 3661.47 |
| Insert executeBatch | 100 | 26.721 | 3742.34 |
| Query indexed lookup | 200 | 50.622 | 3950.87 |
| PreparedStatement lookup | 200 | 22.435 | 8914.83 |
| Batched commit | 100 | 4.072 | 24559.28 |

## Reports and Analysis

- **analysis_report.md**: Detailed technical analysis of transaction integrity, ACID properties, and performance tradeoffs
- **performance_report.csv**: CSV file containing benchmark timing data

## Database Files

The H2 database files are stored in the `libraryDB/` directory:
- `service.properties`: Database configuration
- `log/`: Transaction logs
- `seg0/`: Data segments

**Note**: Do not modify database files manually.

## Error Handling

The system includes comprehensive error handling for:
- Database connection issues
- Constraint violations (duplicate emails/ISBNs)
- Invalid input validation
- Transaction rollbacks

## Future Enhancements

Potential improvements could include:
- GUI interface
- Advanced search and filtering
- Email notifications for due dates
- Multi-user concurrent access
- REST API for web integration

## License

This project is developed for educational purposes as part of the DIIJ course.
