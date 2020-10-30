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
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
    private lateinit var pref2Name: String
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPref2: SharedPreferences

    private lateinit var mEditBody: EditText
    private lateinit var mEdit2gud: EditText
    private lateinit var mEditAmazon: EditText
    private lateinit var mEditAffiliateId: EditText
    private lateinit var mEditAmazonTrackingId: EditText
    private lateinit var check1: CheckBox
    private lateinit var check2: CheckBox
    private lateinit var check3: CheckBox
    private enum class AFF{
        FLIP, ToGud, Amazon
    }
    private var myAffid: String = "svchost96"
    private var myTrackingId: String = "tricks4everyo-21"
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
        mEditAmazon = findViewById(R.id.body3)
        mEditAffiliateId = findViewById(R.id.affiliateId)
        mEditAmazonTrackingId = findViewById(R.id.affiliateId2)
        //CheckBox changes
        check1 = findViewById<CheckBox>(R.id.check1)
        check1.setOnCheckedChangeListener { _, isChecked -> if (isChecked) Toast.makeText(this@MainActivity, "Short Link Activated", Toast.LENGTH_SHORT).show() else Toast.makeText(this@MainActivity, "Short Link Deactivated", Toast.LENGTH_SHORT).show()  }
        check2 = findViewById<CheckBox>(R.id.check2)
        check2.setOnCheckedChangeListener { _, isChecked -> if (isChecked) Toast.makeText(this@MainActivity, "Short Link Activated", Toast.LENGTH_SHORT).show() else Toast.makeText(this@MainActivity, "Short Link Deactivated", Toast.LENGTH_SHORT).show()  }
        check3 = findViewById<CheckBox>(R.id.check3)
        check3.setOnCheckedChangeListener { _, isChecked -> if (isChecked) Toast.makeText(this@MainActivity, "Short Link Activated", Toast.LENGTH_SHORT).show() else Toast.makeText(this@MainActivity, "Short Link Deactivated", Toast.LENGTH_SHORT).show()  }

        //Pref shared
        prefName = getString(R.string.affiliate_id)
        pref2Name = getString(R.string.amazon_id)
        sharedPref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        sharedPref2 = getSharedPreferences(pref2Name, Context.MODE_PRIVATE)

        // Short Link API Call Initialization
        shortLinkService = RetroFitClient.getRetrofitInstance().create(RetroFitClient.ShortLink::class.java)

        findViewById<View>(R.id.saveAffid).setOnClickListener(saveAffiliateId)
        findViewById<View>(R.id.saveAffid2).setOnClickListener(saveAffiliateId2)
        findViewById<View>(R.id.share).setOnClickListener(mOnClickListener)
        findViewById<View>(R.id.clrbtn1).setOnClickListener(mOnClickClear)
        findViewById<View>(R.id.clrbtn2).setOnClickListener(mOnClickClear2)
        findViewById<View>(R.id.clrbtn3).setOnClickListener(mOnClickClear3)
        findViewById<View>(R.id.share2).setOnClickListener(mOnClickListener2)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adViewPage1)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // Get Affiliate id from Shared Pref
        if (sharedPref.contains(prefName)) {
            myAffid = sharedPref.getString(prefName, myAffid).toString()
            mEditAffiliateId.setText(myAffid)
        }
        else if (sharedPref2.contains(pref2Name)) {
            myTrackingId = sharedPref2.getString(pref2Name, myTrackingId).toString()
            mEditAmazonTrackingId.setText(myTrackingId)
        }

        // Get value from intent if available
        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction == Intent.ACTION_SEND) {
            if (receivedType!!.startsWith("text/")) {
                val receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT)
                if (receivedText!!.contains("flipkart.com")) mEditBody.setText(receivedText)
                else if(receivedText.contains("2gud.com")) mEdit2gud.setText(receivedText)
                else if(receivedText.contains("amazon")) mEditAmazon.setText(receivedText)
                if (sharedPref.contains(prefName)) {
                    myAffid = sharedPref.getString(prefName, myAffid).toString()
                    mEditAffiliateId.setText(myAffid)
                }else if(sharedPref2.contains((pref2Name))){
                    myTrackingId = sharedPref2.getString(pref2Name, myTrackingId).toString()
                    mEditAmazonTrackingId.setText(myTrackingId)
                }
            }
        }
    }

    private val mOnClickClear = View.OnClickListener {
        mEditBody.text.clear()
    }
    private val mOnClickClear2 = View.OnClickListener {
        mEdit2gud.text.clear()
    }
    private val mOnClickClear3 = View.OnClickListener {
        mEditAmazon.text.clear()
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

        if (theURL.contains("dl.flipkart.com/dl/") || theURL.contains("www.flipkart.com/")) {
            if (theURL.endsWith("&cmpid=product.share.pp")) {
                val afterReplaceLink = theURL.removeSuffix("&cmpid=product.share.pp")
                setAffid(afterReplaceLink, AFF.FLIP)
            } else {
                val afterReplaceLink = theURL.replace("https://www.flipkart.com/", "https://dl.flipkart.com/dl/")
                setAffid(afterReplaceLink, AFF.FLIP)
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

        if (theURL.contains("dl.2gud.com/dl/") || theURL.contains("www.2gud.com/")) {
            if (theURL.endsWith("&cmpid=product.share.pp")) {
                val afterReplaceLink = theURL.removeSuffix("&cmpid=product.share.pp")
                setAffid(afterReplaceLink, AFF.ToGud)
            } else {
                setAffid(theURL, AFF.ToGud)
            }
        } else {
            Toast.makeText(this@MainActivity, "It's not a valid 2Gud URL", Toast.LENGTH_LONG).show()
            mEdit2gud.text.clear()
        }
    }

    /**
     * Myaffiliate id or other checking
     */
    private fun setAffid(afterReplaceLink: String, source: AFF) {
        val newLink: String
        if (afterReplaceLink.contains("?")){
            newLink = afterReplaceLink.plus("&affid=" + if (mEditAffiliateId.text.isNotBlank()) mEditAffiliateId.text.toString() else myAffid)
            if (source === AFF.FLIP)
                when (check1.isChecked) {
                    true -> createShortLink(newLink, AFF.FLIP)
                    false -> share(newLink)
                }
            else
                when (check2.isChecked) {
                    true -> createShortLink(newLink, AFF.ToGud)
                    false -> share(newLink)
                }

        }else{
            newLink = afterReplaceLink.plus("?affid=" + if (mEditAffiliateId.text.isNotBlank()) mEditAffiliateId.text.toString() else myAffid)
            if (source === AFF.FLIP)
                when (check1.isChecked) {
                    true -> createShortLink(newLink, AFF.FLIP)
                    false -> share(newLink)
                }
            else
            when(check2.isChecked){
                true -> createShortLink(newLink, AFF.ToGud)
                false -> share(newLink)
            }
        }
    }

    /**
     * Before Share Create Short Link
     */

    private fun createShortLink(link: String, source: AFF) {
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
                    if (source === AFF.FLIP) mEditBody.setText(resBody?.result_url) else mEdit2gud.setText(resBody?.result_url)
                    if (resBody != null) {
                        share(resBody.result_url)
                    }
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
    private val saveAffiliateId2 = View.OnClickListener {
        if (mEditAmazonTrackingId.text.isNotEmpty() && mEditAmazonTrackingId.text.isNotBlank()) {
            val editor = sharedPref2.edit()
            editor.putString(pref2Name, mEditAmazonTrackingId.text.toString())
            editor.apply()
        }
    }
}


