package app.nexusforms.android.fragments.nexus

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.nexusforms.android.adapters.recycler.MyFormsRecyclerAdapter
import app.nexusforms.android.database.DatabaseFormsRepository
import app.nexusforms.android.databinding.MyFormsFragmentBinding
import app.nexusforms.android.forms.Form
import app.nexusforms.android.injection.DaggerUtils
import timber.log.Timber
import java.util.ArrayList

class MyFormsFragment : Fragment() {

    lateinit var myFormsFragmentBinding: MyFormsFragmentBinding

    companion object {
        fun newInstance() = MyFormsFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerUtils.getComponent(context).inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myFormsFragmentBinding =
            MyFormsFragmentBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        //attempt to paint some view with all the forms
        initView()

        Toast.makeText(requireContext(), "FORMS", Toast.LENGTH_SHORT).show()

        return myFormsFragmentBinding.root


    }

    private fun formSelectedOnList(selectedForm: Form){
        Timber.d("Selected %s", selectedForm.displayName)
    }

    private fun initView() {


        val formsListAsMutableList = DatabaseFormsRepository().all

            with(myFormsFragmentBinding.allFormsRecycler) {

                val formsAdapter = MyFormsRecyclerAdapter(formsListAsMutableList as ArrayList<Form>, ::formSelectedOnList)

                layoutManager = LinearLayoutManager(context)

                adapter = formsAdapter


            }
        }


}

