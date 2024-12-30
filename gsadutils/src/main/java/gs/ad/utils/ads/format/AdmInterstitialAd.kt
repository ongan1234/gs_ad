package gs.ad.utils.ads.format

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import gs.ad.utils.ads.AdmMachine
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.NetworkUtil
import gs.ad.utils.utils.PreferencesManager

internal class AdmInterstitialAd(
    private val context: Context,
    private val admMachine: AdmMachine,
    listInterstitialAdUnitID: List<String>
) : FullScreenContentCallback() {

    private val listInterstitialAdUnitId: List<String> = listInterstitialAdUnitID
    private var keyPosition: String = ""

    private var mInterstitialAd: InterstitialAd? = null
    private var countTier = 0

    fun loadAds() {
        if (!NetworkUtil.isNetworkAvailable(context)) return
        if (listInterstitialAdUnitId.isEmpty()) return
        val act= admMachine.getCurrentActivity() ?: return
        if (PreferencesManager.getInstance().isSUB()) return
        if (mInterstitialAd != null) {
            admMachine.onAdFailToLoaded(TYPE_ADS.InterstitialAd, keyPosition)
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            act, listInterstitialAdUnitId[countTier], adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.fullScreenContentCallback = this@AdmInterstitialAd
                    Log.d(TAG, "InterstitialAd onAdLoaded")
                    admMachine.onAdLoaded(TYPE_ADS.InterstitialAd, keyPosition)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d(TAG, "InterstitialAd $loadAdError")
                    mInterstitialAd = null
                    admMachine.onAdFailToLoaded(TYPE_ADS.InterstitialAd, keyPosition)
                    //closeAds();
                }
            })

        if (countTier >= listInterstitialAdUnitId.size - 1) {
            countTier = 0
        } else {
            countTier++
        }
    }

    private fun closeAds() {
        admMachine.getCurrentActivity()?.runOnUiThread {
            admMachine.closeAds(
                TYPE_ADS.InterstitialAd,
                keyPosition
            )
        }
        keyPosition = ""
    }

    override fun onAdClicked() {
        // Called when a click is recorded for an ad.
        Log.d(TAG, "Ad was clicked.")
        admMachine.onAdClicked(TYPE_ADS.InterstitialAd, keyPosition)
    }

    override fun onAdDismissedFullScreenContent() {
        // Called when ad is dismissed.
        // Set the ad reference to null so you don't show the ad a second time.
        Log.d(TAG, "Ad dismissed fullscreen content.")
        mInterstitialAd = null
        closeAds()
        loadAds()
    }

    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        // Called when ad fails to show.
        Log.e(TAG, "Ad failed to show fullscreen content.")
        mInterstitialAd = null
    }

    override fun onAdImpression() {
        // Called when an impression is recorded for an ad.
        Log.d(TAG, "Ad recorded an impression.")
    }

    override fun onAdShowedFullScreenContent() {
        // Called when ad is shown.
        Log.d(TAG, "Ad showed fullscreen content.")
        admMachine.onAdShow(TYPE_ADS.InterstitialAd, keyPosition)
    }

    fun showAds(key_pos: String) {
        val act = admMachine.getCurrentActivity() ?: return
        if (PreferencesManager.getInstance().isSUB()) {
            closeAds()
            return
        }

        admMachine.showAds()
        keyPosition = key_pos
        Log.d(TAG, key_pos)
        if (canShowAds()) {
            mInterstitialAd?.show(act)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
//          adsManager.activity.closeAds(TYPE_ADS.InterstitialAd);
//          loadAds();
        }
    }

    fun canShowAds(): Boolean {
        return mInterstitialAd != null
    }

    fun destroyAds() {
        mInterstitialAd = null
    }

    companion object{
        const val TAG = "AdmInterstitialAd"
    }
}
