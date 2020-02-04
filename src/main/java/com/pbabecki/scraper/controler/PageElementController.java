package com.pbabecki.scraper.controler;

import com.pbabecki.scraper.model.PageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PageElementController {

    @PostMapping("/page/elements")
    @CrossOrigin("*")
    public ResponseEntity getPageElements(@RequestBody PageModel pageModel) {

        log.info(pageModel.toString());
        return ResponseEntity.ok().build();
    }

}
