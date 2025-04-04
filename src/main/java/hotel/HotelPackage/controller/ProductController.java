package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.Product;
import hotel.HotelPackage.repository.ProductRepository;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/all")
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        List<String> categories = productRepository.findDistinctCategories();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "products";
    }

    @GetMapping("/category/{category}")
    public String listProductsByCategory(@PathVariable("category") String category, Model model) {
        List<Product> products = productRepository.findByCategory(category);
        List<String> categories = productRepository.findDistinctCategories();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategory", category);
        return "products";
    }

    @GetMapping("/details/{id}")
    public String showProductDetails(@PathVariable("id") int id, Model model) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "productDetails";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showNewProductForm(Model model) {
        model.addAttribute("product", new Product());
        // Add available categories to help with form selection
        List<String> categories = productRepository.findDistinctCategories();
        model.addAttribute("categories", categories);
        return "newProduct";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ins")
    public String saveProduct(@ModelAttribute("product") Product product, Model model) {
        try {
            productRepository.save(product);
            model.addAttribute("successMessage", "Product saved successfully!");
            return "redirect:/products/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving product: " + e.getMessage());
            List<String> categories = productRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "newProduct";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") int id, Model model) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        List<String> categories = productRepository.findDistinctCategories();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "editProduct";
    }

    @PostMapping("/upd")
    public String updateProduct(@ModelAttribute("product") Product product, Model model) {
        try {
            productRepository.save(product);
            model.addAttribute("successMessage", "Product updated successfully!");
            return "redirect:/products/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating product: " + e.getMessage());
            List<String> categories = productRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "editProduct";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id, Model model) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                model.addAttribute("successMessage", "Product deleted successfully!");
            }
            return "redirect:/products/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Cannot delete product: associated orders exist.");
            return "redirect:/products/all";
        }
    }

    @PostMapping("/search")
    public String listProductsByPatternLike(Model model, @RequestParam String pattern) {
        List<Product> products = productRepository.findByPatternLike(pattern);
        List<String> categories = productRepository.findDistinctCategories();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "products";
    }
}