package com.example.sw0b_001.Models.PlatformComposers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.EncryptedContent.EncryptedContent
import com.example.sw0b_001.Models.EncryptedContent.MessagesViewModel
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms._PlatformsHandler
import com.example.sw0b_001.Models.PublisherHandler
import com.example.sw0b_001.Models.SMSHandler
import com.example.sw0b_001.Models.ThreadExecutorPool
import com.example.sw0b_001.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class EmailComposeModalFragment(val platform: Platforms)
    : BottomSheetDialogFragment(R.layout.fragment_modal_email_compose) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when(it.resultCode) {
                    Activity.RESULT_OK -> {}
                    else -> { }
                }
            }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.email_compose_toolbar)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.email_compose_menu_action_send -> {
                    processSend(view)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }

        val bottomSheet = view.findViewById<View>(R.id.email_compose_constraint)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = true

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    private fun processSend(view: View) {
        val toEditText = view.findViewById<TextInputEditText>(R.id.email_to)
        val ccTextInputEditText = view.findViewById<TextInputEditText>(R.id.email_cc)
        val bccTextInputEditText = view.findViewById<TextInputEditText>(R.id.email_bcc)
        val subjectTextInputEditText = view.findViewById<TextInputEditText>(R.id.email_subject)
        val bodyTextInputEditText = view.findViewById<EditText>(R.id.email_compose_body_input)

        if (toEditText.text.isNullOrEmpty()) {
            toEditText.error = getString(R.string.message_compose_empty_recipient)
            return
        }
        if (bodyTextInputEditText.text.isNullOrEmpty()) {
            bodyTextInputEditText.error = getString(R.string.message_compose_empty_body)
            return
        }


        val to = toEditText.text.toString()
        val cc = ccTextInputEditText.text.toString()
        val bcc = bccTextInputEditText.text.toString()
        val subject = subjectTextInputEditText.text.toString()
        val body = bodyTextInputEditText.text.toString()

        val platforms = _PlatformsHandler.getPlatform(view.context, platform.id)
        val formattedContent = processEmailForEncryption(platforms.letter, to, cc, bcc, subject, body)

        val encryptedContentBase64 = PublisherHandler
                .formatForPublishing(view.context, formattedContent)
        val gatewayClientMSISDN = GatewayClientsCommunications(view.context)
                .getDefaultGatewayClient()

        try {
            val sentIntent = SMSHandler.transferToDefaultSMSApp(requireContext(),
                    gatewayClientMSISDN!!, encryptedContentBase64).apply {
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityLauncher.launch(sentIntent)
            val encryptedContent = EncryptedContent()
            encryptedContent.encryptedContent = formattedContent
            encryptedContent.date = System.currentTimeMillis()
            encryptedContent.type = platforms.type
            encryptedContent.platformId = platforms.id
            encryptedContent.platformName = platforms.name

            ThreadExecutorPool.executorService.execute {
                Datastore.getDatastore(requireContext()).encryptedContentDAO()
                        .insert(encryptedContent)
                dismiss()
            }
        } catch(e: Exception) {
            Log.e(javaClass.name, "Exception finding package", e)
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun processEmailForEncryption(platformLetter: String,
                                          to: String,
                                          cc: String,
                                          bcc: String,
                                          subject: String,
                                          body: String): String {
        return "$platformLetter:$to:$cc:$bcc:$subject:$body"
    }
}