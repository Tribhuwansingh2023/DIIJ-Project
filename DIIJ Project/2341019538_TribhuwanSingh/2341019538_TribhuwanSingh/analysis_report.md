# Analysis Report

## Transaction Integrity

Loan processing is handled as one explicit transaction. The service disables auto-commit, validates the member and book, marks the book unavailable, inserts the loan row, updates the member active loan count, and commits only after every step succeeds. If any step fails, the transaction is rolled back so no partial state remains.

The loan flow also creates a savepoint before inserting the loan. If a later operation fails, the code can roll back to the savepoint and then roll back the full transaction. This demonstrates how savepoints protect a multi-step operation from partial completion.

## ACID Properties

- Atomicity: loan and return operations commit all related updates together or roll back together.
- Consistency: foreign keys, unique constraints, and service validation keep records valid.
- Isolation: Derby transactions protect intermediate changes until commit.
- Durability: committed changes are stored in the embedded Derby database files.

## PreparedStatement Safety and Performance

PreparedStatement is used for application CRUD because parameter binding avoids SQL injection and handles type conversion cleanly. It also lets Derby reuse execution plans for repeated statements, which helps repeated inserts and lookups.

The benchmark module includes a Statement comparison only because the specification requires it. Normal application code uses PreparedStatement.

## Derby Indexing

The schema creates indexes on `Books(isbn)`, `Loans(member_id)`, and `Loans(return_date)`. ISBN lookup benefits from an index because it is commonly used to detect duplicates and find a specific book. Loan member and return-date indexes support active-loan and overdue-loan reporting.

## Benchmark Findings

The generated `performance_report.csv` contains local timing results from five measured runs after warm-up. In the verified run, batch inserts were faster than repeated `executeUpdate()` calls, indexed lookup was faster than full table scanning, PreparedStatement lookup was much faster than constructing repeated Statement SQL strings, and batched commit had the best insert throughput.

## Performance and Safety Tradeoffs

Frequent commits reduce the amount of work lost during a failure but add disk synchronization overhead. Batched commits are faster, but more uncommitted work may need to be retried after a failure. This project uses safe transaction boundaries for library operations and uses batched behavior only in benchmark workloads.

## Observations

Derby embedded mode is simple for a university mini project because no database server is required. The main operational requirement is correct classpath setup and disciplined resource cleanup through try-with-resources.
