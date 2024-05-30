package com.example.sw0b_001

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Modals.PlatformComposers.EmailComposeModalFragment
import com.example.sw0b_001.Modals.PlatformComposers.TextComposeModalFragment
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.ThreadExecutorPool
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

class TextViewActivity : AppCompactActivityCustomized() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view)

        val myToolbar = findViewById<MaterialToolbar>(R.id.layout_text_toolbar)
        setSupportActionBar(myToolbar)
        supportActionBar?.apply {
            this.title = intent.getStringExtra("platform_name")
            setDisplayHomeAsUpEnabled(true)
        }

        configureView()
    }

    private fun configureView() {
        ThreadExecutorPool.executorService.execute {
            val message = Datastore.getDatastore(applicationContext).encryptedContentDAO()
                .get(intent.getLongExtra("message_id", -1))
            println(message.encryptedContent)
            runOnUiThread {
                message.encryptedContent.split(":").let {
//                    findViewById<MaterialTextView>(R.id.layout_identity_header_title).apply {
//                        text = "<${it[1]}>"
//                    }
//                    findViewById<MaterialTextView>(R.id.layout_identity_header_subject).apply {
//                        text = "${getString(R.string.email_compose_cc)}: ${it[2]}\n" +
//                                "${getString(R.string.email_compose_bcc)}: ${it[3]}"
//                    }
                    findViewById<MaterialTextView>(R.id.layout_text_body).apply {
                        text = it.subList(1, it.size).joinToString()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose_view_edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.compose_view_edit_menu_edit -> {
                showPlatformsModal(Platforms().apply {
                    id = intent.getLongExtra("platform_id", -1)
                })
                return true
            }
        }
        return false
    }

    private fun showPlatformsModal(platforms: Platforms) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val textComposeModalFragment = TextComposeModalFragment(platforms)
        fragmentTransaction.add(textComposeModalFragment, "text_compose_tag")
        fragmentTransaction.show(textComposeModalFragment)
        fragmentTransaction.commitNow()
    }
}