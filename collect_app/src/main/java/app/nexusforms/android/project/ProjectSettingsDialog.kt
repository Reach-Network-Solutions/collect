package app.nexusforms.android.project

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import app.nexusforms.android.activities.AboutActivity
import app.nexusforms.android.injection.DaggerUtils
import app.nexusforms.android.preferences.dialogs.AdminPasswordDialogFragment
import app.nexusforms.android.preferences.screens.AdminPreferencesActivity
import app.nexusforms.android.preferences.screens.GeneralPreferencesActivity
import app.nexusforms.android.utilities.AdminPasswordProvider
import app.nexusforms.android.utilities.DialogUtils
import com.google.android.material.button.MaterialButton
import app.nexusforms.android.R

import javax.inject.Inject

class ProjectSettingsDialog : DialogFragment() {

    @Inject
    lateinit var adminPasswordProvider: AdminPasswordProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.project_settings_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.close_icon).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.general_settings_button).setOnClickListener {
            startActivity(Intent(requireContext(), GeneralPreferencesActivity::class.java))
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.admin_settings_button).setOnClickListener {
            if (adminPasswordProvider.isAdminPasswordSet) {
                val args = Bundle().also {
                    it.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, AdminPasswordDialogFragment.Action.ADMIN_SETTINGS)
                }
                DialogUtils.showIfNotShowing(AdminPasswordDialogFragment::class.java, args, requireActivity().supportFragmentManager)
            } else {
                startActivity(Intent(requireContext(), AdminPreferencesActivity::class.java))
            }
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.about_button).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            dismiss()
        }
    }
}
