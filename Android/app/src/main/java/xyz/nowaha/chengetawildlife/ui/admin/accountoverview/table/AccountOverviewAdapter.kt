package xyz.nowaha.chengetawildlife.ui.admin.accountoverview.table

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView.Adapter
import xyz.nowaha.chengetawildlife.R

class AccountOverviewAdapter(
    private val context: Context,
    var data: LiveData<List<AccountOverviewDataModel>>
) :
    Adapter<AccountOverviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountOverviewViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.template_table_row, parent, false)
        return AccountOverviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountOverviewViewHolder, position: Int) {
        val actualData = data.value ?: arrayListOf()
        holder.bindData(actualData[position])

        if (position % 2 != 0) {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(R.color.alt_table_row))
        } else {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        }
    }

    override fun getItemCount(): Int = data.value?.size ?: 0

    data class AccountOverviewDataModel(
        var creationDate: Long,
        var username: String,
        var role: Int
    )
}