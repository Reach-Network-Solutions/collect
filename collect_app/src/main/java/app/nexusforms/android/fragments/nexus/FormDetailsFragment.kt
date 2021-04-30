package app.nexusforms.android.fragments.nexus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.nexusforms.android.databinding.FormDetailsFragmentBinding

class FormDetailsFragment : Fragment() {

    private lateinit var binding : FormDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FormDetailsFragmentBinding.inflate(inflater, container, false)

        setSampleData()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setSampleData() {
        binding.textCreatedBy.text = "Reach Network Solution"
        binding.textVersion.text = "3.0"
        binding.textCreatedDate.text = "29 march, 2021"
        binding.textDownloadDate.text = "31 March, 2021"
        binding.textNumberOfQuestions.text = "30 Questions"
    }

}