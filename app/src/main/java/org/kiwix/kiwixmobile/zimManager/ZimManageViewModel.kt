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

package org.kiwix.kiwixmobile.zimManager

import android.app.Application
import android.net.ConnectivityManager
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function6
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import org.kiwix.kiwixmobile.BuildConfig.DEBUG
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.StorageObserver
import org.kiwix.kiwixmobile.core.base.SideEffect
import org.kiwix.kiwixmobile.core.compat.CompatHelper.Companion.isWifi
import org.kiwix.kiwixmobile.core.dao.DownloadRoomDao
import org.kiwix.kiwixmobile.core.dao.NewBookDao
import org.kiwix.kiwixmobile.core.dao.NewLanguagesDao
import org.kiwix.kiwixmobile.core.data.DataSource
import org.kiwix.kiwixmobile.core.data.remote.KiwixService
import org.kiwix.kiwixmobile.core.data.remote.KiwixService.Companion.LIBRARY_NETWORK_PATH
import org.kiwix.kiwixmobile.core.data.remote.ProgressResponseBody
import org.kiwix.kiwixmobile.core.data.remote.UserAgentInterceptor
import org.kiwix.kiwixmobile.core.di.modules.CALL_TIMEOUT
import org.kiwix.kiwixmobile.core.di.modules.CONNECTION_TIMEOUT
import org.kiwix.kiwixmobile.core.di.modules.KIWIX_DOWNLOAD_URL
import org.kiwix.kiwixmobile.core.di.modules.READ_TIMEOUT
import org.kiwix.kiwixmobile.core.di.modules.USER_AGENT
import org.kiwix.kiwixmobile.core.downloader.downloadManager.DEFAULT_INT_VALUE
import org.kiwix.kiwixmobile.core.downloader.model.DownloadModel
import org.kiwix.kiwixmobile.core.entity.LibraryNetworkEntity
import org.kiwix.kiwixmobile.core.entity.LibraryNetworkEntity.Book
import org.kiwix.kiwixmobile.core.extensions.calculateSearchMatches
import org.kiwix.kiwixmobile.core.extensions.registerReceiver
import org.kiwix.kiwixmobile.core.utils.BookUtils
import org.kiwix.kiwixmobile.core.utils.SharedPreferenceUtil
import org.kiwix.kiwixmobile.core.utils.files.Log
import org.kiwix.kiwixmobile.core.utils.files.ScanningProgressListener
import org.kiwix.kiwixmobile.core.zim_manager.ConnectivityBroadcastReceiver
import org.kiwix.kiwixmobile.core.zim_manager.Language
import org.kiwix.kiwixmobile.core.zim_manager.NetworkState
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.SelectionMode.MULTI
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.SelectionMode.NORMAL
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.adapter.BooksOnDiskListItem
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.adapter.BooksOnDiskListItem.BookOnDisk
import org.kiwix.kiwixmobile.zimManager.Fat32Checker.FileSystemState
import org.kiwix.kiwixmobile.core.zim_manager.NetworkState.CONNECTED
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.MultiModeFinished
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.RequestDeleteMultiSelection
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.RequestMultiSelection
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.RequestNavigateTo
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.RequestSelect
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.RequestShareMultiSelection
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.RestartActionMode
import org.kiwix.kiwixmobile.zimManager.ZimManageViewModel.FileSelectActions.UserClickedDownloadBooksButton
import org.kiwix.kiwixmobile.zimManager.fileselectView.FileSelectListState
import org.kiwix.kiwixmobile.zimManager.fileselectView.effects.DeleteFiles
import org.kiwix.kiwixmobile.zimManager.fileselectView.effects.NavigateToDownloads
import org.kiwix.kiwixmobile.zimManager.fileselectView.effects.None
import org.kiwix.kiwixmobile.zimManager.fileselectView.effects.OpenFileWithNavigation
import org.kiwix.kiwixmobile.zimManager.fileselectView.effects.ShareFiles
import org.kiwix.kiwixmobile.zimManager.fileselectView.effects.StartMultiSelection
import org.kiwix.kiwixmobile.zimManager.libraryView.adapter.LibraryListItem
import org.kiwix.kiwixmobile.zimManager.libraryView.adapter.LibraryListItem.BookItem
import org.kiwix.kiwixmobile.zimManager.libraryView.adapter.LibraryListItem.DividerItem
import org.kiwix.kiwixmobile.zimManager.libraryView.adapter.LibraryListItem.LibraryDownloadItem
import java.io.IOException
import java.util.LinkedList
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

