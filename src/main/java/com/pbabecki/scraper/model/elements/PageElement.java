package com.pbabecki.scraper.model.elements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageElement {

    private String xPath;
    private String parentXPath;
    private String tagName;
    private String text;
    private String innerText;
    private PageElementPosition position;
    private List<PageElementAttributes> attributes;

}
