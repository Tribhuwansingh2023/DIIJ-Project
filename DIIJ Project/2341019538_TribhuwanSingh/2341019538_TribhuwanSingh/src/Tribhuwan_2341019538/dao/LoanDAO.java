package tribhuwansingh_2341019538.dao;

import tribhuwansingh_2341019538.db.ConnectionManager;
import tribhuwansingh_2341019538.model.Loan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO {
    public int create(Connection connection, int memberId, int bookId, LocalDate loanDate) throws SQLException {
        String sql = "INSERT INTO Loans(member_id, book_id, loan_date, return_date) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, memberId);
            statement.setInt(2, bookId);
            statement.setDate(3, Date.valueOf(loanDate));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public Optional<Loan> findById(Connection connection, int loanId) throws SQLException {
        String sql = "SELECT loan_id, member_id, book_id, loan_date, return_date FROM Loans WHERE loan_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, loanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapLoan(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Loan> findActiveLoans() throws SQLException {
        String sql = """
                SELECT l.loan_id, l.member_id, l.book_id, l.loan_date, l.return_date, m.name, b.title
                FROM Loans l
                JOIN Members m ON l.member_id = m.member_id
                JOIN Books b ON l.book_id = b.book_id
                WHERE l.return_date IS NULL
                ORDER BY l.loan_id
                """;
        return findLoans(sql, null);
    }

    public List<Loan> findOverdueLoans(int daysAllowed) throws SQLException {
        String sql = """
                SELECT l.loan_id, l.member_id, l.book_id, l.loan_date, l.return_date, m.name, b.title
                FROM Loans l
                JOIN Members m ON l.member_id = m.member_id
                JOIN Books b ON l.book_id = b.book_id
                WHERE l.return_date IS NULL AND l.loan_date < ?
                ORDER BY l.loan_date
                """;
        return findLoans(sql, Date.valueOf(LocalDate.now().minusDays(daysAllowed)));
    }

    public boolean markReturned(Connection connection, int loanId, LocalDate returnDate) throws SQLException {
        String sql = "UPDATE Loans SET return_date = ? WHERE loan_id = ? AND return_date IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(returnDate));
            statement.setInt(2, loanId);
            return statement.executeUpdate() == 1;
        }
    }

    public int countLoansForBook(Connection connection, int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Loans WHERE book_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private List<Loan> findLoans(String sql, Date cutoff) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (cutoff != null) {
                statement.setDate(1, cutoff);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Loan> loans = new ArrayList<>();
                while (resultSet.next()) {
                    loans.add(new Loan(
                            resultSet.getInt("loan_id"),
                            resultSet.getInt("member_id"),
                            resultSet.getInt("book_id"),
                            resultSet.getDate("loan_date"),
                            resultSet.getDate("return_date"),
                            resultSet.getString("name"),
                            resultSet.getString("title")));
                }
                return loans;
            }
        }
    }

    private Loan mapLoan(ResultSet resultSet) throws SQLException {
        return new Loan(
                resultSet.getInt("loan_id"),
                resultSet.getInt("member_id"),
                resultSet.getInt("book_id"),
                resultSet.getDate("loan_date"),
                resultSet.getDate("return_date"));
    }
}
