package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextSecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBannerBar(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    val isInspection = LocalInspectionMode.current
    if (isInspection) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(SneatSurface)
                .testTag("admob_banner_bar")
        ) {
            HorizontalDivider(color = SneatBorder, thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AdMob Adaptive Banner",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SneatTextSecondary
                )
            }
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasLoadFailed by remember(adUnitId) { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_bar")
    ) {
        val widthDp = if (maxWidth.value.isFinite()) {
            maxWidth.value.toInt().coerceAtLeast(320)
        } else {
            360
        }

        val adView = remember(context, adUnitId, widthDp) {
            runCatching {
                AdView(context).apply {
                    val adaptiveSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
                    setAdSize(adaptiveSize)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            hasLoadFailed = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            hasLoadFailed = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }.getOrNull()
        }

        if (adView != null) {
            DisposableEffect(lifecycleOwner, adView) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> runCatching { adView.resume() }
                        Lifecycle.Event.ON_PAUSE -> runCatching { adView.pause() }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    runCatching { adView.destroy() }
                }
            }

            if (!hasLoadFailed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SneatSurface)
                ) {
                    HorizontalDivider(color = SneatBorder, thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { adView },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
