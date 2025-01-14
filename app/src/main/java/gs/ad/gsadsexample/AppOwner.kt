package gs.ad.gsadsexample

import android.util.Log
import androidx.multidex.MultiDexApplication
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.sub.ConsumableProductId
import gs.ad.gsadsexample.sub.SubscriptionProductId
import gs.ad.utils.ads.AdmBuilder
import gs.ad.utils.ads.AdmConfig
import gs.ad.utils.google_iab.BillingClientLifecycle
import gs.ad.utils.utils.GlobalVariables

class AppOwner: MultiDexApplication(){
    lateinit var mAdmBuilder: AdmBuilder
    lateinit var mBillingClientLifecycle: BillingClientLifecycle

    override fun onCreate() {
        super.onCreate()

        mBillingClientLifecycle = BillingClientLifecycle.build(applicationContext){
            licenseKey = applicationContext.getString(R.string.license_key)
            consumableIds = enumValues<ConsumableProductId>().map { it.id }
            subscriptionIds = enumValues<SubscriptionProductId>().map { it.id }
        }

        mAdmBuilder = AdmBuilder.build(applicationContext) {
            keyShowOpen = AdKeyPosition.AppOpenAd_App_From_Background.name
            config = AdmConfig(
                listBannerAdUnitID = resources.getStringArray(R.array.banner_ad_unit_id).toList(),
                listInterstitialAdUnitID = resources.getStringArray(R.array.interstitial_ad_unit_id).toList(),
                listRewardAdUnitID = resources.getStringArray(R.array.reward_ad_unit_id).toList(),
                listNativeAdUnitID = resources.getStringArray(R.array.native_ad_unit_id).toList(),
                listOpenAdUnitID = resources.getStringArray(R.array.open_ad_unit_id).toList()
            )
            billingClient = mBillingClientLifecycle
        }

        enumValues<AdKeyPosition>().forEach {
            GlobalVariables.AdsKeyPositionAllow[it.name] = true
        }

    }

    override fun onTerminate() {
        mAdmBuilder.resetCounterAds(AdKeyPosition.InterstitialAd_ScMain.name)
        super.onTerminate()
    }

    companion object{
        const val TAG = "AppOwner"
    }
}