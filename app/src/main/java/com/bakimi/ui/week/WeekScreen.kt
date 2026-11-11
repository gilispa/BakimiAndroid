package com.bakimi.ui.week

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.shopping.BudgetStatus
import com.bakimi.domain.shopping.ShoppingManager.Companion.MAX_SERVINGS
import com.bakimi.domain.shopping.ShoppingManager.Companion.MIN_SERVINGS
import com.bakimi.domain.shopping.PlannedRecipe
import com.bakimi.domain.shopping.Recommendation
import com.bakimi.domain.shopping.Week
import com.bakimi.domain.shopping.RecipeSuggestion
import com.bakimi.ui.shopping.formatPrice
import com.bakimi.ui.shopping.totalLabel
import com.bakimi.ui.recipes.AvailabilityTag
import com.bakimi.ui.recipes.RecipeImage
import com.bakimi.ui.fridge.editableText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    onOpenRecipe: (String) -> Unit,
    onViewList: () -> Unit,
    viewModel: WeekViewModel = hiltViewModel(),
) {
    val week by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.errors.collect { snackbar.showSnackbar(context.getString(R.string.shopping_error)) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_week)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            week?.takeIf { it.recipes.isNotEmpty() }?.let { SummaryBar(it, onViewList) }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val current = week
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            WeekContent(current, padding, viewModel, onOpenRecipe)
        }
    }
}

@Composable
private fun WeekContent(
    week: Week,
    padding: PaddingValues,
    viewModel: WeekViewModel,
    onOpenRecipe: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.week_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item { PlanSettingsCard(week, viewModel) }

        title(R.string.week_your_week)
        if (week.recipes.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.week_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            items(week.recipes, key = { "plan_" + it.recipe.id }) { planned ->
                PlannedRow(
                    planned = planned,
                    onOpen = { onOpenRecipe(planned.recipe.id) },
                    onServings = { viewModel.changeServings(planned.recipe.id, it) },
                    onRemove = { viewModel.remove(planned.recipe.id) },
                )
            }
        }

        if (week.recommendations.isNotEmpty()) {
            title(R.string.week_use_leftovers)
            items(week.recommendations, key = { "rec_" + it.recipe.id }) { recommendation ->
                RecommendationCard(
                    recommendation,
                    onOpen = { onOpenRecipe(recommendation.recipe.id) },
                    onAdd = { viewModel.add(recommendation.recipe.id) },
                )
            }
        }

        if (week.suggestions.isNotEmpty()) {
            title(R.string.week_add_meals)
            items(week.suggestions, key = { "sug_" + it.evaluation.recipe.id }) { suggestion ->
                SuggestionCard(
                    suggestion,
                    onOpen = { onOpenRecipe(suggestion.evaluation.recipe.id) },
                    onAdd = { viewModel.add(suggestion.evaluation.recipe.id) },
                )
            }
        }
    }
}

private fun LazyListScope.title(text: Int) {
    item(key = "titulo_$text") {
        Text(
            stringResource(text),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .semantics { heading() },
        )
    }
}

@Composable
private fun PlanSettingsCard(week: Week, viewModel: WeekViewModel) {
    // The field keeps what the user types even if it is not a valid number yet.
    var budget by rememberSaveable { mutableStateOf(week.config.budget?.let(::editableText).orEmpty()) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Stepper(
                label = stringResource(R.string.week_people),
                value = week.config.people,
                decreaseDescription = stringResource(R.string.week_fewer_people),
                increaseDescription = stringResource(R.string.week_more_people),
                onChange = viewModel::changePeople,
            )
            OutlinedTextField(
                value = budget,
                onValueChange = {
                    budget = it
                    viewModel.changeBudget(it)
                },
                label = { Text(stringResource(R.string.week_budget)) },
                prefix = { Text("$") },
                supportingText = { Text(stringResource(R.string.week_budget_help)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: Int,
    decreaseDescription: String,
    increaseDescription: String,
    onChange: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange(-1) }, enabled = value > MIN_SERVINGS) {
            Icon(Icons.Default.Remove, decreaseDescription)
        }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 24.dp))
        IconButton(onClick = { onChange(1) }, enabled = value < MAX_SERVINGS) {
            Icon(Icons.Default.Add, increaseDescription)
        }
    }
}

