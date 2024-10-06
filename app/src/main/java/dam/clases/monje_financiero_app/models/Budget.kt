package dam.clases.monje_financiero_app.models

data class Budget(
    val id: String,                      // Unique identifier for the budget
    val userId: String,                  // User ID associated with the budget
    val name: String,                    // Name of the budget
    val limit: Double,                   // Limit amount of the budget
    val categoryId: String,              // Category ID associated with the budget
    val period: String                    // Budget period (e.g., weekly, monthly, yearly)
)