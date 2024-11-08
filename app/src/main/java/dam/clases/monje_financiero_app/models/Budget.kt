package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable
import com.orm.SugarRecord

data class Budget(
    val id: String = "",                  // Unique identifier for the budget
    val userId: String = "",              // User ID associated with the budget
    var name: String = "",                // Name of the budget
    var limit: Double = 0.0,              // Limit amount of the budget
    var categoryId: String = "",          // Category ID associated with the budget
    var period: String = ""               // Budget period (e.g., weekly, monthly, yearly)
) : SugarRecord(), Parcelable {

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

        // CRUD Operations

        // 1. Create (Insert)
        fun createBudget(id: String, userId: String, name: String, limit: Double, categoryId: String, period: String): Budget {
            val budget = Budget(id, userId, name, limit, categoryId, period)
            budget.save()  // Save the budget to the database
            return budget
        }

        // 2. Read (Retrieve)

        // Get all budgets
        fun getAllBudgets(): List<Budget> {
            return listAll(Budget::class.java)
        }

        // Find a budget by its ID
        fun getBudgetById(budgetId: Long): Budget? {
            return findById(Budget::class.java, budgetId)
        }

        // Find budgets by userId and/or period (Custom Query)
        fun getBudgetsByUserIdAndPeriod(userId: String, period: String): List<Budget> {
            return find(Budget::class.java, "userId = ? AND period = ?", userId, period)
        }

        // 3. Update
        fun updateBudget(budgetId: Long, newName: String?, newLimit: Double?, newCategoryId: String?, newPeriod: String?): Boolean {
            val budget = findById(Budget::class.java, budgetId)
            if (budget != null) {
                newName?.let { budget.name = it }
                newLimit?.let { budget.limit = it }
                newCategoryId?.let { budget.categoryId = it }
                newPeriod?.let { budget.period = it }
                budget.save()  // Save the updated budget
                return true
            }
            return false
        }

        // 4. Delete
        fun deleteBudget(budgetId: Long): Boolean {
            val budget = findById(Budget::class.java, budgetId)
            return if (budget != null) {
                budget.delete()  // Delete the budget from the database
                true
            } else {
                false
            }
        }
    }
}