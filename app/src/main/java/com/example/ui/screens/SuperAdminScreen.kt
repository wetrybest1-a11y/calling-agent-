package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Currency
import com.example.data.models.Organization
import com.example.domain.billing.UsageBillingEngine
import com.example.domain.telephony.TelephonyConfig
import com.example.ui.theme.*

@Composable
fun SuperAdminScreen(
    organizations: List<Organization>,
    billingEngine: UsageBillingEngine,
    telephonyConfig: TelephonyConfig,
    onToggleOrgStatus: (String) -> Unit,
    onUpdateRates: (Long, Long) -> Unit,
    onUpdateTelephonyConfig: (TelephonyConfig) -> Unit
) {
    var showRateConfigDialog by remember { mutableStateOf(false) }
    var showTelephonyDialog by remember { mutableStateOf(false) }

    val totalTenants = organizations.size
    val activeTenants = organizations.count { it.isActive }
    val totalRevenueUnits = organizations.sumOf { it.totalSpentLifetime }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Violet600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Super-Admin Control Console",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Global SaaS tenant governance, billing rate configuration & provider health",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        }

        // Platform KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active Businesses", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text("$activeTenants / $totalTenants", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("100% tenant isolation", style = MaterialTheme.typography.bodySmall, color = Emerald600, fontSize = 11.sp)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Platform Usage Revenue", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        val revStr = String.format("₹%.2f", totalRevenueUnits / 100.0)
                        Text(revStr, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Indigo600)
                        Text("Billed on actual seconds", style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                    }
                }
            }
        }

        // System Config Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showRateConfigDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    modifier = Modifier.weight(1f).testTag("configure_rates_btn")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure Rates")
                }

                OutlinedButton(
                    onClick = { showTelephonyDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("configure_providers_btn")
                ) {
                    Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Telephony Keys")
                }
            }
        }

        // Tenants List
        item {
            Text(
                text = "Registered Business Workspaces ($totalTenants)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(organizations) { org ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(org.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (org.isActive) Emerald50 else Rose50
                            ) {
                                Text(
                                    text = if (org.isActive) "Active" else "Suspended",
                                    color = if (org.isActive) Emerald600 else Rose600,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val balFormatted = billingEngine.formatCurrencyAmount(org.creditBalanceUnits, org.currency)
                        val spentFormatted = billingEngine.formatCurrencyAmount(org.totalSpentLifetime, org.currency)
                        Text(
                            text = "Balance: $balFormatted • Lifetime Spent: $spentFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = org.isActive,
                        onCheckedChange = { onToggleOrgStatus(org.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Emerald600,
                            checkedTrackColor = Emerald50
                        )
                    )
                }
            }
        }
    }

    if (showRateConfigDialog) {
        RateConfigDialog(
            currentInr = billingEngine.rateConfig.inrRatePerMinutePaise,
            currentUsd = billingEngine.rateConfig.usdRatePerMinuteCents,
            onDismiss = { showRateConfigDialog = false },
            onSave = { inr, usd ->
                onUpdateRates(inr, usd)
                showRateConfigDialog = false
            }
        )
    }

    if (showTelephonyDialog) {
        TelephonyConfigDialog(
            config = telephonyConfig,
            onDismiss = { showTelephonyDialog = false },
            onSave = { updated ->
                onUpdateTelephonyConfig(updated)
                showTelephonyDialog = false
            }
        )
    }
}

@Composable
fun RateConfigDialog(
    currentInr: Long,
    currentUsd: Long,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    var inrRateText by remember { mutableStateOf((currentInr / 100.0).toString()) }
    var usdRateText by remember { mutableStateOf((currentUsd / 100.0).toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Global Usage Rate Engine", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Changes apply immediately to future connected call calculations.", style = MaterialTheme.typography.bodySmall, color = Slate500)

                OutlinedTextField(
                    value = inrRateText,
                    onValueChange = { inrRateText = it },
                    label = { Text("INR AI Calling Rate (₹ per minute)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usdRateText,
                    onValueChange = { usdRateText = it },
                    label = { Text("USD AI Calling Rate ($ per minute)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val inrPaise = ((inrRateText.toDoubleOrNull() ?: 2.0) * 100).toLong()
                            val usdCents = ((usdRateText.toDoubleOrNull() ?: 0.05) * 100).toLong()
                            onSave(inrPaise, usdCents)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                    ) {
                        Text("Save Rates")
                    }
                }
            }
        }
    }
}

@Composable
fun TelephonyConfigDialog(
    config: TelephonyConfig,
    onDismiss: () -> Unit,
    onSave: (TelephonyConfig) -> Unit
) {
    var twilioSid by remember { mutableStateOf(config.twilioAccountSid) }
    var twilioToken by remember { mutableStateOf(config.twilioAuthToken) }
    var exotelKey by remember { mutableStateOf(config.exotelApiKey) }
    var exotelToken by remember { mutableStateOf(config.exotelApiToken) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Telephony Carrier Adapters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Enter live carrier credentials for Twilio and Exotel, or leave as Interactive Sandbox for testing.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                }

                item {
                    OutlinedTextField(
                        value = twilioSid,
                        onValueChange = { twilioSid = it },
                        label = { Text("Twilio Account SID (e.g. AC...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = twilioToken,
                        onValueChange = { twilioToken = it },
                        label = { Text("Twilio Auth Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = exotelKey,
                        onValueChange = { exotelKey = it },
                        label = { Text("Exotel API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = exotelToken,
                        onValueChange = { exotelToken = it },
                        label = { Text("Exotel API Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSave(
                                    config.copy(
                                        twilioAccountSid = twilioSid,
                                        twilioAuthToken = twilioToken,
                                        exotelApiKey = exotelKey,
                                        exotelApiToken = exotelToken
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                        ) {
                            Text("Update Credentials")
                        }
                    }
                }
            }
        }
    }
}
