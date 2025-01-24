package gs.ad.gsadsexample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import gs.ad.gsadsexample.MainActivity.Companion.TAG
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.databinding.ActivityMain2Binding
import gs.ad.utils.ads.AdmManager
import gs.ad.utils.ads.OnAdmListener
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.google_iab.BillingClientLifecycle
import gs.ad.utils.google_iab.OnBillingListener
import gs.ad.utils.google_iab.models.PurchaseInfo
import gs.ad.utils.utils.GlobalVariables
import gs.ad.utils.utils.PreferencesManager

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private val mAdmManager: AdmManager get() { return (application as AppOwner).mAdmBuilder.getActivity(this)}
    private val mBillingClientLifecycle: BillingClientLifecycle?
        get() {
            return (application as AppOwner).mBillingClientLifecycle ?: null
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        mBillingClientLifecycle?.setListener(this, object : OnBillingListener{
            override fun onPurchasedProductsFetched(purchaseInfos: List<PurchaseInfo>) {
                super.onPurchasedProductsFetched(purchaseInfos)
                runOnUiThread {
                    checkSubToUpdateUI()
                }
            }
        })

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
                        mAdmManager
                            .destroyAdByKeyPosition(TYPE_ADS.NativeAd, AdKeyPosition.NativeAd_ScMain2.name)
                            .destroyAdByKeyPosition(TYPE_ADS.BannerAd, AdKeyPosition.BannerAd_ScMain2.name)
                            .removeListener()
                        mBillingClientLifecycle?.removeListener(this@MainActivity2)
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

        mAdmManager
            .loadNativeAd(-1, AdKeyPosition.NativeAd_ScMain2.name, binding.nativeAdContainerView, R.layout.layout_native_ad_origin,
                isFullScreen = false
            )

        mAdmManager.loadBannerAd(-1, AdKeyPosition.BannerAd_ScMain2.name, binding.bannerView)
    }

    override fun onResume() {
        super.onResume()
        mAdmManager.resumeBannerAdView()
    }

    override fun onPause() {
        super.onPause()
        mAdmManager.pauseBannerAdView()
    }

    private fun checkSubToUpdateUI() {
        if (PreferencesManager.getInstance().isSUB() || PreferencesManager.getInstance().isRemoveAds()) {
            mAdmManager.destroyAdByKeyPosition(
                TYPE_ADS.NativeAd,
                AdKeyPosition.NativeAd_ScMain2.name
            )
        } else {
            //TODO
        }
    }

    override fun onStart() {
        super.onStart()
        GlobalVariables.canShowOpenAd = true
        mBillingClientLifecycle?.fetchSubPurchasedProducts()
    }
}