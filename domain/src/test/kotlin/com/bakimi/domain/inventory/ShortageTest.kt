package com.bakimi.domain.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class ShortageTest {

    @Test
    fun `when stock is not enough it returns the difference`() {
        assertEquals(50.0, calculateShortage(required = 200.0, stock = 150.0), 0.0)
    }

    @Test
    fun `when stock is more than enough it returns zero`() {
        assertEquals(0.0, calculateShortage(required = 100.0, stock = 300.0), 0.0)
    }
}
