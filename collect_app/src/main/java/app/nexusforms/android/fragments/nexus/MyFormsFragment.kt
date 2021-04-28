package app.nexusforms.android.fragments.nexus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.nexusforms.android.adapters.NexusFormsAdapter
import app.nexusforms.android.dao.CursorLoaderFactory
import app.nexusforms.android.databinding.MyFormsFragmentBinding
import app.nexusforms.android.provider.InstanceProviderAPI.InstanceColumns

class MyFormsFragment : Fragment() {

    lateinit var myFormsFragmentBinding: MyFormsFragmentBinding

    companion object {
        fun newInstance() = MyFormsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myFormsFragmentBinding = MyFormsFragmentBinding.inflate(LayoutInflater.from(context), container, false)
        //attempt to paint some view with all the forms
        initView()

        return myFormsFragmentBinding.root


    }

    private fun initView(){

        val formsCursor = CursorLoaderFactory().createSavedInstancesCursorLoader("")

        val readyCursor = formsCursor.loadInBackground()

        with(myFormsFragmentBinding.allFormsRecycler) {

            val formsAdapter = NexusFormsAdapter(readyCursor)

            layoutManager = LinearLayoutManager(context)

           adapter = formsAdapter
        }

    }



}