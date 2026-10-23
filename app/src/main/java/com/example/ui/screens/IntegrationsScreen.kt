package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.telephony.TelephonyConfig
import com.example.ui.theme.*

data class IntegrationItem(
    val name: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val status: String,
    val statusColor: Color
)

@Composable
fun IntegrationsScreen(
    telephonyConfig: TelephonyConfig
) {
    val integrationList = listOf(
        IntegrationItem(
            name = "Google Gemini 3.5 Flash",
            category = "AI Reasoning & Dialogue",
            description = "Powers real-time speech response reasoning, structured entity extraction, and post-call lead qualification.",
            icon = Icons.Default.AutoAwesome,
            status = "Connected & Active",
            statusColor = Emerald600
        ),
        IntegrationItem(
            name = "Twilio Voice Platform",
            category = "Global Telephony",
            description = "Outbound PSTN voice trunking, programmable SIP, and WebRTC streaming media integration.",
            icon = Icons.Default.PhoneIphone,
            status = if (telephonyConfig.twilioAccountSid.isNotBlank()) "Credentials Configured" else "Interactive Sandbox Ready",
            statusColor = if (telephonyConfig.twilioAccountSid.isNotBlank()) Emerald600 else Indigo600
        ),
        IntegrationItem(
            name = "Exotel India Telecom",
            category = "India Telephony",
            description = "TRAI-compliant cloud telephony, PRI lines, and real-time outbound call webhooks.",
            icon = Icons.Default.Call,
            status = if (telephonyConfig.exotelApiKey.isNotBlank()) "Credentials Configured" else "Interactive Sandbox Ready",
            statusColor = if (telephonyConfig.exotelApiKey.isNotBlank()) Emerald600 else Indigo600
        ),
        IntegrationItem(
            name = "Razorpay Payments",
            category = "Billing & Wallet (INR)",
            description = "Enables prepaid wallet recharges via UPI, Indian NetBanking, and credit/debit cards.",
            icon = Icons.Default.Payment,
            status = "Verified & Active",
            statusColor = Emerald600
        ),
        IntegrationItem(
            name = "Stripe Gateway",
            category = "Global Billing (USD)",
            description = "Multi-currency checkout and transaction ledger settlement for international enterprises.",
            icon = Icons.Default.CreditCard,
            status = "Verified & Active",
            statusColor = Emerald600
        ),
        IntegrationItem(
            name = "CRM Webhooks & REST API",
            category = "Data Synchronization",
            description = "Bi-directional webhooks to push call outcomes, recordings, and lead status changes to HubSpot, Salesforce, or Zoho.",
            icon = Icons.Default.SyncAlt,
            status = "Ready",
            statusColor = Emerald600
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Connectors & Integrations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Enterprise telephony adapters, AI reasoning models, and payment gateways",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Indigo50,
                border = androidx.compose.foundation.BorderStroke(1.dp, Indigo200)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Indigo600, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Zero-Downtime Telephony Architecture", fontWeight = FontWeight.Bold, color = Indigo700, style = MaterialTheme.typography.titleSmall)
                        Text("When live carrier credentials (Twilio/Exotel) are omitted, CallPilot seamlessly routes calls to the high-fidelity Voice Sandbox with real-time speech synthesis.", style = MaterialTheme.typography.bodySmall, color = Indigo600, fontSize = 12.sp)
                    }
                }
            }
        }

        items(integrationList) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Indigo50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Slate600, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = item.statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = item.status,
                            color = item.statusColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
