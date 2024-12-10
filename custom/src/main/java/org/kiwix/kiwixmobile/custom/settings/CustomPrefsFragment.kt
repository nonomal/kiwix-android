/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.custom.settings

import android.os.Bundle
import androidx.preference.Preference
import org.kiwix.kiwixmobile.core.settings.CorePrefsFragment
import org.kiwix.kiwixmobile.core.utils.SharedPreferenceUtil.Companion.PREF_LANG
import org.kiwix.kiwixmobile.core.utils.SharedPreferenceUtil.Companion.PREF_WIFI_ONLY
import org.kiwix.kiwixmobile.custom.BuildConfig

class CustomPrefsFragment : CorePrefsFragment() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    super.onCreatePreferences(savedInstanceState, rootKey)
    if (BuildConfig.DISABLE_EXTERNAL_LINK) {
      hideExternalLinksPreference()
    }
    if (BuildConfig.ENFORCED_LANG.isEmpty()) {
      setUpLanguageChooser(PREF_LANG)
    } else {
      findPreference<Preference>("pref_language")?.let(preferenceScreen::removePreference)
    }
    preferenceScreen.removePreferenceRecursively(PREF_WIFI_ONLY)
  }

  /**
   * If "external links" are disabled in a custom app,
   * this function hides the external links preference from settings
   * and sets the shared preference to not show the external link popup
   * when opening external links.
   */
  private fun hideExternalLinksPreference() {
    preferenceScreen.removePreferenceRecursively("pref_external_link_popup")
    sharedPreferenceUtil?.putPrefExternalLinkPopup(false)
  }

  override suspend fun setStorage() {
    findPreference<Preference>("pref_storage")?.let(preferenceScreen::removePreference)
  }
}
