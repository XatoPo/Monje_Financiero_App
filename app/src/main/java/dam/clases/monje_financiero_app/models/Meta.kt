package dam.clases.monje_financiero_app.models

import com.orm.SugarRecord

data class Meta(
    val id: String = "",                   // Unique identifier for the meta
    val userId: String = "",               // User ID associated with the meta
    var targetAmount: Double = 0.0,        // Target amount for savings or expenses
    var achievedAmount: Double = 0.0,      // Amount achieved towards the target
    var deadline: String = ""              // Deadline for achieving the target (YYYY-MM-DD format)
) : SugarRecord() {

    // CRUD Operations

    // 1. Create (Insert)
    fun createMeta(id: String, userId: String, targetAmount: Double, achievedAmount: Double, deadline: String): Meta {
        val meta = Meta(id, userId, targetAmount, achievedAmount, deadline)
        meta.save()  // Save the meta to the database
        return meta
    }

    // 2. Read (Retrieve)

    // Get all metas
    fun getAllMetas(): List<Meta> {
        return listAll(Meta::class.java)
    }

    // Find a meta by its ID
    fun getMetaById(metaId: Long): Meta? {
        return findById(Meta::class.java, metaId)
    }

    // Find metas by userId (Custom Query)
    fun getMetasByUserId(userId: String): List<Meta> {
        return find(Meta::class.java, "userId = ?", userId)
    }

    // 3. Update
    fun updateMeta(metaId: Long, newTargetAmount: Double?, newAchievedAmount: Double?, newDeadline: String?): Boolean {
        val meta = findById(Meta::class.java, metaId)
        if (meta != null) {
            newTargetAmount?.let { meta.targetAmount = it }
            newAchievedAmount?.let { meta.achievedAmount = it }
            newDeadline?.let { meta.deadline = it }
            meta.save()  // Save the updated meta
            return true
        }
        return false
    }

    // 4. Delete
    fun deleteMeta(metaId: Long): Boolean {
        val meta = findById(Meta::class.java, metaId)
        return if (meta != null) {
            meta.delete()  // Delete the meta from the database
            true
        } else {
            false
        }
    }
}