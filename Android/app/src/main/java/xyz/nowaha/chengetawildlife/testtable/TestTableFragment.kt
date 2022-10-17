package xyz.nowaha.chengetawildlife.testtable

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import xyz.nowaha.chengetawildlife.ApiAccessor
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.Session
import xyz.nowaha.chengetawildlife.extensions.getBoolean
import xyz.nowaha.chengetawildlife.extensions.getString
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class TestTableFragment : Fragment(R.layout.fragment_test_table) {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecentEventsListAdapter
    var data: ArrayList<RecentEventsListViewModel> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.tableRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        this.adapter = RecentEventsListAdapter(requireActivity().applicationContext, data)
        recyclerView.adapter = this.adapter

        lifecycleScope.launch(Dispatchers.IO) {
            Session.key = ApiAccessor.attemptLogin("admin", "Pass123")!!.getString("sessionKey")
            loadNewData(100)

            while (true) {
                delay(2000)
                loadNewData(5)
            }
        }
    }

    var highestIdLoaded = -1
    suspend fun loadNewData(amount: Int) {
        var data = ApiAccessor.getLatestEvents(amount)
        if (data != null) {
            if (data.getBoolean("success")!!) {
                println(data.getString("message"))

                var format = SimpleDateFormat("HH:mm:ss")
                var dataArray = data["data"]?.jsonArray!!
                for (valueRaw in dataArray.reversed()) {
                    var asObject = valueRaw.jsonObject!!
                    var id = asObject["Id"]!!.jsonPrimitive.int

                    if (id > highestIdLoaded) {
                        highestIdLoaded = id
                    } else {
                        break
                    }

                    withContext(Dispatchers.Main) {
                        var date = Date(asObject["Date"]!!.jsonPrimitive.long)
                        var label = asObject.getString("SoundLabel")!!
                        label = label[0].uppercase() + label.substring(1)

                        addTableRow(RecentEventsListViewModel(
                            format.format(date),
                            label,
                            asObject["Probability"]!!.jsonPrimitive.int.toString() + "%"
                        ))
                    }
                }
            }
        }
    }

    fun useTestData() {
        data.clear()

        var format = SimpleDateFormat("HH:mm:ss")

        // Add a new row every 1000ms
        lifecycleScope.launch {
            while (true) {
                val date = Date(System.currentTimeMillis())
                val str = format.format(date)
                addTableRow(RecentEventsListViewModel(str, "Value test", "Value test2"))
                delay(1000)
            }
        }
    }

    fun addTableRow(viewModel: RecentEventsListViewModel) {
        adapter.data.add(0, viewModel)
        adapter.notifyDataSetChanged()
    }

}