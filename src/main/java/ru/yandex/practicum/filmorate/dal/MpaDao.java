package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDao {
    private final JdbcTemplate jdbc;
    private final RowMapper<RatingMpa> mpaRowMapper;

    public List<RatingMpa> findAll() {
        String sql = "SELECT * FROM mpa_rating ORDER BY mpa_rating_id";
        return jdbc.query(sql, mpaRowMapper);
    }

    public Optional<RatingMpa> findById(long id) {
        String sql = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, mpaRowMapper, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}