package app.nexusforms.android.fragments.nexus

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.nexusforms.android.R
import app.nexusforms.android.activities.FormEntryActivity
import app.nexusforms.android.activities.FormEntryActivity.KEY_FORMPATH
import app.nexusforms.android.activities.FormEntryActivity.NEWFORM
import app.nexusforms.android.adapters.recycler.MyFormsRecyclerAdapter
import app.nexusforms.android.database.DatabaseFormsRepository
import app.nexusforms.android.databinding.FillFormFragmentBinding
import app.nexusforms.android.forms.Form
import app.nexusforms.android.provider.FormsProviderAPI
import timber.log.Timber
import java.util.*

class FillFormFragment : Fragment() {

    private lateinit var binding: FillFormFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FillFormFragmentBinding.inflate(inflater, container, false)

        initView()

        return binding.root
    }

    private fun formSelectedOnList(selectedForm: Form) {

        val intent = Intent(requireContext(), FormEntryActivity::class.java)

        val formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, selectedForm.id)

        intent.putExtra(KEY_FORMPATH, selectedForm.formFilePath)

        intent.putExtra(NEWFORM, true)

        intent.data = formUri

        val navHostFragment =
            activity?.supportFragmentManager?.findFragmentById(R.id.fragment_container_main) as NavHostFragment?
        val navController = navHostFragment?.navController
        navController?.popBackStack()

        startActivity(intent)

    }

    private fun initView() {

        val formsListAsMutableList = DatabaseFormsRepository().all

        if(formsListAsMutableList.isEmpty()){
            binding.textViewNoFormAvailable.visibility = View.VISIBLE
        }else{
            binding.textViewNoFormAvailable.visibility = View.INVISIBLE
        }

        with(binding.allFormsRecycler) {

            val formsAdapter = MyFormsRecyclerAdapter(
                formsListAsMutableList as ArrayList<Form>,
                ::formSelectedOnList
            )

            layoutManager = LinearLayoutManager(context)

            adapter = formsAdapter

        }

    }

}