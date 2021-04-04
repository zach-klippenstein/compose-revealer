package com.zachklipp.revealer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.coil.CoilImage
import com.zachklipp.revealer.ContentModel.FeatureModel

@Composable fun FeatureCard(model: FeatureModel, modifier: Modifier = Modifier) {
  Column(modifier) {
    ContentHeading(
      title = model.title,
      subtitle = model.subtitle
    )

    CoilImage(
      data = model.heroImage,
      contentDescription = "hero image",
      modifier = Modifier.aspectRatio(1f),
      contentScale = ContentScale.FillHeight
    )
  }
}

