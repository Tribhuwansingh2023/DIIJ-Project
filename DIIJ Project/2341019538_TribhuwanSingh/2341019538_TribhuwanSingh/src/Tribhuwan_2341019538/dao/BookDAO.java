package tribhuwansingh_2341019538.dao;

import tribhuwansingh_2341019538.db.ConnectionManager;
import tribhuwansingh_2341019538.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {
    public int create(String title, String author, String isbn) throws SQLException {
        String sql = "INSERT INTO Books(title, author, isbn, available) VALUES (?, ?, ?, TRUE)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public Optional<Book> findById(int bookId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            return findById(connection, bookId);
        }
    }

    public Optional<Book> findById(Connection connection, int bookId) throws SQLException {
        String sql = "SELECT book_id, title, author, isbn, available FROM Books WHERE book_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBook(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Book> findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT book_id, title, author, isbn, available FROM Books WHERE isbn = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, isbn);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBook(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Book> findAll() throws SQLException {
        String sql = "SELECT book_id, title, author, isbn, available FROM Books ORDER BY book_id";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                books.add(mapBook(resultSet));
            }
            return books;
        }
    }

    public boolean updateAvailability(Connection connection, int bookId, boolean available) throws SQLException {
        String sql = "UPDATE Books SET available = ? WHERE book_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, available);
            statement.setInt(2, bookId);
            return statement.executeUpdate() == 1;
        }
    }

    private Book mapBook(ResultSet resultSet) throws SQLException {
        return new Book(
                resultSet.getInt("book_id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getString("isbn"),
                resultSet.getBoolean("available"));
    }
}
