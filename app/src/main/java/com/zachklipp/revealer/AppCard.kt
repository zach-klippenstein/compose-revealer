package com.zachklipp.revealer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.CoilImage
import com.zachklipp.revealer.ContentModel.AppModel

@Composable fun AppCard(
  appModel: AppModel,
  modifier: Modifier = Modifier
) {
  Box(modifier.aspectRatio(1f)) {
    CoilImage(
      data = appModel.heroImage,
      contentDescription = "hero image",
      contentScale = ContentScale.FillHeight,
    )

    ContentHeading(appModel.title, appModel.subtitle)

    Row(
      Modifier
        .align(Alignment.BottomStart)
        .fillMaxWidth()
        .background(Color.DarkGray.copy(alpha = 0.8f))
        .padding(16.dp)
    ) {
      Text(appModel.shortDescription, Modifier.weight(1f))
      TextButton(onClick = {}) {
        Text(appModel.buttonText)
      }
    }
  }
}
