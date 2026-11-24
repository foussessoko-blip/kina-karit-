package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.OperationResult
import com.example.data.local.ProductEntity
import com.example.data.local.StockStatus
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.repository.InventoryRepository
import com.example.ui.util.FrenchFormatters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppDestination {
    DASHBOARD,
    PRODUCTS,
    OPERATIONS,
    HISTORY
}

enum class OperationTab {
    VENTE,
    APPROVISIONNEMENT
}

enum class PerformanceSortField(val label: String) {
    NOM("Produit"),
    STOCK("Stock"),
    PRIX_REVIENT("Revient"),
    PRIX_VENTE("Vente"),
    BENEFICE_UNITAIRE("Bénéf./u"),
    BENEFICE_TOTAL("Bénéf. total")
}

enum class HistoryFilter(val label: String) {
    TOUS("Tous"),
    VENTES("Ventes"),
    ENTREES("Entrées")
}

data class ProductPerformance(
    val product: ProductEntity,
    val totalProfitGenerated: Double,
    val totalUnitsSold: Double,
    val totalRevenueGenerated: Double
)

data class DashboardKpis(
    val valeurTotaleStock: Double = 0.0,
    val chiffreAffairesTotal: Double = 0.0,
    val beneficeNetTotal: Double = 0.0,
    val nombreAlertesStockBas: Int = 0,
    val nombreProduits: Int = 0,
    val nombreVentes: Int = 0
)

data class FilterState(
    val currentDestination: AppDestination = AppDestination.DASHBOARD,
    val operationTab: OperationTab = OperationTab.VENTE,
    val preselectedProductId: Long? = null,
    val dashboardSearch: String = "",
    val dashboardLowStockOnly: Boolean = false,
    val sortField: PerformanceSortField = PerformanceSortField.BENEFICE_TOTAL,
    val sortAscending: Boolean = false,
    val productsSearch: String = "",
    val productsStatusFilter: StockStatus? = null,
    val historyFilter: HistoryFilter = HistoryFilter.TOUS,
    val historySearch: String = ""
)

data class InventoryUiState(
    val isLoading: Boolean = true,
    val allProducts: List<ProductEntity> = emptyList(),
    val filteredProducts: List<ProductEntity> = emptyList(),
    val performanceRows: List<ProductPerformance> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val kpis: DashboardKpis = DashboardKpis(),
    val filters: FilterState = FilterState()
)

