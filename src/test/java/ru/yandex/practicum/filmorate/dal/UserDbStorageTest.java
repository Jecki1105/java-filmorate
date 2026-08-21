package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.findById(created.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    public void testFindAllUsers() {
        User user1 = createUser("user1@example.com", "login1", "User One");
        User user2 = createUser("user2@example.com", "login2", "User Two");

        userStorage.create(user1);
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    public void testUpdateUser() {
        User user = createUser("test@example.com", "login", "Name");
        User created = userStorage.create(user);

        created.setName("Updated Name");
        userStorage.update(created);

        User updated = userStorage.findById(created.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1995, 5, 5));
        return user;
    }
}
