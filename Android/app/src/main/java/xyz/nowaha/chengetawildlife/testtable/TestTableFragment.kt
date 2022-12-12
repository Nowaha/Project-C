package xyz.nowaha.chengetawildlife.testtable

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.data.repos.Repositories
import java.text.SimpleDateFormat
import java.util.*

class TestTableFragment : Fragment(R.layout.fragment_test_table) {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecentEventsListAdapter
    var data: ArrayList<RecentEventsListViewModel> = ArrayList()

    private var latestAdded: Long = 0

    lateinit var loadingCircle: ProgressBar
    lateinit var refreshButton: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.tableRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        this.adapter = RecentEventsListAdapter(requireActivity().applicationContext, data)
        recyclerView.adapter = this.adapter

        loadingCircle = view.findViewById(R.id.loadingCircle)
        refreshButton = view.findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            if (loadingCircle.visibility != View.GONE) return@setOnClickListener
            loadNewData()
        }

        loadNewData()
    }

    private fun loadNewData() {
        loadingCircle.visibility = View.VISIBLE
        refreshButton.imageAlpha = 0

        lifecycleScope.launch(Dispatchers.IO) {
            val format = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
            val repoResponse = Repositories.getEvents(requireContext(), 100, 0)
            if (repoResponse.responseType == RepoResponse.ResponseType.SUCCESS) {
                withContext(Dispatchers.Main) {
                    addTableRows(repoResponse.result.map {
                        RecentEventsListViewModel(
                            format.format(it.date),
                            it.soundLabel,
                            it.probability.toString() + "%",
                            it.date
                        )
                    })
                }
            } else {
                loadNewData()
                return@launch
            }

            withContext(Dispatchers.Main) {
                loadingCircle.visibility = View.GONE
                refreshButton.imageAlpha = 255
            }
        }
    }

    fun addTableRow(viewModel: RecentEventsListViewModel) {
        if (latestAdded >= viewModel.date) return
        latestAdded = viewModel.date
        adapter.data.add(0, viewModel)
        adapter.notifyDataSetChanged()
    }

    private fun addTableRows(viewModels: List<RecentEventsListViewModel>) {
        for (viewModel in viewModels.reversed()) {
            if (latestAdded >= viewModel.date) continue
            latestAdded = viewModel.date
            adapter.data.add(0, viewModel)
        }
        adapter.notifyDataSetChanged()
    }

}