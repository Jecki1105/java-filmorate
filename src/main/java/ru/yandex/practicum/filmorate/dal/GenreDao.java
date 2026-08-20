package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDao {
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreRowMapper;

    public List<Genre> findAll() {
        String sql = "SELECT * FROM genre ORDER BY genre_id";
        return jdbc.query(sql, genreRowMapper);
    }

    public Optional<Genre> findById(long id) {
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, genreRowMapper, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
