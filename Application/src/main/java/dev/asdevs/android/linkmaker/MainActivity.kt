/*
 * Copyright (C) 2020 The AS Developers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.asdevs.android.linkmaker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Provides the landing screen of this sample. There is nothing particularly interesting here. All
 * the codes related to the Direct Share feature are in [].
 */
class MainActivity : AppCompatActivity() {
    private lateinit var prefName: String
    private lateinit var sharedPref: SharedPreferences

    private lateinit var mEditBody: EditText
    private lateinit var mEdit2gud: EditText
    private lateinit var mEditAffiliateId: EditText
    private lateinit var check1: CheckBox
    private lateinit var check2: CheckBox
    private var sLink: Boolean = true
    private var myAffid: String = "svchost96"
    private lateinit var mAdView: AdView

    private lateinit var shortLinkService: RetroFitClient.ShortLink

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        mEditBody = findViewById(R.id.body)
        mEdit2gud = findViewById(R.id.body2)
        mEditAffiliateId = findViewById(R.id.affiliateId)
        //CheckBox init
        check1 = findViewById(R.id.check1)
        check2 = findViewById(R.id.check2)
        check1.isChecked = true
        check2.isChecked = false
        //Pref shared
        prefName = getString(R.string.affiliate_id)
        sharedPref = getSharedPreferences(prefName, Context.MODE_PRIVATE)

        // Short Link API Call Initialization
        shortLinkService = RetroFitClient.getRetrofitInstance().create(RetroFitClient.ShortLink::class.java)

