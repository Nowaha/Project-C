package xyz.nowaha.chengetawildlife

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.nowaha.chengetawildlife.extensions.iconBasedOnType
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.testtable.TestTableFragment
import kotlin.math.floor

class EventMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val permissionDenied = false

    val viewModel: EventsMapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        with(childFragmentManager.beginTransaction()) {
            replace(R.id.placeholder_bottom_sheet_map, TestTableFragment())
            commit()
        }

        viewModel.mapEvents.observe(viewLifecycleOwner) { eventList ->
           redrawMap(eventList)
        }

        lifecycleScope.launch {
            while (true) {
                delay(100)
                updateMarkers()
            }
        }

        val bottomSheet = view.findViewById<RelativeLayout>(R.id.bottom_sheet_map)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheet.isFocusableInTouchMode = true
        bottomSheet.requestFocus()
        bottomSheet.setOnKeyListener(OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                println("owo")
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return@OnKeyListener true
                }
            }

            false
        })
        bottomSheet.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private val markers = HashMap<Event, Marker>()
    private fun redrawMap(eventList: List<Event>) {
        if (!::googleMap.isInitialized) return

        markers.forEach { it.value.remove() }
        markers.clear()

        if (eventList.isEmpty()) return

        var last: LatLng? = null
        eventList.forEach {
            val position = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
            val marker = MarkerOptions()
            marker.position(position)
            marker.iconBasedOnType(it.soundLabel)
            marker.snippet("${it.probability}% certainty\n${it.latitude}; ${it.longitude}")
            marker.title("Loading...")

            markers[it] = googleMap.addMarker(marker)!!
            last = position
        }

        if (last == null) return
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(last!!, 5f))
    }

    fun updateMarkers() {
        if (!::googleMap.isInitialized) return

        val now = System.currentTimeMillis()

        markers.forEach {
            val event = it.key
            val marker = it.value

            val timeDifference = ((now - event.date) / 1000.0)
            val minutes = floor(timeDifference / 60.0).toInt()
            val seconds = timeDifference - (minutes * 60)

            var timeString = "${seconds.toInt()}s ago"
            if (minutes > 0) {
                timeString = "${minutes}m " + timeString
            }
            marker.title = "${event.soundLabel.uppercase()[0]}${event.soundLabel.substring(1)} ($timeString)"
        }
    }

    @SuppressLint("MissingPermission")
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    googleMap.isMyLocationEnabled = true;
                }
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    googleMap.isMyLocationEnabled = true;
                }
                else -> {
                    // No location access granted.
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true;
            return;
        }

        locationPermissionRequest.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.loadEvents()
        }

        requestLocationPermission()

        redrawMap(viewModel.mapEvents.value ?: arrayListOf())

        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoContents(p0: Marker): View? {
                return null
            }

            override fun getInfoWindow(marker: Marker): View? {
                val info = LinearLayout(requireContext())
                info.orientation = LinearLayout.VERTICAL
                info.setBackgroundColor(Color.WHITE)
                info.setPadding(24)

                val title = TextView(requireContext())
                title.setTextColor(Color.BLACK)
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title

                val snippet = TextView(requireContext())
                snippet.setTextColor(Color.BLACK)
                snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })
    }

}