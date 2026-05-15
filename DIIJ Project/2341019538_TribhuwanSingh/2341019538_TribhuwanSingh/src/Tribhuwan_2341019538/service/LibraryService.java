package tribhuwansingh_2341019538.service;

import tribhuwansingh_2341019538.dao.BookDAO;
import tribhuwansingh_2341019538.dao.LoanDAO;
import tribhuwansingh_2341019538.dao.MemberDAO;
import tribhuwansingh_2341019538.model.Book;
import tribhuwansingh_2341019538.model.Loan;
import tribhuwansingh_2341019538.model.Member;

import java.sql.SQLException;
import java.util.List;

public class LibraryService {
    private static final int LOAN_PERIOD_DAYS = 14;
    private final MemberDAO memberDAO = new MemberDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final TransactionService transactionService = new TransactionService();

    public String registerMember(String name, String email) {
        if (isBlank(name) || isBlank(email)) {
            return "Name and email are required.";
        }
        try {
            int id = memberDAO.create(name.trim(), email.trim());
            return "Member registered with ID: " + id;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return "A member with this email already exists.";
            }
            return "Could not register member: " + e.getMessage();
        }
    }

    public String addBook(String title, String author, String isbn) {
        if (isBlank(title) || isBlank(author) || isBlank(isbn)) {
            return "Title, author, and ISBN are required.";
        }
        try {
            int id = bookDAO.create(title.trim(), author.trim(), isbn.trim());
            return "Book added with ID: " + id;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return "Duplicate ISBN. Book already exists.";
            }
            return "Could not add book: " + e.getMessage();
        }
    }

    public String processLoan(int memberId, int bookId) {
        try {
            int loanId = transactionService.processLoan(memberId, bookId);
            return "Loan processed successfully. Loan ID: " + loanId;
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            return "Loan failed: " + e.getMessage();
        }
    }

    public String returnBook(int loanId) {
        try {
            transactionService.returnBook(loanId);
            return "Book returned successfully.";
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            return "Return failed: " + e.getMessage();
        }
    }

    public List<Member> getMembers() throws SQLException {
        return memberDAO.findAll();
    }

    public List<Book> getBooks() throws SQLException {
        return bookDAO.findAll();
    }

    public List<Loan> getActiveLoans() throws SQLException {
        return loanDAO.findActiveLoans();
    }

    public List<Loan> getOverdueLoans() throws SQLException {
        return loanDAO.findOverdueLoans(LOAN_PERIOD_DAYS);
    }

    public String demonstrateRollback() {
        try {
            transactionService.demonstrateRollback();
            return "Rollback demonstration completed. No partial changes were committed.";
        } catch (SQLException e) {
            return "Rollback demonstration failed: " + e.getMessage();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
