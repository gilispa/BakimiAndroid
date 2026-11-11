package com.bakimi.data.inventory

import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class InventoryMappingTest {

    @Test
    fun `a food with expiry survives the round trip`() {
        val item = InventoryItem(
            id = 7,
            name = "Pechuga de pollo",
            quantity = 1.0,
            unit = MeasureUnit.KILOGRAM,
            location = StorageLocation.FREEZER,
            expiry = LocalDate.of(2026, 11, 20),
            ingredientId = "pechuga_pollo",
        )

        assertEquals(item, item.toEntity().toDomain())
    }

    @Test
    fun `a food outside the catalog keeps an empty link`() {
        val item = InventoryItem(
            name = "Salsa Valentina",
            quantity = 1.0,
            unit = MeasureUnit.PIECE,
            location = StorageLocation.PANTRY,
        )

        assertEquals(null, item.toEntity().ingredientId)
        assertEquals(item, item.toEntity().toDomain())
    }

    @Test
    fun `a food without expiry stores null`() {
        val item = InventoryItem(
            name = "Arroz",
            quantity = 500.0,
            unit = MeasureUnit.GRAM,
            location = StorageLocation.PANTRY,
        )

        assertNull(item.toEntity().expiryEpochDay)
    }

    @Test
    fun `enums are stored with stable codes, not by position`() {
        val entity = InventoryItem(
            name = "Leche",
            quantity = 1.0,
            unit = MeasureUnit.LITER,
            location = StorageLocation.FRIDGE,
        ).toEntity()

        assertEquals("LITRO", entity.unit)
        assertEquals("REFRIGERADOR", entity.location)
    }
}
