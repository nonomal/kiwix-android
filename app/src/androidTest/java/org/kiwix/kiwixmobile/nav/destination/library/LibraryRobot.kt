/*
 * Kiwix Android
 * Copyright (c) 2020 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.kiwix.kiwixmobile.nav.destination.library

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import applyWithViewHierarchyPrinting
import com.adevinta.android.barista.interaction.BaristaSleepInteractions
import com.adevinta.android.barista.interaction.BaristaSwipeRefreshInteractions.refresh
import junit.framework.AssertionFailedError
import org.hamcrest.Matchers.not
import org.kiwix.kiwixmobile.BaseRobot
import org.kiwix.kiwixmobile.Findable.ViewId
import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.core.utils.files.Log
import org.kiwix.kiwixmobile.localFileTransfer.LocalFileTransferRobot
import org.kiwix.kiwixmobile.localFileTransfer.localFileTransfer
import org.kiwix.kiwixmobile.testutils.TestUtils
import org.kiwix.kiwixmobile.testutils.TestUtils.testFlakyView
import org.kiwix.kiwixmobile.utils.RecyclerViewItemCount

fun library(func: LibraryRobot.() -> Unit) = LibraryRobot().applyWithViewHierarchyPrinting(func)

class LibraryRobot : BaseRobot() {

  private val zimFileTitle = "Test_Zim"

  fun assertGetZimNearbyDeviceDisplayed() {
    isVisible(ViewId(R.id.get_zim_nearby_device))
  }

  fun clickFileTransferIcon(func: LocalFileTransferRobot.() -> Unit) {
    clickOn(ViewId(R.id.get_zim_nearby_device))
    localFileTransfer(func)
  }

  fun assertLibraryListDisplayed() {
    isVisible(ViewId(R.id.zimfilelist))
  }

  private fun assertNoFilesTextDisplayed() {
    pauseForBetterTestPerformance()
    testFlakyView({ isVisible(ViewId(R.id.file_management_no_files)) })
  }

  fun refreshList() {
    pauseForBetterTestPerformance()
    try {
      onView(withId(R.id.file_management_no_files)).check(matches(isDisplayed()))
      refresh(R.id.zim_swiperefresh)
    } catch (ignore: AssertionFailedError) {
      try {
        onView(withId(R.id.zimfilelist)).check(matches(isDisplayed()))
        refresh(R.id.zim_swiperefresh)
      } catch (e: AssertionFailedError) {
        Log.i(
          "LOCAL_LIBRARY",
          "No need to refresh the data, since there is no files found"
        )
      }
    }
  }

  fun waitUntilZimFilesRefreshing() {
    testFlakyView({
      onView(withId(R.id.scanning_progress_view)).check(matches(not(isDisplayed())))
    })
  }

  fun deleteZimIfExists() {
    try {
      try {
        onView(withId(R.id.file_management_no_files)).check(matches(isDisplayed()))
        // if this view is displaying then we do not need to run the further code.
        return
      } catch (e: AssertionFailedError) {
        Log.e("DELETE_ZIM_FILE", "Zim files found in local library so we are deleting them")
      }
      val recyclerViewId: Int = R.id.zimfilelist
      val recyclerViewItemsCount = RecyclerViewItemCount(recyclerViewId).checkRecyclerViewCount()
      // Scroll to the end of the RecyclerView to ensure all items are visible
      onView(withId(recyclerViewId))
        .perform(scrollToPosition<ViewHolder>(recyclerViewItemsCount - 1))

      for (position in 0 until recyclerViewItemsCount) {
        // Long-click the item to select it
        onView(withId(recyclerViewId))
          .perform(actionOnItemAtPosition<ViewHolder>(position, longClick()))
      }
      clickOnFileDeleteIcon()
      clickOnDeleteZimFile()
      pauseForBetterTestPerformance()
      assertNoFilesTextDisplayed()
    } catch (e: Exception) {
      Log.i(
        "TEST_DELETE_ZIM",
        "Failed to delete ZIM file with title [" + zimFileTitle + "]... " +
          "Probably because it doesn't exist. \nOriginal Exception = $e"
      )
    }
  }

  private fun clickOnFileDeleteIcon() {
    pauseForBetterTestPerformance()
    testFlakyView({ clickOn(ViewId(R.id.zim_file_delete_item)) })
  }

  private fun clickOnDeleteZimFile() {
    // This code is flaky since the DELETE button is inside the dialog, and sometimes it visible
    // on window but espresso unable to find it so we are adding a retrying mechanism here.
    testFlakyView({ onView(withText("DELETE")).inRoot(isDialog()).perform(click()) })
  }

  private fun pauseForBetterTestPerformance() {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS.toLong())
  }
}
