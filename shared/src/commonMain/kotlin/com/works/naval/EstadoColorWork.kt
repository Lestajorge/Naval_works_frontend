package com.works.naval

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class EstadoColors(
    val background: Color,
    val content: Color,
)

internal fun estadoColors(estado: EstadoTrabajo): EstadoColors =
    when (estado) {
        EstadoTrabajo.PENDIENTE -> EstadoColors(Color(0xFF9E9E9E), Color(0xFF212121))
        EstadoTrabajo.EN_MONTAJE -> EstadoColors(Color(0xFFFFE0B2), Color(0xFFE65100))
        EstadoTrabajo.MONTADO -> EstadoColors(Color(0xFFE1BEE7), Color(0xFF4A148C))
        EstadoTrabajo.SOLDADO -> EstadoColors(Color(0xFFBBDEFB), Color(0xFF0D47A1))
        EstadoTrabajo.REVISADO -> EstadoColors(Color(0xFFC8E6C9), Color(0xFF1B5E20))
    }

@Composable
fun EstadoColorWork(estado: EstadoTrabajo) {
    val colors = estadoColors(estado)

    Surface(
        color = colors.background,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = estado.descripcion,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.content,
            fontWeight = FontWeight.Bold
        )
    }
}