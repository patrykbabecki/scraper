package com.pbabecki.scraper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbabecki.scraper.model.Filter;
import com.pbabecki.scraper.model.page.PageElement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private static final String JS_SCRAPER_PATH = "src/main/resources/js/Scrapper.js";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebDriver webDriver;

    @SneakyThrows
    public List<PageElement> loadPageELements(String pageUrl) {
        FileInputStream fis = new FileInputStream(JS_SCRAPER_PATH);
        String data = IOUtils.toString(fis, "UTF-8");
        List<Map<String, Object>> pageElementsRaw = (List<Map<String, Object>>) ((JavascriptExecutor) webDriver).executeScript(data);
        try {
            List<PageElement> pageElements = new ArrayList<>();
            for (Map<String, Object> element : pageElementsRaw) {
                PageElement pageElement = objectMapper.convertValue(element, PageElement.class);
                pageElement.setXPath(element.get("xPath").toString());
                pageElements.add(pageElement);
            }
            return pageElements;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<PageElement> filterElements(List<PageElement> pageElements, Filter filter) {
        if(filter.getPixelsAround() == 0 && StringUtils.isEmpty(filter.getTagName())) {
            return pageElements;
        }
        List<PageElement> filteredElements = new ArrayList<>();
        List<PageElement> pageElementsByTagName = pageElements.stream()
                .filter(elem -> elem.getTagName().equals(filter.getTagName()))
                .collect(Collectors.toList());
        for(PageElement pageElement: pageElementsByTagName) {
            filteredElements.add(pageElement);
            List<PageElement> elementsAround = findElementsAround(pageElements, pageElement,filter.getPixelsAround());
            filteredElements.addAll(elementsAround);
        }
        return filteredElements;
    }

    private List<PageElement> findElementsAround(List<PageElement> pageElements, PageElement pageElement, double pixelsAround) {
        double x1 = pageElement.getPosition().getX() - pixelsAround;
        double y1 = pageElement.getPosition().getY() - pixelsAround;
        double x2 = pageElement.getPosition().getX() + pageElement.getPosition().getWidth() + pixelsAround;
        double y2 = pageElement.getPosition().getY() + pageElement.getPosition().getHeight() + pixelsAround;

        List<PageElement> filteredElements = pageElements.stream()
                .filter(elem ->
                        elem.getPosition().getX() >= x1 && elem.getPosition().getX() <= x2 &&
                                elem.getPosition().getY() >= y1 && elem.getPosition().getY() <= y2)
                .collect(Collectors.toList());
        filteredElements.remove(pageElement);
        return filteredElements;
    }

}
