package me.ash.reader.ui.page.home.reading

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalReadingRenderer
import me.ash.reader.infrastructure.preference.ReadingRendererPreference
import me.ash.reader.ui.component.base.CanBeDisabledIconButton
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.component.webview.BionicReadingIcon

@Immutable
sealed interface BottomBarState {
    data class Description(
        val isUnread: Boolean,
        val isStarred: Boolean,
        val isNextArticleAvailable: Boolean,
        val isFullContent: Boolean,
        val isBionicReading: Boolean,
    ) : BottomBarState

    data class Webview(
        val canGoBack: Boolean,
        val canGoForward: Boolean,
    ) : BottomBarState
}

@Composable
fun BottomBar(
    state: BottomBarState,
    onUnread: (isUnread: Boolean) -> Unit = {},
    onStarred: (isStarred: Boolean) -> Unit = {},
    onNextArticle: () -> Unit = {},
    onFullContent: (isFullContent: Boolean) -> Unit = {},
    onBionicReading: () -> Unit = {},
    onReadAloud: () -> Unit = {},
    onOpenInBrowser: () -> Unit = {},
    onBack: () -> Unit = {},
    onForward: () -> Unit = {},
    onReload: () -> Unit = {},
) {
    val tonalElevation = LocalReadingPageTonalElevation.current

    Surface(
        tonalElevation = tonalElevation.value.dp,
    ) {
        // TODO: Component styles await refactoring
        AnimatedContent(
            targetState = state,
            modifier = Modifier
              .navigationBarsPadding()
              .fillMaxWidth()
              .height(60.dp),
            label = "BottomBar",
        ) { state ->
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (state) {
                    is BottomBarState.Description -> DescriptionItems(
                        isUnread = state.isUnread,
                        isStarred = state.isStarred,
                        isNextArticleAvailable = state.isNextArticleAvailable,
                        isFullContent = state.isFullContent,
                        isBionicReading = state.isBionicReading,
                        onUnread = onUnread,
                        onStarred = onStarred,
                        onNextArticle = onNextArticle,
                        onFullContent = onFullContent,
                        onBionicReading = onBionicReading,
                        onReadAloud = onReadAloud,
                    )
                    is BottomBarState.Webview -> WebviewItems(
                        canGoBack = state.canGoBack,
                        canGoForward = state.canGoForward,
                        onOpenInBrowser = onOpenInBrowser,
                        onBack = onBack,
                        onForward = onForward,
                        onReload = onReload,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.WebviewItems(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onOpenInBrowser: () -> Unit = {},
    onBack: () -> Unit = {},
    onForward: () -> Unit = {},
    onReload: () -> Unit = {},
) {
    val view = LocalView.current

    CanBeDisabledIconButton(
        modifier = Modifier.size(40.dp),
        disabled = !canGoBack,
        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = "Back",
        tint = MaterialTheme.colorScheme.outline
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onBack()
    }

    CanBeDisabledIconButton(
        modifier = Modifier.size(40.dp),
        disabled = !canGoForward,
        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
        contentDescription = "Forward",
        tint = MaterialTheme.colorScheme.outline
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onForward()
    }

    CanBeDisabledIconButton(
        modifier = Modifier.size(40.dp),
        disabled = false,
        imageVector = Icons.Outlined.Refresh,
        contentDescription = stringResource(R.string.refresh),
        tint = MaterialTheme.colorScheme.outline
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onReload()
    }

    CanBeDisabledIconButton(
        modifier = Modifier.size(40.dp),
        disabled = false,
        imageVector = Icons.Outlined.Public,
        contentDescription = stringResource(R.string.open_link_specific_browser),
        tint = MaterialTheme.colorScheme.outline
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onOpenInBrowser()
    }
}

@Composable
private fun RowScope.DescriptionItems(
    isUnread: Boolean,
    isStarred: Boolean,
    isNextArticleAvailable: Boolean,
    isFullContent: Boolean,
    isBionicReading: Boolean,
    onUnread: (isUnread: Boolean) -> Unit = {},
    onStarred: (isStarred: Boolean) -> Unit = {},
    onNextArticle: () -> Unit = {},
    onFullContent: (isFullContent: Boolean) -> Unit = {},
    onBionicReading: () -> Unit = {},
    onReadAloud: () -> Unit = {},
) {
    val view = LocalView.current
    val renderer = LocalReadingRenderer.current

    CanBeDisabledIconButton(
        modifier = Modifier.size(40.dp),
        disabled = false,
        imageVector = if (isUnread) {
            Icons.Filled.FiberManualRecord
        } else {
            Icons.Outlined.FiberManualRecord
        },
        contentDescription = stringResource(if (isUnread) R.string.mark_as_read else R.string.mark_as_unread),
        tint = if (isUnread) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.outline
        },
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onUnread(!isUnread)
    }
    CanBeDisabledIconButton(
        modifier = Modifier.size(40.dp),
        disabled = false,
        imageVector = if (isStarred) {
            Icons.Rounded.Star
        } else {
            Icons.Rounded.StarOutline
        },
        contentDescription = stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred),
        tint = if (isStarred) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.outline
        },
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onStarred(!isStarred)
    }
    CanBeDisabledIconButton(
        disabled = !isNextArticleAvailable,
        modifier = Modifier.size(40.dp),
        imageVector = Icons.Rounded.ExpandMore,
        contentDescription = "Next Article",
        tint = MaterialTheme.colorScheme.outline,
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onNextArticle()
    }
    CanBeDisabledIconButton(
        modifier = Modifier.size(36.dp),
        disabled = false,
        imageVector = if (renderer == ReadingRendererPreference.WebView) null else Icons.Outlined.Headphones,
        contentDescription = if (renderer == ReadingRendererPreference.WebView) {
            stringResource(R.string.bionic_reading)
        } else {
            stringResource(R.string.read_aloud)
        },
        tint = MaterialTheme.colorScheme.outline,
        icon = {
            BionicReadingIcon(
                filled = isBionicReading,
                size = 24.dp,
                tint = if (renderer == ReadingRendererPreference.WebView) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        },
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (renderer == ReadingRendererPreference.WebView) {
            onBionicReading()
        } else {
            onReadAloud()
        }
    }
    CanBeDisabledIconButton(
        disabled = false,
        modifier = Modifier.size(40.dp),
        imageVector = if (isFullContent) {
            Icons.AutoMirrored.Rounded.Article
        } else {
            Icons.AutoMirrored.Outlined.Article
        },
        contentDescription = stringResource(R.string.parse_full_content),
        tint = if (isFullContent) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.outline
        },
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onFullContent(!isFullContent)
    }

}
