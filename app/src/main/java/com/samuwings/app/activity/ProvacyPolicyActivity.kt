package com.samuwings.app.activity

import android.view.View
import android.webkit.WebViewClient
import com.samuwings.app.R
import com.samuwings.app.api.ApiClient
import com.samuwings.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_privacy_policy.*

class ProvacyPolicyActivity:BaseActivity() {
    override fun setLayout(): Int = R.layout.activity_privacy_policy

    override fun InitView() {
        ivBack.setOnClickListener {
            finish()
        }
        webView.setWebViewClient(WebViewClient())
        webView.getSettings().setLoadsImagesAutomatically(true)
        webView.getSettings().setJavaScriptEnabled(true)
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        webView.loadUrl(ApiClient.PrivicyPolicy)
    }
}