package com.pbabecki.scraper.model.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageElementPosition {

    @JsonIgnore
    private UUID uuid = UUID.randomUUID();
    private double x;
    private double y;
    private double width;
    private double height;

    public double getMinX() {
        return x;
    }

    public double getMinY() {
        return y;
    }

    public double getMaxX() {
        return x + width;
    }

    public double getMaxY() {
        return y + height;
    }

}
