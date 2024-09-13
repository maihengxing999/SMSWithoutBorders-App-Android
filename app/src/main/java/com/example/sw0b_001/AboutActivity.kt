package com.example.sw0b_001

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar = findViewById<View>(R.id.about_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.main_navigation_about)

        // Social media icons
        val xIcon = findViewById<ImageView>(R.id.x_icon)
        val facebookIcon = findViewById<ImageView>(R.id.facebook_icon)
        val youtubeIcon = findViewById<ImageView>(R.id.youtube_icon)

        xIcon.setOnClickListener {
            openSocialLink("https://x.com/RelaySMS")
        }

        facebookIcon.setOnClickListener {
            openSocialLink("https://www.facebook.com/SMSWithoutBorders")
        }

        youtubeIcon.setOnClickListener {
            openSocialLink("https://www.youtube.com/@smswithoutborders9162")
        }

        // View on GitHub
        val viewOnGithub = findViewById<TextView>(R.id.view_on_github)
        viewOnGithub.setOnClickListener {
            openSocialLink("https://github.com/smswithoutborders/SMSWithoutBorders-App-Android")
        }

        // Tutorial button
        val tutorialButton = findViewById<Button>(R.id.tutorial_button)
        tutorialButton.setOnClickListener {
            openSocialLink("https://docs.smswithoutborders.com/docs/App%20Tutorial/New-Tutorial")
        }
    }

    private fun openSocialLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}