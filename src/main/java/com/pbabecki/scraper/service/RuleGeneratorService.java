package com.pbabecki.scraper.service;

import com.pbabecki.scraper.model.RuleModel;
import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.model.page.PageElementAttributes;
import com.pbabecki.scraper.model.page.PageElementPosition;
import com.pbabecki.scraper.model.rule.RuleElement;
import com.pbabecki.scraper.model.rule.RuleElementAttribute;
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

    public Optional<RuleElement> generateApplyRule(List<PageElement> pageElements, PageElement applyPageElement) {
        pageElements.remove(applyPageElement);
        List<PageElement> elementsWithTheSameTagName = pageElements.stream()
                .filter(elem -> elem.getTagName().equals(applyPageElement.getTagName()))
                .collect(Collectors.toList());

        Map<String, String> applyElementAttributeMap = applyPageElement.getAttributes().stream()
                .collect(Collectors.toMap(PageElementAttributes::getName, PageElementAttributes::getValue));
        Map<String, Set<String>> pageElementAttributes = getAttributesMapOfPageElements(elementsWithTheSameTagName);

        Map<String, String> applyRuleAttributesMap = findDifference(pageElementAttributes, applyElementAttributeMap);
        if(applyRuleAttributesMap.isEmpty() && !elementsWithTheSameTagName.isEmpty()) {
            log.error("Cant find unique attributes for apply element : " + applyPageElement);
            return Optional.empty();
        } else {
            RuleElement applyRuleElement = prepareApplyRuleElement(applyPageElement, applyRuleAttributesMap);
            return Optional.of(applyRuleElement);
        }
    }

    private RuleElement prepareApplyRuleElement(PageElement applyPageElement, Map<String, String> applyUniqueAttributes) {
        List<RuleElementAttribute> ruleElementAttributes = new ArrayList<>();
        applyUniqueAttributes.keySet().forEach(key ->
                ruleElementAttributes.add(RuleElementAttribute.builder()
                        .name(key)
                        .values(Collections.singletonList(applyUniqueAttributes.get(key)))
                        .build()));
        return RuleElement.builder()
                .name("Apply Rule : " + applyPageElement.getTagName())
                .tagName(applyPageElement.getTagName())
                .attributes(ruleElementAttributes)
                .isApplyButton(true)
                .labelXOffset("5")
                .labelYOffset("5")
                .validationXOffset("5")
                .validationYOffset("5")
                .build();
    }

    private Map<String, String> findDifference(Map<String, Set<String>> elementsAttributes, Map<String, String> applyAttributes) {
        Map<String, String> difference = new HashMap<>();
        applyAttributes.keySet().forEach(applyKey -> {
            String  applyValue = applyAttributes.get(applyKey);
            if(!elementsAttributes.containsKey(applyKey)) {
                difference.put(applyKey, applyValue);
            } else {
                Set<String> elementAttributeValues = elementsAttributes.get(applyKey);
                Set<String> sameAttributesValues = elementAttributeValues.stream()
                        .filter(elemValue -> !elemValue.equals(applyValue))
                        .collect(Collectors.toSet());
                if(elementAttributeValues.size() == sameAttributesValues.size()) {
                    difference.put(applyKey, applyValue);
                }
            }
        });
        return difference;
    }

    private Map<String, Set<String>> getAttributesMapOfPageElements(List<PageElement> pageElements) {
        Map<String, Set<String>> attributes = new HashMap<>();
        pageElements.stream().map(PageElement::getAttributes).forEach(elemAttributes -> elemAttributes.forEach(attribute -> {
           if(attributes.containsKey(attribute.getName())) {
               attributes.get(attribute.getName()).add(attribute.getValue());
           } else {
               Set<String> valueSet = new HashSet<>();
               valueSet.add(attribute.getValue());
               attributes.put(attribute.getName(), valueSet);
           }
        }));
        return attributes;
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
