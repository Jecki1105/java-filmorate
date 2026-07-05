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
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    public static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate RELEASE_DATA = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов.");
        log.debug("Получен список фильмов: {}", films.values());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление нового фильма: {}", film);
        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на изменение фильма: {}", newFilm);
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден!");
        }

        String newName = newFilm.getName();
        String newDescription = newFilm.getDescription();
        LocalDate newReleaseDate = newFilm.getReleaseDate();
        Integer newDuration = newFilm.getDuration();

        if (newName != null && !newName.isBlank()) {
            log.debug("Обновление названия фильма: {} c {} на {}", oldFilm.getId(), oldFilm.getName(), newName);
            oldFilm.setName(newName);
        }

        if (newDescription != null) {
            if (newDescription.length() > MAX_LENGTH_DESCRIPTION) {
                throw new ValidationException("Максимальная длина описания — 200 символов!");
            }
            log.debug("Обновление описания фильма {} c {} на {}", oldFilm.getId(),
                    oldFilm.getDescription(), newDescription);
            oldFilm.setDescription(newDescription);
        }

        if (newReleaseDate != null) {
            if (newReleaseDate.isBefore(RELEASE_DATA)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
            }
            log.debug("Обновление даты релиза фильма {} c {} на {}", oldFilm.getId(),
                    oldFilm.getReleaseDate(), newReleaseDate);
            oldFilm.setReleaseDate(newReleaseDate);
        }

        if (newDuration != null) {
            if (newDuration <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
            }
            log.debug("Обновление продолжительности фильма: {} c {} на {}",
                    oldFilm.getId(), oldFilm.getDuration(), newDuration);
            oldFilm.setDuration(newDuration);
        }
        log.info("Фильм {} обновлен!", oldFilm);
        return oldFilm;
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый id - {}", newId);
        return newId;
    }

    private void validate(Film film) {
        if (!StringUtils.hasText(film.getName())) {
            throw new ValidationException("Название не может быть пустым!");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(RELEASE_DATA)) {
            throw new ValidationException("Дата релиза должна быть указана и не может" +
                    " быть раньше 28 декабря 1895 года!");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания — 200 символов!");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
        }
    }
}
