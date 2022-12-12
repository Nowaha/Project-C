package xyz.nowaha.chengetawildlife.ui.testtable

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import xyz.nowaha.chengetawildlife.R

class RecentEventsListAdapter(
    private val context: Context,
    var data: ArrayList<RecentEventsListDataModel>
) : Adapter<RecentEventsListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentEventsListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.template_table_row, parent, false)
        return RecentEventsListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentEventsListViewHolder, position: Int) {
        holder.bindData(data[position])

        if (position % 2 != 0) {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(R.color.alt_table_row))
        } else {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}