package me.ash.reader.ui.page.home.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalSharedContent
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.common.RouteName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    windowInsets: WindowInsets,
    showStyle: Boolean,
    title: String? = "",
    link: String? = "",
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current
    val tonalElevation = LocalReadingPageTonalElevation.current
    val sharedContent = LocalSharedContent.current

    TopAppBar(
        title = {},
        windowInsets = windowInsets,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.close),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                onClose()
            }
        },
        actions = {
          AnimatedContent(
            targetState = showStyle,
          ) { showStyle ->
            if (showStyle) {
              FeedbackIconButton(
                modifier = Modifier.size(22.dp),
                imageVector = Icons.Outlined.Palette,
                contentDescription = stringResource(R.string.style),
                tint = MaterialTheme.colorScheme.onSurface
              ) {
                navController.navigate(RouteName.READING_PAGE_STYLE) {
                  launchSingleTop = true
                }
              }
            }
          }

            FeedbackIconButton(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Outlined.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onSurface,
            ) {
                sharedContent.share(context, title, link)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation.value.dp),
        ),
        scrollBehavior = pinnedScrollBehavior(),
    )
}
