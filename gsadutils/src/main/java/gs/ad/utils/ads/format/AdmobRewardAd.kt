package gs.ad.utils.ads.format

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import gs.ad.utils.ads.AdmMachine
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.NetworkUtil
import gs.ad.utils.utils.PreferencesManager

internal class AdmobRewardAd(
    private val context: Context,
    private val admMachine: AdmMachine,
    listRewardAdUnitID: List<String>
) : FullScreenContentCallback() {

    private var keyPosition: String = ""
    private var mRewardedAd: RewardedAd? = null
    private var isReward = false
    private var countTier = 0

    private val listRewardAdUnitId: List<String> = listRewardAdUnitID

    fun loadAds() {
        isReward = false

        if (!NetworkUtil.isNetworkAvailable(context) ||
            listRewardAdUnitId.isEmpty() ||
            mRewardedAd != null ||
            PreferencesManager.getInstance().isRemoveAds()){
            admMachine.onAdFailToLoaded(TYPE_ADS.RewardAd, keyPosition)
            return
        }
        //if(PreferencesManager.getInstance().isSUB())return;

        val act = admMachine.getCurrentActivity()
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            act, listRewardAdUnitId[countTier],
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    Log.d(TAG, loadAdError.toString())
                    admMachine.onAdFailToLoaded(TYPE_ADS.RewardAd, keyPosition)
                    mRewardedAd = null
                    closeAds()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    mRewardedAd = ad
                    mRewardedAd!!.fullScreenContentCallback = this@AdmobRewardAd
                    admMachine.onAdLoaded(TYPE_ADS.RewardAd, keyPosition)
                    Log.d(TAG, "Ad was loaded.")
                }
            })

        if (countTier >= listRewardAdUnitId.size - 1) {
            countTier = 0
        } else {
            countTier++
        }
    }

    private fun closeAds() {
        admMachine.getCurrentActivity()?.runOnUiThread {
            admMachine.closeAds(
                TYPE_ADS.RewardAd,
                keyPosition
            )
        }
        keyPosition = ""
    }

    override fun onAdClicked() {
        // Called when a click is recorded for an ad.
        Log.d(TAG, "Ad was clicked.")
        admMachine.onAdClicked(TYPE_ADS.RewardAd, keyPosition)
    }

    override fun onAdDismissedFullScreenContent() {
        // Called when ad is dismissed.
        // Set the ad reference to null so you don't show the ad a second time.
        Log.d(TAG, "Ad dismissed fullscreen content.")
        mRewardedAd = null
        admMachine.getCurrentActivity()?.runOnUiThread {
            if (isReward) admMachine.haveReward(TYPE_ADS.RewardAd, keyPosition)
            else admMachine.notHaveReward(TYPE_ADS.RewardAd, keyPosition)
            admMachine.closeAds(TYPE_ADS.RewardAd, keyPosition)
        }

        loadAds()
    }

    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        // Called when ad fails to show.
        Log.e(TAG, "Ad failed to show fullscreen content.")
        mRewardedAd = null
    }

    override fun onAdImpression() {
        // Called when an impression is recorded for an ad.
        Log.d(TAG, "Ad recorded an impression.")
    }

    override fun onAdShowedFullScreenContent() {
        // Called when ad is shown.
        Log.d(TAG, "Ad showed fullscreen content.")
    }

    fun ShowAds(key_pos: String) {
        val act = admMachine.getCurrentActivity() ?: return
        keyPosition = key_pos
        admMachine.showAds()
        //        if(PreferencesManager.getInstance().isSUB() || PreferencesManager.getInstance().isRemoveAds() )return;
        if (canShowAds()) {
            mRewardedAd!!.show(
                act
            ) { isReward = true }
        } else {
            Log.d("TAG", "The reward ad wasn't ready yet.")
            //            adsManager.activity.closeAds(TYPE_ADS.RewardAd);
//            loadAds();
        }
    }

    fun canShowAds(): Boolean {
        return mRewardedAd != null
    }

    fun destroyAds() {
        mRewardedAd = null
    }

    companion object{
        const val TAG = "AdmRewardAd"
    }
}