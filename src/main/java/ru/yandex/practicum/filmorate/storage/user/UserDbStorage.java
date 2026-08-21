package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    private static final String SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_USER =
            "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String INSERT_FRIEND =
            "INSERT INTO friendship(user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String SELECT_FRIEND_IDS =
            "SELECT friend_id FROM friendship WHERE user_id = ?";
    private static final String SELECT_COMMON_FRIENDS =
            "SELECT u.* FROM users u " +
                    "JOIN friendship f1 ON u.user_id = f1.friend_id AND f1.user_id = ? " +
                    "JOIN friendship f2 ON u.user_id = f2.friend_id AND f2.user_id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<User> userRowMapper;

    @Override
    public Collection<User> findAll() {
        return jdbc.query(SELECT_ALL_USERS, userRowMapper);
    }

    @Override
    public User create(User user) {
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, new String[]{"user_id"});
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
        int rows = jdbc.update(UPDATE_USER,
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
    public User findById(Long id) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_ID, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        jdbc.update(INSERT_FRIEND, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        jdbc.update(DELETE_FRIEND, userId, friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        return Set.copyOf(jdbc.queryForList(SELECT_FRIEND_IDS, Long.class, userId));
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        return jdbc.query(SELECT_COMMON_FRIENDS, userRowMapper, userId1, userId2);
    }

}