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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import xyz.nowaha.chengetawildlife.MainActivity
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.data.SessionManager
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.Event
import xyz.nowaha.chengetawildlife.data.pojo.EventEditTaskRequest
import xyz.nowaha.chengetawildlife.data.pojo.EventStatus
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.databinding.FragmentEventMapBinding
import xyz.nowaha.chengetawildlife.ui.EventDataViewModel
import xyz.nowaha.chengetawildlife.ui.EventSelectionPipeViewModel
import xyz.nowaha.chengetawildlife.util.TimeUtils
import xyz.nowaha.chengetawildlife.util.extensions.dp
import xyz.nowaha.chengetawildlife.util.extensions.iconBasedOnType
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.min
import kotlin.collections.LinkedHashMap


@Suppress("DEPRECATION")
class EventMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private val markers = hashMapOf<Event, Marker>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private var defaultBottomSheetPeekHeight: Int = 0

    private lateinit var soundLoadingCircle: ProgressBar
    private lateinit var soundPlayButton: MaterialButton

    private var markerCoroutine: Job? = null

    val viewModel: EventMapViewModel by viewModels()
    val eventDataViewModel: EventDataViewModel by navGraphViewModels(R.id.nav_graph_main)
    val eventSelectionPipeViewModel: EventSelectionPipeViewModel by navGraphViewModels(R.id.nav_graph_main)

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

        eventSelectionPipeViewModel.eventSelected.observe(viewLifecycleOwner) {
            if (it >= 0) {
                if (viewModel.selectedEvent == it || viewModel.selectedEvent != null) return@observe
                val event = eventDataViewModel.data.value?.data?.firstOrNull { e -> e.id == it }
                    ?: return@observe
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                eventSelected(event, true)
                moveToMarkerAt(markers[event]!!.position)
            }
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_MEDIA).build()
        )

        soundLoadingCircle = view.findViewById(R.id.soundLoadingCircle)
        soundPlayButton = view.findViewById(R.id.soundPlayButton)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        eventDataViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                EventDataViewModel.EventDataState.Done -> {
                    binding.loadingCircleEventMap.visibility = GONE
                }
                EventDataViewModel.EventDataState.Idle -> {
                    binding.loadingCircleEventMap.visibility = GONE
                }
                EventDataViewModel.EventDataState.Loading -> {
                    binding.loadingCircleEventMap.visibility = VISIBLE
                }
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetMap)
        defaultBottomSheetPeekHeight = bottomSheetBehavior.peekHeight

        var debounce = false
        binding.bottomSheetMap.isFocusableInTouchMode = true
        binding.bottomSheetMap.requestFocus()
        binding.bottomSheetMap.setOnKeyListener(OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (debounce) return@OnKeyListener true
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    debounce = true
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    lifecycleScope.launch {
                        delay(10)
                        debounce = false
                    }
                    return@OnKeyListener true
                } else if (viewModel.selectedEvent != null) {
                    debounce = true
                    eventDeselected()
                    lifecycleScope.launch {
                        delay(10)
                        debounce = false
                    }
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
        markers.forEach { it.value.remove() }
        markers.clear()

        if (eventList.isEmpty()) return

        var shown = 0
        eventList.forEach {
            val position = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
            val marker = MarkerOptions()
            marker.position(position)
            marker.iconBasedOnType(it.soundLabel)
            marker.snippet("${it.probability}% certainty\n${it.latitude}; ${it.longitude}")
            marker.title("Loading...")

            if (++shown >= 16) {
                marker.visible(false)
            }

            markers[it] = googleMap.addMarker(marker)!!
        }

        if (viewModel.selectedEvent != null) {
            val evt = eventList.firstOrNull { it.id == viewModel.selectedEvent }
            if (evt != null) {
                eventSelected(evt, true)
            } else {
                eventDeselected()
            }
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

        if (viewModel.selectedEvent != null) {
            markers.entries.firstOrNull { it.key.id == viewModel.selectedEvent }?.let {
                eventSelected(it.key, noAnimation = true)
            }
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

        eventDataViewModel.data.observe(viewLifecycleOwner) { eventData ->
            if (eventData.lastResponse == RepoResponse.ResponseType.SUCCESS) {
                if (eventData.data.isEmpty()) return@observe
                redrawMap(eventData.data)

                if (viewModel.firstLoad) {
                    viewModel.firstLoad = false

                    markers.entries.firstOrNull()?.let {
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                it.value.position, 5f
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val event =
            markers.filter { it.value == marker }.map { it.key }.firstOrNull() ?: return false

        eventSelected(event)

        // Return false to keep the default behavior (moving to the marker & showing info window)
        return false
    }

    private fun moveToMarkerAt(position: LatLng) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position, 7.5f
            ), 500, null
        )
    }

    private fun eventSelected(event: Event, noAnimation: Boolean = false) {
        eventDeselected(false)
        viewModel.selectedEvent = event.id

        markers.forEach { it.value.alpha = 0.7f }
        markers[event]?.alpha = 1f
        markers[event]?.isVisible = true

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
                moveToMarkerAt(markers[event]!!.position)
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
                    MaterialAlertDialogBuilder(requireContext()).setTitle("Select new status")
                        .setSingleChoiceItems(
                            EventStatus.getValues().toTypedArray(),
                            EventStatus.indexOfNumber(eventStatus)
                        ) { _, _ -> }.setPositiveButton("Confirm") { dialog, _ ->
                            val selection = (dialog as AlertDialog).listView.checkedItemPosition
                            eventStatus = EventStatus.numberAtIndex(selection)

                            val oldStatus = statusValue.text
                            statusValue.text = "Status: ${EventStatus.of(eventStatus) ?: "Unknown"}"

                            lifecycleScope.launch(Dispatchers.IO) {
                                val req = APIClient.getAPIInterface().attemptEditEventStatus(
                                    EventEditTaskRequest(
                                        event.id,
                                        eventStatus
                                    )
                                ).execute()
                                if (!req.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        statusValue.text = oldStatus
                                        Snackbar.make(
                                            requireContext(),
                                            requireView(),
                                            "There was an error changing the event status. Please try again.",
                                            Snackbar.LENGTH_LONG
                                        )
                                    }
                                } else {
                                    eventDataViewModel.update()
                                }
                            }
                        }.setNegativeButton("Cancel") { _, _ -> }.show()
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

    private fun eventDeselected(changeBottomSheet: Boolean = true) {
        markerCoroutine?.cancel()
        markerCoroutine = null

        eventSelectionPipeViewModel.eventSelected.postValue(-1)

        val oldSelected = viewModel.selectedEvent
        viewModel.selectedEvent = null

        if (oldSelected != null) {
            val list = eventDataViewModel.data.value?.data
            if (list != null) {
                val event = list.firstOrNull { it.id == oldSelected }
                val sublist = list.subList(0, list.size.coerceAtMost(16))
                if (!sublist.contains(event)) {
                    markers[event]?.isVisible = false
                }
            }
        }

        if (changeBottomSheet) {
            markers.forEach { it.value.alpha = 1f }

            binding.eventInfoLayoutFrameLayout.visibility = GONE
            binding.tableFragmentHolderFrameLayout.visibility = VISIBLE

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