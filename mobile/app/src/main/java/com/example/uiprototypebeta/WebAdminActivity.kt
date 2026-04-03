package com.brazwebdes.hairstylistbooking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Compatibility wrapper for older mobile routes that still launch WebAdminActivity.
 * The real admin experience now lives in AdminDashboardActivity so every entry
 * point uses the same drawer-backed web admin shell.
 */
class WebAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(
            AdminDashboardActivity.intent(
                context = this,
                title = intent.getStringExtra(AdminDashboardActivity.extraTitle).orEmpty().ifBlank { "Admin Dashboard" },
                path = intent.getStringExtra(AdminDashboardActivity.extraPath).orEmpty().ifBlank { "/admin/dashboard" }
            )
        )
        finish()
    }
}
