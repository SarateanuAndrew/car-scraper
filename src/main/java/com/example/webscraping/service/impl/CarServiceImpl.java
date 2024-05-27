package com.example.webscraping.service.impl;

import com.example.webscraping.model.dbo.CarDbo;
import com.example.webscraping.model.dto.request.FilteredCarsByTitleRequest;
import com.example.webscraping.model.dto.response.CarResponseDto;
import com.example.webscraping.repository.CarRepository;
import com.example.webscraping.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;

    @Override
    public CarResponseDto getCarById(Long id) {
        CarDbo carDbo = carRepository.findById(id).orElseThrow();
        return convertFromDboIntoDto(carDbo);
    }

    @Override
    public Page<CarResponseDto> getCarByTitle(FilteredCarsByTitleRequest filter) {
        List<CarResponseDto> carResponseDtos = carRepository.findCarByTitle("%" + filter.getSearch() + "%",
                        PageRequest.of(filter.getPage(), filter.getPerPage(),
                                Sort.by(filter.getSort(), filter.getSortBy())),
                        filter.getPriceMin(), filter.getPriceMax(),
                        filter.getMillageMin(), filter.getMillageMax(),
                        filter.getDisMin(), filter.getDisMax(),
                        filter.getSourceList())
                .map(this::convertFromDboIntoDto)
                .toList();
        return new PageImpl<>(carResponseDtos);
    }

    private CarResponseDto convertFromDboIntoDto(CarDbo carDbo) {
        return CarResponseDto.builder()
                .id(carDbo.getId())
                .title(carDbo.getTitle())
                .unitOfMillage(carDbo.getUnitOfMillage())
                .millage((carDbo.getUnitOfMillage().equals("Kilometres")) ? carDbo.getMillage() : (int) (carDbo.getMillage() * 1.60934))
                .url(carDbo.getUrl())
                .price(carDbo.getPrice())
                .registrationDate(carDbo.getRegistrationDate())
                .displacement(carDbo.getDisplacement())
                .auctionDate(carDbo.getAuctionDate())
                .photoUrls(carDbo.getPhotoUrls())
                .gearBox(carDbo.getGearBox())
                .bodyType(carDbo.getBodyType())
                .source(carDbo.getSource())
                .build();
    }
}
