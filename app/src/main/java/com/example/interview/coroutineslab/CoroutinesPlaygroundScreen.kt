package com.example.interview.coroutineslab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ─── Terminal palette ─────────────────────────────────────────────────────────
private val TerminalBg = Color(0xFF0D1117)
private val TerminalGreen = Color(0xFF39D353)
private val TerminalYellow = Color(0xFFF0E68C)
private val TerminalRed = Color(0xFFFF6B6B)
private val TerminalGray = Color(0xFF8B949E)
private val TerminalWhite = Color(0xFFE6EDF3)

@Composable
fun CoroutinesPlaygroundScreen(
    onBack: () -> Unit,
    viewModel: CoroutinesPlaygroundViewModel = hiltViewModel(),
) {
    val selectedProblem by viewModel.selectedProblem.collectAsStateWithLifecycle()
    val output by viewModel.output.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    AnimatedContent(targetState = selectedProblem, label = "screen") { problem ->
        if (problem == null) {
            ProblemListScreen(
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                onProblemSelected = viewModel::selectProblem,
                onBack = onBack,
            )
        } else {
            ProblemDetailScreen(
                problem = problem,
                output = output,
                isRunning = isRunning,
                onRun = viewModel::runProblem,
                onStop = viewModel::stopProblem,
                onClearOutput = viewModel::clearOutput,
                onBack = viewModel::clearSelection,
            )
        }
    }
}

// ─── Problem List ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProblemListScreen(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onProblemSelected: (CoroutineProblem) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Coroutines Playground",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${coroutineProblemCategories.sumOf { it.problems.size }} problems · tap to run",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip(
                    label = "All",
                    emoji = "📚",
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                )
                coroutineProblemCategories.forEach { cat ->
                    CategoryChip(
                        label = cat.name,
                        emoji = cat.emoji,
                        selected = selectedCategory == cat.name,
                        onClick = { onCategorySelected(cat.name) },
                    )
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                val categories = if (selectedCategory == null) {
                    coroutineProblemCategories
                } else {
                    coroutineProblemCategories.filter { it.name == selectedCategory }
                }

                categories.forEach { category ->
                    item(key = "header_${category.name}") {
                        CategoryHeader(category)
                    }
                    items(category.problems, key = { it.id }) { problem ->
                        ProblemRow(
                            problem = problem,
                            onClick = { onProblemSelected(problem) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text("$emoji $label", style = MaterialTheme.typography.labelMedium)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}

@Composable
private fun CategoryHeader(category: ProblemCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = category.emoji, fontSize = 18.sp)
        Text(
            text = category.name,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${category.problems.size} problems",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProblemRow(
    problem: CoroutineProblem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = problem.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = problem.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
        }
    }
}

// ─── Problem Detail ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProblemDetailScreen(
    problem: CoroutineProblem,
    output: List<OutputLine>,
    isRunning: Boolean,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onClearOutput: () -> Unit,
    onBack: () -> Unit,
) {
    val outputListState = rememberLazyListState()

    LaunchedEffect(output.size) {
        if (output.isNotEmpty()) {
            outputListState.animateScrollToItem(output.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = problem.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Category badge ────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val cat = coroutineProblemCategories.find { it.name == problem.category }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "${cat?.emoji ?: ""} ${problem.category}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── Description ───────────────────────────────────────────────────
            Text(
                text = problem.description,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )

            // ── Hint ──────────────────────────────────────────────────────────
            AnimatedVisibility(visible = problem.hint.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                ) {
                    Text(
                        text = "💡 ${problem.hint}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Code Snippet ──────────────────────────────────────────────────
            Text(
                text = "Code",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF161B22),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
            ) {
                Text(
                    text = problem.codeSnippet,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(14.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFE6EDF3),
                    lineHeight = 20.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Run controls ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { if (isRunning) onStop() else onRun() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isRunning) Color(0xFF3D1F1F) else Color(0xFF1B3A1F),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isRunning) TerminalRed.copy(alpha = 0.6f) else TerminalGreen.copy(alpha = 0.6f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isRunning) TerminalRed else TerminalGreen,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (isRunning) "Stop" else "Run",
                            color = if (isRunning) TerminalRed else TerminalGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = output.isNotEmpty() && !isRunning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Surface(
                        onClick = onClearOutput,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Terminal Output ───────────────────────────────────────────────
            Text(
                text = "Output",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(320.dp),
                shape = RoundedCornerShape(10.dp),
                color = TerminalBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
            ) {
                Column {
                    // Terminal title bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161B22))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFFF5F57)))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFFFBD2E)))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF28C840)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "zsh — coroutines-playground",
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalGray,
                            fontFamily = FontFamily.Monospace,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isRunning) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerminalGreen),
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF30363D), thickness = 1.dp)

                    if (output.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Press Run ▶ to execute",
                                style = MaterialTheme.typography.bodySmall,
                                color = TerminalGray,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    } else {
                        LazyColumn(
                            state = outputListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(output) { line ->
                                TerminalLine(line)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TerminalLine(line: OutputLine) {
    val (color, _) = when {
        line.isError -> TerminalRed to ""
        line.text.startsWith("▶ Running") -> TerminalYellow to ""
        line.text.startsWith("✓ Completed") -> TerminalGreen to ""
        line.text.startsWith("⏹") -> TerminalYellow to ""
        line.text.startsWith("✗") -> TerminalRed to ""
        line.text.startsWith("─") -> TerminalGray to ""
        line.text.contains("ms  ") -> {
            val parts = line.text.split("  ", limit = 2)
            val timestamp = parts.getOrNull(0) ?: ""
            val content = parts.getOrNull(1) ?: ""
            Row {
                Text(
                    text = "$timestamp  ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalGray,
                    lineHeight = 18.sp,
                )
                Text(
                    text = content,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalWhite,
                    lineHeight = 18.sp,
                )
            }
            return
        }
        else -> TerminalWhite to ""
    }
    Text(
        text = line.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = color,
        lineHeight = 18.sp,
    )
}
