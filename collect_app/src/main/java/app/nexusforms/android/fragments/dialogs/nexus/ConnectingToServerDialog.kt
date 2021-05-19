package app.nexusforms.android.fragments.dialogs.nexus

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import app.nexusforms.android.R
import app.nexusforms.android.databinding.DialogConnectingToServerBinding
import app.nexusforms.android.fragments.nexus.FormsLibraryFragment

class ConnectingToServerDialog : DialogFragment() {

    private var binding: DialogConnectingToServerBinding? = null

    private var listener: ConnectingToServerDialogFragmentListener? = null

    override fun getTheme(): Int {
        return R.style.Widget_NexusForms_CustomDialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (targetFragment != null) {
            if (targetFragment is FormsLibraryFragment) {
                listener = targetFragment as FormsLibraryFragment
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogConnectingToServerBinding.inflate(inflater, container, false)
        binding?.buttonConnectingToServerCancel?.setOnClickListener {
            if (listener != null) {
                listener!!.onCancelFormLoading()
            }
            dismiss()
        }

        val message = arguments?.getString(DownloadResultDialogFragment.DOWNLOAD_RESULT)
        message?.let {
            binding?.textMessageConnectingToServer?.text = it
        }

        return binding?.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        binding = null

        if (listener != null) {
            listener?.onCancelFormLoading()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun setMessage(message: String, progress: String) {
        binding?.textMessageConnectingToServer?.text = message
        binding?.textProgressConnectingToServer?.text = progress
    }

    interface ConnectingToServerDialogFragmentListener {
        fun onCancelFormLoading()
    }
}