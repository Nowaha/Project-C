package xyz.nowaha.chengetawildlife

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import xyz.nowaha.chengetawildlife.databinding.FragmentEventMapBinding
import xyz.nowaha.chengetawildlife.extensions.dp
import xyz.nowaha.chengetawildlife.extensions.iconBasedOnType
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.testtable.TestTableFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


@Suppress("DEPRECATION")
class EventMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private val markers = HashMap<Event, Marker>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private var defaultBottomSheetPeekHeight: Int = 0

    private lateinit var soundLoadingCircle: ProgressBar
    private lateinit var soundPlayButton: MaterialButton

    private var markerCoroutine: Job? = null

    val viewModel: EventMapViewModel by viewModels()

    private var mediaPlayer: MediaPlayer? = null
    private var mediaLoading = false
    private var lastLoadedSound: String? = null

    private var _binding: FragmentEventMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        runBlocking {
            if (SessionManager.getCurrentSession()?.isAdmin == true) {
                inflater.inflate(R.menu.menu_map_admin, menu)
            } else {
                inflater.inflate(R.menu.menu_map, menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_MEDIA).build()
        )

        soundLoadingCircle = view.findViewById(R.id.soundLoadingCircle)
        soundPlayButton = view.findViewById(R.id.soundPlayButton)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        with(childFragmentManager.beginTransaction()) {
            replace(R.id.placeholder_bottom_sheet_map, TestTableFragment())
            commit()
        }

        viewModel.mapEvents.observe(viewLifecycleOwner) { eventList ->
            redrawMap(eventList)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetMap)
        defaultBottomSheetPeekHeight = bottomSheetBehavior.peekHeight

        binding.bottomSheetMap.isFocusableInTouchMode = true
        binding.bottomSheetMap.requestFocus()
        binding.bottomSheetMap.setOnKeyListener(OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return@OnKeyListener true
                }
            }

            false
        })

        binding.bottomSheetMap.setOnClickListener {
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

        eventList.forEach {
            val position = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
            val marker = MarkerOptions()
            marker.position(position)
            marker.iconBasedOnType(it.soundLabel)
            marker.snippet("${it.probability}% certainty\n${it.latitude}; ${it.longitude}")
            marker.title("Loading...")

            markers[it] = googleMap.addMarker(marker)!!
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logoutMenuItem -> {
                logOutClick()
                true
            }
            R.id.adminMenuItem -> {
                runBlocking { markerCoroutine?.cancelAndJoin() }
                findNavController().navigate(R.id.action_eventMapFragment_to_nav_graph_admin)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun logOutClick() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Confirm logging out")
            .setMessage("You will have to log in with a username and password again to use the app.")
            .setPositiveButton("Log out") { _, _ ->
                lifecycleScope.launch {
                    SessionManager.logOut()
                }
            }.setNegativeButton("Cancel") { _, _ -> }.show()
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
                    googleMap.isMyLocationEnabled = true
                }
                permissions.getOrDefault(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, false
                ) -> {
                    // Only approximate location access granted.
                    googleMap.isMyLocationEnabled = true
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
            googleMap.isMyLocationEnabled = true
            return
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

        if (viewModel.mapEvents.value == null || viewModel.mapEvents.value!!.isEmpty()) {
            lifecycleScope.launch {
                while (!viewModel.loadEvents()) {
                    delay(1000)
                }
                markers.entries.firstOrNull()?.let {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it.value.position, 5f))
                }
            }
        } else {
            if (viewModel.selectedEvent != null) {
                markers.entries.firstOrNull { it.key.id == viewModel.selectedEvent }?.let {
                    eventSelected(it.key, noAnimation = true)
                }
            }
        }

        requestLocationPermission()

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

        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMapClickListener { eventDeselected() }

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

    private fun eventSelected(event: Event, noAnimation: Boolean = false) {
        viewModel.selectedEvent = event.id

        markers.forEach { it.value.alpha = 0.7f }
        markers[event]?.alpha = 1f

        mediaLoading = false
        lastLoadedSound = null
        mediaPlayer?.apply {
            stop()
            reset()
        }
        resetSoundButton()

        with(requireView()) {
            findViewById<FrameLayout>(R.id.tableHolder).visibility = GONE
            with(findViewById<View>(R.id.eventInfoLayout)) {
                val soundName = event.soundLabel[0].uppercase() + event.soundLabel.substring(1)

                if (visibility == GONE) {
                    visibility = VISIBLE
                    if (!noAnimation) TransitionManager.beginDelayedTransition(binding.bottomSheetMap)
                    bottomSheetBehavior.peekHeight = dp(262)
                    googleMap.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
                }

                findViewById<ImageButton>(R.id.targetButton).setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            markers[event]!!.position, 7.5f
                        ), 500, null
                    )
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

                soundPlayButton.setOnClickListener {
                    if (mediaLoading || mediaPlayer?.isPlaying == true) return@setOnClickListener
                    mediaLoading = true
                    soundLoadingCircle.visibility = VISIBLE
                    soundPlayButton.setIconResource(R.drawable.ic_invisible_24)

                    mediaPlayer?.apply {
                        if (lastLoadedSound != event.soundUrl) {
                            setDataSource(event.soundUrl)
                            prepareAsync()
                            lastLoadedSound = event.soundUrl
                        } else {
                            seekTo(0)
                            playSound()
                        }
                        setOnPreparedListener { playSound() }
                        setOnCompletionListener { resetSoundButton() }
                    }
                }

                markerCoroutine = lifecycleScope.launch {
                    while (true) {
                        requireView().findViewById<TextView>(R.id.eventDetailsTitle)?.text =
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

    private fun playSound() {
        mediaPlayer?.start()
        soundPlayButton.setIconResource(R.drawable.drawable_pause_button)
        soundLoadingCircle.visibility = INVISIBLE
        mediaLoading = false
    }

    private fun resetSoundButton() {
        soundLoadingCircle.visibility = INVISIBLE
        soundPlayButton.setIconResource(R.drawable.drawable_play_arrow)
    }

    private fun eventDeselected() {
        markerCoroutine?.cancel()
        markerCoroutine = null

        viewModel.selectedEvent = null

        markers.forEach { it.value.alpha = 1f }

        with(requireView()) {
            findViewById<View>(R.id.eventInfoLayout).visibility = GONE
            findViewById<FrameLayout>(R.id.tableHolder).visibility = VISIBLE

            TransitionManager.beginDelayedTransition(binding.bottomSheetMap)
            bottomSheetBehavior.peekHeight = defaultBottomSheetPeekHeight

            googleMap.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        _binding = null
    }

}