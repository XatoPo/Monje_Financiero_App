package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable
import com.orm.SugarRecord

data class User(
    val id: String = "",                   // Unique identifier for the user
    var name: String = "",                 // Name of the user
    var email: String = "",                // User's email address
    var password: String = "",             // User's password
    var dateOfBirth: String = "",          // User's date of birth (YYYY-MM-DD format)
    var profileImageUrl: String = ""       // URL of the user's profile image
) : Parcelable, SugarRecord() { // Inherit from SugarRecord to make this a database model

    // Constructor to initialize the User object from a Parcel (for Parcelable)
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        password = parcel.readString() ?: "",
        dateOfBirth = parcel.readString() ?: "",
        profileImageUrl = parcel.readString() ?: ""
    )

    // Write the object to a Parcel (for Parcelable)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(dateOfBirth)
        parcel.writeString(profileImageUrl)
    }

    // Describe contents for Parcelable
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to create and restore the User object from a Parcel (Parcelable)
    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

    // CRUD Operations for User

    // 1. Create (Insert)
    fun createUser(id: String, name: String, email: String, password: String, dateOfBirth: String, profileImageUrl: String): User {
        val user = User(id, name, email, password, dateOfBirth, profileImageUrl)
        user.save()  // Save the user to the database
        return user
    }

    // 2. Read (Retrieve)

    // Get all users
    fun getAllUsers(): List<User> {
        return listAll(User::class.java)
    }

    // Find a user by ID
    fun getUserById(userId: Long): User? {
        return findById(User::class.java, userId)
    }

    // Find a user by email (e.g., to check if the user already exists or for login)
    fun getUserByEmail(email: String): List<User> {
        return find(User::class.java, "email = ?", email)
    }

    // 3. Update
    fun updateUser(userId: Long, newName: String?, newEmail: String?, newPassword: String?, newDateOfBirth: String?, newProfileImageUrl: String?): Boolean {
        val user = findById(User::class.java, userId)
        if (user != null) {
            newName?.let { user.name = it }
            newEmail?.let { user.email = it }
            newPassword?.let { user.password = it }
            newDateOfBirth?.let { user.dateOfBirth = it }
            newProfileImageUrl?.let { user.profileImageUrl = it }
            user.save()  // Save the updated user
            return true
        }
        return false
    }

    // 4. Delete
    fun deleteUser(userId: Long): Boolean {
        val user = findById(User::class.java, userId)
        return if (user != null) {
            user.delete()  // Delete the user from the database
            true
        } else {
            false
        }
    }
}