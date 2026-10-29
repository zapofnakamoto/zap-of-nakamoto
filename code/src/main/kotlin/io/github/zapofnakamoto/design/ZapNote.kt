package io.github.zapofnakamoto.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import whitewallet.brand.WalletStatus
import whitewallet.brand.ui.ListIconParams
import whitewallet.brand.ui.ListIconSize
import whitewallet.brand.ui.LocalBrandDesign
import whitewallet.brand.ui.LocalReduceMotion
import whitewallet.brand.ui.NoteFaceParams
import whitewallet.brand.ui.NoteFacePlace
import whitewallet.brand.ui.NoteFaceSize
import whitewallet.brand.ui.NoteStatus
import whitewallet.brand.ui.QrCodeCoverParams
import whitewallet.brand.ui.ToggleKind
import whitewallet.brand.ui.ToggleParams

/** How the card looks: [Spark] while it is money, an outline once it isn't, ink only on paper. */
private enum class Look { Spark, Claimed, TakenBack, Paper }

private val CARD = RoundedCornerShape(24.dp)
private val COVER = RoundedCornerShape(12.dp)
private val TILE = RoundedCornerShape(8.dp)

/**
 * A zap as design/explorations/r13-spark-card.html draws it, large: a spark card with its state on top, the mark, the
 * amount in Unbounded, and below "Scan to claim" the QR code (or the cover over it). Once claimed it is a dashed
 * outline, the money gone; taken back a solid one, the money yours again; on paper an ink outline, to save ink. In the
 * Zaps tab, a row with the card in small ([ZapRow]).
 */
