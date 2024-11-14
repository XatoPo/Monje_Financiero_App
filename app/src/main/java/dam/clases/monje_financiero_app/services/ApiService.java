package dam.clases.monje_financiero_app.services;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.*;

import java.io.IOException;

public class ApiService {

    private static final String BASE_URL = "https://api-node-monje-299345047999.us-central1.run.app/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    // Guardar user_id en SharedPreferences
    public static void saveUserId(Context context, String userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MonjeFinancieroPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userId);
        editor.apply();
    }

    // Obtener user_id de SharedPreferences
    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MonjeFinancieroPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null);
    }

    // Método POST
    public void post(String endpoint, String json, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Método GET
    public void get(String endpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Método PUT
    public void put(String endpoint, String json, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Método DELETE
    public void delete(String endpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }
}