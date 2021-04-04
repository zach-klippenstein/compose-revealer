package com.zachklipp.revealer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zachklipp.revealer.ContentModel.AppModel

@Composable fun AppDetailsScreen(model: AppModel, onDismiss: () -> Unit) {
  BackHandler(onBack = onDismiss)

  Surface(Modifier.verticalScroll(rememberScrollState())) {
    Column {
      AppCard(model)
      Text(model.longDescription, Modifier.padding(16.dp))
    }
  }
}
