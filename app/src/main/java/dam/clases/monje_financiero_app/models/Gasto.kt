package dam.clases.monje_financiero_app.models

import java.time.LocalDate

data class Gasto(
    val id: Int,
    val monto: Double,
    val fecha: LocalDate,
    val categoria: Categoria,
    val descripcion: String?
)
