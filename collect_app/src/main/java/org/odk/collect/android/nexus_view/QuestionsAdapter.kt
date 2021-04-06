package org.odk.collect.android.nexus_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.widgets.QuestionWidget

private const val TAG = "QuestionAdapterTag"

class QuestionsAdapter(
    private val dataSource: List<QuestionWidget>,

    ) : RecyclerView.Adapter<QuestionsAdapter.QuestionWidgetViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuestionWidgetViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nexus_recycler_item, parent, false)

        return QuestionWidgetViewHolder(view)
    }


    override fun getItemCount(): Int = dataSource.size


    override fun onBindViewHolder(holder: QuestionWidgetViewHolder, position: Int) {

        holder.linearLayoutContainer.removeAllViews()

        if (holder.linearLayoutContainer.parent != null) {
            (holder.linearLayoutContainer.parent as ViewGroup).removeView(holder.itemView)
        }

        val questionWidget: QuestionWidget =
            dataSource[position]

        holder.linearLayoutContainer.addView(questionWidget)

    }

    inner class QuestionWidgetViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val linearLayoutContainer: LinearLayout =
            mView.findViewById(R.id.custom_question_widget_container)

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


}