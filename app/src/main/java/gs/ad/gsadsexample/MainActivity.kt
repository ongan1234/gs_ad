package gs.ad.gsadsexample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.databinding.ActivityMainBinding
import gs.ad.utils.ads.AdmManager
import gs.ad.utils.ads.IAdsManager
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.GlobalVariables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mAdmManager: AdmManager get() { return (application as AppOwner).mAdmBuilder.getActivity(this)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            mAdmManager.showInterstitialAd(AdKeyPosition.InterstitialAd_ScMain.name).setListener(object: IAdsManager {
                override fun onAdClosed(typeAds: TYPE_ADS, keyPosition: String) {
                    super.onAdClosed(typeAds, keyPosition)
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }
            })
        }

        binding.button1.setOnClickListener {
            mAdmManager.showRewardAd(AdKeyPosition.RewardAd_ScMain.name).setListener(object: IAdsManager {
                override fun onAdClosed(typeAds: TYPE_ADS, keyPosition: String) {
                    super.onAdClosed(typeAds, keyPosition)
                }
            })
        }

        binding.bannerView.postDelayed({
            mAdmManager.loadBannerAd(0, AdKeyPosition.BannerAd_ScMain.name, binding.bannerView)
                .setListener(object: IAdsManager {
                    override fun onAdLoaded(typeAds: TYPE_ADS, keyPosition: String) {
                        super.onAdLoaded(typeAds, keyPosition)
                    }

                    override fun onAdClicked(typeAds: TYPE_ADS, keyPosition: String) {
                        super.onAdClicked(typeAds, keyPosition)
                    }

                    override fun onAdClosed(typeAds: TYPE_ADS, keyPosition: String) {
                        super.onAdClosed(typeAds, keyPosition)
                    }
                })

        }, 1000)


        startShimmerLoading()
        binding.nativeAdContainerView.postDelayed({
            mAdmManager
                .loadNativeAd(0, AdKeyPosition.NativeAd_ScMain.name, binding.nativeAdContainerView, R.layout.layout_native_ad,
                    isFullScreen = false
                ).setListener(object: IAdsManager{
                    override fun onAdFailToLoaded(typeAds: TYPE_ADS, keyPosition: String) {
                        super.onAdFailToLoaded(typeAds, keyPosition)
                        stopShimmerLoading()
                    }

                    override fun onAdLoaded(typeAds: TYPE_ADS, keyPosition: String) {
                        super.onAdLoaded(typeAds, keyPosition)
                        stopShimmerLoading()
                    }
                })
        }, 1000)
    }

    private fun startShimmerLoading() {
        binding.layoutNativeAdLoaderContainer.visibility = VISIBLE
    }

    // Normally this should happen after a network request 😎
    private fun stopShimmerLoading() {
        binding.layoutNativeAdLoaderContainer.visibility = GONE
    }

    override fun onStart() {
        super.onStart()
        GlobalVariables.canShowOpenAd = true
    }

    override fun onResume() {
        super.onResume()
        mAdmManager.resumeBannerAdView()
    }

    override fun onPause() {
        super.onPause()
        mAdmManager.pauseBannerAdView()
    }

    override fun onDestroy() {
        mAdmManager.destroyAdByKeyPosition(TYPE_ADS.BannerAd, AdKeyPosition.BannerAd_ScMain.name)
        mAdmManager.removeListener()
        super.onDestroy()
    }
}