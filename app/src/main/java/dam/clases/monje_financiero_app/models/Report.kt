package dam.clases.monje_financiero_app.models

data class Report(
    val id: String,                      // Unique identifier for the report
    val userId: String,                  // User ID associated with the report
    val startDate: String,               // Start date of the report (YYYY-MM-DD format)
    val endDate: String,                 // End date of the report (YYYY-MM-DD format)
    val totalExpenses: Double,           // Total expenses in the report
    val categoryBreakdown: Map<String, Double> // Breakdown of expenses by category
)