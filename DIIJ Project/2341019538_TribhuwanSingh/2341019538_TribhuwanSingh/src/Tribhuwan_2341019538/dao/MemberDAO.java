package tribhuwansingh_2341019538.dao;

import tribhuwansingh_2341019538.db.ConnectionManager;
import tribhuwansingh_2341019538.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAO {
    public int create(String name, String email) throws SQLException {
        String sql = "INSERT INTO Members(name, email, active_loans) VALUES (?, ?, 0)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public Optional<Member> findById(int memberId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            return findById(connection, memberId);
        }
    }

    public Optional<Member> findById(Connection connection, int memberId) throws SQLException {
        String sql = "SELECT member_id, name, email, active_loans FROM Members WHERE member_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMember(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Member> findAll() throws SQLException {
        String sql = "SELECT member_id, name, email, active_loans FROM Members ORDER BY member_id";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Member> members = new ArrayList<>();
            while (resultSet.next()) {
                members.add(mapMember(resultSet));
            }
            return members;
        }
    }

    public boolean updateActiveLoans(Connection connection, int memberId, int delta) throws SQLException {
        String sql = "UPDATE Members SET active_loans = active_loans + ? WHERE member_id = ? AND active_loans + ? >= 0";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, delta);
            statement.setInt(2, memberId);
            statement.setInt(3, delta);
            return statement.executeUpdate() == 1;
        }
    }

    private Member mapMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getInt("member_id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getInt("active_loans"));
    }
}
