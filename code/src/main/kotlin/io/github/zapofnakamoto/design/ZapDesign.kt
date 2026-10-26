package io.github.zapofnakamoto.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
 * Zap of Nakamoto's design in code (../zap-app/DESIGN.md), on top of its resources (palette, fonts, the static mark):
 * the living mark where the wallet shows how things are going, the wordmark for the name, spark pill buttons and the
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
            mark != null -> ZapMark(context.status, mark, context.reduceMotion, followEvents = anchor == Anchor.HomeHeader)
            anchor == Anchor.OnboardingTitle -> Wordmark(large = true)
            anchor == Anchor.HomeTitle -> Wordmark(large = false)
            else -> default()
        }
    }
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
