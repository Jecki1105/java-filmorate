package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> userRowMapper;

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, userRowMapper);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setTimestamp(4, Timestamp.valueOf(user.getBirthday().atStartOfDay()));
            return ps;
        });

        Long id = jdbc.queryForObject("SELECT MAX(user_id) FROM users", Long.class);
        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rows = jdbc.update(sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Timestamp.valueOf(newUser.getBirthday().atStartOfDay()),
                newUser.getId());
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }
        return newUser;
    }

    @Override
    public Long getNextId() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbc.queryForObject(sql, Long.class) + 1;
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        String sql = "INSERT INTO friendship(user_id, friend_id, status) VALUES (?, ?, 'confirmed')";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void addFriendRequest(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        String sql = "INSERT INTO friendship(user_id, friend_id, status) VALUES (?, ?, 'pending')";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ? AND status = 'confirmed'";
        List<Long> friendIds = jdbc.queryForList(sql, Long.class, userId);
        return Set.copyOf(friendIds);
    }

    @Override
    public Set<Long> getPendingFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ? AND status = 'pending'";
        List<Long> pendingIds = jdbc.queryForList(sql, Long.class, userId);
        return Set.copyOf(pendingIds);
    }

    @Override
    public FriendshipStatus getFriendshipStatus(Long userId, Long friendId) {
        String sql = "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?";
        List<String> statuses = jdbc.queryForList(sql, String.class, userId, friendId);
        if (statuses.isEmpty()) {
            return null;
        }
        return FriendshipStatus.valueOf(statuses.get(0).toUpperCase());
    }
}