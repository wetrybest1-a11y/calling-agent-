package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.FlowNode
import com.example.data.models.VoiceAgent
import com.example.ui.theme.*

@Composable
fun VoiceAgentsScreen(
    voiceAgents: List<VoiceAgent>,
    onSaveAgent: (VoiceAgent) -> Unit,
    onTestAgent: (VoiceAgent) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingAgent by remember { mutableStateOf<VoiceAgent?>(null) }
    var selectedAgentForFlow by remember { mutableStateOf<VoiceAgent?>(voiceAgents.firstOrNull()) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Indigo600,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Build New Agent", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_agent_fab")
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
                            text = "AI Voice Agents Studio",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure voice personalities, conversation flow nodes, and objection scripts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            // Agent Cards
            items(voiceAgents) { agent ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        if (selectedAgentForFlow?.id == agent.id) 2.dp else 1.dp,
                        if (selectedAgentForFlow?.id == agent.id) Indigo600 else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAgentForFlow = agent }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Indigo50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = Indigo600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = agent.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${agent.language} • ${agent.voiceTone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { editingAgent = agent },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { onTestAgent(agent) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = agent.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        // Greeting Script preview
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = agent.greetingScript,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }
                }
            }

            // Visual Conversation Flow Diagram for Selected Agent
            selectedAgentForFlow?.let { agent ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountTree, contentDescription = null, tint = Violet600)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Conversation Flow Diagram: ${agent.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Autonomous node-based decision graph executed by real-time speech engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Flow Nodes Pipeline
                            val nodes = agent.flowNodes.ifEmpty {
                                listOf(
                                    FlowNode("1", "start", "Start Call", "Dial contact & wait for connect"),
                                    FlowNode("2", "greeting", "Intro & Verification", "Verify contact identity"),
                                    FlowNode("3", "question", "Discovery / Qualification", "Inquire about pain points"),
                                    FlowNode("4", "objection", "Price Transparency", "Clarify pay-per-second model"),
                                    FlowNode("5", "schedule", "Booking Callback", "Lock in time slot"),
                                    FlowNode("6", "end", "Call Wrap-up", "Confirm and disconnect")
                                )
                            }

                            nodes.forEachIndexed { index, node ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (index == 0) Indigo600 else Violet50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) PureWhite else Violet600
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(node.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                            Text(node.promptContent, style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                                        }
                                    }
                                }

                                if (index < nodes.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 14.dp)
                                            .width(2.dp)
                                            .height(14.dp)
                                            .background(Slate300)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Agent Dialog
    val activeDialogAgent = editingAgent ?: if (showCreateDialog) VoiceAgent(
        orgId = "org_zenith_ai",
        name = "",
        description = "",
        greetingScript = "Hello {name}, this is an automated calling specialist from CallPilot AI.",
        systemPrompt = "You are a professional calling agent. Be concise and polite."
    ) else null

    activeDialogAgent?.let { agent ->
        EditAgentDialog(
            agent = agent,
            isNew = editingAgent == null,
            onDismiss = {
                showCreateDialog = false
                editingAgent = null
            },
            onSave = { updated ->
                onSaveAgent(updated)
                showCreateDialog = false
                editingAgent = null
            }
        )
    }
}

@Composable
fun EditAgentDialog(
    agent: VoiceAgent,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (VoiceAgent) -> Unit
) {
    var name by remember { mutableStateOf(agent.name) }
    var description by remember { mutableStateOf(agent.description) }
    var greeting by remember { mutableStateOf(agent.greetingScript) }
    var prompt by remember { mutableStateOf(agent.systemPrompt) }
    var speed by remember { mutableFloatStateOf(agent.speakingSpeed) }
    var tone by remember { mutableStateOf(agent.voiceTone) }

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
                        text = if (isNew) "Create Voice Agent" else "Edit Voice Agent: ${agent.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Agent Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Role Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = tone,
                        onValueChange = { tone = it },
                        label = { Text("Voice Tone (e.g. Empathetic, Authoritative)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Speaking Speed: ${String.format("%.1fx", speed)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = speed,
                        onValueChange = { speed = it },
                        valueRange = 0.8f..1.4f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = Indigo600, activeTrackColor = Indigo600)
                    )
                }

                item {
                    OutlinedTextField(
                        value = greeting,
                        onValueChange = { greeting = it },
                        label = { Text("Opening Greeting Script (Use {name})") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("System Instructions & Guidelines") },
                        modifier = Modifier.fillMaxWidth().height(110.dp)
                    )
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
                                    onSave(
                                        agent.copy(
                                            name = name,
                                            description = description,
                                            voiceTone = tone,
                                            speakingSpeed = speed,
                                            greetingScript = greeting,
                                            systemPrompt = prompt
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                        ) {
                            Text("Save Agent")
                        }
                    }
                }
            }
        }
    }
}
