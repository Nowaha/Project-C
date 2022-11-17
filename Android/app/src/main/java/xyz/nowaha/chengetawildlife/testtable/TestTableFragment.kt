package xyz.nowaha.chengetawildlife.testtable

import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import retrofit2.Response
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.http.APIClient
import xyz.nowaha.chengetawildlife.pojo.EventListResponse
import java.net.ConnectException
import java.net.ProtocolException
import java.net.SocketTimeoutException
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

    fun loadNewData() {
        loadingCircle.visibility = View.VISIBLE
        refreshButton.imageAlpha = 0

        lifecycleScope.launch(Dispatchers.IO) {
            delay(100)
            val format = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
            val events: Response<EventListResponse>
            try {
                events = APIClient.getAPIInterface().getLatestEvents(100).execute()
            } catch (ex: Exception) {
                loadNewData()
                ex.printStackTrace()
                return@launch
            }

            if (events.isSuccessful && events.body() != null && events.body()!!.data != null) {
                withContext(Dispatchers.Main) {
                    addTableRows(events.body()!!.data!!.map {
                        RecentEventsListViewModel(
                            format.format(it.date),
                            it.soundLabel,
                            it.probability.toString() + "%",
                            it.date
                        )
                    })
                }
            }

            lifecycleScope.launch(Dispatchers.Main) {
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