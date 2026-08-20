package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryFilmStorage filmStorage;

    @Autowired
    private InMemoryUserStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        filmStorage.getFilms().clear();
        filmStorage.getFilmLikes().clear();
        userStorage.getUsers().clear();
        userStorage.getFriends().clear();
    }

    Film createValidFilm() {
        return Film.builder()
                .name("Имя1")
                .description("Описание")
                .releaseDate(LocalDate.of(2010, 5, 10))
                .duration(160)
                .mpaRating(new RatingMpa(1L, "G"))
                .build();
    }

    User createValidUser() {
        return User.builder()
                .email("user@example.com")
                .login("Логин")
                .name("Имя")
                .birthday(LocalDate.of(2008, 4, 12))
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldCreateValidFilmSuccessfully() throws Exception {
        Film film = createValidFilm();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Имя1"))
                .andExpect(jsonPath("$.description").value("Описание"))
                .andExpect(jsonPath("$.releaseDate").value("2010-05-10"))
                .andExpect(jsonPath("$.duration").value(160));
    }

    @Test
    void shouldFailOnTooEarlyReleaseDate() throws Exception {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void shouldFailOnEmptyFilmName() throws Exception {
        Film film = createValidFilm();
        film.setName("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Название не может быть пустым!"));
    }

    @Test
    void shouldFailOnLongDescription() throws Exception {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Максимальная длина описания — 200 символов!"));
    }

    @Test
    void shouldFailOnMissingReleaseDate() throws Exception {
        Film film = createValidFilm();
        film.setReleaseDate(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года!"));
    }

    @Test
    void shouldFailOnZeroDuration() throws Exception {
        Film film = createValidFilm();
        film.setDuration(0);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Продолжительность фильма должна быть положительным числом!"));
    }

    @Test
    void shouldFailOnNegativeDuration() throws Exception {
        Film film = createValidFilm();
        film.setDuration(-50);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Продолжительность фильма должна быть положительным числом!"));
    }

    @Test
    void shouldThrowNotFoundOnUpdateWithFakeFilmId() throws Exception {
        Film oldFilm = createValidFilm();

        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oldFilm)));

        Film newFilm = Film.builder()
                .id(121L)
                .name("Обновленное имя")
                .description("Описание")
                .releaseDate(LocalDate.of(2010, 5, 10))
                .duration(160)
                .mpaRating(new RatingMpa(1L, "G"))
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilm)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(
                        "Фильм с id 121 не найден!"));
    }

    @Test
    void shouldCreateValidUserSuccessfully() throws Exception {
        User user = createValidUser();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.login").value("Логин"))
                .andExpect(jsonPath("$.name").value("Имя"))
                .andExpect(jsonPath("$.birthday").value("2008-04-12"));
    }

    @Test
    void shouldThrowNotFoundOnUpdateWithFakeUserId() throws Exception {
        User oldUser = createValidUser();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oldUser)));

        User newUser = User.builder()
                .id(121L)
                .email("user@example.com")
                .login("Логин")
                .name("Имя")
                .birthday(LocalDate.of(2002, 8, 28))
                .build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(
                        "Пользователь с id 121 не найден"));
    }

    @Test
    void shouldGetAllUsersSuccessfully() throws Exception {
        User user1 = createValidUser();
        User user2 = User.builder()
                .email("secondEmail@com")
                .login("Логин2")
                .name("Имя2")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFailOnInvalidEmail() throws Exception {
        User user = createValidUser();
        user.setEmail("email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Электронная почта не может быть пустой и должна содержать символ: @"));
    }

    @Test
    void shouldFailOnNullEmail() throws Exception {
        User user = createValidUser();
        user.setEmail(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Электронная почта не может быть пустой и должна содержать символ: @"));
    }

    @Test
    void shouldFailOnFutureBirthday() throws Exception {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Дата рождения должна быть указана и не может быть в будущем!"));
    }

    @Test
    void shouldFailOnNullBirthday() throws Exception {
        User user = createValidUser();
        user.setBirthday(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Дата рождения должна быть указана и не может быть в будущем!"));
    }
}
