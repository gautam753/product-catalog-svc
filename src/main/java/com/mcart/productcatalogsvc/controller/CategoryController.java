package com.mcart.productcatalogsvc.controller;

import com.mcart.productcatalogsvc.model.CategoryDto;
import com.mcart.productcatalogsvc.service.CategoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping(value = "/tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CategoryDto> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    @GetMapping("/{id}")
    public Mono<CategoryDto> getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping("/slug/{slug}")
    public Mono<CategoryDto> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug);
    }

    @GetMapping("/root")
    public Flux<CategoryDto> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @GetMapping("/{id}/subcategories")
    public Flux<CategoryDto> getSubcategories(@PathVariable String id) {
        return categoryService.getSubcategories(id);
    }
}
