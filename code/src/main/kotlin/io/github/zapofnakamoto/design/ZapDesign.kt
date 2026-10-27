package io.github.zapofnakamoto.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import whitewallet.brand.WalletStatus
import whitewallet.brand.ui.AmountParams
import whitewallet.brand.ui.Anchor
import whitewallet.brand.ui.BrandComponents
import whitewallet.brand.ui.BrandDesign
import whitewallet.brand.ui.ButtonKind
import whitewallet.brand.ui.ButtonParams
import whitewallet.brand.ui.DecorationContext
import whitewallet.brand.ui.DefaultComponents
import whitewallet.brand.ui.Tone
import whitewallet.brand.ui.walletColors

/**
 * Zap of Nakamoto's design in code (design/DESIGN.md), on top of its resources (palette, fonts, the static mark):
 * the living mark where the wallet shows how things are going, a celebration when a payment arrives, the wordmark for the name, spark pill buttons and the
 * balance in Unbounded.
 */
object ZapDesign : BrandDesign {
    override val components: BrandComponents = ZapComponents

    @Composable
    override fun Decoration(anchor: Anchor, context: DecorationContext, default: @Composable () -> Unit) {
        val mark = when (anchor) {
            Anchor.OnboardingHeader -> 120.dp
            Anchor.AppLock -> 96.dp
            Anchor.HomeHeader -> 88.dp
            Anchor.ReceiveHeader, Anchor.SendHeader, Anchor.ClaimResult -> 72.dp
            else -> null
        }
        when {
            anchor == Anchor.ReceiveSuccess || anchor == Anchor.SendSuccess -> Celebration(context.status, context.reduceMotion)
            mark != null -> ZapMark(context.status, mark, context.reduceMotion, followEvents = anchor == Anchor.HomeHeader)
            anchor == Anchor.OnboardingTitle -> Wordmark(large = true)
            anchor == Anchor.HomeTitle -> Wordmark(large = false)
            else -> default()
        }
    }
}

/**
 * Money moved, made bigger than design/DESIGN.md's Receive and Send, and the same on both screens: little bolts
 * fly and the mark ends happy, hopping twice.
 * - Arrived: the mark drops in and lights up; as it lands the bolts burst out all around.
 * - Sent: the mark focuses and rises out with a trail of bolts shooting up after it, then comes back and lights up.
 * With reduced motion: the happy mark, still.
 */
@Composable
private fun Celebration(status: WalletStatus, reduceMotion: Boolean) {
    val sending = status is WalletStatus.Sent
    val burst = remember { Animatable(0f) }
    val hop = remember { Animatable(0f) }
    LaunchedEffect(status) {
        if (reduceMotion) return@LaunchedEffect
        burst.snapTo(0f)
        hop.snapTo(0f)
        delay(if (sending) LIFTS_OFF_AFTER_MILLIS else LANDS_AFTER_MILLIS)
        launch { burst.animateTo(1f, tween(1_100, easing = LinearOutSlowInEasing)) }
        delay(if (sending) BACK_AFTER_MILLIS - LIFTS_OFF_AFTER_MILLIS else 500)
        repeat(2) {
            hop.animateTo(1f, tween(170, easing = FastOutSlowInEasing))
            hop.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
        }
    }
    val colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary)
    Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val t = burst.value
            if (t <= 0f || t >= 1f) return@Canvas
            SPARKS.forEachIndexed { i, spark ->
                // Sent: the same bolts, narrowed to a fan above the mark and carried up after it instead of falling.
                val angle = if (sending) -PI.toFloat() / 2 + (spark.angle / PI.toFloat() - 1) * 0.8f else spark.angle
                val distance = size.minDimension * (if (sending) 0.1f + spark.reach * 0.42f * t else 0.18f + spark.reach * 0.32f * t)
                val fall = size.minDimension * (if (sending) -0.08f * t else 0.12f * t * t)
                val x = center.x + cos(angle) * distance
                val y = center.y + sin(angle) * distance + fall
                rotate(spark.spin * 360f * t, Offset(x, y)) {
                    translate(x, y) { drawBolt(colors[i % colors.size].copy(alpha = 1f - t * t), spark.size * size.minDimension / 240f) }
                }
            }
        }
        ZapMark(status, 150.dp, reduceMotion, Modifier.graphicsLayer { translationY = -hop.value * 18.dp.toPx() }, celebrate = true)
    }
}

/** When the mark's drop-in (ZapMark's receive) touches down. */
private const val LANDS_AFTER_MILLIS = 400L

/** When the mark, after focusing and squashing (ZapMark's send), leaves upwards; and when it is back and happy. */
private const val LIFTS_OFF_AFTER_MILLIS = 600L
private const val BACK_AFTER_MILLIS = 2_000L

private class Spark(val angle: Float, val reach: Float, val spin: Float, val size: Float)

/** Twelve bolts around the circle, each a little different, the same every time. */
private val SPARKS = Random(7).let { r ->
    List(12) { i -> Spark(angle = (i / 12f + r.nextFloat() * 0.05f) * 2 * PI.toFloat(), reach = 0.6f + r.nextFloat() * 0.4f, spin = r.nextFloat() - 0.5f, size = 14f + r.nextFloat() * 10f) }
}

/** A little bolt, [height] tall, centred on the origin. */
private fun DrawScope.drawBolt(color: Color, height: Float) {
    val u = height / 88f
    val bolt = Path().apply {
        moveTo(8f * u, -44f * u); lineTo(-30f * u, 8f * u); lineTo(-2f * u, 8f * u)
        lineTo(-12f * u, 44f * u); lineTo(30f * u, -12f * u); lineTo(3f * u, -12f * u); close()
    }
    drawPath(bolt, color)
}

/** "zap" in Unbounded over "of Nakamoto": the name as the brand sets it. Screen readers get the app's name instead. */
@Composable
private fun Wordmark(large: Boolean) {
    val type = MaterialTheme.typography
    Column(horizontalAlignment = if (large) Alignment.CenterHorizontally else Alignment.Start) {
        Text(stringResource(R.string.wordmark_zap), style = if (large) type.displayMedium else type.headlineSmall)
        Text(
            stringResource(R.string.wordmark_of_nakamoto),
            style = if (large) type.titleMedium else type.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private object ZapComponents : BrandComponents {
    /** The main action is a spark pill with ink on it, light or dark; the others keep Material's, as pills too. */
    @Composable
    override fun Button(params: ButtonParams, modifier: Modifier) {
        if (params.kind != ButtonKind.Primary || params.tone != Tone.Neutral) return DefaultComponents.button(params, modifier)
        val colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Button(params.onClick, modifier, enabled = params.enabled && !params.busy, shape = CircleShape, colors = colors) {
            if (params.busy) CircularProgressIndicator(Modifier.size(20.dp), color = colors.contentColor, strokeWidth = 2.dp) else params.content(this)
        }
    }

    /** The balance, big, in Unbounded (the display font). */
    @Composable
    override fun Amount(params: AmountParams, modifier: Modifier) {
        Text(params.text, modifier, color = MaterialTheme.walletColors.amount, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
    }
}
