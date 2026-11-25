package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RewardedAdState {
    data object Idle : RewardedAdState
    data object Loading : RewardedAdState
    data object Ready : RewardedAdState
    data object Showing : RewardedAdState
    data class Failed(val message: String) : RewardedAdState
}

class AdMobManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var rewardedAd: RewardedAd? = null
    @Volatile
    private var isInitializedFlag = false

    private val _sdkInitialized = MutableStateFlow(false)
    val sdkInitialized: StateFlow<Boolean> = _sdkInitialized.asStateFlow()

    private val _rewardedAdState = MutableStateFlow<RewardedAdState>(RewardedAdState.Idle)
    val rewardedAdState: StateFlow<RewardedAdState> = _rewardedAdState.asStateFlow()

    val appId: String
        get() = BuildConfig.ADMOB_APP_ID.ifBlank { TEST_APP_ID }

    val rewardedAdUnitId: String
        get() = BuildConfig.ADMOB_REWARDED_AD_UNIT_ID.ifBlank { TEST_REWARDED_AD_UNIT_ID }

    val bannerAdUnitId: String
        get() = BuildConfig.ADMOB_BANNER_AD_UNIT_ID.ifBlank { TEST_BANNER_AD_UNIT_ID }

    val isUsingTestIds: Boolean
        get() = appId == TEST_APP_ID ||
            rewardedAdUnitId == TEST_REWARDED_AD_UNIT_ID ||
            bannerAdUnitId == TEST_BANNER_AD_UNIT_ID

    fun initialize(context: Context) {
        if (isInitializedFlag) return
        val appContext = context.applicationContext
        scope.launch(Dispatchers.IO) {
            runCatching {
                MobileAds.initialize(appContext) {
                    isInitializedFlag = true
                    _sdkInitialized.value = true
                    scope.launch(Dispatchers.Main.immediate) {
                        loadRewardedAd(appContext)
                    }
                }
            }.onFailure { err ->
                Log.w(TAG, "MobileAds initialization unavailable: ${err.localizedMessage}")
            }
        }
    }

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null) {
            _rewardedAdState.value = RewardedAdState.Ready
            return
        }
        if (_rewardedAdState.value is RewardedAdState.Loading ||
            _rewardedAdState.value is RewardedAdState.Showing
        ) {
            return
        }

        val appContext = context.applicationContext
        _rewardedAdState.value = RewardedAdState.Loading

        scope.launch(Dispatchers.Main.immediate) {
            runCatching {
                val request = AdRequest.Builder().build()
                RewardedAd.load(
                    appContext,
                    rewardedAdUnitId,
                    request,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardedAd = ad
                            _rewardedAdState.value = RewardedAdState.Ready
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            rewardedAd = null
                            _rewardedAdState.value = RewardedAdState.Failed(
                                "Gagal memuat iklan AdMob: ${loadAdError.message}"
                            )
                        }
                    }
                )
            }.onFailure { err ->
                rewardedAd = null
                _rewardedAdState.value = RewardedAdState.Failed(
                    "Layanan AdMob belum tersedia: ${err.localizedMessage ?: "Periksa koneksi / Google Play Services"}"
                )
            }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onClosedWithoutReward: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewardedAd(activity)
            return
        }

        var earnedReward = false
        _rewardedAdState.value = RewardedAdState.Showing

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                _rewardedAdState.value = RewardedAdState.Failed(
                    "Iklan AdMob gagal ditampilkan: ${adError.message}"
                )
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _rewardedAdState.value = RewardedAdState.Idle
                if (!earnedReward) {
                    onClosedWithoutReward()
                }
                loadRewardedAd(activity.applicationContext)
            }
        }

        runCatching {
            ad.show(activity) {
                earnedReward = true
                onRewardEarned()
            }
        }.onFailure { err ->
            rewardedAd = null
            _rewardedAdState.value = RewardedAdState.Failed(
                "Gagal memutar iklan AdMob: ${err.localizedMessage ?: "Kesalahan tidak diketahui"}"
            )
        }
    }

    companion object {
        private const val TAG = "AdMobManager"
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
