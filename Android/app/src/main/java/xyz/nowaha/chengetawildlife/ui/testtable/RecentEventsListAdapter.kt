package xyz.nowaha.chengetawildlife.ui.testtable

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import kotlinx.coroutines.flow.update
import xyz.nowaha.chengetawildlife.R

class RecentEventsListAdapter(
    private val context: Context,
    private val fragment: RecentEventsListFragment,
    var data: ArrayList<RecentEventsListDataModel>,
    var filteredData: List<RecentEventsListDataModel>,
) : Adapter<RecentEventsListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentEventsListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.template_table_row_event, parent, false)
        return RecentEventsListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentEventsListViewHolder, position: Int) {
        val data = filteredData[position]
        holder.bindData(data)

        if (position % 2 != 0) {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(R.color.alt_table_row))
        } else {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        }

        holder.rowLinearLayout.setOnClickListener { fragment.eventSelectionPipeViewModel.eventSelected.postValue(data.eventId) }
    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}