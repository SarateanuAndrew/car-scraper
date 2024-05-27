package com.example.webscraping.service;

import com.example.webscraping.model.dto.request.FilteredCarsByTitleRequest;
import com.example.webscraping.model.dto.response.CarResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CarService {
    CarResponseDto getCarById(Long id);
    Page<CarResponseDto> getCarByTitle(FilteredCarsByTitleRequest filter);
}
