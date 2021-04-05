package com.zachklipp.revealer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.Placeable.PlacementScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round

@Stable
interface RevealerScope {
  @Composable fun Revealable(
    content: @Composable (RevealableState) -> Unit
  )
}

@Stable
interface RevealableState {
  val isFullyCollapsed: Boolean
  val isFullyExpanded: Boolean
  suspend fun reveal(animationSpec: AnimationSpec<Float> = SpringSpec())
  suspend fun unreveal(animationSpec: AnimationSpec<Float> = SpringSpec())
}

@Composable fun Revealer(
  placeholder: Modifier = Modifier,
  content: @Composable RevealerScope.() -> Unit
) {
  val rememberedPlaceholder = rememberUpdatedState(placeholder)
  val state = remember { RevealerState(rememberedPlaceholder) }

  SubcomposeLayout(
    Modifier.onGloballyPositioned {
      state.revealerCoordinates = it
    }
  ) { constraints ->
    state.revealerConstraints = constraints
    state.subcomposeMeasureScope = this

    println("OMG subcomposing Revealer")
    val mainMeasurable = subcompose(Unit) {
      Box {
        state.content()
      }
    }.single()

    val mainPlaceable = mainMeasurable.measure(constraints)

    // This has to happen after measuring the main composable in case it also uses subcomposelayout
    // to add Revealables during the measure pass.
    state.revealables.forEach { (_, revealable) ->
      revealable.measure()
    }

    layout(width = mainPlaceable.width, height = mainPlaceable.height) {
      mainPlaceable.placeRelative(0, 0)

      state.revealables.forEach { (_, revealable) ->
        revealable.place(this)
      }

      state.subcomposeMeasureScope = null
    }
  }
}

private class RevealableStateImpl(
  private val revealerState: RevealerState,
  val key: Int
) : RevealableState {

  var content: @Composable (RevealableState) -> Unit by mutableStateOf({})

  var measurable: Measurable? = null
  private lateinit var placeable: Placeable

  var placeholderOffset: Offset by mutableStateOf(Offset.Zero)
  var layerOffset: Offset by mutableStateOf(Offset.Zero)

  val revealAnimation = Animatable(0f)

  private var cachedPlaceholderConstraints: Constraints? by mutableStateOf(null)
  var cachedPlaceholderSize: IntSize? by mutableStateOf(null)

  override val isFullyCollapsed: Boolean
    get() = revealAnimation.value == 0f

  override val isFullyExpanded: Boolean
    get() = revealAnimation.value == 1f

  override suspend fun reveal(animationSpec: AnimationSpec<Float>) {
    revealAnimation.animateTo(1f, animationSpec)
  }

  override suspend fun unreveal(animationSpec: AnimationSpec<Float>) {
    revealAnimation.animateTo(0f, animationSpec)
  }

  fun updatePlaceholderConstraints(constraints: Constraints) {
    cachedPlaceholderConstraints = constraints

    // If the Revealable happens to be getting laid out while the Revealer is doing so, we can
    // measure synchronously – and must, so that the cachedPlaceholderSize is initialized.
    requestPlaceholderMeasure()
  }

  /** This helps track if we've already composed in the current pass. */
  private var subcomposedWithScope: SubcomposeMeasureScope? = null

  fun measure() {
    // Always register a read for the animation, even if the measure has already been done and
    // cached, so we get re-laid-out when the animation starts.
    revealAnimation.value
    needsSubcompose = true
    requestPlaceholderMeasure()
  }

  private var needsSubcompose = true

  fun place(scope: PlacementScope) {
    // Tell the revealable how much to offset the canvas when drawing.
    layerOffset = placeholderOffset

    val offset = when {
      isFullyCollapsed -> placeholderOffset
      isFullyExpanded -> Offset.Zero
      else -> lerp(placeholderOffset, Offset.Zero, revealAnimation.value)
    }

    with(scope) {
      placeable.placeRelative(offset.round())
    }

    subcomposedWithScope = null
  }

  private fun requestPlaceholderMeasure() {
    // If there's no measurable, we're not in the middle of a Revealer layout, so just use the
    // cached values.
    requestSubcompose()
    val measurable = measurable ?: return
    this.measurable = null

    val placeholderConstraints = checkNotNull(cachedPlaceholderConstraints)

    val constraints: Constraints = when {
      isFullyCollapsed -> placeholderConstraints
      isFullyExpanded -> revealerState.revealerConstraints!!
      else -> {
        lerp(
          // While animating, set the max constraints so the content can immediately ask to fill
          // its entire size. Otherwise, it is likely that the start and end max constraints are
          // both infinite, so no animation will occur. We can provide special modifiers in-scope
          // for content to fill its Revealer size.
          start = placeholderConstraints.copy(
            maxWidth = cachedPlaceholderSize!!.width,
            maxHeight = cachedPlaceholderSize!!.height
          ),
          stop = revealerState.revealerConstraints!!,
          fraction = revealAnimation.value
        )
      }
    }

    placeable = measurable.measure(constraints)

    if (isFullyCollapsed) {
      cachedPlaceholderSize = IntSize(placeable.width, placeable.height)
    }
  }

  private fun requestSubcompose() {
    println("OMG [$key] requestSubcompose: measurable=$measurable, needsSubcompose=$needsSubcompose, subcomposedWithScope=$subcomposedWithScope, subcomposeMeasureScope=${revealerState.subcomposeMeasureScope}")

    // We're already measured, nothing to do.
    if (measurable != null) return

    if (!needsSubcompose) return
    // We've already composed in this pass.
    if (subcomposedWithScope == revealerState.subcomposeMeasureScope) return
    subcomposedWithScope = revealerState.subcomposeMeasureScope

    revealerState.subcomposeMeasureScope?.let {
      needsSubcompose = false

      measurable = it.subcompose(key) {
        Box(Modifier.drawWithContent {
          withTransform({
            val latestOffset = placeholderOffset
            val layerOffset = layerOffset
            val canvasOffset = latestOffset - layerOffset
            // Note that this canvas translation also affects Android Views embedded in the
            // revealable.
            translate(left = canvasOffset.x, top = canvasOffset.y)
          }) {
            this@drawWithContent.drawContent()
          }
        }) {
          content(this@RevealableStateImpl)
        }
      }.single()
    }
  }
}