        findViewById<View>(R.id.saveAffid).setOnClickListener(saveAffiliateId)
        findViewById<View>(R.id.share).setOnClickListener(mOnClickListener)
        findViewById<View>(R.id.clrbtn1).setOnClickListener(mOnClickClear)
        findViewById<View>(R.id.clrbtn2).setOnClickListener(mOnClickClear2)
        findViewById<View>(R.id.share2).setOnClickListener(mOnClickListener2)
        findViewById<View>(R.id.check1).setOnClickListener(mOnCheck)
        findViewById<View>(R.id.check2).setOnClickListener(mOnCheck2)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adViewPage1)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // Get Affiliate id from Shared Pref
        if (sharedPref.contains(prefName)) {
            myAffid = sharedPref.getString(prefName, myAffid).toString()
            mEditAffiliateId.setText(myAffid)
        }

        // Get value from intent if available
        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction == Intent.ACTION_SEND) {
            if (receivedType!!.startsWith("text/")) {
                val receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT)
                if (receivedText!!.contains("flipkart.com")) {
                    mEditBody.setText(receivedText)
                } else if(receivedText!!.contains("2gud.com")) {
                    mEdit2gud.setText(receivedText)
                }
            }
        }
    }
    private val mOnCheck = View.OnClickListener {
        sLink = check1.isChecked
    }
    private val mOnCheck2 = View.OnClickListener {
        sLink = check2.isChecked
    }
    private val mOnClickClear = View.OnClickListener {
        mEditBody.text.clear()
    }
    private val mOnClickClear2 = View.OnClickListener {
        mEdit2gud.text.clear()
    }
    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.share -> if (mEditBody.text.isNotEmpty()) {
                if (mEditBody.text.toString().contains("affid=")) {
                    Toast.makeText(this@MainActivity, "It's Already an Affiliate Link", Toast.LENGTH_LONG).show()
                    mEditBody.text.clear()
                } else {
                    generateAffiliateLinkForFlipkart()
                }
            } else {
                Toast.makeText(this@MainActivity, "This Field Can't be Empty", Toast.LENGTH_LONG).show()
            }
        }
    }
    private val mOnClickListener2 = View.OnClickListener { v ->
        when (v.id) {
            R.id.share2 -> if (mEdit2gud.text.isNotEmpty()) {
                if (mEdit2gud.text.toString().contains("affid=")) {
                    Toast.makeText(this@MainActivity, "It's Already an Affiliate Link", Toast.LENGTH_LONG).show()
                    mEdit2gud.text.clear()
                } else {
                    generateAffiliateLinkFor2Gud()
                }
            } else {
                Toast.makeText(this@MainActivity, "This Field Can't be Empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Link genrator Function
     */
    private fun generateAffiliateLinkForFlipkart() {
        val onlyText = mEditBody.text.toString().split(Regex("(http|https|ftp|ftps)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(\\S*)?"))
        val theURL = mEditBody.text.toString().removePrefix(onlyText[0])

        if (theURL.contains("http://dl.flipkart.com/dl/") || theURL.contains("https://www.flipkart.com/")) {
            if (theURL.endsWith("&cmpid=product.share.pp")) {
                val afterReplaceLink = theURL.removeSuffix("&cmpid=product.share.pp")
                setAffid(afterReplaceLink)
            } else {
                val afterReplaceLink = theURL.replace("https://www.flipkart.com/", "http://dl.flipkart.com/dl/")
                setAffid(afterReplaceLink)
            }
        } else {
            Toast.makeText(this@MainActivity, "It's not a valid Flipkart URL", Toast.LENGTH_LONG).show()
            mEditBody.text.clear()
        }
    }
    /**
     * 2GUd Link Generation
     */
    private fun generateAffiliateLinkFor2Gud() {
        val onlyText = mEdit2gud.text.toString().split(Regex("(http|https|ftp|ftps)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(\\S*)?"))
        val theURL = mEdit2gud.text.toString().removePrefix(onlyText[0])

        if (theURL.contains("http://dl.2gud.com/dl/") || theURL.contains("https://www.2gud.com/")) {
            if (theURL.endsWith("&cmpid=product.share.pp")) {
                val afterReplaceLink = theURL.removeSuffix("&cmpid=product.share.pp")
                setAffid(afterReplaceLink)
            } else {
                setAffid(theURL)
            }
        } else {
            Toast.makeText(this@MainActivity, "It's not a valid 2Gud URL", Toast.LENGTH_LONG).show()
            mEdit2gud.text.clear()
        }
    }

    /**
     * Myaffiliate id or other checking
     */
    private fun setAffid(afterReplaceLink: String) {
        val newLink: String
        if (afterReplaceLink.contains("?")) {
            if (mEditAffiliateId.text.isNotBlank()) {
                newLink = afterReplaceLink.plus("&affid=" + mEditAffiliateId.text.toString())
                when(sLink){
                    true -> createShortLink(newLink)
                    false -> share(newLink)
                }
            } else {
                newLink = afterReplaceLink.plus("&affid=" + myAffid)
                when(sLink){
                    true -> createShortLink(newLink)
                    false -> share(newLink)
                }
            }
        }else{
            if (mEditAffiliateId.text.isNotBlank()) {
                newLink = afterReplaceLink.plus("?affid=" + mEditAffiliateId.text.toString())
                when(sLink){
                    true -> createShortLink(newLink)
                    false -> share(newLink)
                }
            } else {
                newLink = afterReplaceLink.plus("?affid=" + myAffid)
                when(sLink){
                    true -> createShortLink(newLink)
                    false -> share(newLink)
                }
            }
        }
    }

    /**
     * Before Share Create Short Link
     */

    private fun createShortLink(link: String) {
        val url = ShortUrlPost(link)
        val call: Call<ShortLinkResponse> = shortLinkService.getShortLink(url)
        call.enqueue(object : Callback<ShortLinkResponse> {
            override fun onFailure(call: Call<ShortLinkResponse>, t: Throwable) {
                println(t.message)
                Toast.makeText(this@MainActivity, "Short Link Generation Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ShortLinkResponse>, response: Response<ShortLinkResponse>) {
                if (response.code() == 200 || response.code() == 201) {
                    val resBody = response.body()
                    val shortLink = "https://rel.ink/${resBody?.hashid}"
                    mEditBody.setText(shortLink)
                    share(shortLink)
                } else {
                    Log.i("ShortLink", "Error: ${response.code()} : ${response.message()}")
                    Toast.makeText(this@MainActivity, "Short Link Generation Failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Emits a sample share [Intent].
     */
    private fun share(value: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, value)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.send_intent_title)))
    }

    private val saveAffiliateId = View.OnClickListener {
        if (mEditAffiliateId.text.isNotEmpty() && mEditAffiliateId.text.isNotBlank()) {
            val editor = sharedPref.edit()
            editor.putString(prefName, mEditAffiliateId.text.toString())
            editor.apply()
        }
    }
}


