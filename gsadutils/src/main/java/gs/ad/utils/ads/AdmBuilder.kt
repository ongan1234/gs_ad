package gs.ad.utils.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import gs.ad.utils.utils.PreferencesManager

class AdmBuilder(
    val context: Context,
    private val keyShowOpenAd: String?,
    private val config: AdmConfig
): DefaultLifecycleObserver
{
    private lateinit var mAdmMachine: AdmMachine
    private lateinit var mAdmManager: AdmManager
    private constructor(builder: Builder) : this(builder.context, builder.keyShowOpen, builder.config){
        mAdmMachine = AdmMachine.build(builder.context){ config = builder.config}
        mAdmManager = AdmManager.build(mAdmMachine) {}
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "owner onStart")
        val keyShowOpenAd = keyShowOpenAd ?: return
        mAdmManager.showOpenAd(keyShowOpenAd)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(TAG, "owner onCreate")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "owner onDestroy")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "owner onStop")
        mAdmManager.resetInitUMP()
    }

    fun getActivity(activity: Activity): AdmManager{
        mAdmMachine.setActivity(activity)
        return mAdmManager
    }

    fun resetCounterAds(keyCount: String){
        PreferencesManager.getInstance().resetCounterAds(keyCount)
    }

    companion object {
        const val TAG = "AdmManager"
        inline fun build(context: Context, block: Builder.() -> Unit) = Builder(context).apply(block).build()
    }

    class Builder(
        val context: Context
    ) {
        var keyShowOpen: String? = null
        lateinit var config: AdmConfig
        fun build() = AdmBuilder(this)
    }
}