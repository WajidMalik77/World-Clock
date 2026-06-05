package com.worldclock.app_themes.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.LangAdapter
import com.worldclock.app_themes.databinding.ActivityLanguagesBinding
import com.worldclock.app_themes.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.utils.Constants
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.SharePref
import com.worldclock.app_themes.utils.getLangData

class LanguagesActivity : BaseActivity() {
    private val binding by lazy {
        ActivityLanguagesBinding.inflate(layoutInflater)
    }
    var pos = 0
    private var isSplash = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        pos = getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("lang", 0)

        isSplash = intent.getBooleanExtra("isSplash", false)

        if (!isSplash)
            binding.back.visibility = View.VISIBLE
        else binding.back.visibility = View.INVISIBLE
        binding.back.setOnClickListener {
            finish()
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        val adapter = LangAdapter(getLangData()) { it, it1 ->
            pos = it1

        }
        binding.recycler.adapter = adapter
        adapter.setPos(getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("lang", 0))

        binding.done.setOnClickListener {
            getSharedPreferences("MySharedPref", MODE_PRIVATE).edit { putInt("lang", pos) }
            Log.d("TAG", "onCreate: ${getLangData()[pos].locale}")

            updateLocale(getLangData()[pos].locale)
            goNext()


        }
    }
    private fun goNext() {
        Log.d("lang123", "goNext: $isSplash")
        if (isSplash) {
            if (!getSharedPreferences(
                    PrefsName,
                    Context.MODE_PRIVATE
                ).getBoolean(
                    isFirstTime,
                    false
                )
            ) {
                if (!isFinishing) {
                    startActivity(Intent(this, OnBoardingActivity::class.java))
                    finish()
                }

            } else {
                startActivity(
                    Intent(this, ActivityPurchase::class.java)
                        .putExtra("isSplash", true)
                )
                finish()
            }


        } else finish()
    }

   /* private fun goNext() {
        Log.d("lang123", "goNext: $isSplash")
        if (isSplash) {
            if (!getSharedPreferences(
                    PrefsName,
                    Context.MODE_PRIVATE
                ).getBoolean(
                    isFirstTime,
                    false
                )
            ) {

//                preLoadShowInterstitial(Islang_inter_ad_key, lang_inter_ad_key) {

//                    loadInterstitial(Ispurchase_inter_ad_key, purchase_inter_ad_key) {}
                startActivity(Intent(this, OnBoardingActivity::class.java))
                finish()
            }

//            }
        } else finish()
    }*/
}