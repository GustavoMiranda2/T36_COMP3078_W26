package com.brazwebdes.hairstylistbooking

import android.app.Activity
import android.content.Intent
import android.widget.Toast

private val authExpiryNeedles = listOf(
    "session expired",
    "authentication credentials were not provided",
    "saved session not found"
)

fun Activity.handleAuthExpiry(message: String): Boolean {
    val normalized = message.trim().lowercase()
    if (authExpiryNeedles.none { normalized.contains(it) }) {
        return false
    }

    AppSessionStore.clear(this)
    Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_LONG).show()
    startActivity(Intent(this, LoginActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    })
    finish()
    return true
}
