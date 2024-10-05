package dam.clases.monje_financiero_app.models

import android.graphics.drawable.Drawable

data class Categoria(
    val id: Int,
    val nombre: String,
    val icono: Drawable,
    val presupuestos: List<Presupuesto>
)
