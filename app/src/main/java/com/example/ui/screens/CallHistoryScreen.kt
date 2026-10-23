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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.*
import com.example.ui.components.CallStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallHistoryScreen(
    currentOrg: Organization?,
    callRecords: List<CallRecord>,
    onViewDetail: (CallRecord) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCallForDialog by remember { mutableStateOf<CallRecord?>(null) }

    val filteredRecords = callRecords.filter {
        it.contactName.contains(searchQuery, ignoreCase = true) ||
                it.contactPhone.contains(searchQuery, ignoreCase = true) ||
                it.agentName.contains(searchQuery, ignoreCase = true) ||
                it.aiSummary.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Call History & Conversation Logs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Inspect transcripts, duration records, and exact usage charges",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by contact name, phone or summary...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Slate300, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No call records found", style = MaterialTheme.typography.titleMedium, color = Slate600)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRecords) { call ->
                    val dateFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val callDateStr = dateFmt.format(Date(call.startedAt))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCallForDialog = call }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (call.qualificationScore >= 75) Emerald50 else Indigo50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneCallback,
                                            contentDescription = null,
                                            tint = if (call.qualificationScore >= 75) Emerald600 else Indigo600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(call.contactName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("${call.contactPhone} • ${call.agentName}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                }

                                CallStatusBadge(call.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (call.aiSummary.isNotBlank()) {
                                Text(
                                    text = call.aiSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600,
                                    maxLines = 2,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Bottom Bar: Date, Duration, and Cost
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(callDateStr, style = MaterialTheme.typography.bodySmall, color = Slate400, fontSize = 11.sp)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${call.actualDurationSecs}s duration",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val costStr = if (currentOrg?.currency == Currency.INR) String.format("₹%.2f", call.costUnits / 100.0)
                                    else String.format("$%.2f", call.costUnits / 100.0)

                                    Text(
                                        text = costStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Indigo600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedCallForDialog?.let { call ->
        CallDetailModal(
            call = call,
            currency = currentOrg?.currency ?: Currency.INR,
            onDismiss = { selectedCallForDialog = null }
        )
    }
}

@Composable
fun CallDetailModal(
    call: CallRecord,
    currency: Currency,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(call.contactName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(call.contactPhone, style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                        CallStatusBadge(call.status)
                    }
                }

                item {
                    HorizontalDivider(color = Slate200)
                }

                // AI Qualification & Analysis
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Indigo50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Lead Qualification Score", fontWeight = FontWeight.Bold, color = Indigo700, style = MaterialTheme.typography.bodySmall)
                                Text("${call.qualificationScore}/100", fontWeight = FontWeight.ExtraBold, color = Indigo600, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(call.aiSummary, style = MaterialTheme.typography.bodySmall, color = Slate700)
                        }
                    }
                }

                // Cost & Billable Duration
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Actual Talk Time: ${call.actualDurationSecs}s", style = MaterialTheme.typography.bodySmall)
                                Text("Billable Time: ${call.billableDurationSecs}s", style = MaterialTheme.typography.bodySmall)
                            }

                            val costFormatted = if (currency == Currency.INR) String.format("₹%.2f", call.costUnits / 100.0)
                            else String.format("$%.2f", call.costUnits / 100.0)

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Usage Charge", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text(costFormatted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Slate900)
                            }
                        }
                    }
                }

                // Full Transcript
                item {
                    Text("Call Transcript", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                if (call.transcript.isEmpty()) {
                    item {
                        Text("No transcript available for this call", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    }
                } else {
                    items(call.transcript) { msg ->
                        val isAgent = msg.sender == "Agent"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isAgent) Arrangement.Start else Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAgent) Indigo50 else Slate100,
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(msg.sender, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isAgent) Indigo600 else Slate600)
                                    Text(msg.text, style = MaterialTheme.typography.bodySmall, color = Slate900)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Close Log")
                    }
                }
            }
        }
    }
}
