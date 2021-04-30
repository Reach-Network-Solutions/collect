package app.nexusforms.android.fragments.nexus

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.nexusforms.android.R
import app.nexusforms.android.databinding.FillFormFragmentBinding

class FillFormFragment : Fragment() {

    private lateinit var binding: FillFormFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FillFormFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

}