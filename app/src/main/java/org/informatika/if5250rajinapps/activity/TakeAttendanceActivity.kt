package org.informatika.if5250rajinapps.activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.PolyUtil
import org.informatika.if5250rajinapps.BuildConfig
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.databinding.ActivityTakeAttendanceBinding
import org.informatika.if5250rajinapps.model.Cek
import org.informatika.if5250rajinapps.model.Presence
import org.informatika.if5250rajinapps.model.Wilayah
import org.informatika.if5250rajinapps.service.ForegroundOnlyLocationService
import org.informatika.if5250rajinapps.util.SharedPreferenceUtil
import org.informatika.if5250rajinapps.util.toText
import org.informatika.if5250rajinapps.viewmodel.TakeAttendanceViewModel

private const val TAG = "TakeAttendanceActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class TakeAttendanceActivity : AppCompatActivity(), OnMapReadyCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityTakeAttendanceBinding
    private var currentPosition: LatLng? = null
    private var polygons: List<List<LatLng>>? = null

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var outputTextView: TextView

    lateinit var viewModel: TakeAttendanceViewModel

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProvider(this)[TakeAttendanceViewModel::class.java]


        binding = ActivityTakeAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)


        outputTextView = binding.outputTextView
        val enabled = sharedPreferences.getBoolean(
            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
        )

        val btRefreshLoc = binding.btRefreshLoc
        btRefreshLoc.setOnClickListener {
            requestLocationUpdate()
        }

        binding.btCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btPresensi.setOnClickListener {
            if (currentPosition == null) {
                Snackbar.make(
                    findViewById(R.id.activity_take_attendance),
                    "Mohon menunggu, lokasi anda masih belum ditemukan.",
                    Snackbar.LENGTH_LONG
                ).show()
                requestLocationUpdate()
            } else {
                cekBtPresensiVisibility()
            }
        }

        val rgJenis = binding.rg
        rgJenis.setOnCheckedChangeListener { _, _ ->
            val jenis = if (binding.rg.checkedRadioButtonId == R.id.rbWfo) "wfo" else "wfh"
            if (jenis == "wfh") {
                binding.btPresensi.isEnabled = true
                binding.btPresensi.text = "PRESENSI"
            }
        }

        requestLocationUpdate()

            viewModel.presensi.observe(this) {
            Log.d(TAG, "presensi : $it")
            binding.btPresensi.text = "MASUK"
            if (it != null) {

                rgJenis.check(if (it.jenis == "wfo") R.id.rbWfo else R.id.rbWfh)
                binding.cardView3.visibility = View.INVISIBLE
                binding.btPresensi.text = "PULANG"
            }
        }

        viewModel.unitKerja.observe(this) {
            if (it != null) {
                for (option in getPolylineOptionsList(it!!.batasWilayah!!)) {
                    mMap.addPolyline(option)
                }
                polygons = getPolygons(it!!.batasWilayah!!)
            }
        }
    }

    private fun actionPresensi() {

        val it = viewModel.presensi.value
        if (it == null) {
            var mAuth = FirebaseAuth.getInstance()
            if (mAuth.currentUser == null) throw Exception("Anda belum login")

            val jenis = if (binding.rg.checkedRadioButtonId == R.id.rbWfo) "wfo" else "wfh"
            val checkIn = Cek(
                waktu = Timestamp.now(),
                isMockLocation = false,
                GeoPoint(currentPosition!!.latitude, currentPosition!!.longitude)
            )
            val p = Presence(
                UID = mAuth.uid,
                jenis = jenis,
                ket = null,
                checkIn = checkIn,
                checkOut = null,
                isLembur = false,
                timeCreate = Timestamp.now(),
                timeUpdate = Timestamp.now()
            )
            viewModel.presensiMasuk(p)

            onBackPressedDispatcher.onBackPressed()
            Toast.makeText(applicationContext, "Berhasil presensi masuk", Toast.LENGTH_SHORT).show()
        } else {

            val checkOut = Cek(
                waktu = Timestamp.now(),
                isMockLocation = false,
                GeoPoint(currentPosition!!.latitude, currentPosition!!.longitude)
            )
            val p = Presence(
                UID = it.UID,
                jenis = it.jenis,
                ket = it.ket,
                checkIn = it.checkIn,
                checkOut = checkOut,
                isLembur = it.isLembur,
                timeCreate = it.timeCreate,
                timeUpdate = Timestamp.now()
            )
            viewModel.presensiPulang(p)

            onBackPressedDispatcher.onBackPressed()
            Toast.makeText(applicationContext, "Berhasil presensi pulang", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun requestLocationUpdate() {

        if (!gpsEnabled()) {
            Snackbar.make(
                findViewById(R.id.activity_take_attendance),
                "Mohon aktifkan GPS anda.",
                Snackbar.LENGTH_LONG
            ).setAction(R.string.ok) {
                // Request permission
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }.show()
        }

        foregroundOnlyLocationService?.unsubscribeToLocationUpdates()

        if (foregroundPermissionApproved()) {
            foregroundOnlyLocationService?.subscribeToLocationUpdates()
                ?: Log.d(TAG, "Service Not Bound")
        } else {
            requestForegroundPermissions()
        }
    }

    private fun gpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: ")
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        requestLocationUpdate()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }


    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_take_attendance),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@TakeAttendanceActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@TakeAttendanceActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.

                    Snackbar.make(
                        findViewById(R.id.activity_take_attendance),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.request) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)

        if (foregroundPermissionApproved()) {
            mMap.isMyLocationEnabled = true
        } else {
            requestForegroundPermissions()
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getPolylineOptionsList(batasWilayah: List<Wilayah>): List<PolylineOptions> {

        var polylineOptionsList = mutableListOf<PolylineOptions>()

        for (w in batasWilayah) {
            var polylines = mutableListOf<LatLng>()
            for (p in w.polygons!!) {
                val latLng = LatLng(p.latitude, p.longitude)
                polylines.add(latLng)
            }
            if (!w.polygons!!.isEmpty()){
                polylines.add(LatLng(w.polygons!![0].latitude, w.polygons!![0].longitude))
            }

            val polylineOptions = PolylineOptions().addAll(polylines).color(Color.RED).width(5f)
            polylineOptionsList.add(polylineOptions)
        }


        return polylineOptionsList
    }

    private fun getPolygons(batasWilayah: List<Wilayah>): List<List<LatLng>> {
        var polygons = mutableListOf<List<LatLng>>()

        for (w in batasWilayah) {
            var polygon = mutableListOf<LatLng>()
            for (p in w.polygons!!) {
                val latLng = LatLng(p.latitude, p.longitude)
                polygon.add(latLng)
            }
            if (!w.polygons!!.isEmpty()){
                polygon.add(LatLng(w.polygons!![0].latitude, w.polygons!![0].longitude))
            }

            polygons.add(polygon)
        }

        return polygons
    }

    private fun logResultsToScreen(output: String) {
        val outputWithPreviousLogs = "$output\n${outputTextView.text}"
        outputTextView.text = outputWithPreviousLogs
    }

    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                logResultsToScreen("Foreground location: ${location.toText()}")
//                mMap.clear()
                currentPosition = LatLng(location.latitude, location.longitude)
//                mMap.addMarker(MarkerOptions().position(position).title("Your position"))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition!!, 16f))
            }
        }

    }

    private fun cekBtPresensiVisibility() {
        val jenis = if (binding.rg.checkedRadioButtonId == R.id.rbWfo) "wfo" else "wfh"
        if (jenis == "wfo" && polygons != null) {
            for (p in polygons!!) {

                Log.d(TAG, "currenPosition: $currentPosition")
                val isWithin = PolyUtil.containsLocation(currentPosition, p, true)
                Log.d(TAG, "isWithin : $isWithin")

                if (isWithin) {
                    actionPresensi()
                } else {
                    Snackbar.make(
                        findViewById(R.id.activity_take_attendance),
                        "Mohon maaf, presensi WFO hanya bisa dilakukan di dalam area kerja.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            actionPresensi()
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
//            updateButtonState(sharedPreferences.getBoolean(
//                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
//            )
        }
    }
}