package io.github.zapofnakamoto.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import whitewallet.brand.WalletEvent
import whitewallet.brand.WalletStatus
import whitewallet.brand.ui.LocalWalletEvents

/**
 * The mark (../zap-app/DESIGN.md "The mark", "States", "Motion"): a chubby bolt with a face cut out of it, which shows
 * how things are going. Drawn in the theme's primary colour (ink on light, spark on dark), in a box of [size].
 *
 * [status] picks the face and, when it changes, the motion: a payment arriving drops the bolt in (Happy), one going
 * out lifts it away and back (Focus, then Rest), a problem shakes it (Oops). With [followEvents] the mark also reacts
 * to payments anywhere in the app (the wallet tab). With [reduceMotion] nothing moves; only the face changes.
 */
@Composable
internal fun ZapMark(status: WalletStatus, size: Dp, reduceMotion: Boolean, modifier: Modifier = Modifier, followEvents: Boolean = false) {
    val color = MaterialTheme.colorScheme.primary
    val motion = remember { MarkMotion() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    motion.reduceMotion = reduceMotion
    motion.onArrived = { haptics.performHapticFeedback(HapticFeedbackType.Confirm) }

    LaunchedEffect(status) { motion.show(status) }
    if (followEvents) {
        val events = LocalWalletEvents.current
        LaunchedEffect(events) {
            events.collect { event ->
                scope.launch {
                    when (event) {
                        is WalletEvent.PaymentReceived -> {
                            motion.receive()
                            delay(HAPPY_FOR_MILLIS)
                            motion.show(status)
                        }
                        is WalletEvent.PaymentSent -> motion.send()
                        else -> Unit
                    }
                }
            }
        }
    }
    // Idle blink: the open dots close for a moment, about every three seconds.
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        while (true) {
            delay(2_600L + Random.nextLong(800))
            motion.blink = true
            delay(130)
            motion.blink = false
        }
    }
    // Waiting breathes slowly.
    val breath = if (motion.face == Face.Waiting && !reduceMotion) {
        rememberInfiniteTransition(label = "breath").animateFloat(1f, 1.04f, infiniteRepeatable(tween(1_400), RepeatMode.Reverse), label = "breath").value
    } else {
        1f
    }

    Box(modifier.size(size)) {
        // The flash ring sits behind the bolt and doesn't move with it.
        Canvas(Modifier.fillMaxSize()) {
            if (motion.ringAlpha <= 0f) return@Canvas
            val unit = this.size.minDimension / BOX
            drawCircle(
                color.copy(alpha = motion.ringAlpha),
                radius = 50 * unit * motion.ringScale,
                center = center,
                style = Stroke(width = 3 * unit),
            )
        }
        Canvas(
            Modifier.fillMaxSize().graphicsLayer {
                val boltHeight = this.size.minDimension * BOLT_HEIGHT / BOX
                translationY = motion.ty * boltHeight
                translationX = motion.shake * boltHeight
                scaleX = motion.sx * breath
                scaleY = motion.sy * breath
                rotationZ = motion.tilt
                alpha = motion.alpha
                // An offscreen layer, so the face can be cleared out of the bolt.
                compositingStrategy = CompositingStrategy.Offscreen
            },
        ) { drawMark(color, motion) }
    }
}

/** How long the mark stays happy after a payment arrived on the wallet tab. */
private const val HAPPY_FOR_MILLIS = 2_500L

/** The mark's box is -10..110: 120 units. */
private const val BOX = 120f

/** The bolt's height, stroke included: what translations are relative to (CSS `transform-box: fill-box`). */
private const val BOLT_HEIGHT = 100f

private val BOLT = listOf(62f to 6f, 16f to 58f, 44f to 58f, 34f to 94f, 84f to 38f, 57f to 38f, 70f to 6f)

