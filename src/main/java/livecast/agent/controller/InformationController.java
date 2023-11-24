package livecast.agent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/informations")
public class InformationController {
    private static final Logger logger = LoggerFactory.getLogger(InformationController.class);
    public static final String NAME = "livecast.agent";
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final String BUILD_ID = "20221103_1207";
    public static final String BUILD_NUMBER = "0";

    public InformationController() {
        logger.info("Appliction Information : {}", getFullName());
    }

    @GetMapping("/name")
    public ResponseEntity<String> name(HttpServletRequest httpRequest) {
        return ResponseEntity
                .ok(NAME);
    }

    @GetMapping("/version")
    public ResponseEntity<String> version(HttpServletRequest httpRequest) {
        return ResponseEntity
                .ok(VERSION);
    }

    @GetMapping("/build-id")
    public ResponseEntity<String> buildId(HttpServletRequest httpRequest) {
        return ResponseEntity
                .ok(BUILD_ID);
    }

    @GetMapping("/build-number")
    public ResponseEntity<String> buildNumber(HttpServletRequest httpRequest) {
        return ResponseEntity
                .ok(BUILD_NUMBER);
    }

    @GetMapping("/fullname")
    public ResponseEntity<String> fullName(HttpServletRequest httpRequest) {
        return ResponseEntity
                .ok(getFullName());
    }

    private String getFullName() {
        return String.format("%s %s %s #%s", NAME, VERSION, BUILD_ID, BUILD_NUMBER);
    }
}

