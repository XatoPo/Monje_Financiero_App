package dam.clases.monje_financiero_app.services;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ExpensesService {

    private ApiService apiService;

    public ExpensesService(Context context) {
        this.apiService = new ApiService();
    }

    // Método para agregar un gasto
    public void addExpense(String userId, String description, double amount, String categoryId, String date, boolean isRecurring, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("description", description);
            json.put("amount", amount);
            json.put("category_id", categoryId);
            json.put("date", date);
            json.put("is_recurring", isRecurring);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Asegúrate de que el endpoint sea correcto
        apiService.post("expenses", json.toString(), callback);
    }

    // Método para obtener un gasto por ID
    public void getExpense(String expenseId, Callback callback) {
        apiService.get("expenses/" + expenseId, callback);
    }

    // Método para obtener todos los gastos
    public void getAllExpenses(String userId, Callback callback) {
        apiService.get("expenses?user_id=" + userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
            }
        });
    }

    // Método para actualizar un gasto
    public void updateExpense(String expenseId, String description, double amount, String categoryId, String date, boolean isRecurring, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("description", description);
            json.put("amount", amount);
            json.put("category_id", categoryId);
            json.put("date", date);
            json.put("is_recurring", isRecurring);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Asegúrate de que el endpoint sea correcto
        apiService.put("expenses/" + expenseId, json.toString(), callback);
    }

    // Método para eliminar un gasto
    public void deleteExpense(String expenseId, Callback callback) {
        apiService.delete("expenses/" + expenseId, callback);
    }
}