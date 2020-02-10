package com.pbabecki.scraper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbabecki.scraper.model.PageModel;
import com.pbabecki.scraper.model.RuleModel;
import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.model.page.PageElementAttributes;
import com.pbabecki.scraper.model.page.PageElementPosition;
import com.pbabecki.scraper.model.page.PageOptionEnum;
import com.pbabecki.scraper.model.rule.RuleElement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.pbabecki.scraper.service.DirectionEnum.LEFT;
import static com.pbabecki.scraper.service.DirectionEnum.UP;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageAnalyzerService {

    private static final double MAX_PIXEL_DISTANCE_BWETWEEN_ELEM_AND_LABEL = 50;
    private static final double STEP_DISTANCE_IN_PIXELS = 5;

    private static final String INPUT_TAG_NAME = "INPUT";
    private static final String SELECT_TAG_NAME = "SELECT";
    private static final String TEXT_AREA_TAG_NAME = "TEXTAREA";

    private static final String LABEL_TAG_NAME = "LABEL";
    private static final String H3_TAG_NAME = "H3";

    private static final String BUTTON_TAG_NAME = "BUTTON";
    private static final String A_TAG_NAME = "A";

    private final List<String> INPUT_TAG_NAMES = Arrays.asList(INPUT_TAG_NAME, SELECT_TAG_NAME, TEXT_AREA_TAG_NAME);
    private final List<String> LABEL_TAG_NAMES = Arrays.asList(LABEL_TAG_NAME, H3_TAG_NAME);
    private final List<String> APPLY_TAG_NAMES = Arrays.asList(BUTTON_TAG_NAME, A_TAG_NAME);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PositionService positionService;
    private final RuleGeneratorService ruleGeneratorService;

    public RuleModel analyzePages(Map<PageOptionEnum, PageModel> pages) {
        RuleModel blankPageRuleModel = analyzeBlankPage(pages.get(PageOptionEnum.BLANK_FORM));
        return blankPageRuleModel;
    }

    @SneakyThrows
    private RuleModel analyzeBlankPage(PageModel pageModel) {
        List<PageElement> pageElements = pageModel.getElements().stream()
                .filter(elem -> elem.getPosition().getHeight() != 0 && elem.getPosition().getWidth() != 0)
                .collect(Collectors.toList());
        Map<PageElementPosition, PageElement> pageElementMap = transformPageElementsToPositionElementMap(pageElements);

        List<RuleElement> labelRuleElements = analyzeInputElements(pageElementMap, pageElements);


        return RuleModel.builder()
                .rules(labelRuleElements)
                .build();
    }

    @SneakyThrows
    private List<RuleElement> analyzeInputElements(Map<PageElementPosition, PageElement> pageElementMap, List<PageElement> pageElements) {
        List<PageElement> inputElements = filterPageElements(pageElements, pageElem -> INPUT_TAG_NAMES.contains(pageElem.getTagName()));

        Set<DirectionEnum> labelDirections = new HashSet<>(Arrays.asList(UP, LEFT));
        Map<PageElement, Map<DirectionEnum, Set<PageElement>>> nearestLabelElements = inputElements.stream()
                .collect(Collectors
                        .toMap(Function.identity(),
                                elem -> findNearestElements(pageElementMap, elem, labelDirections, findLabelPrecondition(), MAX_PIXEL_DISTANCE_BWETWEEN_ELEM_AND_LABEL)));
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(nearestLabelElements);
        log.info(json);
        List<RuleElement> labelRuleElements = ruleGeneratorService.generateLabelRules(nearestLabelElements);
        return labelRuleElements;
    }

    private Map<DirectionEnum, Set<PageElement>> findNearestElements(Map<PageElementPosition, PageElement> pageElementMap, PageElement pageElement, Set<DirectionEnum> directions, Predicate<PageElement> filter, double maxDistance) {
        Map<DirectionEnum, Set<PageElement>> directionEnumSetMap = new HashMap<>();
        directions.forEach(direction -> {
            Set<PageElement> elementsByPosition = positionService.findElementsByPosition(pageElementMap, pageElement.getPosition(), direction,
                    MAX_PIXEL_DISTANCE_BWETWEEN_ELEM_AND_LABEL, STEP_DISTANCE_IN_PIXELS);
            Set<PageElement> filteredElements = elementsByPosition.stream()
                    .filter(filter)
                    .collect(Collectors.toSet());
            directionEnumSetMap.put(direction, filteredElements);
        });
        return directionEnumSetMap;
    }

    private Predicate<PageElement> findLabelPrecondition() {
        return elem -> LABEL_TAG_NAMES.contains(elem.getTagName()) && StringUtils.isNotBlank(elem.getText());
    }

    private Map<PageElementPosition, PageElement> transformPageElementsToPositionElementMap(List<PageElement> pageElements) {
        Map<PageElementPosition, PageElement> rectangle2DPageElementMap = pageElements.stream()
                .collect(Collectors.toMap(
                        PageElement::getPosition,
                        Function.identity()));
        return rectangle2DPageElementMap;
    }

    private List<PageElement> filterPageElements(List<PageElement> pageElements, Predicate<PageElement> filterPredicate) {
        return pageElements.stream()
                .filter(filterPredicate)
                .collect(Collectors.toList());
    }

    private boolean isPageElemContainAnyAttribute(PageElement pageElement, List<PageElementAttributes> attributes) {
        //TODO: write logic when PageElementAttribute will contain values as list
        return true;
    }

}
