package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.stream.Collectors;
import java.util.Collections;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Map<Long, FriendshipStatus>> friends = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }

        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь: {} обновлен", oldUser);
        return oldUser;
    }

    @Override
    public Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый id - {}", newId);
        return newId;
    }

    @Override
    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    public Map<Long, User> getUsers() {
        return users;
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        if (friends.containsKey(userId)) {
            friends.get(userId).remove(friendId);
        }
        if (friends.containsKey(friendId)) {
            friends.get(friendId).remove(userId);
        }
    }

    @Override
    public void addFriendRequest(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        friends.putIfAbsent(userId, new HashMap<>());
        friends.putIfAbsent(friendId, new HashMap<>());
        friends.get(userId).put(friendId, FriendshipStatus.PENDING);
        friends.get(friendId).put(userId, FriendshipStatus.PENDING);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        if (friends.containsKey(userId) && friends.get(userId).containsKey(friendId)) {
            friends.get(userId).put(friendId, FriendshipStatus.CONFIRMED);
        }
        if (friends.containsKey(friendId) && friends.get(friendId).containsKey(userId)) {
            friends.get(friendId).put(userId, FriendshipStatus.CONFIRMED);
        }
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        Map<Long, FriendshipStatus> userFriends = friends.get(userId);
        if (userFriends == null) return Collections.emptySet();
        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getPendingFriendIds(Long userId) {
        Map<Long, FriendshipStatus> userFriends = friends.get(userId);
        if (userFriends == null) return Collections.emptySet();
        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.PENDING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public FriendshipStatus getFriendshipStatus(Long userId, Long friendId) {
        Map<Long, FriendshipStatus> userFriends = friends.get(userId);
        if (userFriends == null) return null;
        return userFriends.get(friendId);
    }

    public Map<Long, Map<Long, FriendshipStatus>> getFriends() {
        return friends;
    }
}
