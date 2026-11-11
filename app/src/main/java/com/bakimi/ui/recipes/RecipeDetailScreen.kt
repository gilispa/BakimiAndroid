package com.bakimi.ui.recipes

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.recipes.PhotoCredit
import com.bakimi.domain.recipes.IngredientStatus
import com.bakimi.domain.recipes.RecipeEvaluation
import com.bakimi.domain.recipes.EvaluatedIngredient
import com.bakimi.domain.recipes.Step
import com.bakimi.ui.recipes.RecipeDetailViewModel.Companion.MAX_SERVINGS
import com.bakimi.ui.recipes.RecipeDetailViewModel.Companion.MIN_SERVINGS
import com.bakimi.ui.fridge.formatNumber
import com.bakimi.ui.fridge.quantityLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onStartCooking: (recipeId: String, servings: Int) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    (state as? DetailUiState.Ready)?.let { Text(it.evaluation.recipe.name, maxLines = 1) }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.form_back))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                DetailUiState.NotFound -> Text(
                    stringResource(R.string.detail_not_found),
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )

                is DetailUiState.Ready -> DetailContent(
                    evaluation = current.evaluation,
                    servingsInWeek = current.servingsInWeek,
                    onChangeServings = viewModel::changeServings,
                    onSaveToWeek = viewModel::saveToWeek,
                    onRemoveFromWeek = viewModel::removeFromWeek,
                    onStartCooking = {
                        onStartCooking(current.evaluation.recipe.id, current.evaluation.servings)
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    evaluation: RecipeEvaluation,
    servingsInWeek: Int?,
    onChangeServings: (Int) -> Unit,
    onSaveToWeek: () -> Unit,
    onRemoveFromWeek: () -> Unit,
    onStartCooking: () -> Unit,
) {
    val recipe = evaluation.recipe
    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            RecipeImage(recipe.id, Modifier.fillMaxWidth().aspectRatio(3f / 2f))
            PhotoCreditLink(recipe.photoCredit)
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(recipe.description, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    InfoItem(
                        title = null,
                        icon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)) },
                        value = stringResource(R.string.recipe_minutes_accessible, recipe.minutes),
                    )
                    InfoItem(
                        title = stringResource(R.string.detail_protein),
                        icon = null,
                        value = recipe.proteinPerServingG
                            ?.let { stringResource(R.string.detail_protein_value, formatNumber(it)) }
                            ?: stringResource(R.string.detail_no_data),
                    )
                }
                ServingsPicker(evaluation.servings, onChangeServings)
                AvailabilityTag(evaluation)
                // Starting or leaving Cooking Mode deducts nothing; only the final confirmation does.
                Button(onClick = onStartCooking, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Outlined.Restaurant, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_start_cooking))
                }
                WeekAction(
                    servings = evaluation.servings,
                    servingsInWeek = servingsInWeek,
                    onSave = onSaveToWeek,
                    onRemove = onRemoveFromWeek,
                )
            }
        }
        item { SectionTitle(R.string.detail_ingredients) }
        itemsIndexed(evaluation.ingredients, key = { _, it -> it.required.ingredient.id }) { index, ingredient ->
            IngredientRow(ingredient)
            if (index < evaluation.ingredients.lastIndex) HorizontalDivider(Modifier.padding(start = 56.dp))
        }
        item { SectionTitle(R.string.detail_preparation) }
        itemsIndexed(recipe.steps) { index, step -> StepRow(index + 1, step) }
    }
}

@Composable
private fun PhotoCreditLink(credit: PhotoCredit) {
    val uri = LocalUriHandler.current
    Text(
        stringResource(R.string.recipe_photo_credit, credit.author),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .clickable(role = Role.Button) { uri.openUri(credit.photoUrl) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun InfoItem(title: String?, icon: (@Composable () -> Unit)?, value: String) {
    Column {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(4.dp))
            }
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ServingsPicker(servings: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.detail_servings), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onChange(-1) }, enabled = servings > MIN_SERVINGS) {
            Icon(Icons.Default.Remove, stringResource(R.string.detail_fewer_servings))
        }
        Text(
            stringResource(R.string.detail_servings_value, servings),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(min = 96.dp).padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
        FilledTonalIconButton(onClick = { onChange(1) }, enabled = servings < MAX_SERVINGS) {
            Icon(Icons.Default.Add, stringResource(R.string.detail_more_servings))
        }
    }
}

@Composable
private fun WeekAction(servings: Int, servingsInWeek: Int?, onSave: () -> Unit, onRemove: () -> Unit) {
    if (servingsInWeek == null) {
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.detail_add_to_week))
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.detail_in_week, servingsInWeek), style = MaterialTheme.typography.bodyMedium)
        }
        if (servings != servingsInWeek) {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.detail_update_week, servings))
            }
        }
        OutlinedButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.detail_remove_from_week))
        }
    }
}

@Composable
private fun SectionTitle(@StringRes text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@Composable
private fun IngredientRow(item: EvaluatedIngredient) {
    val required = item.required
    val (icon, color, state) = when (item.state) {
        IngredientStatus.COVERED -> Triple(
            Icons.Filled.CheckCircle,
            MaterialTheme.colorScheme.onSurface,
            if (item.approximate) {
                stringResource(R.string.detail_have_enough_approx, quantityLabel(item.available, required.unit))
            } else {
                stringResource(R.string.detail_have_enough)
            },
        )
        IngredientStatus.INSUFFICIENT -> Triple(
            Icons.Outlined.RemoveCircleOutline,
            MaterialTheme.colorScheme.error,
            stringResource(
                if (item.approximate) R.string.detail_missing_approx else R.string.detail_missing,
                quantityLabel(item.shortage, required.unit),
            ),
        )
        IngredientStatus.MISSING -> Triple(
            Icons.Outlined.Cancel,
            MaterialTheme.colorScheme.error,
            stringResource(R.string.detail_not_in_fridge),
        )
        IngredientStatus.OTHER_UNIT -> Triple(
            Icons.AutoMirrored.Outlined.HelpOutline,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.detail_other_unit),
        )
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(required.ingredient.name, style = MaterialTheme.typography.bodyLarge)
            Text(state, style = MaterialTheme.typography.bodyMedium, color = color)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(quantityLabel(required.quantity, required.unit), style = MaterialTheme.typography.bodyLarge)
            required.note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, step: Step) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(step.instruction, style = MaterialTheme.typography.bodyLarge)
            step.minutes?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.detail_step_minutes, it),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
