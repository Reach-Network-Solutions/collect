package org.odk.collect.android.nexus_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.javarosa.core.model.FormIndex
import org.odk.collect.android.R
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.listeners.WidgetValueChangedListener
import org.odk.collect.android.widgets.QuestionWidget

class QuestionsAdapter(
    private val dataSource: MutableList<out QuestionWidget>,
    private val formController: FormController,

    ) : ListAdapter<QuestionWidget,QuestionsAdapter.QuestionWidgetViewHolder>(QuestionWidgetDiffCallback()),
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

        val initialWidgetParent : ViewParent? = questionWidget.parent

       if(initialWidgetParent == null) {
           holder.linearLayoutContainer.addView(questionWidget)
       }

    }

    fun getItemPosition (index : FormIndex): Int {
        var indexPosition : Int = 0
        dataSource.forEach { questionWidget->
            if(index == questionWidget.formEntryPrompt.index){
                indexPosition =  dataSource.indexOf(questionWidget)
            }
        }

        return indexPosition
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class QuestionWidgetViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val linearLayoutContainer: LinearLayout =
            mView.findViewById(R.id.custom_question_widget_container)

    }

    class QuestionWidgetDiffCallback : DiffUtil.ItemCallback<QuestionWidget>() {
        override fun areItemsTheSame(oldItem: QuestionWidget, newItem: QuestionWidget): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: QuestionWidget, newItem: QuestionWidget): Boolean {
            return oldItem.formEntryPrompt.index == newItem.formEntryPrompt.index
        }

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    fun getQuestionWidgetAt(position: Int): QuestionWidget {
        return dataSource[position]
    }

    override fun widgetValueChanged(changedWidget: QuestionWidget?) {

        val notifiedAnswer = changedWidget?.answer

        val notifiedIndex = changedWidget?.formEntryPrompt?.index

        formController.saveAnswer(notifiedIndex, notifiedAnswer)

    }


}