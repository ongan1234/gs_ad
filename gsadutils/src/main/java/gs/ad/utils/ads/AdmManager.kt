package gs.ad.utils.ads

import android.app.Activity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.nativead.NativeAdOptions
import gs.ad.utils.utils.PreferencesManager

class AdmManager(private val mAdmMachine: AdmMachine) {
    private constructor(builder: Builder) : this(builder.admMachine)

    fun resetCounterAds(keyCount: String){
        PreferencesManager.getInstance().resetCounterAds(keyCount)
    }

    fun getCounterAds(keyCount: String): Int{
        return PreferencesManager.getInstance().getCounterAds(keyCount)
    }

    fun initUMP(
        isTestUMP: Boolean = false,
        hashID: String = "",
        gatherConsentFinished: () -> Unit
    ) {
        mAdmMachine.initUMP(isTestUMP, hashID, gatherConsentFinished)
    }

    fun resetInitUMP() {
        mAdmMachine.resetInitUMP()
    }

    fun setListener(event: OnAdmListener){
        mAdmMachine.setListener(event)
    }

    fun removeListener():AdmManager {
        mAdmMachine.removeListener()
        return this
    }

    fun removeMainActivity():AdmManager{
        mAdmMachine.removeMainActivity()
        return this
    }

    fun preloadInterstitialAd(){
        mAdmMachine.preloadInterstitialAd()
    }

    fun preloadRewardAd() {
        mAdmMachine.preloadRewardAd()
    }

    fun preloadOpenAd() {
        mAdmMachine.preloadOpenAd()
    }

    fun countToShowInterstitialAd(keyPosition: String, firstShowAd: Int, loopShowAd: Int) {
        mAdmMachine.countToShowAds(TYPE_ADS.InterstitialAd, keyPosition, firstShowAd, loopShowAd)
    }

    fun countToShowRewardAd(keyPosition: String, firstShowAd: Int, loopShowAd: Int) {
        mAdmMachine.countToShowAds(TYPE_ADS.RewardAd, keyPosition, firstShowAd, loopShowAd)
    }

    fun showOpenAd(keyPosition: String) {
        mAdmMachine.showOpenAd(keyPosition)
    }

    fun showInterstitialAd(keyPosition: String) {
        mAdmMachine.showInterstitialAd(keyPosition)
    }

    fun showRewardAd(keyPosition: String) {
        mAdmMachine.showRewardAd(keyPosition)
    }

    fun preloadNativeAd(id: Int = -1, keyPosition: String, isFullScreen: Boolean,
                        isVideoOption: Boolean = false,
                        isMutedVideo: Boolean = true,
                        mediaAspectRatio: Int = MediaAspectRatio.PORTRAIT,
                        nativeAdOptions: Int = NativeAdOptions.ADCHOICES_TOP_LEFT) {
        mAdmMachine.preloadNativeAd(id, keyPosition, isFullScreen, isVideoOption, isMutedVideo, mediaAspectRatio, nativeAdOptions)
    }

    fun applyNativeAdView(
        keyPosition: String,
        container: ConstraintLayout,
        layoutId: Int
    ) {
        mAdmMachine.applyNativeAdView(keyPosition, container, layoutId)
    }

    fun loadNativeAd(
        id: Int = -1,
        keyPosition: String,
        container: ConstraintLayout,
        layoutId: Int,
        isFullScreen: Boolean,
        isVideoOption: Boolean = false,
        isMutedVideo: Boolean = true,
        mediaAspectRatio: Int = MediaAspectRatio.PORTRAIT,
        nativeAdOptions: Int = NativeAdOptions.ADCHOICES_TOP_LEFT
    ) {
        mAdmMachine.loadNativeAd(id, keyPosition, container, layoutId, isFullScreen, isVideoOption, isMutedVideo, mediaAspectRatio, nativeAdOptions)
    }

    fun loadBannerAd(id: Int = -1, keyPosition: String, container: ConstraintLayout) {
        mAdmMachine.loadBannerAd(id, keyPosition, container)
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

    fun pauseBannerAdView() {
        mAdmMachine.pauseBannerAdView()
    }

    fun resumeBannerAdView() {
        mAdmMachine.resumeBannerAdView()
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