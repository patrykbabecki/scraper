package com.pbabecki.scraper.service;

import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.model.page.PageElementPosition;
import javafx.geometry.Point2D;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private static final double OFFSET_FROM_CORNER = 5;

    public Set<PageElement> findElementsByPosition(Map<PageElementPosition, PageElement> pageElementMap, PageElementPosition pageElementPosition, DirectionEnum direction, double maxDistance, double positionStep) {
        Point2D startPoint = getStartPoint(pageElementPosition, direction);
        double stepValue = positionStep * direction.getDirectionSign();
        Set<PageElement> pageElements = new HashSet<>();
        Set<PageElementPosition> pageElementPositions;
        switch (direction){
            case UP:
            case DOWN:
                pageElementPositions = findElements(pageElementMap.keySet(), stepValue, maxDistance, cureentStep -> new Point2D(startPoint.getX(), startPoint.getY() + cureentStep));
                break;
            case LEFT:
            case RIGHT:
                pageElementPositions = findElements(pageElementMap.keySet(), stepValue, maxDistance, cureentStep -> new Point2D(startPoint.getX() + cureentStep, startPoint.getY()));
                break;
                default:
                    throw new IllegalStateException("Direction not impelemented : " + direction);
        }
        pageElementPositions
                .forEach(position -> pageElements.add(pageElementMap.get(position)));
        return pageElements;
    }

    private Set<PageElementPosition> findElements(Set<PageElementPosition> pageElementPositions,  double stepValue, double maxDistance, Function<Double, Point2D> calculateCurrentPoint) {
        Set<PageElementPosition> elements = new HashSet<>();
        for(double currentStep = stepValue; Math.abs(currentStep) < maxDistance ; currentStep += stepValue) {
            Point2D currentPoint = calculateCurrentPoint.apply(currentStep);
            Set<PageElementPosition> elementPositions = pageElementPositions.stream()
                    .filter(pageElementPosition -> isPageElementPositionContainPoint(pageElementPosition, currentPoint.getX(), currentPoint.getY()))
                    .collect(Collectors.toSet());
            elements.addAll(elementPositions);
        }
        return elements;
    }

    private boolean isPageElementPositionContainPoint(PageElementPosition pageElementPosition, double x, double y) {
        return x < pageElementPosition.getMaxX() && x > pageElementPosition.getMinX() &&
                y < pageElementPosition.getMaxY() && y > pageElementPosition.getMinY();
    }

    private Point2D getStartPoint(PageElementPosition pageElementPosition, DirectionEnum direction) {
        switch (direction){
            case UP:
                return new Point2D(pageElementPosition.getMinX() + OFFSET_FROM_CORNER, pageElementPosition.getMinY());
            case DOWN:
                return new Point2D(pageElementPosition.getMinX() + OFFSET_FROM_CORNER, pageElementPosition.getMaxY());
            case LEFT:
                return new Point2D(pageElementPosition.getMinX(), pageElementPosition.getMinY() + OFFSET_FROM_CORNER);
            case RIGHT:
                return new Point2D(pageElementPosition.getMaxX(), pageElementPosition.getMinY() + OFFSET_FROM_CORNER);
                default:
                    throw new IllegalStateException("Direction not implemented : " + direction);
        }
    }

}
