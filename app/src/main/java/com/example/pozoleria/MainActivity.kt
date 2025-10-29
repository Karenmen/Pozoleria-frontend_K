package com.example.pozoleria

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.pozoleria.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🟠 Botón de inicio de sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            iniciarSesion(email, password)
        }

        // 🟢 Texto "¿No tienes cuenta? Regístrate aquí"
        binding.txtRegistrar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // 🔹 Función para iniciar sesión
    private fun iniciarSesion(email: String, password: String) {
        val url = "http://10.0.2.2:3000/api/usuarios/login"

        val params = JSONObject().apply {
            put("correo", email)
            put("password", password)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                val mensaje = response.optString("message", "Inicio de sesión exitoso ✅")
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                // ✅ Ir a la pantalla principal
                val intent = Intent(this, MenuPrincipalActivity::class.java)
                startActivity(intent)
                finish()
            },
            { error ->
                val code = error.networkResponse?.statusCode
                val body = error.networkResponse?.data?.toString(Charsets.UTF_8)
                val mensaje = when {
                    code != null -> "Error HTTP $code: $body"
                    else -> "❌ Error de conexión con el servidor"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                Log.e("VOLLEY", mensaje)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }

        // ⏱ Política de reintento
        request.retryPolicy = DefaultRetryPolicy(
            8000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(this).add(request)
    }
}
