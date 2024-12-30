package gs.ad.gsadsexample

import androidx.multidex.MultiDexApplication
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.utils.ads.AdmBuilder
import gs.ad.utils.ads.AdmConfig
import gs.ad.utils.utils.GlobalVariables

class AppOwner: MultiDexApplication() {
    lateinit var mAdmBuilder: AdmBuilder

    override fun onCreate() {
        super.onCreate()
        mAdmBuilder = AdmBuilder.build(applicationContext) {
            keyShowOpen = AdKeyPosition.AppOpenAd_App_From_Background.name
            config = AdmConfig(
                listBannerAdUnitID = resources.getStringArray(R.array.banner_ad_unit_id).toList(),
                listInterstitialAdUnitID = resources.getStringArray(R.array.interstitial_ad_unit_id).toList(),
                listRewardAdUnitID = resources.getStringArray(R.array.reward_ad_unit_id).toList(),
                listNativeAdUnitID = resources.getStringArray(R.array.native_ad_unit_id).toList(),
                listOpenAdUnitID = resources.getStringArray(R.array.open_ad_unit_id).toList()
            )
        }

        enumValues<AdKeyPosition>().forEach {
            GlobalVariables.AdsKeyPositionAllow[it.name] = true
        }
    }
}