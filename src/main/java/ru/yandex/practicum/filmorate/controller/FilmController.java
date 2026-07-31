package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов.");
        Collection<Film> films = filmService.findAll();
        log.debug("Получен список фильмов: {}", films);
        return films;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление нового фильма: {}", film);
        Film createdFilm = filmService.create(film);
        log.info("Фильм создан: id={}, name={}", film.getId(), film.getName());
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на изменение фильма: {}", newFilm);
        Film updatedFilm = filmService.update(newFilm);
        log.info("Фильм {} обновлен", updatedFilm);
        return updatedFilm;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с ID: {}", id);
        Film film = filmService.findById(id);
        log.debug("Найден фильм: {}", film);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на добавление лайка: фильм {}, пользователь {}", id, userId);
        filmService.addLike(id, userId);
        log.info("Лайк добавлен: фильм {}, пользователь {}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка: фильм {}, пользователь {}", id, userId);
        filmService.deleteLike(id, userId);
        log.info("Лайк удалён: фильм {}, пользователь {}", id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос на получение {} самых популярных фильмов", count);
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.debug("Возвращаем {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}
