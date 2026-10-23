package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.ActiveCallHUDState
import com.example.domain.ai.CallAnalysisResult
import com.example.domain.ai.GeminiVoiceService
import com.example.domain.billing.CallChargeBreakdown
import com.example.domain.billing.UsageBillingEngine
import com.example.domain.telephony.CallEvent
import com.example.domain.telephony.VoiceSandboxAdapter
import com.example.ui.components.CallStatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiveCallConsoleScreen(
    currentOrg: Organization?,
    contacts: List<Contact>,
    voiceAgents: List<VoiceAgent>,
    preSelectedContact: Contact?,
    preSelectedAgent: VoiceAgent?,
    billingEngine: UsageBillingEngine,
    onCallCompleted: (CallRecord, CallChargeBreakdown) -> Unit,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiVoiceService() }
    val sandboxAdapter = remember { VoiceSandboxAdapter() }

    var selectedContact by remember { mutableStateOf(preSelectedContact ?: contacts.firstOrNull { !it.isDndSuppressed } ?: contacts.firstOrNull()) }
    var selectedAgent by remember { mutableStateOf(preSelectedAgent ?: voiceAgents.firstOrNull()) }

    var isCallActive by remember { mutableStateOf(false) }
    var callStatus by remember { mutableStateOf<CallStatus>(CallStatus.QUEUED) }
    var callDurationSecs by remember { mutableIntStateOf(0) }
    var transcriptMessages by remember { mutableStateOf<List<TranscriptMessage>>(emptyList()) }
    var postCallAnalysis by remember { mutableStateOf<CallAnalysisResult?>(null) }
    var callChargeBreakdown by remember { mutableStateOf<CallChargeBreakdown?>(null) }
    var activeJob by remember { mutableStateOf<Job?>(null) }
    var isMuted by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new transcript message arrives
    LaunchedEffect(transcriptMessages.size) {
        if (transcriptMessages.isNotEmpty()) {
            listState.animateScrollToItem(transcriptMessages.size - 1)
        }
    }

    // Call duration timer
    LaunchedEffect(isCallActive, callStatus) {
        if (isCallActive && (callStatus == CallStatus.CONNECTED || callStatus == CallStatus.INITIATING || callStatus == CallStatus.RINGING)) {
            while (isCallActive) {
                delay(1000L)
                if (callStatus == CallStatus.CONNECTED) {
                    callDurationSecs++
                }
            }
        }
    }

    // Pulsing animation for active call soundwave
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    fun startCall() {
        val contact = selectedContact ?: return
        val agent = selectedAgent ?: return

        // DND Check
        if (contact.isDndSuppressed) return

        // Credit Balance Check
        if (currentOrg != null && currentOrg.creditBalanceUnits <= 0L && currentOrg.billingMode == BillingMode.PREPAID) {
            return
        }

        isCallActive = true
        callStatus = CallStatus.INITIATING
        callDurationSecs = 0
        transcriptMessages = emptyList()
        postCallAnalysis = null
        callChargeBreakdown = null

        activeJob = coroutineScope.launch {
            sandboxAdapter.startCall(
                orgId = currentOrg?.id ?: "org_default",
                contactPhone = contact.phone,
                contactName = contact.name,
                agentName = agent.name,
                greetingPrompt = agent.greetingScript
            ).collect { event ->
                when (event) {
                    is CallEvent.StatusChanged -> {
                        callStatus = event.status
                    }
                    is CallEvent.SpeechChunk -> {
                        transcriptMessages = transcriptMessages + TranscriptMessage(
                            sender = event.speaker,
                            text = event.text,
                            timestampOffsetSecs = event.offsetSecs
                        )
                    }
                    is CallEvent.CallEnded -> {
                        isCallActive = false
                        callStatus = CallStatus.COMPLETED

                        // Finalize Billing
                        val breakdown = billingEngine.calculateCallCharge(
                            status = CallStatus.COMPLETED,
                            durationSecs = callDurationSecs.coerceAtLeast(event.finalDurationSecs),
                            currency = currentOrg?.currency ?: Currency.INR
                        )
                        callChargeBreakdown = breakdown

                        // Gemini Post-Call Analysis
                        val analysis = geminiService.analyzeCallTranscript(
                            contactName = contact.name,
                            company = contact.company,
                            agentName = agent.name,
                            transcript = transcriptMessages
                        )
                        postCallAnalysis = analysis

                        // Record Call
                        val newRecord = CallRecord(
                            orgId = currentOrg?.id ?: "org_default",
                            agentId = agent.id,
                            agentName = agent.name,
                            contactId = contact.id,
                            contactName = contact.name,
                            contactPhone = contact.phone,
                            provider = "Voice Sandbox",
                            status = CallStatus.COMPLETED,
                            actualDurationSecs = breakdown.actualSecs,
                            billableDurationSecs = breakdown.billableSecs,
                            costUnits = breakdown.totalUnits,
                            transcript = transcriptMessages,
                            aiSummary = analysis.summary,
                            qualificationScore = analysis.qualificationScore,
                            customerIntent = analysis.customerIntent,
                            objectionsRaised = analysis.objections,
                            followUpBooked = analysis.nextAction
                        )

                        onCallCompleted(newRecord, breakdown)
                    }
                    is CallEvent.Error -> {
                        isCallActive = false
                        callStatus = CallStatus.FAILED
                    }
                }
            }
        }
    }

    fun endCall() {
        activeJob?.cancel()
        isCallActive = false
        callStatus = CallStatus.COMPLETED

        val contact = selectedContact ?: return
        val agent = selectedAgent ?: return

        val breakdown = billingEngine.calculateCallCharge(
            status = CallStatus.COMPLETED,
            durationSecs = callDurationSecs,
            currency = currentOrg?.currency ?: Currency.INR
        )
        callChargeBreakdown = breakdown

        coroutineScope.launch {
            val analysis = geminiService.analyzeCallTranscript(
                contactName = contact.name,
                company = contact.company,
                agentName = agent.name,
                transcript = transcriptMessages
            )
            postCallAnalysis = analysis

            val newRecord = CallRecord(
                orgId = currentOrg?.id ?: "org_default",
                agentId = agent.id,
                agentName = agent.name,
                contactId = contact.id,
                contactName = contact.name,
                contactPhone = contact.phone,
                provider = "Voice Sandbox",
                status = CallStatus.COMPLETED,
                actualDurationSecs = breakdown.actualSecs,
                billableDurationSecs = breakdown.billableSecs,
                costUnits = breakdown.totalUnits,
                transcript = transcriptMessages,
                aiSummary = analysis.summary,
                qualificationScore = analysis.qualificationScore,
                customerIntent = analysis.customerIntent,
                objectionsRaised = analysis.objections,
                followUpBooked = analysis.nextAction
            )

            onCallCompleted(newRecord, breakdown)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card: Target Lead & Voice Agent Selector (Only active when call is not in-flight)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Outbound Call Dispatcher",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time speech streaming, interruption handling, and live Gemini extraction",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isCallActive) {
                        // Contact Picker
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calling Contact: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${selectedContact?.name ?: "None"} (${selectedContact?.phone ?: ""})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Indigo600,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Agent Picker
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Violet600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voice Agent: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = selectedAgent?.name ?: "None",
                                style = MaterialTheme.typography.bodySmall,
                                color = Violet600,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Start Call Button
                        Button(
                            onClick = { startCall() },
                            enabled = selectedContact != null && !selectedContact!!.isDndSuppressed && (currentOrg?.creditBalanceUnits ?: 0L) > 0L,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_live_call_btn")
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Initiate Real-Time Voice Call", fontWeight = FontWeight.Bold)
                        }

                        if (selectedContact?.isDndSuppressed == true) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ Calling blocked: Contact has opted out of communications (DND)",
                                color = Rose600,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        // In-Call Active Telemetry Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(LiveCallPulse)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = selectedContact?.name ?: "In Call",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = selectedContact?.phone ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CallStatusBadge(callStatus)
                                Spacer(modifier = Modifier.width(10.dp))
                                val mins = callDurationSecs / 60
                                val secs = callDurationSecs % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // In-Call Action Buttons (Mute, Hang Up)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isMuted = !isMuted },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isMuted) Rose600 else Slate700
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isMuted) "Unmute" else "Mute Mic")
                            }

                            Button(
                                onClick = { endCall() },
                                colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                                modifier = Modifier.weight(1f).testTag("hang_up_call_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Call", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Transcript Section
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 400.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = Indigo600)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Audio & Conversation Stream",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isCallActive) {
                            Text(
                                text = "Streaming audio...",
                                style = MaterialTheme.typography.labelSmall,
                                color = LiveCallPulse,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (transcriptMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCallActive) "Connecting audio line and initiating agent handshake..." else "No active call. Tap 'Initiate Real-Time Voice Call' above.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate400
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(transcriptMessages) { msg ->
                                val isAgent = msg.sender == "Agent"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isAgent) Arrangement.Start else Arrangement.End
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAgent) 2.dp else 12.dp,
                                            bottomEnd = if (isAgent) 12.dp else 2.dp
                                        ),
                                        color = if (isAgent) Indigo50 else MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isAgent) Indigo200 else Slate200
                                        ),
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = msg.sender,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAgent) Indigo600 else Slate700
                                                )
                                                Text(
                                                    text = "+${msg.timestampOffsetSecs}s",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Slate400,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate900
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Post-Call Gemini Analysis & Billing Card
        postCallAnalysis?.let { analysis ->
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Indigo600)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini Post-Call Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Qualification Meter
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (analysis.qualificationScore >= 75) Emerald50 else Amber50,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (analysis.qualificationScore >= 75) Emerald600 else Amber500
                                )
                            ) {
                                Text(
                                    text = "Lead Score: ${analysis.qualificationScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (analysis.qualificationScore >= 75) Emerald600 else Amber500,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = analysis.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Slate700
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer Intent & Next Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Customer Intent", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(analysis.customerIntent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Indigo600)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Recommended Action", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(analysis.nextAction, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Emerald600)
                            }
                        }

                        // Exact Billing Ledger Breakdown
                        callChargeBreakdown?.let { breakdown ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Billable Usage (${breakdown.billableSecs}s connected)", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                        Text("No platform monthly fee • Exact per-second deduction", style = MaterialTheme.typography.bodySmall, color = Slate600, fontSize = 11.sp)
                                    }

                                    val costFormatted = if (currentOrg?.currency == Currency.INR) String.format("₹%.2f", breakdown.totalUnits / 100.0)
                                    else String.format("$%.2f", breakdown.totalUnits / 100.0)

                                    Text(
                                        text = costFormatted,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
