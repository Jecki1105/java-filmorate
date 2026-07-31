package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
    public void addFriend(Long userId, Long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавлен в друзья пользователя {}", friendId, userId);
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
        Set<Long> user1Friends = userStorage.getFriendIds(userId1);
        Set<Long> user2Friends = userStorage.getFriendIds(userId2);
        return user1Friends.stream()
                .filter(user2Friends::contains)
                .map(userStorage::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
