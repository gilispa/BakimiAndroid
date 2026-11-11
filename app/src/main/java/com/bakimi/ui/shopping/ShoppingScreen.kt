package com.bakimi.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.data.ingredients.REFERENCE_PRICES_DATE
import com.bakimi.domain.shopping.BudgetStatus
import com.bakimi.domain.shopping.ShoppingLine
import com.bakimi.domain.shopping.Week
import com.bakimi.domain.ingredients.Category
import com.bakimi.domain.inventory.parseQuantity
import com.bakimi.ui.fridge.label
import com.bakimi.ui.fridge.quantityLabel
import com.bakimi.ui.fridge.editableText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    onAddProduct: () -> Unit,
    onPlan: () -> Unit,
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val week by viewModel.state.collectAsStateWithLifecycle()
    val finishing by viewModel.finishing.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var confirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is ShoppingEvent.Added ->
                    context.resources.getQuantityString(R.plurals.shopping_added, event.quantity, event.quantity)
                ShoppingEvent.Error -> context.getString(R.string.shopping_error)
            }
            snackbar.showSnackbar(message)
        }
    }

    val checked = week?.shoppingList?.checked.orEmpty()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_shopping)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            if (checked.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddProduct,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.shopping_add_product)) },
                )
            }
        },
        bottomBar = {
            if (checked.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Button(
                        onClick = { confirm = true },
                        enabled = !finishing,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    ) {
                        Text(stringResource(R.string.shopping_finish, checked.size), textAlign = TextAlign.Center)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val current = week
        when {
            current == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            current.shoppingList.lines.isEmpty() -> EmptyShoppingList(
                planEmpty = current.recipes.isEmpty(),
                onPlan = onPlan,
                modifier = Modifier.padding(padding),
            )
            else -> ShoppingItems(current, padding, viewModel)
        }
    }

    if (confirm && checked.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.shopping_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.shopping_confirm_message))
                    checked.forEach {
                        Text(stringResource(R.string.shopping_confirm_line, it.name, quantityLabel(it.quantity, it.unit)))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.shopping_confirm_pending),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirm = false
                        viewModel.finishShopping()
                    },
                    enabled = !finishing,
                ) { Text(stringResource(R.string.shopping_confirm_action)) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.form_cancel)) }
            },
        )
    }
}

@Composable
private fun EmptyShoppingList(planEmpty: Boolean, onPlan: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(if (planEmpty) R.string.shopping_empty_title else R.string.shopping_covered_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (planEmpty) R.string.shopping_empty_message else R.string.shopping_covered_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (planEmpty) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onPlan) { Text(stringResource(R.string.shopping_plan)) }
        }
    }
}

/** Group order of the list (product definition, section 13); "Otros" last. */
private val GROUP_ORDER: List<Category?> = Category.entries + null

@Composable
private fun ShoppingItems(week: Week, padding: PaddingValues, viewModel: ShoppingViewModel) {
    var editing by remember { mutableStateOf<ShoppingLine?>(null) }
    val recipeNames = week.recipes.associate { it.recipe.id to it.recipe.name }
    // Within each group, pending items first and purchased ones after.
    val groups = week.shoppingList.lines
        .groupBy { it.category }
        .mapValues { (_, lines) -> lines.sortedBy { it.purchased } }

    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 96.dp,
        ),
    ) {
        item { Summary(week) }
        GROUP_ORDER.forEach { category ->
            val lines = groups[category] ?: return@forEach
            item(key = "grupo_${category?.name ?: "otros"}") {
                Text(
                    stringResource(category?.label ?: R.string.shopping_others),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
                        .semantics { heading() },
                )
            }
            items(lines, key = { it.key }) { line ->
                ShoppingRow(
                    line,
                    recipes = line.recipes.mapNotNull(recipeNames::get),
                    onMark = { viewModel.mark(line, it) },
                    onCorrect = { editing = line },
                )
            }
        }
    }

    editing?.let { line ->
        CorrectionDialog(
            line = line,
            onClose = { editing = null },
            onSave = { quantity, price ->
                viewModel.correct(line, quantity, price)
                editing = null
            },
            onDelete = {
                viewModel.delete(line)
                editing = null
            },
        )
    }
}

