package com.bakimi.domain.ingredients

import com.bakimi.domain.inventory.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EquivalenceTest {

    private val tortilla = Ingredient("tortilla_maiz", "Tortilla de maíz", gramsPerPiece = 30.0)
    private val garlic = Ingredient("ajo", "Ajo")

    @Test
    fun `between compatible units the conversion is exact`() {
        val conversion = tortilla.convert(1.0, MeasureUnit.KILOGRAM, MeasureUnit.GRAM)!!

        assertEquals(1000.0, conversion.quantity, 0.0)
        assertFalse(conversion.approximate)
    }

    @Test
    fun `a kilo of tortillas is about 33 pieces`() {
        val conversion = tortilla.convert(1.0, MeasureUnit.KILOGRAM, MeasureUnit.PIECE)!!

        assertEquals(33.3, conversion.quantity, 0.0)
        assertTrue(conversion.approximate)
    }

    @Test
    fun `from pieces to grams uses the same weight per piece`() {
        val conversion = tortilla.convert(6.0, MeasureUnit.PIECE, MeasureUnit.GRAM)!!

        assertEquals(180.0, conversion.quantity, 0.0)
        assertTrue(conversion.approximate)
    }

    @Test
    fun `without weight per piece it does not convert pieces to weight`() {
        assertNull(garlic.convert(1.0, MeasureUnit.PIECE, MeasureUnit.GRAM))
    }

    @Test
    fun `weight per piece cannot convert volume`() {
        assertNull(tortilla.convert(1.0, MeasureUnit.LITER, MeasureUnit.PIECE))
    }
}
