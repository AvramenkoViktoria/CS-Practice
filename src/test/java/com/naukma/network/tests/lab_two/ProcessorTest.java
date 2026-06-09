package com.naukma.network.tests.lab_two;

import com.naukma.db.DataSourceProvider;
import com.naukma.model.Warehouse;
import com.naukma.network.messaging.Processor;
import com.naukma.network.packet.*;
import com.naukma.network.tests.util.DBUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorTest {

    private Processor processor;
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
    void setUp() throws SQLException {
        DBUtil.clearDatabase();
        warehouse = Warehouse.createDefault();
        processor = new Processor(warehouse);
        warehouse.addProduct("P001", "Laptop", 1000);
        warehouse.addProduct("P002", "Mouse", 200);
        warehouse.addStock("P001", 100);
    }

    @Test
    void shouldProcessGetStockQuantity() {
        GetStockQuantityPacket packet = new GetStockQuantityPacket("P001");
        String result = processor.process(packet);

        assertTrue(result.contains("P001"));
        assertTrue(result.contains("100"));
    }

    @Test
    void shouldProcessAddStock() throws SQLException {
        AddStockPacket packet = new AddStockPacket("P001", 50);
        String result = processor.process(packet);

        assertTrue(result.contains("Added"));
        assertTrue(result.contains("P001"));
        assertEquals(150, warehouse.getStockQuantity("P001"));
    }

    @Test
    void shouldProcessDeductStockSuccessfully() throws SQLException {
        DeductStockPacket packet = new DeductStockPacket("P001", 30);
        String result = processor.process(packet);

        assertTrue(result.contains("Successfully deducted"));
        assertEquals(70, warehouse.getStockQuantity("P001"));
    }

    @Test
    void shouldProcessDeductStockWithInsufficientQuantity() throws SQLException {
        DeductStockPacket packet = new DeductStockPacket("P001", 200);
        String result = processor.process(packet);

        assertTrue(result.contains("Not enough stock") || result.contains("Error"));
        assertEquals(100, warehouse.getStockQuantity("P001"));
    }

    @Test
    void shouldProcessAddProductGroup() {
        AddProductGroupPacket packet = new AddProductGroupPacket("G001", "Electronics");
        String result = processor.process(packet);

        assertTrue(result.contains("Group created"));
        assertTrue(result.contains("Electronics"));
    }

    @Test
    void shouldProcessAddProductToGroup() throws SQLException {
        warehouse.addGroup("G001", "Electronics");
        AddProductToGroupPacket packet = new AddProductToGroupPacket("G001", "P001");
        String result = processor.process(packet);

        assertTrue(result.contains("added to group"));
    }

    @Test
    void shouldProcessSetProductPrice() {
        SetProductPricePacket packet = new SetProductPricePacket("P001", 1299.99);
        String result = processor.process(packet);

        assertTrue(result.contains("Price for P001"));
        assertTrue(result.contains("1299.99"));
    }

    @Test
    void shouldReturnUnknownPacketMessageForUnsupportedType() {
        Packet unknownPacket = new Packet() {};
        String result = processor.process(unknownPacket);

        assertTrue(result.contains("Unknown packet"));
    }

    @Test
    void shouldHandleNonExistentProductGracefully() {
        GetStockQuantityPacket packet = new GetStockQuantityPacket("NONEXISTENT");
        String result = processor.process(packet);

        assertTrue(result.contains("0") || result.contains("not found"));
    }

    @Test
    void shouldProcessMultipleOperationsCorrectly() {
        processor.process(new AddStockPacket("P002", 75));
        processor.process(new SetProductPricePacket("P002", 49.99));
        String result = processor.process(new GetStockQuantityPacket("P002"));

        assertTrue(result.contains("P002"));
        assertTrue(result.contains("75"));
    }
}