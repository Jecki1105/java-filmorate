package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDao {

    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreRowMapper;

    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY, genreRowMapper);
    }

    public Optional<Genre> findById(long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_QUERY, genreRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Жанр с id {} не найден", id);
            return Optional.empty();
        }
    }
}
