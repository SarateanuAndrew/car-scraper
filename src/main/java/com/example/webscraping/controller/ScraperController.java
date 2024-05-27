package com.example.webscraping.controller;

import com.example.webscraping.model.dto.response.CarResponseDto;
import com.example.webscraping.service.CarService;
import com.example.webscraping.service.SaveCarService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin("*")
public class ScraperController {
    private final SaveCarService saveCarService;

    @GetMapping(path = "/all")
    public void saveVehiclesFromAutoBid() {
        saveCarService.saveVehicles();
    }
}
