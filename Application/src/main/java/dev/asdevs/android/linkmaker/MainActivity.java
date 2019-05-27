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

package dev.asdevs.android.linkmaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toolbar;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
/**
 * Provides the landing screen of this sample. There is nothing particularly interesting here. All
 * the codes related to the Direct Share feature are in {@link SampleChooserTargetService}.
 */
public class MainActivity extends Activity {

    private EditText mEditBody;
    public EditText mEditAffiliateId;
    private String myCode = "affid=svchost96";
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setActionBar((Toolbar) findViewById(R.id.toolbar));
        mEditBody = (EditText) findViewById(R.id.body);
        mEditAffiliateId = (EditText) findViewById(R.id.affiliateId);
        findViewById(R.id.share).setOnClickListener(mOnClickListener);
        findViewById(R.id.clrbtn1).setOnClickListener(mOnClickClear);
        mAdView = findViewById(R.id.adViewPage1);
        MobileAds.initialize(this,"ca-app-pub-3774806131455333~4164055925");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private View.OnClickListener mOnClickClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mEditBody.getText().clear();
            mEditAffiliateId.getText().clear();

        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.share:
                    if(mEditBody.getText().length() >0){
                        if(mEditBody.getText().toString().contains("affid=")){
                            Toast.makeText(MainActivity.this, "It's Already an Affiliate Link", Toast.LENGTH_LONG).show();
                            mEditBody.getText().clear();
                            break;
                        }else {
                            genrateLink();
                            break;
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "This Field Can't be Empty", Toast.LENGTH_LONG).show();
                        break;
                    }


            }
        }
    };

    /**
     * Link genrator Function
     */
    private void genrateLink() {
        String newLink;
        if( mEditBody.getText().toString().contains("http://dl.flipkart.com/dl/") || mEditBody.getText().toString().contains("https://www.flipkart.com/") ){
            if (mEditBody.getText().toString().contains("&cmpid=product.share.pp")){
                newLink = mEditBody.getText().toString().replace("&cmpid=product.share.pp", "");
                mEditBody.setText(newLink);
                if(mEditAffiliateId.getText().length() > 0) {
                    mEditBody.getText().append("&affid=" + mEditAffiliateId.getText().toString());
                    share();
                }else{
                    mEditBody.getText().append("&" + myCode);
                    share();
                }
            }else{
                newLink = mEditBody.getText().toString().replace("https://www.flipkart.com/", "http://dl.flipkart.com/dl/");
                mEditBody.setText(newLink);
                if(mEditBody.getText().toString().contains("?")){
                    if(mEditAffiliateId.getText().length() > 0) {
                        mEditBody.getText().append("&affid=" + mEditAffiliateId.getText().toString());
                        share();
                    }else{
                        mEditBody.getText().append("&" + myCode);
                        share();
                    }

                }else{
                    if(mEditAffiliateId.getText().length() > 0) {
                        mEditBody.getText().append("?affid=" + mEditAffiliateId.getText().toString());
                        share();
                    }else{
                        mEditBody.getText().append("?" + myCode);
                        share();
                    }
                }
            }

        }else {
            Toast.makeText(MainActivity.this, "It's not a valid Flipkart URL", Toast.LENGTH_LONG).show();
            mEditBody.getText().clear();
        }
    }

    /**
     * Emits a sample share {@link Intent}.
     */
    private void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mEditBody.getText().toString());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.send_intent_title)));
    }

}
