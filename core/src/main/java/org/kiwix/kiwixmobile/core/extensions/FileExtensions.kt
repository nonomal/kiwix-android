/*
 * Kiwix Android
 * Copyright (c) 2023 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.core.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

fun File.isFileExist(): Boolean = runBlocking {
  withContext(Dispatchers.IO) {
    exists()
  }
}

fun File.freeSpace(): Long = runBlocking {
  withContext(Dispatchers.IO) {
    freeSpace
  }
}

suspend fun File.totalSpace(): Long = withContext(Dispatchers.IO) { totalSpace }

suspend fun File.canReadFile(): Boolean = withContext(Dispatchers.IO) { canRead() }

suspend fun File.deleteFile(): Boolean = withContext(Dispatchers.IO) { delete() }
