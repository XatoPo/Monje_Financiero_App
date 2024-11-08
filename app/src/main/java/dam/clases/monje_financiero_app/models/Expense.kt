package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable
import com.orm.SugarRecord

data class Expense(
    val id: String = "",                     // Unique identifier for the expense
    val userId: String = "",                 // User ID associated with the expense
    var description: String = "",            // Description of the expense
    var amount: Double = 0.0,                // Amount of the expense
    var categoryId: String = "",             // Category ID associated with the expense
    var date: String = "",                   // Date of the expense (YYYY-MM-DD format)
    var isRecurring: Boolean = false         // Indicates if the expense is recurring
) : SugarRecord(), Parcelable {

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

        // CRUD Operations

        // 1. Create (Insert)
        fun createExpense(id: String, userId: String, description: String, amount: Double, categoryId: String, date: String, isRecurring: Boolean): Expense {
            val expense = Expense(id, userId, description, amount, categoryId, date, isRecurring)
            expense.save()  // Save the expense to the database
            return expense
        }

        // 2. Read (Retrieve)

        // Get all expenses
        fun getAllExpenses(): List<Expense> {
            return listAll(Expense::class.java)
        }

        // Find an expense by its ID
        fun getExpenseById(expenseId: Long): Expense? {
            return findById(Expense::class.java, expenseId)
        }

        // Find expenses by userId (Custom Query)
        fun getExpensesByUserId(userId: String): List<Expense> {
            return find(Expense::class.java, "userId = ?", userId)
        }

        // Find expenses by categoryId (Custom Query)
        fun getExpensesByCategoryId(categoryId: String): List<Expense> {
            return find(Expense::class.java, "categoryId = ?", categoryId)
        }

        // 3. Update
        fun updateExpense(expenseId: Long, newDescription: String?, newAmount: Double?, newCategoryId: String?, newDate: String?, newIsRecurring: Boolean?): Boolean {
            val expense = findById(Expense::class.java, expenseId)
            if (expense != null) {
                newDescription?.let { expense.description = it }
                newAmount?.let { expense.amount = it }
                newCategoryId?.let { expense.categoryId = it }
                newDate?.let { expense.date = it }
                newIsRecurring?.let { expense.isRecurring = it }
                expense.save()  // Save the updated expense
                return true
            }
            return false
        }

        // 4. Delete
        fun deleteExpense(expenseId: Long): Boolean {
            val expense = findById(Expense::class.java, expenseId)
            return if (expense != null) {
                expense.delete()  // Delete the expense from the database
                true
            } else {
                false
            }
        }
    }
}