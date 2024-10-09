package dam.clases.monje_financiero_app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import dam.clases.monje_financiero_app.R;
import dam.clases.monje_financiero_app.services.ReportsService;
import dam.clases.monje_financiero_app.services.ApiService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {
    private CalendarView calendarStartDate;
    private CalendarView calendarEndDate;
    private Button btnGenerateReport;
    private BarChart barChart;
    private ReportsService reportsService;
    private String startDate;
    private String endDate;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Inicialización de componentes
        calendarStartDate = findViewById(R.id.calendarStartDate);
        calendarEndDate = findViewById(R.id.calendarEndDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        barChart = findViewById(R.id.barChart);

        // Inicialización del ReportsService
        reportsService = new ReportsService(); // Elimina el paso de contexto de actividad

        // Obtener el userId de SharedPreferences
        userId = ApiService.getUserId(this); // Asegúrate de que ApiService está implementado correctamente

        // Configurar la selección de fechas
        configureDateSelection();

        // Configurar el botón de generar reporte
        btnGenerateReport.setOnClickListener(v -> {
            if (startDate != null && endDate != null) {
                generateReport();
            } else {
                Toast.makeText(ReportsActivity.this, "Por favor, selecciona las fechas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para configurar la selección de fechas
    private void configureDateSelection() {
        calendarStartDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
        });

        calendarEndDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
        });
    }

    // Método para generar el reporte
    private void generateReport() {
        reportsService.getReportData(userId, startDate, endDate, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "Error al generar el reporte", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray categoryBreakdown = jsonResponse.getJSONArray("category_breakdown");

                        // Procesar los datos para el gráfico
                        List<BarEntry> entries = new ArrayList<>();
                        List<String> categoryNames = new ArrayList<>();

                        for (int i = 0; i < categoryBreakdown.length(); i++) {
                            JSONObject categoryData = categoryBreakdown.getJSONObject(i);
                            String category = categoryData.getString("category");
                            double amount = categoryData.getDouble("amount");

                            entries.add(new BarEntry(i, (float) amount));
                            categoryNames.add(category);
                        }

                        // Mostrar los datos en el gráfico
                        runOnUiThread(() -> showBarChart(entries, categoryNames));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "Error al procesar el reporte", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "Error al generar el reporte", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Método para mostrar el gráfico de barras
    private void showBarChart(List<BarEntry> entries, List<String> categoryNames) {
        BarDataSet dataSet = new BarDataSet(entries, "Gastos por Categoría");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // ancho de la barra

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.invalidate(); // refrescar el gráfico

        // Configuración adicional del eje X para mostrar los nombres de las categorías
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(categoryNames.size());

        // Usar ValueFormatter en lugar de lambda
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;  // Convertir el valor a int
                if (index >= 0 && index < categoryNames.size()) {
                    return categoryNames.get(index);  // Obtener el nombre de la categoría según el índice
                } else {
                    return "";  // Valor por defecto si el índice está fuera de rango
                }
            }
        });

        // Configuración del eje Y (opcional)
        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setGranularityEnabled(true);

        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false); // Deshabilitar el eje Y derecho
    }

}