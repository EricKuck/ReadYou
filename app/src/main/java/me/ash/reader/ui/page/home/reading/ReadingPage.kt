@file:OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)

package me.ash.reader.ui.page.home.reading

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.WebViewNavigator
import com.kevinnzou.web.WebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalPullToSwitchArticle
import me.ash.reader.infrastructure.preference.LocalReadingBionicReading
import me.ash.reader.infrastructure.preference.LocalReadingTextLineHeight
import me.ash.reader.infrastructure.preference.OpenLinkPreference
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.motion.materialSharedAxisY
import me.ash.reader.ui.page.home.HomeViewModel
import kotlin.math.abs
import android.graphics.Color as ViewColor

private const val UPWARD = 1
private const val DOWNWARD = -1

@OptIn(
    ExperimentalFoundationApi::class
)
@Composable
fun ReadingPage(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    readingViewModel: ReadingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isPullToSwitchArticleEnabled = LocalPullToSwitchArticle.current.value
    val readingUiState = readingViewModel.readingUiState.collectAsStateValue()
    val readerState = readingViewModel.readerStateStateFlow.collectAsStateValue()
    val homeUiState = homeViewModel.homeUiState.collectAsStateValue()
    val bionicReading = LocalReadingBionicReading.current

    var isReaderScrollingDown by remember { mutableStateOf(false) }
    var showFullScreenImageViewer by remember { mutableStateOf(false) }

    var currentImageData by remember { mutableStateOf(ImageData()) }

    val pagingItems = homeUiState.pagingData.collectAsLazyPagingItems().itemSnapshotList

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            it.arguments?.getString("articleId")?.let { articleId ->
                if (readerState.articleId != articleId) {
                    readingViewModel.initData(articleId)
                }
            }
        }
    }

    LaunchedEffect(readerState.articleId, pagingItems.isNotEmpty()) {
        if (pagingItems.isNotEmpty() && readerState.articleId != null) {
//            Log.i("RLog", "ReadPage: ${readingUiState.articleWithFeed}")
            readingViewModel.prefetchArticleId(pagingItems)
            if (readingUiState.isUnread) {
                readingViewModel.markAsRead()
            }
        }
    }

    val webviewState = readerState.link?.removeSuffixes(IgnoredSuffixes)?.let {
        rememberWebViewState(it)
    }

    val webviewNavigator = rememberWebViewNavigator()

    val pagerState = rememberPagerState(
        pageCount = {
            if (webviewState != null) 2 else 1
        }
    )

    val isNextArticleAvailable = !readerState.nextArticleId.isNullOrEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(
                navController = navController,
                windowInsets = WindowInsets.statusBars,
                showStyle = pagerState.currentPage == 0,
                title = readerState.title,
                link = readerState.link,
                onClose = {
                    navController.popBackStack()
                },
            )
        },
        bottomBar = {
            val bottomBarState = if (pagerState.currentPage == 0) {
                BottomBarState.Description(
                    isUnread = readingUiState.isUnread,
                    isStarred = readingUiState.isStarred,
                    isNextArticleAvailable = isNextArticleAvailable,
                    isFullContent = readerState.content is ReaderState.FullContent,
                    isBionicReading = bionicReading.value,
                )
            } else {
                BottomBarState.Webview(
                    canGoBack = webviewNavigator.canGoBack,
                    canGoForward = webviewNavigator.canGoForward,
                )
            }
            BottomBar(
                state = bottomBarState,
                onUnread = readingViewModel::updateReadStatus,
                onStarred = readingViewModel::updateStarredStatus,
                onNextArticle = readingViewModel::loadNext,
                onFullContent = {
                    if (it) {
                        readingViewModel.renderFullContent()
                    } else {
                        readingViewModel.renderDescriptionContent()
                    }
                },
                onBionicReading = {
                    (!bionicReading).put(context, homeViewModel.viewModelScope)
                },
                onReadAloud = {
                    context.showToast(context.getString(R.string.coming_soon))
                },
                onOpenInBrowser = {
                    context.openURL(
                        url = webviewState?.lastLoadedUrl ?: readerState.link!!,
                        openLink = OpenLinkPreference.AutoPreferDefaultBrowser,
                    )
                },
                onBack = webviewNavigator::navigateBack,
                onForward = webviewNavigator::navigateForward,
                onReload = webviewNavigator::reload,
            )
        },
        content = { paddings ->
            val isPreviousArticleAvailable = !readerState.previousArticleId.isNullOrEmpty()

            if (readerState.articleId != null) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = pagerState.currentPage == 0,
                ) { page ->
                    when (page) {
                        0 -> Article(
                            readerState = readerState,
                            isPullToSwitchArticleEnabled = isPullToSwitchArticleEnabled,
                            isNextArticleAvailable = isNextArticleAvailable,
                            isPreviousArticleAvailable = isPreviousArticleAvailable,
                            onLoadNext = readingViewModel::loadNext,
                            onLoadPrevious = readingViewModel::loadPrevious,
                            setIsReaderScrollingDown = { isReaderScrollingDown = it },
                            setCurrentImageData = { currentImageData = it },
                            setShowFullScreenImageViewer = { showFullScreenImageViewer = it },
                            paddings = paddings,
                            modifier = Modifier.fillMaxSize(),
                        )
                        1 -> WebViewArticle(
                            state = webviewState!!,
                            navigator = webviewNavigator,
                            paddings = paddings,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    )
    if (showFullScreenImageViewer) {

        ReaderImageViewer(
            imageData = currentImageData,
            onDownloadImage = {
                readingViewModel.downloadImage(
                    it,
                    onSuccess = { context.showToast(context.getString(R.string.image_saved)) },
                    onFailure = {
                        // FIXME: crash the app for error report
                            th ->
                        throw th
                    }
                )
            },
            onDismissRequest = { showFullScreenImageViewer = false }
        )
    }
}

@Composable
private fun Article(
    readerState: ReaderState,
    isPullToSwitchArticleEnabled: Boolean,
    isNextArticleAvailable: Boolean,
    isPreviousArticleAvailable: Boolean,
    onLoadNext: () -> Unit,
    onLoadPrevious: () -> Unit,
    setIsReaderScrollingDown: (Boolean) -> Unit,
    setCurrentImageData: (ImageData) -> Unit,
    setShowFullScreenImageViewer: (Boolean) -> Unit,
    paddings: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Content
    AnimatedContent(
        targetState = readerState,
        modifier = modifier,
        contentKey = { it.articleId + it.content.text },
        transitionSpec = {
            val direction = when {
                initialState.nextArticleId == targetState.articleId -> UPWARD
                initialState.previousArticleId == targetState.articleId -> DOWNWARD
                initialState.articleId == targetState.articleId -> {
                    when (targetState.content) {
                        is ReaderState.Description -> DOWNWARD
                        else -> UPWARD
                    }
                }

                else -> UPWARD
            }
            materialSharedAxisY(
                initialOffsetY = { (it * 0.1f * direction).toInt() },
                targetOffsetY = { (it * -0.1f * direction).toInt() },
                durationMillis = 400
            )
        }, label = ""
    ) {
        remember { it }.run {
            val state =
                rememberPullToLoadState(
                    key = content,
                    onLoadNext = onLoadNext,
                    onLoadPrevious = onLoadPrevious,
                )

            val listState = rememberSaveable(
                inputs = arrayOf(content),
                saver = LazyListState.Saver
            ) { LazyListState() }

            CompositionLocalProvider(
                LocalOverscrollConfiguration provides
                  if (isPullToSwitchArticleEnabled) null else LocalOverscrollConfiguration.current,
                LocalTextStyle provides LocalTextStyle.current.run {
                    merge(lineHeight = if (lineHeight.isSpecified) (lineHeight.value * LocalReadingTextLineHeight.current).sp else TextUnit.Unspecified)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Content(
                        modifier = Modifier
                            .padding(paddings)
                            .fillMaxSize()
                            .pullToLoad(
                                state = state,
                                onScroll = { f ->
                                    if (abs(f) > 2f) {
                                        setIsReaderScrollingDown(f < 0f)
                                    }
                                },
                                enabled = isPullToSwitchArticleEnabled
                            ),
                        content = content.text ?: "",
                        feedName = feedName,
                        title = title.toString(),
                        author = author,
                        link = link,
                        publishedDate = publishedDate,
                        isLoading = content is ReaderState.Loading,
                        listState = listState,
                        onImageClick = { imgUrl, altText ->
                            setCurrentImageData(ImageData(imgUrl, altText))
                            setShowFullScreenImageViewer(true)
                        }
                    )
                    PullToLoadIndicator(
                        state = state,
                        canLoadPrevious = isPreviousArticleAvailable,
                        canLoadNext = isNextArticleAvailable
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewArticle(
    state: WebViewState,
    navigator: WebViewNavigator,
    paddings: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(paddings),
    ) {
        val progress = when (val loadingState = state.loadingState) {
            LoadingState.Initializing -> 0f
            is LoadingState.Loading -> loadingState.progress
            LoadingState.Finished -> 0f
        }

        WebView(
            state = state,
            modifier = modifier
                .fillMaxSize()
                .background(Color.White),
            captureBackPresses = false,
            navigator = navigator,
            onCreated = { webView ->
                webView.setBackgroundColor(ViewColor.argb(1, 0, 0, 0));
                webView.settings.javaScriptEnabled = true
            }
        )

        if (progress < 1f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

private fun String.removeSuffixes(suffixes: List<String>): String {
    suffixes.forEach { suffix ->
        if (endsWith(suffix)) {
            return substring(0, length - suffix.length)
        }
    }

    return this
}

val IgnoredSuffixes = listOf(
    "/rss.xml",
)