package com.example.bloodbank.Email

/**
 * Email configuration utility.
 * 
 * ⚠️ SECURITY WARNING: These credentials should be moved to:
 * 1. local.properties (for development)
 * 2. Environment variables (for production)
 * 3. Firebase Remote Config (recommended)
 * 
 * Never commit real credentials to version control!
 */
object EmailConfig {
    // TODO: Move these to secure storage
    const val EMAIL = "ishiney741@gmail.com"
    const val PASSWORD = "ptzzrryjpgblwjej"
    
    // SMTP Configuration
    const val SMTP_HOST = "smtp.gmail.com"
    const val SMTP_PORT = "465"
    const val SMTP_SOCKET_FACTORY_PORT = "465"
    const val SMTP_SOCKET_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory"
}
