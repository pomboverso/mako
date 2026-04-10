package com.rama.mako.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class EntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}