package xyz.nowaha.chengetawildlife.ui.admin.accountoverview.table

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.Adapter
import xyz.nowaha.chengetawildlife.R
import xyz.nowaha.chengetawildlife.ui.admin.accountoverview.AccountOverviewFragmentDirections

class AccountOverviewAdapter(
    private val context: Context,
    private val view: View,
    var data: LiveData<List<AccountOverviewDataModel>>
) :
    Adapter<AccountOverviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountOverviewViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.template_overview_table_row, parent, false)
        return AccountOverviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountOverviewViewHolder, position: Int) {
        val actualData = data.value ?: arrayListOf()
        val currentData = actualData[position]
        holder.bindData(currentData)

        if (position % 2 != 0) {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(R.color.alt_table_row))
        } else {
            holder.rowLinearLayout.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        }

        holder.editButton.setOnClickListener {
            view.findNavController().navigate(AccountOverviewFragmentDirections.actionAccountOverviewFragmentToEditAccountFragment(currentData.username))
        }
        holder.deleteButton.setOnClickListener {
            view.findNavController().navigate(AccountOverviewFragmentDirections.actionAccountOverviewFragmentToDeleteAccountFragment(currentData.username))
        }
    }

    override fun getItemCount(): Int = data.value?.size ?: 0

    data class AccountOverviewDataModel(
        var creationDate: Long,
        var username: String,
        var role: Int
    )
}