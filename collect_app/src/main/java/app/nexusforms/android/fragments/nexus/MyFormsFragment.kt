package app.nexusforms.android.fragments.nexus

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.nexusforms.android.adapters.NexusFormsAdapter
import app.nexusforms.android.dao.CursorLoaderFactory
import app.nexusforms.android.databinding.MyFormsFragmentBinding
import app.nexusforms.android.injection.DaggerUtils
import app.nexusforms.android.provider.FormsProviderAPI
import app.nexusforms.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME
import app.nexusforms.android.provider.InstanceProviderAPI.InstanceColumns
import timber.log.Timber

class MyFormsFragment : Fragment() {

    lateinit var myFormsFragmentBinding: MyFormsFragmentBinding

    companion object {
        fun newInstance() = MyFormsFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerUtils.getComponent(context).Inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myFormsFragmentBinding = MyFormsFragmentBinding.inflate(LayoutInflater.from(requireContext()), container, false)

        //attempt to paint some view with all the forms
        initView()

        return myFormsFragmentBinding.root
    }

    private fun initView(){

        val formsCursor = CursorLoaderFactory().createSavedInstancesCursorLoader("")

        val readyCursor = formsCursor.loadInBackground()

        if(readyCursor == null){
            Timber.d("EMPTY list")
            return
        }
        var count = 0
        while(count < readyCursor.count){
            count++

            readyCursor.moveToPosition(count)
            Timber.d("PASSING %s", readyCursor.getString(readyCursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.DISPLAY_NAME)))
        }

        with(myFormsFragmentBinding.allFormsRecycler) {

            val formsAdapter = NexusFormsAdapter(readyCursor)

            layoutManager = LinearLayoutManager(context)

           adapter = formsAdapter
        }

    }



}