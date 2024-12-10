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
package org.kiwix.kiwixmobile.utils.files

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.kiwix.kiwixmobile.core.entity.LibraryNetworkEntity
import org.kiwix.kiwixmobile.core.utils.files.DocumentResolverWrapper
import org.kiwix.kiwixmobile.core.utils.files.FileUtils
import org.kiwix.kiwixmobile.core.utils.files.FileUtils.documentProviderContentQuery
import org.kiwix.kiwixmobile.core.utils.files.FileUtils.getAllZimParts
import org.kiwix.kiwixmobile.core.utils.files.FileUtils.hasPart
import org.kiwix.kiwixmobile.testutils.RetryRule
import org.kiwix.kiwixmobile.testutils.TestUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Random

class FileUtilsInstrumentationTest {
  private var context: Context? = null
  private var testDir: File? = null
  private val commonPath = "Download/beer.stackexchange.com_en_all_2023-05.zim"
  private val commonUri = "Download%2Fbeer.stackexchange.com_en_all_2023-05.zim"
  private val downloadDocumentUriPrefix =
    "content://com.android.providers.downloads.documents/document/"
  private val primaryStorageUriPrefix =
    "content://com.android.externalstorage.documents/document/"
  private val downloadUriPrefix = "content://media/external/downloads/"
  private val expectedFilePath = "${Environment.getExternalStorageDirectory()}/$commonPath"

  @Rule
  @JvmField
  var retryRule = RetryRule()

  @Before
  fun executeBefore() {
    context = InstrumentationRegistry.getInstrumentation().targetContext

    // Create a temporary directory where all the test files will be saved
    testDir = context?.getDir("testDir", Context.MODE_PRIVATE)
  }

  @Test
  @Throws(IOException::class)
  fun testGetAllZimParts() {

    // Filename ends with .zimXX and the files up till "FileName.zimer" exist
    // i.e. 26 * 4 + 18 = 122 files exist
    val testId = "2rs5475f-51h7-vbz6-331b-7rr25r58251s"
    val fileName = testDir?.path + "/" + testId + "testfile.zim"
    var fileNameWithExtension: String
    val r = Random()
    val bool = BooleanArray(122)

    // Creating the files for the test
    var index = 0
    var char1 = 'a'
    while (char1 <= 'z') {
      var char2 = 'a'
      while (char2 <= 'z') {
        bool[index] = r.nextBoolean()
        fileNameWithExtension = fileName + char1 + char2
        fileNameWithExtension =
          if (bool[index]) fileNameWithExtension else "$fileNameWithExtension.part"
        val file = File(fileNameWithExtension)
        file.createNewFile()
        if (char1 == 'e' && char2 == 'r') {
          break
        }
        index++
        char2++
      }
      if (char1 == 'e') {
        break
      }
      char1++
    }
    val book = LibraryNetworkEntity.Book()
    book.file = File(fileName + "bg")
    val files = getAllZimParts(book)

    // Testing the data returned
    Assert.assertEquals(
      "26 * 4 + 18 = 122 files should be returned",
      122,
      files.size.toLong()
    )
    index = 0
    while (index < 122) {
      if (bool[index]) {
        Assert.assertEquals(
          "if the file fileName.zimXX exists, then no need to add the .part extension at the end",
          false,
          files[index].path.endsWith(
            ".part"
          )
        )
      } else {
        Assert.assertEquals(
          "if the file fileName.zimXX.part exists, then the file" +
            " returned should also have the same ending .zimXX.part",
          true,
          files[index].path.endsWith(
            ".part"
          )
        )
      }
      index++
    }
  }

