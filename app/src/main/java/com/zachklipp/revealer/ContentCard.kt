package com.zachklipp.revealer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun ContentCard(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Card(modifier.padding(16.dp)) {
    Box(content = content)
  }
}

@Composable fun ContentHeading(title: String, subtitle: String) {
  Column(Modifier.padding(16.dp)) {
    Text(
      subtitle,
      style = MaterialTheme.typography.subtitle1,
      fontWeight = FontWeight.Bold
    )
    Text(
      title,
      style = MaterialTheme.typography.h5,
      fontWeight = FontWeight.Bold
    )
  }
}
