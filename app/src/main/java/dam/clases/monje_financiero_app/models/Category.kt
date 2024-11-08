package dam.clases.monje_financiero_app.models

import android.os.Parcel
import android.os.Parcelable
import com.orm.SugarRecord

data class Category(
    val id: String = "",                  // Unique identifier for the category
    val userId: String = "",              // User ID associated with the category
    var name: String = "",                // Name of the category
    var color: String = "",               // Color associated with the category
    var iconText: String = ""             // URL of the category icon
) : SugarRecord(), Parcelable {

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

        // CRUD Operations

        // 1. Create (Insert)
        fun createCategory(id: String, userId: String, name: String, color: String, iconText: String): Category {
            val category = Category(id, userId, name, color, iconText)
            category.save()  // Save the category to the database
            return category
        }

        // 2. Read (Retrieve)

        // Get all categories
        fun getAllCategories(): List<Category> {
            return listAll(Category::class.java)
        }

        // Find a category by its ID
        fun getCategoryById(categoryId: Long): Category? {
            return findById(Category::class.java, categoryId)
        }

        // Find categories by userId (Custom Query)
        fun getCategoriesByUserId(userId: String): List<Category> {
            return find(Category::class.java, "userId = ?", userId)
        }

        // 3. Update
        fun updateCategory(categoryId: Long, newName: String?, newColor: String?, newIconText: String?): Boolean {
            val category = findById(Category::class.java, categoryId)
            if (category != null) {
                newName?.let { category.name = it }
                newColor?.let { category.color = it }
                newIconText?.let { category.iconText = it }
                category.save()  // Save the updated category
                return true
            }
            return false
        }

        // 4. Delete
        fun deleteCategory(categoryId: Long): Boolean {
            val category = findById(Category::class.java, categoryId)
            return if (category != null) {
                category.delete()  // Delete the category from the database
                true
            } else {
                false
            }
        }
    }
}