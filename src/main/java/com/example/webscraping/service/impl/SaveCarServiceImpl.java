package com.example.webscraping.service.impl;

import com.example.webscraping.model.dbo.CarDbo;
import com.example.webscraping.model.dto.request.CarRequestDto;
import com.example.webscraping.model.enums.Source;
import com.example.webscraping.repository.CarRepository;
import com.example.webscraping.service.SaveCarService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SaveCarServiceImpl implements SaveCarService {
    private final CarRepository carRepository;
    //Reading data from property file to a list
    @Value("#{'${website.urls}'.split(',')}")
    List<String> urls;

    @Override
    @SneakyThrows
    @Scheduled(cron = "0 0 1 * * *")
    public void saveVehicles() {
        for (String url : urls) {
            getVehicleByModelFromAutobid(url, Map.of("X-POWERED-BY", "Spring Framework 6"));
        }
    }

    public void getVehicleByModelFromAutobid(String url, @RequestHeader Map<String, String> header) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch();
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setLocale("en-US")
                .setTimezoneId("Europe/Berlin"));
        Page page = context.newPage();
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);


        page.querySelectorAll(".auctionType_t.atc_4");

        //select all actions per day
        List<ElementHandle> elementHandles1 = page.querySelectorAll(".term_d_padd");

        //working function to search all non expired auctions
        List<ElementHandle> filteredAuctions = elementHandles1.stream()
                .filter(elementHandle -> elementHandle.innerHTML().contains("auctionType_t atc_4"))
                .toList();

        //working function to search all available auctions
        List<ElementHandle> auctionTypeTAtc4 = filteredAuctions.stream()
                .map(elementHandle -> elementHandle.querySelectorAll(".term_box_day.js_auction_row").stream()
                        .filter(elementHandle1 -> elementHandle1.getAttribute("href").contains("https://autobid.de"))
                        .toList())
                .filter(elementHandles2 -> !elementHandles2.isEmpty())
                .flatMap(Collection::stream)
                .toList();

        List<String> auctionIdList = auctionTypeTAtc4.stream()
                .map(element -> element.getAttribute("autobid:auctionid"))
                .toList();


        //working code to save all cars into db
        auctionIdList.forEach(attribute -> {
                    page.navigate("https://autobid.de/?action=car&show=next&auction=" + attribute);
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    String[] split = page.querySelector(".nav-side").querySelectorAll("div").get(0).innerText().split("\n");
                    LocalDate date = LocalDate.parse(split[1].split(" ")[1].replaceAll("\\.", "-").replaceAll("\n", ""), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    LocalTime time = LocalTime.parse(split[2].split(" ")[1]);
                    ZonedDateTime auctionDateTime = LocalDateTime.of(date, time).atZone(ZoneId.of("GMT+2")).withZoneSameInstant(ZoneId.of("GMT+0"));
                    String hrefToScrapePage = page.querySelector(".js_popup_window").getAttribute("href");
                    page.navigate("https://autobid.de/" + hrefToScrapePage);
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    saveEveryCarByAuction(page, hrefToScrapePage, auctionDateTime);
                }
        );


        //code to test scraping
//        page.navigate("https://autobid.de/?action=car&show=next&auction=65074");
//        page.waitForLoadState(LoadState.NETWORKIDLE);
//
//        System.out.println();
//        String hrefToScrapePage = page.querySelector(".js_popup_window").getAttribute("href");
//        page.navigate("https://autobid.de/" + hrefToScrapePage);
//        page.waitForLoadState(LoadState.NETWORKIDLE);
//        String currentPageUrl = "https://autobid.de/?action=car&show=details&id=2558857";

        System.out.println();

    }

    private void saveEveryCarByAuction(Page page, String hrefToScrapePage, ZonedDateTime auctionDateTime) {
        String hrefToNextPage = hrefToScrapePage;
        while (hrefToNextPage != null) {
            //scrape
            String currentPageUrl = "https://autobid.de/" + hrefToNextPage;
            page.navigate(currentPageUrl);
            String[] millageWithDimension = page.querySelector(".detal_general").querySelectorAll("tr").stream()
                    .filter(elementHandle -> elementHandle.querySelector("th").innerText().equals("Read mileage:"))
                    .toList().get(0).innerText().split("\t")[1].split(" ");
            int millage;
            String unitOfMillage;
            if (millageWithDimension.length != 2) {
                unitOfMillage = "";
                millage = 0;
            } else {
                Optional<Integer> optionalMillage = Optional.of(millageWithDimension[0].replace(".", ""))
                        .map(str -> {
                            try {
                                return Integer.parseInt(str);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        });
                unitOfMillage = millageWithDimension[1];
                millage = optionalMillage.orElse(0);
            }
            //
            saveCar(CarRequestDto.builder()
                    .unitOfMillage(unitOfMillage)
                    .title(getTitle(page))
                    .registrationDate(getRegistrationDate(page))
                    .displacement(getDisplacement(page))
                    .price(getPrice(page))
                    .photoUrls(getPhotoUrls(page))
                    .gearBox(getGearBox(page))
                    .bodyType(getBodyType(page))
                    .millage(millage)
                    .url(currentPageUrl)
                    .source(Source.AUTOBID)
                    .auctionDate(auctionDateTime)
                    .build());


            hrefToNextPage = page.querySelectorAll("a.arrow").get(1).getAttribute("href");
            System.out.println();
        }
    }

    private static String getGearBox(Page page) {
        List<ElementHandle> gearBoxElements = page.querySelector(".detal_general").querySelectorAll("tr").stream()
                .filter(elementHandle -> elementHandle.querySelector("th").innerText().equals("Transmission:"))
                .toList();
        String gearBox;
        if (gearBoxElements.isEmpty()) {
            gearBox = "";
        } else {
            gearBox = gearBoxElements.get(0).innerText().split("\t")[1].split(",")[0];
        }
        return gearBox;
    }

    private static String getBodyType(Page page) {
        List<ElementHandle> bodyTypeElement = page.querySelector(".detal_general").querySelectorAll("tr").stream()
                .filter(elementHandle -> elementHandle.querySelector("th").innerText().equals("Category:"))
                .toList();
        String bodyType;
        if (bodyTypeElement.isEmpty()) {
            bodyType = "";
        } else {
            bodyType = bodyTypeElement.get(0).innerText().split("\t")[1].split(",")[0];
        }
        return bodyType;
    }

    private static List<String> getPhotoUrls(Page page) {
        return page.querySelectorAll(".js_details_gallery_single").stream()
                .map(elementHandle -> elementHandle.querySelector("img").getAttribute("src"))
                .toList();
    }

    private static YearMonth getRegistrationDate(Page page) {
        List<ElementHandle> registrationDateElement = page.querySelector(".detal_general").querySelectorAll("tr").stream()
                .filter(elementHandle -> elementHandle.querySelector("th").innerText().equals("First registration:"))
                .toList();
        YearMonth dateOfRegistration;
        if (registrationDateElement.isEmpty()) {
            dateOfRegistration = null;
        } else {
            dateOfRegistration = YearMonth.parse(registrationDateElement.get(0).innerText().split("\t")[1], DateTimeFormatter.ofPattern("MM.yyyy"));
        }
        return dateOfRegistration;
    }

    private static Double getDisplacement(Page page) {
        double displacement;
        try {
            displacement = Double.parseDouble(page.querySelector(".d_s_det").innerText().split("\n")[1].split(" ")[0]);
        } catch (NumberFormatException e) {
            displacement = 0.0;
        }
        return displacement;
    }

    private static Integer getPrice(Page page) {
        return Optional.of(page.querySelector(".price_font").querySelector("td").innerHTML().split(" ")[0].replace(".", ""))
                .map(Integer::parseInt)
                .orElse(0);
    }

    private static String getTitle(Page page) {
        String bruteModel = page.querySelector(".d_s_det").querySelector("b").innerHTML();
        return Optional.of(bruteModel.substring(0, bruteModel.length() - 2)).orElse(bruteModel);
    }

    private void saveCar(CarRequestDto carDto) {
        carRepository.save(CarDbo.builder()
                .auctionDate(carDto.getAuctionDate())
                .bodyType(carDto.getBodyType())
                .registrationDate(carDto.getRegistrationDate())
                .displacement(carDto.getDisplacement())
                .gearBox(carDto.getGearBox())
                .price(carDto.getPrice())
                .millage(carDto.getMillage())
                .photoUrls(carDto.getPhotoUrls())
                .title(carDto.getTitle())
                .url(carDto.getUrl())
                .unitOfMillage(carDto.getUnitOfMillage())
                .source(carDto.getSource())
                .build());
    }

    private void deleteCars(){
    }


}



