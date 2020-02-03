package com.pbabecki.scraper.controller;

import com.pbabecki.scraper.model.Filter;
import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ScraperController {

    private String pageUrl = "https://www.hotelcareer.pl/index.php?sei_id=329&ang_id=2343180&count=1&button=top&k=6ffa408eabeb72b061013a3984d2e53ad83c8b5b";
    private Filter currentFilter = new Filter();
    private final ScraperService scraperService;

    @Autowired
    private WebDriver webDriver;

    @PostMapping("/filter")
    public void setFilterValues(@RequestBody Filter filter) {
        this.currentFilter = filter;
        if(filter.getPageUrl() != null) {
            this.pageUrl = filter.getPageUrl();
        }
        webDriver.get(pageUrl);
    }

    @SneakyThrows
    @GetMapping(value = "/scrape", produces = APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000")
    public List<PageElement> scrape() {
        List<PageElement> pageElements = scraperService.loadPageELements(pageUrl);
        List<PageElement> filteredPageElements = scraperService.filterElements(pageElements, currentFilter);
        return filteredPageElements;
    }

}
