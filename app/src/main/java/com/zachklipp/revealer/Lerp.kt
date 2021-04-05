package com.zachklipp.revealer

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Constraints

internal fun lerp(start: Constraints, stop: Constraints, fraction: Float): Constraints {
  val startMinSize = Size(
    width = start.minWidth.toFloat(),
    height = start.minHeight.toFloat()
  )
  val startMaxSize = Size(
    width = start.maxWidth.toFloat(),
    height = start.maxHeight.toFloat()
  )

  val targetMinSize = Size(
    width = stop.minWidth.toFloat(),
    height = stop.minHeight.toFloat()
  )
  val targetMaxSize = Size(
    width = stop.maxWidth.toFloat(),
    height = stop.maxHeight.toFloat()
  )
  val actualMinSize = androidx.compose.ui.geometry.lerp(startMinSize, targetMinSize, fraction)
  val actualMaxSize = androidx.compose.ui.geometry.lerp(startMaxSize, targetMaxSize, fraction)

  return Constraints(
    minWidth = actualMinSize.width.toInt(),
    minHeight = actualMinSize.height.toInt(),
    maxWidth = actualMaxSize.width.toInt(),
    maxHeight = actualMaxSize.height.toInt()
  )
}
