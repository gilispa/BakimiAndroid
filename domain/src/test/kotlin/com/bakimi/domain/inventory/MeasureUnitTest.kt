package com.bakimi.domain.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasureUnitTest {

    @Test
    fun `kilograms to grams multiplies by a thousand`() {
        assertEquals(1500.0, convert(1.5, MeasureUnit.KILOGRAM, MeasureUnit.GRAM)!!, 0.0)
    }

    @Test
    fun `milliliters to liters divides by a thousand`() {
        assertEquals(0.25, convert(250.0, MeasureUnit.MILLILITER, MeasureUnit.LITER)!!, 0.0)
    }

    @Test
    fun `converting to the same unit keeps the quantity`() {
        assertEquals(3.0, convert(3.0, MeasureUnit.PIECE, MeasureUnit.PIECE)!!, 0.0)
    }

    @Test
    fun `pieces to grams is not converted without an equivalence`() {
        assertNull(convert(2.0, MeasureUnit.PIECE, MeasureUnit.GRAM))
    }

    @Test
    fun `weight to volume is not converted`() {
        assertNull(convert(1.0, MeasureUnit.KILOGRAM, MeasureUnit.LITER))
    }

    @Test
    fun `conversion does not carry floating point errors`() {
        assertEquals(0.3, convert(300.0, MeasureUnit.GRAM, MeasureUnit.KILOGRAM)!!, 0.0)
        assertEquals(300.0, convert(0.3, MeasureUnit.KILOGRAM, MeasureUnit.GRAM)!!, 0.0)
    }

    @Test
    fun `compatibility depends on the dimension`() {
        assertTrue(MeasureUnit.GRAM.isCompatibleWith(MeasureUnit.KILOGRAM))
        assertFalse(MeasureUnit.PIECE.isCompatibleWith(MeasureUnit.GRAM))
    }
}
