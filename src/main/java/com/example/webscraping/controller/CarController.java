package com.example.webscraping.controller;

import com.example.webscraping.model.dto.request.FilteredCarsByTitleRequest;
import com.example.webscraping.model.dto.response.CarResponseDto;
import com.example.webscraping.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/vehicle")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CarController {
    private final CarService carService;

    @GetMapping("/{id}")
    public CarResponseDto getCarById(@PathVariable("id") Long id) {
        return carService.getCarById(id);
    }

    @PostMapping
    public Page<CarResponseDto> getCarByName(@RequestBody FilteredCarsByTitleRequest filteredCarsByTitleRequest) {
        return carService.getCarByTitle(filteredCarsByTitleRequest);
    }
}
