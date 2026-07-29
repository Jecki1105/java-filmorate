package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

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
}
