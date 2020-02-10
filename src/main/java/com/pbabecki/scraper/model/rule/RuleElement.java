package com.pbabecki.scraper.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleElement {

    private String name;
    private String tagName;
    private String labelXOffset = "0";
    private String labelYOffset = "0";
    private String validationXOffset = "0";
    private String validationYOffset = "0";
    @JsonProperty("isNavigation")
    private boolean isNavigation;
    @JsonProperty("isApplyButton")
    private boolean isApplyButton;
    private List<RuleElementAttribute> attributes = new ArrayList<>();
    private List<RuleElementAttribute> validationAttributes = new ArrayList<>();

}
