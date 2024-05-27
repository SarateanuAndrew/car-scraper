package com.example.webscraping.model.dto.request;

import com.example.webscraping.model.enums.Source;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.example.webscraping.model.enums.Source.AUTOBID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public class FilteredCarsByTitleRequest {
    @Builder.Default
    private String search = "";
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer perPage = 10;
    @Builder.Default
    private Integer priceMin = 0;
    @Builder.Default
    private Integer priceMax = Integer.MAX_VALUE;
    @Builder.Default
    private Integer millageMin = 0;
    @Builder.Default
    private Integer millageMax = Integer.MAX_VALUE;
    @Builder.Default
    private Double disMin = 0.0;
    @Builder.Default
    private Double disMax = Double.MAX_VALUE;
    @Builder.Default
    private List<Source> sourceList = List.of(AUTOBID);
    @Builder.Default
    private String sortBy = "auctionDate";
    @Builder.Default
    private Sort.Direction sort = Sort.Direction.ASC;
}
