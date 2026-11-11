package com.bakimi.ui.fridge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.ItemError
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    onFinish: () -> Unit,
    viewModel: ItemFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onFinish()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.form_title_edit else R.string.form_title_add,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.form_back))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ItemForm(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemForm(
    state: ItemFormUiState,
    viewModel: ItemFormViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
                error = ItemError.EMPTY_NAME in state.errors,
                onSearch = viewModel::changeSearch,
                onChoose = viewModel::choose,
                onOther = viewModel::chooseOther,
            )
            return@Column
        }
        SelectedItemCard(selection, onChange = viewModel::changeSelection)

        val quantityError = ItemError.INVALID_QUANTITY in state.errors
        OutlinedTextField(
            value = state.quantityText,
            onValueChange = viewModel::changeQuantity,
            label = { Text(stringResource(R.string.form_quantity)) },
            isError = quantityError,
            supportingText = {
                Text(
                    stringResource(
                        if (quantityError) R.string.form_quantity_error else R.string.form_quantity_help,
                    ),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
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

        FormSection(title = stringResource(R.string.form_location)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StorageLocation.entries.forEach { location ->
                    ChoiceChip(
                        text = stringResource(location.label),
                        selected = state.location == location,
                        onClick = { viewModel.changeLocation(location) },
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.form_expiry)) {
            ExpiryPicker(date = state.expiry, onChange = viewModel::changeExpiry)
        }

        if (state.saveFailed) {
            Text(
                stringResource(R.string.form_save_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = viewModel::save,
            enabled = !state.saving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(R.string.form_save))
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Ingredient catalog search, with the option to add an "other" food. */
@Composable
internal fun IngredientSearch(
    searchText: String,
    results: List<Ingredient>,
    offerOther: Boolean,
    error: Boolean,
    onSearch: (String) -> Unit,
    onChoose: (Ingredient) -> Unit,
    onOther: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    // When the form opens, the keyboard is ready to search.
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearch,
            label = { Text(stringResource(R.string.form_search)) },
            placeholder = { Text(stringResource(R.string.form_search_example)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            isError = error,
            supportingText = {
                Text(stringResource(if (error) R.string.form_name_error else R.string.form_search_help))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )

        results.forEach { ingredient ->
            ListItem(
                headlineContent = { Text(ingredient.name) },
                supportingContent = { Text(stringResource(ingredient.category.label)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.clickable { onChoose(ingredient) },
            )
        }
        if (offerOther) {
            ListItem(
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                headlineContent = { Text(stringResource(R.string.form_add_other, searchText.trim())) },
                supportingContent = { Text(stringResource(R.string.form_other_explanation)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.clickable(onClick = onOther),
            )
        }
    }
}

@Composable
internal fun SelectedItemCard(selection: Selection, onChange: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(selection.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    when (selection) {
                        is Selection.FromCatalog -> stringResource(selection.ingredient.category.label)
                        is Selection.Other -> stringResource(R.string.form_outside_catalog)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onChange) { Text(stringResource(R.string.form_change)) }
        }
    }
}

@Composable
internal fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Composable
internal fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        // The check mark shows the selection in addition to color.
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpiryPicker(date: LocalDate?, onChange: (LocalDate?) -> Unit) {
    var dialogOpen by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { dialogOpen = true }) {
            Icon(Icons.Outlined.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(date?.let(::formatDate) ?: stringResource(R.string.form_expiry_add))
        }
        if (date != null) {
            IconButton(onClick = { onChange(null) }) {
                Icon(Icons.Default.Close, stringResource(R.string.form_expiry_remove))
            }
        }
    }

    if (dialogOpen) {
        // DatePicker works in UTC milliseconds; converting in UTC avoids shifting the day.
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        dialogOpen = false
                    },
                ) { Text(stringResource(R.string.form_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) {
                    Text(stringResource(R.string.form_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
