package com.bakimi.ui.cooking

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.ui.fridge.label
import com.bakimi.ui.fridge.quantityLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    onExit: () -> Unit,
    viewModel: CookingModeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.detail_not_found))
        }
        return
    }

    // With busy hands, the screen must not turn off while cooking.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(current.recipe.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onExit) { Icon(Icons.Default.Close, stringResource(R.string.cooking_exit)) }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (current.phase == CookingPhase.COOKING) {
                StepNavigation(
                    canGoBack = current.step > 0,
                    isLast = current.isLastStep,
                    onPrevious = viewModel::previous,
                    onNext = viewModel::next,
                    onFinish = viewModel::finishCooking,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (current.phase) {
                CookingPhase.COOKING -> CurrentStep(current)
                CookingPhase.CONFIRMING -> ConfirmationContent(current, viewModel, onExit)
                CookingPhase.FINISHED -> DoneContent(current.alreadyRegistered, onExit)
            }
        }
    }
}

@Composable
private fun CurrentStep(state: CookingModeUiState) {
    val step = state.recipe.steps[state.step]
    var showIngredients by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.cooking_progress, state.step + 1, state.totalSteps),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Announced when the step changes, for screen reader users.
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        LinearProgressIndicator(
            progress = { (state.step + 1f) / state.totalSteps },
            modifier = Modifier.fillMaxWidth(),
        )
        step.image?.let { StepImage(it, number = state.step + 1) }
        Text(
            step.instruction,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        step.minutes?.let { minutes ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.detail_step_minutes, minutes), style = MaterialTheme.typography.titleMedium)
            }
        }
        TextButton(onClick = { showIngredients = true }) { Text(stringResource(R.string.cooking_show_ingredients)) }
    }

    if (showIngredients) {
        val factor = state.servings.toDouble() / state.recipe.baseServings
        AlertDialog(
            onDismissRequest = { showIngredients = false },
            title = { Text(stringResource(R.string.cooking_ingredients_title, state.servings)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.recipe.ingredients.forEach {
                        Text("${it.ingredient.name}: ${quantityLabel(it.quantity * factor, it.unit)}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIngredients = false }) { Text(stringResource(R.string.cooking_close)) }
            },
        )
    }
}

private sealed interface ImageLoad {
    data object Loading : ImageLoad
    data class ShoppingItems(val image: ImageBitmap) : ImageLoad
    data object Unavailable : ImageLoad
}

/**
 * Reference image for the step, read from assets off the main thread.
 *
 * Images and videos are optional: if the file does not exist or cannot be read, the step is shown
 * with its text only, just like a step without an image.
 */
@Composable
private fun StepImage(path: String, number: Int) {
    val context = LocalContext.current
    val load by produceState<ImageLoad>(initialValue = ImageLoad.Loading, path) {
        value = withContext(Dispatchers.IO) {
            runCatching { context.assets.open(path).use { BitmapFactory.decodeStream(it) } }
                .getOrNull()
                ?.let { ImageLoad.ShoppingItems(it.asImageBitmap()) }
                ?: ImageLoad.Unavailable
        }
    }
    val imageModifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(MaterialTheme.shapes.large)
    when (val current = load) {
        // Reserves the space while loading so the text does not jump.
        ImageLoad.Loading -> Surface(imageModifier, color = MaterialTheme.colorScheme.surfaceVariant) {}
        is ImageLoad.ShoppingItems -> Image(
            bitmap = current.image,
            contentDescription = stringResource(R.string.cooking_step_image, number),
            contentScale = ContentScale.Crop,
            modifier = imageModifier,
        )
        ImageLoad.Unavailable -> Unit
    }
}

@Composable
private fun StepNavigation(
    canGoBack: Boolean,
    isLast: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onPrevious, enabled = canGoBack, modifier = Modifier.weight(1f).height(56.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.cooking_previous))
            }
            if (isLast) {
                Button(onClick = onFinish, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.cooking_finished), textAlign = TextAlign.Center)
                }
            } else {
                Button(onClick = onNext, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.cooking_next))
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun ConfirmationContent(state: CookingModeUiState, viewModel: CookingModeViewModel, onExit: () -> Unit) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.cooking_confirm_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.cooking_confirm_help), style = MaterialTheme.typography.bodyMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cooking_prepared_servings),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { viewModel.changePreparedServings(-1) },
                enabled = state.preparedServings > ShoppingManager.MIN_SERVINGS,
            ) { Icon(Icons.Default.Remove, stringResource(R.string.detail_fewer_servings)) }
            Text(
                state.preparedServings.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 24.dp),
            )
            IconButton(
                onClick = { viewModel.changePreparedServings(1) },
                enabled = state.preparedServings < ShoppingManager.MAX_SERVINGS,
            ) { Icon(Icons.Default.Add, stringResource(R.string.detail_more_servings)) }
        }

        state.usages.forEachIndexed { index, usage ->
            OutlinedTextField(
                value = usage.text,
                onValueChange = { viewModel.changeUsage(index, it) },
                label = {
                    Text(
                        stringResource(
                            R.string.cooking_quantity_used,
                            usage.used.ingredient.name,
                            stringResource(usage.used.unit.label),
                        ),
                    )
                },
                isError = usage.quantity == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ConsumptionPreview(state)

        if (state.error) {
            Text(stringResource(R.string.cooking_error), color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = viewModel::confirm,
            enabled = !state.saving && state.usagesValid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(stringResource(R.string.cooking_deduct)) }
        OutlinedButton(onClick = viewModel::backToSteps, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cooking_back_to_steps))
        }
        TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cooking_exit_without_deducting))
        }
    }
}

/** Preview of how Mi refri will look, including discrepancies, before confirming. */
@Composable
private fun ConsumptionPreview(state: CookingModeUiState) {
    val consumption = state.consumption ?: return
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.cooking_fridge_preview), style = MaterialTheme.typography.titleSmall)
            if (consumption.changes.isEmpty()) {
                Text(stringResource(R.string.cooking_no_changes), style = MaterialTheme.typography.bodyMedium)
            }
            consumption.changes.forEach { after ->
                val before = state.before[after.id]
                Text(
                    stringResource(
                        R.string.cooking_change,
                        after.name,
                        before?.let { quantityLabel(it.quantity, it.unit) }.orEmpty(),
                        quantityLabel(after.quantity, after.unit),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            consumption.discrepancies.forEach { discrepancy ->
                val inFridge = consumption.changes.any { it.ingredientId == discrepancy.ingredient.id }
                val quantity = quantityLabel(discrepancy.unrecorded, discrepancy.unit)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (inFridge) {
                            stringResource(R.string.cooking_used_more, discrepancy.ingredient.name.lowercase(), quantity)
                        } else {
                            stringResource(R.string.cooking_not_in_fridge, "${discrepancy.ingredient.name} ($quantity)")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (consumption.discrepancies.isNotEmpty()) {
                Text(
                    stringResource(R.string.cooking_fix_later),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (consumption.approximate) {
                Text(
                    stringResource(R.string.cooking_approximate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DoneContent(alreadyRegistered: Boolean, onExit: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.cooking_done_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (alreadyRegistered) R.string.cooking_already_recorded else R.string.cooking_done_message),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExit) { Text(stringResource(R.string.cooking_back_to_recipe)) }
    }
}
