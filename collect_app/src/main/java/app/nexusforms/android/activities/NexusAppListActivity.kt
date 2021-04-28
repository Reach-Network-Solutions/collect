/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.nexusforms.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import app.nexusforms.android.R
import app.nexusforms.android.adapters.SortDialogAdapter
import app.nexusforms.android.provider.InstanceProviderAPI.InstanceColumns
import app.nexusforms.android.utilities.MultiClickGuard
import app.nexusforms.android.utilities.SnackbarUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import timber.log.Timber
import java.util.*

internal abstract class NexusAppListActivity : CollectAbstractActivity() {
    protected var listAdapter: CursorAdapter? = null
    protected var selectedInstances: LinkedHashSet<Long>? = LinkedHashSet()
    protected lateinit var sortingOptions: IntArray
    protected var selectedSortingOrder: Int? = null
    protected lateinit var listView: ListView
    protected var llParent: LinearLayout? = null
    protected var progressBar: ProgressBar? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var filterText: String? = null
    private var savedFilterText: String? = null
    private var isSearchBoxShown = false
    private var searchView: SearchView? = null
    private var canHideProgressBar = false
    private var progressBarVisible = false
    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        listView = findViewById(android.R.id.list)
        listView.onItemClickListener = this as OnItemClickListener
        listView.emptyView = findViewById(android.R.id.empty)
        progressBar = findViewById(R.id.progressBar)
        llParent = findViewById(R.id.llParent)

        // Use the nicer-looking drawable with Material Design insets.
        listView.divider = ContextCompat.getDrawable(this, R.drawable.list_item_divider)
        listView.dividerHeight = 1
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onResume() {
        super.onResume()
        restoreSelectedSortingOrder()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SELECTED_INSTANCES, selectedInstances)
        if (searchView != null) {
            outState.putBoolean(IS_SEARCH_BOX_SHOWN, !searchView!!.isIconified)
            outState.putString(SEARCH_TEXT, searchView!!.query.toString())
        } else {
            Timber.e("Unexpected null search view (issue #1412)")
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        selectedInstances = state.getSerializable(SELECTED_INSTANCES) as LinkedHashSet<Long>?
        isSearchBoxShown = state.getBoolean(IS_SEARCH_BOX_SHOWN)
        savedFilterText = state.getString(SEARCH_TEXT)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_menu, menu)
        val sortItem = menu.findItem(R.id.menu_sort)
        val searchItem = menu.findItem(R.id.menu_filter)
        searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        val searchEditText =
            searchView!!.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(themeUtils.colorOnPrimary)
        searchView!!.queryHint = resources.getString(R.string.search)
        searchView!!.maxWidth = Int.MAX_VALUE
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterText = query
                updateAdapter()
                searchView!!.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterText = newText
                updateAdapter()
                return false
            }
        })
        MenuItemCompat.setOnActionExpandListener(
            searchItem,
            object : MenuItemCompat.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    sortItem.isVisible = false
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    sortItem.isVisible = true
                    return true
                }
            })
        if (isSearchBoxShown) {
            searchItem.expandActionView()
            searchView!!.setQuery(savedFilterText, false)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!MultiClickGuard.allowClick(javaClass.name)) {
            return true
        }
        when (item.itemId) {
            R.id.menu_sort -> {
                showBottomSheetDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun performSelectedSearch(position: Int) {
        saveSelectedSortingOrder(position)
        updateAdapter()
    }

    protected fun checkPreviouslyCheckedItems() {
        listView!!.clearChoices()
        val selectedPositions: MutableList<Int> = ArrayList()
        var listViewPosition = 0
        val cursor = listAdapter!!.cursor
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val instanceId = cursor.getLong(cursor.getColumnIndex(InstanceColumns._ID))
                if (selectedInstances!!.contains(instanceId)) {
                    selectedPositions.add(listViewPosition)
                }
                listViewPosition++
            } while (cursor.moveToNext())
        }
        for (position in selectedPositions) {
            listView!!.setItemChecked(position, true)
        }
    }

    protected abstract fun updateAdapter()
    protected abstract val sortingOrderKey: String?
    protected fun areCheckedItems(): Boolean {
        return checkedCount > 0
    }

    protected val checkedCount: Int
        protected get() = listView!!.checkedItemCount

    private fun saveSelectedSortingOrder(selectedStringOrder: Int) {
        selectedSortingOrder = selectedStringOrder
        settingsProvider.getGeneralSettings().save(sortingOrderKey!!, selectedStringOrder)
    }

    protected fun restoreSelectedSortingOrder() {
        selectedSortingOrder = settingsProvider.getGeneralSettings().getInt(sortingOrderKey!!)
    }

    protected fun getSelectedSortingOrder(): Int {
        if (selectedSortingOrder == null) {
            restoreSelectedSortingOrder()
        }
        return selectedSortingOrder!!
    }

    protected fun getFilterText(): CharSequence {
        return if (filterText != null) filterText!! else ""
    }

    protected fun clearSearchView() {
        searchView!!.setQuery("", false)
    }

    private fun showBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this, themeUtils.bottomDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        val recyclerView: RecyclerView = sheetView.findViewById(R.id.recyclerView)
        val adapter = SortDialogAdapter(
            this,
            recyclerView,
            sortingOptions,
            getSelectedSortingOrder()
        ) { holder, position ->
            holder.updateItemColor(selectedSortingOrder!!)
            performSelectedSearch(position)
            bottomSheetDialog!!.dismiss()
        }
        val layoutManager: LayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        bottomSheetDialog!!.setContentView(sheetView)
        bottomSheetDialog!!.show()
    }

    protected fun showSnackbar(result: String) {
        SnackbarUtils.showShortSnackbar(llParent!!, result)
    }

    protected fun hideProgressBarIfAllowed() {
        if (canHideProgressBar && progressBarVisible) {
            hideProgressBar()
        }
    }

    protected fun hideProgressBarAndAllow() {
        canHideProgressBar = true
        hideProgressBar()
    }

    private fun hideProgressBar() {
        progressBar!!.visibility = View.GONE
        progressBarVisible = false
    }

    protected fun showProgressBar() {
        progressBar!!.visibility = View.VISIBLE
        progressBarVisible = true
    }

    companion object {
        protected const val LOADER_ID = 0x01
        private const val SELECTED_INSTANCES = "selectedInstances"
        private const val IS_SEARCH_BOX_SHOWN = "isSearchBoxShown"
        private const val SEARCH_TEXT = "searchText"

        // toggles to all checked or all unchecked
        // returns:
        // true if result is all checked
        // false if result is all unchecked
        //
        // Toggle behavior is as follows:
        // if ANY items are unchecked, check them all
        // if ALL items are checked, uncheck them all
        fun toggleChecked(lv: ListView?): Boolean {
            // shortcut null case
            if (lv == null) {
                return false
            }
            val newCheckState = lv.count > lv.checkedItemCount
            setAllToCheckedState(lv, newCheckState)
            return newCheckState
        }

        fun setAllToCheckedState(lv: ListView?, check: Boolean) {
            // no-op if ListView null
            if (lv == null) {
                return
            }
            for (x in 0 until lv.count) {
                lv.setItemChecked(x, check)
            }
        }

        // Function to toggle button label
        fun toggleButtonLabel(toggleButton: Button, lv: ListView) {
            if (lv.checkedItemCount != lv.count) {
                toggleButton.setText(R.string.select_all)
            } else {
                toggleButton.setText(R.string.clear_all)
            }
        }
    }
}