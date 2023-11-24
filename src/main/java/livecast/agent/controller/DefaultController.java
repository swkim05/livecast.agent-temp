package livecast.agent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {
    @RequestMapping({"/", "/home", "/oci"})
    public String getUIResource() {
        return "forward:/index.html";
    }
}
