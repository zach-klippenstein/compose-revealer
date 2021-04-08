package com.zachklipp.revealer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.CoilImage
import com.zachklipp.revealer.ContentModel.AppModel

@Composable fun AppCard(
  appModel: AppModel,
  modifier: Modifier = Modifier,
  imageModifier: Modifier = Modifier
) {
  Box(modifier) {
    // CoilImage(
    //   data = appModel.heroImage,
    Image(
      painterResource(appModel.heroImage),
      contentDescription = "hero image",
      contentScale = ContentScale.FillWidth,
      modifier = imageModifier
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
