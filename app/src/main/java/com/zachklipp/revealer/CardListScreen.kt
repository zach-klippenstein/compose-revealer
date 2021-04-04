package com.zachklipp.revealer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zachklipp.revealer.ContentModel.AppModel
import com.zachklipp.revealer.ContentModel.FeatureModel
import com.zachklipp.revealer.ContentModel.TrendingModel

@Composable fun CardListScreen(onClicked: (ContentModel) -> Unit) {
  Scaffold(
    topBar = {
      TopAppBar(title = {
        Text("Today")
      })
    }
  ) {
    LazyColumn {
      items(cards) { model ->
        ContentCard(
          modifier = Modifier
            .clickable(
              onClick = { onClicked(model) },
              onClickLabel = "open card"
            )
        ) {
          when (model) {
            is AppModel -> AppCard(model)
            is FeatureModel -> FeatureCard(model)
            is TrendingModel -> TrendingCard(model)
          }
        }
      }
    }
  }
}
