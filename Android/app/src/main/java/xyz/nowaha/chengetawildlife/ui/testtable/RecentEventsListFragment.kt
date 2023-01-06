package xyz.nowaha.chengetawildlife.ui.testtable

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.MainActivity
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.data.SessionManager
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.data.repos.Repositories
import xyz.nowaha.chengetawildlife.databinding.FragmentRecentEventsListBinding
import xyz.nowaha.chengetawildlife.ui.EventDataViewModel
import xyz.nowaha.chengetawildlife.ui.EventSelectionPipeViewModel
import xyz.nowaha.chengetawildlife.ui.map.EventMapViewModel
import java.text.SimpleDateFormat
import java.util.*

class RecentEventsListFragment : Fragment(R.layout.fragment_recent_events_list) {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecentEventsListAdapter

    private val eventDataViewModel: EventDataViewModel by navGraphViewModels(R.id.nav_graph_main)
    val eventSelectionPipeViewModel: EventSelectionPipeViewModel by navGraphViewModels(R.id.nav_graph_main)

    var data: ArrayList<RecentEventsListDataModel> = ArrayList()
    var filteredData: ArrayList<RecentEventsListDataModel> = ArrayList()

    private var _binding: FragmentRecentEventsListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.tableRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        this.adapter =
            RecentEventsListAdapter(requireActivity().applicationContext, this, data, filteredData)
        recyclerView.adapter = this.adapter

        binding.refreshButton.setOnClickListener {
            if (binding.loadingCircle.visibility != View.GONE) return@setOnClickListener
            eventDataViewModel.update()
        }

        registerFilterListeners()

        MainActivity.offlineMode.observe(viewLifecycleOwner) {
            binding.refreshButton.isEnabled = !it
        }

        eventDataViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EventDataViewModel.EventDataState.Loading -> {
                    binding.loadingCircle.visibility = View.VISIBLE
                    binding.refreshButton.imageAlpha = 0
                }
                EventDataViewModel.EventDataState.Idle -> {
                    binding.loadingCircle.visibility = View.GONE
                    binding.refreshButton.imageAlpha = 255
                }
                else -> {}
            }
        }

        val format = SimpleDateFormat("HH:mm:ss, dd MMM", Locale.ENGLISH)
        eventDataViewModel.data.observe(viewLifecycleOwner) { eventData ->
            when (eventData.lastResponse) {
                RepoResponse.ResponseType.SUCCESS -> {
                    data.clear()
                    filteredData.clear()
                    addTableRows(eventData.data.map {
                        RecentEventsListDataModel(
                            format.format(it.date),
                            it.soundLabel,
                            it.probability.toString() + "%",
                            it.statusString() ?: "Unknown",
                            it.date,
                            it.id
                        )
                    })
                }
                RepoResponse.ResponseType.UNAUTHORIZED -> {
                    Toast.makeText(
                        requireContext(),
                        "Your session expired. Please log in again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch {
                        SessionManager.logOut()
                    }
                }
                else -> {
                    Snackbar.make(
                        requireContext(),
                        requireView(),
                        "Failed to connect to server.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Retry") { eventDataViewModel.update() }.show()
                }
            }
        }
    }

    private fun registerFilterListeners() {
        binding.filterButton.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.tableConstraintLayout)
            if (binding.filterScrollView.visibility == View.VISIBLE) {
                binding.filterScrollView.visibility = View.GONE
            } else {
                binding.filterScrollView.visibility = View.VISIBLE
            }
        }

        binding.filterAnimalCheckBox.setOnCheckedChangeListener { _, _ -> updateFilters() }
        binding.filterGunshotCheckBox.setOnCheckedChangeListener { _, _ -> updateFilters() }
        binding.filterThunderCheckBox.setOnCheckedChangeListener { _, _ -> updateFilters() }
        binding.filterVehicleCheckBox.setOnCheckedChangeListener { _, _ -> updateFilters() }
        binding.filterUnknownCheckBox.setOnCheckedChangeListener { _, _ -> updateFilters() }
    }

    private fun updateFilters() {
        val filters = arrayListOf<String>()

        if (!binding.filterAnimalCheckBox.isChecked) filters.add("animal")
        if (!binding.filterGunshotCheckBox.isChecked) filters.add("gunshot")
        if (!binding.filterThunderCheckBox.isChecked) filters.add("thunder")
        if (!binding.filterVehicleCheckBox.isChecked) filters.add("vehicle")
        if (!binding.filterUnknownCheckBox.isChecked) filters.add("unknown")

        if (filters.size == 0) adapter.filteredData = data
        else {
            adapter.filteredData = data.filter { !filters.contains(it.text2Value.lowercase()) }
        }
        adapter.notifyDataSetChanged()
    }

    private fun addTableRows(viewModels: List<RecentEventsListDataModel>) {
        for (viewModel in viewModels.reversed()) {
            data.add(0, viewModel)
        }
        updateFilters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}