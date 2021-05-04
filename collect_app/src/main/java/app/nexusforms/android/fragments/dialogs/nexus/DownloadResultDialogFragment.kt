package app.nexusforms.android.fragments.dialogs.nexus

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import app.nexusforms.android.R
import app.nexusforms.android.databinding.DialogDownloadResultBinding
import timber.log.Timber

class DownloadResultDialogFragment : DialogFragment() {

    private var binding: DialogDownloadResultBinding? = null

    override fun getTheme(): Int {
        return R.style.Widget_NexusForms_CustomDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogDownloadResultBinding.inflate(inflater, container, false)
        binding?.buttonDownloadResultOk?.setOnClickListener {
            dismiss()
        }

        val message = arguments?.getString(DOWNLOAD_RESULT)
        binding?.textDownloadResults?.text = message

        return binding?.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val DOWNLOAD_RESULT = "DOWNLOAD RESULT"
    }
}