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

package org.kiwix.kiwixmobile.core.search.viewmodel.effects

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize
import org.kiwix.kiwixmobile.core.base.SideEffect
import org.kiwix.kiwixmobile.core.extensions.ActivityExtensions.setNavigationResultOnCurrent
import org.kiwix.kiwixmobile.core.main.CoreMainActivity
import org.kiwix.kiwixmobile.core.main.SEARCH_ITEM_TITLE_KEY
import org.kiwix.kiwixmobile.core.reader.addContentPrefix
import org.kiwix.kiwixmobile.core.search.adapter.SearchListItem
import org.kiwix.kiwixmobile.core.utils.TAG_FILE_SEARCHED

data class OpenSearchItem(
  private val searchListItem: SearchListItem,
  private val openInNewTab: Boolean = false
) : SideEffect<Unit> {
  override fun invokeWith(activity: AppCompatActivity) {
    val readerFragmentResId = (activity as CoreMainActivity).readerFragmentResId
    activity.navigate(
      readerFragmentResId,
      bundleOf(SEARCH_ITEM_TITLE_KEY to SEARCH_ITEM_TITLE_KEY)
    )
    activity.setNavigationResultOnCurrent(
      SearchItemToOpen(
        searchListItem.value,
        openInNewTab,
        searchListItem.url?.addContentPrefix
      ),
      TAG_FILE_SEARCHED
    )
  }
}

@Parcelize
data class SearchItemToOpen(
  val pageTitle: String,
  val shouldOpenInNewTab: Boolean,
  val pageUrl: String?
) : Parcelable
