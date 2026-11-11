package com.bakimi.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.recipes.RecipeEvaluation
import com.bakimi.domain.recipes.HomeSections
import com.bakimi.ui.recipes.AvailabilityTag
import com.bakimi.ui.recipes.RecipeImage
import com.bakimi.ui.recipes.availabilityLabel

@Composable
fun HomeScreen(
    onOpenRecipe: (String) -> Unit,
    onGoToFridge: () -> Unit,
    onPlan: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        HomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is HomeUiState.Ready -> HomeContent(
            sections = current.sections,
            fridgeEmpty = current.fridgeEmpty,
            onOpenRecipe = onOpenRecipe,
            onGoToFridge = onGoToFridge,
            onPlan = onPlan,
        )
    }
}

@Composable
private fun HomeContent(
    sections: HomeSections,
    fridgeEmpty: Boolean,
    onOpenRecipe: (String) -> Unit,
    onGoToFridge: () -> Unit,
    onPlan: () -> Unit,
) {
    // The carousel takes about half of the screen and leaves the content below visible.
    val carouselHeight = (LocalConfiguration.current.screenHeightDp * 0.42f).dp

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_bakimi_horizontal),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.height(40.dp).semantics { heading() },
                )
                Text(
                    stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            FeaturedCarousel(sections.featured, height = carouselHeight, onOpenRecipe = onOpenRecipe)
        }
        item {
            Button(onClick = onPlan, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_plan))
            }
        }
        if (fridgeEmpty) {
            item { FridgeInvitation(onGoToFridge) }
        }
        section(R.string.home_ready_to_cook, sections.readyToCook, onOpenRecipe)
        section(R.string.home_almost_ready, sections.almostReady, onOpenRecipe)
        section(R.string.home_quick, sections.quick, onOpenRecipe)
        section(R.string.home_more_ideas, sections.moreIdeas, onOpenRecipe)
    }
}

private fun LazyListScope.section(
    @StringRes title: Int,
    recipes: List<RecipeEvaluation>,
    onOpenRecipe: (String) -> Unit,
) {
    if (recipes.isEmpty()) return
    item(key = title) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() },
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(recipes, key = { it.recipe.id }) { evaluation ->
                    RecipeCard(evaluation, onClick = { onOpenRecipe(evaluation.recipe.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedCarousel(
    recipes: List<RecipeEvaluation>,
    height: Dp,
    onOpenRecipe: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.home_featured),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() },
        )
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { recipes.size },
            preferredItemWidth = 300.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth().height(height),
        ) { index ->
            val evaluation = recipes[index]
            val description = "${evaluation.recipe.name}. ${availabilityLabel(evaluation)}. " +
                stringResource(R.string.recipe_minutes_accessible, evaluation.recipe.minutes)
            Box(
                Modifier
                    .fillMaxSize()
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable(role = Role.Button) { onOpenRecipe(evaluation.recipe.id) }
                    .semantics(mergeDescendants = true) { contentDescription = description },
            ) {
                RecipeImage(evaluation.recipe.id, Modifier.fillMaxSize())
                // Gradient so white text is readable over any photo.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.45f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.75f),
                            ),
                        ),
                )
                Column(
                    Modifier.align(Alignment.BottomStart).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AvailabilityTag(evaluation)
                    Text(
                        evaluation.recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CookTime(evaluation.recipe.minutes, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(evaluation: RecipeEvaluation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        RecipeImage(evaluation.recipe.id, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                evaluation.recipe.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2,
            )
            CookTime(evaluation.recipe.minutes, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AvailabilityTag(evaluation)
        }
    }
}

@Composable
private fun CookTime(minutes: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.recipe_minutes, minutes),
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Composable
private fun FridgeInvitation(onGoToFridge: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.home_empty_fridge_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.home_empty_fridge_message), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onGoToFridge) { Text(stringResource(R.string.home_empty_fridge_action)) }
            }
        }
    }
}
