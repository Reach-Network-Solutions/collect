package app.nexusforms.android.fragments.nexus

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.nexusforms.android.R
import app.nexusforms.android.activities.FormDownloadListActivity
import app.nexusforms.android.activities.viewmodels.FormDownloadListViewModel
import app.nexusforms.android.formentry.RefreshFormListDialogFragment
import app.nexusforms.android.formmanagement.Constants.Companion.FORMDETAIL_KEY
import app.nexusforms.android.formmanagement.Constants.Companion.FORM_ID_KEY
import app.nexusforms.android.formmanagement.Constants.Companion.FORM_VERSION_KEY
import app.nexusforms.android.formmanagement.FormDownloader
import app.nexusforms.android.formmanagement.FormSourceExceptionMapper
import app.nexusforms.android.formmanagement.ServerFormDetails
import app.nexusforms.android.formmanagement.ServerFormsDetailsFetcher
import app.nexusforms.android.forms.FormSourceException
import app.nexusforms.android.forms.FormSourceException.AuthRequired
import app.nexusforms.android.injection.DaggerUtils
import app.nexusforms.android.listeners.DownloadFormsTaskListener
import app.nexusforms.android.listeners.FormListDownloaderListener
import app.nexusforms.android.network.NetworkStateProvider
import app.nexusforms.android.tasks.DownloadFormListTask
import app.nexusforms.android.tasks.DownloadFormsTask
import app.nexusforms.android.utilities.*
import app.nexusforms.android.utilities.AuthDialogUtility.AuthDialogUtilityResultListener
import timber.log.Timber
import java.net.URI
import java.util.*
import javax.inject.Inject


