package dam.clases.monje_financiero_app.models

data class Expense(
    val id: String,                      // Unique identifier for the expense
    val userId: String,                  // User ID associated with the expense
    val description: String,              // Description of the expense
    val amount: Double,                  // Amount of the expense
    val categoryId: String,              // Category ID associated with the expense
    val date: String,                    // Date of the expense (YYYY-MM-DD format)
    val isRecurring: Boolean              // Indicates if the expense is recurring
)