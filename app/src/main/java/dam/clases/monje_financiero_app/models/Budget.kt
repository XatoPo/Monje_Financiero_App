package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable

data class Budget(
    val id: String,                      // Unique identifier for the budget
    val userId: String,                  // User ID associated with the budget
    val name: String,                    // Name of the budget
    val limit: Double,                   // Limit amount of the budget
    val categoryId: String,              // Category ID associated with the budget
    val period: String                    // Budget period (e.g., weekly, monthly, yearly)
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        limit = parcel.readDouble(),
        categoryId = parcel.readString() ?: "",
        period = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(name)
        parcel.writeDouble(limit)
        parcel.writeString(categoryId)
        parcel.writeString(period)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Budget> {
        override fun createFromParcel(parcel: Parcel): Budget {
            return Budget(parcel)
        }

        override fun newArray(size: Int): Array<Budget?> {
            return arrayOfNulls(size)
        }
    }
}