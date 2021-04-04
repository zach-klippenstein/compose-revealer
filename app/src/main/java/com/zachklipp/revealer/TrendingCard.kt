package com.zachklipp.revealer

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zachklipp.revealer.ContentModel.TrendingModel

@OptIn(ExperimentalMaterialApi::class)
@Composable fun TrendingCard(model: TrendingModel, modifier: Modifier = Modifier) {
  Column(modifier) {
    ContentHeading(title = model.title, subtitle = model.subtitle)
    model.apps.forEach { app ->
      ListItem(
        icon = { Icon(app.logo, contentDescription = "logo for ${app.title}") },
        text = { Text(app.title) },
        secondaryText = { Text(app.description) },
        trailing = {
          TextButton(onClick = {}) {
            Text("GET")
          }
        }
      )
      Divider()
    }
  }
}