package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class InMemoryFilmService implements FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final LocalDate DATE_FIRST_MOVIE = LocalDate.of(1895, 12, 28);
    public static final int MAX_LENGTH_DESCRIPTION = 200;

    @Autowired
    public InMemoryFilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film create(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(DATE_FIRST_MOVIE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        return filmStorage.create(film);
    }


    @Override
    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        filmStorage.deleteLike(filmId, userId);
        log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getDescription() != null && newFilm.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания — 200 символов!");
        }
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(DATE_FIRST_MOVIE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        if (newFilm.getDuration() != null && newFilm.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
        }
        return filmStorage.update(newFilm);
    }

    @Override
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @Override
    public Film findById(Long id) {
        return filmStorage.findById(id);
    }
}
