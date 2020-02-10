package com.pbabecki.scraper.model.rule;

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
    private String labelXOffset;
    private String labelYOffset;
    private String validationXOffset = "0";
    private String validationYOffset = "0";
    private boolean isNavigation;
    private boolean isApplyButton;
    private List<RuleElementAttribute> attributes = new ArrayList<>();
    private List<RuleElementAttribute> validationAttributes = new ArrayList<>();

}
