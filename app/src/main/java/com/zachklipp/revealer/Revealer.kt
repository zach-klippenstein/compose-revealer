package com.zachklipp.revealer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round

@Stable
interface RevealerScope {
  @Composable fun Revealable(
    state: RevealableState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
  )

  @Stable
  fun Modifier.matchRevealedWidth(): Modifier
}

@Stable
interface RevealableState {
  val isFullyCollapsed: Boolean
  val isFullyExpanded: Boolean
  val isExpanding: Boolean
  val expandedFraction: Float

  suspend fun reveal(animationSpec: AnimationSpec<Float> = SpringSpec())
  suspend fun unreveal(animationSpec: AnimationSpec<Float> = SpringSpec())
}

@Composable fun rememberRevealableState(): RevealableState {
  return remember { RevealableStateImpl() }
}

@Composable fun Revealer(
  modifier: Modifier = Modifier,
  content: @Composable RevealerScope.() -> Unit
) {
  val state = remember { RevealerState() }

  SubcomposeLayout(
    modifier.onGloballyPositioned {
      state.revealerCoordinates = it
    }
  ) { constraints ->
    state.revealerConstraints = constraints
    state.subcomposeMeasureScope = this

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

private class RevealableStateImpl : RevealableState {

  private val revealAnimation = Animatable(0f)

  override val isFullyCollapsed: Boolean
    get() = revealAnimation.value == 0f

  override val isFullyExpanded: Boolean
    get() = revealAnimation.value == 1f

  override val isExpanding: Boolean
    get() = revealAnimation.targetValue == 1f

  override val expandedFraction: Float
    get() = revealAnimation.value

  override suspend fun reveal(animationSpec: AnimationSpec<Float>) {
    revealAnimation.animateTo(1f, animationSpec)
  }

  override suspend fun unreveal(animationSpec: AnimationSpec<Float>) {
    revealAnimation.animateTo(0f, animationSpec)
  }
}

private class RevealableController(
  private val revealerState: RevealerState,
  state: State<RevealableState>,
  val key: Int
) {
  private val state by state

  var content: @Composable () -> Unit by mutableStateOf({})

  var measurable: Measurable? = null
  private lateinit var placeable: Placeable

  var placeholderOffset: Offset by mutableStateOf(Offset.Zero)
  var layerOffset: Offset by mutableStateOf(Offset.Zero)

  private var cachedPlaceholderConstraints: Constraints? by mutableStateOf(null)
  var cachedPlaceholderSize: IntSize? by mutableStateOf(null)

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
    state.expandedFraction
    needsSubcompose = true
    requestPlaceholderMeasure()
  }

  private var needsSubcompose = true

  fun place(scope: PlacementScope) {
    // Tell the revealable how much to offset the canvas when drawing.
    layerOffset = placeholderOffset

    val offset = when {
      state.isFullyCollapsed -> placeholderOffset
      state.isFullyExpanded -> Offset.Zero
      else -> lerp(placeholderOffset, Offset.Zero, state.expandedFraction)
    }

    with(scope) {
      placeable.placeRelative(
        position = offset.round(),
        // Revealables that are expanded should be drawn on top of those which aren't.
        // Those which are expandING should be drawn on top of those which are collapsing.
        zIndex = when {
          state.isFullyCollapsed -> 0f
          state.isExpanding -> 2f
          else -> 1f
        }
      )
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
      state.isFullyCollapsed -> placeholderConstraints
      state.isFullyExpanded -> revealerState.revealerConstraints!!
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
          fraction = state.expandedFraction
        )
      }
    }

    placeable = measurable.measure(constraints)

    if (state.isFullyCollapsed) {
      cachedPlaceholderSize = IntSize(placeable.width, placeable.height)
    }
  }

  private fun requestSubcompose() {
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
          content()
        }
      }.single()
    }
  }
}

private class RevealerState : RevealerScope {

  var revealerConstraints: Constraints? by mutableStateOf(null)
  var revealerCoordinates: LayoutCoordinates? by mutableStateOf(null)

  /** Only non-null while the revealer's layout is laying out. */
  var subcomposeMeasureScope: SubcomposeMeasureScope? = null

  val revealables: MutableMap<Int, RevealableController> = mutableStateMapOf()

  @Composable override fun Revealable(
    state: RevealableState,
    modifier: Modifier,
    content: @Composable () -> Unit
  ) {
    val key = currentCompositeKeyHash
    val rememberedState = rememberUpdatedState(state)
    val controller = remember { getOrCreateRevealable(key, rememberedState) }
    controller.content = content

    DisposableEffect(Unit) {
      onDispose {
        revealables -= key
      }
    }

    Layout(
      // TODO can this be passed to the layout directly?
      content = { Box(modifier) },
      modifier = Modifier.onGloballyPositioned { coordinates ->
        controller.placeholderOffset =
          revealerCoordinates!!.localPositionOf(coordinates, Offset.Zero)
      }
    ) { measurables, constraints ->
      // This will ensure cachedPlaceholderSize is set.
      controller.updatePlaceholderConstraints(constraints)

      // Once the real content has been composed, force the placeholder to be its size. Until then,
      // the size is determined solely by the placeholder. This will happen when, for example,
      // the revealable is inside a lazy list item. In that case, the lazy list will subcompose
      // item outside of the revealer's layout block, so we need to measure some non-zero initial
      // size until the next frame when the new revealable will actually be calculated. If we just
      // measure the placeholder as zero initially, then when scrolling down, the new item will
      // jump fully into view.
      val placeholderConstraints =
        controller.cachedPlaceholderSize?.let { Constraints.fixed(it.width, it.height) }
          ?: constraints
      val placeholderPlaceable = measurables.single().measure(placeholderConstraints)
      val size = controller.cachedPlaceholderSize ?: IntSize(
        placeholderPlaceable.width,
        placeholderPlaceable.height
      )

      println("OMG [$key] measured revealable: $size")

      layout(width = size.width, height = size.height) {
        placeholderPlaceable.placeRelative(0, 0)
      }
    }
  }

  override fun Modifier.matchRevealedWidth(): Modifier = composed {
    with(LocalDensity.current) {
      val maxWidth = revealerConstraints!!.maxWidth.toDp()
      widthIn(min = maxWidth, max = maxWidth)
    }
  }

  private fun getOrCreateRevealable(
    key: Int,
    state: State<RevealableState>
  ): RevealableController {
    check(key !in revealables)
    return RevealableController(this, state, key).also {
      revealables[key] = it
    }
  }
}
