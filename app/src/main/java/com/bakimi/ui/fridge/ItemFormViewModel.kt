package com.bakimi.ui.fridge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.IngredientRepository
import com.bakimi.domain.ingredients.searchIngredients
import com.bakimi.domain.ingredients.buscarPorNombre
import com.bakimi.domain.inventory.ItemError
import com.bakimi.domain.inventory.InventoryManager
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.SaveResult
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.parseQuantity
import com.bakimi.domain.inventory.validate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Food chosen in the search. */
sealed interface Selection {
    val name: String

    data class FromCatalog(val ingredient: Ingredient) : Selection {
        override val name: String get() = ingredient.name
    }

    /** Food that is not in the catalog; it is saved with the name the user typed. */
    data class Other(override val name: String) : Selection
}

data class ItemFormUiState(
    val isEditing: Boolean = false,
    val loading: Boolean = false,
    val selection: Selection? = null,
    val searchText: String = "",
    val results: List<Ingredient> = emptyList(),
    /** The search does not exactly match anything in the catalog: offer to add it as "other". */
    val offerOther: Boolean = false,
    val quantityText: String = "1",
    val unit: MeasureUnit = MeasureUnit.PIECE,
    val location: StorageLocation = StorageLocation.FRIDGE,
    val expiry: LocalDate? = null,
    val errors: Set<ItemError> = emptySet(),
    val saveFailed: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class ItemFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val manager: InventoryManager,
    ingredients: IngredientRepository,
) : ViewModel() {

    private val id: Long = savedStateHandle.get<Long>(ARG_ID) ?: 0L
    private val catalog: List<Ingredient> = ingredients.all()

    private val _state = MutableStateFlow(ItemFormUiState(isEditing = id != 0L, loading = id != 0L))
    val state: StateFlow<ItemFormUiState> = _state.asStateFlow()

    init {
        if (id != 0L) {
            viewModelScope.launch {
                val item = manager.get(id)
                _state.update { current ->
                    if (item == null) {
                        // It no longer exists (for example, it was deleted): treat it as a new food.
                        current.copy(isEditing = false, loading = false)
                    } else {
                        current.copy(
                            loading = false,
                            selection = selectionFor(item),
                            quantityText = editableText(item.quantity),
                            unit = item.unit,
                            location = item.location,
                            expiry = item.expiry,
                        )
                    }
                }
            }
        }
    }

    /**
  * Records created before the catalog have no link: if their name matches an ingredient, they get
  * linked when saved.
  */
    private fun selectionFor(item: InventoryItem): Selection {
        val ingredient = item.ingredientId?.let { id -> catalog.firstOrNull { it.id == id } }
            ?: catalog.buscarPorNombre(item.name)
        return ingredient?.let { Selection.FromCatalog(it) } ?: Selection.Other(item.name)
    }

    fun changeSearch(text: String) = _state.update {
        it.copy(
            searchText = text,
            results = searchIngredients(text, catalog),
            offerOther = text.isNotBlank() && catalog.buscarPorNombre(text) == null,
            errors = it.errors - ItemError.EMPTY_NAME,
        )
    }

    /** Choosing from the catalog also suggests its usual unit and location. */
    fun choose(ingredient: Ingredient) = _state.update {
        it.copy(
            selection = Selection.FromCatalog(ingredient),
            unit = ingredient.suggestedUnit,
            location = ingredient.suggestedLocation,
            searchText = "",
            results = emptyList(),
            offerOther = false,
        )
    }

    fun chooseOther() = _state.update {
        val name = it.searchText.trim()
        if (name.isEmpty()) it else it.copy(selection = Selection.Other(name), results = emptyList(), offerOther = false)
    }

    /** Goes back to the search with the current name typed, to correct it quickly. */
    fun changeSelection() {
        val name = _state.value.selection?.name.orEmpty()
        _state.update { it.copy(selection = null) }
        changeSearch(name)
    }

    fun changeQuantity(value: String) =
        _state.update { it.copy(quantityText = value, errors = it.errors - ItemError.INVALID_QUANTITY) }

    fun changeUnit(value: MeasureUnit) = _state.update { it.copy(unit = value) }

    fun changeLocation(value: StorageLocation) = _state.update { it.copy(location = value) }

    fun changeExpiry(value: LocalDate?) = _state.update { it.copy(expiry = value) }

    fun save() {
        val current = _state.value
        if (current.saving || current.loading) return

        val quantity = parseQuantity(current.quantityText)
        val item = InventoryItem(
            id = if (current.isEditing) id else 0L,
            name = current.selection?.name.orEmpty(),
            quantity = quantity ?: 0.0,
            unit = current.unit,
            location = current.location,
            expiry = current.expiry,
            ingredientId = (current.selection as? Selection.FromCatalog)?.ingredient?.id,
        )
        val errors = validate(item) + if (quantity == null) setOf(ItemError.INVALID_QUANTITY) else emptySet()
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }

        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                when (val result = manager.save(item)) {
                    is SaveResult.Saved ->
                        _state.update { it.copy(saving = false, saved = true) }

                    is SaveResult.Invalid ->
                        _state.update { it.copy(saving = false, errors = result.errors) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, saveFailed = true) }
            }
        }
    }

    companion object {
        const val ARG_ID = "id"
    }
}
