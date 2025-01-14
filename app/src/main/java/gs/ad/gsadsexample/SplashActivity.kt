package gs.ad.gsadsexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.databinding.ActivitySplashBinding
import gs.ad.utils.ads.AdmManager
import gs.ad.utils.ads.OnAdmListener
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.google_iab.BillingClientLifecycle
import gs.ad.utils.google_iab.OnBillingListener
import gs.ad.utils.google_iab.enums.ErrorType
import gs.ad.utils.google_iab.models.ProductInfo
import gs.ad.utils.google_iab.models.PurchaseInfo
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
    private val mBillingClientLifecycle: BillingClientLifecycle? get() { return  (application as AppOwner).mBillingClientLifecycle ?: null}

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
    private var isInitUMP : Boolean = false

    private data class AdPreload(
        val typeAds: TYPE_ADS,
        val id : Int,
        val position : String,
        val isFullScreen: Boolean = false
    )

    private var adPosition: MutableList<AdPreload> = mutableListOf(
        AdPreload(TYPE_ADS.OpenAd, -1, ""),
        AdPreload(TYPE_ADS.InterstitialAd, -1, ""),
        AdPreload(TYPE_ADS.NativeAd, -1, AdKeyPosition.NativeAd_ScOnBoard_1.name),
        AdPreload(TYPE_ADS.NativeAd, -1, AdKeyPosition.NativeAd_ScOnBoard_2.name),
        AdPreload(TYPE_ADS.NativeAd, -1, AdKeyPosition.NativeAd_ScOnBoard_3.name, true),
        AdPreload(TYPE_ADS.NativeAd, -1, AdKeyPosition.NativeAd_ScOnBoard_4.name),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //reset onboard
        //PreferencesManager.getInstance().saveShowOnBoard(false)

        //reset sub
        //PreferencesManager.getInstance().purchaseFailed()

        GlobalVariables.canShowOpenAd = false
        binding.splashProcessBar.visibility = View.GONE
        binding.splashProcessBar.progress = 0

        mAdmManager.setListener(this@SplashActivity)

        if (mBillingClientLifecycle == null){
            mAdmManager.initUMP(gatherConsentFinished = {
                loadProgress()
            })
        }else{
            isInitUMP = false
            mBillingClientLifecycle?.setListener(this, eventListener = object: OnBillingListener {
                override fun onProductDetailsFetched(productInfos: HashMap<String, ProductInfo>) {
                    super.onProductDetailsFetched(productInfos)
                    mBillingClientLifecycle?.fetchSubPurchasedProducts()
                }
                override fun onPurchasedProductsFetched(purchaseInfos: List<PurchaseInfo>) {
                    super.onPurchasedProductsFetched(purchaseInfos)
                    if(isInitUMP) return
                    isInitUMP = true
                    mAdmManager.initUMP(gatherConsentFinished = {
                        loadProgress()
                    })
                }

                override fun onBillingError(errorType: ErrorType) {
                    super.onBillingError(errorType)
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        mBillingClientLifecycle?.fetchSubPurchasedProducts()
    }

    private fun loadProgress(){
        if (!PreferencesManager.getInstance().isSUB() ) {
            val progressBar = binding.splashProcessBar
            progressBar.visibility = View.VISIBLE
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
            mAdmManager.removeListener()
            mBillingClientLifecycle?.removeListener(this@SplashActivity)
            finish()
        }
    }
}