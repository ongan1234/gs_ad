package gs.ad.utils.ads

import androidx.constraintlayout.widget.ConstraintLayout
import gs.ad.utils.utils.PreferencesManager

class AdmManager(private val mAdmMachine: AdmMachine) {
    private constructor(builder: Builder) : this(builder.admMachine)

    fun resetCounterAds(keyCount: String){
        PreferencesManager.getInstance().resetCounterAds(keyCount)
    }

    fun getCounterAds(keyCount: String): Long{
        return PreferencesManager.getInstance().getCounterAds(keyCount)
    }

    fun initUMP(
        isTestUMP: Boolean = false,
        hashID: String = "AF6B8FFE15C0A60C3C1657041484F04E",
        gatherConsentFinished: () -> Unit
    ) {
        mAdmMachine.initUMP(isTestUMP, hashID, gatherConsentFinished)
    }

    fun resetInitUMP() {
        mAdmMachine.resetInitUMP()
    }

    fun setListener(event: OnAdmListener): AdmManager {
        mAdmMachine.setListener(event)
        return this
    }

    fun removeListener() {
        mAdmMachine.removeListener()
    }

    fun preloadInterstitialAd(): AdmManager {
        mAdmMachine.preloadInterstitialAd()
        return this
    }

    fun preloadRewardAd(): AdmManager {
        mAdmMachine.preloadRewardAd()
        return this
    }

    fun preloadOpenAd(): AdmManager {
        mAdmMachine.preloadOpenAd()
        return this
    }

    fun countToShowInterstitialAd(keyPosition: String, firstShowAd: Int, loopShowAd: Int): AdmManager {
        mAdmMachine.countToShowAds(TYPE_ADS.InterstitialAd, keyPosition, firstShowAd, loopShowAd)
        return this
    }

    fun countToShowRewardAd(keyPosition: String, firstShowAd: Int, loopShowAd: Int): AdmManager {
        mAdmMachine.countToShowAds(TYPE_ADS.RewardAd, keyPosition, firstShowAd, loopShowAd)
        return this
    }

    fun showOpenAd(keyPosition: String): AdmManager {
        mAdmMachine.showOpenAd(keyPosition)
        return this
    }

    fun showInterstitialAd(keyPosition: String): AdmManager {
        mAdmMachine.showInterstitialAd(keyPosition)
        return this
    }

    fun showRewardAd(keyPosition: String): AdmManager {
        mAdmMachine.showRewardAd(keyPosition)
        return this
    }

    fun preloadNativeAd(id: Int = 0, keyPosition: String, isFullScreen: Boolean): AdmManager {
        mAdmMachine.preloadNativeAd(id, keyPosition, isFullScreen)
        return this
    }

    fun applyNativeAdView(
        keyPosition: String,
        container: ConstraintLayout,
        layoutId: Int
    ): AdmManager {
        mAdmMachine.applyNativeAdView(keyPosition, container, layoutId)
        return this
    }

    fun loadNativeAd(
        id: Int = 0,
        keyPosition: String,
        container: ConstraintLayout,
        layoutId: Int,
        isFullScreen: Boolean
    ): AdmManager {
        mAdmMachine.loadNativeAd(id, keyPosition, container, layoutId, isFullScreen)
        return this
    }

    fun loadBannerAd(id: Int = 0, keyPosition: String, container: ConstraintLayout): AdmManager {
        mAdmMachine.loadBannerAd(id, keyPosition, container)
        return this
    }

    fun destroyAdByKeyPosition(typeAds: TYPE_ADS, keyPosition: String): AdmManager {
        when (typeAds) {
            TYPE_ADS.BannerAd -> mAdmMachine.destroyBannerAdByKeyPosition(keyPosition)
            TYPE_ADS.NativeAd -> mAdmMachine.destroyNativeAdByKeyPosition(keyPosition)
            TYPE_ADS.OpenAd -> TODO()
            TYPE_ADS.InterstitialAd -> TODO()
            TYPE_ADS.RewardAd -> TODO()
        }
        return this
    }

    fun pauseBannerAdView(): AdmManager {
        mAdmMachine.pauseBannerAdView()
        return this
    }

    fun resumeBannerAdView(): AdmManager {
        mAdmMachine.resumeBannerAdView()
        return this
    }

    companion object {
        const val TAG = "AdmManager"
        inline fun build(admMachine: AdmMachine, block: Builder.() -> Unit) =
            Builder(admMachine).apply(block).build()
    }

    class Builder(
        val admMachine: AdmMachine
    ) {
        fun build() = AdmManager(this)
    }
}