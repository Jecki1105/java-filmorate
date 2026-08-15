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


    void addFriendRequest(Long userId, Long friendId);   // создать запрос (PENDING)
    void confirmFriend(Long userId, Long friendId);      // подтвердить (CONFIRMED)
    void deleteFriend(Long userId, Long friendId);
    Set<Long> getFriendIds(Long userId);                // получить подтверждённых друзей
    Set<Long> getPendingFriendIds(Long userId);          // получить входящие заявки
    FriendshipStatus getFriendshipStatus(Long userId, Long friendId);
}
