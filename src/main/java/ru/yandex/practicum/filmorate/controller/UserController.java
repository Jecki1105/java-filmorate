package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение всех пользователей.");
        log.debug("Получен список пользователей: {}", users.values());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);
        validate(user);
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя заменено на логин, так как не указано");
        }
        users.put(user.getId(), user);
        log.info("Пользователь создан: id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Запрос на изменение пользователя {}", newUser);
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }

        String newEmail = newUser.getEmail();
        String newLogin = newUser.getLogin();
        String newName = newUser.getName();
        LocalDate newBirthday = newUser.getBirthday();

        if (newLogin != null && !newLogin.isBlank()) {
            if (newLogin.contains(" ")) {
                throw new ValidationException("Логин не может содержать пробелы");
            }
            log.debug("Обновление логина пользователя {} c {} на {}", oldUser.getId(), oldUser.getLogin(), newLogin);
            oldUser.setLogin(newLogin);
        }
        if (newEmail != null && !newEmail.isBlank()) {
            if (!newEmail.contains("@")) {
                throw new ValidationException("Электронная почта должна содержать символ: @");
            }
            log.debug("Обновление email пользователя: {} c {} на {}", oldUser.getId(), oldUser.getEmail(), newEmail);
            oldUser.setEmail(newEmail);
        }
        if (newName != null) {
            if (newName.isBlank()) {
                log.debug("Обновление имени пользователя: {} c {} на {}",
                        oldUser.getId(), oldUser.getName(), oldUser.getLogin());
                oldUser.setName(oldUser.getLogin());
            } else {
                log.debug("Обновление имени пользователя: {} c {} на {}",
                        oldUser.getId(), oldUser.getName(), newName);
                oldUser.setName(newName);
            }
        }
        if (newBirthday != null) {
            if (newBirthday.isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем!");
            }
            log.debug("Обновление даты рождения пользователя {} c {} на {}",
                    oldUser.getId(), oldUser.getBirthday(), newBirthday);
            oldUser.setBirthday(newBirthday);
        }

        log.info("Пользователь: {} обновлен", oldUser);
        return oldUser;
    }

    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый id - {}", newId);
        return newId;
    }

    private void validate(User user) {
        if (!StringUtils.hasText(user.getEmail()) || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ: @");
        }
        if (!StringUtils.hasText(user.getLogin()) || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения должна быть указана и не может быть в будущем!");
        }
    }
}
