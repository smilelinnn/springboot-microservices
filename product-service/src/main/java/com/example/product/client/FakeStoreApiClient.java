package com.example.product.client;

import com.example.product.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FakeStoreApiClient {

    private final WebClient webClient;
    private final String baseUrl = "https://fakestoreapi.com";

    public FakeStoreApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }

    /**
     * 获取所有产品
     */
    public List<Product> getAllProducts() {
        try {
            log.info("Fetching all products from FakeStore API");
            return webClient.get()
                    .uri("/products")
                    .retrieve()
                    .bodyToFlux(Product.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(10))
                    .onErrorReturn(Collections.emptyList())
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error fetching all products: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error fetching all products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 根据ID获取产品
     */
    public Product getProductById(Long id) {
        try {
            log.info("Fetching product with id: {} from FakeStore API", id);
            return webClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorReturn(null)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Product with id {} not found", id);
            return null;
        } catch (WebClientResponseException e) {
            log.error("Error fetching product with id {}: {}", id, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching product with id {}: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有产品分类
     */
    public List<String> getCategories() {
        try {
            log.info("Fetching categories from FakeStore API");
            return webClient.get()
                    .uri("/products/categories")
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(10))
                    .onErrorReturn(Collections.emptyList())
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error fetching categories: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error fetching categories: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 根据分类获取产品
     */
    public List<Product> getProductsByCategory(String category) {
        try {
            log.info("Fetching products for category: {} from FakeStore API", category);
            return webClient.get()
                    .uri("/products/category/{category}", category)
                    .retrieve()
                    .bodyToFlux(Product.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(10))
                    .onErrorReturn(Collections.emptyList())
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error fetching products for category {}: {}", category, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error fetching products for category {}: {}", category, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 限制产品数量（用于分页）
     */
    public List<Product> getProductsWithLimit(int limit) {
        try {
            log.info("Fetching {} products from FakeStore API", limit);
            return webClient.get()
                    .uri("/products?limit={limit}", limit)
                    .retrieve()
                    .bodyToFlux(Product.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(10))
                    .onErrorReturn(Collections.emptyList())
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error fetching products with limit {}: {}", limit, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error fetching products with limit {}: {}", limit, e.getMessage());
            return Collections.emptyList();
        }
    }
}
