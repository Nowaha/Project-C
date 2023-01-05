package xyz.nowaha.chengetawildlife.ui.map

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.transition.TransitionManager
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
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
import xyz.nowaha.chengetawildlife.MainActivity
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.data.SessionManager
import xyz.nowaha.chengetawildlife.data.pojo.Event
import xyz.nowaha.chengetawildlife.data.pojo.EventStatus
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.databinding.FragmentEventMapBinding
import xyz.nowaha.chengetawildlife.util.TimeUtils
import xyz.nowaha.chengetawildlife.util.extensions.dp
import xyz.nowaha.chengetawildlife.util.extensions.iconBasedOnType
import java.text.SimpleDateFormat
import java.util.*


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

    @SuppressLint("MissingPermission")
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

        MainActivity.offlineMode.observe(viewLifecycleOwner) {
            soundPlayButton.isEnabled = !it
            binding.eventInfoLayout.eventStatusConstraintLayout.isEnabled = !it

            val imageView =
                binding.eventInfoLayout.eventStatusConstraintLayout.findViewById<ImageView>(R.id.eventStatusIconImageView)

            imageView.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (it) R.drawable.ic_password else R.drawable.ic_baseline_edit_24,
                    requireContext().theme
                )
            )
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
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        if (viewModel.mapEvents.value == null || viewModel.mapEvents.value!!.isEmpty()) {
            lifecycleScope.launch {
                while (true) {
                    when (viewModel.loadEvents(requireContext())) {
                        RepoResponse.ResponseType.SUCCESS -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingCircleEventMap.visibility = GONE
                            }
                            markers.entries.firstOrNull()?.let {
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        it.value.position, 5f
                                    )
                                )
                            }
                            break
                        }
                        else -> {
                            delay(1000)
                        }
                    }
                }
            }
        } else {
            if (viewModel.selectedEvent != null) {
                markers.entries.firstOrNull { it.key.id == viewModel.selectedEvent }?.let {
                    eventSelected(it.key, noAnimation = true)
                }
            }

            binding.loadingCircleEventMap.visibility = GONE
        }

        (activity as? MainActivity)?.requestLocationPermission()

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

        MainActivity.hasLocationPermission.observe(viewLifecycleOwner) {
            googleMap.isMyLocationEnabled = it
        }

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

        binding.tableFragmentHolderFrameLayout.visibility = GONE
        with(binding.eventInfoLayoutFrameLayout) {
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

            with(binding.eventInfoLayout) {
                dateValue.text = getString(
                    R.string.event_data_title,
                    SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.GERMAN).format(event.date)
                )
                soundValue.text = soundName
                certaintyValue.text =
                    getString(R.string.event_data_certainty_value, event.probability)
                longitude.text =
                    getString(R.string.event_data_coordinates_longitude_value, event.longitude)
                latitude.text =
                    getString(R.string.event_data_coordinates_latitude_value, event.latitude)
                statusValue.text = "Status: ${event.statusString() ?: "Unknown"}"

                var eventStatus = event.status

                eventStatusConstraintLayout.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Select new status")
                        .setSingleChoiceItems(
                            EventStatus.getValues().toTypedArray(),
                            EventStatus.indexOfNumber(eventStatus)
                        ) { dialog, index ->

                        }
                        .setPositiveButton("Confirm") { dialog, _ ->
                            val selection = (dialog as AlertDialog).listView.checkedItemPosition
                            eventStatus = EventStatus.numberAtIndex(selection)
                            statusValue.text = "Status: ${EventStatus.of(eventStatus) ?: "Unknown"}"
                        }
                        .setNegativeButton("Cancel") { _, _ -> }
                        .show()
                }
            }

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
                            TimeUtils.getRelativeTimeString(
                                event.date, System.currentTimeMillis()
                            )
                        })"
                    delay(500)
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

        binding.eventInfoLayoutFrameLayout.visibility = GONE
        binding.tableFragmentHolderFrameLayout.visibility = VISIBLE

        TransitionManager.beginDelayedTransition(binding.bottomSheetMap)
        bottomSheetBehavior.peekHeight = defaultBottomSheetPeekHeight

        googleMap.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        _binding = null
    }

}