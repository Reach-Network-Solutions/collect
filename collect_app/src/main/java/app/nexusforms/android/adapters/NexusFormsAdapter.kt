package app.nexusforms.android.adapters

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.databinding.ItemNexusFormBinding
import app.nexusforms.android.provider.FormsProviderAPI

class NexusFormsAdapter(private var dataCursor: Cursor?) :
    RecyclerView.Adapter<NexusFormsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NexusFormsAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val formBinder = ItemNexusFormBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(formBinder)
    }

    override fun onBindViewHolder(
        holder: NexusFormsAdapter.ViewHolder,
        position: Int
    ) {

        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return dataCursor?.count ?: 0
    }

    inner class ViewHolder(private val binding: ItemNexusFormBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(formItemCursor: Cursor?) {

            if (formItemCursor == null) {
                return
            }

            // Extract properties from cursor
            val formName = formItemCursor.getString(formItemCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.DISPLAY_NAME))
            val formStatus = formItemCursor.getString(formItemCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.JR_VERSION))
            val date = formItemCursor.getString(formItemCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.JR_FORM_ID))

            binding.formNameInList.text = formName
            binding.dateLastUpdated.text = date
            binding.formStatus.text = formStatus
        }

    }

    fun changeCursor(cursor: Cursor?) {
        val old = swapCursor(cursor)
        old?.close()
    }

    fun swapCursor(cursor: Cursor?): Cursor? {
        if (dataCursor === cursor) {
            return null
        }
        val oldCursor = dataCursor
        dataCursor = cursor
        if (cursor != null) {
            notifyDataSetChanged()
        }
        return oldCursor
    }

    private fun getItem(position: Int): Cursor? {
        dataCursor!!.moveToPosition(position)
        // Load data from dataCursor and return it...
        return dataCursor
    }
}