package hotel.HotelPackage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/luxuryrealestate")
public class LuxuryRealEstateController {

    @GetMapping
    public String showLuxuryRealEstatePage(Model model) {
        return "luxuryrealestate";
    }
}