private fun DrawScope.drawMark(color: Color, motion: MarkMotion) {
    val unit = size.minDimension / BOX
    translate(10 * unit, 10 * unit) {
        val bolt = Path().apply {
            BOLT.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * unit, y * unit) else lineTo(x * unit, y * unit) }
            close()
        }
        drawPath(bolt, color)
        drawPath(bolt, color, style = Stroke(width = 12 * unit, join = StrokeJoin.Round))

        // The face, cut out: whatever is behind the mark shows through.
        val line = Stroke(width = 3.6f * unit, cap = StrokeCap.Round)
        val eyes = listOf(Offset(41f, 47f), Offset(58f, 45f)).map { it + motion.look }
        when {
            motion.happyEyes -> listOf(Offset(36.5f, 48.5f), Offset(53.5f, 46.5f)).forEach { (x, y) ->
                val arc = Path().apply {
                    moveTo(x * unit, y * unit)
                    quadraticTo((x + 4.5f) * unit, (y - 5.5f) * unit, (x + 9f) * unit, y * unit)
                }
                drawPath(arc, Color.Black, style = line, blendMode = BlendMode.Clear)
            }
            motion.blink -> eyes.forEach { (x, y) ->
                drawLine(Color.Black, Offset((x - 3.4f) * unit, y * unit), Offset((x + 3.4f) * unit, y * unit), 2.6f * unit, StrokeCap.Round, blendMode = BlendMode.Clear)
            }
            else -> eyes.forEach { (x, y) -> drawCircle(Color.Black, 3.8f * unit, Offset(x * unit, y * unit), blendMode = BlendMode.Clear) }
        }
        val m = motion.mouth
        val mouth = Path().apply {
            moveTo(m[0] * unit, m[1] * unit)
            quadraticTo(m[2] * unit, m[3] * unit, m[4] * unit, m[5] * unit)
        }
        drawPath(mouth, Color.Black, style = line, blendMode = BlendMode.Clear)
    }
}

/** A face: its eyes and its mouth (a quadratic curve `x0 y0 cx cy x1 y1`, so faces morph into each other). */
internal enum class Face(val mouth: FloatArray, val look: Offset = Offset.Zero, val happyEyes: Boolean = false) {
    Rest(floatArrayOf(44f, 55.5f, 51f, 61.5f, 58f, 53.5f)),
    Curious(floatArrayOf(47.5f, 56.5f, 51f, 59f, 54.5f, 56f)),
    Waiting(floatArrayOf(46f, 56f, 51f, 58.5f, 56f, 55.5f), look = Offset(2f, -1.5f)),
    Focus(floatArrayOf(46f, 56f, 51f, 58.5f, 56f, 55.5f)),
    Happy(floatArrayOf(42.5f, 54f, 51f, 66f, 60f, 52f), happyEyes = true),
    Oops(floatArrayOf(45f, 59f, 51f, 54f, 57f, 58f)),
}

private val EASE_OUT = CubicBezierEasing(0f, 0f, 0.58f, 1f)
private val EASE_IN = CubicBezierEasing(0.42f, 0f, 1f, 1f)
private val DROP = CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f)
private val RISE = CubicBezierEasing(0.5f, 0f, 0.7f, 0.4f)

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

/** The mark's moving parts, and the motions of DESIGN.md that drive them. */
private class MarkMotion {
    var reduceMotion = false
    var onArrived: () -> Unit = {}

    var face by mutableStateOf(Face.Rest)
    var mouth by mutableStateOf(Face.Rest.mouth)
    var look by mutableStateOf(Offset.Zero)
    var happyEyes by mutableStateOf(false)
    var blink by mutableStateOf(false)
    var ty by mutableFloatStateOf(0f)
    var sx by mutableFloatStateOf(1f)
    var sy by mutableFloatStateOf(1f)
    var alpha by mutableFloatStateOf(1f)
    var tilt by mutableFloatStateOf(0f)
    var shake by mutableFloatStateOf(0f)
    var ringScale by mutableFloatStateOf(1f)
    var ringAlpha by mutableFloatStateOf(0f)

    /** Settles on the face for [status], with the motion that leads there. */
    suspend fun show(status: WalletStatus) {
        if (status !is WalletStatus.Empty) untilt()
        when (status) {
            is WalletStatus.PaymentReceived -> receive()
            is WalletStatus.Sent -> send()
            is WalletStatus.Problem -> oops()
            WalletStatus.Empty -> curious()
            WalletStatus.AwaitingPayment -> face(Face.Waiting, 220)
            WalletStatus.Sending -> face(Face.Focus, 200)
            else -> face(Face.Rest, 220)
        }
    }

