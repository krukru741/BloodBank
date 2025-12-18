package com.example.bloodbank.Email

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.bloodbank.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * EmailSender - Modern Kotlin implementation for sending emails.
 * Replaces deprecated AsyncTask with Coroutines.
 * 
 * Usage:
 * ```
 * lifecycleScope.launch {
 *     val sender = EmailSender(context)
 *     val success = sender.sendEmail(
 *         to = "recipient@example.com",
 *         subject = "Blood Donation Request",
 *         message = "We need your help..."
 *     )
 *     if (success) {
 *         // Email sent successfully
 *     }
 * }
 * ```
 */
class EmailSender(private val context: Context) {
    
    /**
     * Send email using JavaMail API with Kotlin Coroutines.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param message Email body
     * @param onProgress Optional callback for progress updates
     * @return true if email sent successfully, false otherwise
     */
    suspend fun sendEmail(
        to: String,
        subject: String,
        message: String,
        onProgress: ((String) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke("Configuring email session...")
            
            // Configure SMTP properties
            val properties = Properties().apply {
                put("mail.smtp.host", EmailConfig.SMTP_HOST)
                put("mail.smtp.socketFactory.port", EmailConfig.SMTP_SOCKET_FACTORY_PORT)
                put("mail.smtp.socketFactory.class", EmailConfig.SMTP_SOCKET_FACTORY_CLASS)
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", EmailConfig.SMTP_PORT)
                put("mail.smtp.ssl.enable", "true")
            }
            
            // Create session with authentication
            val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(EmailConfig.EMAIL, EmailConfig.PASSWORD)
                }
            })
            
            onProgress?.invoke("Composing email...")
            
            // Create and send message
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(EmailConfig.EMAIL))
                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject)
                setText(message)
            }
            
            onProgress?.invoke("Sending email...")
            Transport.send(mimeMessage)
            
            onProgress?.invoke("Email sent successfully!")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress?.invoke("Failed to send email: ${e.message}")
            false
        }
    }
    
    /**
     * Show success dialog after email is sent.
     */
    suspend fun showSuccessDialog() = withContext(Dispatchers.Main) {
        AlertDialog.Builder(context)
            .setView(R.layout.output_layout)
            .setCancelable(false)
            .create()
            .apply {
                show()
                findViewById<android.widget.Button>(R.id.closeButton)?.setOnClickListener {
                    dismiss()
                }
            }
    }
}

/**
 * Extension function for easy email sending from Activities/Fragments.
 * 
 * Usage in Activity:
 * ```
 * lifecycleScope.launch {
 *     sendEmailWithProgress(
 *         to = "donor@example.com",
 *         subject = "Blood Donation Request",
 *         message = "We urgently need your blood type..."
 *     )
 * }
 * ```
 */
suspend fun Context.sendEmailWithProgress(
    to: String,
    subject: String,
    message: String
): Boolean {
    val sender = EmailSender(this)
    
    // Show progress on main thread
    withContext(Dispatchers.Main) {
        // You can show a ProgressDialog or SnackBar here
    }
    
    val success = sender.sendEmail(to, subject, message)
    
    if (success) {
        sender.showSuccessDialog()
    }
    
    return success
}
