package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable

data class Expense(
    val id: String,                      // Unique identifier for the expense
    val userId: String,                  // User ID associated with the expense
    val description: String,              // Description of the expense
    val amount: Double,                  // Amount of the expense
    val categoryId: String,              // Category ID associated with the expense
    val date: String,                    // Date of the expense (YYYY-MM-DD format)
    val isRecurring: Boolean              // Indicates if the expense is recurring
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        amount = parcel.readDouble(),
        categoryId = parcel.readString() ?: "",
        date = parcel.readString() ?: "",
        isRecurring = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(description)
        parcel.writeDouble(amount)
        parcel.writeString(categoryId)
        parcel.writeString(date)
        parcel.writeByte(if (isRecurring) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}
