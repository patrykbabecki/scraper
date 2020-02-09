package com.pbabecki.scraper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pbabecki.scraper.model.rule.RuleElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleModel {

    private List<RuleElement> rules;

}
