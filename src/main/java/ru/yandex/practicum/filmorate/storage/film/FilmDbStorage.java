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

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String SELECT_FILMS =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                    "m.mpa_rating_id, m.name AS mpa_name " +
                    "FROM films f LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_rating_id";

    private static final String SELECT_FILM_BY_ID = SELECT_FILMS + " WHERE f.film_id = ?";
    private static final String SELECT_GENRES_BY_FILM_IDS =
            "SELECT fg.film_id, g.genre_id, g.name FROM film_genre fg " +
                    "JOIN genre g ON fg.genre_id = g.genre_id WHERE fg.film_id IN (%s)";

    private static final String INSERT_FILM =
            "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                    "WHERE film_id = ?";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String INSERT_FILM_GENRE =
            "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";

    private static final String INSERT_LIKE =
            "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String COUNT_LIKES =
            "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
    private static final String SELECT_POPULAR =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                    "m.mpa_rating_id, m.name AS mpa_name, " +
                    "(SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.film_id) AS like_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_rating_id " +
                    "ORDER BY like_count DESC " +
                    "LIMIT ?";

    private static final String CHECK_MPA_EXISTS =
            "SELECT COUNT(*) FROM mpa_rating WHERE mpa_rating_id = ?";
    private static final String CHECK_GENRES_EXIST =
            "SELECT COUNT(*) FROM genre WHERE genre_id IN (%s)";

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> filmRowMapper;
    private final RowMapper<Genre> genreRowMapper;

    private Map<Long, Set<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = filmIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = String.format(SELECT_GENRES_BY_FILM_IDS, inClause);
        Map<Long, Set<Genre>> map = new HashMap<>();
        jdbc.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("name"));
            map.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());
        return map;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(SELECT_FILMS, filmRowMapper);
        if (films.isEmpty()) {
            return films;
        }
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Long, Set<Genre>> genresByFilm = getGenresByFilmIds(filmIds);
        films.forEach(film -> film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>())));
        return films;
    }

    @Override
    public Film create(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }
        checkMpaExists(film.getMpaRating().getId());
        List<Long> genreIds = film.getGenres().stream().map(Genre::getId).distinct().collect(Collectors.toList());
        checkGenresExist(genreIds);

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpaRating().getId());
            return ps;
        });
        Long id = jdbc.queryForObject("SELECT MAX(film_id) FROM films", Long.class);
        film.setId(id);

        for (Long genreId : genreIds) {
            jdbc.update(INSERT_FILM_GENRE, id, genreId);
        }
        return findById(id);
    }

    private void checkMpaExists(long mpaId) {
        Integer count = jdbc.queryForObject(CHECK_MPA_EXISTS, Integer.class, mpaId);
        if (count == 0) {
            throw new NotFoundException("Рейтинг MPA с id " + mpaId + " не найден");
        }
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(new LinkedHashSet<>());
        }
        checkMpaExists(newFilm.getMpaRating().getId());
        List<Long> genreIds = newFilm.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());
        checkGenresExist(genreIds);

        int rows = jdbc.update(UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                java.sql.Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpaRating().getId(),
                newFilm.getId());
        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден!");
        }

        jdbc.update(DELETE_FILM_GENRES, newFilm.getId());
        for (Long genreId : genreIds) {
            jdbc.update(INSERT_FILM_GENRE, newFilm.getId(), genreId);
        }
        return findById(newFilm.getId());
    }

    @Override
    public Film findById(Long id) {
        try {
            Film film = jdbc.queryForObject(SELECT_FILM_BY_ID, filmRowMapper, id);
            Map<Long, Set<Genre>> genres = getGenresByFilmIds(List.of(id));
            film.setGenres(genres.getOrDefault(id, new LinkedHashSet<>()));
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
        jdbc.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        findById(filmId);
        jdbc.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        return jdbc.queryForObject(COUNT_LIKES, Integer.class, filmId);
    }

    private void checkGenresExist(List<Long> genreIds) {
        if (genreIds.isEmpty()) return;
        String inClause = genreIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = String.format(CHECK_GENRES_EXIST, inClause);
        Integer count = jdbc.queryForObject(sql, Integer.class, genreIds.toArray());
        if (count != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> popularFilms = jdbc.query(SELECT_POPULAR, filmRowMapper, count);
        if (popularFilms.isEmpty()) {
            return popularFilms;
        }
        List<Long> filmIds = popularFilms.stream().map(Film::getId).collect(Collectors.toList());
        Map<Long, Set<Genre>> genresByFilm = getGenresByFilmIds(filmIds);
        popularFilms.forEach(film -> film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>())));
        return popularFilms;
    }
}