package dam.clases.monje_financiero_app.models

data class Category(
    val id: String,                      // Unique identifier for the category
    val userId: String,                  // User ID associated with the category
    val name: String,                    // Name of the category
    val color: String,                   // Color associated with the category
    val iconText: String                 // URL of the category icon
)