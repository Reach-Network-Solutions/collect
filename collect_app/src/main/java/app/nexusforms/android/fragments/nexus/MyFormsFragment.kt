package app.nexusforms.android.fragments.nexus

import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.loader.content.CursorLoader
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.nexusforms.android.R
import app.nexusforms.android.activities.FormEntryActivity
import app.nexusforms.android.activities.InstanceUploaderActivity
import app.nexusforms.android.activities.InstanceUploaderListActivity
import app.nexusforms.android.adapters.NexusFormsAdapter
import app.nexusforms.android.dao.CursorLoaderFactory
import app.nexusforms.android.databinding.MyFormsFragmentBinding
import app.nexusforms.android.formmanagement.Constants
import app.nexusforms.android.formmanagement.Constants.Companion.IS_INTRO_FORMS
import app.nexusforms.android.gdrive.GoogleSheetsUploaderActivity
import app.nexusforms.android.injection.DaggerUtils
import app.nexusforms.android.instances.Instance
import app.nexusforms.android.network.NetworkStateProvider
import app.nexusforms.android.preferences.keys.GeneralKeys
import app.nexusforms.android.preferences.source.SettingsProvider
import app.nexusforms.android.provider.InstanceProviderAPI.InstanceColumns
import app.nexusforms.android.utilities.ApplicationConstants
import app.nexusforms.android.utilities.DialogUtils
import app.nexusforms.android.utilities.PlayServicesChecker
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptFocal
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocal
import java.lang.Boolean
import javax.inject.Inject

class MyFormsFragment : Fragment() {

    lateinit var myFormsFragmentBinding: MyFormsFragmentBinding

    @Inject
    lateinit var connectivityProvider: NetworkStateProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var alertDialog: AlertDialog

    var currentScreen: Constants.HomeFormSelection = Constants.HomeFormSelection.DRAFTS

    companion object {
        fun newInstance() = MyFormsFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onResume() {
        super.onResume()
        initRecyclerView(currentScreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myFormsFragmentBinding =
            MyFormsFragmentBinding.inflate(LayoutInflater.from(requireContext()), container, false)

        setOnClickListener()
        setupButtons()
        initRecyclerView(Constants.HomeFormSelection.DRAFTS)

        return myFormsFragmentBinding.root
    }

    private fun setupButtons() {
        myFormsFragmentBinding.buttonFilterDraft.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        myFormsFragmentBinding.buttonFilterCompleted.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_color))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        myFormsFragmentBinding.buttonFilterFailedUploads.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_color))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

    }


    private fun setOnClickListener() {
        myFormsFragmentBinding.buttonFilterDraft.setOnClickListener {
            myFormsFragmentBinding.buttonFilterDraft.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            myFormsFragmentBinding.buttonFilterCompleted.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_color
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            myFormsFragmentBinding.buttonFilterFailedUploads.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_color
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            initRecyclerView(Constants.HomeFormSelection.DRAFTS)
        }
        myFormsFragmentBinding.buttonFilterCompleted.setOnClickListener {
            myFormsFragmentBinding.buttonFilterCompleted.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            myFormsFragmentBinding.buttonFilterDraft.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_color
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            myFormsFragmentBinding.buttonFilterFailedUploads.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_color
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            initRecyclerView(Constants.HomeFormSelection.COMPLETED)
        }


        myFormsFragmentBinding.buttonFilterFailedUploads.setOnClickListener {
            myFormsFragmentBinding.buttonFilterFailedUploads.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_blue))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            myFormsFragmentBinding.buttonFilterCompleted.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_color
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            myFormsFragmentBinding.buttonFilterDraft.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_color
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            initRecyclerView(Constants.HomeFormSelection.FAILED_UPLOAD)
        }


    }


    private fun initRecyclerView(selectionType: Constants.HomeFormSelection) {

        currentScreen = selectionType

        val resultCursor: Cursor?


        val selectionCursor: CursorLoader? = when (selectionType) {
            Constants.HomeFormSelection.COMPLETED -> CursorLoaderFactory().createCompletedInstancesCursorLoader(
                ""
            )

            Constants.HomeFormSelection.FAILED_UPLOAD -> CursorLoaderFactory().createFailedUploadInstancesCursorLoader(
                ""
            )

            Constants.HomeFormSelection.DRAFTS -> CursorLoaderFactory().createDraftInstancesCursorLoader(
                ""
            )
        }

        resultCursor = selectionCursor?.loadInBackground()


        if (resultCursor?.count == 0) {
            myFormsFragmentBinding.noEntryInSelection.visibility = View.VISIBLE
            myFormsFragmentBinding.noEntryInSelection.text =
                getString(R.string.no_entry_in_selection)
        } else {
            myFormsFragmentBinding.noEntryInSelection.visibility = View.GONE
        }

        paintRecycler(resultCursor, selectionType)

    }


    private fun paintRecycler(returnedCursor: Cursor?, selectionType: Constants.HomeFormSelection) {

        with(myFormsFragmentBinding.filterableRecyclerForms) {

            val formsAdapter =
                NexusFormsAdapter(returnedCursor, selectionType, ::openForm, ::uploadSelectedFiles)

            layoutManager = LinearLayoutManager(context)

            adapter = formsAdapter

        }
    }

    private fun uploadSelectedFiles(selectionId: Long) {
        val instanceIds = LongArray(1) { selectionId }
        // otherwise, do the normal aggregate/other thing.
        val i = Intent(requireContext(), InstanceUploaderActivity::class.java)
        i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIds)
        startActivityForResult(i, 0)

    }

    private fun openForm(c: Cursor?) {

        if (c != null) {
            val instanceUri = ContentUris.withAppendedId(
                InstanceColumns.CONTENT_URI,
                c.getLong(c.getColumnIndex(InstanceColumns._ID))
            )

            val status: String = c.getString(c.getColumnIndex(InstanceColumns.STATUS))
            val strCanEditWhenComplete: String =
                c.getString(c.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE))
            val canEdit =
                status == Instance.STATUS_INCOMPLETE || Boolean.parseBoolean(
                    strCanEditWhenComplete
                )
            if (!canEdit) {
                createAlertDialog(
                    "Not allowed",
                    getString(R.string.cannot_edit_completed_form)

                )
                return
            }

            val intent = Intent(Intent.ACTION_EDIT, instanceUri)

            intent.putExtra(
                ApplicationConstants.BundleKeys.FORM_MODE,
                ApplicationConstants.FormModes.EDIT_SAVED
            )

            intent.putExtra(FormEntryActivity.NEWFORM, false)

            //c.close()

            startActivity(intent)
        }

    }

    private fun createAlertDialog(title: String, message: String) {
        alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        val quitListener =
            DialogInterface.OnClickListener { dialog, i ->
                when (i) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog.dismiss()
                    }


                }
            }
        alertDialog.setCancelable(false)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), quitListener)
        alertDialog.setIcon(android.R.drawable.ic_dialog_info)

        DialogUtils.showDialog(alertDialog, requireActivity())
    }


}








