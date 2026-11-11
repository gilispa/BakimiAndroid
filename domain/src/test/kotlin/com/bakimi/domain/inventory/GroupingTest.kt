package com.bakimi.domain.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupingTest {

    private fun item(name: String, location: StorageLocation, quantity: Double = 1.0) =
        InventoryItem(name = name, quantity = quantity, unit = MeasureUnit.PIECE, location = location)

    @Test
    fun `groups in fridge, freezer, pantry order and skips empty groups`() {
        val groups = groupByLocation(
            listOf(
                item("Arroz", StorageLocation.PANTRY),
                item("Huevo", StorageLocation.FRIDGE),
            ),
        )

        assertEquals(listOf(StorageLocation.FRIDGE, StorageLocation.PANTRY), groups.keys.toList())
    }

    @Test
    fun `sorts by name ignoring case and accents`() {
        val groups = groupByLocation(
            listOf(
                item("lechuga", StorageLocation.FRIDGE),
                item("Ajo", StorageLocation.FRIDGE),
                item("Ácido cítrico", StorageLocation.FRIDGE),
                item("Brócoli", StorageLocation.FRIDGE),
            ),
        )

        assertEquals(
            listOf("Ácido cítrico", "Ajo", "Brócoli", "lechuga"),
            groups.getValue(StorageLocation.FRIDGE).map { it.name },
        )
    }

    @Test
    fun `depleted foods go to the end of their group`() {
        val groups = groupByLocation(
            listOf(
                item("Aceite", StorageLocation.PANTRY, quantity = 0.0),
                item("Sal", StorageLocation.PANTRY),
                item("Arroz", StorageLocation.PANTRY),
            ),
        )

        assertEquals(
            listOf("Arroz", "Sal", "Aceite"),
            groups.getValue(StorageLocation.PANTRY).map { it.name },
        )
    }

    @Test
    fun `empty inventory produces no groups`() {
        assertEquals(emptyMap<StorageLocation, List<InventoryItem>>(), groupByLocation(emptyList()))
    }
}
