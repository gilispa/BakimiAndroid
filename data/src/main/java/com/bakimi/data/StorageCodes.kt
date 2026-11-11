package com.bakimi.data

import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.StorageLocation

/*
 * Codes persisted in the database. They are fixed on purpose: renaming or reordering an enum in
 * the code must never change data that is already stored. They keep the original Spanish values
 * from the first database versions.
 */

private val UNIT_CODES: Map<MeasureUnit, String> = mapOf(
    MeasureUnit.GRAM to "GRAMO",
    MeasureUnit.KILOGRAM to "KILOGRAMO",
    MeasureUnit.MILLILITER to "MILILITRO",
    MeasureUnit.LITER to "LITRO",
    MeasureUnit.PIECE to "PIEZA",
)

private val LOCATION_CODES: Map<StorageLocation, String> = mapOf(
    StorageLocation.FRIDGE to "REFRIGERADOR",
    StorageLocation.FREEZER to "CONGELADOR",
    StorageLocation.PANTRY to "DESPENSA",
)

private val UNITS_BY_CODE = UNIT_CODES.entries.associate { (unit, code) -> code to unit }
private val LOCATIONS_BY_CODE = LOCATION_CODES.entries.associate { (location, code) -> code to location }

val MeasureUnit.storageCode: String get() = UNIT_CODES.getValue(this)

val StorageLocation.storageCode: String get() = LOCATION_CODES.getValue(this)

fun measureUnitFromCode(code: String): MeasureUnit =
    UNITS_BY_CODE[code] ?: error("Unknown stored unit: $code")

fun storageLocationFromCode(code: String): StorageLocation =
    LOCATIONS_BY_CODE[code] ?: error("Unknown stored location: $code")
