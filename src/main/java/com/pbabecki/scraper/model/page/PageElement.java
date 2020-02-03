package com.pbabecki.scraper.model.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageElement {

    private String xPath;
    private String parentXPath;
    private String tagName;
    private PageElementAttributes attributes;
    private String text;
    private String innerText;
    private PageElementPosition position;

}
