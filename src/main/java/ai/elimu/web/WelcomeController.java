package ai.elimu.web;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {
    
    private final Logger logger = Logger.getLogger(getClass());

    @RequestMapping("/")
    public String handleRequest(Model model) {
    	logger.info("handleRequest");
    	
        return "welcome";
    }
}
