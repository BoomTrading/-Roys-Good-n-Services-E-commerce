package hotel.HotelPackage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/investments")
public class InvestmentsController {

    @GetMapping
    public String showInvestmentsPage(Model model) {
        return "investments";
    }
}