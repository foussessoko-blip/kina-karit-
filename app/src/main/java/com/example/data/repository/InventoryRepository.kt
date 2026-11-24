package com.example.data.repository

import com.example.data.local.InventoryDao
import com.example.data.local.OperationResult
import com.example.data.local.ProductEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.util.FrenchFormatters
import kotlinx.coroutines.flow.Flow
import kotlin.math.round

class InventoryRepository(private val dao: InventoryDao) {

    val products: Flow<List<ProductEntity>> = dao.observeAllProducts()
    val transactions: Flow<List<TransactionEntity>> = dao.observeAllTransactions()

    suspend fun ensureSeeded() {
        if (dao.getProductCount() > 0 || dao.getTransactionCount() > 0) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 24 * hour

        val initialProducts = listOf(
            ProductEntity(
                nom = "Beurre de karité",
                unite = "kg",
                quantiteStock = 28.0,
                seuilAlerte = 8.0,
                prixAchatUnitaire = 2200.0,
                fraisAnnexesUnitaire = 300.0,
                prixVenteUnitaire = 4000.0,
                dateMiseAJour = now - 2 * day
            ),
            ProductEntity(
                nom = "Miel",
                unite = "L",
                quantiteStock = 18.0,
                seuilAlerte = 6.0,
                prixAchatUnitaire = 3500.0,
                fraisAnnexesUnitaire = 500.0,
                prixVenteUnitaire = 6000.0,
                dateMiseAJour = now - 2 * day
            ),
            ProductEntity(
                nom = "Poudre de cacao",
                unite = "kg",
                quantiteStock = 15.0,
                seuilAlerte = 5.0,
                prixAchatUnitaire = 2800.0,
                fraisAnnexesUnitaire = 400.0,
                prixVenteUnitaire = 5000.0,
                dateMiseAJour = now - 3 * day
            ),
            ProductEntity(
                nom = "Huile rouge",
                unite = "bidons",
                quantiteStock = 14.0,
                seuilAlerte = 5.0,
                prixAchatUnitaire = 6500.0,
                fraisAnnexesUnitaire = 700.0,
                prixVenteUnitaire = 9500.0,
                dateMiseAJour = now - 1 * day
            ),
            ProductEntity(
                nom = "Huile de palmiste",
                unite = "L",
                quantiteStock = 11.0,
                seuilAlerte = 5.0,
                prixAchatUnitaire = 1800.0,
                fraisAnnexesUnitaire = 250.0,
                prixVenteUnitaire = 3200.0,
                dateMiseAJour = now - 2 * day
            ),
            ProductEntity(
                nom = "Huile de coco",
                unite = "L",
                quantiteStock = 4.0,
                seuilAlerte = 5.0,
                prixAchatUnitaire = 4200.0,
                fraisAnnexesUnitaire = 500.0,
                prixVenteUnitaire = 7000.0,
                dateMiseAJour = now - 1 * day
            ),
            ProductEntity(
                nom = "Soumara moulu",
                unite = "pots",
                quantiteStock = 32.0,
                seuilAlerte = 10.0,
                prixAchatUnitaire = 900.0,
                fraisAnnexesUnitaire = 150.0,
                prixVenteUnitaire = 1750.0,
                dateMiseAJour = now - 3 * day
            ),
            ProductEntity(
                nom = "Pâte d'arachide",
                unite = "pots",
                quantiteStock = 24.0,
                seuilAlerte = 8.0,
                prixAchatUnitaire = 1400.0,
                fraisAnnexesUnitaire = 200.0,
                prixVenteUnitaire = 2500.0,
                dateMiseAJour = now - 1 * day
            ),
            ProductEntity(
                nom = "Beurre de cacao",
                unite = "kg",
                quantiteStock = 3.0,
                seuilAlerte = 5.0,
                prixAchatUnitaire = 4800.0,
                fraisAnnexesUnitaire = 600.0,
                prixVenteUnitaire = 8000.0,
                dateMiseAJour = now - 1 * day
            )
        )

        val insertedIds = dao.insertProducts(initialProducts)
        val byName = initialProducts.mapIndexed { index, product ->
            product.nom to product.copy(id = insertedIds[index])
        }.toMap()

        val initialTransactions = buildList {
            byName["Beurre de karité"]?.let { p ->
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.ENTREE,
                        quantite = 35.0,
                        prixUnitaire = p.prixRevientUnitaire,
                        fraisAnnexesUnitaire = p.fraisAnnexesUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 35.0 * p.prixRevientUnitaire,
                        beneficeNet = 0.0,
                        note = "Coopérative de Korhogo — Stock initial",
                        timestamp = now - 5 * day
                    )
                )
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.VENTE,
                        quantite = 7.0,
                        prixUnitaire = p.prixVenteUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 7.0 * p.prixVenteUnitaire,
                        beneficeNet = 7.0 * p.beneficeUnitaire,
                        note = "Boutique Bio Plateau",
                        timestamp = now - 18 * hour
                    )
                )
            }
            byName["Miel"]?.let { p ->
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.ENTREE,
                        quantite = 24.0,
                        prixUnitaire = p.prixRevientUnitaire,
                        fraisAnnexesUnitaire = p.fraisAnnexesUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 24.0 * p.prixRevientUnitaire,
                        beneficeNet = 0.0,
                        note = "Apiculteur Katiola",
                        timestamp = now - 4 * day
                    )
                )
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.VENTE,
                        quantite = 6.0,
                        prixUnitaire = p.prixVenteUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 6.0 * p.prixVenteUnitaire,
                        beneficeNet = 6.0 * p.beneficeUnitaire,
                        note = "Vente comptoir",
                        timestamp = now - 8 * hour
                    )
                )
            }
            byName["Huile rouge"]?.let { p ->
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.VENTE,
                        quantite = 4.0,
                        prixUnitaire = p.prixVenteUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 4.0 * p.prixVenteUnitaire,
                        beneficeNet = 4.0 * p.beneficeUnitaire,
                        note = "Restaurant Saveurs d'Or",
                        timestamp = now - 26 * hour
                    )
                )
            }
            byName["Huile de coco"]?.let { p ->
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.VENTE,
                        quantite = 5.0,
                        prixUnitaire = p.prixVenteUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 5.0 * p.prixVenteUnitaire,
                        beneficeNet = 5.0 * p.beneficeUnitaire,
                        note = "Commande cosmétique naturelle",
                        timestamp = now - 5 * hour
                    )
                )
            }
            byName["Pâte d'arachide"]?.let { p ->
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.VENTE,
                        quantite = 10.0,
                        prixUnitaire = p.prixVenteUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 10.0 * p.prixVenteUnitaire,
                        beneficeNet = 10.0 * p.beneficeUnitaire,
                        note = "Épicerie Cocody",
                        timestamp = now - 3 * hour
                    )
                )
            }
            byName["Beurre de cacao"]?.let { p ->
                add(
                    TransactionEntity(
                        produitId = p.id,
                        produitNom = p.nom,
                        unite = p.unite,
                        type = TransactionType.VENTE,
                        quantite = 3.0,
                        prixUnitaire = p.prixVenteUnitaire,
                        prixRevientSnapshot = p.prixRevientUnitaire,
                        montantTotal = 3.0 * p.prixVenteUnitaire,
                        beneficeNet = 3.0 * p.beneficeUnitaire,
                        note = "Atelier artisanal",
                        timestamp = now - 2 * hour
                    )
                )
            }
        }
        dao.insertTransactions(initialTransactions)
    }

    suspend fun saveProduct(
        id: Long?,
        nom: String,
        unite: String,
        quantiteStock: Double,
        seuilAlerte: Double,
        prixAchatUnitaire: Double,
        fraisAnnexesUnitaire: Double,
        prixVenteUnitaire: Double
    ): OperationResult {
        val trimmedName = nom.trim()
        val trimmedUnit = unite.trim()
        if (trimmedName.isEmpty()) {
            return OperationResult.Error("Veuillez saisir le nom du produit.")
        }
        if (trimmedUnit.isEmpty()) {
            return OperationResult.Error("Veuillez indiquer l'unité de mesure.")
        }
        if (quantiteStock < 0.0 || seuilAlerte < 0.0 || prixAchatUnitaire < 0.0 || fraisAnnexesUnitaire < 0.0 || prixVenteUnitaire < 0.0) {
            return OperationResult.Error("Les quantités et prix ne peuvent pas être négatifs.")
        }

        val entity = ProductEntity(
            id = id ?: 0L,
            nom = trimmedName,
            unite = trimmedUnit,
            quantiteStock = round2(quantiteStock),
            seuilAlerte = round2(seuilAlerte),
            prixAchatUnitaire = round2(prixAchatUnitaire),
            fraisAnnexesUnitaire = round2(fraisAnnexesUnitaire),
            prixVenteUnitaire = round2(prixVenteUnitaire),
            dateMiseAJour = System.currentTimeMillis()
        )
        if (id == null || id == 0L) {
            dao.insertProduct(entity)
            return OperationResult.Success("Produit « $trimmedName » ajouté au catalogue.")
        } else {
            dao.updateProduct(entity)
            return OperationResult.Success("Produit « $trimmedName » mis à jour.")
        }
    }

    suspend fun deleteProduct(product: ProductEntity): OperationResult {
        dao.deleteProduct(product)
        return OperationResult.Success("Produit « ${product.nom} » supprimé.")
    }

    suspend fun recordRestock(
        produitId: Long,
        quantiteAjoutee: Double,
        prixAchatLot: Double,
        fraisAnnexesLot: Double,
        note: String
    ): OperationResult {
        return dao.recordRestock(produitId, quantiteAjoutee, prixAchatLot, fraisAnnexesLot, note)
    }

    suspend fun recordSale(
        produitId: Long,
        quantiteVendue: Double,
        prixVenteUnitaire: Double,
        note: String
    ): OperationResult {
        return dao.recordSale(produitId, quantiteVendue, prixVenteUnitaire, note)
    }

    fun buildTransactionsCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        // UTF-8 BOM for proper French accent display in Excel / Sheets
        sb.append('\uFEFF')
        sb.appendLine("Date;Type;Produit;Quantite;Unite;Prix unitaire (FCFA);Prix de revient CMP (FCFA);Montant total (FCFA);Benefice net (FCFA);Note")
        for (tx in transactions) {
            val typeLabel = if (tx.type == TransactionType.VENTE) "Vente" else "Entrée"
            val row = listOf(
                FrenchFormatters.formatDateShort(tx.timestamp),
                typeLabel,
                tx.produitNom,
                FrenchFormatters.formatQuantity(tx.quantite),
                tx.unite,
                tx.prixUnitaire.toLong().toString(),
                tx.prixRevientSnapshot.toLong().toString(),
                tx.montantTotal.toLong().toString(),
                tx.beneficeNet.toLong().toString(),
                tx.note
            ).joinToString(";") { escapeCsv(it) }
            sb.appendLine(row)
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(';') || value.contains('"') || value.contains('\n') || value.contains('\r')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private fun round2(value: Double): Double = round(value * 100.0) / 100.0
}
