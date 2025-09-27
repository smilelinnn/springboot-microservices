package com.example.product.service;

import com.example.product.client.FakeStoreApiClient;
import com.example.product.domain.Product;
import com.example.product.dto.ProductDTO;
import com.example.product.dto.ProductStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final FakeStoreApiClient fakeStoreApiClient;

    /**
     * 获取所有产品
     */
    public List<ProductDTO> getAllProducts() {
        log.info("Getting all products");
        List<Product> products = fakeStoreApiClient.getAllProducts();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取产品
     */
    public ProductDTO getProductById(Long id) {
        log.info("Getting product with id: {}", id);
        Product product = fakeStoreApiClient.getProductById(id);
        if (product == null) {
            return null;
        }
        return convertToDTO(product);
    }

    /**
     * 获取所有产品分类
     */
    public List<String> getCategories() {
        log.info("Getting all categories");
        return fakeStoreApiClient.getCategories();
    }

    /**
     * 根据分类获取产品
     */
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Getting products for category: {}", category);
        List<Product> products = fakeStoreApiClient.getProductsByCategory(category);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 限制产品数量（用于分页）
     */
    public List<ProductDTO> getProductsWithLimit(int limit) {
        log.info("Getting products with limit: {}", limit);
        List<Product> products = fakeStoreApiClient.getProductsWithLimit(limit);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 搜索产品（按标题或描述）
     */
    public List<ProductDTO> searchProducts(String query) {
        log.info("Searching products with query: {}", query);
        List<Product> allProducts = fakeStoreApiClient.getAllProducts();

        if (query == null || query.trim().isEmpty()) {
            return allProducts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        String lowerQuery = query.toLowerCase();
        return allProducts.stream()
                .filter(product ->
                        (product.getTitle() != null && product.getTitle().toLowerCase().contains(lowerQuery)) ||
                                (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery)) ||
                                (product.getCategory() != null && product.getCategory().toLowerCase().contains(lowerQuery))
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取产品统计信息
     */
    public ProductStatsDTO getProductStats() {
        log.info("Getting product statistics");
        List<Product> products = fakeStoreApiClient.getAllProducts();

        if (products.isEmpty()) {
            return ProductStatsDTO.builder()
                    .totalProducts(0L)
                    .categories(Collections.emptyList())
                    .productsByCategory(Collections.emptyMap())
                    .averagePrice(0.0)
                    .minPrice(0.0)
                    .maxPrice(0.0)
                    .build();
        }

        // 计算基本统计
        long totalProducts = products.size();
        List<String> categories = products.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 按分类统计产品数量
        Map<String, Long> productsByCategory = products.stream()
                .filter(product -> product.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.counting()
                ));

        // 价格统计
        List<Double> prices = products.stream()
                .map(Product::getPrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        double averagePrice = prices.isEmpty() ? 0.0 : prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double minPrice = prices.isEmpty() ? 0.0 : Collections.min(prices);
        double maxPrice = prices.isEmpty() ? 0.0 : Collections.max(prices);

        return ProductStatsDTO.builder()
                .totalProducts(totalProducts)
                .categories(categories)
                .productsByCategory(productsByCategory)
                .averagePrice(Math.round(averagePrice * 100.0) / 100.0) // 保留两位小数
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }

    /**
     * 获取价格范围的产品
     */
    public List<ProductDTO> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        log.info("Getting products with price range: {} - {}", minPrice, maxPrice);
        List<Product> allProducts = fakeStoreApiClient.getAllProducts();

        return allProducts.stream()
                .filter(product -> {
                    Double price = product.getPrice();
                    if (price == null) return false;

                    boolean minCheck = minPrice == null || price >= minPrice;
                    boolean maxCheck = maxPrice == null || price <= maxPrice;

                    return minCheck && maxCheck;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换Product到ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .description(product.getDescription())
                .category(product.getCategory())
                .image(product.getImage())
                .rating(product.getRating())
                .build();
    }
}
