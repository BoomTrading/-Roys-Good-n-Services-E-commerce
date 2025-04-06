package hotel.HotelPackage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConciergeController {

    @GetMapping("/contactconcierge")
    public String showContactConciergeAlternative() {
        return "contactconcierge";
    }
}