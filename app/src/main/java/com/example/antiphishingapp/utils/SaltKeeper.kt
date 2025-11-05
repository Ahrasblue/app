package com.example.antiphishingapp.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

object SaltKeeper {

    private const val PREFS = "secure_prefs"
    private const val KEY_SALT = "install_salt"

    fun getSalt(context: Context): String {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var salt = prefs.getString(KEY_SALT, null)
        if (salt == null) {
            salt = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_SALT, salt).apply()
        }
        return salt
    }
}
