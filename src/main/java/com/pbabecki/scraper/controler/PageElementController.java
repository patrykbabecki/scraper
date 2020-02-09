package com.pbabecki.scraper.controler;

import com.pbabecki.scraper.model.PageModel;
import com.pbabecki.scraper.model.RuleModel;
import com.pbabecki.scraper.model.page.PageElement;
import com.pbabecki.scraper.model.page.PageOptionEnum;
import com.pbabecki.scraper.service.PageAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PageElementController {

    private final PageAnalyzerService pageAnalyzerService;

    @PostMapping("/page/elements")
    @CrossOrigin("*")
    public RuleModel getPageElements(@RequestBody PageModel pageModel) {
        Map<PageOptionEnum, PageModel> pageOptionEnumPageModelMap = prepareMap(pageModel);
        RuleModel ruleModel = pageAnalyzerService.analyzePages(pageOptionEnumPageModelMap);
        return ruleModel;
    }

    private Map<PageOptionEnum, PageModel> prepareMap(PageModel pageModel) {
        Map<PageOptionEnum, PageModel> map = new HashMap<>();
        map.put(pageModel.getOption(), pageModel);
        return map;
    }

}
