package com.samuwings.app.activity


import android.os.Handler
import android.os.Looper
import com.samuwings.app.R
import com.samuwings.app.base.BaseActivity
import com.samuwings.app.utils.Common
import com.samuwings.app.utils.SharePreference
import com.samuwings.app.utils.SharePreference.Companion.getBooleanPref


class SplashActivity : BaseActivity(){
    override fun setLayout(): Int {
       return R.layout.activity_splash
    }

    override fun InitView() {
        Common.getCurrentLanguage(this@SplashActivity, false)
        Handler(Looper.getMainLooper()).postDelayed({
            if(!getBooleanPref(this@SplashActivity,SharePreference.isTutorial)){
                openActivity(TutorialActivity::class.java)
                finish()
            }else{
                openActivity(DashboardActivity::class.java)
                finish()
            }
        },3000)
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@SplashActivity, false)
    }
}