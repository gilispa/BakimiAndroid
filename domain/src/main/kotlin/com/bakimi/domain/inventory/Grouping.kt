package com.bakimi.domain.inventory

import java.text.Collator
import java.util.Locale

/**
 * Groups the inventory by location, in [StorageLocation] order, skipping empty groups.
 *
 * Within each group, available items come first and depleted ones last, both sorted by name
 * ignoring case and accents.
 */
fun groupByLocation(items: List<InventoryItem>): Map<StorageLocation, List<InventoryItem>> {
    val byName = Collator.getInstance(Locale.forLanguageTag("es")).apply {
        strength = Collator.PRIMARY
    }
    val order = compareBy<InventoryItem> { it.depleted }.thenComparator { a, b ->
        byName.compare(a.name, b.name)
    }
    val byLocation = items.groupBy { it.location }
    return StorageLocation.entries
        .mapNotNull { location -> byLocation[location]?.let { location to it.sortedWith(order) } }
        .toMap()
}
