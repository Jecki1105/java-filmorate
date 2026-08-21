-- Заполняем рейтинги MPA
MERGE INTO mpa_rating (mpa_rating_id, name)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');

-- Заполняем жанры -- MERGE чтоб не писать условие IF NOT EXISTS
MERGE INTO genre (genre_id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');