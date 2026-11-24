package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object TransactionType {
    const val ENTREE = "ENTREE"
    const val VENTE = "VENTE"
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val produitId: Long,
    val produitNom: String,
    val unite: String,
    val type: String,
    val quantite: Double,
    val prixUnitaire: Double,
    val fraisAnnexesUnitaire: Double = 0.0,
    val prixRevientSnapshot: Double,
    val montantTotal: Double,
    val beneficeNet: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
