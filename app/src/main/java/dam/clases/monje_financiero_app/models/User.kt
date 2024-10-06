package dam.clases.monje_financiero_app.models

data class User(
    val id: String,                      // Unique identifier for the user
    val name: String,                    // Name of the user
    val email: String,                   // User's email address
    val password: String,                // User's password
    val dateOfBirth: String,             // User's date of birth (YYYY-MM-DD format)
    val profileImageUrl: String          // URL of the user's profile image
)