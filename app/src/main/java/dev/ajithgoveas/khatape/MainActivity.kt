package dev.ajithgoveas.khatape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.ajithgoveas.khatape.ui.theme.KhataPeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /*
    private var notificationsAllowed by mutableStateOf(true)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationsAllowed = granted
            Log.d("MainActivity", "POST_NOTIFICATIONS ${if (granted) "granted" else "denied"}")
        }
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        notificationsAllowed = isNotificationPermissionGranted()

        setContent {
            KhataPeTheme {
                KhataPe()
            }
        }

        /*
        maybeAutoRequestPermission()
        logDueNotificationWork()
         */
    }

    /*
    private fun isNotificationPermissionGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSharedPreferences("khatape_prefs", MODE_PRIVATE)
                .edit { putBoolean("notif_requested_before", true) }
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
     */

    /*
    private fun maybeAutoRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val prefs = getSharedPreferences("khatape_prefs", MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("notif_requested_before", false)
        val isGranted = isNotificationPermissionGranted()

        if (!isGranted) {
            if (alreadyAsked) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                prefs.edit { putBoolean("notif_requested_before", true) }
            }
        }
    }
    */

    /*
    private fun logDueNotificationWork() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                runCatching {
                    val workManager = androidx.work.WorkManager.getInstance(applicationContext)
                    val infos = withContext(Dispatchers.IO) {
                        workManager.getWorkInfosByTag("due_notification").get()
                    }
                    Log.d("MainActivity", "due_notification count=${infos.size}")
                    infos.forEach { info ->
                        Log.d(
                            "MainActivity",
                            "work id=${info.id} state=${info.state} tags=${info.tags}"
                        )
                    }
                }.onFailure {
                    Log.w("MainActivity", "Failed to query WorkManager: ${it.message}")
                }
            }
        }
    }
     */
}