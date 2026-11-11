package com.bakimi.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.shopping.ManualItem
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.IngredientRepository
import com.bakimi.domain.ingredients.searchIngredients
import com.bakimi.domain.ingredients.buscarPorNombre
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.parseQuantity
import com.bakimi.ui.fridge.Selection
import com.bakimi.ui.fridge.editableText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddProductUiState(
    val selection: Selection? = null,
    val searchText: String = "",
    val results: List<Ingredient> = emptyList(),
    val offerOther: Boolean = false,
    val quantityText: String = "1",
    val unit: MeasureUnit = MeasureUnit.PIECE,
    val priceText: String = "",
    val nameError: Boolean = false,
    val quantityError: Boolean = false,
    val priceError: Boolean = false,
    val saveFailed: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val manager: ShoppingManager,
    ingredients: IngredientRepository,
) : ViewModel() {

    private val catalog = ingredients.all()

    private val _state = MutableStateFlow(AddProductUiState())
    val state: StateFlow<AddProductUiState> = _state.asStateFlow()

    fun changeSearch(text: String) = _state.update {
        it.copy(
            searchText = text,
            results = searchIngredients(text, catalog),
            offerOther = text.isNotBlank() && catalog.buscarPorNombre(text) == null,
            nameError = false,
        )
    }

    /** Suggests the usual purchase package (a liter of milk, a bag of rice). */
    fun choose(ingredient: Ingredient) = _state.update {
        val purchase = ingredient.purchase
        it.copy(
            selection = Selection.FromCatalog(ingredient),
            unit = purchase?.unit ?: ingredient.suggestedUnit,
            quantityText = editableText(purchase?.packageSize ?: 1.0),
            searchText = "",
            results = emptyList(),
            offerOther = false,
        )
    }

    fun chooseOther() = _state.update {
        val name = it.searchText.trim()
        if (name.isEmpty()) it else it.copy(selection = Selection.Other(name), results = emptyList(), offerOther = false)
    }

    fun changeSelection() {
        val name = _state.value.selection?.name.orEmpty()
        _state.update { it.copy(selection = null) }
        changeSearch(name)
    }

    fun changeQuantity(text: String) = _state.update { it.copy(quantityText = text, quantityError = false) }

    fun changeUnit(unit: MeasureUnit) = _state.update { it.copy(unit = unit) }

    fun changePrice(text: String) = _state.update { it.copy(priceText = text, priceError = false) }

    fun save() {
        val current = _state.value
        if (current.saving) return
        val selection = current.selection
        val quantity = parseQuantity(current.quantityText)?.takeIf { it > 0.0 }
        val price = current.priceText.takeIf { it.isNotBlank() }?.let(::parseQuantity)
        val priceError = current.priceText.isNotBlank() && price == null
        if (selection == null || quantity == null || priceError) {
            _state.update {
                it.copy(nameError = selection == null, quantityError = quantity == null, priceError = priceError)
            }
            return
        }

        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                manager.addManual(
                    ManualItem(
                        ingredientId = (selection as? Selection.FromCatalog)?.ingredient?.id,
                        name = selection.name,
                        quantity = quantity,
                        unit = current.unit,
                        price = price,
                    ),
                )
                _state.update { it.copy(saving = false, saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, saveFailed = true) }
            }
        }
    }
}
