package gs.ad.gsadsexample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.databinding.ActivitySplashBinding
import gs.ad.utils.ads.AdmManager
import gs.ad.utils.ads.OnAdmListener
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.GlobalVariables
import gs.ad.utils.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity(), OnAdmListener {
    private lateinit var binding: ActivitySplashBinding
    private val mAdmManager: AdmManager get() { return (application as AppOwner).mAdmBuilder.getActivity(this)}

    private var countLoadAd = 0
    private var totalLoadAd = 0
        set(value) {
            maxProgress = value
            field = value
        }
    private var maxProgress : Int = 0
        set(value) {
            field = value * 100 + 1000
        }

    private data class AdPreload(
        val typeAds: TYPE_ADS,
        val id : Int,
        val position : String,
        val isFullScreen: Boolean = false
    )

    private var adPosition: MutableList<AdPreload> = mutableListOf(
        AdPreload(TYPE_ADS.OpenAd, 0, ""),
        AdPreload(TYPE_ADS.InterstitialAd, 0, ""),
        AdPreload(TYPE_ADS.NativeAd, 0, AdKeyPosition.NativeAd_ScOnBoard_1.name),
        AdPreload(TYPE_ADS.NativeAd, 0, AdKeyPosition.NativeAd_ScOnBoard_2.name),
        AdPreload(TYPE_ADS.NativeAd, 0, AdKeyPosition.NativeAd_ScOnBoard_3.name, true),
        AdPreload(TYPE_ADS.NativeAd, 0, AdKeyPosition.NativeAd_ScOnBoard_4.name),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        PreferencesManager.getInstance().saveShowOnBoard(false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        GlobalVariables.canShowOpenAd = false

        mAdmManager.initUMP(gatherConsentFinished = {
            loadProgress()
        })

        mAdmManager.setListener(this)

        binding.splashProcessBar.visibility = View.GONE
        binding.splashProcessBar.progress = 0
    }

    override fun onDestroy() {
        mAdmManager.removeListener()
        super.onDestroy()
    }

    private fun loadProgress(){
        val progressBar = binding.splashProcessBar
        progressBar.visibility = View.VISIBLE

        if (!PreferencesManager.getInstance().isSUB() ) {
            if(PreferencesManager.getInstance().isShowOnBoard()) adPosition.removeAll{it.typeAds == TYPE_ADS.NativeAd}

            var countLoadedAd = 0
            var countProgress = 0
            totalLoadAd = adPosition.count()
            binding.splashProcessBar.max = maxProgress

            adPosition.forEach {
                when(it.typeAds){
                    TYPE_ADS.NativeAd -> mAdmManager.preloadNativeAd(it.id, it.position, it.isFullScreen)
                    TYPE_ADS.BannerAd -> TODO()
                    TYPE_ADS.OpenAd -> mAdmManager.preloadOpenAd()
                    TYPE_ADS.InterstitialAd -> mAdmManager.preloadInterstitialAd()
                    TYPE_ADS.RewardAd -> TODO()
                }
            }

            lifecycleScope.launch(Dispatchers.Main) {
                val progressJob = async {
                    while (countProgress < maxProgress) {
                        countProgress += 1
                        progressBar.progress = countProgress
                        delay(1)
                    }
                }

                val loadAdsJob = async(Dispatchers.IO)  {
                    while (getCountLoadAd() < totalLoadAd){
                        if(countLoadedAd != getCountLoadAd()){
                            countLoadedAd = getCountLoadAd()
                            countProgress+=100
                            progressBar.progress = countProgress
                        }
                    }
                }

                progressJob.await()
                loadAdsJob.await()

                startMainActivity()
            }
        } else {
            startMainActivity()
        }
    }

    private suspend fun getCountLoadAd(): Int = withContext(Dispatchers.IO){
        return@withContext countLoadAd
    }

    override fun onAdLoaded(typeAds: TYPE_ADS, keyPosition: String) {
        super.onAdLoaded(typeAds, keyPosition)
        countLoadAd += 1
    }

    override fun onAdFailToLoaded(typeAds: TYPE_ADS, keyPosition: String) {
        super.onAdFailToLoaded(typeAds, keyPosition)
        countLoadAd += 1
    }

    private fun startMainActivity() {
        val intent =
            if (PreferencesManager.getInstance().isShowOnBoard()) Intent(this, MainActivity::class.java)
            else Intent(this, OnBoardActivity::class.java)
        MainScope().launch {
            startActivity(intent)
            finish()
        }
    }
}