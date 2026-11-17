package com.works.naval.platform

import com.works.naval.domain.model.EstadoTrabajo
import com.works.naval.domain.model.TrabajoItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.JsonPrimitive

expect suspend fun writeOfflineIsometricoPdf(
    isometricoId: Long,
    pdfBytes: ByteArray,
    metadata: String,
)

expect suspend fun readOfflineIsometricoPdf(isometricoId: Long): ByteArray?

expect suspend fun listOfflineIsometricoIds(): List<Long>

expect suspend fun readOfflineIsometricoMetadata(isometricoId: Long): String?

expect suspend fun deleteOfflineIsometricoPdf(isometricoId: Long)

suspend fun saveOfflineIsometricoPdf(item: TrabajoItem, pdfBytes: ByteArray) {
    require(item.isometricoId > 0) { "El identificador del isométrico no es válido" }
    require(pdfBytes.isNotEmpty()) { "El PDF está vacío" }
    writeOfflineIsometricoPdf(item.isometricoId, pdfBytes, item.encodeOfflineMetadata())
}

suspend fun loadOfflineIsometricoTrabajo(isometricoId: Long): TrabajoItem? =
    readOfflineIsometricoMetadata(isometricoId)?.decodeOfflineMetadata()

suspend fun loadOfflineTrabajos(): List<TrabajoItem> =
    listOfflineIsometricoIds().map { isometricoId ->
        loadOfflineIsometricoTrabajo(isometricoId)
            ?: error("Faltan los datos del isométrico guardado $isometricoId")
    }

private fun TrabajoItem.encodeOfflineMetadata(): String = buildJsonObject {
    put("id", JsonPrimitive(id))
    put("isometricoId", JsonPrimitive(isometricoId))
    put("codigoIsometrico", JsonPrimitive(codigoIsometrico))
    put("bloque", JsonPrimitive(bloque))
    put("estado", JsonPrimitive(estado.name))
    put("operarioIds", buildJsonArray { operarioIds.forEach { add(JsonPrimitive(it)) } })
}.toString()

private fun String.decodeOfflineMetadata(): TrabajoItem {
    val json = Json.parseToJsonElement(this).jsonObject
    val estado = json["estado"]?.jsonPrimitive?.content
        ?.let { name -> EstadoTrabajo.entries.firstOrNull { it.name == name } }
        ?: error("Estado no válido en los datos del isométrico guardado")

    return TrabajoItem(
        id = json["id"]?.jsonPrimitive?.longOrNull
            ?: error("Falta el identificador del trabajo guardado"),
        isometricoId = json["isometricoId"]?.jsonPrimitive?.longOrNull
            ?: error("Falta el identificador del isométrico guardado"),
        codigoIsometrico = json["codigoIsometrico"]?.jsonPrimitive?.content
            ?: error("Falta el código del isométrico guardado"),
        bloque = json["bloque"]?.jsonPrimitive?.content
            ?: error("Falta el bloque del isométrico guardado"),
        estado = estado,
        operarioIds = json["operarioIds"]?.jsonArray?.map { element ->
            element.jsonPrimitive.longOrNull
                ?: error("Hay un operario no válido en los datos guardados")
        } ?: error("Faltan los operarios del trabajo guardado"),
    )
}
