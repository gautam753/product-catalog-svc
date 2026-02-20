package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.model.CategoryDto;
import com.mcart.productcatalogsvc.entity.Category;
import com.mcart.productcatalogsvc.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Flux<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
            .map(this::toDto);
    }

    public Mono<CategoryDto> getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
            .map(this::toDto);
    }

    public Mono<CategoryDto> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
            .map(this::toDto);
    }

    public Flux<CategoryDto> getRootCategories() {
        return categoryRepository.findRootCategories()
            .map(this::toDto);
    }

    public Flux<CategoryDto> getSubcategories(String parentId) {
        return categoryRepository.findSubcategories(parentId)
            .map(this::toDto);
    }

    /**
     * Build full category tree (root + recursive subcategories)
     */
    public Flux<CategoryDto> getCategoryTree() {
        return getRootCategories()
            .flatMap(root -> buildTree(root, 1));
    }

    private Mono<CategoryDto> buildTree(CategoryDto root, int depth) {
        return getSubcategories(root.getCategoryId())
            .collectList()
            .flatMap(subList -> {
                if (subList.isEmpty()) {
                    return Mono.just(root);
                }
                return Flux.fromIterable(subList)
                    .flatMap(sub -> buildTree(sub, depth + 1))
                    .collectList()
                    .map(children -> {
                        root.setSubcategories(children);
                        return root;
                    });
            });
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
            .categoryId(category.getCategoryId())
            .name(category.getName())
            .slug(category.getSlug())
            .parentCategoryId(category.getParentCategoryId())
            .level(category.getLevel())
            .isLeaf(category.getIsLeaf())
            .displayOrder(category.getDisplayOrder())
            .description(category.getDescription())
            .image(category.getImage())
            .build();
    }
}
