package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();
    public static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate RELEASE_DATA = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден!");
        }

        String newName = newFilm.getName();
        String newDescription = newFilm.getDescription();
        LocalDate newReleaseDate = newFilm.getReleaseDate();
        Integer newDuration = newFilm.getDuration();

        if (newName != null && !newName.isBlank()) {
            oldFilm.setName(newName);
        }

        if (newDescription != null) {
            if (newDescription.length() > MAX_LENGTH_DESCRIPTION) {
                throw new ValidationException("Максимальная длина описания — 200 символов!");
            }
            oldFilm.setDescription(newDescription);
        }

        if (newReleaseDate != null) {
            if (newReleaseDate.isBefore(RELEASE_DATA)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
            }
            oldFilm.setReleaseDate(newReleaseDate);
        }

        if (newDuration != null) {
            if (newDuration <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
            }
            oldFilm.setDuration(newDuration);
        }
        return oldFilm;
    }

    @Override
    public Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        return newId;
    }

    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    public Map<Long, Film> getFilms() {
        return films;
    }


    @Override
    public void addLike(Long filmId, Long userId) {
        findById(filmId);
        filmLikes.putIfAbsent(filmId, new HashSet<>());
        filmLikes.get(filmId).add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        findById(filmId);
        Set<Long> likes = filmLikes.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }
    }

    @Override
    public int getLikesCount(Long filmId) {
        Set<Long> likes = filmLikes.get(filmId);
        return likes == null ? 0 : likes.size();
    }

    public Map<Long, Set<Long>> getFilmLikes() {
        return filmLikes;
    }
}
