package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> getCommonFriends(Long userId1, Long userId2);

    Set<Long> getFriendIds(Long userId);

    User create(User user);
}
