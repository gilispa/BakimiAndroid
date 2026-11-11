package com.bakimi.data.inventario

import com.bakimi.domain.inventario.InventarioRepository
import com.bakimi.domain.inventario.ItemInventario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventarioRepositoryRoom @Inject constructor(
    private val dao: InventarioDao,
) : InventarioRepository {

    override fun observarTodos(): Flow<List<ItemInventario>> =
        dao.observarTodos().map { filas -> filas.map { it.aDominio() } }

    override suspend fun obtener(id: Long): ItemInventario? = dao.obtener(id)?.aDominio()

    override suspend fun guardar(item: ItemInventario): Long {
        val idInsertado = dao.guardar(item.aEntidad())
        return if (item.id == 0L) idInsertado else item.id
    }

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