const val DEFAULT_PROGRESS = 0
const val MAX_PROGRESS = 100
private const val TAG_RX_JAVA_DEFAULT_ERROR_HANDLER = "RxJavaDefaultErrorHandler"

class ZimManageViewModel @Inject constructor(
  private val downloadDao: DownloadRoomDao,
  private val bookDao: NewBookDao,
  private val languageDao: NewLanguagesDao,
  private val storageObserver: StorageObserver,
  private var kiwixService: KiwixService,
  val context: Application,
  private val connectivityBroadcastReceiver: ConnectivityBroadcastReceiver,
  private val bookUtils: BookUtils,
  private val fat32Checker: Fat32Checker,
  private val defaultLanguageProvider: DefaultLanguageProvider,
  private val dataSource: DataSource,
  private val connectivityManager: ConnectivityManager,
  private val sharedPreferenceUtil: SharedPreferenceUtil
) : ViewModel() {
  sealed class FileSelectActions {
    data class RequestNavigateTo(val bookOnDisk: BookOnDisk) : FileSelectActions()
    data class RequestSelect(val bookOnDisk: BookOnDisk) : FileSelectActions()
    data class RequestMultiSelection(val bookOnDisk: BookOnDisk) : FileSelectActions()
    object RequestDeleteMultiSelection : FileSelectActions()
    object RequestShareMultiSelection : FileSelectActions()
    object MultiModeFinished : FileSelectActions()
    object RestartActionMode : FileSelectActions()
    object UserClickedDownloadBooksButton : FileSelectActions()
  }

  private var isUnitTestCase: Boolean = false
  val sideEffects = PublishProcessor.create<SideEffect<Any?>>()
  val libraryItems: MutableLiveData<List<LibraryListItem>> = MutableLiveData()
  val fileSelectListStates: MutableLiveData<FileSelectListState> = MutableLiveData()
  val deviceListScanningProgress = MutableLiveData<Int>()
  val libraryListIsRefreshing = MutableLiveData<Boolean>()
  val shouldShowWifiOnlyDialog = MutableLiveData<Boolean>()
  val networkStates = MutableLiveData<NetworkState>()

  val requestFileSystemCheck = PublishProcessor.create<Unit>()
  val fileSelectActions = PublishProcessor.create<FileSelectActions>()
  val requestDownloadLibrary = BehaviorProcessor.createDefault(Unit)
  val requestFiltering = BehaviorProcessor.createDefault("")

  private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
  val downloadProgress = MutableLiveData<String>()

  init {
    compositeDisposable?.addAll(*disposables())
    context.registerReceiver(connectivityBroadcastReceiver)
  }

  fun setIsUnitTestCase() {
    isUnitTestCase = true
  }

  private fun createKiwixServiceWithProgressListener(): KiwixService {
    if (isUnitTestCase) return kiwixService
    val contentLength = getContentLengthOfLibraryXmlFile()
    val customOkHttpClient = OkHttpClient().newBuilder()
      .followRedirects(true)
      .followSslRedirects(true)
      .connectTimeout(CONNECTION_TIMEOUT, SECONDS)
      .readTimeout(READ_TIMEOUT, SECONDS)
      .callTimeout(CALL_TIMEOUT, SECONDS)
      .addNetworkInterceptor(
        HttpLoggingInterceptor().apply {
          level = if (DEBUG) BASIC else NONE
        }
      )
      .addNetworkInterceptor(UserAgentInterceptor(USER_AGENT))
      .addNetworkInterceptor { chain ->
        val originalResponse = chain.proceed(chain.request())
        originalResponse.newBuilder()
          .body(
            ProgressResponseBody(
              originalResponse.body!!,
              AppProgressListenerProvider(this),
              contentLength
            )
          )
          .build()
      }
      .build()
    return KiwixService.ServiceCreator.newHackListService(customOkHttpClient, KIWIX_DOWNLOAD_URL)
      .also {
        kiwixService = it
      }
  }

  private fun getContentLengthOfLibraryXmlFile(): Long {
    val headRequest = Request.Builder()
      .url("$KIWIX_DOWNLOAD_URL$LIBRARY_NETWORK_PATH")
      .head()
      .header("Accept-Encoding", "identity")
      .build()
    val client = OkHttpClient().newBuilder()
      .followRedirects(true)
      .followSslRedirects(true)
      .connectTimeout(CONNECTION_TIMEOUT, SECONDS)
      .readTimeout(READ_TIMEOUT, SECONDS)
      .callTimeout(CALL_TIMEOUT, SECONDS)
      .addNetworkInterceptor(UserAgentInterceptor(USER_AGENT))
      .build()
    try {
      client.newCall(headRequest).execute().use { response ->
        if (response.isSuccessful) {
          return@getContentLengthOfLibraryXmlFile response.header("content-length")?.toLongOrNull()
            ?: DEFAULT_INT_VALUE.toLong()
        }
      }
    } catch (ignore: Exception) {
      // do nothing
    }
    return DEFAULT_INT_VALUE.toLong()
  }

  @VisibleForTesting
  fun onClearedExposed() {
    onCleared()
  }

  override fun onCleared() {
    compositeDisposable?.clear()
    context.unregisterReceiver(connectivityBroadcastReceiver)
    connectivityBroadcastReceiver.stopNetworkState()
    requestFileSystemCheck.onComplete()
    fileSelectActions.onComplete()
    requestDownloadLibrary.onComplete()
    compositeDisposable = null
    super.onCleared()
  }

  private fun disposables(): Array<Disposable> {
    val downloads = downloadDao.downloads()
    val booksFromDao = books()
    val networkLibrary = PublishProcessor.create<LibraryNetworkEntity>()
    val languages = languageDao.languages()
    return arrayOf(
      updateBookItems(),
      checkFileSystemForBooksOnRequest(booksFromDao),
      updateLibraryItems(booksFromDao, downloads, networkLibrary, languages),
      updateLanguagesInDao(networkLibrary, languages),
      updateNetworkStates(),
      requestsAndConnectivtyChangesToLibraryRequests(networkLibrary),
      fileSelectActions()
    ).also {
      setUpUncaughtErrorHandlerForOnlineLibrary(networkLibrary)
    }
  }

  private fun fileSelectActions() = fileSelectActions.subscribe({
    sideEffects.offer(
      when (it) {
        is RequestNavigateTo -> OpenFileWithNavigation(it.bookOnDisk)
        is RequestMultiSelection -> startMultiSelectionAndSelectBook(it.bookOnDisk)
        RequestDeleteMultiSelection -> DeleteFiles(selectionsFromState())
        RequestShareMultiSelection -> ShareFiles(selectionsFromState())
        MultiModeFinished -> noSideEffectAndClearSelectionState()
        is RequestSelect -> noSideEffectSelectBook(it.bookOnDisk)
        RestartActionMode -> StartMultiSelection(fileSelectActions)
        UserClickedDownloadBooksButton -> NavigateToDownloads
      }
    )
  }, Throwable::printStackTrace)

  private fun startMultiSelectionAndSelectBook(
    bookOnDisk: BookOnDisk
  ): StartMultiSelection {
    fileSelectListStates.value?.let {
      fileSelectListStates.postValue(
        it.copy(
          bookOnDiskListItems = selectBook(it, bookOnDisk),
          selectionMode = MULTI
        )
      )
    }
    return StartMultiSelection(fileSelectActions)
  }

  private fun selectBook(
    it: FileSelectListState,
    bookOnDisk: BookOnDisk
  ): List<BooksOnDiskListItem> {
    return it.bookOnDiskListItems.map { listItem ->
      if (listItem.id == bookOnDisk.id) listItem.apply { isSelected = !isSelected }
      else listItem
    }
  }

  private fun noSideEffectSelectBook(bookOnDisk: BookOnDisk): SideEffect<Unit> {
    fileSelectListStates.value?.let {
      fileSelectListStates.postValue(
        it.copy(bookOnDiskListItems = selectBook(it, bookOnDisk))
      )
    }
    return None
  }

  private fun selectionsFromState() = fileSelectListStates.value?.selectedBooks ?: emptyList()

  private fun noSideEffectAndClearSelectionState(): SideEffect<Unit> {
    fileSelectListStates.value?.let {
      fileSelectListStates.postValue(
        it.copy(
          bookOnDiskListItems = it.bookOnDiskListItems.map { booksOnDiskListItem ->
            booksOnDiskListItem.apply { isSelected = false }
          },
          selectionMode = NORMAL
        )
      )
    }
    return None
  }

  private fun requestsAndConnectivtyChangesToLibraryRequests(
    library: PublishProcessor<LibraryNetworkEntity>,
  ) =
    Flowable.combineLatest(
      requestDownloadLibrary,
      connectivityBroadcastReceiver.networkStates.distinctUntilChanged().filter(
        CONNECTED::equals
      )
    ) { _, _ -> }
      .switchMap {
        if (connectivityManager.isWifi()) {
          Flowable.just(Unit)
        } else {
          sharedPreferenceUtil.prefWifiOnlys
            .doOnNext {
              if (it) {
                shouldShowWifiOnlyDialog.postValue(true)
              }
            }
            .filter { !it }
            .map { }
        }
      }
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .concatMap {
        Flowable.fromCallable {
          synchronized(this, ::createKiwixServiceWithProgressListener)
        }
      }
      .concatMap {
        kiwixService.library
          .toFlowable()
          .retry(5)
          .doOnSubscribe {
            downloadProgress.postValue(
              context.getString(R.string.starting_downloading_remote_library)
            )
          }
          .map { response ->
            downloadProgress.postValue(context.getString(R.string.parsing_remote_library))
            response
          }
          .doFinally {
            downloadProgress.postValue(context.getString(R.string.parsing_remote_library))
          }
          .onErrorReturn {
            it.printStackTrace()
            LibraryNetworkEntity().apply { book = LinkedList() }
          }
      }
      .subscribe(library::onNext, Throwable::printStackTrace).also {
        compositeDisposable?.add(it)
      }

  private fun updateNetworkStates() =
    connectivityBroadcastReceiver.networkStates.subscribe(
      networkStates::postValue, Throwable::printStackTrace
    )

  private fun updateLibraryItems(
    booksFromDao: Flowable<List<BookOnDisk>>,
    downloads: Flowable<List<DownloadModel>>,
    library: Flowable<LibraryNetworkEntity>,
    languages: Flowable<List<Language>>
  ) = Flowable.combineLatest(
    booksFromDao,
    downloads,
    languages.filter(List<Language>::isNotEmpty),
    library,
    Flowable.merge(
      Flowable.just(""),
      requestFiltering
        .doOnNext { libraryListIsRefreshing.postValue(true) }
        .debounce(500, MILLISECONDS)
        .observeOn(Schedulers.io())
    ),
    fat32Checker.fileSystemStates,
    Function6(::combineLibrarySources)
  )
    .doOnNext { libraryListIsRefreshing.postValue(false) }
    .doOnError { throwable ->
      if (throwable is OutOfMemoryError) {
        Log.e("ZimManageViewModel", "Error----${throwable.printStackTrace()}")
      }
    }
    .subscribeOn(Schedulers.io())
    .subscribe(
      libraryItems::postValue,
      Throwable::printStackTrace
    )

  private fun updateLanguagesInDao(
    library: Flowable<LibraryNetworkEntity>,
    languages: Flowable<List<Language>>
  ) = library
    .subscribeOn(Schedulers.io())
    .map(LibraryNetworkEntity::book)
    .withLatestFrom(
      languages,
      BiFunction(::combineToLanguageList)
    )
    .map { it.sortedBy(Language::language) }
    .filter(List<Language>::isNotEmpty)
    .subscribe(
      languageDao::insert,
      Throwable::printStackTrace
    )

  private fun combineToLanguageList(
    booksFromNetwork: List<Book>,
    allLanguages: List<Language>
  ) = when {
    booksFromNetwork.isEmpty() && allLanguages.isEmpty() -> defaultLanguage()
    booksFromNetwork.isEmpty() && allLanguages.isNotEmpty() -> emptyList()
    booksFromNetwork.isNotEmpty() && allLanguages.isEmpty() ->
      fromLocalesWithNetworkMatchesSetActiveBy(
        networkLanguageCounts(booksFromNetwork), defaultLanguage()
      )

    booksFromNetwork.isNotEmpty() && allLanguages.isNotEmpty() ->
      fromLocalesWithNetworkMatchesSetActiveBy(
        networkLanguageCounts(booksFromNetwork), allLanguages
      )

    else -> throw RuntimeException("Impossible state")
  }

  private fun networkLanguageCounts(booksFromNetwork: List<Book>) =
    booksFromNetwork.mapNotNull(Book::language)
      .fold(
        mutableMapOf<String, Int>()
      ) { acc, language -> acc.increment(language) }

  private fun <K> MutableMap<K, Int>.increment(key: K) =
    apply { set(key, getOrElse(key) { 0 } + 1) }

  private fun fromLocalesWithNetworkMatchesSetActiveBy(
    networkLanguageCounts: MutableMap<String, Int>,
    listToActivateBy: List<Language>
  ) = Locale.getISOLanguages()
    .map(::Locale)
    .filter { networkLanguageCounts.containsKey(it.isO3Language) }
    .map { locale ->
      Language(
        locale.isO3Language,
        languageIsActive(listToActivateBy, locale),
        networkLanguageCounts.getOrElse(locale.isO3Language) { 0 }
      )
    }

  private fun defaultLanguage() =
    listOf(
      defaultLanguageProvider.provide()
    )

  private fun languageIsActive(
    allLanguages: List<Language>,
    locale: Locale
  ) = allLanguages.firstOrNull { it.languageCode == locale.isO3Language }?.active == true

  @Suppress("UnsafeCallOnNullableType")
  private fun combineLibrarySources(
    booksOnFileSystem: List<BookOnDisk>,
    activeDownloads: List<DownloadModel>,
    allLanguages: List<Language>,
    libraryNetworkEntity: LibraryNetworkEntity,
    filter: String,
    fileSystemState: FileSystemState
  ): List<LibraryListItem> {
    val activeLanguageCodes = allLanguages.filter(Language::active)
      .map(Language::languageCode)
    val booksUnfilteredByLanguage =
      applySearchFilter(
        libraryNetworkEntity.book!! - booksOnFileSystem.map(BookOnDisk::book),
        filter
      )

    val booksWithActiveLanguages =
      booksUnfilteredByLanguage.filter { activeLanguageCodes.contains(it.language) }
    return createLibrarySection(
      booksWithActiveLanguages,
      activeDownloads,
      fileSystemState,
      R.string.your_languages,
      Long.MAX_VALUE
    ) +
      createLibrarySection(
        booksUnfilteredByLanguage - booksWithActiveLanguages,
        activeDownloads,
        fileSystemState,
        R.string.other_languages,
        Long.MIN_VALUE
      )
  }

  private fun createLibrarySection(
    books: List<Book>,
    activeDownloads: List<DownloadModel>,
    fileSystemState: FileSystemState,
    sectionStringId: Int,
    sectionId: Long
  ) =
    if (books.isNotEmpty())
      listOf(DividerItem(sectionId, sectionStringId)) +
        books.asLibraryItems(activeDownloads, fileSystemState)
    else emptyList()

  private fun applySearchFilter(
    unDownloadedBooks: List<Book>,
    filter: String
  ) = if (filter.isEmpty()) {
    unDownloadedBooks
  } else {
    unDownloadedBooks.iterator().forEach { it.calculateSearchMatches(filter, bookUtils) }
    unDownloadedBooks.filter { it.searchMatches > 0 }
  }

  private fun List<Book>.asLibraryItems(
    activeDownloads: List<DownloadModel>,
    fileSystemState: FileSystemState
  ) = map { book ->
    activeDownloads.firstOrNull { download -> download.book == book }
      ?.let(::LibraryDownloadItem)
      ?: BookItem(book, fileSystemState)
  }

  private fun checkFileSystemForBooksOnRequest(booksFromDao: Flowable<List<BookOnDisk>>):
    Disposable =
    requestFileSystemCheck
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .onBackpressureDrop()
      .doOnNext { deviceListScanningProgress.postValue(DEFAULT_PROGRESS) }
      .switchMap(
        {
          booksFromStorageNotIn(
            booksFromDao,
            object : ScanningProgressListener {
              override fun onProgressUpdate(scannedDirectory: Int, totalDirectory: Int) {
                // Calculate the overall progress based on the number of processed directories
                val overallProgress =
                  (scannedDirectory.toDouble() / totalDirectory.toDouble() * MAX_PROGRESS).toInt()
                if (overallProgress != MAX_PROGRESS) {
                  // Send the progress if it is not 100% because after scanning the entire storage,
                  // it takes a bit of time to organize the ZIM files, filter them,
                  // and remove any duplicate ZIM files. We send the 100% progress
                  // in the doOnNext method to hide the progressBar from the UI
                  // and display all the filtered ZIM files.
                  deviceListScanningProgress.postValue(overallProgress)
                }
              }
            }
          )
        },
        1
      )
      .onBackpressureDrop()
      .doOnNext { deviceListScanningProgress.postValue(MAX_PROGRESS) }
      .filter(List<BookOnDisk>::isNotEmpty)
      .map { it.distinctBy { bookOnDisk -> bookOnDisk.book.id } }
      .subscribe(
        bookDao::insert,
        Throwable::printStackTrace
      )

  private fun books() = bookDao.books()
    .subscribeOn(Schedulers.io())
    .map { it.sortedBy { book -> book.book.title } }

  private fun booksFromStorageNotIn(
    booksFromDao: Flowable<List<BookOnDisk>>,
    scanningProgressListener: ScanningProgressListener
  ) =
    storageObserver.getBooksOnFileSystem(scanningProgressListener)
      .withLatestFrom(
        booksFromDao.map { it.map { bookOnDisk -> bookOnDisk.book.id } },
        BiFunction(::removeBooksAlreadyInDao)
      )

  private fun removeBooksAlreadyInDao(
    booksFromFileSystem: Collection<BookOnDisk>,
    idsInDao: List<String>
  ) = booksFromFileSystem.filterNot { idsInDao.contains(it.book.id) }

  private fun updateBookItems() =
    dataSource.booksOnDiskAsListItems()
      .subscribe({ newList ->
        fileSelectListStates.postValue(
          fileSelectListStates.value?.let {
            inheritSelections(
              it,
              newList.toMutableList()
            )
          } ?: FileSelectListState(newList)
        )
      }, Throwable::printStackTrace)

  private fun inheritSelections(
    oldState: FileSelectListState,
    newList: MutableList<BooksOnDiskListItem>
  ): FileSelectListState {
    return oldState.copy(
      bookOnDiskListItems = newList.map { newBookOnDisk ->
        val firstOrNull =
          oldState.bookOnDiskListItems.firstOrNull { oldBookOnDisk ->
            oldBookOnDisk.id == newBookOnDisk.id
          }
        newBookOnDisk.apply { isSelected = firstOrNull?.isSelected ?: false }
      }
    )
  }

  private fun setUpUncaughtErrorHandlerForOnlineLibrary(
    library: PublishProcessor<LibraryNetworkEntity>
  ) {
    RxJavaPlugins.setErrorHandler { exception ->
      if (exception is RuntimeException && exception.cause == IOException()) {
        Log.i(
          TAG_RX_JAVA_DEFAULT_ERROR_HANDLER,
          "Caught undeliverable exception: ${exception.cause}"
        )
      }
      when (exception) {
        is UndeliverableException -> {
          library.onNext(
            LibraryNetworkEntity().apply { book = LinkedList() }
          ).also {
            Log.i(
              TAG_RX_JAVA_DEFAULT_ERROR_HANDLER,
              "Caught undeliverable exception: ${exception.cause}"
            )
          }
        }

        else -> {
          Thread.currentThread().also { thread ->
            thread.uncaughtExceptionHandler?.uncaughtException(thread, exception)
          }
        }
      }
    }
  }
}
