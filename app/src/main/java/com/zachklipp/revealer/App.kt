package com.zachklipp.revealer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.revealer.ContentModel.AppModel
import com.zachklipp.revealer.ContentModel.FeatureModel
import com.zachklipp.revealer.ContentModel.TrendingModel
import com.zachklipp.revealer.ContentModel.TrendingModel.TrendingApp
import com.zachklipp.revealer.R.drawable
import kotlinx.coroutines.launch

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
  FeatureModel(
    heroImage = R.drawable.animated,
    title = "Don't Miss These Game Updates",
    subtitle = "NOW TRENDING",
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
)

@OptIn(ExperimentalAnimationApi::class)
@Composable fun App() {
  val revealSpec = remember { /*spring<Float>() */tween<Float>(1000) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Today") },
        backgroundColor = MaterialTheme.colors.primarySurface.copy(alpha = .8f)
      )
    }
  ) {
    Revealer(
      // Specify a default placeholder size of a square, so items don't jump into view when
      // scrolling down.
      placeholder = Modifier.aspectRatio(1f)
    ) {
      LazyColumn {
        items(cards) { model ->
          // Let the card wrapping the placeholder handle pointer input, so it falls back to
          // scrolling correctly, but only show the indication on the revealable content.
          val interactionSource = remember { MutableInteractionSource() }
          val indication = LocalIndication.current

          ContentCard(Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClickLabel = "open card",
            onClick = {
              // scope.launch {
              //   revealableState.reveal(revealSpec)
              // }
            }
          )) {
            Revealable { revealableState ->
              val scope = rememberCoroutineScope()
              val scrollState = rememberScrollState()

              if (!revealableState.isFullyCollapsed) {
                BackHandler(onBack = {
                  scope.launch {
                    launch { revealableState.unreveal(revealSpec) }
                    // Reset scroll position.
                    launch { scrollState.animateScrollTo(0, revealSpec) }
                  }
                })
              }

              Surface(
                if (revealableState.isFullyCollapsed) {
                  Modifier.indication(interactionSource, indication)
                } else {
                  Modifier.fillMaxSize()
                }
              ) {
                Column(
                  if (!revealableState.isFullyCollapsed) {
                    Modifier.verticalScroll(scrollState)
                  } else Modifier
                ) {
                  when (model) {
                    is AppModel -> {
                      AppCard(model)

                      if (!revealableState.isFullyCollapsed) {
                        Text(model.longDescription, Modifier.padding(16.dp))
                      }
                    }
                    is FeatureModel -> FeatureCard(model)
                    is TrendingModel -> TrendingCard(model)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable fun ScrollTestApp() {
  val scrollState = rememberScrollState()

  Column {
    Row(
      Modifier
        .weight(1f)
        .horizontalScroll(scrollState)
    ) {
      repeat(30) { i ->
        Text(
          "$i",
          Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .background(Color.Blue)
        )
      }
    }
    Row(
      Modifier
        .weight(1f)
        .fillMaxWidth(.5f)
        .horizontalScroll(scrollState)
    ) {
      repeat(30) { i ->
        Text(
          "$i",
          Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .background(Color.Green)
        )
      }
    }
  }
}

@Composable fun TestApp() {
  Revealer {
    Surface(Modifier.fillMaxSize()) {
      Column {
        Card(Modifier.padding(16.dp)) {
          Revealable { revealableState ->
            val revealScope = rememberCoroutineScope()

            Card {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                  Text("Revealable content", Modifier.padding(16.dp))
                  Button(onClick = {
                    revealScope.launch {
                      revealableState.reveal()
                    }
                  }) {
                    Text("EXPAND")
                  }

                  AndroidView(factory = {
                    android.widget.Button(it).apply {
                      text = "android"
                    }
                  })
                }

                if (!revealableState.isFullyCollapsed) {
                  BackHandler(onBack = {
                    revealScope.launch {
                      revealableState.unreveal()
                    }
                  })
                  Text("Expanded content!!")
                }
              }
            }
          }
        }
        Card(Modifier.padding(16.dp)) {
          Revealable {
            Text("Other card")
          }
        }
      }
    }
  }
}
