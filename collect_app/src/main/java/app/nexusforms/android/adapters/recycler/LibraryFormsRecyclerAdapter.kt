package app.nexusforms.android.adapters.recycler

import android.annotation.SuppressLint
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.R
import app.nexusforms.android.activities.FormDownloadListActivity
import app.nexusforms.android.adapters.recycler.LibraryFormsRecyclerAdapter.ViewHolder.Companion.from
import app.nexusforms.android.databinding.ItemFormsLibraryBinding
import app.nexusforms.android.formmanagement.Constants
import app.nexusforms.android.formmanagement.Constants.Companion.FORM_VERSION_KEY
import java.util.ArrayList

class LibraryFormsRecyclerAdapter(private val list: ArrayList<HashMap<String, String>>) :
    RecyclerView.Adapter<LibraryFormsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val form = list[position]
        holder.bind(form)
    }

    override fun getItemCount(): Int = list.size


    class ViewHolder(val binding: ItemFormsLibraryBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("LogNotTimber")
        fun bind(form: HashMap<String, String>) {
            val name = form[FormDownloadListActivity.FORMNAME]
            val version = form[FORM_VERSION_KEY]

            binding.textItemFormName.text = name
            binding.textItemFormsVersion.text = version
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val itemBinding = ItemFormsLibraryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return ViewHolder(itemBinding)
            }

        }
    }
}