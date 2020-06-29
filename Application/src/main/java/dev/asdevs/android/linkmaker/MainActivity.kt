/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * Provides the landing screen of this sample. There is nothing particularly interesting here. All
 * the codes related to the Direct Share feature are in [].
 */
class MainActivity : Activity() {
    private lateinit var prefName: String
    private lateinit var sharedPref: SharedPreferences

    private lateinit var mEditBody: EditText
    private lateinit var mEditAffiliateId: EditText
    private var myAffid: String = ""
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        mEditBody = findViewById(R.id.body)
        mEditAffiliateId = findViewById(R.id.affiliateId)

        prefName = getString(R.string.affiliate_id)
        sharedPref = getSharedPreferences(prefName, Context.MODE_PRIVATE)

        findViewById<View>(R.id.share).setOnClickListener(mOnClickListener)
        findViewById<View>(R.id.clrbtn1).setOnClickListener(mOnClickClear)
        mAdView = findViewById(R.id.adViewPage1)
        MobileAds.initialize(this, "ca-app-pub-3774806131455333~4164055925")
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // Get Affiliate id from Shared Pref
        if (sharedPref.contains(prefName)) {
            myAffid = sharedPref.getString(prefName, "").toString()
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
                    saveAffiliateId()
                    genrateLink()
                }
            } else {
                Toast.makeText(this@MainActivity, "This Field Can't be Empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Link genrator Function
     */
    private fun genrateLink() {
        val newLink: String
        if (mEditBody.text.toString().contains("http://dl.flipkart.com/dl/") || mEditBody.text.toString().contains("https://www.flipkart.com/")) {
            if (mEditBody.text.toString().contains("&cmpid=product.share.pp")) {
                newLink = mEditBody.text.toString().replace("&cmpid=product.share.pp", "")
                mEditBody.setText(newLink)
                if (mEditAffiliateId.text.isNotEmpty()) {
                    mEditBody.text.append("&affid=").append(mEditAffiliateId.text.toString())
                    share()
                } else {
                    mEditBody.text.append("&").append(myAffid)
                    share()
                }
            } else {
                newLink = mEditBody.text.toString().replace("https://www.flipkart.com/", "http://dl.flipkart.com/dl/")
                mEditBody.setText(newLink)
                if (mEditBody.text.toString().contains("?")) {
                    if (mEditAffiliateId.text.isNotEmpty()) {
                        mEditBody.text.append("&affid=").append(mEditAffiliateId.text.toString())
                        share()
                    } else {
                        mEditBody.text.append("&").append(myAffid)
                        share()
                    }
                } else {
                    if (mEditAffiliateId.text.isNotEmpty()) {
                        mEditBody.text.append("?affid=").append(mEditAffiliateId.text.toString())
                        share()
                    } else {
                        mEditBody.text.append("?").append(myAffid)
                        share()
                    }
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "It's not a valid Flipkart URL", Toast.LENGTH_LONG).show()
            mEditBody.text.clear()
        }
    }

    /**
     * Emits a sample share [Intent].
     */
    private fun share() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, mEditBody.text.toString())
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.send_intent_title)))
    }

    private fun saveAffiliateId() {
        if (mEditAffiliateId.text.isNotEmpty() && mEditAffiliateId.text.isNotBlank()) {
            val editor = sharedPref.edit()
            editor.putString(prefName, mEditAffiliateId.text.toString())
            editor.apply()
        }
    }
}