package com.oliveira.osapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oliveira.osapp.adapter.OSAdapter
import com.oliveira.osapp.data.OSStorage

class MainActivity : AppCompatActivity() {

    private lateinit var storage: OSStorage
    private lateinit var adapter: OSAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var textVazio: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        storage = OSStorage(this)
        recycler = findViewById(R.id.recyclerOS)
        textVazio = findViewById(R.id.textVazio)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = OSAdapter(emptyList()) { os ->
            val i = Intent(this, OSEditActivity::class.java)
            i.putExtra(OSEditActivity.EXTRA_OS_ID, os.id)
            startActivity(i)
        }
        recycler.adapter = adapter

        findViewById<android.view.View>(R.id.fabNovaOS).setOnClickListener {
            startActivity(Intent(this, OSEditActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarLista()
    }

    private fun carregarLista() {
        val arquivos = storage.listar()
        val ordens = arquivos.map { storage.carregar(it) }
        adapter.atualizar(ordens)
        textVazio.visibility = if (ordens.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
