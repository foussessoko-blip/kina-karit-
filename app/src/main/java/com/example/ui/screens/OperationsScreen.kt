package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.OperationResult
import com.example.data.local.ProductEntity
import com.example.ui.InventoryUiState
import com.example.ui.OperationTab
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProductSelectorDropdown
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.util.FrenchFormatters
import kotlin.math.round

@Composable
fun OperationsScreen(
    uiState: InventoryUiState,
    onSelectTab: (OperationTab) -> Unit,
    onSelectProduct: (Long) -> Unit,
    onSubmitSale: (
        produitId: Long,
        quantiteVendue: Double,
        prixVenteUnitaire: Double,
        note: String,
        onResult: (OperationResult) -> Unit
    ) -> Unit,
    onSubmitRestock: (
        produitId: Long,
        quantiteAjoutee: Double,
        prixAchatLot: Double,
        fraisAnnexesLot: Double,
        note: String,
        onResult: (OperationResult) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = uiState.filters.operationTab
    val products = uiState.allProducts
    val selectedProduct = remember(products, uiState.filters.preselectedProductId) {
        products.firstOrNull { it.id == uiState.filters.preselectedProductId } ?: products.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("operations_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Ventes & Approvisionnement",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enregistrez vos sorties de stock et entrées au Coût Moyen Pondéré",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = activeTab == OperationTab.VENTE,
                onClick = { onSelectTab(OperationTab.VENTE) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PointOfSale,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("tab_nouvelle_vente")
            ) {
                Text(text = stringResource(R.string.action_new_sale))
            }
            SegmentedButton(
                selected = activeTab == OperationTab.APPROVISIONNEMENT,
                onClick = { onSelectTab(OperationTab.APPROVISIONNEMENT) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("tab_nouvel_appro")
            ) {
                Text(text = "Approvisionnement")
            }
        }

        if (products.isEmpty() || selectedProduct == null) {
            EmptyStateCard(
                icon = Icons.Rounded.Inventory2,
                title = "Aucun produit disponible",
                description = "Créez d'abord un produit dans l'onglet Produits avant d'enregistrer une vente ou un approvisionnement."
            )
        } else if (activeTab == OperationTab.VENTE) {
            SaleFormCard(
                products = products,
                selectedProduct = selectedProduct,
                onSelectProduct = { onSelectProduct(it.id) },
                onSubmitSale = onSubmitSale
            )
        } else {
            RestockFormCard(
                products = products,
                selectedProduct = selectedProduct,
                onSelectProduct = { onSelectProduct(it.id) },
                onSubmitRestock = onSubmitRestock
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SaleFormCard(
    products: List<ProductEntity>,
    selectedProduct: ProductEntity,
    onSelectProduct: (ProductEntity) -> Unit,
    onSubmitSale: (
        produitId: Long,
        quantiteVendue: Double,
        prixVenteUnitaire: Double,
        note: String,
        onResult: (OperationResult) -> Unit
    ) -> Unit
) {
    var quantityText by remember(selectedProduct.id) { mutableStateOf("1") }
    var unitPriceText by remember(selectedProduct.id) {
        mutableStateOf(selectedProduct.prixVenteUnitaire.toLong().toString())
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember(selectedProduct.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedProduct.id, selectedProduct.prixVenteUnitaire) {
        unitPriceText = selectedProduct.prixVenteUnitaire.toLong().toString()
    }

    val quantity = FrenchFormatters.parseFrenchDouble(quantityText) ?: 0.0
    val unitPrice = FrenchFormatters.parseFrenchDouble(unitPriceText) ?: selectedProduct.prixVenteUnitaire
    val totalRevenue = quantity * unitPrice
    val totalCost = quantity * selectedProduct.prixRevientUnitaire
    val netProfit = totalRevenue - totalCost
    val stockAfterSale = selectedProduct.quantiteStock - quantity
    val isOutOfStock = selectedProduct.quantiteStock <= 0.0
    val exceedsStock = quantity > selectedProduct.quantiteStock + 1e-6

    val liveStockError = when {
        isOutOfStock ->
            "Rupture de stock pour ${selectedProduct.nom}. Réapprovisionnez avant de vendre."
        exceedsStock ->
            "Stock insuffisant ! Quantité demandée (${FrenchFormatters.formatQuantityWithUnit(quantity, selectedProduct.unite)}) supérieure au stock disponible (${FrenchFormatters.formatQuantityWithUnit(selectedProduct.quantiteStock, selectedProduct.unite)})."
        else -> null
    }

    val semantic = LocalSemanticColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Enregistrer une vente",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StockStatusBadge(
                    status = selectedProduct.statutStock,
                    stockLabel = FrenchFormatters.formatQuantityWithUnit(
                        selectedProduct.quantiteStock,
                        selectedProduct.unite
                    )
                )
            }

            ProductSelectorDropdown(
                products = products,
                selectedProduct = selectedProduct,
                onSelectProduct = {
                    onSelectProduct(it)
                    errorMessage = null
                },
                label = "Produit à vendre",
                testTag = "sale_product_selector"
            )

            // Quantity stepper + input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quantité vendue (${selectedProduct.unite})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            val next = maxOf(0.5, quantity - 1.0)
                            quantityText = FrenchFormatters.formatQuantity(next).replace(" ", "")
                            errorMessage = null
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sale_qty_minus")
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Diminuer la quantité")
                    }

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it; errorMessage = null },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        suffix = { Text(selectedProduct.unite) },
                        isError = liveStockError != null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sale_quantity_input")
                    )

                    FilledTonalIconButton(
                        onClick = {
                            val next = quantity + 1.0
                            quantityText = FrenchFormatters.formatQuantity(next).replace(" ", "")
                            errorMessage = null
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sale_qty_plus")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Augmenter la quantité")
                    }
                }

                // Quick quantity chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.0, 2.0, 5.0, 10.0).forEach { preset ->
                        SuggestionChip(
                            onClick = {
                                quantityText = FrenchFormatters.formatQuantity(preset).replace(" ", "")
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    text = "${FrenchFormatters.formatQuantity(preset)} ${selectedProduct.unite}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = unitPriceText,
                onValueChange = { unitPriceText = it; errorMessage = null },
                label = { Text("Prix de vente unitaire (FCFA)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sale_price_input")
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Client ou remarque (optionnel)") },
                placeholder = { Text("Ex. : Vente comptoir, Épicerie Bio…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sale_note_input")
            )

            // Blocking French error banner if insufficient stock
            val displayedError = liveStockError ?: errorMessage
            AnimatedVisibility(visible = displayedError != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_error_banner"),
                    shape = RoundedCornerShape(14.dp),
                    color = semantic.statusRedBg,
                    border = BorderStroke(1.dp, semantic.statusRedText.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "Erreur stock",
                            tint = semantic.statusRedText
                        )
                        Text(
                            text = displayedError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = semantic.statusRedText
                        )
                    }
                }
            }

            // Real-time financial calculation card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Récapitulatif de la vente",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SummaryLine(
                        label = "Montant total (Chiffre d'affaires)",
                        value = FrenchFormatters.formatFcfa(maxOf(0.0, totalRevenue)),
                        emphasized = true
                    )
                    SummaryLine(
                        label = "Coût de revient (${FrenchFormatters.formatFcfa(selectedProduct.prixRevientUnitaire)} / ${selectedProduct.unite})",
                        value = FrenchFormatters.formatFcfa(maxOf(0.0, totalCost))
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                    SummaryLine(
                        label = "Bénéfice net de cette vente",
                        value = FrenchFormatters.formatFcfaSigned(netProfit),
                        emphasized = true,
                        valueColor = if (netProfit >= 0) semantic.statusGreenText else semantic.statusRedText
                    )
                    SummaryLine(
                        label = "Stock restant après vente",
                        value = if (stockAfterSale >= 0) {
                            FrenchFormatters.formatQuantityWithUnit(stockAfterSale, selectedProduct.unite)
                        } else {
                            "Stock insuffisant"
                        },
                        valueColor = when {
                            stockAfterSale < 0 -> semantic.statusRedText
                            stockAfterSale <= selectedProduct.seuilAlerte -> semantic.statusAmberText
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Button(
                onClick = {
                    val parsedQty = FrenchFormatters.parseFrenchDouble(quantityText)
                    val parsedPrice = FrenchFormatters.parseFrenchDouble(unitPriceText)
                    when {
                        parsedQty == null || parsedQty <= 0.0 ->
                            errorMessage = "Veuillez saisir une quantité vendue supérieure à zéro."
                        parsedPrice == null || parsedPrice < 0.0 ->
                            errorMessage = "Veuillez saisir un prix de vente valide."
                        liveStockError != null ->
                            errorMessage = liveStockError
                        else -> {
                            onSubmitSale(selectedProduct.id, parsedQty, parsedPrice, note) { res ->
                                if (res is OperationResult.Success) {
                                    quantityText = "1"
                                    note = ""
                                    errorMessage = null
                                } else if (res is OperationResult.Error) {
                                    errorMessage = res.message
                                }
                            }
                        }
                    }
                },
                enabled = liveStockError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_sale_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_confirm_sale),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun RestockFormCard(
    products: List<ProductEntity>,
    selectedProduct: ProductEntity,
    onSelectProduct: (ProductEntity) -> Unit,
    onSubmitRestock: (
        produitId: Long,
        quantiteAjoutee: Double,
        prixAchatLot: Double,
        fraisAnnexesLot: Double,
        note: String,
        onResult: (OperationResult) -> Unit
    ) -> Unit
) {
    var quantityText by remember(selectedProduct.id) { mutableStateOf("10") }
    var purchasePriceText by remember(selectedProduct.id) {
        mutableStateOf(selectedProduct.prixAchatUnitaire.toLong().toString())
    }
    var ancillaryFeesText by remember(selectedProduct.id) {
        mutableStateOf(selectedProduct.fraisAnnexesUnitaire.toLong().toString())
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember(selectedProduct.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedProduct.id) {
        purchasePriceText = selectedProduct.prixAchatUnitaire.toLong().toString()
        ancillaryFeesText = selectedProduct.fraisAnnexesUnitaire.toLong().toString()
    }

    val qtyAdded = FrenchFormatters.parseFrenchDouble(quantityText) ?: 0.0
    val achatLot = FrenchFormatters.parseFrenchDouble(purchasePriceText) ?: 0.0
    val fraisLot = FrenchFormatters.parseFrenchDouble(ancillaryFeesText) ?: 0.0
    val coutLotUnitaire = achatLot + fraisLot
    val totalRestockCost = qtyAdded * coutLotUnitaire

    val currentStock = maxOf(0.0, selectedProduct.quantiteStock)
    val projectedStock = currentStock + maxOf(0.0, qtyAdded)
    val projectedCmp = if (projectedStock > 0.0 && qtyAdded > 0.0) {
        round(
            (((currentStock * selectedProduct.prixRevientUnitaire) + (qtyAdded * coutLotUnitaire)) / projectedStock) * 100.0
        ) / 100.0
    } else {
        selectedProduct.prixRevientUnitaire
    }
    val projectedUnitProfit = selectedProduct.prixVenteUnitaire - projectedCmp
    val semantic = LocalSemanticColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.action_new_restock),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StockStatusBadge(
                    status = selectedProduct.statutStock,
                    stockLabel = FrenchFormatters.formatQuantityWithUnit(
                        selectedProduct.quantiteStock,
                        selectedProduct.unite
                    )
                )
            }

            ProductSelectorDropdown(
                products = products,
                selectedProduct = selectedProduct,
                onSelectProduct = {
                    onSelectProduct(it)
                    errorMessage = null
                },
                label = "Produit à réapprovisionner",
                testTag = "restock_product_selector"
            )

            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it; errorMessage = null },
                label = { Text("Quantité reçue (${selectedProduct.unite})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text(selectedProduct.unite) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restock_quantity_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = purchasePriceText,
                    onValueChange = { purchasePriceText = it; errorMessage = null },
                    label = { Text("Prix d'achat / u") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("restock_purchase_input")
                )
                OutlinedTextField(
                    value = ancillaryFeesText,
                    onValueChange = { ancillaryFeesText = it; errorMessage = null },
                    label = { Text("Frais annexes / u") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("restock_fees_input")
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Fournisseur ou note (optionnel)") },
                placeholder = { Text("Ex. : Coopérative locale, transport inclus…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restock_note_input")
            )

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Live Weighted Average Cost (CMP) Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recalcul automatique du Coût Moyen Pondéré (CMP)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SummaryLine(
                        label = "Prix de revient de ce lot",
                        value = FrenchFormatters.formatFcfa(coutLotUnitaire)
                    )
                    SummaryLine(
                        label = "Ancien prix de revient",
                        value = FrenchFormatters.formatFcfa(selectedProduct.prixRevientUnitaire)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                    SummaryLine(
                        label = "Nouveau prix de revient (CMP)",
                        value = FrenchFormatters.formatFcfa(projectedCmp),
                        emphasized = true,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    SummaryLine(
                        label = "Nouveau bénéfice / ${selectedProduct.unite}",
                        value = FrenchFormatters.formatFcfa(projectedUnitProfit),
                        valueColor = if (projectedUnitProfit >= 0) semantic.statusGreenText else semantic.statusRedText
                    )
                    SummaryLine(
                        label = "Nouveau stock total",
                        value = FrenchFormatters.formatQuantityWithUnit(projectedStock, selectedProduct.unite),
                        emphasized = true
                    )
                    SummaryLine(
                        label = "Coût total de l'entrée",
                        value = FrenchFormatters.formatFcfa(maxOf(0.0, totalRestockCost))
                    )
                }
            }

            Button(
                onClick = {
                    val parsedQty = FrenchFormatters.parseFrenchDouble(quantityText)
                    val parsedAchat = FrenchFormatters.parseFrenchDouble(purchasePriceText)
                    val parsedFrais = FrenchFormatters.parseFrenchDouble(ancillaryFeesText) ?: 0.0
                    when {
                        parsedQty == null || parsedQty <= 0.0 ->
                            errorMessage = "Veuillez saisir une quantité reçue supérieure à zéro."
                        parsedAchat == null || parsedAchat < 0.0 || parsedFrais < 0.0 ->
                            errorMessage = "Veuillez saisir des montants positifs valides."
                        else -> {
                            onSubmitRestock(selectedProduct.id, parsedQty, parsedAchat, parsedFrais, note) { res ->
                                if (res is OperationResult.Success) {
                                    quantityText = "10"
                                    note = ""
                                    errorMessage = null
                                } else if (res is OperationResult.Error) {
                                    errorMessage = res.message
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_restock_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_confirm_restock),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    emphasized: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = if (emphasized) {
                MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                )
            } else {
                MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
            },
            color = valueColor
        )
    }
}
