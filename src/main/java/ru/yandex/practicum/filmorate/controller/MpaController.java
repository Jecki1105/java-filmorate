package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<RatingMpa> findAll() {
        return mpaService.findAll();
    }

    @GetMapping("/{id}")
    public RatingMpa findById(@PathVariable long id) {
        return mpaService.findById(id);
    }
}
