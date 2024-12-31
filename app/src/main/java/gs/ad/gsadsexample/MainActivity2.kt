package gs.ad.gsadsexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.databinding.ActivityMain2Binding
import gs.ad.utils.ads.AdmManager
import gs.ad.utils.ads.OnAdmListener
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.GlobalVariables

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private val mAdmManager: AdmManager get() { return (application as AppOwner).mAdmBuilder.getActivity(this)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        mAdmManager.setListener(object: OnAdmListener {
            override fun onAdLoaded(typeAds: TYPE_ADS, keyPosition: String) {
                super.onAdLoaded(typeAds, keyPosition)
            }

            override fun onAdClicked(typeAds: TYPE_ADS, keyPosition: String) {
                super.onAdClicked(typeAds, keyPosition)
            }

            override fun onAdClosed(typeAds: TYPE_ADS, keyPosition: String) {
                super.onAdClosed(typeAds, keyPosition)
                if (typeAds == TYPE_ADS.InterstitialAd){
                    if (keyPosition == AdKeyPosition.InterstitialAd_ScMain2.name){
                        finish()
                    }
                }
            }
        })

        binding.button.setOnClickListener {
            mAdmManager.showInterstitialAd(AdKeyPosition.InterstitialAd_ScMain2.name)
        }

        binding.button1.setOnClickListener {
            mAdmManager.showRewardAd(AdKeyPosition.RewardAd_ScMain2.name)
        }

        binding.nativeAdContainerView.postDelayed({
            mAdmManager
                .loadNativeAd(0, AdKeyPosition.NativeAd_ScMain2.name, binding.nativeAdContainerView, R.layout.layout_native_ad_origin,
                    isFullScreen = false
                )
        }, 1000)
    }

    override fun onStart() {
        super.onStart()
        GlobalVariables.canShowOpenAd = true
    }

    override fun onDestroy() {
        mAdmManager.destroyAdByKeyPosition(TYPE_ADS.NativeAd, AdKeyPosition.NativeAd_ScMain2.name)
        mAdmManager.removeListener()
        super.onDestroy()
    }
}