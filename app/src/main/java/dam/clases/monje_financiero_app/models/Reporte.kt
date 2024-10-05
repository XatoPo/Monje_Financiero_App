package dam.clases.monje_financiero_app.models

data class Reporte(
    val id: Int,
    val usuario: Usuario,
    val presupuestos: List<Presupuesto>,
    val totalGastos: Double,
    val totalPresupuestos: Double
)