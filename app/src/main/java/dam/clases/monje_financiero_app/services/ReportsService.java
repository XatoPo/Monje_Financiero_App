package dam.clases.monje_financiero_app.services;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportsService {

    private ApiService apiService;

    public ReportsService() {
        this.apiService = new ApiService();
    }

    // Obtener todos los reportes de un usuario
    public void getAllReports(String userId, Callback callback) {
        apiService.get("reports?user_id=" + userId, callback);
    }

    // Generar un reporte
    public void addReport(String userId, String startDate, String endDate, double totalExpenses, JSONObject categoryBreakdown, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("start_date", startDate);
            json.put("end_date", endDate);
            json.put("total_expenses", totalExpenses);
            json.put("category_breakdown", categoryBreakdown);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.post("reports", json.toString(), callback);
    }

    // Eliminar un reporte
    public void deleteReport(String reportId, Callback callback) {
        apiService.delete("reports/" + reportId, callback);
    }

    // Método para obtener los datos del reporte
    public void getReportData(String userId, String startDate, String endDate, Callback callback) {
        String url = "reports/data?user_id=" + userId + "&start_date=" + startDate + "&end_date=" + endDate;
        apiService.get(url, callback);
    }
}