@Composable
private fun PlannedRow(
    planned: PlannedRecipe,
    onOpen: () -> Unit,
    onServings: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    val name = planned.recipe.name
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            planned.recipe.id,
            Modifier.size(56.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onOpen),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).clickable(onClick = onOpen)) {
            Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.detail_servings_value, planned.servings),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onServings(planned.servings - 1) }, enabled = planned.servings > MIN_SERVINGS) {
            Icon(Icons.Default.Remove, stringResource(R.string.week_fewer_servings, name))
        }
        IconButton(onClick = { onServings(planned.servings + 1) }, enabled = planned.servings < MAX_SERVINGS) {
            Icon(Icons.Default.Add, stringResource(R.string.week_more_servings, name))
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, stringResource(R.string.week_remove, name))
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: Recommendation, onOpen: () -> Unit, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RecipeImage(
                recommendation.recipe.id,
                Modifier.size(56.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onOpen),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).clickable(onClick = onOpen)) {
                Text(recommendation.recipe.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(
                        R.string.week_uses,
                        recommendation.shared.joinToString(", ") { it.name.lowercase() },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            FilledTonalButton(onClick = onAdd) { Text(stringResource(R.string.week_add)) }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: RecipeSuggestion, onOpen: () -> Unit, onAdd: () -> Unit) {
    val evaluation = suggestion.evaluation
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(12.dp)) {
            RecipeImage(
                evaluation.recipe.id,
                Modifier.size(88.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onOpen),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(Modifier.clickable(onClick = onOpen)) {
                    Text(evaluation.recipe.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.week_time_servings, evaluation.recipe.minutes, evaluation.servings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AvailabilityTag(evaluation)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        missingPurchaseLabel(suggestion),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    FilledTonalButton(onClick = onAdd) { Text(stringResource(R.string.week_add)) }
                }
            }
        }
    }
}

/** The card shows what buying that recipe's missing ingredients would cost with the current inventory. */
@Composable
private fun missingPurchaseLabel(suggestion: RecipeSuggestion): String {
    val total = suggestion.missingPurchase
    return when {
        suggestion.evaluation.isReady -> stringResource(R.string.week_nothing_to_buy)
        total.total == 0.0 && total.partial -> stringResource(R.string.price_unknown)
        else -> stringResource(R.string.week_missing_purchase, totalLabel(total))
    }
}

@Composable
private fun SummaryBar(week: Week, onViewList: () -> Unit) {
    val total = week.shoppingList.total
    val budget = week.config.budget
    val meals = pluralStringResource(R.plurals.week_meals, week.recipes.size, week.recipes.size)
    val purchase = if (budget == null) {
        stringResource(R.string.week_purchase_estimate, totalLabel(total))
    } else {
        stringResource(
            R.string.week_purchase_vs_budget,
            totalLabel(total),
            stringResource(R.string.price, formatPrice(budget)),
        )
    }
    val (aviso, colorAviso) = when (week.budgetStatus) {
        BudgetStatus.EXCEEDED -> stringResource(R.string.week_budget_exceeded) to MaterialTheme.colorScheme.error
        BudgetStatus.UNCERTAIN -> stringResource(R.string.week_budget_uncertain) to MaterialTheme.colorScheme.onSurfaceVariant
        BudgetStatus.WITHIN -> stringResource(R.string.week_budget_within) to MaterialTheme.colorScheme.onSurfaceVariant
        BudgetStatus.NO_BUDGET ->
            (if (total.partial) stringResource(R.string.week_missing_prices) else null) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$meals · $purchase", style = MaterialTheme.typography.titleSmall)
            aviso?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colorAviso) }
            Button(onClick = onViewList, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.week_view_list))
            }
        }
    }
}
