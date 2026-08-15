package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    Long getNextId();

    User findById(Long id);


    void addFriendRequest(Long userId, Long friendId);

    void confirmFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Set<Long> getFriendIds(Long userId);

    Set<Long> getPendingFriendIds(Long userId);

    FriendshipStatus getFriendshipStatus(Long userId, Long friendId);
}
