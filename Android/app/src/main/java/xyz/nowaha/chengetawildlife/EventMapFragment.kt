package xyz.nowaha.chengetawildlife

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.*
import xyz.nowaha.chengetawildlife.extensions.dp
import xyz.nowaha.chengetawildlife.extensions.iconBasedOnType
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.testtable.TestTableFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.floor


class EventMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private val markers = HashMap<Event, Marker>()

    private lateinit var bottomSheet: RelativeLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private var defaultBottomSheetPeekHeight: Int = 0

    private var markerCoroutine: Job? = null

    val viewModel: EventsMapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
                //updateMarkers()
            }
        }

        bottomSheet = view.findViewById(R.id.bottom_sheet_map)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        defaultBottomSheetPeekHeight = bottomSheetBehavior.peekHeight

        bottomSheet.isFocusableInTouchMode = true
        bottomSheet.requestFocus()
        bottomSheet.setOnKeyListener(OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
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

    private fun getRelativeTimeString(date1: Long, date2: Long): String {
        val timeDifference = ((date2 - date1) / 1000.0)
        val hours = floor(timeDifference / 3600.0).toInt()
        val minutes = floor((timeDifference - (hours * 3600.0)) / 60.0).toInt()
        val seconds = timeDifference - (hours * 3600) - (minutes * 60)

        var timeString = "${seconds.toInt()}s ago"
        if (minutes > 0 || hours > 0) {
            timeString = "${minutes}m " + timeString
        }
        if (hours > 0) {
            timeString = "${hours}h " + timeString
        }

        return timeString
    }

    @SuppressLint("MissingPermission")
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permissions.getOrDefault(
                    android.Manifest.permission.ACCESS_FINE_LOCATION, false
                ) -> {
                    // Precise location access granted.
                    googleMap.isMyLocationEnabled = true;
                }
                permissions.getOrDefault(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, false
                ) -> {
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
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true;
            return;
        }

        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        lifecycleScope.launch(Dispatchers.IO) {
            while(!viewModel.loadEvents()) { delay(1000) }
        }

        requestLocationPermission()

        redrawMap(viewModel.mapEvents.value ?: arrayListOf())

        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? {
                return null
            }

            override fun getInfoWindow(marker: Marker): View? {
                return layoutInflater.inflate(
                    R.layout.layout_marker_info_window, requireView() as ViewGroup, false
                )
            }
        })

        googleMap.setOnMapClickListener { eventDeselected() }
        googleMap.setOnMarkerClickListener(this)

        this.googleMap.setPadding(0, 0, 0, defaultBottomSheetPeekHeight)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        markerCoroutine?.cancel()
        markerCoroutine = null

        val event =
            markers.filter { it.value == marker }.map { it.key }.firstOrNull() ?: return false

        eventSelected(event)

        // Return false to keep the default behavior (moving to the marker & showing info window)
        return false
    }

    private fun eventSelected(event: Event) {
        markers.forEach { it.value.alpha = 0.7f }
        markers[event]?.alpha = 1f

        with(requireView()) {
            findViewById<FrameLayout>(R.id.tableHolder).visibility = GONE
            with(findViewById<View>(R.id.eventInfoLayout)) {
                val soundName = event.soundLabel[0].uppercase() + event.soundLabel.substring(1)

                if (visibility == GONE) {
                    visibility = VISIBLE
                    TransitionManager.beginDelayedTransition(bottomSheet)
                    bottomSheetBehavior.peekHeight = dp(262)
                    googleMap.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
                }

                findViewById<ImageButton>(R.id.targetButton).setOnClickListener {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers[event]!!.position, 7.5f), 500, null)
                }

                findViewById<TextView>(R.id.dateValue).text = getString(
                    R.string.event_data_title,
                    SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.GERMAN).format(event.date)
                )
                findViewById<TextView>(R.id.soundValue).text = soundName
                findViewById<TextView>(R.id.certaintyValue).text =
                    getString(R.string.event_data_certainty_value, event.probability)
                findViewById<TextView>(R.id.longitude).text =
                    getString(R.string.event_data_coordinates_longitude_value, event.longitude)
                findViewById<TextView>(R.id.latitude).text =
                    getString(R.string.event_data_coordinates_latitude_value, event.latitude)

                markerCoroutine = lifecycleScope.launch {
                    while (true) {
                        requireView().findViewById<TextView>(R.id.eventDetailsTitle).text =
                            "$soundName (${
                                getRelativeTimeString(
                                    event.date, System.currentTimeMillis()
                                )
                            })"
                        delay(500)
                    }
                }
            }
        }
    }

    private fun eventDeselected() {
        markerCoroutine?.cancel()
        markerCoroutine = null

        markers.forEach { it.value.alpha = 1f }

        with(requireView()) {
            findViewById<View>(R.id.eventInfoLayout).visibility = GONE
            findViewById<FrameLayout>(R.id.tableHolder).visibility = VISIBLE

            TransitionManager.beginDelayedTransition(bottomSheet)
            bottomSheetBehavior.peekHeight = defaultBottomSheetPeekHeight

            googleMap.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
        }
    }


}