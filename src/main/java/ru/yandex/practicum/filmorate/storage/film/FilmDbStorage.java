package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> filmRowMapper;
    private final RowMapper<Genre> genreRowMapper;
    private final RowMapper<RatingMpa> mpaRowMapper;

    private List<Genre> getGenresByFilmId(long filmId) {
        String sql = "SELECT g.genre_id, g.name FROM genre g " +
                "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.genre_id";
        return jdbc.query(sql, genreRowMapper, filmId);
    }

    private RatingMpa getMpaById(long mpaId) {
        String sql = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?";
        return jdbc.queryForObject(sql, mpaRowMapper, mpaId);
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbc.query(sql, filmRowMapper);
        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>(getGenresByFilmId(film.getId())));
            film.setMpaRating(getMpaById(film.getMpaRating().getId()));
        });
        return films;
    }

    @Override
    public Film create(Film film) {
        checkMpaExists(film.getMpaRating().getId());
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> checkGenreExists(genre.getId()));
        }
        String sql = "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpaRating().getId());
            return ps;
        });
        Long id = jdbc.queryForObject("SELECT MAX(film_id) FROM films", Long.class);
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Long genreId : uniqueGenreIds) {
                jdbc.update("INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)", id, genreId);
            }
        }
        return findById(id);
    }

    @Override
    public Film update(Film newFilm) {
        checkMpaExists(newFilm.getMpaRating().getId());
        if (newFilm.getGenres() != null) {
            newFilm.getGenres().forEach(genre -> checkGenreExists(genre.getId()));
        }
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE film_id = ?";
        int rows = jdbc.update(sql,
                newFilm.getName(),
                newFilm.getDescription(),
                java.sql.Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpaRating().getId(),
                newFilm.getId());
        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден!");
        }

        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", newFilm.getId());
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = newFilm.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Long genreId : uniqueGenreIds) {
                jdbc.update("INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)", newFilm.getId(), genreId);
            }
        }
        return findById(newFilm.getId());
    }

    @Override
    public Long getNextId() {
        String sql = "SELECT COUNT(*) FROM films";
        return jdbc.queryForObject(sql, Long.class) + 1;
    }

    @Override
    public Film findById(Long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film film = jdbc.queryForObject(sql, filmRowMapper, id);
            film.setGenres(new LinkedHashSet<>(getGenresByFilmId(id)));
            film.setMpaRating(getMpaById(film.getMpaRating().getId()));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        findById(filmId);
        String checkUser = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbc.queryForObject(checkUser, Integer.class, userId);
        if (count == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        String sql = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        findById(filmId);
        String checkUser = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbc.queryForObject(checkUser, Integer.class, userId);
        if (count == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
        return jdbc.queryForObject(sql, Integer.class, filmId);
    }

    private void checkMpaExists(long mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa_rating WHERE mpa_rating_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mpaId);
        if (count == 0) {
            throw new NotFoundException("Рейтинг MPA с id " + mpaId + " не найден");
        }
    }

    private void checkGenreExists(long genreId) {
        String sql = "SELECT COUNT(*) FROM genre WHERE genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, genreId);
        if (count == 0) {
            throw new NotFoundException("Жанр с id " + genreId + " не найден");
        }
    }
}