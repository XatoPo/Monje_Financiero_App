package dam.clases.monje_financiero_app.services;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

public class MetaService {

    private ApiService apiService;

    public MetaService() {
        this.apiService = new ApiService();
    }

    // Obtener todas las metas del usuario
    public void getAllMeta(String userId, Callback callback) {
        apiService.get("meta?user_id=" + userId, callback);
    }

    // Agregar una nueva meta
    public void addMeta(String userId, double targetAmount, double achievedAmount, String deadline, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("target_amount", targetAmount);
            json.put("achieved_amount", achievedAmount);
            json.put("deadline", deadline);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.post("meta", json.toString(), callback);
    }

    // Actualizar una meta
    public void updateMeta(String metaId, double targetAmount, double achievedAmount, String deadline, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("target_amount", targetAmount);
            json.put("achieved_amount", achievedAmount);
            json.put("deadline", deadline);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.put("meta/" + metaId, json.toString(), callback);
    }

    // Eliminar una meta
    public void deleteMeta(String metaId, Callback callback) {
        apiService.delete("meta/" + metaId, callback);
    }
}