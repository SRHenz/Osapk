package com.oliveira.osapp.data

import android.content.Context

class Preferencias(context: Context) {
    private val prefs = context.getSharedPreferences("osapp_prefs", Context.MODE_PRIVATE)

    var nomeTecnico: String
        get() = prefs.getString("nome_tecnico", "") ?: ""
        set(value) = prefs.edit().putString("nome_tecnico", value).apply()
}
