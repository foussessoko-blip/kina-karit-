package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.SouthWest
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.HistoryFilter
import com.example.ui.InventoryUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OrganicSearchField
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.util.FrenchFormatters

@Composable
fun HistoryScreen(
    uiState: InventoryUiState,
    onSelectFilter: (HistoryFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val filtered = uiState.filteredTransactions
    val salesInFilter = filtered.filter { it.type == TransactionType.VENTE }
    val restocksInFilter = filtered.filter { it.type == TransactionType.ENTREE }
    val totalSalesAmount = salesInFilter.sumOf { it.montantTotal }
    val totalSalesProfit = salesInFilter.sumOf { it.beneficeNet }
    val totalRestockAmount = restocksInFilter.sumOf { it.montantTotal }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "history_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Historique des mouvements",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filtered.size} opération(s) affichée(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onExportCsv,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("export_csv_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.action_export_csv))
                }
            }
        }

        item(key = "history_filters") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OrganicSearchField(
                    value = uiState.filters.historySearch,
                    onValueChange = onSearchChange,
                    placeholder = stringResource(R.string.search_history_hint),
                    testTag = "history_search_input"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistoryFilter.entries.forEach { filter ->
                        val selected = uiState.filters.historyFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFilter(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("history_filter_${filter.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Summary Strip for filtered transactions
        item(key = "history_summary") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Ventes (${salesInFilter.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FrenchFormatters.formatFcfa(totalSalesAmount),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Bénéfice net",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FrenchFormatters.formatFcfaSigned(totalSalesProfit),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = semantic.statusGreenText
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Entrées (${restocksInFilter.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FrenchFormatters.formatFcfa(totalRestockAmount),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item(key = "empty_history") {
                EmptyStateCard(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.empty_history_title),
                    description = stringResource(R.string.empty_history_desc)
                )
            }
        } else {
            items(filtered, key = { it.id }) { tx ->
                TransactionRowCard(tx = tx)
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TransactionRowCard(tx: TransactionEntity) {
    val semantic = LocalSemanticColors.current
    val isSale = tx.type == TransactionType.VENTE

    val badgeBg = if (isSale) semantic.statusGreenBg else semantic.statusAmberBg
    val badgeFg = if (isSale) semantic.statusGreenText else semantic.statusAmberText

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${tx.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSale) Icons.Rounded.NorthEast else Icons.Rounded.SouthWest,
                    contentDescription = if (isSale) "Vente" else "Entrée de stock",
                    tint = badgeFg,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tx.produitNom,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isSale) "VENTE" else "ENTRÉE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${if (isSale) "-" else "+"}${FrenchFormatters.formatQuantityWithUnit(tx.quantite, tx.unite)} • ${FrenchFormatters.formatFcfa(tx.prixUnitaire)} / ${tx.unite}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = buildString {
                        append(FrenchFormatters.formatDateTime(tx.timestamp))
                        if (tx.note.isNotBlank()) {
                            append(" • ")
                            append(tx.note)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = FrenchFormatters.formatFcfa(tx.montantTotal),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isSale) {
                    Text(
                        text = "Bénéf. ${FrenchFormatters.formatFcfaSigned(tx.beneficeNet)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = if (tx.beneficeNet >= 0) semantic.statusGreenText else semantic.statusRedText
                    )
                } else {
                    Text(
                        text = "CMP ${FrenchFormatters.formatFcfa(tx.prixRevientSnapshot)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