  @Test
  @Throws(IOException::class)
  fun testHasPart() {
    val testId = "3yd5474g-55d1-aqw0-108z-1xp69x25260d"
    val baseName = testDir?.path + "/" + testId + "testFile"

    // FileName ends with .zim
    val file1 = File(baseName + "1" + ".zim")
    file1.createNewFile()
    Assert.assertEquals(
      "if the fileName ends with .zim and exists in memory, return false",
      false, hasPart(file1)
    )

    // FileName ends with .part
    val file2 = File(baseName + "2" + ".zim")
    file2.createNewFile()
    Assert.assertEquals(
      "if the fileName ends with .part and exists in memory, return true",
      false, hasPart(file2)
    )

    // FileName ends with .zim, however, only the FileName.zim.part file exists in memory
    val file3 = File(baseName + "3" + ".zim" + ".part")
    file3.createNewFile()
    val file4 = File(baseName + "3" + ".zim")
    Assert.assertEquals(
      "if the fileName ends with .zim, but instead the .zim.part file exists in memory",
      true, hasPart(file4)
    )

    // FileName ends with .zimXX
    val testCall = File("$baseName.zimcj")
    testCall.createNewFile()

    // Case : FileName.zimXX.part does not exist for any value of "XX" from "aa"
    // till "bl", but FileName.zimXX exists for all "XX" from "aa',
    // till "bk", then it does not exist
    var char1 = 'a'
    while (char1 <= 'z') {
      var char2 = 'a'
      while (char2 <= 'z') {
        val file = File("$baseName.zim$char1$char2")
        file.createNewFile()
        if (char1 == 'b' && char2 == 'k') {
          break
        }
        char2++
      }
      if (char1 == 'b') {
        break
      }
      char1++
    }
    Assert.assertEquals(false, hasPart(testCall))

    // Case : FileName.zim is the calling file, but neither FileName.zim,
    //        nor FileName.zim.part exist
    // In this case the answer will be the same as that
    //        in the previous (FileName.zimXX) case
    val testCall2 = File("$baseName.zim")
    Assert.assertEquals(false, hasPart(testCall2))

    // Case : FileName.zimXX.part exists for some "XX" between "aa" till "bl"
    // And FileName.zimXX exists for all "XX" from "aa', till "bk",
    //        and then it does not exist
    val t = File("$baseName.zimaj.part")
    t.createNewFile()
    Assert.assertEquals(true, hasPart(testCall))

    // Case : FileName.zim is the calling file, but neither FileName.zim,
    //        nor FileName.zim.part exist
    // In this case the answer will be the same as that in the
    //        previous (FileName.zimXX) case
    Assert.assertEquals(true, hasPart(testCall2))
  }

  @After
  fun removeTestDirectory() {
    testDir?.let {
      it.listFiles()?.let { files ->
        for (child in files) {
          child.delete()
        }
      }
      it.delete()
    }
    context?.let(TestUtils::deleteTemporaryFilesOfTestCases)
  }

  @Test
  fun testDecodeFileName() {
    val dummyUrlArray = listOf(
      DummyUrlData(
        "https://kiwix.org/contributors/contributors_list.pdf",
        src = null,
        "contributors_list.pdf",
        "https://kiwix.org/contributors/contributors_list.pdf"
      ),
      DummyUrlData(
        "https://kiwix.org/contributors/",
        src = null,
        null,
        "https://kiwix.org/contributors/"
      ),
      DummyUrlData(
        "android_tutorials.pdf",
        src = null,
        null,
        "android_tutorials.pdf"
      ),
      DummyUrlData(
        null,
        src = null,
        null,
        null
      ),
      DummyUrlData(
        "/html/images/test.png",
        src = null,
        "test.png",
        "/html/images/test.png"
      ),
      DummyUrlData(
        "/html/images/",
        src = null,
        null,
        "/html/images/"
      ),
      DummyUrlData(
        "https://kiwix.org/contributors/images/wikipedia.png",
        src = null,
        "wikipedia.png",
        "https://kiwix.org/contributors/images/wikipedia.png"
      ),
      DummyUrlData(
        "https://kiwix.org/contributors/images/wikipedia",
        src = null,
        null,
        "https://kiwix.org/contributors/images/wikipedia"
      ),
      DummyUrlData(
        "https://kiwix.org/contributors/images/wikipedia:hello.epub",
        src = null,
        "wikipediahello.epub",
        "https://kiwix.org/contributors/images/wikipedia:hello.epub",
      ),
      DummyUrlData(
        "https://kiwix.org/contributors/Y Gododin: A Poem of the Battle: of Cattraeth.9842.epub",
        src = null,
        "Y Gododin A Poem of the Battle of Cattraeth.9842.epub",
        "https://kiwix.org/contributors/Y Gododin: A Poem of the Battle: of Cattraeth.9842.epub",
      ),
      DummyUrlData(
        "https://kiwix.org/contributors/images/",
        "https://kiwix.org/contributors/images/wikipedia.png",
        "wikipedia.png",
        "https://kiwix.org/contributors/images/wikipedia.png"
      ),
      DummyUrlData(
        "https://kiwix.app/a-virtual-choir-2000-voices-strong?lang=undefined",
        "https://kiwix.app/videos/1110/thumbnail.webp",
        "thumbnail.webp",
        "https://kiwix.app/videos/1110/thumbnail.webp"
      )
    )
    dummyUrlArray.forEach {
      Assertions.assertEquals(
        FileUtils.getSafeFileNameAndSourceFromUrlOrSrc(it.url, it.src)?.first,
        it.expectedFileName
      )
      Assertions.assertEquals(
        FileUtils.getSafeFileNameAndSourceFromUrlOrSrc(it.url, it.src)?.second,
        it.expectedUrl
      )
    }
  }

