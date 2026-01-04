package com.cryptosignal.assistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptosignal.assistant.domain.models.Direction
import com.cryptosignal.assistant.domain.models.Signal
import com.cryptosignal.assistant.domain.models.SignalStatus
import com.cryptosignal.assistant.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SignalCard(
    signal: Signal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(getSignalBackgroundColor(signal))
        ) {
            // Side indicator for direction
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(getDirectionColor(signal.direction))
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(start = 4.dp)
            ) {
                // Header with pair and status
                SignalHeader(signal = signal)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Prices
                SignalPrices(signal = signal)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Metrics row
                SignalMetrics(signal = signal)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Comment
                if (signal.comment.isNotEmpty()) {
                    Text(
                        text = signal.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Footer with time and outcome
                SignalFooter(signal = signal)
            }
        }
    }
}

@Composable
private fun SignalHeader(signal: Signal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(getDirectionColor(signal.direction))
            )
            
            // Pair name
            Text(
                text = signal.pair,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Status badge
        SignalStatusBadge(signal = signal)
    }
}

@Composable
private fun SignalPrices(signal: Signal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Entry price
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Вход",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$${String.format("%.2f", signal.entryPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Stop Loss
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$${String.format("%.2f", signal.stopLoss)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LossRed
            )
        }
        
        // Take Profit
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "TP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$${String.format("%.2f", signal.takeProfit)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ProfitGreen
            )
        }
    }
}

@Composable
private fun SignalMetrics(signal: Signal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // R/R Ratio
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "R/R",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("%.1f", signal.riskReward),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (signal.riskReward >= 2.0) ProfitGreen else WarningOrange
            )
        }
        
        // Leverage
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Плечо",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${String.format("%.1f", signal.leverage)}x",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Confidence
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Доверие",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(signal.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    signal.confidence >= 0.8 -> ProfitGreen
                    signal.confidence >= 0.6 -> WarningOrange
                    else -> LossRed
                }
            )
        }
    }
}

@Composable
private fun SignalFooter(signal: Signal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time and timeframe
        Column {
            Text(
                text = SimpleDateFormat("HH:mm • dd.MM", Locale.getDefault())
                    .format(Date(signal.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = signal.timeframe.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Outcome for completed signals
        if (signal.status == SignalStatus.COMPLETED && signal.outcome != null) {
            val outcome = signal.outcome
            val isProfit = outcome.pnlR > 0
            
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isProfit) ProfitGreen else LossRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${if (isProfit) "+" else ""}${String.format("%.1f", outcome.pnlR)}R",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitGreen else LossRed
                    )
                }
                Text(
                    text = "${if (outcome.pnlPercent > 0) "+" else ""}${String.format("%.2f", outcome.pnlPercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isProfit) ProfitGreen else LossRed
                )
            }
        }
    }
}

@Composable
private fun SignalStatusBadge(signal: Signal) {
    val (text, color) = when (signal.status) {
        SignalStatus.ACTIVE -> "Активный" to InfoBlue
        SignalStatus.COMPLETED -> "Завершен" to NeutralGray
        SignalStatus.CANCELLED -> "Отменен" to WarningOrange
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getDirectionColor(direction: Direction): Color {
    return when (direction) {
        Direction.LONG -> LongGreen
        Direction.SHORT -> ShortRed
    }
}

private fun getSignalBackgroundColor(signal: Signal): Color {
    return when (signal.status) {
        SignalStatus.ACTIVE -> MaterialTheme.colorScheme.surfaceVariant
        SignalStatus.COMPLETED -> {
            if (signal.outcome?.pnlR ?: 0.0 > 0) {
                ProfitGreen.copy(alpha = 0.1f)
            } else {
                LossRed.copy(alpha = 0.1f)
            }
        }
        SignalStatus.CANCELLED -> WarningOrange.copy(alpha = 0.1f)
    }
}