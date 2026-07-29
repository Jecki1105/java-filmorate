package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InMemoryFilmService implements FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    private static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate DATE_FIRST_MOVIE = LocalDate.of(1895, 12, 28);

    @Autowired
    public InMemoryFilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film create(Film film) {
        validationEmptyFields(film);
        validateFormat(film);
        return filmStorage.create(film);
    }

    private void validationEmptyFields(Film film) {
        if (film.getName() == null) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getDuration() == null) {
            throw new ValidationException("Продолжительность должна быть указана");
        }
    }

    private void validateFormat(Film film) {
        if (film.getName() != null && !StringUtils.hasText(film.getName())) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(DATE_FIRST_MOVIE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        filmLikes.putIfAbsent(filmId, new HashSet<>());

        Set<Long> likes = filmLikes.get(filmId);

        if (likes.contains(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return;
        }
        likes.add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        filmStorage.deleteLike(filmId, userId);
        log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {

        List<Film> allFilms = new ArrayList<>(filmStorage.findAll());

        allFilms.sort((film1, film2) -> {
            int likes1 = filmLikes.getOrDefault(film1.getId(), Collections.emptySet()).size();
            int likes2 = filmLikes.getOrDefault(film2.getId(), Collections.emptySet()).size();
            return Integer.compare(likes2, likes1);
        });

        return allFilms.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}
