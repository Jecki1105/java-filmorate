package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank(message = "Название не может быть пустым!")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов!")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года!")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана!")
    @Positive(message = "Продолжительность фильма должна быть положительным числом!")
    private Integer duration;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    @JsonProperty("mpa")
    private RatingMpa mpaRating;
}