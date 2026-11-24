package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StockStatus {
    EN_STOCK,
    STOCK_BAS,
    RUPTURE
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nom: String,
    val unite: String,
    val quantiteStock: Double,
    val seuilAlerte: Double,
    val prixAchatUnitaire: Double,
    val fraisAnnexesUnitaire: Double,
    val prixVenteUnitaire: Double,
    val dateMiseAJour: Long = System.currentTimeMillis()
) {
    val prixRevientUnitaire: Double
        get() = prixAchatUnitaire + fraisAnnexesUnitaire

    val beneficeUnitaire: Double
        get() = prixVenteUnitaire - prixRevientUnitaire

    val margePourcentage: Double
        get() = if (prixVenteUnitaire > 0.0) {
            (beneficeUnitaire / prixVenteUnitaire) * 100.0
        } else {
            0.0
        }

    val valeurStockRevient: Double
        get() = quantiteStock * prixRevientUnitaire

    val statutStock: StockStatus
        get() = when {
            quantiteStock <= 0.0 -> StockStatus.RUPTURE
            quantiteStock <= seuilAlerte -> StockStatus.STOCK_BAS
            else -> StockStatus.EN_STOCK
        }
}
