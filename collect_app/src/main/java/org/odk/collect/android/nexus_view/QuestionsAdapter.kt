package org.odk.collect.android.nexus_view

import android.content.Context
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import java.util.*

private const val TAG = "MembersForTeamAdapter"

class QuestionsAdapter(
    private val context: Context,
    private val dataSource: List<FormEntryPrompt>,
) : RecyclerView.Adapter<QuestionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nexus_recycler_item, parent, false)


        return ViewHolder(view)

    }

    override fun getItemCount(): Int = dataSource.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pQuestionText.text = dataSource[position].questionText

        holder.pHelperText.text = dataSource[position].helpText

        holder.pInputType.text = dataSource[position].dataType.toString()


    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {

        val pQuestionText: TextView = mView.findViewById(R.id.question_text)

        val pHelperText: TextView = mView.findViewById(R.id.question_help_text)

        val pInputType: TextView = mView.findViewById(R.id.question_help_text)

    }

    override fun getItemId(position: Int): Long {
        //TODO should return userName for member
        return position.toLong()
    }


}