@Composable
internal fun ZapNote(params: NoteFaceParams, modifier: Modifier) {
    if (params.size == NoteFaceSize.Row) return ZapRow(params, modifier)
    val reduceMotion = LocalReduceMotion.current
    val colors = MaterialTheme.colorScheme
    val look = when {
        params.place == NoteFacePlace.Paper -> Look.Paper
        params.status == NoteStatus.Redeemed -> Look.Claimed
        params.status == NoteStatus.TakenBack -> Look.TakenBack
        else -> Look.Spark
    }
    // Claimed while the page is open: the card empties once the mark has risen out of it (ZapMark's send). Taken back,
    // the mark doesn't move, so neither does the card wait.
    val change = if (reduceMotion) snap() else tween<Color>(300, delayMillis = if (look == Look.Claimed) 700 else 0)
    val fill by animateColorAsState(if (look == Look.Spark) colors.primaryContainer else Color.Transparent, change, label = "zap fill")
    val ink by animateColorAsState(if (look == Look.Spark) colors.onPrimaryContainer else colors.onSurface, change, label = "zap ink")
    val edge = when (look) {
        Look.Spark -> Modifier
        Look.Claimed -> Modifier.dashedOutline(ink, 2.dp, 24.dp)
        Look.TakenBack -> Modifier.border(2.dp, ink, CARD)
        Look.Paper -> Modifier.border(2.5.dp, ink, CARD)
    }
    Column(
        modifier.widthIn(min = 220.dp).background(fill, CARD).then(edge).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        stateLine(params)?.let { StateLine(stringResource(it), dot = look == Look.Spark, pill = look == Look.TakenBack, color = ink) }
        ZapMark(markStatus(params), 40.dp, reduceMotion, celebrate = true, settleFirst = true, color = ink)
        Text(
            params.amount,
            color = ink.copy(alpha = if (look == Look.Claimed) 0.55f else 1f),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        params.qrCode?.let { qrCode ->
            if (!params.qrCovered) Text(stringResource(R.string.zap_card_scan), color = ink, style = MaterialTheme.typography.labelLarge)
            qrCode()
        }
    }
}

/**
 * One zap in the Zaps tab: the card in small (a spark tile with the bolt, dashed while the zap is being paid or its
 * payment is unclear), the amount, what is unusual about it (or, handed out, its mint), and its age. The tap and, while
 * selecting, the checkbox's state are the app's.
 */
@Composable
private fun ZapRow(params: NoteFaceParams, modifier: Modifier) {
    val components = LocalBrandDesign.current.components
    val colors = MaterialTheme.colorScheme
    val busy = params.status == NoteStatus.Paying || params.status == NoteStatus.Unclear
    Row(
        modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        params.selected?.let { components.Toggle(ToggleParams(ToggleKind.Checkbox, it, onCheckedChange = null, enabled = params.enabled), Modifier) }
        Box(
            Modifier.size(32.dp).then(
                if (busy) Modifier.dashedOutline(colors.onSurfaceVariant, 1.5.dp, 8.dp) else Modifier.background(colors.primaryContainer, TILE),
            ),
            contentAlignment = Alignment.Center,
        ) {
            ZapBolt(if (busy) colors.onSurfaceVariant else colors.onPrimaryContainer, 18.dp)
        }
        Column(Modifier.weight(1f)) {
            Text(params.amount, style = MaterialTheme.typography.titleMedium)
            // A handed-out zap has no mint header above it, so its row names the mint.
            (params.statusText ?: params.mint?.takeIf { params.place == NoteFacePlace.Shared })?.let { text ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    params.statusIcon?.let { components.ListIcon(ListIconParams(it, size = ListIconSize.Inline), Modifier) }
                    Text(text, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        params.age?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
    }
}

/** What the card says on top: only where somebody waits for it or is about to take it. */
private fun stateLine(params: NoteFaceParams): Int? = when (params.place) {
    NoteFacePlace.Claim -> R.string.zap_card_ready
    NoteFacePlace.Shared -> when (params.status) {
        NoteStatus.Waiting -> R.string.zap_card_waiting
        NoteStatus.Redeemed -> R.string.zap_card_claimed
        NoteStatus.TakenBack -> R.string.zap_card_taken_back
        else -> null
    }
    else -> null
}

/**
 * The mark's face, as White Wallet's handed-out page tells it (waiting, sent once claimed, calm once back), happy on
 * the claim preview, where the zap is about to arrive; calm everywhere else.
 */
private fun markStatus(params: NoteFaceParams): WalletStatus = when (params.place) {
    NoteFacePlace.Claim -> WalletStatus.PaymentReceived(params.amountMsat)
    NoteFacePlace.Shared -> when (params.status) {
        NoteStatus.Waiting -> WalletStatus.WaitingForRecipient
        NoteStatus.Redeemed -> WalletStatus.Sent(params.amountMsat)
        else -> WalletStatus.Idle
    }
    else -> WalletStatus.Idle
}

/** Small capitals with a dot while the zap is money; on a spark pill once it came back. */
@Composable
private fun StateLine(text: String, dot: Boolean, pill: Boolean, color: Color) {
    val colors = MaterialTheme.colorScheme
    Row(
        if (pill) Modifier.background(colors.primaryContainer, CircleShape).padding(horizontal = 10.dp, vertical = 3.dp) else Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val textColor = if (pill) colors.onPrimaryContainer else color
        if (dot) Box(Modifier.size(7.dp).background(textColor, CircleShape))
        Text(
            text.toUpperCase(LocaleList.current),
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.1.em),
            textAlign = TextAlign.Center,
        )
    }
}

/** The cover over the zap's code, until a tap: deep spark (gold) with a dashed edge, so it reads as part of the card. */
@Composable
internal fun ZapQrCodeCover(params: QrCodeCoverParams, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.size(params.size).clip(COVER).then(modifier)
            .background(colors.tertiary)
            .dashedOutline(colors.onTertiary.copy(alpha = 0.35f), 2.dp, 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            params.label,
            Modifier.padding(24.dp),
            color = colors.onTertiary,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
    }
}

/** A dashed edge just inside the bounds, with [radius] corners. */
private fun Modifier.dashedOutline(color: Color, width: Dp, radius: Dp) = drawBehind {
    val w = width.toPx()
    drawRoundRect(
        color,
        topLeft = Offset(w / 2, w / 2),
        size = Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius(radius.toPx() - w / 2),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3 * w, 2 * w))),
    )
}
