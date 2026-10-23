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
import com.example.data.models.*
import com.example.ui.theme.*

@Composable
fun CampaignManagerScreen(
    currentOrg: Organization?,
    campaigns: List<Campaign>,
    voiceAgents: List<VoiceAgent>,
    contacts: List<Contact>,
    onSaveCampaign: (Campaign) -> Unit,
    onUpdateStatus: (String, CampaignStatus) -> Unit
) {
    var showCreateWizard by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateWizard = true },
                containerColor = Indigo600,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                text = { Text("New Campaign", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_campaign_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
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
                        Text(
                            text = "Outbound Call Campaigns",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Automated outbound multi-lead dialing with concurrency limits & calling hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            if (campaigns.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Slate300, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No campaigns created yet", style = MaterialTheme.typography.titleMedium, color = Slate600)
                            Text("Create a campaign to dial leads autonomously with your AI voice agent", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                    }
                }
            } else {
                items(campaigns) { campaign ->
                    val agent = voiceAgents.find { it.id == campaign.agentId }
                    val totalTarget = campaign.contactIds.size.coerceAtLeast(1)
                    val progress = (campaign.totalCallsConnected.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f)

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = campaign.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Agent: ${agent?.name ?: "Priya"} • ${campaign.callingTimezone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (campaign.status) {
                                        CampaignStatus.RUNNING -> Emerald50
                                        CampaignStatus.PAUSED -> Amber50
                                        CampaignStatus.COMPLETED -> Indigo50
                                        else -> Slate100
                                    }
                                ) {
                                    Text(
                                        text = campaign.status.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (campaign.status) {
                                            CampaignStatus.RUNNING -> Emerald600
                                            CampaignStatus.PAUSED -> Amber500
                                            CampaignStatus.COMPLETED -> Indigo600
                                            else -> Slate600
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Campaign Progress Bar
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Campaign Execution Progress", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                    Text("${campaign.totalCallsConnected}/${campaign.contactIds.size} Leads Reached", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Indigo600,
                                    trackColor = Indigo50
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Calling hours and Concurrency details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Permitted Hours: ${campaign.allowedCallingHourStart}:00 - ${campaign.allowedCallingHourEnd}:00",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Max Concurrent: ${campaign.concurrencyLimit} lines",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Control Buttons: Start, Pause, Stop
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (campaign.status == CampaignStatus.RUNNING) {
                                    FilledTonalButton(
                                        onClick = { onUpdateStatus(campaign.id, CampaignStatus.PAUSED) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pause")
                                    }
                                } else {
                                    Button(
                                        onClick = { onUpdateStatus(campaign.id, CampaignStatus.RUNNING) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Launch Campaign")
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = { onUpdateStatus(campaign.id, CampaignStatus.COMPLETED) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Mark Complete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateWizard) {
        CreateCampaignDialog(
            orgId = currentOrg?.id ?: "org_default",
            voiceAgents = voiceAgents,
            contacts = contacts,
            onDismiss = { showCreateWizard = false },
            onSave = { newCampaign ->
                onSaveCampaign(newCampaign)
                showCreateWizard = false
            }
        )
    }
}

@Composable
fun CreateCampaignDialog(
    orgId: String,
    voiceAgents: List<VoiceAgent>,
    contacts: List<Contact>,
    onDismiss: () -> Unit,
    onSave: (Campaign) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedAgentId by remember { mutableStateOf(voiceAgents.firstOrNull()?.id ?: "") }
    var concurrency by remember { mutableIntStateOf(3) }
    var maxAttempts by remember { mutableIntStateOf(3) }
    var hourStart by remember { mutableIntStateOf(9) }
    var hourEnd by remember { mutableIntStateOf(19) }

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
                    Text(
                        text = "Create Calling Campaign",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zero fixed platform fee • Billed strictly per connected second",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Campaign Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("campaign_name_field")
                    )
                }

                item {
                    Text("Select Voice Agent", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    voiceAgents.forEach { agent ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAgentId = agent.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedAgentId == agent.id,
                                onClick = { selectedAgentId = agent.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(agent.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item {
                    Text("Concurrent Dialing Lines: $concurrency", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = concurrency.toFloat(),
                        onValueChange = { concurrency = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }

                item {
                    Text("Permitted Calling Window: $hourStart:00 - $hourEnd:00 IST", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Text("Protects customer convenience and complies with TRAI / telecom guidelines", style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Indigo50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Target Lead Count: ${contacts.filter { !it.isDndSuppressed }.size} eligible contacts", fontWeight = FontWeight.Bold, color = Indigo700, style = MaterialTheme.typography.bodySmall)
                            Text("DND contacts are automatically suppressed and will never be called.", style = MaterialTheme.typography.bodySmall, color = Indigo600, fontSize = 11.sp)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val eligibleContactIds = contacts.filter { !it.isDndSuppressed }.map { it.id }
                                    onSave(
                                        Campaign(
                                            orgId = orgId,
                                            name = name,
                                            agentId = selectedAgentId,
                                            contactIds = eligibleContactIds,
                                            concurrencyLimit = concurrency,
                                            maxAttemptsPerContact = maxAttempts,
                                            allowedCallingHourStart = hourStart,
                                            allowedCallingHourEnd = hourEnd,
                                            status = CampaignStatus.SCHEDULED
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                        ) {
                            Text("Create & Schedule")
                        }
                    }
                }
            }
        }
    }
}
