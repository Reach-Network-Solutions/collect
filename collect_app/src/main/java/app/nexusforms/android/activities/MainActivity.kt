package app.nexusforms.android.activities

import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import app.nexusforms.android.R
import app.nexusforms.android.dao.CursorLoaderFactory
import app.nexusforms.android.databinding.ActivityMainBinding
import app.nexusforms.android.formmanagement.BlankFormListMenuDelegate
import app.nexusforms.android.formmanagement.BlankFormsListViewModel
import app.nexusforms.android.formmanagement.Constants.Companion.IS_INTRO_DOWNLOAD
import app.nexusforms.android.formmanagement.Constants.Companion.IS_INTRO_FORMS
import app.nexusforms.android.formmanagement.Constants.Companion.LOADER_ID_OTHER__FORMS
import app.nexusforms.android.injection.DaggerUtils
import app.nexusforms.android.listeners.DiskSyncListener
import app.nexusforms.android.network.NetworkStateProvider
import app.nexusforms.android.preferences.dialogs.ServerAuthDialogFragment
import app.nexusforms.android.preferences.source.SettingsProvider
import app.nexusforms.android.project.ProjectSettingsDialog
import app.nexusforms.android.tasks.FormSyncTask
import app.nexusforms.android.tasks.InstanceSyncTask
import app.nexusforms.android.utilities.DialogUtils
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import javax.inject.Inject


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var activityMainBinding: ActivityMainBinding

    private lateinit var navController: NavController


    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var blankFormsListViewModelFactory: BlankFormsListViewModel.Factory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private var newFormSyncTask: FormSyncTask? = null

    private var otherFormsInstanceSyncTask: InstanceSyncTask? = null

    lateinit var menuDelegate: BlankFormListMenuDelegate

    private var firstIntroAlreadyShown = false

    var target = 0

    var guideToLibraryBuilder: MaterialTapTargetPrompt.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NexusForms)
        injectDaggerOnCreation()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setUpNavigation()
        setClickListeners()
        initializeNewForms()
        initializeOtherForms()
    }

    private fun injectDaggerOnCreation() {
        DaggerUtils.getComponent(this).Inject(this)
    }


    private fun initializeOtherForms() {
        otherFormsInstanceSyncTask = InstanceSyncTask(settingsProvider)
        otherFormsInstanceSyncTask?.setDiskSyncListener { result ->
            Timber.d(
                "Sync completed for other forms with result -> %s",
                result
            )
        }

        LoaderManager.getInstance(this).initLoader(LOADER_ID_OTHER__FORMS, null, this)
    }

    private fun initializeNewForms() {
        //Init viewModel
        val blankFormsListViewModel =
            ViewModelProvider(
                this,
                blankFormsListViewModelFactory
            )[BlankFormsListViewModel::class.java]


        //Check auth for form
        blankFormsListViewModel.isAuthenticationRequired.observe(this,
            { authenticationRequired: Boolean ->
                if (authenticationRequired) {
                    DialogUtils.showIfNotShowing(
                        ServerAuthDialogFragment::class.java,
                        supportFragmentManager
                    )
                } else {
                    DialogUtils.dismissDialog(
                        ServerAuthDialogFragment::class.java,
                        supportFragmentManager
                    )
                }
            })

        //setup sync task
        // DiskSyncTask checks the disk for any forms not already in the content provider
        // that is, put here by dragging and dropping onto the SDCard

        newFormSyncTask = (lastCustomNonConfigurationInstance as FormSyncTask?)
        if (newFormSyncTask == null) {
            Timber.i("Starting new forms disk sync task : MAIN-ACTIVITY")
            newFormSyncTask = FormSyncTask()
            newFormSyncTask?.setDiskSyncListener {
                DiskSyncListener { syncResult ->
                    Timber.i("Disk scan complete : MAIN-ACTIVITY with result -> %s", syncResult)
                }
            }
            newFormSyncTask?.execute() //*null as Array<Void?>?
        }


        blankFormsListViewModel.isSyncing.observe(this,
            { syncing: Boolean ->

                if (syncing) {

                } else {

                }
            })

    }

    private fun setClickListeners() {
        activityMainBinding.fabMainBottomBar.setOnClickListener {
            navController.navigate(R.id.fillFormFragment)
            //navController.navigate(R.id.formDetailsFragment)
        }

        activityMainBinding.menuActionSettings.setOnClickListener {
            DialogUtils.showIfNotShowing(
                ProjectSettingsDialog::class.java,
                supportFragmentManager
            )
        }
    }

    private fun setUpNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_main) as NavHostFragment?
        navController = navHostFragment!!.navController

        val bottomNavigationView = activityMainBinding.bottomNavMainHome
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { _, destination, bundleArg ->
            when (destination.id) {
                R.id.myFormsFragment -> {
                    setUpToolbarTitle(R.string.my_forms, R.id.myFormsFragment)
                    setupFab(true)
                    handleNavigationArgsForWalkthrough(bundleArg)
                }
                R.id.formsLibraryFragment -> {
                    setUpToolbarTitle(R.string.forms_library, R.id.formsLibraryFragment)
                    setupFab(false)
                }

                R.id.filledFormsFragment -> {
                    setupFab(false)
                }

                R.id.formsSummaryFragment -> {
                    setupFab(false)
                }

                R.id.profileFragment -> {
                    setUpToolbarTitle(R.string.my_profile, R.id.profileFragment)
                    setupFab(false)
                }

                R.id.fillFormFragment -> {
                    setUpToolbarTitle(R.string.fill_form, R.id.fillFormFragment)
                    setupFab(false)
                }

            }
        }
    }

    private fun handleNavigationArgsForWalkthrough(bundleArg: Bundle?) {
        playIntroForPlusFabIfNeedBe(bundleArg)

        if (!firstIntroAlreadyShown) {
            target = R.id.formsLibraryFragment

            guideToLibraryBuilder = MaterialTapTargetPrompt.Builder(this)

            guideToLibrary(
                target,
                "Search your library",
                "All forms reside in your library. Search " +
                        "your library to download different forms."

            )

            firstIntroAlreadyShown = true

            guideToLibraryBuilder?.setPromptStateChangeListener { prompt, state ->

                if (target != R.id.formsLibraryFragment) return@setPromptStateChangeListener

                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    // User has pressed the prompt target
                    prompt.finish()
                    guideToLibraryBuilder = null

                    if (target == R.id.formsLibraryFragment) {

                        val graph = navController.graph

                        val argument =
                            NavArgument.Builder().setDefaultValue(true).build()

                        graph.addArgument(IS_INTRO_DOWNLOAD, argument)

                        val bundle = Bundle()

                        bundle.putBoolean(IS_INTRO_DOWNLOAD, true)

                        navController.setGraph(graph, bundle)

                        navController.navigate(R.id.formsLibraryFragment)

                    }

                }
            }
        }
    }

    private fun playIntroForPlusFabIfNeedBe(bundle: Bundle?) {

        val shouldRun = bundle?.getBoolean(IS_INTRO_FORMS)

        val shouldPlay = shouldRun ?: false

        if (shouldPlay) {
            target = activityMainBinding.fabMainBottomBar.id

            guideToLibraryBuilder = MaterialTapTargetPrompt.Builder(this)

            guideToLibrary(
                target,
                "Fill your forms",
                "Click on this button to select a form to fill for your submission from the list of downloaded forms."
            )
        }


    }

    private fun guideToLibrary(
        target: Int,
        primaryText: String,
        secondaryText: String,
    ) {
        val gotItPrompt = "\n\n Got it!"

        val primarySpannerText = SpannableStringBuilder()

        primarySpannerText.append(
            primaryText,
            StyleSpan(Typeface.BOLD),
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        val secondaryTextSpanner = SpannableStringBuilder()

        val gotItStyleSpanColor = ForegroundColorSpan(
            ContextCompat.getColor(
                this,
                R.color.guide_action_button
            )
        )

        secondaryTextSpanner.append(secondaryText)

        secondaryTextSpanner.append(
            gotItPrompt,
            gotItStyleSpanColor, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        guideToLibraryBuilder?.setTarget(target)
        ?.setPrimaryText(primarySpannerText)
        ?.setSecondaryText(secondaryTextSpanner)
        ?.setPrimaryTextColour(ContextCompat.getColor(this, R.color.white))
        ?.setSecondaryTextColour(ContextCompat.getColor(this, R.color.white))
        ?.setBackgroundColour(ContextCompat.getColor(this, R.color.light_blue))
        ?.setIconDrawable(ContextCompat.getDrawable(
            this,
            if (target == R.id.formsLibraryFragment) R.drawable.ic_library else R.drawable.ic_plus
        ))
        ?.setPrimaryTextGravity(Gravity.CENTER_HORIZONTAL)
        ?.setSecondaryTextGravity(Gravity.CENTER_HORIZONTAL)
        ?.setCaptureTouchEventOnFocal(true)
        ?.create()
        ?.show()
    }

    private fun setupFab(visible: Boolean) {
        if (visible) {
            activityMainBinding.fabMainBottomBar.visibility = View.VISIBLE
        } else {
            activityMainBinding.fabMainBottomBar.visibility = View.INVISIBLE
        }
    }


    private fun setUpToolbarTitle(titleId: Int, fragmentId: Int) {
        if (fragmentId == R.id.fillFormFragment) {
            activityMainBinding.bottomNavMainHome.visibility = View.INVISIBLE
            activityMainBinding.menuActionSettings.visibility = View.INVISIBLE
            activityMainBinding.menuActionSearch.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_arrow_back
                )
            )
            activityMainBinding.menuActionSearch.setOnClickListener {
                navController.navigateUp()
            }
            activityMainBinding.fabMainBottomBar.visibility = View.INVISIBLE
        } else {
            activityMainBinding.bottomNavMainHome.visibility = View.VISIBLE
            activityMainBinding.menuActionSettings.visibility = View.VISIBLE
            activityMainBinding.menuActionSearch.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_search
                )
            )
            activityMainBinding.menuActionSearch.setOnClickListener {
                // navController.navigateUp()
            }
            activityMainBinding.fabMainBottomBar.visibility = View.VISIBLE
        }
        activityMainBinding.toolbarTitleMain.text = getString(titleId)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoaderFactory().createUnsentInstancesCursorLoader(
            "", ""

        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        Timber.d("DONE SYNCING CURSOR - OTHER FORMS")
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Timber.d("LOADER HAS BEEN RESET - OTHER FORMS")
    }
}