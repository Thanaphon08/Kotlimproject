    package com.example.applocker1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.applocker1.ui.theme.AppLocker1Theme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException

    class MainActivity : ComponentActivity() {
        private lateinit var webSocketClient: WebSocketClient

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                // State variables
                var connectionState by remember { mutableStateOf("Disconnected") }
                var messageToSend by remember { mutableStateOf("") }
                var receivedMessage by remember { mutableStateOf("") }

                // UI Content
                MyApp(
                    connectionState = connectionState,
                    messageToSend = messageToSend,
                    receivedMessage = receivedMessage,
                    onConnectClick = {
                        connectionState = connectWebSocket("ws://172.20.10.2:7000") {
                            receivedMessage = it
                        }
                    },
                    onSendClick = {
                        sendMessage(messageToSend)
                    },
                    onMessageChange = { newMessage ->
                        messageToSend = newMessage
                    }
                )
            }
        }

        private fun connectWebSocket(url: String, onMessageReceived: (String) -> Unit): String {
            return try {
                val uri = URI(url)
                webSocketClient = object : WebSocketClient(uri) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        println("WebSocket Opened")
                    }

                    override fun onMessage(message: String?) {
                        message?.let {
                            // Use runOnUiThread to update the UI from the background thread
                            runOnUiThread {
                                onMessageReceived(it)
                            }
                        }
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        println("WebSocket Closed: $reason")
                    }

                    override fun onError(ex: Exception?) {
                        ex?.printStackTrace()
                    }
                }
                webSocketClient.connect()
                "Connected"
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                "Error: Invalid URI"
            }
        }

        private fun sendMessage(message: String) {
            if (::webSocketClient.isInitialized && webSocketClient.isOpen) {
                webSocketClient.send(message)
            } else {
                println("WebSocket is not open")
            }
        }
    }

    @Composable
    fun MyApp(
        connectionState: String,
        messageToSend: String,
        receivedMessage: String,
        onConnectClick: () -> Unit,
        onSendClick: () -> Unit,
        onMessageChange: (String) -> Unit
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Connection State: $connectionState")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Received Message: $receivedMessage")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onConnectClick) {
                        Text("Connect to WebSocket")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = messageToSend,
                        onValueChange = onMessageChange,
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onSendClick) {
                        Text("Send Data")
                    }
                }
            }
        }
    }