package dam.clases.monje_financiero_app.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dam.clases.monje_financiero_app.R

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var markerCount = 1 // Contador para marcadores
    private var lastMarker: Marker? = null // Último marcador agregado
    private val markers = mutableListOf<Marker>() // Lista de marcadores para trazar líneas
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val defaultLocation = LatLng(-12.073074, -77.163868) // Ubicación por defecto
    private lateinit var loadingDialog: ALoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingDialog()
        setContentView(R.layout.activity_location)

        // Obtén el fragmento del mapa y configúralo
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configura el botón flotante para reubicar la cámara a la ubicación actual
        val btnRecenter = findViewById<FloatingActionButton>(R.id.fab_recenter)
        btnRecenter.setOnClickListener {
            getDeviceLocation()
        }

        val btnViewDirections = findViewById<Button>(R.id.btnViewDirections)
        btnViewDirections.setOnClickListener {
            getLocalLocation()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        configureBottomNavigation()
    }

    private fun showLoadingDialog() {
        loadingDialog = ALoadingDialog(this) // Inicializar el diálogo de carga
        loadingDialog.show() // Mostrar el diálogo
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        // Desactivar el botón de ubicación predeterminado de Google Maps
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        // Habilitar gestos
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true

        // Habilitar el indicador de ubicación actual si hay permisos
        getLocationPermission()

        // Mover la cámara a la ubicación por defecto y añadir el primer marcador
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        addMarker(defaultLocation, "Oficina Monje Financiero", "Ubicación del local", R.drawable.ic_local)

        // Añadir listener para clics en el mapa
        googleMap.setOnMapClickListener { latLng ->
            markerCount++
            addMarker(latLng, "Destino $markerCount", "Marcador en la posición $markerCount", R.drawable.ic_destino)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getLocalLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                    getDeviceLocation()
                }
            } else {
                moveToDefaultLocation()
            }
        }
    }

    private fun getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                } else {
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                    moveToDefaultLocation()
                }
            }
        } catch (e: SecurityException) {
            loadingDialog.dismiss() // Ocultar el diálogo de carga
            e.printStackTrace()
        }
    }

    private fun getLocalLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                } else {
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                    moveToDefaultLocation()
                }
            }
        } catch (e: SecurityException) {
            loadingDialog.dismiss() // Ocultar el diálogo de carga
            e.printStackTrace()
        }
    }

    private fun moveToDefaultLocation() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        loadingDialog.dismiss() // Ocultar el diálogo de carga
        Toast.makeText(this, "No se pudo obtener la ubicación actual. Mostrando ubicación por defecto.", Toast.LENGTH_SHORT).show()
    }

    private fun addMarker(latLng: LatLng, title: String, description: String, iconResId: Int) {
        // Crear un Bitmap a partir del recurso
        val bitmap = BitmapFactory.decodeResource(resources, iconResId)

        // Redimensionar el Bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)

        // Añadir nuevo marcador
        val marker = googleMap.addMarker(
            MarkerOptions().position(latLng)
                .title(title)
                .snippet(description)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap)) // Usar ícono redimensionado y  personalizado
        )
        marker?.let {
            markers.add(it)
        }

        // Si ya hay al menos un marcador previo, trazar línea entre el último y el nuevo
        if (markers.size > 1) {
            val previousMarker = markers[markers.size - 2]
            val polylineOptions = PolylineOptions()
                .add(previousMarker.position, latLng)
                .width(5f)
                .color(Color.RED)
            googleMap.addPolyline(polylineOptions)
        }
    }

    private fun configureBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java))
                    true
                }
                R.id.navigation_budgets -> {
                    startActivity(Intent(this, BudgetsActivity::class.java))
                    true
                }
                R.id.navigation_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}