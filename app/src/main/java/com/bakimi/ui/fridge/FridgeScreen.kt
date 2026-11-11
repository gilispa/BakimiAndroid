package com.bakimi.ui.fridge

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: FridgeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                FridgeEvent.OperationError ->
                    snackbarHostState.showSnackbar(context.getString(R.string.fridge_operation_error))

                is FridgeEvent.Deleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.fridge_deleted, event.item.name),
                        actionLabel = context.getString(R.string.fridge_undo),
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.item)
                    }
                }
            }
        }
    }

    Scaffold(
        // The root Scaffold already applies the system bars.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_fridge)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            val groups = (state as? FridgeUiState.Ready)?.groups
            if (!groups.isNullOrEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAdd,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.fridge_add)) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val current = state) {
            FridgeUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is FridgeUiState.Ready ->
                if (current.groups.isEmpty()) {
                    EmptyFridge(onAdd = onAdd, modifier = Modifier.padding(padding))
                } else {
                    InventoryList(
                        groups = current.groups,
                        contentPadding = padding,
                        onEdit = onEdit,
                        onIncrease = viewModel::increase,
                        onDecrease = viewModel::decrease,
                        onMarkDepleted = viewModel::markDepleted,
                        onDelete = viewModel::delete,
                    )
                }
        }
    }
}

@Composable
private fun EmptyFridge(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.Kitchen,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.fridge_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.fridge_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.fridge_empty_action))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InventoryList(
    groups: Map<StorageLocation, List<InventoryItem>>,
    contentPadding: PaddingValues,
    onEdit: (Long) -> Unit,
    onIncrease: (Long) -> Unit,
    onDecrease: (Long) -> Unit,
    onMarkDepleted: (Long) -> Unit,
    onDelete: (InventoryItem) -> Unit,
) {
    val today = remember { LocalDate.now() }
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            // Space so the floating button does not cover the last food.
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        groups.forEach { (location, items) ->
            stickyHeader(key = location.name) {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            R.string.fridge_group_header,
                            stringResource(location.label),
                            items.size,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics { heading() },
                    )
                }
            }
            items(items, key = { it.id }) { item ->
                InventoryRow(
                    item = item,
                    today = today,
                    onEdit = { onEdit(item.id) },
                    onIncrease = { onIncrease(item.id) },
                    onDecrease = { onDecrease(item.id) },
                    onMarkDepleted = { onMarkDepleted(item.id) },
                    onDelete = { onDelete(item) },
                    modifier = Modifier.animateItem(),
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    today: LocalDate,
    onEdit: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onMarkDepleted: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = quantityLabel(item.unit.quickStep, item.unit)
    ListItem(
        modifier = modifier.clickable(onClick = onEdit),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        headlineContent = {
            Text(item.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column {
                if (item.depleted) {
                    InlineLabel(
                        icon = { Icon(Icons.Outlined.RemoveShoppingCart, null, Modifier.size(16.dp)) },
                        text = stringResource(R.string.fridge_depleted),
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(quantityLabel(item.quantity, item.unit))
                }
                item.expiry?.let { date ->
                    val expired = date.isBefore(today)
                    InlineLabel(
                        icon = {
                            Icon(
                                if (expired) Icons.Outlined.ReportProblem else Icons.Outlined.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        text = stringResource(
                            if (expired) R.string.fridge_expired else R.string.fridge_expires,
                            formatDate(date),
                        ),
                        color = if (expired) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, enabled = !item.depleted) {
                    Icon(Icons.Default.Remove, stringResource(R.string.fridge_decrease, step, item.name))
                }
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, stringResource(R.string.fridge_increase, step, item.name))
                }
                ItemMenu(
                    name = item.name,
                    depleted = item.depleted,
                    onEdit = onEdit,
                    onMarkDepleted = onMarkDepleted,
                    onDelete = onDelete,
                )
            }
        },
    )
}

@Composable
private fun InlineLabel(icon: @Composable () -> Unit, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CompositionLocalProvider(LocalContentColor provides color) {
            icon()
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ItemMenu(
    name: String,
    depleted: Boolean,
    onEdit: () -> Unit,
    onMarkDepleted: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.fridge_more_options, name))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.fridge_edit)) },
                onClick = { expanded = false; onEdit() },
            )
            if (!depleted) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.fridge_mark_depleted)) },
                    onClick = { expanded = false; onMarkDepleted() },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.fridge_remove)) },
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}
