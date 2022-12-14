package xyz.nowaha.chengetawildlife.ui.admin.accountoverview.table

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.nowaha.chengetawildlife.R

class AccountOverviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val rowLinearLayout: LinearLayout = itemView.findViewById(R.id.rowLinearLayout)
    val value1TextView: TextView = itemView.findViewById(R.id.text1)
    val value2TextView: TextView = itemView.findViewById(R.id.text2)
    val editButton: ImageButton = itemView.findViewById(R.id.editAccountButton)
    val deleteButton: ImageButton = itemView.findViewById(R.id.deleteAccountButton)

    fun bindData(viewModel: AccountOverviewAdapter.AccountOverviewDataModel) {
        value1TextView.text = viewModel.username
        value2TextView.text = if (viewModel.role == 1) "Admin" else "Ranger"
    }

}