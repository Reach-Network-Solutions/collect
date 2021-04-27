package app.nexusforms.android.fragments.nexus

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.nexusforms.android.R

class FormsSummaryFragment : Fragment() {

    companion object {
        fun newInstance() = FormsSummaryFragment()
    }

    private lateinit var viewModel: FormsSummaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forms_summary_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FormsSummaryViewModel::class.java)
        // TODO: Use the ViewModel
    }

}