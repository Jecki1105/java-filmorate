package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой и должна содержать символ: @")
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ: @")
    private String email;

    @NotBlank(message = "Логин не может быть пустым и содержать пробелы!")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения должна быть указана и не может быть в будущем!")
    @PastOrPresent(message = "Дата рождения должна быть указана и не может быть в будущем!")
    private LocalDate birthday;
}
