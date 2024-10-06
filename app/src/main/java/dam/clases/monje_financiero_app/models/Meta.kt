package dam.clases.monje_financiero_app.models

data class Meta(
    val id: String,                      // Unique identifier for the meta
    val userId: String,                  // User ID associated with the meta
    val targetAmount: Double,            // Target amount for savings or expenses
    val achievedAmount: Double,          // Amount achieved towards the target
    val deadline: String                  // Deadline for achieving the target (YYYY-MM-DD format)
)