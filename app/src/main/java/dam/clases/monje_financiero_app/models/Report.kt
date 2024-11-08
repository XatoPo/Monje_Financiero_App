package dam.clases.monje_financiero_app.models

import com.orm.SugarRecord

data class Report(
    val id: String = "",                   // Unique identifier for the report
    val userId: String = "",               // User ID associated with the report
    var startDate: String = "",            // Start date of the report (YYYY-MM-DD format)
    var endDate: String = "",              // End date of the report (YYYY-MM-DD format)
    var totalExpenses: Double = 0.0,       // Total expenses in the report
    var categoryBreakdown: Map<String, Double> = mapOf()  // Breakdown of expenses by category
) : SugarRecord() {

    // CRUD Operations

    // 1. Create (Insert)
    fun createReport(id: String, userId: String, startDate: String, endDate: String, totalExpenses: Double, categoryBreakdown: Map<String, Double>): Report {
        val report = Report(id, userId, startDate, endDate, totalExpenses, categoryBreakdown)
        report.save()  // Save the report to the database
        return report
    }

    // 2. Read (Retrieve)

    // Get all reports
    fun getAllReports(): List<Report> {
        return listAll(Report::class.java)
    }

    // Find a report by its ID
    fun getReportById(reportId: Long): Report? {
        return findById(Report::class.java, reportId)
    }

    // Find reports by userId (Custom Query)
    fun getReportsByUserId(userId: String): List<Report> {
        return find(Report::class.java, "userId = ?", userId)
    }

    // 3. Update
    fun updateReport(reportId: Long, newStartDate: String?, newEndDate: String?, newTotalExpenses: Double?, newCategoryBreakdown: Map<String, Double>?): Boolean {
        val report = findById(Report::class.java, reportId)
        if (report != null) {
            newStartDate?.let { report.startDate = it }
            newEndDate?.let { report.endDate = it }
            newTotalExpenses?.let { report.totalExpenses = it }
            newCategoryBreakdown?.let { report.categoryBreakdown = it }
            report.save()  // Save the updated report
            return true
        }
        return false
    }

    // 4. Delete
    fun deleteReport(reportId: Long): Boolean {
        val report = findById(Report::class.java, reportId)
        return if (report != null) {
            report.delete()  // Delete the report from the database
            true
        } else {
            false
        }
    }
}