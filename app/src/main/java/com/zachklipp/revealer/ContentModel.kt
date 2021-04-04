package com.zachklipp.revealer

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString

sealed class ContentModel {

  data class AppModel(
    val heroImage: Int,
    val title: String,
    val subtitle: String,
    val shortDescription: String,
    val buttonText: String,
    val longDescription: AnnotatedString
  ) : ContentModel()

  data class TrendingModel(
    val title: String,
    val subtitle: String,
    val apps: List<TrendingApp>
  ) : ContentModel() {
    data class TrendingApp(
      val logo: ImageVector,
      val title: String,
      val description: String
    )
  }

  data class FeatureModel(
    val heroImage: Int,
    val title: String,
    val subtitle: String,
    val longDescription: AnnotatedString
  ) : ContentModel()
}
