package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@Slf4j
public class InMemoryUserService implements UserService {

    private final UserStorage userStorage;

    @Autowired
    public InMemoryUserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    @Override
    public void deleteFriend(Long userId, Long friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь {} удалён из друзей пользователя {}", friendId, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        userStorage.findById(userId1);
        userStorage.findById(userId2);
        return userStorage.getCommonFriends(userId1, userId2);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавлен в друзья к пользователю {}", userId, friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        return userStorage.getFriendIds(userId);
    }

    @Override
    public User create(User user) {
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }
        return userStorage.create(user);
    }

    @Override
    public User update(User newUser) {
        if (newUser.getLogin() != null && newUser.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (newUser.getEmail() != null && !newUser.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ: @");
        }
        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }
        return userStorage.update(newUser);
    }

    @Override
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    @Override
    public User findById(Long id) {
        return userStorage.findById(id);
    }

    @Override
    public List<User> getFriends(Long id) {
        userStorage.findById(id);
        Set<Long> friendIds = getFriendIds(id);
        return friendIds.stream()
                .map(userStorage::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
