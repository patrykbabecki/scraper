package com.pbabecki.scraper.model.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageElement {

    @JsonProperty("xPath")
    private String xPath;
    private String parentXPath;
    private String tagName;
    private String text;
    private String innerText;
    private PageElementPosition position;
    private List<PageElementAttributes> attributes;

}
