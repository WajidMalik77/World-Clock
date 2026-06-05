package com.worldclock.app_themes.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.MenuAdapter
import com.worldclock.app_themes.databinding.ActivityLanguagesBinding
import com.worldclock.app_themes.databinding.ActivityMenuBinding
import com.worldclock.app_themes.utils.getMenuData
import com.worldclock.app_themes.utils.moreApps
import com.worldclock.app_themes.utils.openPrivacyPolicy
import com.worldclock.app_themes.utils.rateApp
import com.worldclock.app_themes.utils.sendFeedback
import com.worldclock.app_themes.utils.shareApp

class MenuActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMenuBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.toolbar.back.setOnClickListener {
            finish()
        }
        binding.toolbar.title.text = getString(R.string.settings1)

        binding.pro.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }
        binding.recycler.adapter = MenuAdapter(getMenuData()) {
            when (it) {
                0 -> startActivity(Intent(this, LanguagesActivity::class.java))
                1 -> rateApp(this)
                2 -> sendFeedback(this)
//                3 -> moreApps(this)
                3 -> shareApp(this)
                4 -> openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-_terms-conditions/")
                5 -> openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-privacy-policy/")
            }
        }

    }
}