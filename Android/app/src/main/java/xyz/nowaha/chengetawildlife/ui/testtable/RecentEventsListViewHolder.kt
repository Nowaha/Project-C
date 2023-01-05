package xyz.nowaha.chengetawildlife.ui.testtable

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.nowaha.chengetawildlife.R

class RecentEventsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var rowLinearLayout: LinearLayout = itemView.findViewById(R.id.rowLinearLayout)
    var value1TextView: TextView = itemView.findViewById(R.id.text1)
    var value2TextView: TextView = itemView.findViewById(R.id.text2)
    var value3TextView: TextView = itemView.findViewById(R.id.text3)
    var value4TextView: TextView = itemView.findViewById(R.id.text4)

    fun bindData(viewModel: RecentEventsListDataModel) {
        value1TextView.text = viewModel.text1Value
        value2TextView.text = viewModel.text2Value
        value3TextView.text = viewModel.text3Value
        value4TextView.text = viewModel.text4Value
    }

}