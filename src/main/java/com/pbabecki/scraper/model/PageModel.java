package com.pbabecki.scraper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.model.page.PageOptionEnum;
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
public class PageModel {

    private String atsName;
    private PageOptionEnum option;
    private List<PageElement> elements;

}