private class RevealerState(
  private val placeholder: State<Modifier>
) : RevealerScope {

  fun summarize(): String =
    "${revealables.size} revealables: ${
      revealables.toList().joinToString { it.second.key.toString() }
    }"

  var revealerConstraints: Constraints? = null
  var revealerCoordinates: LayoutCoordinates? by mutableStateOf(null)

  /** Only non-null while the revealer's layout is laying out. */
  var subcomposeMeasureScope: SubcomposeMeasureScope? = null

  val revealables: MutableMap<Int, RevealableStateImpl> = mutableStateMapOf()

  @Composable override fun Revealable(
    content: @Composable (RevealableState) -> Unit
  ) {
    val key = currentCompositeKeyHash
    val state = remember { getOrCreateRevealable(key) }
    state.content = content

    DisposableEffect(Unit) {
      println("OMG [$key] placeholder entered composition")
      onDispose {
        println("OMG [$key] placeholder left composition")
      }
    }

    DisposableEffect(Unit) {
      onDispose {
        revealables -= key
      }
    }

    Layout(
      content = { Box(placeholder.value) },
      modifier = Modifier.onGloballyPositioned { coordinates ->
        state.placeholderOffset = revealerCoordinates!!.localPositionOf(coordinates, Offset.Zero)
      }
    ) { measurables, constraints ->
      println("OMG [$key] measuring placeholder…")

      // This will ensure cachedPlaceholderSize is set.
      state.updatePlaceholderConstraints(constraints)

      // Once the real content has been composed, force the placeholder to be its size. Until then,
      // the size is determined solely by the placeholder. This will happen when, for example,
      // the revealable is inside a lazy list item. In that case, the lazy list will subcompose
      // item outside of the revealer's layout block, so we need to measure some non-zero initial
      // size until the next frame when the new revealable will actually be calculated. If we just
      // measure the placeholder as zero initially, then when scrolling down, the new item will
      // jump fully into view.
      val placeholderConstraints =
        state.cachedPlaceholderSize?.let { Constraints.fixed(it.width, it.height) }
          ?: constraints
      val placeholderPlaceable = measurables.single().measure(placeholderConstraints)
      val size = state.cachedPlaceholderSize ?: IntSize(
        placeholderPlaceable.width,
        placeholderPlaceable.height
      )

      layout(width = size.width, height = size.height) {
        placeholderPlaceable.placeRelative(0, 0)
      }
    }
  }

  private fun getOrCreateRevealable(key: Int): RevealableStateImpl {
    check(key !in revealables)
    return RevealableStateImpl(this, key).also {
      println("OMG [$key] created new RevealableState for $this")
      revealables[key] = it
    }
  }
}

private fun lerp(start: Constraints, stop: Constraints, fraction: Float): Constraints {
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
  val actualMinSize = lerp(startMinSize, targetMinSize, fraction)
  val actualMaxSize = lerp(startMaxSize, targetMaxSize, fraction)

  return Constraints(
    minWidth = actualMinSize.width.toInt(),
    minHeight = actualMinSize.height.toInt(),
    maxWidth = actualMaxSize.width.toInt(),
    maxHeight = actualMaxSize.height.toInt()
  )
}