@Composable
private fun Summary(week: Week) {
    val total = week.shoppingList.total
    val budget = week.config.budget
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (budget == null) {
                    stringResource(R.string.week_purchase_estimate, totalLabel(total))
                } else {
                    stringResource(
                        R.string.week_purchase_vs_budget,
                        totalLabel(total),
                        stringResource(R.string.price, formatPrice(budget)),
                    )
                },
                style = MaterialTheme.typography.titleMedium,
            )
            when (week.budgetStatus) {
                BudgetStatus.EXCEEDED -> Text(
                    stringResource(R.string.week_budget_exceeded),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                BudgetStatus.UNCERTAIN -> Text(
                    stringResource(R.string.week_budget_uncertain),
                    style = MaterialTheme.typography.bodyMedium,
                )
                BudgetStatus.WITHIN -> Text(
                    stringResource(R.string.week_budget_within),
                    style = MaterialTheme.typography.bodyMedium,
                )
                BudgetStatus.NO_BUDGET -> if (total.partial) {
                    Text(stringResource(R.string.week_missing_prices), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                stringResource(R.string.reference_prices, REFERENCE_PRICES_DATE),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ShoppingRow(
    line: ShoppingLine,
    recipes: List<String>,
    onMark: (Boolean) -> Unit,
    onCorrect: () -> Unit,
) {
    val dimmed = if (line.purchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = line.purchased, role = Role.Checkbox, onValueChange = onMark)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The whole row is the control; the checkbox only reflects it.
        Checkbox(checked = line.purchased, onCheckedChange = null, modifier = Modifier.padding(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                line.name,
                style = MaterialTheme.typography.bodyLarge,
                color = dimmed,
                textDecoration = if (line.purchased) TextDecoration.LineThrough else null,
            )
            Text(purchaseQuantityLabel(line), style = MaterialTheme.typography.bodyMedium, color = dimmed)
            val detail = when {
                line.purchased -> stringResource(R.string.shopping_purchased)
                line.manual -> stringResource(R.string.shopping_added_manually)
                recipes.isNotEmpty() -> stringResource(R.string.shopping_for_recipes, recipes.joinToString(", "))
                else -> null
            }
            detail?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            priceLabel(line.price, line.actualPrice),
            style = MaterialTheme.typography.bodyMedium,
            color = dimmed,
        )
        IconButton(onClick = onCorrect) {
            Icon(Icons.Outlined.Edit, stringResource(R.string.shopping_correct, line.name))
        }
    }
}

@Composable
private fun CorrectionDialog(
    line: ShoppingLine,
    onClose: () -> Unit,
    onSave: (quantity: Double?, price: Double?) -> Unit,
    onDelete: () -> Unit,
) {
    var quantity by rememberSaveable { mutableStateOf(editableText(line.quantity)) }
    var price by rememberSaveable {
        mutableStateOf(if (line.actualPrice) line.price?.let(::editableText).orEmpty() else "")
    }
    val validQuantity = parseQuantity(quantity)?.takeIf { it > 0.0 }
    val validPrice = price.isBlank() || parseQuantity(price) != null

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.shopping_correct_title, line.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.shopping_quantity_actual, stringResource(line.unit.label))) },
                    isError = validQuantity == null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.shopping_price_actual)) },
                    prefix = { Text("$") },
                    supportingText = { Text(stringResource(R.string.shopping_price_actual_help)) },
                    isError = !validPrice,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                if (line.manual) {
                    TextButton(onClick = onDelete) { Text(stringResource(R.string.shopping_remove)) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // With no quantity change the package is kept ("1 × bolsa de 1 kg").
                    val unchanged = line.packages != null && validQuantity == line.quantity
                    onSave(
                        if (unchanged) null else validQuantity,
                        price.takeIf { it.isNotBlank() }?.let(::parseQuantity),
                    )
                },
                enabled = validQuantity != null && validPrice,
            ) { Text(stringResource(R.string.shopping_save)) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.form_cancel)) } },
    )
}
