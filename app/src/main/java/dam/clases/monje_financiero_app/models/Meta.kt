package dam.clases.monje_financiero_app.models

import java.time.LocalDate

data class Meta(
    val id: Int,
    val nombre: String,
    val montoObjetivo: Double,
    val montoActual: Double,
    val fechaObjetivo: LocalDate
)