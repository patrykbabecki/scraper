package com.pbabecki.scraper.service;

import lombok.Getter;

public enum DirectionEnum {
    UP(-1),
    LEFT(-1),
    RIGHT(1),
    DOWN(1);

    @Getter
    private double directionSign;

    DirectionEnum(double directionSign) {
        this.directionSign = directionSign;
    }

}
