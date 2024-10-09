package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: String,                      // Unique identifier for the user
    val name: String,                    // Name of the user
    val email: String,                   // User's email address
    val password: String,                // User's password
    val dateOfBirth: String,             // User's date of birth (YYYY-MM-DD format)
    val profileImageUrl: String          // URL of the user's profile image
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        password = parcel.readString() ?: "",
        dateOfBirth = parcel.readString() ?: "",
        profileImageUrl = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(dateOfBirth)
        parcel.writeString(profileImageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}