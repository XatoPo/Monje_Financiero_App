package dam.clases.monje_financiero_app.services;

import android.content.Context;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CategoriesService {
    private ApiService apiService;

    public CategoriesService(Context context) {
        this.apiService = new ApiService();
    }

    public void addCategory(String userId, String name, String color, String iconText, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("name", name);
            json.put("color", color);
            json.put("icon_text", iconText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.post("categories", json.toString(), callback);
    }

    public void getCategory(String categoryId, Callback callback) {
        apiService.get("categories/" + categoryId, new Callback() {
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

    public void getAllCategories(String userId, Callback callback) {
        apiService.get("categories?user_id=" + userId, new Callback() { // Cambia aquí
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

    public void updateCategory(String categoryId, String name, String color, String iconText, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("color", color);
            json.put("icon_text", iconText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.put("categories/" + categoryId, json.toString(), callback);
    }

    public void deleteCategory(String categoryId, Callback callback) {
        apiService.delete("categories/" + categoryId, new Callback() {
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
}