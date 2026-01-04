package com.cryptosignal.assistant.presentation.screens.signals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cryptosignal.assistant.R
import com.cryptosignal.assistant.domain.models.Direction
import com.cryptosignal.assistant.domain.models.Signal
import com.cryptosignal.assistant.domain.models.SignalStatus
import com.cryptosignal.assistant.presentation.components.SignalCard
import com.cryptosignal.assistant.presentation.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignalsScreen(
    navController: NavController,
    viewModel: SignalsViewModel = hiltViewModel()
) {
    val signals by viewModel.signals.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        SignalsAppBar(navController = navController)
        
        // Filter Chips
        SignalFilterChips(viewModel = viewModel)
        
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                ErrorState(
                    message = error ?: stringResource(R.string.error_unknown),
                    onRetry = { viewModel.refreshSignals() }
                )
            }
            signals.isEmpty() -> {
                EmptyState(onRefresh = { viewModel.refreshSignals() })
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(signals, key = { it.id }) { signal ->
                        SignalCard(
                            signal = signal,
                            onClick = {
                                navController.navigate("signal_detail/${signal.id}")
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalsAppBar(navController: NavController) {
    SmallTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.signals_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = { navController.navigate("notifications") }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.notifications)
                )
            }
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }
    )
}

@Composable
private fun SignalFilterChips(viewModel: SignalsViewModel) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == SignalFilter.ALL,
            onClick = { viewModel.setFilter(SignalFilter.ALL) },
            label = { Text("Все") }
        )
        FilterChip(
            selected = selectedFilter == SignalFilter.ACTIVE,
            onClick = { viewModel.setFilter(SignalFilter.ACTIVE) },
            label = { Text("Активные") }
        )
        FilterChip(
            selected = selectedFilter == SignalFilter.LONG,
            onClick = { viewModel.setFilter(SignalFilter.LONG) },
            label = { Text("LONG") }
        )
        FilterChip(
            selected = selectedFilter == SignalFilter.SHORT,
            onClick = { viewModel.setFilter(SignalFilter.SHORT) },
            label = { Text("SHORT") }
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun EmptyState(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.signal_no_data),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Агент анализирует рынок...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRefresh) {
                Text(stringResource(R.string.refresh))
            }
        }
    }
}