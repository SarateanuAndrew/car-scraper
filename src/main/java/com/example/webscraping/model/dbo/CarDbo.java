package com.example.webscraping.model.dbo;

import com.example.webscraping.model.enums.Source;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CarDbo {
    @Id
    @GeneratedValue
    private Long id;
    private String bodyType;
    private String title;
    private YearMonth registrationDate;
    private Integer millage;
    private Double displacement;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> photoUrls;
    private String gearBox;
    private Integer price;
    private ZonedDateTime auctionDate;
    private String url;
    private String unitOfMillage;
    @Enumerated(EnumType.STRING)
    private Source source;
}
