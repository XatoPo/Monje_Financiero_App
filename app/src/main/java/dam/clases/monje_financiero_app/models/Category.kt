package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable

data class Category(
    val id: String,                      // Unique identifier for the category
    val userId: String,                  // User ID associated with the category
    val name: String,                    // Name of the category
    val color: String,                   // Color associated with the category
    val iconText: String                 // URL of the category icon
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        color = parcel.readString() ?: "",
        iconText = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(name)
        parcel.writeString(color)
        parcel.writeString(iconText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}