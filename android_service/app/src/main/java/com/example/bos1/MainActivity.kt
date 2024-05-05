package com.example.bos1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bos1.ui.theme.Bos1Theme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            var serviceState by remember {
                mutableStateOf(false)
            }
            Bos1Theme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartApp(serviceState) { isChecked ->
                        showToast(this, if (isChecked) "Запущен сервис!" else "Остановлен сервис!")
                        serviceState = isChecked

                        val intent = Intent(this, Collector::class.java)

                        if (isChecked) {
                            ContextCompat.startForegroundService(this, intent)
                        } else {
                            stopService(intent)
                        }
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET

            ), 1
        )
    }
}

@Composable
fun StartApp(
    serviceState: Boolean,
    onSwitchStateChanged: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Сервис сбора информации",
            modifier = Modifier.padding(vertical = 20.dp)
        )
        Switch(
            checked = serviceState,
            onCheckedChange = onSwitchStateChanged,
            thumbContent = if (serviceState) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .scale(2.0f)
        )
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Bos1Theme {
        StartApp(true)
    }
}