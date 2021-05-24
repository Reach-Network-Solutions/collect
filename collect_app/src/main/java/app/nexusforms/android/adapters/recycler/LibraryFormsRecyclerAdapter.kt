package app.nexusforms.android.adapters.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.R
import app.nexusforms.android.activities.viewmodels.FormDownloadListViewModel
import app.nexusforms.android.adapters.recycler.LibraryFormsRecyclerAdapter.ViewHolder.Companion.from
import app.nexusforms.android.databinding.ItemFormsLibraryBinding
import app.nexusforms.android.formmanagement.Constants
import app.nexusforms.android.formmanagement.Constants.Companion.FORMID_DISPLAY
import app.nexusforms.android.formmanagement.Constants.Companion.FORMNAME
import app.nexusforms.android.formmanagement.Constants.Companion.FORM_VERSION_KEY
import java.util.*

class LibraryFormsRecyclerAdapter(
    private val list: ArrayList<HashMap<String, String>>,
    val selectForDownloadClickListener: OnClickListener,
    private val viewModel: FormDownloadListViewModel

) :
    RecyclerView.Adapter<LibraryFormsRecyclerAdapter.ViewHolder>() {

    private var isSelectAll = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val form = list[position]
        form.map {
            DownloadForms(
                formName = form[FORMNAME],
                formVersion = form[FORM_VERSION_KEY],
                formId = form[Constants.FORM_ID_KEY],
                formIdDisplay = form[FORMID_DISPLAY],
                formDetailsKey = form[Constants.FORMDETAIL_KEY]
            )
        }.also {
            holder.bind(it[0], isSelectAll)
            //TODO SET CLICK LISTENERS
            holder.binding.checkboxDownload.setOnCheckedChangeListener { _, isChecked ->
                selectForDownloadClickListener.onClick(it[0], isChecked)
            }
        }

    }

    override fun getItemCount(): Int = list.size

    fun selectAll() {
        isSelectAll = true
        notifyDataSetChanged()
    }

    fun deSelectAll() {
        isSelectAll = false
        notifyDataSetChanged()
    }


    class OnClickListener(val onClickListener: (form: DownloadForms, isChecked: Boolean) -> Unit) {
        fun onClick(form: DownloadForms, isChecked: Boolean) = onClickListener(form, isChecked)
    }

    class ViewHolder(val binding: ItemFormsLibraryBinding, val viewModel: FormDownloadListViewModel) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(form: DownloadForms, isSelectAll: Boolean) {
            val name = form.formName
            val version = form.formVersion

            binding.textFormName.text = name
            binding.textFormsVersion.text = if (version == "") form.formId else
                "${binding.root.context.resources.getString(R.string.version)} $version"
            binding.checkboxDownload.isChecked = isSelectAll

            val formObject = viewModel.formDetailsByFormId[form.formId]

            if (form.formId != null
                && formObject?.isUpdated == true
            ) {
                binding.formUpdateAlert.visibility = View.VISIBLE
            } else {
                binding.formUpdateAlert.visibility = View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: FormDownloadListViewModel): ViewHolder {
                val itemBinding = ItemFormsLibraryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return ViewHolder(itemBinding, viewModel)
            }

        }
    }

    companion object {
        data class DownloadForms(
            val formName: String?,
            val formVersion: String?,
            val formId: String?,
            val formIdDisplay: String?,
            val formDetailsKey: String?,
        )
    }
}
