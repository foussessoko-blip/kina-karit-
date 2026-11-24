package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.InventoryUiState
import com.example.ui.OperationTab
import com.example.ui.PerformanceSortField
import com.example.ui.ProductPerformance
import com.example.ui.components.BrandHeaderBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KpiCard
import com.example.ui.components.OrganicSearchField
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.util.FrenchFormatters

@Composable
fun DashboardScreen(
    uiState: InventoryUiState,
    isWideScreen: Boolean,
    onOpenOperation: (OperationTab, Long?) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleLowStockOnly: () -> Unit,
    onSelectSortField: (PerformanceSortField) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    var showTableView by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "header") {
            BrandHeaderBanner(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.app_subtitle)
            )
        }

        // Quick Actions Bar
        item(key = "quick_actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenOperation(OperationTab.VENTE, null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("quick_new_sale_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PointOfSale,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nouvelle vente",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledTonalButton(
                    onClick = { onOpenOperation(OperationTab.APPROVISIONNEMENT, null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("quick_restock_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Approvisionner",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // KPI Summary Cards Grid (2x2 on phone, 4-column on tablet)
        item(key = "kpi_grid") {
            val kpis = uiState.kpis
            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = stringResource(R.string.kpi_stock_value),
                        value = FrenchFormatters.formatFcfa(kpis.valeurTotaleStock),
                        subtitle = "${kpis.nombreProduits} produits au prix de revient",
                        icon = Icons.Rounded.Inventory2,
                        accentContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        accentContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = stringResource(R.string.kpi_revenue),
                        value = FrenchFormatters.formatFcfa(kpis.chiffreAffairesTotal),
                        subtitle = "${kpis.nombreVentes} ventes réalisées",
                        icon = Icons.Rounded.Payments,
                        accentContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        accentContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = stringResource(R.string.kpi_net_profit),
                        value = FrenchFormatters.formatFcfa(kpis.beneficeNetTotal),
                        subtitle = stringResource(R.string.kpi_net_profit_sub),
                        icon = Icons.Rounded.TrendingUp,
                        accentContainerColor = semantic.statusGreenBg,
                        accentContentColor = semantic.statusGreenText,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = stringResource(R.string.kpi_low_stock),
                        value = "${kpis.nombreAlertesStockBas}",
                        subtitle = if (kpis.nombreAlertesStockBas > 0) "À réapprovisionner" else "Tous les stocks sont bons",
                        icon = Icons.Rounded.WarningAmber,
                        accentContainerColor = if (kpis.nombreAlertesStockBas > 0) semantic.statusAmberBg else semantic.statusGreenBg,
                        accentContentColor = if (kpis.nombreAlertesStockBas > 0) semantic.statusAmberText else semantic.statusGreenText,
                        highlightedBorder = kpis.nombreAlertesStockBas > 0,
                        onClick = onToggleLowStockOnly,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = stringResource(R.string.kpi_stock_value),
                            value = FrenchFormatters.formatFcfa(kpis.valeurTotaleStock),
                            subtitle = "${kpis.nombreProduits} réf. au revient",
                            icon = Icons.Rounded.Inventory2,
                            accentContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            accentContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = stringResource(R.string.kpi_revenue),
                            value = FrenchFormatters.formatFcfa(kpis.chiffreAffairesTotal),
                            subtitle = "${kpis.nombreVentes} ventes enregistrées",
                            icon = Icons.Rounded.Payments,
                            accentContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            accentContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = stringResource(R.string.kpi_net_profit),
                            value = FrenchFormatters.formatFcfa(kpis.beneficeNetTotal),
                            subtitle = stringResource(R.string.kpi_net_profit_sub),
                            icon = Icons.Rounded.TrendingUp,
                            accentContainerColor = semantic.statusGreenBg,
                            accentContentColor = semantic.statusGreenText,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = stringResource(R.string.kpi_low_stock),
                            value = "${kpis.nombreAlertesStockBas} alerte${if (kpis.nombreAlertesStockBas > 1) "s" else ""}",
                            subtitle = if (kpis.nombreAlertesStockBas > 0) "Appuyer pour filtrer" else "Stock optimal",
                            icon = Icons.Rounded.WarningAmber,
                            accentContainerColor = if (kpis.nombreAlertesStockBas > 0) semantic.statusAmberBg else semantic.statusGreenBg,
                            accentContentColor = if (kpis.nombreAlertesStockBas > 0) semantic.statusAmberText else semantic.statusGreenText,
                            highlightedBorder = kpis.nombreAlertesStockBas > 0,
                            onClick = onToggleLowStockOnly,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Low stock alert banner
        item(key = "low_stock_banner") {
            AnimatedVisibility(visible = uiState.kpis.nombreAlertesStockBas > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleLowStockOnly() },
                    shape = RoundedCornerShape(16.dp),
                    color = semantic.statusAmberBg,
                    border = BorderStroke(1.dp, semantic.statusAmberText.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "Alerte stock bas",
                            tint = semantic.statusAmberText,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${uiState.kpis.nombreAlertesStockBas} produit(s) sous le seuil d'alerte. Touchez pour afficher uniquement les urgences.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = semantic.statusAmberText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section Header: Tableau de Performance par Produit
        item(key = "performance_header") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.table_title_performance),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Suivi des marges et bénéfices générés par produit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showTableView = true },
                                modifier = Modifier.testTag("toggle_table_view")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TableChart,
                                    contentDescription = "Vue tableau",
                                    tint = if (showTableView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showTableView = false },
                                modifier = Modifier.testTag("toggle_card_view")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GridView,
                                    contentDescription = "Vue cartes",
                                    tint = if (!showTableView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OrganicSearchField(
                    value = uiState.filters.dashboardSearch,
                    onValueChange = onSearchChange,
                    placeholder = stringResource(R.string.search_products_hint),
                    testTag = "dashboard_search_input"
                )

                // Sort & Low-stock filter row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item(key = "low_stock_chip") {
                        FilterChip(
                            selected = uiState.filters.dashboardLowStockOnly,
                            onClick = onToggleLowStockOnly,
                            label = { Text(text = "Stock bas") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = semantic.statusAmberBg,
                                selectedLabelColor = semantic.statusAmberText,
                                selectedLeadingIconColor = semantic.statusAmberText
                            ),
                            modifier = Modifier.testTag("filter_low_stock_chip")
                        )
                    }

                    items(PerformanceSortField.entries, key = { it.name }) { field ->
                        val isSelected = uiState.filters.sortField == field
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSortField(field) },
                            label = { Text(text = field.label) },
                            trailingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = if (uiState.filters.sortAscending) {
                                            Icons.Rounded.ArrowUpward
                                        } else {
                                            Icons.Rounded.ArrowDownward
                                        },
                                        contentDescription = if (uiState.filters.sortAscending) "Tri croissant" else "Tri décroissant",
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Performance Data
        if (uiState.performanceRows.isEmpty()) {
            item(key = "empty_performance") {
                EmptyStateCard(
                    icon = Icons.Rounded.Inventory2,
                    title = stringResource(R.string.empty_products_title),
                    description = stringResource(R.string.empty_products_desc)
                )
            }
        } else if (showTableView) {
            item(key = "performance_table") {
                PerformanceDataTable(
                    rows = uiState.performanceRows,
                    sortField = uiState.filters.sortField,
                    sortAscending = uiState.filters.sortAscending,
                    onSelectSortField = onSelectSortField,
                    onSellClick = { productId -> onOpenOperation(OperationTab.VENTE, productId) },
                    onRestockClick = { productId -> onOpenOperation(OperationTab.APPROVISIONNEMENT, productId) }
                )
            }
        } else {
            items(uiState.performanceRows, key = { it.product.id }) { row ->
                PerformanceProductCard(
                    row = row,
                    onSellClick = { onOpenOperation(OperationTab.VENTE, row.product.id) },
                    onRestockClick = { onOpenOperation(OperationTab.APPROVISIONNEMENT, row.product.id) }
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PerformanceDataTable(
    rows: List<ProductPerformance>,
    sortField: PerformanceSortField,
    sortAscending: Boolean,
    onSelectSortField: (PerformanceSortField) -> Unit,
    onSellClick: (Long) -> Unit,
    onRestockClick: (Long) -> Unit
) {
    val horizontalScroll = rememberScrollState()
    val semantic = LocalSemanticColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("performance_table"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            // Table Header Row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SortableHeaderCell(
                    label = "Produit",
                    field = PerformanceSortField.NOM,
                    activeField = sortField,
                    ascending = sortAscending,
                    onClick = onSelectSortField,
                    modifier = Modifier.width(156.dp),
                    alignEnd = false
                )
                SortableHeaderCell(
                    label = "Stock restant",
                    field = PerformanceSortField.STOCK,
                    activeField = sortField,
                    ascending = sortAscending,
                    onClick = onSelectSortField,
                    modifier = Modifier.width(126.dp),
                    alignEnd = false
                )
                SortableHeaderCell(
                    label = "Prix de revient",
                    field = PerformanceSortField.PRIX_REVIENT,
                    activeField = sortField,
                    ascending = sortAscending,
                    onClick = onSelectSortField,
                    modifier = Modifier.width(114.dp),
                    alignEnd = true
                )
                SortableHeaderCell(
                    label = "Prix de vente",
                    field = PerformanceSortField.PRIX_VENTE,
                    activeField = sortField,
                    ascending = sortAscending,
                    onClick = onSelectSortField,
                    modifier = Modifier.width(114.dp),
                    alignEnd = true
                )
                SortableHeaderCell(
                    label = "Bénéf. / unité",
                    field = PerformanceSortField.BENEFICE_UNITAIRE,
                    activeField = sortField,
                    ascending = sortAscending,
                    onClick = onSelectSortField,
                    modifier = Modifier.width(122.dp),
                    alignEnd = true
                )
                SortableHeaderCell(
                    label = "Bénéfice généré",
                    field = PerformanceSortField.BENEFICE_TOTAL,
                    activeField = sortField,
                    ascending = sortAscending,
                    onClick = onSelectSortField,
                    modifier = Modifier.width(132.dp),
                    alignEnd = true
                )
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(96.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            rows.forEachIndexed { index, row ->
                val p = row.product
                Row(
                    modifier = Modifier
                        .background(
                            if (index % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(156.dp)) {
                        Text(
                            text = p.nom,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Seuil : ${FrenchFormatters.formatQuantityWithUnit(p.seuilAlerte, p.unite)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(modifier = Modifier.width(126.dp)) {
                        StockStatusBadge(
                            status = p.statutStock,
                            stockLabel = FrenchFormatters.formatQuantityWithUnit(p.quantiteStock, p.unite)
                        )
                    }

                    Text(
                        text = FrenchFormatters.formatFcfa(p.prixRevientUnitaire),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(114.dp)
                    )

                    Text(
                        text = FrenchFormatters.formatFcfa(p.prixVenteUnitaire),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(114.dp)
                    )

                    Column(
                        modifier = Modifier.width(122.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = FrenchFormatters.formatFcfa(p.beneficeUnitaire),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (p.beneficeUnitaire >= 0) semantic.statusGreenText else semantic.statusRedText
                        )
                        Text(
                            text = "Marge ${FrenchFormatters.formatPercent(p.margePourcentage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.width(132.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = FrenchFormatters.formatFcfa(row.totalProfitGenerated),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (row.totalProfitGenerated > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${FrenchFormatters.formatQuantityWithUnit(row.totalUnitsSold, p.unite)} vendu(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.width(96.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { onSellClick(p.id) }) {
                            Icon(
                                imageVector = Icons.Rounded.AddShoppingCart,
                                contentDescription = "Vendre ${p.nom}",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { onRestockClick(p.id) }) {
                            Icon(
                                imageVector = Icons.Rounded.LocalShipping,
                                contentDescription = "Approvisionner ${p.nom}",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun SortableHeaderCell(
    label: String,
    field: PerformanceSortField,
    activeField: PerformanceSortField,
    ascending: Boolean,
    onClick: (PerformanceSortField) -> Unit,
    modifier: Modifier = Modifier,
    alignEnd: Boolean
) {
    val active = field == activeField
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(field) }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (active) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = if (ascending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun PerformanceProductCard(
    row: ProductPerformance,
    onSellClick: () -> Unit,
    onRestockClick: () -> Unit
) {
    val p = row.product
    val semantic = LocalSemanticColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = p.nom,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Marge : ${FrenchFormatters.formatPercent(p.margePourcentage)} • Seuil : ${FrenchFormatters.formatQuantityWithUnit(p.seuilAlerte, p.unite)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StockStatusBadge(
                    status = p.statutStock,
                    stockLabel = FrenchFormatters.formatQuantityWithUnit(p.quantiteStock, p.unite)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricMiniColumn(label = "Revient", value = FrenchFormatters.formatFcfa(p.prixRevientUnitaire))
                MetricMiniColumn(label = "Vente", value = FrenchFormatters.formatFcfa(p.prixVenteUnitaire))
                MetricMiniColumn(
                    label = "Bénéf. / u",
                    value = FrenchFormatters.formatFcfa(p.beneficeUnitaire),
                    valueColor = if (p.beneficeUnitaire >= 0) semantic.statusGreenText else semantic.statusRedText
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Bénéfice total généré",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FrenchFormatters.formatFcfa(row.totalProfitGenerated),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onRestockClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Entrée", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onSellClick,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Vendre", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniColumn(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
            color = valueColor
        )
    }
}
