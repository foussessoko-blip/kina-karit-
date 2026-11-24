package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.ProductEntity
import com.example.data.local.StockStatus
import com.example.ui.InventoryUiState
import com.example.ui.OperationTab
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OrganicSearchField
import com.example.ui.components.PresetUnitChips
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.util.FrenchFormatters

@Composable
fun ProductsScreen(
    uiState: InventoryUiState,
    onSearchChange: (String) -> Unit,
    onSelectStatusFilter: (StockStatus?) -> Unit,
    onSaveProduct: (
        id: Long?,
        nom: String,
        unite: String,
        quantiteStock: Double,
        seuilAlerte: Double,
        prixAchatUnitaire: Double,
        fraisAnnexesUnitaire: Double,
        prixVenteUnitaire: Double,
        onComplete: (Boolean) -> Unit
    ) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onOpenOperation: (OperationTab, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val semantic = LocalSemanticColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("products_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "products_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Catalogue & Coûts",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.allProducts.size} produits naturels configurés",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Nouveau")
                }
            }
        }

        item(key = "products_search_and_filters") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OrganicSearchField(
                    value = uiState.filters.productsSearch,
                    onValueChange = onSearchChange,
                    placeholder = stringResource(R.string.search_products_hint),
                    testTag = "products_search_input"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "all_status") {
                        FilterChip(
                            selected = uiState.filters.productsStatusFilter == null,
                            onClick = { onSelectStatusFilter(null) },
                            label = { Text("Tous (${uiState.allProducts.size})") }
                        )
                    }
                    item(key = "en_stock") {
                        val count = uiState.allProducts.count { it.statutStock == StockStatus.EN_STOCK }
                        FilterChip(
                            selected = uiState.filters.productsStatusFilter == StockStatus.EN_STOCK,
                            onClick = { onSelectStatusFilter(StockStatus.EN_STOCK) },
                            label = { Text("En stock ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = semantic.statusGreenBg,
                                selectedLabelColor = semantic.statusGreenText
                            )
                        )
                    }
                    item(key = "stock_bas") {
                        val count = uiState.allProducts.count { it.statutStock == StockStatus.STOCK_BAS }
                        FilterChip(
                            selected = uiState.filters.productsStatusFilter == StockStatus.STOCK_BAS,
                            onClick = { onSelectStatusFilter(StockStatus.STOCK_BAS) },
                            label = { Text("Stock bas ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = semantic.statusAmberBg,
                                selectedLabelColor = semantic.statusAmberText
                            )
                        )
                    }
                    item(key = "rupture") {
                        val count = uiState.allProducts.count { it.statutStock == StockStatus.RUPTURE }
                        FilterChip(
                            selected = uiState.filters.productsStatusFilter == StockStatus.RUPTURE,
                            onClick = { onSelectStatusFilter(StockStatus.RUPTURE) },
                            label = { Text("Rupture ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = semantic.statusRedBg,
                                selectedLabelColor = semantic.statusRedText
                            )
                        )
                    }
                }
            }
        }

        if (uiState.filteredProducts.isEmpty()) {
            item(key = "empty_products") {
                EmptyStateCard(
                    icon = Icons.Rounded.Inventory2,
                    title = stringResource(R.string.empty_products_title),
                    description = stringResource(R.string.empty_products_desc)
                )
            }
        } else {
            items(uiState.filteredProducts, key = { it.id }) { product ->
                ProductCatalogCard(
                    product = product,
                    onEdit = { editingProduct = product },
                    onDelete = { deletingProduct = product },
                    onSell = { onOpenOperation(OperationTab.VENTE, product.id) },
                    onRestock = { onOpenOperation(OperationTab.APPROVISIONNEMENT, product.id) }
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCreateDialog || editingProduct != null) {
        ProductEditorDialog(
            initialProduct = editingProduct,
            onDismiss = {
                showCreateDialog = false
                editingProduct = null
            },
            onSave = { nom, unite, stock, seuil, achat, frais, vente ->
                onSaveProduct(
                    editingProduct?.id,
                    nom,
                    unite,
                    stock,
                    seuil,
                    achat,
                    frais,
                    vente
                ) { ok ->
                    if (ok) {
                        showCreateDialog = false
                        editingProduct = null
                    }
                }
            }
        )
    }

    deletingProduct?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text(text = "Supprimer le produit ?") },
            text = {
                Text(
                    text = "Voulez-vous supprimer « ${target.nom} » du catalogue ? L'historique des transactions déjà enregistrées sera conservé."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(target)
                        deletingProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_product_button")
                ) {
                    Text(text = stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun ProductCatalogCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSell: () -> Unit,
    onRestock: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nom,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Seuil d'alerte : ${FrenchFormatters.formatQuantityWithUnit(product.seuilAlerte, product.unite)} • Valeur stock : ${FrenchFormatters.formatFcfa(product.valeurStockRevient)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StockStatusBadge(
                    status = product.statutStock,
                    stockLabel = FrenchFormatters.formatQuantityWithUnit(product.quantiteStock, product.unite)
                )
            }

            // Cost structure box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CostStatItem(label = "Prix d'achat", value = FrenchFormatters.formatFcfa(product.prixAchatUnitaire))
                    CostStatItem(label = "Frais annexes", value = FrenchFormatters.formatFcfa(product.fraisAnnexesUnitaire))
                    CostStatItem(
                        label = "Prix de revient",
                        value = FrenchFormatters.formatFcfa(product.prixRevientUnitaire),
                        bold = true
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CostStatItem(
                        label = "Prix de vente",
                        value = FrenchFormatters.formatFcfa(product.prixVenteUnitaire),
                        bold = true
                    )
                    CostStatItem(
                        label = "Bénéfice / ${product.unite}",
                        value = FrenchFormatters.formatFcfa(product.beneficeUnitaire),
                        bold = true,
                        valueColor = if (product.beneficeUnitaire >= 0) semantic.statusGreenText else semantic.statusRedText
                    )
                    CostStatItem(
                        label = "Marge %",
                        value = FrenchFormatters.formatPercent(product.margePourcentage),
                        bold = true,
                        valueColor = if (product.margePourcentage >= 0) MaterialTheme.colorScheme.primary else semantic.statusRedText
                    )
                }
            }

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSell,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sell_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "Vendre", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = onRestock,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("restock_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "Entrée", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Modifier ${product.nom}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Supprimer ${product.nom}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CostStatItem(
    label: String,
    value: String,
    bold: Boolean = false,
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
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
                fontFeatureSettings = "tnum"
            ),
            color = valueColor
        )
    }
}

