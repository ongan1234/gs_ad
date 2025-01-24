package gs.ad.utils.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import gs.ad.utils.ads.format.AdmBannerAd
import gs.ad.utils.ads.format.AdmInterstitialAd
import gs.ad.utils.ads.format.AdmNativeAd
import gs.ad.utils.ads.format.AdmOpenAd
import gs.ad.utils.ads.format.AdmobRewardAd
import gs.ad.utils.databinding.PopupLoadAdsBinding
import gs.ad.utils.utils.GlobalVariables
import gs.ad.utils.utils.NetworkUtil
import gs.ad.utils.utils.PreferencesManager
import gs.ad.utils.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AdmMachine(
    val context: Context, val config: AdmConfig
) {

    private var mainActivity: Activity? = null
    private var currentActivity: Activity? = null
    private var dialogLoadAds: Dialog? = null
    private var countDownTimerLoadAds: CountDownTimer? = null
    private var countTimeShowAds: Int = 0
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager =
        GoogleMobileAdsConsentManager.getInstance(context)

    private lateinit var mBannerAd: AdmBannerAd
    private lateinit var mNativeAd: AdmNativeAd
    private lateinit var mAdmInterstitialAd: AdmInterstitialAd
    private lateinit var mAdmRewardAd: AdmobRewardAd
    private lateinit var mAdmAppOpenAd: AdmOpenAd

    private var mHandleEvent: HashMap<String, OnAdmListener> = HashMap()

    private val keyEvent: String
        get() {
            val act = getCurrentActivity()
            val key = act::class.java.simpleName
            return key
        }

    private val eventManager: OnAdmListener?
        get(){
            return mHandleEvent[keyEvent]
        }

    private constructor(builder: Builder) : this(builder.context, builder.config) {
        mBannerAd = AdmBannerAd(builder.context, this, builder.config.listBannerAdUnitID)
        mNativeAd = AdmNativeAd(builder.context, this, builder.config.listNativeAdUnitID)
        mAdmInterstitialAd =
            AdmInterstitialAd(builder.context, this, builder.config.listInterstitialAdUnitID)
        mAdmRewardAd = AdmobRewardAd(builder.context, this, builder.config.listRewardAdUnitID)
        mAdmAppOpenAd = AdmOpenAd(builder.context, this, builder.config.listOpenAdUnitID)
        Prefs.init(builder.context)
    }

    fun getCurrentActivity(): Activity {
        val act = currentActivity ?: mainActivity!!
        return act
    }

    fun isMainActivity(activity: Activity){
        mainActivity = activity
    }

    fun removeMainActivity(){
        mainActivity = null
    }

    fun setActivity(activity: Activity) {
        if(activity.isDestroyed || activity.isFinishing) return
        currentActivity = activity
    }

    fun setListener(event: OnAdmListener) {
        val key = keyEvent
        mHandleEvent[key] = event
    }

    fun removeListener() {
        try {
            val key = keyEvent
            mHandleEvent.remove(key)
            currentActivity = null
        }catch(e : Exception) {
            Log.d(TAG, "removeListener: " + e.localizedMessage)
        }
    }

    fun showInterstitialAd(keyPosition: String) {
        showPopupLoadAds(TYPE_ADS.InterstitialAd, keyPosition)
    }

    fun showRewardAd(keyPosition: String) {
        showPopupLoadAds(TYPE_ADS.RewardAd, keyPosition)
    }

    fun showAds() {
        getCurrentActivity().runOnUiThread { delEventDialogLoadAds() }
    }

    fun closeAds(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "closeAds: " + typeAds.name + ", position: " + keyPosition)
        if (typeAds != TYPE_ADS.BannerAd && typeAds != TYPE_ADS.NativeAd) {
            mBannerAd.showAdView()
            mNativeAd.showAdView()
        }

        GlobalVariables.isShowPopup = false
        delEventDialogLoadAds()

        eventManager?.onAdClosed(typeAds, keyPosition)
    }

    fun haveReward(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "haveReward: " + typeAds.name + ", position: " + keyPosition)

        eventManager?.onAdHaveReward(typeAds, keyPosition)
    }

    fun notHaveReward(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "notHaveReward: " + typeAds.name + ", position: " + keyPosition)

        eventManager?.onAdNotHaveReward(typeAds, keyPosition)
    }

    fun onAdFailToLoaded(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "onAdFailToLoaded: " + typeAds.name + ", position: " + keyPosition)

        eventManager?.onAdFailToLoaded(typeAds, keyPosition)
    }

    fun onAdShow(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "onAdShow: " + typeAds.name + ", position: " + keyPosition)
        if (typeAds != TYPE_ADS.BannerAd && typeAds != TYPE_ADS.NativeAd) {
            mBannerAd.hideAdView()
            mNativeAd.hideAdView()
        }

        eventManager?.onAdShowed(typeAds, keyPosition)
    }

    fun onAdLoaded(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "onAdLoaded: " + typeAds.name + ", position: " + keyPosition)

        eventManager?.onAdLoaded(typeAds, keyPosition)
    }

    fun onAdClicked(typeAds: TYPE_ADS, keyPosition: String) {
        Log.d(TAG, "onAdClicked: " + typeAds.name + ", position: " + keyPosition)

        eventManager?.onAdClicked(typeAds, keyPosition)
    }

    fun countToShowAds(typeAds: TYPE_ADS, keyPosition: String, startAds: Int, loopAds: Int) {
        if (PreferencesManager.getInstance().isSUB() || PreferencesManager.getInstance().isRemoveAds()) {
            closeAds(typeAds, keyPosition)
            return
        }

        var countFullAds = PreferencesManager.getInstance().getCounterAds(keyPosition)
        countFullAds += 1
        PreferencesManager.getInstance().saveCounterAds(keyPosition, countFullAds)
        val isShowAds = if (countFullAds < startAds) {
            false
        } else if (countFullAds == startAds) {
            true
        } else {
            (countFullAds - startAds) % loopAds == 0
        }

        if (isShowAds) {
            showPopupLoadAds(typeAds, keyPosition)
        } else {
            closeAds(typeAds, keyPosition)
        }
    }

    private fun showPopupLoadAds(typeAds: TYPE_ADS, keyPosition: String) {
        if (GlobalVariables.AdsKeyPositionAllow[keyPosition] == false || !googleMobileAdsConsentManager.canRequestAds || PreferencesManager.getInstance().isRemoveAds()) {
            closeAds(typeAds, keyPosition)
            return
        }

        if (typeAds == TYPE_ADS.InterstitialAd && PreferencesManager.getInstance().isSUB()){
            closeAds(typeAds, keyPosition)
            return
        }

        GlobalVariables.isShowPopup = true
        dialogLoadAds = Dialog(getCurrentActivity())
        dialogLoadAds?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogLoadAds?.setCancelable(false)
        dialogLoadAds?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val binding = PopupLoadAdsBinding.inflate(dialogLoadAds!!.layoutInflater)
        dialogLoadAds?.setContentView(binding.getRoot())

//        binding.animationView.spin();

        //*** Admob ***
        if (typeAds == TYPE_ADS.RewardAd) mAdmRewardAd.loadAds() else if (typeAds == TYPE_ADS.InterstitialAd) mAdmInterstitialAd.loadAds()
        countTimeShowAds = 0
        countDownTimerLoadAds = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countTimeShowAds++
                if (countTimeShowAds < 3) return
                //*** Admob ***
                if (typeAds == TYPE_ADS.RewardAd && mAdmRewardAd.canShowAds()) {
                    mAdmRewardAd.ShowAds(keyPosition)
                } else if (typeAds == TYPE_ADS.InterstitialAd && mAdmInterstitialAd.canShowAds()) {
                    mAdmInterstitialAd.showAds(keyPosition)
                }
            }

            override fun onFinish() {
                if (dialogLoadAds != null && dialogLoadAds!!.isShowing) {
                    dialogLoadAds?.dismiss()
                }

                //*** Admob ***
                if (typeAds == TYPE_ADS.RewardAd) {
                    if (mAdmRewardAd.canShowAds()) mAdmRewardAd.ShowAds(keyPosition) else closeAds(
                        typeAds, keyPosition
                    )
                } else if (typeAds == TYPE_ADS.InterstitialAd) {
                    if (mAdmInterstitialAd.canShowAds()) mAdmInterstitialAd.showAds(keyPosition) else closeAds(
                        typeAds, keyPosition
                    )
                }
            }
        }.start()
        dialogLoadAds?.show()
    }

    private fun delEventDialogLoadAds() {
        if (dialogLoadAds != null && dialogLoadAds?.isShowing == true) {
            dialogLoadAds?.dismiss()
            dialogLoadAds = null
        }
        if (countDownTimerLoadAds != null) {
            countDownTimerLoadAds?.cancel()
            countDownTimerLoadAds = null
        }
    }


    fun preloadInterstitialAd() {
        if (googleMobileAdsConsentManager.canRequestAds) mAdmInterstitialAd.loadAds()
    }

    fun preloadRewardAd() {
        if (googleMobileAdsConsentManager.canRequestAds) mAdmRewardAd.loadAds()
    }

    fun preloadOpenAd() {
        if (googleMobileAdsConsentManager.canRequestAds) mAdmAppOpenAd.loadAds()
    }

    fun showOpenAd(keyPosition: String) {
        if (googleMobileAdsConsentManager.canRequestAds && GlobalVariables.AdsKeyPositionAllow[keyPosition] == true)
            mAdmAppOpenAd.showAds(keyPosition)
    }

    fun preloadNativeAd(id: Int = -1, keyPosition: String, isFullScreen: Boolean,
                        isVideoOption: Boolean = false,
                        isMutedVideo: Boolean = true,
                        mediaAspectRatio: Int = MediaAspectRatio.PORTRAIT,
                        nativeAdOptions: Int = NativeAdOptions.ADCHOICES_TOP_LEFT) {
        if (GlobalVariables.AdsKeyPositionAllow[keyPosition] == true && googleMobileAdsConsentManager.canRequestAds) mNativeAd.preloadAd(
            id,
            keyPosition,
            isFullScreen,
            isVideoOption,
            isMutedVideo,
            mediaAspectRatio,
            nativeAdOptions
        )
        else mNativeAd.destroyView()
    }

    fun applyNativeAdView(
        keyPosition: String, container: ConstraintLayout, layoutId: Int
    ) {
        if (GlobalVariables.AdsKeyPositionAllow[keyPosition] == true && googleMobileAdsConsentManager.canRequestAds) {
            val nativeAdView = LayoutInflater.from(context).inflate(layoutId, null) as NativeAdView
            mNativeAd.applyNativeAdView(keyPosition, container, nativeAdView)
        } else mNativeAd.destroyView()
    }

    fun loadNativeAd(
        id: Int = -1,
        keyPosition: String,
        container: ConstraintLayout,
        layoutNativeAdViewId: Int,
        isFullScreen: Boolean,
        isVideoOption: Boolean = false,
        isMutedVideo: Boolean = true,
        mediaAspectRatio: Int = MediaAspectRatio.PORTRAIT,
        nativeAdOptions: Int = NativeAdOptions.ADCHOICES_TOP_LEFT
    ) {
        if (GlobalVariables.AdsKeyPositionAllow[keyPosition] == true && googleMobileAdsConsentManager.canRequestAds) {
            mNativeAd.loadAd(id, keyPosition, container, layoutNativeAdViewId, isFullScreen, isVideoOption, isMutedVideo, mediaAspectRatio, nativeAdOptions)
        } else mNativeAd.destroyView()
    }

    fun destroyNativeAdByKeyPosition(keyPosition: String) {
        mNativeAd.destroyView(keyPosition)
    }

    fun loadBannerAd(id: Int = -1, keyPosition: String, container: ConstraintLayout) {
        if (GlobalVariables.AdsKeyPositionAllow[keyPosition] == true && googleMobileAdsConsentManager.canRequestAds) mBannerAd.loadBanner(
            id,
            keyPosition,
            container
        )
        else mBannerAd.destroyView()
    }

    fun destroyBannerAdByKeyPosition(keyPosition: String) {
        mBannerAd.destroyView(keyPosition)
    }

    fun pauseBannerAdView() {
        mBannerAd.pauseAdView()
    }

    fun resumeBannerAdView() {
        mBannerAd.resumeAdView()
    }

    private fun showPopupNetworkError(
        isTestUMP: Boolean, hashID: String, gatherConsentFinished: () -> Unit
    ) {
        if (NetworkUtil.isNetworkAvailable(context)) return
        AlertDialog.Builder(getCurrentActivity()!!).setTitle("Network error")
            .setMessage("The connection to the network is impossible. Please check the status of your connection or try again in a few minutes.")
            .setCancelable(false).setPositiveButton(
                "OK"
            ) { _, _ ->
                clickPopupNetworkErrorButtonOK(
                    isTestUMP, hashID, gatherConsentFinished
                )
            }.create().show()
    }


    private fun clickPopupNetworkErrorButtonOK(
        isTestUMP: Boolean, hashID: String, gatherConsentFinished: () -> Unit
    ) {
        initUMP(isTestUMP, hashID, gatherConsentFinished)
    }

    fun initUMP(
        isTestUMP: Boolean = false,
        hashID: String = "",
        gatherConsentFinished: () -> Unit
    ) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            googleMobileAdsConsentManager.gatherConsent(
                getCurrentActivity(), isTestUMP, hashID
            ) { consentError ->
                if (consentError != null) {
                    // Consent not obtained in current session.
                    Log.d(
                        TAG, String.format("%s: %s", consentError.errorCode, consentError.message)
                    )
                }

                if (googleMobileAdsConsentManager.canRequestAds) {
                    initializeMobileAdsSdk(isTestUMP, hashID, gatherConsentFinished)
                } else {
                    gatherConsentFinished()
                }
            }
        } else {
            showPopupNetworkError(isTestUMP, hashID, gatherConsentFinished)
        }
    }

    fun resetInitUMP() {
        isMobileAdsInitializeCalled.set(false)
    }

    private fun initializeMobileAdsSdk(
        isTestUMP: Boolean, hashID: String, gatherConsentFinished: () -> Unit
    ) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }


        if (isTestUMP) {
            // Set your test devices.
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(listOf(hashID)).build()
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            //if (MobileAds.getInitializationStatus() == InitializationStatus)
            MobileAds.initialize(getCurrentActivity()) {
                getCurrentActivity().runOnUiThread {
                    gatherConsentFinished()
                }
            }
        }
    }

    companion object {
        const val TAG = "AdmMachine"

        inline fun build(context: Context, block: Builder.() -> Unit) =
            Builder(context).apply(block).build()
    }

    class Builder(
        val context: Context
    ) {
        lateinit var config: AdmConfig
        fun build() = AdmMachine(this)
    }
}