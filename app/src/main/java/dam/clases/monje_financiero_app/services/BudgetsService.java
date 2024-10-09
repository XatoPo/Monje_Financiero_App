package dam.clases.monje_financiero_app.services;

import android.content.Context;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BudgetsService {

    private ApiService apiService;

    public BudgetsService(Context context) {
        this.apiService = new ApiService();
    }

    // Método para agregar un presupuesto
    public void addBudget(String userId, String name, double budgetLimit, String categoryId, String period, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("name", name);
            json.put("budget_limit", budgetLimit);
            json.put("category_id", categoryId);
            json.put("period", period);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.post("budgets", json.toString(), callback);
    }

    // Método para obtener un presupuesto por ID
    public void getBudget(String budgetId, Callback callback) {
        apiService.get("budgets/" + budgetId, callback);
    }

    // Método para obtener todos los presupuestos
    public void getAllBudgets(Callback callback) {
        apiService.get("budgets", callback);
    }

    // Método para actualizar un presupuesto
    public void updateBudget(String budgetId, String name, double budgetLimit, String categoryId, String period, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("budget_limit", budgetLimit);
            json.put("category_id", categoryId);
            json.put("period", period);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.put("budgets/" + budgetId, json.toString(), callback);
    }

    // Método para eliminar un presupuesto
    public void deleteBudget(String budgetId, Callback callback) {
        apiService.delete("budgets/" + budgetId, callback);
    }
}