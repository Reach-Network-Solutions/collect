package app.nexusforms.android.adapters.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.nexusforms.android.R
import app.nexusforms.android.adapters.recycler.MyFormsRecyclerAdapter.ViewHolder.Companion.from
import app.nexusforms.android.databinding.ItemFormsBinding

import app.nexusforms.android.forms.Form
import java.util.*

class MyFormsRecyclerAdapter(
    private val list: ArrayList<Form>,
    val selectForDownloadClickListener: (selection : Form) -> Unit
) :
    RecyclerView.Adapter<MyFormsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val form = list[position]

            holder.bind(form)

            holder.binding.root.setOnClickListener { view ->
                selectForDownloadClickListener(form)

        }

    }

    override fun getItemCount(): Int = list.size


    class ViewHolder(val binding: ItemFormsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(form: Form) {
            val name = form.displayName
            val version = form.jrVersion

            binding.textFormName .text = name
            binding.textFormTemplateName.text = if (version == "") form.jrFormId else
                "${binding.root.context.resources.getString(R.string.version)} $version"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val itemBinding = ItemFormsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                return ViewHolder(itemBinding)
            }

        }
    }

}
