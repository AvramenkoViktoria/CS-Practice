package com.naukma.network.tests.lab_four;

import com.naukma.db.DataSourceProvider;
import com.naukma.model.Page;
import com.naukma.model.Product;
import com.naukma.model.ProductFilter;
import com.naukma.model.Warehouse;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DAOTest {

    private Warehouse warehouse;

    @BeforeAll
    static void initDatabase() {
        DataSourceProvider.init(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
                "sa",
                ""
        );
        System.out.println("Test H2 database initialized.");
    }

    @AfterAll
    static void closeDatabase() {
        DataSourceProvider.close();
    }

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.createDefault();
    }

    @Test
    @Order(1)
    void testAddProduct() throws SQLException {
        warehouse.addProduct("p1", "Laptop", 999.99);
        Optional<Product> product = warehouse.getProduct("p1");

        assertTrue(product.isPresent());
        assertEquals("Laptop", product.get().getName());
        assertEquals(999.99, product.get().getPrice());
        assertEquals(0, product.get().getQuantity());
    }

    @Test
    @Order(2)
    void testAddProduct_Idempotent() throws SQLException {
        warehouse.addProduct("p2", "Phone", 599.0);
        warehouse.addProduct("p2", "Phone Updated", 1000.0);

        Optional<Product> product = warehouse.getProduct("p2");
        assertEquals("Phone", product.get().getName());
    }

    @Test
    void testGetStockQuantity() throws SQLException {
        warehouse.addProduct("p3", "Tablet", 300.0);
        warehouse.addStock("p3", 15);

        int quantity = warehouse.getStockQuantity("p3");
        assertEquals(15, quantity);
    }

    @Test
    void testAddStock() throws SQLException {
        warehouse.addProduct("p4", "Monitor", 250.0);
        warehouse.addStock("p4", 10);

        assertEquals(10, warehouse.getStockQuantity("p4"));
    }

    @Test
    void testDeductStock_Success() throws SQLException {
        warehouse.addProduct("p5", "Keyboard", 50.0);
        warehouse.addStock("p5", 20);

        boolean success = warehouse.deductStock("p5", 8);
        assertTrue(success);
        assertEquals(12, warehouse.getStockQuantity("p5"));
    }

    @Test
    void testDeductStock_NotEnoughStock() throws SQLException {
        warehouse.addProduct("p6", "Mouse", 30.0);
        warehouse.addStock("p6", 5);

        boolean success = warehouse.deductStock("p6", 10);
        assertFalse(success);
        assertEquals(5, warehouse.getStockQuantity("p6"));
    }

    @Test
    void testSetPrice() throws SQLException {
        warehouse.addProduct("p7", "Headphones", 80.0);
        warehouse.setPrice("p7", 89.99);

        Optional<Product> p = warehouse.getProduct("p7");
        assertEquals(89.99, p.get().getPrice());
    }

    @Test
    void testDeleteProduct() throws SQLException {
        warehouse.addProduct("p8", "ToDelete", 100.0);
        assertTrue(warehouse.getProduct("p8").isPresent());

        warehouse.deleteProduct("p8");
        assertTrue(warehouse.getProduct("p8").isEmpty());
    }


    @Test
    void testAddGroup() throws SQLException {
        warehouse.addGroup("g1", "Electronics");
        assertDoesNotThrow(() -> warehouse.addProductToGroup("g1", "p1"));
    }

    @Test
    void testAddProductToGroup() throws SQLException {
        warehouse.addProduct("p10", "Camera", 450.0);
        warehouse.addGroup("g2", "Photo Equipment");

        assertDoesNotThrow(() -> warehouse.addProductToGroup("g2", "p10"));
    }

    @Test
    void testSearchProducts_WithVariousFilters() throws SQLException {
        warehouse.addProduct("sp1", "Gaming Laptop", 1299.99);
        warehouse.addStock("sp1", 5);

        warehouse.addProduct("sp2", "Office Mouse", 29.99);
        warehouse.addStock("sp2", 50);

        warehouse.addProduct("sp3", "Wireless Headphones", 89.99);
        warehouse.addStock("sp3", 20);

        warehouse.addProduct("sp4", "4K Monitor", 399.99);
        warehouse.addStock("sp4", 8);

        warehouse.addProduct("sp5", "Gaming Mouse", 45.50);
        warehouse.addStock("sp5", 30);

        ProductFilter filter1 = ProductFilter.builder()
                .nameContains("Gaming")
                .build();

        Page<Product> result1 = warehouse.searchProducts(filter1);
        assertTrue(result1.getTotalElements() >= 2, "Should find Gaming Laptop and Gaming Mouse");
        assertTrue(result1.getContent().stream().anyMatch(p -> p.getName().contains("Gaming")));

        ProductFilter filter2 = ProductFilter.builder()
                .minPrice(300.0)
                .maxPrice(1000.0)
                .build();

        Page<Product> result2 = warehouse.searchProducts(filter2);
        assertTrue(result2.getTotalElements() >= 1, "Should find 4K Monitor");

        ProductFilter filter3 = ProductFilter.builder()
                .minQuantity(20)
                .build();

        Page<Product> result3 = warehouse.searchProducts(filter3);
        assertTrue(result3.getTotalElements() >= 2);

        ProductFilter filter4 = ProductFilter.builder()
                .nameContains("Mouse")
                .minPrice(30.0)
                .minQuantity(25)
                .build();

        Page<Product> result4 = warehouse.searchProducts(filter4);
        assertEquals(1, result4.getTotalElements(), "Should find only Office Mouse");

        ProductFilter filter5 = ProductFilter.builder()
                .page(1)
                .pageSize(3)
                .build();

        Page<Product> result5 = warehouse.searchProducts(filter5);
        assertEquals(3, result5.getContent().size());
        assertTrue(result5.getTotalElements() >= 5);
    }

    @Test
    void testValidation_ThrowsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () ->
                warehouse.addProduct("", "Test", 100));

        assertThrows(IllegalArgumentException.class, () ->
                warehouse.addProduct("p9", "Test", -50));

        assertThrows(IllegalArgumentException.class, () ->
                warehouse.addStock("p9", -5));

        assertThrows(IllegalArgumentException.class, () ->
                warehouse.deductStock("p9", 0));
    }

    @Test
    void testProductNotFound_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                warehouse.setPrice("non-existent", 100.0));
    }
}