package com.zachklipp.revealer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.zachklipp.revealer.ContentModel.AppModel
import com.zachklipp.revealer.ContentModel.FeatureModel
import com.zachklipp.revealer.ContentModel.TrendingModel
import com.zachklipp.revealer.ContentModel.TrendingModel.TrendingApp
import com.zachklipp.revealer.R.drawable

private val longDescription = buildAnnotatedString {
  withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
    append("Lorem ipsum dolor ")
  }
  append(
    """
      sit amet, consectetur adipiscing elit. Vestibulum varius ex posuere ex blandit, fringilla laoreet augue elementum. Duis dapibus id elit nec fermentum. Etiam et turpis ut neque vulputate maximus. Phasellus vitae tristique arcu. Vivamus pretium ullamcorper erat, vitae pellentesque ante pharetra non. Cras interdum efficitur quam, ac elementum nunc bibendum convallis. Cras convallis ante id justo porta, ac elementum ex tempus. In vitae arcu at libero ullamcorper gravida. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.

      Suspendisse et ultricies risus. Etiam imperdiet porttitor libero eget molestie. Aenean accumsan quam sed nibh sagittis faucibus. Aliquam erat volutpat. Aliquam erat volutpat. Pellentesque mollis vehicula magna et lobortis. Cras faucibus erat in consequat molestie. Morbi libero sem, malesuada at auctor sed, posuere eu elit. Nullam lacinia mollis lorem. Maecenas et massa at nulla finibus malesuada. In blandit auctor mi sit amet lobortis. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Curabitur sodales, lacus blandit feugiat aliquet, felis ante euismod neque, sit amet sagittis mauris ipsum non nibh. Ut pretium, diam ac semper gravida, nisl mauris mollis massa, sit amet rutrum felis ex in dolor. Maecenas eu rhoncus diam.

      Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Nulla vel molestie augue, a sodales mauris. Integer aliquam condimentum lacus, non tempor quam porta nec. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vivamus vitae iaculis leo, ac semper velit. Cras sit amet urna tristique, tincidunt erat sit amet, aliquam elit. Ut faucibus laoreet porttitor. Donec maximus eros ut ipsum congue elementum. Maecenas a urna vitae tellus semper placerat. Nulla mollis sem vel erat dictum facilisis. Quisque tincidunt quis nisl ut sollicitudin. Ut luctus enim vitae congue interdum. Nam laoreet, nibh a fringilla ultrices, nulla nunc porttitor lorem, id pulvinar nulla massa eu tortor.
    """.trimIndent()
  )
}

val cards = listOf(
  AppModel(
    heroImage = drawable.app,
    title = "Title",
    subtitle = "Subtitle",
    shortDescription = "Description\nWith lots of lines.",
    buttonText = "EXPLORE",
    longDescription = longDescription
  ),
  TrendingModel(
    title = "Top Apps Right Now",
    subtitle = "NOW TRENDING",
    apps = listOf(
      TrendingApp(
        logo = Icons.Default.Image,
        title = "App 1",
        description = "Share the moment"
      ),
      TrendingApp(
        logo = Icons.Default.AccountBalance,
        title = "App 2",
        description = "Description 2"
      ),
      TrendingApp(
        logo = Icons.Default.Analytics,
        title = "App 3",
        description = "Description 3"
      ),
      TrendingApp(
        logo = Icons.Default.Android,
        title = "App 4",
        description = "Description 4"
      ),
    )
  ),
  FeatureModel(
    heroImage = R.drawable.animated,
    title = "Don't Miss These Game Updates",
    subtitle = "NOW TRENDING",
    longDescription = longDescription
  ),
)

data class Screen(
  val content: @Composable () -> Unit
)

@Composable fun App() {
  var currentScreen: Screen? by remember { mutableStateOf(null) }
  val cardListScreen = remember {
    Screen {
      CardListScreen(onClicked = { model ->
        val previousScreen = currentScreen!!
        currentScreen = when (model) {
          is AppModel -> Screen {
            AppDetailsScreen(
              model,
              onDismiss = { currentScreen = previousScreen }
            )
          }
          is TrendingModel -> previousScreen
          is FeatureModel -> Screen {
            FeatureDetailsScreen(model,
              onDismiss = { currentScreen = previousScreen })
          }
        }
      })
    }.also { currentScreen = it }
  }

  currentScreen!!.content()
}
