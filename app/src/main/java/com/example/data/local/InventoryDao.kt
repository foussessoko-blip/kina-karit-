package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlin.math.round

sealed interface OperationResult {
    data class Success(val message: String) : OperationResult
    data class Error(val message: String) : OperationResult
}

@Dao
interface InventoryDao {

    @Query("SELECT * FROM products ORDER BY nom COLLATE NOCASE ASC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Transaction
    suspend fun recordRestock(
        produitId: Long,
        quantiteAjoutee: Double,
        prixAchatLot: Double,
        fraisAnnexesLot: Double,
        note: String
    ): OperationResult {
        if (quantiteAjoutee <= 0.0) {
            return OperationResult.Error("La quantité approvisionnée doit être supérieure à zéro.")
        }
        if (prixAchatLot < 0.0 || fraisAnnexesLot < 0.0) {
            return OperationResult.Error("Les coûts ne peuvent pas être négatifs.")
        }
        val product = getProductById(produitId)
            ?: return OperationResult.Error("Produit introuvable dans le catalogue.")

        val currentStock = maxOf(0.0, product.quantiteStock)
        val newStock = round2(currentStock + quantiteAjoutee)

        // Coût Moyen Pondéré (CMP) recalculation
        val newPurchasePrice = if (newStock > 0.0) {
            round2(((currentStock * product.prixAchatUnitaire) + (quantiteAjoutee * prixAchatLot)) / newStock)
        } else {
            round2(prixAchatLot)
        }
        val newAncillaryFees = if (newStock > 0.0) {
            round2(((currentStock * product.fraisAnnexesUnitaire) + (quantiteAjoutee * fraisAnnexesLot)) / newStock)
        } else {
            round2(fraisAnnexesLot)
        }
        val newCmp = round2(newPurchasePrice + newAncillaryFees)
        val batchUnitCost = round2(prixAchatLot + fraisAnnexesLot)
        val totalCost = round2(quantiteAjoutee * batchUnitCost)
        val now = System.currentTimeMillis()

        val updated = product.copy(
            quantiteStock = newStock,
            prixAchatUnitaire = newPurchasePrice,
            fraisAnnexesUnitaire = newAncillaryFees,
            dateMiseAJour = now
        )
        updateProduct(updated)

        insertTransaction(
            TransactionEntity(
                produitId = product.id,
                produitNom = product.nom,
                unite = product.unite,
                type = TransactionType.ENTREE,
                quantite = round2(quantiteAjoutee),
                prixUnitaire = batchUnitCost,
                fraisAnnexesUnitaire = round2(fraisAnnexesLot),
                prixRevientSnapshot = newCmp,
                montantTotal = totalCost,
                beneficeNet = 0.0,
                note = note.trim(),
                timestamp = now
            )
        )
        return OperationResult.Success(
            "Approvisionnement enregistré : +${formatCompact(quantiteAjoutee)} ${product.unite} de ${product.nom}."
        )
    }

    @Transaction
    suspend fun recordSale(
        produitId: Long,
        quantiteVendue: Double,
        prixVenteUnitaire: Double,
        note: String
    ): OperationResult {
        if (quantiteVendue <= 0.0) {
            return OperationResult.Error("La quantité vendue doit être supérieure à zéro.")
        }
        if (prixVenteUnitaire < 0.0) {
            return OperationResult.Error("Le prix de vente ne peut pas être négatif.")
        }
        val product = getProductById(produitId)
            ?: return OperationResult.Error("Produit introuvable dans le catalogue.")

        if (product.quantiteStock <= 0.0) {
            return OperationResult.Error(
                "Rupture de stock pour ${product.nom}. Veuillez réapprovisionner avant d'enregistrer une vente."
            )
        }
        if (quantiteVendue > product.quantiteStock + 1e-6) {
            return OperationResult.Error(
                "Stock insuffisant ! Quantité demandée (${formatCompact(quantiteVendue)} ${product.unite}) supérieure au stock disponible (${formatCompact(product.quantiteStock)} ${product.unite})."
            )
        }

        val remainingStock = maxOf(0.0, round2(product.quantiteStock - quantiteVendue))
        val costPrice = product.prixRevientUnitaire
        val totalRevenue = round2(quantiteVendue * prixVenteUnitaire)
        val netProfit = round2((prixVenteUnitaire - costPrice) * quantiteVendue)
        val now = System.currentTimeMillis()

        val updated = product.copy(
            quantiteStock = remainingStock,
            dateMiseAJour = now
        )
        updateProduct(updated)

        insertTransaction(
            TransactionEntity(
                produitId = product.id,
                produitNom = product.nom,
                unite = product.unite,
                type = TransactionType.VENTE,
                quantite = round2(quantiteVendue),
                prixUnitaire = round2(prixVenteUnitaire),
                fraisAnnexesUnitaire = 0.0,
                prixRevientSnapshot = round2(costPrice),
                montantTotal = totalRevenue,
                beneficeNet = netProfit,
                note = note.trim(),
                timestamp = now
            )
        )
        return OperationResult.Success(
            "Vente enregistrée : ${formatCompact(quantiteVendue)} ${product.unite} de ${product.nom}."
        )
    }

    private fun round2(value: Double): Double = round(value * 100.0) / 100.0

    private fun formatCompact(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString().replace('.', ',')
    }
}
