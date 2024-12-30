package gs.ad.utils.ads.format

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import gs.ad.utils.ads.AdmMachine
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.NetworkUtil
import gs.ad.utils.utils.PreferencesManager
import gs.ad.utils.utils.GlobalVariables
import java.util.Date

internal class AdmOpenAd(
    var context: Context,
    private val admMachine: AdmMachine,
    listOpenAdUnitID: List<String>
) : FullScreenContentCallback() {
    private var keyPosition: String = ""

    val hasUsing4Hours: Boolean
        get() = GlobalVariables.hasUsing4Hours

    private var mAppOpenAd: AppOpenAd? = null
    private var countTier = 0
    private var loadTime: Long = 0
    private var isLoadedAds: Boolean = false

    private val listOpenAdId: List<String> = listOpenAdUnitID

    fun loadAds() {
        if (!NetworkUtil.isNetworkAvailable(context)) return
        val act = admMachine.getCurrentActivity() ?: return
        if (listOpenAdId.isEmpty()) return
        if (PreferencesManager.getInstance().isSUB()) return
        if (isAdAvailable()) {
            admMachine.onAdFailToLoaded(TYPE_ADS.OpenAd, keyPosition)
            return
        }

        if (isLoadedAds) return
        isLoadedAds = true

        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            act, listOpenAdId[countTier], adRequest,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    // Called when an app open ad has loaded.
                    Log.d(TAG, "AppOpenAd Ad was loaded.")
                    isLoadedAds = false
                    mAppOpenAd = ad
                    mAppOpenAd?.fullScreenContentCallback = this@AdmOpenAd
                    if (hasUsing4Hours) {
                        loadTime = Date().time
                    }

                    admMachine.onAdLoaded(TYPE_ADS.OpenAd, keyPosition)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Called when an app open ad has failed to load.
                    Log.d(TAG, loadAdError.message)
                    if (!hasUsing4Hours) {
                        mAppOpenAd = null
                    }
                    isLoadedAds = false
                    admMachine.onAdFailToLoaded(TYPE_ADS.OpenAd, keyPosition)
                    //closeAds();
                }
            })

        if (countTier >= listOpenAdId.size - 1) {
            countTier = 0
        } else {
            countTier++
        }
    }

    private fun closeAds() {
        admMachine.getCurrentActivity()?.runOnUiThread {
            admMachine.closeAds(TYPE_ADS.OpenAd, keyPosition)
        }
        keyPosition = ""
    }

    override fun onAdClicked() {
        // Called when a click is recorded for an ad.
        Log.d(TAG, "Ad was clicked.")
        admMachine.onAdClicked(TYPE_ADS.OpenAd, keyPosition)
    }

    override fun onAdDismissedFullScreenContent() {
        // Called when ad is dismissed.
        // Set the ad reference to null so you don't show the ad a second time.
        Log.d(TAG, "Ad dismissed fullscreen content.")
        // Dung 4 tieng command mAppOpenAd = null; va loadAds();
        if (!hasUsing4Hours) {
            mAppOpenAd = null
            loadAds()
        }
        closeAds()
    }

    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        // Called when ad fails to show.
        Log.e(TAG, "Ad failed to show fullscreen content.")
        // Dung 4 tieng command mAppOpenAd = null;
        if (!hasUsing4Hours) {
            mAppOpenAd = null
        }
    }

    override fun onAdImpression() {
        // Called when an impression is recorded for an ad.
        Log.d(TAG, "Ad recorded an impression.")
    }

    override fun onAdShowedFullScreenContent() {
        // Called when ad is shown.
        Log.d(TAG, "Ad showed fullscreen content.")
        admMachine.onAdShow(TYPE_ADS.OpenAd, keyPosition)
    }

    fun showAds(keyPosition: String) {
        val act = admMachine.getCurrentActivity() ?: return
        if (PreferencesManager.getInstance().isSUB()) return

        this.keyPosition = keyPosition
        if (isAdAvailable()) {
            mAppOpenAd?.show(act)
        } else {
            Log.d(TAG, "The open ad wasn't ready yet.")
            closeAds()
            loadAds()
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour = (60 * 60 * 1000).toLong()
        Log.d(
            TAG,
            "wasLoadTimeLessThanNHoursAgo : " + dateDifference + ", " + (numMilliSecondsPerHour * numHours)
        )
        return (dateDifference < (numMilliSecondsPerHour * numHours))
    }

    private fun isAdAvailable(): Boolean {
        Log.d(TAG, "isAdAvailable() : " + hasUsing4Hours)
        return if (!hasUsing4Hours) {
            mAppOpenAd != null
        } else {
            mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }
    }

    fun destroyAds() {
        mAppOpenAd = null
    }

    companion object{
        const val TAG = "AdmOpenAd"
    }
}