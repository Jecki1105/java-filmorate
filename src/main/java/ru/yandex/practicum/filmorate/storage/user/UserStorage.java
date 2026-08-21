package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    User findById(Long id);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Set<Long> getFriendIds(Long userId);

    List<User> getCommonFriends(Long userId1, Long userId2);

}
