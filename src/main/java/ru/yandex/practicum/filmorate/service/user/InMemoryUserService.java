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

@Service
@Slf4j
public class InMemoryUserService implements UserService {

    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    @Autowired
    public InMemoryUserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        friends.putIfAbsent(userId, new HashSet<>());
        friends.putIfAbsent(friendId, new HashSet<>());

        friends.get(userId).add(friendId);
        friends.get(friendId).add(userId);
        log.info("Пользователь {} добавлен в друзья пользователя {}", friendId, userId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {

        userStorage.findById(userId);
        userStorage.findById(friendId);

        Set<Long> userFriends = friends.get(userId);
        Set<Long> friendFriends = friends.get(friendId);

        if (userFriends != null) {
            userFriends.remove(friendId);
        }
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
        log.info("Пользователь {} удалён из друзей пользователя {}", friendId, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {

        userStorage.findById(userId1);
        userStorage.findById(userId2);

        Set<Long> user1Friends = friends.getOrDefault(userId1, Collections.emptySet());
        Set<Long> user2Friends = friends.getOrDefault(userId2, Collections.emptySet());

        return user1Friends.stream()
                .filter(user2Friends::contains)
                .map(userStorage::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        return friends.getOrDefault(userId, Collections.emptySet());
    }

    @Override
    public User create(User user) {

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ: @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения должна быть указана и не может быть в будущем!");
        }
        return userStorage.create(user);
    }
}
