package com.cryptosignal.assistant.presentation.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.cryptosignal.assistant.domain.models.TimeRange
import com.cryptosignal.assistant.presentation.components.MetricCard
import com.cryptosignal.assistant.presentation.theme.*

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val statistics by viewModel.statistics.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        StatisticsAppBar()
        
        // Time Range Selector
        TimeRangeSelector(
            selectedTimeRange = selectedTimeRange,
            onTimeRangeSelected = { viewModel.setTimeRange(it) }
        )
        
        // Statistics Content
        if (statistics != null) {
            StatisticsContent(
                statistics = statistics!!,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun StatisticsAppBar() {
    SmallTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.statistics_title),
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}

@Composable
private fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.values().forEach { timeRange ->
            FilterChip(
                selected = selectedTimeRange == timeRange,
                onClick = { onTimeRangeSelected(timeRange) },
                label = { Text(getTimeRangeLabel(timeRange)) }
            )
        }
    }
}

@Composable
private fun getTimeRangeLabel(timeRange: TimeRange): String {
    return when (timeRange) {
        TimeRange.DAY -> stringResource(R.string.statistics_time_range_day)
        TimeRange.WEEK -> stringResource(R.string.statistics_time_range_week)
        TimeRange.MONTH -> stringResource(R.string.statistics_time_range_month)
        TimeRange.QUARTER -> "Квартал"
        TimeRange.YEAR -> "Год"
        TimeRange.ALL -> stringResource(R.string.statistics_time_range_all)
    }
}

@Composable
private fun StatisticsContent(
    statistics: com.cryptosignal.assistant.domain.models.Statistics,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Key Metrics Grid
        item {
            MetricsGrid(statistics = statistics)
        }
        
        // Profit Chart Placeholder
        item {
            ProfitChartCard(statistics = statistics)
        }
        
        // Top Pairs
        item {
            TopPairsCard(statistics = statistics)
        }
        
        // Risk Metrics
        item {
            RiskMetricsCard(statistics = statistics)
        }
    }
}

@Composable
private fun MetricsGrid(statistics: com.cryptosignal.assistant.domain.models.Statistics) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = stringResource(R.string.statistics_winrate),
                value = "${String.format("%.1f", statistics.winRate)}%",
                change = null,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(R.string.statistics_profit_factor),
                value = String.format("%.2f", statistics.profitFactor),
                change = null,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(R.string.statistics_max_drawdown),
                value = "${String.format("%.1f", statistics.maxDrawdown)}%",
                change = null,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = stringResource(R.string.statistics_avg_rr),
                value = String.format("%.1f", statistics.averageRR),
                change = null,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(R.string.statistics_total_signals),
                value = statistics.totalSignals.toString(),
                change = null,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(R.string.statistics_this_month),
                value = "${if (statistics.netProfit >= 0) "+" else ""}${String.format("%.1f", statistics.netProfit)}%",
                change = null,
                modifier = Modifier.weight(1f),
                valueColor = if (statistics.netProfit >= 0) ProfitGreen else LossRed
            )
        }
    }
}

@Composable
private fun ProfitChartCard(statistics: com.cryptosignal.assistant.domain.models.Statistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Кумулятивная доходность",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Placeholder for chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "График доходности",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Итог: ${if (statistics.netProfit >= 0) "+" else ""}${String.format("%.1f", statistics.netProfit)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (statistics.netProfit >= 0) ProfitGreen else LossRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "R: ${String.format("%.1f", statistics.netProfit / 100 * 50)}", // Assuming 2% risk per trade
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopPairsCard(statistics: com.cryptosignal.assistant.domain.models.Statistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.statistics_profit_by_pair),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val topPairs = statistics.profitByPair
                .toList()
                .sortedByDescending { it.second }
                .take(5)
            
            topPairs.forEach { (pair, profit) ->
                PairProfitRow(
                    pair = pair,
                    profit = profit,
                    maxProfit = topPairs.first().second
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PairProfitRow(pair: String, profit: Double, maxProfit: Double) {
    val isProfit = profit >= 0
    val barWidth = if (maxProfit != 0.0) abs(profit) / abs(maxProfit) else 0.0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pair,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.3f)
        )
        
        Box(
            modifier = Modifier
                .weight(0.5f)
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth.toFloat())
                    .fillMaxHeight()
                    .background(
                        color = if (isProfit) ProfitGreen else LossRed,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
        
        Text(
            text = "${if (isProfit) "+" else ""}${String.format("%.1f", profit)}%",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isProfit) ProfitGreen else LossRed,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun RiskMetricsCard(statistics: com.cryptosignal.assistant.domain.models.Statistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Метрики риска",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Ожидание",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f", statistics.expectancy),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (statistics.expectancy >= 0) ProfitGreen else LossRed
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Win Rate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", statistics.winRate)}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Макс. серия",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${statistics.maxWinStreak}/${statistics.maxLossStreak}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}