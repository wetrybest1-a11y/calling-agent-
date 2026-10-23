package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.CallStatusBadge
import com.example.ui.components.MetricKpiCard
import com.example.ui.theme.*

@Composable
fun OverviewDashboardScreen(
    currentOrg: Organization?,
    contacts: List<Contact>,
    voiceAgents: List<VoiceAgent>,
    campaigns: List<Campaign>,
    callRecords: List<CallRecord>,
    onNavigateToLiveCall: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onViewCallDetail: (CallRecord) -> Unit
) {
    val totalContacts = contacts.size
    val totalCalls = callRecords.size
    val connectedCalls = callRecords.count { it.status == CallStatus.COMPLETED || it.status == CallStatus.CONNECTED }
    val answerRate = if (totalCalls > 0) ((connectedCalls.toDouble() / totalCalls.toDouble()) * 100).toInt() else 85
    val totalTalkTimeMins = callRecords.sumOf { it.actualDurationSecs } / 60
    val totalSpendStr = if (currentOrg != null) {
        if (currentOrg.currency == Currency.INR) String.format("₹%.2f", currentOrg.totalSpentLifetime / 100.0)
        else String.format("$%.2f", currentOrg.totalSpentLifetime / 100.0)
    } else "₹0.00"

    val balanceStr = if (currentOrg != null) {
        if (currentOrg.currency == Currency.INR) String.format("₹%.2f", currentOrg.creditBalanceUnits / 100.0)
        else String.format("$%.2f", currentOrg.creditBalanceUnits / 100.0)
    } else "₹0.00"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner: Pay-As-You-Use Guarantee
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Indigo600),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zero Fixed Subscription Fee",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Indigo100
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Metered AI Calling Platform",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You only pay for actual connected conversation seconds. Unanswered calls are ₹0.00.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Indigo100
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToLiveCall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = Indigo600
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("launch_test_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Call", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Key Performance Indicators (Grid of 4)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricKpiCard(
                        title = "Current Balance",
                        value = balanceStr,
                        subtitle = "Prepaid available",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBgColor = Emerald50,
                        iconTintColor = Emerald600,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Total Calling Spend",
                        value = totalSpendStr,
                        subtitle = "Billed exact seconds",
                        icon = Icons.Default.TrendingUp,
                        iconBgColor = Violet50,
                        iconTintColor = Violet600,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricKpiCard(
                        title = "Answer Rate",
                        value = "$answerRate%",
                        subtitle = "$connectedCalls connected calls",
                        icon = Icons.Default.PhoneCallback,
                        iconBgColor = Cyan50,
                        iconTintColor = Cyan500,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Talk Time",
                        value = "${totalTalkTimeMins}m",
                        subtitle = "$totalContacts total CRM leads",
                        icon = Icons.Default.Timer,
                        iconBgColor = Amber50,
                        iconTintColor = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active AI Agents Quick Showcase
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = Indigo600)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Voice Agents (${voiceAgents.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = onNavigateToAgents) {
                            Text("Manage All", color = Indigo600)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    voiceAgents.take(2).forEach { agent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Indigo100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = Indigo600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(agent.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${agent.language} • Speed: ${agent.speakingSpeed}x",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald50)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Ready", color = Emerald600, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Recent Calling Activities
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Interactions & Calls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToContacts) {
                    Text("View CRM", color = Indigo600)
                }
            }
        }

        if (callRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No calls recorded yet. Tap Live Call to make your first interaction!", color = Slate500)
                }
            }
        } else {
            items(callRecords.take(5)) { call ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewCallDetail(call) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (call.qualificationScore >= 75) Emerald50 else Indigo50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (call.qualificationScore >= 75) Icons.Default.CheckCircle else Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (call.qualificationScore >= 75) Emerald600 else Indigo600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = call.contactName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${call.contactPhone} • ${call.actualDurationSecs}s talk time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                                if (call.aiSummary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = call.aiSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600,
                                        maxLines = 1,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            CallStatusBadge(call.status)
                            Spacer(modifier = Modifier.height(4.dp))
                            val costText = if (currentOrg?.currency == Currency.INR) String.format("₹%.2f", call.costUnits / 100.0)
                            else String.format("$%.2f", call.costUnits / 100.0)
                            Text(
                                text = costText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        }
                    }
                }
            }
        }
    }
}
