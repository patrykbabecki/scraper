package com.pbabecki.scraper.model.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private boolean isNavigation;
    private boolean isApplyButton;
    private List<RuleElementAttribute> attributes;
    private List<RuleElementAttribute> validationAttributes;

}