sealed interface UiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : UiEvent
    data class RequestCsvSave(val fileName: String, val csvContent: String) : UiEvent
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterState())
    private val isSeeded = MutableStateFlow(false)

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.products,
        repository.transactions,
        filterState,
        isSeeded
    ) { products, transactions, filters, seeded ->
        val salesTransactions = transactions.filter { it.type == TransactionType.VENTE }
        val profitByProductId = salesTransactions.groupBy { it.produitId }.mapValues { (_, list) ->
            Triple(
                list.sumOf { it.beneficeNet },
                list.sumOf { it.quantite },
                list.sumOf { it.montantTotal }
            )
        }

        val kpis = DashboardKpis(
            valeurTotaleStock = products.sumOf { it.valeurStockRevient },
            chiffreAffairesTotal = salesTransactions.sumOf { it.montantTotal },
            beneficeNetTotal = salesTransactions.sumOf { it.beneficeNet },
            nombreAlertesStockBas = products.count { it.statutStock != StockStatus.EN_STOCK },
            nombreProduits = products.size,
            nombreVentes = salesTransactions.size
        )

        val dashQuery = filters.dashboardSearch.trim().lowercase()
        val rawPerformance = products.map { product ->
            val stats = profitByProductId[product.id] ?: Triple(0.0, 0.0, 0.0)
            ProductPerformance(
                product = product,
                totalProfitGenerated = stats.first,
                totalUnitsSold = stats.second,
                totalRevenueGenerated = stats.third
            )
        }.filter { row ->
            val matchesQuery = dashQuery.isEmpty() ||
                row.product.nom.lowercase().contains(dashQuery) ||
                row.product.unite.lowercase().contains(dashQuery)
            val matchesLowStock = !filters.dashboardLowStockOnly ||
                row.product.statutStock != StockStatus.EN_STOCK
            matchesQuery && matchesLowStock
        }

        val sortedPerformance = when (filters.sortField) {
            PerformanceSortField.NOM ->
                if (filters.sortAscending) rawPerformance.sortedBy { it.product.nom.lowercase() }
                else rawPerformance.sortedByDescending { it.product.nom.lowercase() }
            PerformanceSortField.STOCK ->
                if (filters.sortAscending) rawPerformance.sortedBy { it.product.quantiteStock }
                else rawPerformance.sortedByDescending { it.product.quantiteStock }
            PerformanceSortField.PRIX_REVIENT ->
                if (filters.sortAscending) rawPerformance.sortedBy { it.product.prixRevientUnitaire }
                else rawPerformance.sortedByDescending { it.product.prixRevientUnitaire }
            PerformanceSortField.PRIX_VENTE ->
                if (filters.sortAscending) rawPerformance.sortedBy { it.product.prixVenteUnitaire }
                else rawPerformance.sortedByDescending { it.product.prixVenteUnitaire }
            PerformanceSortField.BENEFICE_UNITAIRE ->
                if (filters.sortAscending) rawPerformance.sortedBy { it.product.beneficeUnitaire }
                else rawPerformance.sortedByDescending { it.product.beneficeUnitaire }
            PerformanceSortField.BENEFICE_TOTAL ->
                if (filters.sortAscending) rawPerformance.sortedBy { it.totalProfitGenerated }
                else rawPerformance.sortedByDescending { it.totalProfitGenerated }
        }

        val prodQuery = filters.productsSearch.trim().lowercase()
        val filteredProducts = products.filter { p ->
            val matchesQuery = prodQuery.isEmpty() ||
                p.nom.lowercase().contains(prodQuery) ||
                p.unite.lowercase().contains(prodQuery)
            val matchesStatus = filters.productsStatusFilter == null || p.statutStock == filters.productsStatusFilter
            matchesQuery && matchesStatus
        }

        val histQuery = filters.historySearch.trim().lowercase()
        val filteredTransactions = transactions.filter { tx ->
            val matchesType = when (filters.historyFilter) {
                HistoryFilter.TOUS -> true
                HistoryFilter.VENTES -> tx.type == TransactionType.VENTE
                HistoryFilter.ENTREES -> tx.type == TransactionType.ENTREE
            }
            val matchesQuery = histQuery.isEmpty() ||
                tx.produitNom.lowercase().contains(histQuery) ||
                tx.note.lowercase().contains(histQuery)
            matchesType && matchesQuery
        }

        InventoryUiState(
            isLoading = !seeded && products.isEmpty(),
            allProducts = products,
            filteredProducts = filteredProducts,
            performanceRows = sortedPerformance,
            allTransactions = transactions,
            filteredTransactions = filteredTransactions,
            kpis = kpis,
            filters = filters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            isSeeded.value = true
        }
    }

    fun navigateTo(destination: AppDestination) {
        filterState.update { it.copy(currentDestination = destination) }
    }

    fun openOperation(tab: OperationTab, productId: Long? = null) {
        filterState.update {
            it.copy(
                currentDestination = AppDestination.OPERATIONS,
                operationTab = tab,
                preselectedProductId = productId ?: it.preselectedProductId
            )
        }
    }

    fun selectOperationTab(tab: OperationTab) {
        filterState.update { it.copy(operationTab = tab) }
    }

    fun setPreselectedProduct(productId: Long?) {
        filterState.update { it.copy(preselectedProductId = productId) }
    }

    fun updateDashboardSearch(query: String) {
        filterState.update { it.copy(dashboardSearch = query) }
    }

    fun toggleDashboardLowStockOnly() {
        filterState.update { it.copy(dashboardLowStockOnly = !it.dashboardLowStockOnly) }
    }

    fun setDashboardLowStockOnly(enabled: Boolean) {
        filterState.update { it.copy(dashboardLowStockOnly = enabled) }
    }

    fun selectSortField(field: PerformanceSortField) {
        filterState.update { current ->
            if (current.sortField == field) {
                current.copy(sortAscending = !current.sortAscending)
            } else {
                val defaultAsc = field == PerformanceSortField.NOM || field == PerformanceSortField.STOCK
                current.copy(sortField = field, sortAscending = defaultAsc)
            }
        }
    }

    fun updateProductsSearch(query: String) {
        filterState.update { it.copy(productsSearch = query) }
    }

    fun selectProductsStatusFilter(status: StockStatus?) {
        filterState.update {
            it.copy(productsStatusFilter = if (it.productsStatusFilter == status) null else status)
        }
    }

    fun selectHistoryFilter(filter: HistoryFilter) {
        filterState.update { it.copy(historyFilter = filter) }
    }

    fun updateHistorySearch(query: String) {
        filterState.update { it.copy(historySearch = query) }
    }

    fun saveProduct(
        id: Long?,
        nom: String,
        unite: String,
        quantiteStock: Double,
        seuilAlerte: Double,
        prixAchatUnitaire: Double,
        fraisAnnexesUnitaire: Double,
        prixVenteUnitaire: Double,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.saveProduct(
                id = id,
                nom = nom,
                unite = unite,
                quantiteStock = quantiteStock,
                seuilAlerte = seuilAlerte,
                prixAchatUnitaire = prixAchatUnitaire,
                fraisAnnexesUnitaire = fraisAnnexesUnitaire,
                prixVenteUnitaire = prixVenteUnitaire
            )
            emitResult(result)
            onComplete(result is OperationResult.Success)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            val result = repository.deleteProduct(product)
            emitResult(result)
        }
    }

    fun recordSale(
        produitId: Long,
        quantiteVendue: Double,
        prixVenteUnitaire: Double,
        note: String,
        onResult: (OperationResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.recordSale(
                produitId = produitId,
                quantiteVendue = quantiteVendue,
                prixVenteUnitaire = prixVenteUnitaire,
                note = note
            )
            emitResult(result)
            onResult(result)
        }
    }

    fun recordRestock(
        produitId: Long,
        quantiteAjoutee: Double,
        prixAchatLot: Double,
        fraisAnnexesLot: Double,
        note: String,
        onResult: (OperationResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.recordRestock(
                produitId = produitId,
                quantiteAjoutee = quantiteAjoutee,
                prixAchatLot = prixAchatLot,
                fraisAnnexesLot = fraisAnnexesLot,
                note = note
            )
            emitResult(result)
            onResult(result)
        }
    }

    fun requestCsvExport() {
        val currentTransactions = uiState.value.filteredTransactions
        if (currentTransactions.isEmpty()) {
            _events.tryEmit(UiEvent.ShowMessage("Aucun mouvement à exporter.", isError = true))
            return
        }
        val csv = repository.buildTransactionsCsv(currentTransactions)
        val fileName = "kina_karite_gestock_${FrenchFormatters.formatFileTimestamp()}.csv"
        _events.tryEmit(UiEvent.RequestCsvSave(fileName = fileName, csvContent = csv))
    }

    fun notifyCsvExportResult(success: Boolean) {
        if (success) {
            _events.tryEmit(UiEvent.ShowMessage("Fichier CSV exporté avec succès."))
        } else {
            _events.tryEmit(UiEvent.ShowMessage("Erreur lors de l'enregistrement du fichier CSV.", isError = true))
        }
    }

    private fun emitResult(result: OperationResult) {
        when (result) {
            is OperationResult.Success -> _events.tryEmit(UiEvent.ShowMessage(result.message, isError = false))
            is OperationResult.Error -> _events.tryEmit(UiEvent.ShowMessage(result.message, isError = true))
        }
    }

    companion object {
        fun factory(repository: InventoryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InventoryViewModel(repository) as T
                }
            }
    }
}
