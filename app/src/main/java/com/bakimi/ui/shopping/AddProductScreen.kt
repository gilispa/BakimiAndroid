package com.bakimi.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.ui.fridge.IngredientSearch
import com.bakimi.ui.fridge.ChoiceChip
import com.bakimi.ui.fridge.FormSection
import com.bakimi.ui.fridge.SelectedItemCard
import com.bakimi.ui.fridge.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddProductScreen(
    onFinish: () -> Unit,
    viewModel: AddProductViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onFinish() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_add_title)) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.form_back))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            val selection = state.selection
            if (selection == null) {
                IngredientSearch(
                    searchText = state.searchText,
                    results = state.results,
                    offerOther = state.offerOther,
                    error = state.nameError,
                    onSearch = viewModel::changeSearch,
                    onChoose = viewModel::choose,
                    onOther = viewModel::chooseOther,
                )
                return@Column
            }
            SelectedItemCard(selection, onChange = viewModel::changeSelection)

            OutlinedTextField(
                value = state.quantityText,
                onValueChange = viewModel::changeQuantity,
                label = { Text(stringResource(R.string.form_quantity)) },
                isError = state.quantityError,
                supportingText = if (state.quantityError) {
                    { Text(stringResource(R.string.form_quantity_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            FormSection(title = stringResource(R.string.form_unit)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeasureUnit.entries.forEach { unit ->
                        ChoiceChip(
                            text = stringResource(unit.label),
                            selected = state.unit == unit,
                            onClick = { viewModel.changeUnit(unit) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.priceText,
                onValueChange = viewModel::changePrice,
                label = { Text(stringResource(R.string.shopping_price_optional)) },
                prefix = { Text("$") },
                isError = state.priceError,
                supportingText = { Text(stringResource(R.string.shopping_price_optional_help)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.saveFailed) {
                Text(
                    stringResource(R.string.shopping_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(stringResource(R.string.form_save)) }
        }
    }
}
