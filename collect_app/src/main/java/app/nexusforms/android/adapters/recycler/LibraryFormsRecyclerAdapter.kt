package app.nexusforms.android.adapters.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.activities.FormDownloadListActivity
import app.nexusforms.android.adapters.recycler.LibraryFormsRecyclerAdapter.ViewHolder.Companion.from
import app.nexusforms.android.databinding.ItemFormsLibraryBinding
import app.nexusforms.android.formmanagement.Constants
import app.nexusforms.android.formmanagement.Constants.Companion.FORM_VERSION_KEY
import java.util.ArrayList

class LibraryFormsRecyclerAdapter(
    private val list: ArrayList<HashMap<String, String>>,
    val favoriteClickListener: OnClickListener,
    val downloadClickListener: OnClickListener
) :
    RecyclerView.Adapter<LibraryFormsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val form = list[position]
        form.map {
            DownloadForms(
                formName = form[FormDownloadListActivity.FORMNAME],
                formVersion = form[FORM_VERSION_KEY],
                formId = form[Constants.FORM_ID_KEY]
            )
        }.also {
            holder.bind(it[0])
            holder.binding.imageItemFormsFavourite.setOnClickListener { _ ->
                favoriteClickListener.onClick(it[0])
            }
            holder.binding.imageItemFormsDownload.setOnClickListener { _ ->
                downloadClickListener.onClick(it[0])
            }
        }

    }

    override fun getItemCount(): Int = list.size


    class OnClickListener(val onClickListener: (form: DownloadForms) -> Unit) {
        fun onClick(form: DownloadForms) = onClickListener(form)
    }

    class ViewHolder(val binding: ItemFormsLibraryBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("LogNotTimber")
        fun bind(form: DownloadForms) {
            val name = form.formName
            val version = form.formVersion

            binding.textFormName.text = name
            binding.textFormsVersion.text = version
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

    companion object {
        data class DownloadForms(
            val formName: String?,
            val formVersion: String?,
            val formId: String?
        )
    }
}