    /** Morphs to [to] over [millis]: the mouth tweens (ease-out cubic), the eyes switch halfway. */
    suspend fun face(to: Face, millis: Int) {
        val fromMouth = mouth
        val fromLook = look
        face = to
        if (reduceMotion || millis == 0) {
            mouth = to.mouth; look = to.look; happyEyes = to.happyEyes
            return
        }
        animate(0f, 1f, animationSpec = tween(millis, easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f))) { t, _ ->
            mouth = FloatArray(6) { lerp(fromMouth[it], to.mouth[it], t) }
            look = Offset(lerp(fromLook.x, to.look.x, t), lerp(fromLook.y, to.look.y, t))
            if (t >= 0.5f) happyEyes = to.happyEyes
        }
    }

    private fun rest() {
        ty = 0f; sx = 1f; sy = 1f; alpha = 1f; shake = 0f; ringAlpha = 0f
    }

    /** Receive: the bolt drops in from above and squashes, a flash ring goes out, then the face lights up. */
    suspend fun receive() {
        if (reduceMotion) {
            rest(); face(Face.Happy, 0); onArrived(); return
        }
        coroutineScope {
            ty = -0.7f; alpha = 0f
            launch {
                animate(0f, 1f, animationSpec = tween(700, easing = EASE_OUT)) { v, _ ->
                    ringScale = lerp(0.7f, 1.35f, v); ringAlpha = lerp(0.8f, 0f, v)
                }
            }
            animate(0f, 1f, animationSpec = tween(650, easing = DROP)) { v, _ ->
                if (v < 0.6f) {
                    val k = v / 0.6f
                    ty = lerp(-0.7f, 0.06f, k); sx = lerp(1f, 1.08f, k); sy = lerp(1f, 0.9f, k); alpha = k.coerceIn(0f, 1f)
                } else {
                    val k = (v - 0.6f) / 0.4f
                    ty = lerp(0.06f, 0f, k); sx = lerp(1.08f, 1f, k); sy = lerp(0.9f, 1f, k); alpha = 1f
                }
            }
        }
        rest()
        onArrived()
        face(Face.Happy, 220)
    }

    /** Send is receive backwards: focus, the ring pulls in, the bolt squashes and rises out, then fades back calmly. */
    suspend fun send() {
        if (reduceMotion) {
            rest(); face(Face.Rest, 0); return
        }
        face(Face.Focus, 200)
        coroutineScope {
            launch {
                animate(0f, 1f, animationSpec = tween(480, easing = EASE_IN)) { v, _ ->
                    if (v < 0.8f) {
                        ringScale = lerp(1.35f, 0.75f, v / 0.8f); ringAlpha = 0.8f
                    } else {
                        val k = (v - 0.8f) / 0.2f
                        ringScale = lerp(0.75f, 0.7f, k); ringAlpha = lerp(0.8f, 0f, k)
                    }
                }
            }
            delay(260)
            animate(0f, 1f, animationSpec = tween(650, easing = RISE)) { v, _ ->
                if (v < 0.35f) {
                    val k = v / 0.35f
                    ty = lerp(0f, 0.06f, k); sx = lerp(1f, 1.08f, k); sy = lerp(1f, 0.9f, k)
                } else {
                    val k = (v - 0.35f) / 0.65f
                    ty = lerp(0.06f, -0.7f, k); sx = lerp(1.08f, 1f, k); sy = lerp(0.9f, 1f, k); alpha = lerp(1f, 0f, k)
                }
            }
        }
        delay(220)
        ty = 0f; ringAlpha = 0f
        face(Face.Rest, 0)
        animate(0f, 1f, animationSpec = tween(420, easing = EASE_OUT)) { v, _ ->
            alpha = v; sx = lerp(0.94f, 1f, v); sy = sx
        }
        rest()
    }

    /** Oops: the face worries, then shakes once. */
    suspend fun oops() {
        rest()
        face(Face.Oops, 200)
        if (reduceMotion) return
        animate(
            0f, 0f,
            animationSpec = keyframes {
                durationMillis = 420
                -0.06f at 105
                0.06f at 210
                -0.04f at 315
            },
        ) { v, _ -> shake = v }
        shake = 0f
    }

    /** Empty: the head tilts and the smile gets small. */
    suspend fun curious() = coroutineScope {
        rest()
        launch { face(Face.Curious, 400) }
        if (reduceMotion) tilt = -9f else animate(tilt, -9f, animationSpec = tween(400, easing = EASE_OUT)) { v, _ -> tilt = v }
    }

    private suspend fun untilt() {
        if (tilt == 0f) return
        if (reduceMotion) tilt = 0f else animate(tilt, 0f, animationSpec = tween(300, easing = EASE_OUT)) { v, _ -> tilt = v }
    }
}
