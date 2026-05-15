package tribhuwansingh_2341019538.service;

import tribhuwansingh_2341019538.db.ConnectionManager;
import tribhuwansingh_2341019538.util.BenchmarkUtil;
import tribhuwansingh_2341019538.util.ReportGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PerformanceEvaluator {
    private static final int RUNS = 5;
    private static final int INSERT_RECORDS = 100;
    private static final int QUERY_RECORDS = 200;
    private final ReportGenerator reportGenerator = new ReportGenerator();

    public void runBenchmarks(Path reportPath) throws Exception {
        prepareBenchmarkTables();
        warmUp();

        List<String> rows = new ArrayList<>();
        rows.add(row("Insert executeUpdate", INSERT_RECORDS, average(() -> insertWithExecuteUpdate(INSERT_RECORDS))));
        rows.add(row("Insert executeBatch", INSERT_RECORDS, average(() -> insertWithBatch(INSERT_RECORDS))));
        rows.add(row("Query full table scan", QUERY_RECORDS, average(() -> fullTableScan(QUERY_RECORDS))));
        rows.add(row("Query indexed lookup", QUERY_RECORDS, average(() -> indexedLookup(QUERY_RECORDS))));
        rows.add(row("Statement lookup", QUERY_RECORDS, average(() -> statementLookup(QUERY_RECORDS))));
        rows.add(row("PreparedStatement lookup", QUERY_RECORDS, average(() -> preparedStatementLookup(QUERY_RECORDS))));
        rows.add(row("Per-operation commit", INSERT_RECORDS, average(() -> perOperationCommit(INSERT_RECORDS))));
        rows.add(row("Batched commit", INSERT_RECORDS, average(() -> batchedCommit(INSERT_RECORDS))));

        reportGenerator.writeCsv(reportPath, rows);
    }

    private void warmUp() throws Exception {
        insertWithBatch(20);
        indexedLookup(20);
        preparedStatementLookup(20);
        clearBenchmarkRows();
    }

    private double average(BenchmarkUtil.CheckedRunnable runnable) throws Exception {
        double[] measurements = new double[RUNS];
        for (int i = 0; i < RUNS; i++) {
            clearBenchmarkRows();
            measurements[i] = BenchmarkUtil.measureMillis(runnable);
        }
        return BenchmarkUtil.average(measurements);
    }

    private String row(String operation, int records, double millis) {
        return String.format("%s,%d,%.3f,%.2f",
                operation, records, millis, BenchmarkUtil.throughput(records, millis));
    }

    private void prepareBenchmarkTables() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            executeIgnoreExists(connection, """
                    CREATE TABLE BenchmarkBooks (
                        id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        title VARCHAR(200),
                        author VARCHAR(100),
                        isbn VARCHAR(80) UNIQUE,
                        category VARCHAR(40)
                    )
                    """);
            executeIgnoreExists(connection, "CREATE INDEX idx_benchmark_books_isbn ON BenchmarkBooks(isbn)");
        }
    }

    private void executeIgnoreExists(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState()) && !"X0Y68".equals(e.getSQLState())) {
                throw e;
            }
        }
    }

    private void clearBenchmarkRows() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM BenchmarkBooks")) {
            statement.executeUpdate();
        }
    }

    private void insertWithExecuteUpdate(int records) throws SQLException {
        String sql = "INSERT INTO BenchmarkBooks(title, author, isbn, category) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < records; i++) {
                bindBenchmarkBook(statement, "EU", i);
                statement.executeUpdate();
            }
        }
    }

    private void insertWithBatch(int records) throws SQLException {
        String sql = "INSERT INTO BenchmarkBooks(title, author, isbn, category) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < records; i++) {
                bindBenchmarkBook(statement, "BATCH", i);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void fullTableScan(int records) throws SQLException {
        seedQueryRows(records);
        String sql = "SELECT COUNT(*) FROM BenchmarkBooks WHERE LOWER(title) LIKE ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < records; i++) {
                statement.setString(1, "%benchmark%");
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                }
            }
        }
    }

    private void indexedLookup(int records) throws SQLException {
        seedQueryRows(records);
        String sql = "SELECT id FROM BenchmarkBooks WHERE isbn = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < records; i++) {
                statement.setString(1, "QUERY-" + i);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        resultSet.getInt(1);
                    }
                }
            }
        }
    }

    private void statementLookup(int records) throws SQLException {
        seedQueryRows(records);
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            for (int i = 0; i < records; i++) {
                String isbn = "QUERY-" + i;
                try (ResultSet resultSet = statement.executeQuery(
                        "SELECT id FROM BenchmarkBooks WHERE isbn = '" + isbn + "'")) {
                    while (resultSet.next()) {
                        resultSet.getInt(1);
                    }
                }
            }
        }
    }

    private void preparedStatementLookup(int records) throws SQLException {
        indexedLookup(records);
    }

    private void perOperationCommit(int records) throws SQLException {
        String sql = "INSERT INTO BenchmarkBooks(title, author, isbn, category) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            try {
                for (int i = 0; i < records; i++) {
                    bindBenchmarkBook(statement, "PERCOMMIT", i);
                    statement.executeUpdate();
                    connection.commit();
                }
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void batchedCommit(int records) throws SQLException {
        String sql = "INSERT INTO BenchmarkBooks(title, author, isbn, category) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            try {
                for (int i = 0; i < records; i++) {
                    bindBenchmarkBook(statement, "BATCOMMIT", i);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void seedQueryRows(int records) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) FROM BenchmarkBooks");
             ResultSet resultSet = countStatement.executeQuery()) {
            resultSet.next();
            if (resultSet.getInt(1) >= records) {
                return;
            }
        }
        insertQueryRows(records);
    }

    private void insertQueryRows(int records) throws SQLException {
        String sql = "INSERT INTO BenchmarkBooks(title, author, isbn, category) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < records; i++) {
                statement.setString(1, "Benchmark Book " + i);
                statement.setString(2, "Benchmark Author");
                statement.setString(3, "QUERY-" + i);
                statement.setString(4, "QUERY");
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void bindBenchmarkBook(PreparedStatement statement, String prefix, int index) throws SQLException {
        String unique = prefix + "-" + System.nanoTime() + "-" + index;
        statement.setString(1, "Benchmark Book " + index);
        statement.setString(2, "Benchmark Author");
        statement.setString(3, unique);
        statement.setString(4, prefix);
    }
}
