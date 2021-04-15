package org.odk.collect.android.nexus_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.javarosa.core.model.FormIndex
import org.odk.collect.android.R
import org.odk.collect.android.listeners.WidgetValueChangedListener
import org.odk.collect.android.widgets.QuestionWidget

private const val TAG = "QuestionAdapterTag"

class QuestionsAdapter(
    private val dataSource: MutableList<out QuestionWidget>,
    private val notifyAnswerChanged: (changedWidget: QuestionWidget?) -> Int

) : RecyclerView.Adapter<QuestionsAdapter.QuestionWidgetViewHolder>(),
    WidgetValueChangedListener {

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

        holder.linearLayoutContainer.removeAllViewsInLayout()

        val questionWidget: QuestionWidget =
            dataSource[position]

        questionWidget.setValueChangedListener(this)

        val initialWidgetParent: ViewParent? = questionWidget.parent

        (initialWidgetParent as? ViewGroup)?.removeView(questionWidget)

        holder.linearLayoutContainer.addView(questionWidget)


    }


    inner class QuestionWidgetViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val linearLayoutContainer: LinearLayout =
            mView.findViewById(R.id.custom_question_widget_container)

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getPositionForWidget(candidateWidget: FormIndex): Int {
        return binarySearch(candidateWidget)
    }

    private fun binarySearch(x: FormIndex): Int {
        var l = 0
        val r = dataSource.size - 1

        while (l <= r) {
            // Check if x is present at mid
            val checking  = dataSource[l].questionDetails.prompt
            if (checking.index === x) return l
            l += 1
        }

        // if we reach here, then element was
        // not present
        return -1
    }



    fun getQuestionWidgetAt(position: Int): QuestionWidget {
        return dataSource[position]
    }


    override fun widgetValueChanged(changedWidget: QuestionWidget?) {
        notifyAnswerChanged(changedWidget)
    }


}