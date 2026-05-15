package tribhuwansingh_2341019538.service;

import tribhuwansingh_2341019538.dao.BookDAO;
import tribhuwansingh_2341019538.dao.LoanDAO;
import tribhuwansingh_2341019538.dao.MemberDAO;
import tribhuwansingh_2341019538.db.ConnectionManager;
import tribhuwansingh_2341019538.model.Book;
import tribhuwansingh_2341019538.model.Loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDate;

public class TransactionService {
    private final MemberDAO memberDAO = new MemberDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    public int processLoan(int memberId, int bookId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            Savepoint beforeLoanInsert = null;
            try {
                if (memberDAO.findById(connection, memberId).isEmpty()) {
                    throw new IllegalArgumentException("Invalid member ID.");
                }

                Book book = bookDAO.findById(connection, bookId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid book ID."));
                if (!book.isAvailable()) {
                    throw new IllegalStateException("Book is not available.");
                }

                if (!bookDAO.updateAvailability(connection, bookId, false)) {
                    throw new SQLException("Could not update book availability.");
                }

                beforeLoanInsert = connection.setSavepoint("before_loan_insert");
                int loanId = loanDAO.create(connection, memberId, bookId, LocalDate.now());

                if (!memberDAO.updateActiveLoans(connection, memberId, 1)) {
                    connection.rollback(beforeLoanInsert);
                    throw new SQLException("Could not update member active loan count.");
                }

                connection.commit();
                return loanId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                if (beforeLoanInsert != null) {
                    try {
                        connection.releaseSavepoint(beforeLoanInsert);
                    } catch (SQLException ignored) {
                    }
                }
                connection.setAutoCommit(true);
            }
        }
    }

    public void returnBook(int loanId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Loan loan = loanDAO.findById(connection, loanId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid loan ID."));
                if (loan.getReturnDate() != null) {
                    throw new IllegalStateException("This loan has already been returned.");
                }

                if (!loanDAO.markReturned(connection, loanId, LocalDate.now())) {
                    throw new SQLException("Could not update return date.");
                }
                if (!bookDAO.updateAvailability(connection, loan.getBookId(), true)) {
                    throw new SQLException("Could not mark book as available.");
                }
                if (!memberDAO.updateActiveLoans(connection, loan.getMemberId(), -1)) {
                    throw new SQLException("Could not update member active loan count.");
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void demonstrateRollback() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Books(title, author, isbn, available) VALUES (?, ?, ?, TRUE)")) {
                statement.setString(1, "Rollback Demo Book");
                statement.setString(2, "System");
                statement.setString(3, "ROLLBACK-DEMO-" + System.nanoTime());
                statement.executeUpdate();
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
