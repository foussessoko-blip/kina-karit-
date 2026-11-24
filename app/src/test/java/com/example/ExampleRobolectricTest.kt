package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.OperationResult
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: InventoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InventoryRepository(database.inventoryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kina Karité gestock", appName)
    }

    @Test
    fun `seeding loads 9 agribusiness products in French`() = runTest {
        repository.ensureSeeded()
        val products = repository.products.first()
        assertEquals(9, products.size)
        assertTrue(products.any { it.nom == "Beurre de karité" })
        assertTrue(products.any { it.nom == "Soumara moulu" })
        assertTrue(products.any { it.nom == "Beurre de cacao" })
    }

    @Test
    fun `restocking recalculates weighted average cost CMP accurately`() = runTest {
        repository.ensureSeeded()
        val karite = repository.products.first().first { it.nom == "Beurre de karité" }
        // Initial: 28 kg @ 2200 achat + 300 frais = 2500 revient
        val result = repository.recordRestock(
            produitId = karite.id,
            quantiteAjoutee = 12.0,
            prixAchatLot = 2700.0,
            fraisAnnexesLot = 300.0,
            note = "Lot test"
        )
        assertTrue(result is OperationResult.Success)
        val updated = repository.products.first().first { it.id == karite.id }
        assertEquals(40.0, updated.quantiteStock, 0.01)
        // Expected CMP = (28*2500 + 12*3000) / 40 = (70000 + 36000) / 40 = 2650
        assertEquals(2650.0, updated.prixRevientUnitaire, 0.01)
    }

    @Test
    fun `sale blocks when requested quantity exceeds available stock`() = runTest {
        repository.ensureSeeded()
        val coco = repository.products.first().first { it.nom == "Huile de coco" }
        // Initial stock is 4.0 L
        val blocked = repository.recordSale(
            produitId = coco.id,
            quantiteVendue = 10.0,
            prixVenteUnitaire = coco.prixVenteUnitaire,
            note = "Test blocage"
        )
        assertTrue(blocked is OperationResult.Error)
        assertTrue((blocked as OperationResult.Error).message.contains("Stock insuffisant"))

        val allowed = repository.recordSale(
            produitId = coco.id,
            quantiteVendue = 2.0,
            prixVenteUnitaire = 7000.0,
            note = "Vente valide"
        )
        assertTrue(allowed is OperationResult.Success)
        val updated = repository.products.first().first { it.id == coco.id }
        assertEquals(2.0, updated.quantiteStock, 0.01)
    }
}
