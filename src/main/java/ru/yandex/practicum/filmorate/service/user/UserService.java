package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserService {
    User create(User user);

    User update(User newUser);

    Collection<User> findAll();

    User findById(Long id);

    List<User> getFriends(Long id);

    void addFriendRequest(Long userId, Long friendId);

    void confirmFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> getCommonFriends(Long userId1, Long userId2);

    Set<Long> getFriendIds(Long userId);

    Set<Long> getPendingFriendIds(Long userId);
}