  @Test
  fun testGetLocalFilePathByUri() {
    val loadFileStream =
      FileUtilsInstrumentationTest::class.java.classLoader.getResourceAsStream("testzim.zim")
    val zimFile = File(testDir, "testzim.zim")
    if (zimFile.exists()) zimFile.delete()
    zimFile.createNewFile()
    loadFileStream.use { inputStream ->
      val outputStream: OutputStream = FileOutputStream(zimFile)
      outputStream.use { it ->
        val buffer = ByteArray(inputStream.available())
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
          it.write(buffer, 0, length)
        }
      }
    }
    // get the SD card path
    val sdCardPath = context?.getExternalFilesDirs("")
      ?.get(1)?.path?.substringBefore("/Android")
    val dummyUriData = listOf(
      // test the download uri on older devices
      DummyUrlData(
        null,
        null,
        expectedFilePath,
        null,
        Uri.parse("${downloadDocumentUriPrefix}raw%3A%2Fstorage%2Femulated%2F0%2F$commonUri")
      ),
      // test the download uri with new version of android
      DummyUrlData(
        null,
        null,
        expectedFilePath,
        null,
        Uri.parse("$downloadDocumentUriPrefix%2Fstorage%2Femulated%2F0%2F$commonUri")
      ),
      // test with file scheme
      DummyUrlData(
        null,
        null,
        zimFile.path,
        null,
        Uri.fromFile(zimFile)
      ),
      // test with internal storage uri
      DummyUrlData(
        null,
        null,
        expectedFilePath,
        null,
        Uri.parse("${primaryStorageUriPrefix}primary%3A$commonUri")
      ),
      // // test with SD card uri
      DummyUrlData(
        null,
        null,
        "$sdCardPath/$commonPath",
        null,
        Uri.parse(
          primaryStorageUriPrefix +
            sdCardPath?.substringAfter("storage/") +
            "%3A$commonUri"
        )
      ),
      // test with USB stick uri
      DummyUrlData(
        null,
        null,
        "/mnt/media_rw/USB/$commonPath",
        null,
        Uri.parse("${primaryStorageUriPrefix}USB%3A$commonUri")
      ),
      // test with invalid uri
      DummyUrlData(
        null,
        null,
        null,
        null,
        Uri.parse(primaryStorageUriPrefix)
      ),
      // test with invalid download uri
      DummyUrlData(
        null,
        null,
        null,
        null,
        Uri.parse(
          "${downloadUriPrefix}0"
        )
      ),
      DummyUrlData(
        null,
        null,
        null,
        null,
        Uri.parse(
          "${downloadDocumentUriPrefix}msf%3A1000000057"
        )
      )
    )
    context?.let { context ->
      dummyUriData.forEach { dummyUrlData ->
        dummyUrlData.uri?.let { uri ->
          Assertions.assertEquals(
            FileUtils.getLocalFilePathByUri(context, uri),
            dummyUrlData.expectedFileName
          )
        }
      }
    }
  }

  @Test
  fun testExtractDocumentId() {
    // We are not running this test case on Android 13 and above. In this version,
    // numerous security updates have been included, preventing us from modifying the
    // default behavior of ContentResolver.
    // Therefore, we are restricting this test to API level 33 and below.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      val dummyDownloadUriData = arrayOf(
        DummyUrlData(
          null,
          null,
          "raw:$expectedFilePath",
          null,
          Uri.parse("${downloadDocumentUriPrefix}raw%3A%2Fstorage%2Femulated%2F0%2F$commonUri")
        ),
        DummyUrlData(
          null,
          null,
          expectedFilePath,
          null,
          Uri.parse("$downloadDocumentUriPrefix%2Fstorage%2Femulated%2F0%2F$commonUri")
        ),
        DummyUrlData(
          null,
          null,
          "",
          null,
          Uri.parse(downloadUriPrefix)
        )
      )

      dummyDownloadUriData.forEach { dummyUrlData ->
        dummyUrlData.uri?.let { uri ->
          Assertions.assertEquals(
            FileUtils.extractDocumentId(uri, DocumentResolverWrapper()),
            dummyUrlData.expectedFileName
          )
        }
      }

      // Testing with a dynamically generated URI. This URI creates at runtime,
      // and passing it statically would result in an `IllegalArgumentException` exception.
      // Therefore, we simulate this scenario using the `DocumentsContractWrapper`
      // to conduct the test.
      val mockDocumentsContractWrapper: DocumentResolverWrapper = mockk()
      val expectedDocumentId = "1000020403"
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Below Android 10, download URI schemes are of the content type, such as:
        // content://com.android.chrome.FileProvider/downloads/alpinelinux_en_all_nopic_2022-12.zim
        // As a result, this method will not be called during that time. We are not testing it on
        // Android versions below 10, as it would result in an "IllegalArgumentException" exception.
        val mockedUri = Uri.parse("$downloadUriPrefix$expectedDocumentId")
        every { mockDocumentsContractWrapper.getDocumentId(mockedUri) } returns expectedDocumentId
        val actualDocumentId = FileUtils.extractDocumentId(mockedUri, mockDocumentsContractWrapper)
        assertEquals(expectedDocumentId, actualDocumentId)
      }
    }
  }

  @Test
  fun testDocumentProviderContentQuery() {
    // We are not running this test case on Android 13 and above. In this version,
    // numerous security updates have been included, preventing us from modifying the
    // default behavior of ContentResolver.
    // Therefore, we are restricting this test to API level 33 and below.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      // test to get the download uri on old device
      testWithDownloadUri(
        Uri.parse("${downloadDocumentUriPrefix}raw%3A%2Fstorage%2Femulated%2F0%2F$commonUri"),
        expectedFilePath
      )

      // test to get the download uri on new device
      testWithDownloadUri(
        Uri.parse("$downloadDocumentUriPrefix%2Fstorage%2Femulated%2F0%2F$commonUri"),
        expectedFilePath
      )

      // test with all possible download uris
      val contentUriPrefixes = arrayOf(
        "content://downloads/public_downloads",
        "content://downloads/my_downloads",
        "content://downloads/all_downloads"
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Below Android 10, download URI schemes are of the content type, such as:
        // content://com.android.chrome.FileProvider/downloads/alpinelinux_en_all_nopic_2022-12.zim
        // As a result, this method will not be called during that time. We are not testing it on
        // Android versions below 10, as it would result in an "IllegalArgumentException" exception.
        contentUriPrefixes.forEach {
          val mockDocumentsContractWrapper: DocumentResolverWrapper = mockk()
          val expectedDocumentId = "1000020403"
          val mockedUri = Uri.parse("$it/$expectedDocumentId")
          every { mockDocumentsContractWrapper.getDocumentId(mockedUri) } returns expectedDocumentId
          every {
            mockDocumentsContractWrapper.query(
              context!!,
              mockedUri,
              "_data",
              null,
              null,
              null
            )
          } returns expectedFilePath
          testWithDownloadUri(
            mockedUri,
            expectedFilePath,
            mockDocumentsContractWrapper
          )
        }
      }
    }
  }

  private fun testWithDownloadUri(
    uri: Uri,
    expectedPath: String,
    documentsContractWrapper: DocumentResolverWrapper = DocumentResolverWrapper()
  ) {
    context?.let { context ->
      assertEquals(
        expectedPath,
        documentProviderContentQuery(context, uri, documentsContractWrapper)
      )
    }
  }

  data class DummyUrlData(
    val url: String?,
    val src: String?,
    val expectedFileName: String?,
    val expectedUrl: String?,
    val uri: Uri? = null
  )
}
