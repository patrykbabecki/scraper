package com.pbabecki.scraper.service;

import com.pbabecki.scraper.model.RuleModel;
import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.model.page.PageElementPosition;
import com.pbabecki.scraper.model.rule.RuleElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleGeneratorService {

    private static final int PIXEL_TOLLERANCE = 5;

    public List<RuleElement> generateLabelRules(Map<PageElement, Map<DirectionEnum, Set<PageElement>>> pageElementMapMap) {
        Set<String> pageElementTagNames = pageElementMapMap.keySet().stream().map(PageElement::getTagName).collect(Collectors.toSet());
        List<RuleElement> ruleElements = new ArrayList<>();
        pageElementTagNames.forEach(tagName -> {
            Set<PageElement> pageElementsWithTheSameTagName = pageElementMapMap.keySet().stream().filter(elem -> tagName.equals(elem.getTagName())).collect(Collectors.toSet());
            Map<PageElement, Map<DirectionEnum, Set<PageElement>>> tagNamePageElementMap = pageElementsWithTheSameTagName.stream()
                    .collect(Collectors.toMap(Function.identity(), pageElementMapMap::get));
            List<RuleElement> generatedRuleElements = prepareRuleForTheSameTagNameElements(tagNamePageElementMap, tagName);
            ruleElements.addAll(generatedRuleElements);
        });
        for(int i = 0; i < ruleElements.size() ; i++) {
            RuleElement ruleElement = ruleElements.get(i);
            ruleElement.setName(i + " Rule : " + ruleElement.getTagName());
        }
        return ruleElements;
    }

    private List<RuleElement> prepareRuleForTheSameTagNameElements(Map<PageElement, Map<DirectionEnum, Set<PageElement>>> pageElementMapMap, String tagName) {
        Map<DirectionEnum, Integer> directionEnumCount = countDirectionOfPageElementMap(pageElementMapMap);
        DirectionEnum calculatedDirection = getDirectionWithHighestCount(directionEnumCount);
        Set<Double> elementOffset = pageElementMapMap.keySet().stream()
                .map(elem -> calculateOffset(calculatedDirection, elem, pageElementMapMap.get(elem).get(calculatedDirection)))
                .collect(Collectors.toSet());
        Double offsetValue = elementOffset.stream().filter(x -> x != 0.0).findAny().orElse(0.0);
        RuleElement ruleElement = prepareRuleElement(calculatedDirection, offsetValue, tagName);
        return Arrays.asList(ruleElement);
    }

    private RuleElement prepareRuleElement(DirectionEnum direction, double offsetValue, String tagName) {
        RuleElement ruleElement = new RuleElement();
        ruleElement.setTagName(tagName);
        switch (direction) {
            case UP:
            case DOWN:
                ruleElement.setLabelXOffset("5");
                ruleElement.setLabelYOffset(Integer.toString((int)offsetValue));
                break;
            case LEFT:
            case RIGHT:
                ruleElement.setLabelYOffset("5");
                ruleElement.setLabelXOffset(Integer.toString((int)offsetValue));
                break;
                default:
                    throw new IllegalStateException("Direction not supported : " + direction);
        }
        return ruleElement;
    }

    private Double calculateOffset(DirectionEnum direction, PageElement pageElement, Set<PageElement> nextElements) {
        Double offset = 0.0;
        switch (direction) {
            case UP:
            case DOWN:
                offset = calculateOffset(pageElement, nextElements, PageElementPosition::getMinY);
                break;
            case RIGHT:
            case LEFT:
                offset = calculateOffset(pageElement, nextElements, PageElementPosition::getMinX);
                break;
            default:
                    throw new IllegalStateException("Direction not supported : " + direction);
        }
        offset = Math.abs(offset)*direction.getDirectionSign();
        return offset;
    }

    private Double calculateOffset(PageElement pageElement, Set<PageElement> pageElements, Function<PageElementPosition, Double> positionFunction) {
        Double pageElementPosition = Math.abs(positionFunction.apply(pageElement.getPosition()));
        Set<Double> offsetValues = pageElements.stream().map(pageElem -> {
            Double correlatedElementPosition = Math.abs(positionFunction.apply(pageElem.getPosition()));
            return Math.abs(correlatedElementPosition - pageElementPosition);
        }).collect(Collectors.toSet());
        return offsetValues.stream().filter(x -> x!=0.0).findAny().orElse(0.0);
    }

    private DirectionEnum getDirectionWithHighestCount(Map<DirectionEnum, Integer> directionEnumIntegerMap) {
        Integer count = 0;
        DirectionEnum directionEnum = null;
        for(DirectionEnum direction : directionEnumIntegerMap.keySet()) {
            Integer directionCount = directionEnumIntegerMap.get(direction);
            if(directionCount >= count) {
                count = directionCount;
                directionEnum = direction;
            }
        }
        return directionEnum;
    }

    private Map<DirectionEnum, Integer> countDirectionOfPageElementMap(Map<PageElement, Map<DirectionEnum, Set<PageElement>>> pageElementMapMap ) {
        Map<DirectionEnum, Integer> directionCountMap = new HashMap<>();
        pageElementMapMap.keySet().forEach(elem -> {
            Map<DirectionEnum, Set<PageElement>> directionEnumSetMap = pageElementMapMap.get(elem);
            directionEnumSetMap.keySet().forEach(direction -> {
                if(directionCountMap.containsKey(direction)) {
                    if(!directionEnumSetMap.get(direction).isEmpty()) {
                        Integer count = directionCountMap.get(direction);
                        count++;
                        directionCountMap.put(direction, count);
                    }
                } else {
                    if(!directionEnumSetMap.get(direction).isEmpty()) {
                        Integer count = 1;
                        directionCountMap.put(direction, count);
                    }
                }
            });
        });
        return directionCountMap;
    }

}
