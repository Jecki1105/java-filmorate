package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение всех пользователей.");
        Collection<User> users = userService.findAll();
        log.debug("Получен список пользователей: {}", users);
        return users;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);
        User createdUser = userService.create(user);
        log.info("Пользователь создан: id={}, email={}", user.getId(), user.getEmail());
        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Запрос на изменение пользователя {}", newUser);
        User oldUser = userService.update(newUser);
        log.info("Пользователь: {} обновлен", oldUser);
        return oldUser;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя с ID: {}", id);
        User user = userService.findById(id);
        log.debug("Найден пользователь: {}", user);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на добавление пользователя {} в друзья пользователя {}", friendId, id);
        userService.addFriend(id, friendId);
        log.info("Пользователь {} добавлен в друзья пользователя {}", friendId, id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на удаление пользователя {} из друзей пользователя {}", friendId, id);
        userService.deleteFriend(id, friendId);
        log.info("Пользователь {} удалён из друзей пользователя {}", friendId, id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос на получение общих друзей пользователей {} и {}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.debug("Найдено {} общих друзей", commonFriends.size());
        return commonFriends;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Запрос на получение списка друзей пользователя с ID: {}", id);
        return userService.getFriends(id);
    }

}