class FormsLibraryFragment : Fragment(), DownloadFormsTaskListener, FormListDownloaderListener,
    AuthDialogUtilityResultListener{

    @Inject
    lateinit var connectivityProvider: NetworkStateProvider

    @Inject
    lateinit var webCredentialsUtils: WebCredentialsUtils

    @Inject
    lateinit var serverFormsDetailsFetcher: ServerFormsDetailsFetcher

    @Inject
    lateinit var formDownloader: FormDownloader

    private lateinit var viewModel: FormDownloadListViewModel

    private  var downloadFormListTask: DownloadFormListTask? = null

    private var displayOnlyUpdatedForms = false

    private val filteredFormList = ArrayList<HashMap<String, String>>()

    lateinit var alertDialog: AlertDialog

    private val cancelDialog: ProgressDialog? = null

    private var downloadFormsTask: DownloadFormsTask? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerUtils.getComponent(context).Inject(this)

        viewModel = ViewModelProvider(
            this,
            FormDownloadListViewModel.Factory()
        )[FormDownloadListViewModel::class.java]

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forms_library, container, false)
    }

    private fun init(savedInstanceState: Bundle?) {

        val options = listOf(ApplicationConstants.BundleKeys.FORM_MODE,
        ApplicationConstants.FormModes.EDIT_SAVED)

        if (options.isNotEmpty()) {
            if (options.contains(FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS)) {
                displayOnlyUpdatedForms = true

            }
            if (options.contains(ApplicationConstants.BundleKeys.FORM_IDS)) {
                viewModel.isDownloadOnlyMode = true
                //viewModel.formIdsToDownload =
                //    bundle.getStringArray(ApplicationConstants.BundleKeys.FORM_IDS)

                if (viewModel.formIdsToDownload == null) {
                    createAlertDialog("Null Ids", "Form Ids is null", false)
                   // finish()
                }
//                if (bundle.containsKey(ApplicationConstants.BundleKeys.URL)) {
//                    viewModel.url = bundle.getString(ApplicationConstants.BundleKeys.URL)
//                    if (bundle.containsKey(ApplicationConstants.BundleKeys.USERNAME)
//                        && bundle.containsKey(ApplicationConstants.BundleKeys.PASSWORD)
//                    ) {
//                        viewModel.username =
//                            bundle.getString(ApplicationConstants.BundleKeys.USERNAME)
//                        viewModel.password =
//                            bundle.getString(ApplicationConstants.BundleKeys.PASSWORD)
//                    }
//                }
            }
        }
        //downloadButton = findViewById<Button>(R.id.add_button)

        //downloadButton.setEnabled(listView.getCheckedItemCount() > 0)

        /*downloadButton.setOnClickListener(View.OnClickListener { v: View? ->

            val filesToDownload: ArrayList<ServerFormDetails> = getFilesToDownload()
            startFormsDownload(filesToDownload)
        })

        toggleButton = findViewById<Button>(R.id.toggle_button)

        toggleButton.setEnabled(false)

        toggleButton.setOnClickListener(View.OnClickListener {

            downloadButton.setEnabled(AppListActivity.toggleChecked(listView))

            AppListActivity.toggleButtonLabel(toggleButton, listView)

            viewModel.clearSelectedFormIds()
            if (listView.getCheckedItemCount() == listView.getCount()) {
                for (map in viewModel.formList) {
                    viewModel.addSelectedFormId(map[FormDownloadListActivity.FORMDETAIL_KEY])
                }
            }
        })

        val refreshButton: Button = findViewById<Button>(R.id.refresh_button)
        refreshButton.setOnClickListener {
            viewModel.setLoadingCanceled(false)
            viewModel.clearFormList()
            updateAdapter()
            clearChoices()
            downloadFormList()
        }
        if (savedInstanceState != null) {
            // how many items we've selected
            // Android should keep track of this, but broken on rotate...
            if (savedInstanceState.containsKey(FormDownloadListActivity.BUNDLE_SELECTED_COUNT)) {
                downloadButton.setEnabled(savedInstanceState.getInt(FormDownloadListActivity.BUNDLE_SELECTED_COUNT) > 0)
            }
        }*/
        filteredFormList.addAll(viewModel.formList)

        downloadFormList()

//        if (getLastCustomNonConfigurationInstance() is DownloadFormListTask) {
//            downloadFormListTask = getLastCustomNonConfigurationInstance() as DownloadFormListTask
//            if (downloadFormListTask!!.status == AsyncTask.Status.FINISHED) {
//                DialogUtils.dismissDialog(
//                    RefreshFormListDialogFragment::class.java,
//                    getSupportFragmentManager()
//                )
//                downloadFormsTask = null
//            }
//        } else if (getLastCustomNonConfigurationInstance() is DownloadFormsTask) {
//            downloadFormsTask = getLastCustomNonConfigurationInstance() as DownloadFormsTask
//            if (downloadFormsTask!!.status == AsyncTask.Status.FINISHED) {
//                DialogUtils.dismissDialog(
//                    RefreshFormListDialogFragment::class.java,
//                    getSupportFragmentManager()
//                )
//                downloadFormsTask = null
//            }
//        } else if (viewModel.formDetailsByFormId.isEmpty()
//            && getLastCustomNonConfigurationInstance() == null && !viewModel.wasLoadingCanceled()
//        ) {
//            // first time, so get the formlist
//            //downloadFormList()
//        }
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE)
//        listView.setItemsCanFocus(false)
//        sortingOptions = intArrayOf(
//            R.string.sort_by_name_asc, R.string.sort_by_name_desc
//        )
    }
