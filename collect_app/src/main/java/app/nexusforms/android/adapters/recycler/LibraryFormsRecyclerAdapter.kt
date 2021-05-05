package app.nexusforms.android.adapters.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.R
import app.nexusforms.android.activities.FormDownloadListActivity
import app.nexusforms.android.adapters.recycler.LibraryFormsRecyclerAdapter.ViewHolder.Companion.from
import app.nexusforms.android.databinding.ItemFormsLibraryBinding
import app.nexusforms.android.formmanagement.Constants
import app.nexusforms.android.formmanagement.Constants.Companion.FORM_VERSION_KEY
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptFocal
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocal
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.ArrayList

class LibraryFormsRecyclerAdapter(
    private val list: ArrayList<HashMap<String, String>>,
    val selectForDownloadClickListener: OnClickListener

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
                formId = form[Constants.FORM_ID_KEY],
                formIdDisplay = form[FormDownloadListActivity.FORMID_DISPLAY],
                formDetailsKey = form[Constants.FORMDETAIL_KEY]
            )
        }.also {
            holder.bind(it[0])
            //TODO SET CLICK LISTENERS
            holder.binding.checkboxDownload.setOnCheckedChangeListener { _, isChecked ->
                selectForDownloadClickListener.onClick(it[0], isChecked)
            }
        }

    }

    override fun getItemCount(): Int = list.size


    class OnClickListener(val onClickListener: (form: DownloadForms, isChecked: Boolean) -> Unit) {
        fun onClick(form: DownloadForms, isChecked: Boolean) = onClickListener(form, isChecked)
    }

    class ViewHolder(val binding: ItemFormsLibraryBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(form: DownloadForms) {
            val name = form.formName
            val version = form.formVersion

            binding.textFormName.text = name
            binding.textFormsVersion.text = if (version == "") form.formId else
                "${binding.root.context.resources.getString(R.string.version)} $version"
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
            val formId: String?,
            val formIdDisplay: String?,
            val formDetailsKey: String?,
        )
    }
}