@Composable
private fun ProductEditorDialog(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (
        nom: String,
        unite: String,
        stock: Double,
        seuil: Double,
        achat: Double,
        frais: Double,
        vente: Double
    ) -> Unit
) {
    var nom by remember(initialProduct) { mutableStateOf(initialProduct?.nom ?: "") }
    var unite by remember(initialProduct) { mutableStateOf(initialProduct?.unite ?: "kg") }
    var stockText by remember(initialProduct) {
        mutableStateOf(initialProduct?.quantiteStock?.let { FrenchFormatters.formatQuantity(it).replace(" ", "") } ?: "0")
    }
    var seuilText by remember(initialProduct) {
        mutableStateOf(initialProduct?.seuilAlerte?.let { FrenchFormatters.formatQuantity(it).replace(" ", "") } ?: "5")
    }
    var achatText by remember(initialProduct) {
        mutableStateOf(initialProduct?.prixAchatUnitaire?.toLong()?.toString() ?: "")
    }
    var fraisText by remember(initialProduct) {
        mutableStateOf(initialProduct?.fraisAnnexesUnitaire?.toLong()?.toString() ?: "0")
    }
    var venteText by remember(initialProduct) {
        mutableStateOf(initialProduct?.prixVenteUnitaire?.toLong()?.toString() ?: "")
    }
    var formError by remember { mutableStateOf<String?>(null) }

    val parsedAchat = FrenchFormatters.parseFrenchDouble(achatText) ?: 0.0
    val parsedFrais = FrenchFormatters.parseFrenchDouble(fraisText) ?: 0.0
    val parsedVente = FrenchFormatters.parseFrenchDouble(venteText) ?: 0.0
    val prixRevient = parsedAchat + parsedFrais
    val beneficeUnitaire = parsedVente - prixRevient
    val margePct = if (parsedVente > 0.0) (beneficeUnitaire / parsedVente) * 100.0 else 0.0

    val semantic = LocalSemanticColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialProduct == null) {
                        stringResource(R.string.action_add_product)
                    } else {
                        stringResource(R.string.action_edit_product)
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it; formError = null },
                    label = { Text(stringResource(R.string.label_product_name)) },
                    placeholder = { Text("Ex. : Beurre de karité bio") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.label_unit),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PresetUnitChips(
                        selectedUnit = unite,
                        onSelectUnit = { unite = it; formError = null }
                    )
                    OutlinedTextField(
                        value = unite,
                        onValueChange = { unite = it; formError = null },
                        label = { Text(stringResource(R.string.label_custom_unit)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_unit_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it; formError = null },
                        label = { Text("Stock actuel") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_stock_input")
                    )
                    OutlinedTextField(
                        value = seuilText,
                        onValueChange = { seuilText = it; formError = null },
                        label = { Text("Seuil alerte") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_threshold_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = achatText,
                        onValueChange = { achatText = it; formError = null },
                        label = { Text("Prix d'achat (FCFA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_purchase_input")
                    )
                    OutlinedTextField(
                        value = fraisText,
                        onValueChange = { fraisText = it; formError = null },
                        label = { Text("Frais annexes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_fees_input")
                    )
                }

                OutlinedTextField(
                    value = venteText,
                    onValueChange = { venteText = it; formError = null },
                    label = { Text(stringResource(R.string.label_selling_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_selling_input")
                )

                // Live Calculated Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Calcul automatique des marges",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CostStatItem(
                                label = "Prix de revient",
                                value = FrenchFormatters.formatFcfa(prixRevient),
                                bold = true
                            )
                            CostStatItem(
                                label = "Bénéfice / u",
                                value = FrenchFormatters.formatFcfa(beneficeUnitaire),
                                bold = true,
                                valueColor = if (beneficeUnitaire >= 0) semantic.statusGreenText else semantic.statusRedText
                            )
                            CostStatItem(
                                label = "Marge %",
                                value = FrenchFormatters.formatPercent(margePct),
                                bold = true,
                                valueColor = if (margePct >= 0) MaterialTheme.colorScheme.primary else semantic.statusRedText
                            )
                        }
                    }
                }

                if (formError != null) {
                    Text(
                        text = formError!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = {
                            val s = FrenchFormatters.parseFrenchDouble(stockText)
                            val th = FrenchFormatters.parseFrenchDouble(seuilText)
                            val a = FrenchFormatters.parseFrenchDouble(achatText)
                            val f = FrenchFormatters.parseFrenchDouble(fraisText) ?: 0.0
                            val v = FrenchFormatters.parseFrenchDouble(venteText)
                            when {
                                nom.trim().isEmpty() -> formError = "Veuillez saisir le nom du produit."
                                unite.trim().isEmpty() -> formError = "Veuillez indiquer une unité de mesure."
                                s == null || th == null || a == null || v == null ->
                                    formError = "Veuillez remplir tous les montants et quantités avec des nombres valides."
                                s < 0.0 || th < 0.0 || a < 0.0 || f < 0.0 || v < 0.0 ->
                                    formError = "Les quantités et prix doivent être positifs ou nuls."
                                else -> onSave(nom, unite, s, th, a, f, v)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_product_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    }
}
