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
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.net.URLDecoder


/**
 * Provides the landing screen of this sample. There is nothing particularly interesting here. All
 * the codes related to the Direct Share feature are in [].
 */
class MainActivity : AppCompatActivity() {
    private lateinit var prefName: String
    private lateinit var sharedPref: SharedPreferences

    private lateinit var mEditBody: EditText
    private lateinit var mEditAffiliateId: EditText
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
        mEditAffiliateId = findViewById(R.id.affiliateId)

        prefName = getString(R.string.affiliate_id)
        sharedPref = getSharedPreferences(prefName, Context.MODE_PRIVATE)

        // Short Link API Call Initialization
        shortLinkService = RetroFitClient.getRetrofitInstance().create(RetroFitClient.ShortLink::class.java)

        findViewById<View>(R.id.saveAffid).setOnClickListener(saveAffiliateId)
        findViewById<View>(R.id.share).setOnClickListener(mOnClickListener)
        findViewById<View>(R.id.clrbtn1).setOnClickListener(mOnClickClear)

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
                } else {
                    Toast.makeText(applicationContext, "Invalid Link! Try a valid flipkart link.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val mOnClickClear = View.OnClickListener {
        mEditBody.text.clear()
        mEditAffiliateId.text.clear()
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

    /**
     * Link genrator Function
     */
    private fun generateAffiliateLinkForFlipkart() {
        val onlyText = mEditBody.text.toString().split(Regex("(http|https|ftp|ftps)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(\\S*)?"))
        val theURL = mEditBody.text.toString().removePrefix(onlyText[0])
        val newLink: String
        if (theURL.contains("http://dl.flipkart.com/dl/") || theURL.contains("https://www.flipkart.com/")) {
            if (theURL.endsWith("&cmpid=product.share.pp")) {
                val afterReplaceLink = theURL.removeSuffix("&cmpid=product.share.pp")
                if (mEditAffiliateId.text.isNotBlank()) {
                    newLink = afterReplaceLink.plus("&affid=" + mEditAffiliateId.text.toString())
                    createShortLink(newLink/*, onlyText[0]*/)
                } else {
                    newLink = afterReplaceLink.plus("&affid=" + myAffid)
                    createShortLink(newLink/*, onlyText[0]*/)
                }
            } else {
                val afterReplaceLink = theURL.replace("https://www.flipkart.com/", "http://dl.flipkart.com/dl/")
                if (afterReplaceLink.contains("?")) {
                    if (mEditAffiliateId.text.isNotBlank()) {
                        newLink = afterReplaceLink.plus("&affid=" + mEditAffiliateId.text.toString())
                        createShortLink(newLink/*, onlyText[0]*/)
                    } else {
                        newLink = afterReplaceLink.plus("&affid=" + myAffid)
                        createShortLink(newLink/*, onlyText[0]*/)
                    }
                } else {
                    if (mEditAffiliateId.text.isNotBlank()) {
                        newLink = afterReplaceLink.plus("?affid=" + mEditAffiliateId.text.toString())
                        createShortLink(newLink/*, onlyText[0]*/)
                    } else {
                        newLink = afterReplaceLink.plus("?affid=" + myAffid)
                        createShortLink(newLink/*, onlyText[0]*/)
                    }
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "It's not a valid Flipkart URL", Toast.LENGTH_LONG).show()
            mEditBody.text.clear()
        }
    }

    /**
     * Before Share Create Short Link
     */
    private fun createShortLink(newLink: String, linkTitle: String) {
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(newLink)
            domainUriPrefix = "https://asdevs.dev/afflink"
            androidParameters {
                minimumVersion = 6
            }
            socialMetaTagParameters {
                title = linkTitle
                description = "Short Link For Affiliate Link .\nCopyright (C) 2020. AS Developers "
            }
        }
        val url = URL(URLDecoder.decode(dynamicLink.uri.toString(), "UTF-8"))
        mEditBody.setText(url.toString())
        shortenLongLink(url.toString())

    }

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
                    share(Uri.parse(shortLink))
                    //Log.i("ShortLink", "https://rel.ink/${resBody?.hashid}")
                } else {
                    Log.i("ShortLink", "Error: ${response.code()} : ${response.message()}")
                    Toast.makeText(this@MainActivity, "Short Link Generation Failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun shortenLongLink(link: String) {
        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            longLink = Uri.parse(link)
        }.addOnSuccessListener { result ->
            val shortLink = result.shortLink
            mEditBody.setText(shortLink.toString())
            share(shortLink)
        }.addOnFailureListener {
            print("Firebase Link Generation Failed")
            share(Uri.parse(link))
        }
    }

    /**
     * Emits a sample share [Intent].
     */
    private fun share(value: Uri?) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, value.toString())
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


