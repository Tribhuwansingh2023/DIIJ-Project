package tribhuwansingh_2341019538.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseSetup {
    public void initializeDatabase() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            createTable(connection, """
                    CREATE TABLE Members (
                        member_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) UNIQUE,
                        active_loans INT DEFAULT 0
                    )
                    """);
            createTable(connection, """
                    CREATE TABLE Books (
                        book_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        title VARCHAR(200) NOT NULL,
                        author VARCHAR(100) NOT NULL,
                        isbn VARCHAR(50) UNIQUE,
                        available BOOLEAN DEFAULT TRUE
                    )
                    """);
            createTable(connection, """
                    CREATE TABLE Loans (
                        loan_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        member_id INT NOT NULL,
                        book_id INT NOT NULL,
                        loan_date DATE NOT NULL,
                        return_date DATE,
                        CONSTRAINT fk_loans_members FOREIGN KEY (member_id) REFERENCES Members(member_id),
                        CONSTRAINT fk_loans_books FOREIGN KEY (book_id) REFERENCES Books(book_id)
                    )
                    """);

            createIndex(connection, "CREATE INDEX idx_books_isbn ON Books(isbn)");
            createIndex(connection, "CREATE INDEX idx_loans_member_id ON Loans(member_id)");
            createIndex(connection, "CREATE INDEX idx_loans_return_date ON Loans(return_date)");
        }
    }

    private void createTable(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                throw e;
            }
        }
    }

    private void createIndex(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState()) && !"X0Y68".equals(e.getSQLState())) {
                throw e;
            }
        }
    }
}
