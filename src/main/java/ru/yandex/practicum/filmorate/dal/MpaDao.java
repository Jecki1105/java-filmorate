package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MpaDao {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_rating ORDER BY mpa_rating_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<RatingMpa> mpaRowMapper;

    public List<RatingMpa> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mpaRowMapper);
    }

    public Optional<RatingMpa> findById(long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_QUERY, mpaRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Рейтинг MPA с id {} не найден", id);
            return Optional.empty();
        }
    }
}