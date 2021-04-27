package app.nexusforms.android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import app.nexusforms.android.R
import app.nexusforms.android.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NexusForms)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setUpNavigation()
        setClickListeners()
    }

    private fun setClickListeners() {
        activityMainBinding.fabMainBottomBar.setOnClickListener {
            navController.navigate(R.id.formsLibraryFragment)
        }
    }

    private fun setUpNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_main) as NavHostFragment?
        navController = navHostFragment!!.navController

        val bottomNavigationView = activityMainBinding.bottomNavMainHome
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.myFormsFragment->{
                    setUpToolbarTitle(R.string.my_forms)
                }
                R.id.formsLibraryFragment->{
                    setUpToolbarTitle(R.string.forms_library)
                }

                R.id.filledFormsFragment->{

                }

                R.id.formsSummaryFragment->{

                }

                R.id.profileFragment->{
                    setUpToolbarTitle(R.string.my_profile)
                }

            }
        }
    }

    private fun setUpToolbarTitle(id : Int){
        activityMainBinding.toolbarTitleMain.text = getString(id)
    }
}