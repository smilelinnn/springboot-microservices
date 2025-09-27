package com.example.product.web;

import com.example.product.dto.ProductDTO;
import com.example.product.dto.ProductStatsDTO;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
@Slf4j
public class ProductControllerV2 {

    private final ProductService productService;

    /**
     * 获取所有产品
     * GET /api/v2/products
     */
    @GetMapping
    @Cacheable(value = "products", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("Getting all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 根据ID获取产品
     * GET /api/v2/products/{id}
     */
    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("Getting product with id: {}", id);
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 获取所有产品分类
     * GET /api/v2/products/categories
     */
    @GetMapping("/categories")
    @Cacheable(value = "categories", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<List<String>> getCategories() {
        log.info("Getting all categories");
        List<String> categories = productService.getCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据分类获取产品
     * GET /api/v2/products/category/{category}
     */
    @GetMapping("/category/{category}")
    @Cacheable(value = "products", key = "'category:' + #category", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        log.info("Getting products for category: {}", category);
        List<ProductDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * 限制产品数量（用于分页）
     * GET /api/v2/products?limit={limit}
     */
    @GetMapping(params = "limit")
    @Cacheable(value = "products", key = "'limit:' + #limit", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<List<ProductDTO>> getProductsWithLimit(@RequestParam int limit) {
        log.info("Getting products with limit: {}", limit);
        if (limit <= 0 || limit > 100) {
            return ResponseEntity.badRequest().build();
        }
        List<ProductDTO> products = productService.getProductsWithLimit(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * 搜索产品
     * GET /api/v2/products/search?q={query}
     */
    @GetMapping("/search")
    @Cacheable(value = "products", key = "'search:' + #query", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String q) {
        log.info("Searching products with query: {}", q);
        List<ProductDTO> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }

    /**
     * 获取产品统计信息
     * GET /api/v2/products/stats
     */
    @GetMapping("/stats")
    @Cacheable(value = "productStats", key = "'all'", unless = "#result == null")
    public ResponseEntity<ProductStatsDTO> getProductStats() {
        log.info("Getting product statistics");
        ProductStatsDTO stats = productService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 根据价格范围获取产品
     * GET /api/v2/products/price-range?min={minPrice}&max={maxPrice}
     */
    @GetMapping("/price-range")
    @Cacheable(value = "products", key = "'price-range:' + #minPrice + ':' + #maxPrice", unless = "#result == null || #result.isEmpty()")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max) {
        log.info("Getting products with price range: {} - {}", min, max);
        List<ProductDTO> products = productService.getProductsByPriceRange(min, max);
        return ResponseEntity.ok(products);
    }

    /**
     * 清除所有缓存
     * POST /api/v2/products/cache/clear
     */
    @PostMapping("/cache/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = {"products", "categories", "productStats"}, allEntries = true)
    public void clearCache() {
        log.info("Clearing all product caches");
    }

    /**
     * 清除特定产品缓存
     * DELETE /api/v2/products/{id}/cache
     */
    @DeleteMapping("/{id}/cache")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = "products", key = "#id")
    public void clearProductCache(@PathVariable Long id) {
        log.info("Clearing cache for product with id: {}", id);
    }
}
