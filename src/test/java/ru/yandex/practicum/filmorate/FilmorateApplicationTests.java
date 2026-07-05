package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;


import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;

@SpringBootTest
class FilmorateApplicationTests {

    private UserController userController;
    private FilmController filmController;
    public static final int FAKE_ID_FOR_TEST = 121;

    @BeforeEach
    void create() {
        userController = new UserController();
        filmController = new FilmController();
    }

    Film createValidFilm() {
        return Film.builder()
                .name("Имя1")
                .description("Описание")
                .releaseDate(LocalDate.of(2010, 5, 10))
                .duration(160)
                .build();
    }

    User createValidUser() {
        return User.builder()
                .email("email@.com")
                .login("Логин")
                .name("Имя")
                .birthday(LocalDate.of(2008, 4, 12))
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldCreateValidFilmSuccessfully() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);

        assertEquals(1, createdFilm.getId());
        assertEquals("Имя1", createdFilm.getName());
        assertEquals("Описание", createdFilm.getDescription());
        assertEquals(LocalDate.of(2010, 5, 10), createdFilm.getReleaseDate());
        assertEquals(160, createdFilm.getDuration());
    }

    @Test
    void shouldFailOnTooEarlyReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года!",
                exception.getMessage());
    }

    @Test
    void shouldFailOnEmptyFilmName() {
        Film film = createValidFilm();
        film.setName("");

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Название не может быть пустым!", exception.getMessage());
    }

    @Test
    void shouldFailOnLongDescription() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Максимальная длина описания — 200 символов!", exception.getMessage());
    }

    @Test
    void shouldFailOnMissingReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года!",
                exception.getMessage());
    }

    @Test
    void shouldFailOnZeroDuration() {
        Film film = createValidFilm();
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом!",
                exception.getMessage());
    }

    @Test
    void shouldFailOnNegativeDuration() {
        Film film = createValidFilm();
        film.setDuration(-50);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом!",
                exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundOnUpdateWithFakeFilmId() {
        Film oldFilm = createValidFilm();
        filmController.create(oldFilm);

        Film newFilm = Film.builder()
                .id(FAKE_ID_FOR_TEST)
                .name("Обновленное имя")
                .description("Описание")
                .releaseDate(LocalDate.of(2010, 5, 10))
                .duration(160)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmController.update(newFilm));
        assertEquals("Фильм с id " + FAKE_ID_FOR_TEST + " не найден!",
                exception.getMessage());

        Collection<Film> films = filmController.findAll();
        assertEquals(1, films.size());
        assertTrue(films.contains(oldFilm));
    }

    @Test
    void shouldCreateValidUserSuccessfully() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        assertEquals(1, createdUser.getId());
        assertEquals("email@.com", createdUser.getEmail());
        assertEquals("Логин", createdUser.getLogin());
        assertEquals("Имя", createdUser.getName());
        assertEquals(LocalDate.of(2008, 4, 12), createdUser.getBirthday());
    }

    @Test
    void shouldThrowNotFoundOnUpdateWithFakeUserId() {
        User oldUser = createValidUser();
        userController.create(oldUser);

        User newUser = User.builder()
                .id(FAKE_ID_FOR_TEST)
                .email("email@.com")
                .login("Логин")
                .name("Имя")
                .birthday(LocalDate.of(2002, 8, 28))
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userController.update(newUser));
        assertEquals("Пользователь с id " + FAKE_ID_FOR_TEST + " не найден",
                exception.getMessage());

        Collection<User> users = userController.findAll();
        assertEquals(1, users.size());
        assertTrue(users.contains(oldUser));
    }

    @Test
    void shouldGetAllUsersSuccessfully() {

        User user1 = userController.create(createValidUser());
        User user2 = userController.create(
                User.builder()
                        .email("secondEmail@.com")
                        .login("Логин2")
                        .name("Имя2")
                        .birthday(LocalDate.of(2000, 1, 1))
                        .build()
        );
        Collection<User> allUsers = userController.findAll();
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.contains(user1));
        assertTrue(allUsers.contains(user2));

    }

    @Test
    void shouldFailOnInvalidEmail() {
        User user = createValidUser();
        user.setEmail("email.com");

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ: @", exception.getMessage());
    }

    @Test
    void shouldFailOnNullEmail() {
        User user = createValidUser();
        user.setEmail(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ: @", exception.getMessage());
    }

    @Test
    void shouldFailOnFutureBirthday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Дата рождения должна быть указана и не может быть в будущем!", exception.getMessage());
    }

    @Test
    void shouldFailOnNullBirthday() {
        User user = createValidUser();
        user.setBirthday(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Дата рождения должна быть указана и не может быть в будущем!", exception.getMessage());
    }

}