//
//    private fun getFilesToDownload(): ArrayList<ServerFormDetails?>? {
//        val filesToDownload = ArrayList<ServerFormDetails?>()
//
//        val sba: SparseBooleanArray = listView.getCheckedItemPositions()
//
//        for (i in 0 until listView.getCount()) {
//            if (sba[i, false]) {
//
//                val item = listView.getAdapter().getItem(i) as HashMap<String, String>
//
//                filesToDownload.add(viewModel.formDetailsByFormId[item[FORMDETAIL_KEY]])
//            }
//        }
//        return filesToDownload
//    }

    /**
     * Starts the download task and shows the progress dialog.
     */
    private fun downloadFormList() {
        if (!connectivityProvider.isDeviceOnline) {
            ToastUtils.showShortToast(R.string.no_connection)
            if (viewModel.isDownloadOnlyMode) {
                createAlertDialog("No Connection", getString(R.string.no_connection), false)
            }
        } else {
            viewModel.clearFormDetailsByFormId()
            DialogUtils.showIfNotShowing(
                RefreshFormListDialogFragment::class.java,
                childFragmentManager
            )
            if (downloadFormListTask != null
                && downloadFormListTask?.status != AsyncTask.Status.FINISHED
            ) {
                return  // we are already doing the download!!!
            } else if (downloadFormListTask != null) {
                downloadFormListTask?.setDownloaderListener(null)
                downloadFormListTask?.cancel(true)
                downloadFormListTask = null
            }
            if (viewModel.isDownloadOnlyMode) {
                // Handle external app download case with different server
                downloadFormListTask = DownloadFormListTask(serverFormsDetailsFetcher)
                downloadFormListTask?.setAlternateCredentials(
                    webCredentialsUtils,
                    viewModel.url,
                    viewModel.username,
                    viewModel.password
                )
                downloadFormListTask?.setDownloaderListener(this)
                downloadFormListTask?.execute()
            } else {
                downloadFormListTask = DownloadFormListTask(serverFormsDetailsFetcher)
                downloadFormListTask?.setDownloaderListener(this)
                downloadFormListTask?.execute()
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            FormsLibraryFragment()
    }

    override fun formsDownloadingComplete(result: MutableMap<ServerFormDetails, String>?) {
        TODO("Not yet implemented")
    }

    override fun progressUpdate(currentFile: String?, progress: Int, total: Int) {
        TODO("Not yet implemented")
    }

    override fun formsDownloadingCancelled() {
        TODO("Not yet implemented")
    }

    override fun formListDownloadingComplete(
        formList: HashMap<String, ServerFormDetails>?,
        exception: FormSourceException?
    ) {
        DialogUtils.dismissDialog(
            RefreshFormListDialogFragment::class.java,
            childFragmentManager
        )
        downloadFormListTask!!.setDownloaderListener(null)
        downloadFormListTask = null

        if (exception == null) {
            // Everything worked. Clear the list and add the results.
            viewModel.formDetailsByFormId = formList
            viewModel.clearFormList()
            val ids = ArrayList(viewModel.formDetailsByFormId.keys)
            for (i in 0 until formList!!.size) {
                val formDetailsKey = ids[i]
                val details = viewModel.formDetailsByFormId[formDetailsKey]
                if (!displayOnlyUpdatedForms || details!!.isUpdated) {
                    val item = HashMap<String, String>()
                    item[FormDownloadListActivity.FORMNAME] = details!!.formName
                    item[FormDownloadListActivity.FORMID_DISPLAY] =
                        (if (details!!.formVersion == null) "" else getString(R.string.version) + " "
                                + details!!.formVersion + " ") + "ID: " + details!!.formId
                    item[FORMDETAIL_KEY] = formDetailsKey
                    item[FORM_ID_KEY] = details!!.formId
                    item[FORM_VERSION_KEY] = details!!.formVersion

                    // Insert the new form in alphabetical order.
                    if (viewModel.formList.isEmpty()) {
                        viewModel.addForm(item)
                    } else {
                        var j: Int
                        j = 0
                        while (j < viewModel.formList.size) {
                            val compareMe = viewModel.formList[j]
                            val name = compareMe[FormDownloadListActivity.FORMNAME]
                            if (name!!.compareTo(viewModel.formDetailsByFormId[ids[i]]!!.formName) > 0) {
                                break
                            }
                            j++
                        }
                        viewModel.addForm(j, item)
                    }
                }
            }
            filteredFormList.addAll(viewModel.formList)
           // updateAdapter()

            for (item in filteredFormList){
                Timber.d("ITEM in list %s with %s", item.keys, item.values)
            }

            selectSupersededForms()
/*
            downloadButton.setEnabled(listView.getCheckedItemCount() > 0)

            toggleButton.setEnabled(listView.getCount() > 0)

            AppListActivity.toggleButtonLabel(toggleButton, listView)

 */


            if (viewModel.isDownloadOnlyMode) {
                //performDownloadModeDownload()
                Timber.d("IS DOWNLOAD MODE ")
            }
        } else {
            if (exception is AuthRequired) {
                createAuthDialog()
            } else {
                val dialogMessage = FormSourceExceptionMapper(requireContext()).getMessage(exception)
                val dialogTitle = getString(R.string.load_remote_form_error)
                if (viewModel.isDownloadOnlyMode) {
                    //setReturnResult(false, dialogMessage, viewModel.formResults)
                }
                createAlertDialog(dialogTitle, dialogMessage, false)
            }
        }
    }

    private fun performDownloadModeDownload() {
        //1. First check if all form IDS could be found on the server - Register forms that could not be found
        for (formId in viewModel.formIdsToDownload) {
            viewModel.putFormResult(formId, false)
        }
        val filesToDownload = ArrayList<ServerFormDetails>()
        for (serverFormDetails in viewModel.formDetailsByFormId.values) {
            val formId = serverFormDetails.formId
            if (viewModel.formResults.containsKey(formId)) {
                filesToDownload.add(serverFormDetails)
            }
        }

        //2. Select forms and start downloading
        if (!filesToDownload.isEmpty()) {
            startFormsDownload(filesToDownload)
        } else {
            // None of the forms was found
            createAlertDialog("No Forms", "Forms not found on server", false)
            //finish()
        }
    }

    /**
     * starts the task to download the selected forms, also shows progress dialog
     */
    private fun startFormsDownload(filesToDownload: ArrayList<ServerFormDetails>) {
        val totalCount = filesToDownload.size
        if (totalCount > 0) {
            // show dialog box
            DialogUtils.showIfNotShowing(
                RefreshFormListDialogFragment::class.java,
                childFragmentManager
            )
            downloadFormsTask = DownloadFormsTask(formDownloader)

            downloadFormsTask?.setDownloaderListener(this)

            if (viewModel.url != null) {
                if (viewModel.username != null && viewModel.password != null) {
                    webCredentialsUtils.saveCredentials(
                        viewModel.url,
                        viewModel.username,
                        viewModel.password
                    )
                } else {
                    webCredentialsUtils.clearCredentials(viewModel.url)
                }
            }

            downloadFormsTask?.execute(filesToDownload)
        } else {
            ToastUtils.showShortToast(R.string.noselect_error)
        }
    }

    fun isLocalFormSuperseded(formId: String?): Boolean {
        if (formId == null) {
            Timber.e("isLocalFormSuperseded: server is not OpenRosa-compliant. <formID> is null!")
            return true
        }
        val form = viewModel.formDetailsByFormId[formId]
        return form!!.isNotOnDevice || form.isUpdated
    }

    /**
     * Causes any local forms that have been updated on the server to become checked in the list.
     * This is a prompt and a
     * convenience to users to download the latest version of those forms from the server.
     */
    private fun selectSupersededForms() {
//        val ls: ListView = listView
//        for (idx in filteredFormList.indices) {
//            val item = filteredFormList[idx]
//            if (isLocalFormSuperseded(item[FORM_ID_KEY])) {
//                ls.setItemChecked(idx, true)
//                viewModel.addSelectedFormId(item[FORMDETAIL_KEY])
//            }
//        }
    }

    /**
     * Creates an alert dialog with the given tite and message. If shouldExit is set to true, the
     * activity will exit when the user clicks "ok".
     */
    private fun createAlertDialog(title: String, message: String, shouldExit: Boolean) {
        alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        val quitListener =
            DialogInterface.OnClickListener { dialog, i ->
                when (i) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        // just close the dialog
                        viewModel.isAlertShowing = false
                        // successful download, so quit
                        // Also quit if in download_mode only(called by another app/activity just to download)
                        if (shouldExit || viewModel.isDownloadOnlyMode) {
                            //finish()

                            dialog.dismiss()
                        }
                    }
                }
            }
        alertDialog.setCancelable(false)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), quitListener)
        alertDialog.setIcon(android.R.drawable.ic_dialog_info)
        viewModel.alertDialogMsg = message
        viewModel.alertTitle = title
        viewModel.isAlertShowing = true
        viewModel.setShouldExit(shouldExit)
        DialogUtils.showDialog(alertDialog, requireActivity())
    }

    private fun createAuthDialog() {
        viewModel.isAlertShowing = false
        val authDialogUtility = AuthDialogUtility()
        if (viewModel.url != null && viewModel.username != null && viewModel.password != null) {
            authDialogUtility.setCustomUsername(viewModel.username)
            authDialogUtility.setCustomPassword(viewModel.password)
        }
        DialogUtils.showDialog(authDialogUtility.createDialog(requireContext(), this, viewModel.url), requireActivity())
    }

    override fun updatedCredentials() {
        // If the user updated the custom credentials using the dialog, let us update our
        // variables holding the custom credentials

        if (viewModel.url != null) {
            val httpCredentials = webCredentialsUtils.getCredentials(URI.create(viewModel.url))
            if (httpCredentials != null) {
                viewModel.username = httpCredentials.username
                viewModel.password = httpCredentials.password
            }
        }

        downloadFormList()
    }

    override fun cancelledUpdatingCredentials() {
        Toast.makeText(requireContext(), "Credentials are required!", Toast.LENGTH_SHORT).show()
    }
}