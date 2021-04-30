package app.nexusforms.android.adapters

import android.database.Cursor
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.R
import app.nexusforms.android.databinding.ItemFormsBinding
import app.nexusforms.android.formmanagement.Constants
import app.nexusforms.android.provider.FormsProviderAPI
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NexusFormsAdapter(
    private var dataCursor: Cursor?,
    var selectionType : Constants.HomeFormSelection,
    var openForm: (formId: Cursor?) -> Unit,
    var uploadForm: (id: Long) -> Unit
) :
    RecyclerView.Adapter<NexusFormsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NexusFormsAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val formBinder = ItemFormsBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(formBinder)
    }

    override fun onBindViewHolder(
        holder: NexusFormsAdapter.ViewHolder,
        position: Int
    ) {

        holder.bind(getItem(position))
        val item = getItem(position)
        holder.itemView.setOnClickListener {

            openForm(item)

        }

        holder.itemView.setOnLongClickListener {

            if(selectionType != Constants.HomeFormSelection.COMPLETED){
                return@setOnLongClickListener true
            }

            val popup = PopupMenu(holder.itemView.context, holder.itemView)
            popup.inflate(R.menu.popupmenu)

            popup.setOnMenuItemClickListener { itemMenu: MenuItem? ->

                when (itemMenu!!.itemId) {
                    R.id.send_to_server_option -> {
                        Toast.makeText(holder.itemView.context, itemMenu.title, Toast.LENGTH_SHORT)
                            .show()
                        if (item != null) {
                            uploadForm(
                                item.getString(item.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns._ID))
                                    .toLong()
                            )



                        } else {
                            Toast.makeText(
                                holder.itemView.context,
                                "Null selection",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }

                }

                true
            }

            popup.show()

            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return dataCursor?.count ?: 0
    }

    inner class ViewHolder(private val binding: ItemFormsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(formItemCursor: Cursor?) {

            if (formItemCursor == null) {
                return
            }

            // Extract properties from cursor
            val formName =
                formItemCursor.getString(formItemCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.DISPLAY_NAME))
            val date =
                formItemCursor.getString(formItemCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.DATE))
            val formTemplateName =
                formItemCursor.getString(formItemCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.JR_FORM_ID))

            // define once somewhere in order to reuse it
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val timeFormatter = DateTimeFormatter.ofPattern(" HH:mm a")


            // JVM representation of a millisecond epoch absolute instant
            val instant = Instant.ofEpochMilli(date.toLong())

            // Adding the timezone information to be able to format it (change accordingly)
            val dateF = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

            binding.textFormName.text = formName
            binding.textFormTemplateName.text = formTemplateName
            binding.textDateFilled.text = dateFormatter.format(dateF)
            binding.textTimeFilled.text = timeFormatter.format(dateF)
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