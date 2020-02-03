package com.pbabecki.scraper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {

    private int pixelsAround = 0;
    private String tagName = "INPUT";
    private String pageUrl = "https://www.hotelcareer.pl/index.php?sei_id=329&ang_id=2343180&count=1&button=top&k=6ffa408eabeb72b061013a3984d2e53ad83c8b5b";

}
