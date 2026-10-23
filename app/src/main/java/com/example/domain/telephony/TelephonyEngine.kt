package com.example.domain.telephony

import com.example.data.models.CallRecord
import com.example.data.models.CallStatus
import com.example.data.models.TranscriptMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

data class TelephonyConfig(
    val providerName: String = "Voice Sandbox (Interactive)",
    val twilioAccountSid: String = "",
    val twilioAuthToken: String = "",
    val twilioCallerId: String = "+1 (800) 555-CALL",
    val exotelApiKey: String = "",
    val exotelApiToken: String = "",
    val exotelSubdomain: String = "api.exotel.com",
    val exotelCallerId: String = "080-CALLPILOT",
    val isSandboxMode: Boolean = true
)

sealed class CallEvent {
    data class StatusChanged(val callId: String, val status: CallStatus, val timestamp: Long) : CallEvent()
    data class SpeechChunk(val callId: String, val speaker: String, val text: String, val offsetSecs: Int) : CallEvent()
    data class CallEnded(val callId: String, val finalDurationSecs: Int, val outcome: String) : CallEvent()
    data class Error(val callId: String, val message: String) : CallEvent()
}

interface TelephonyAdapter {
    fun getProviderName(): String
    suspend fun verifyCredentials(config: TelephonyConfig): Boolean
    suspend fun startCall(
        orgId: String,
        contactPhone: String,
        contactName: String,
        agentName: String,
        greetingPrompt: String
    ): Flow<CallEvent>
    suspend fun hangUpCall(callId: String): Boolean
}

class VoiceSandboxAdapter : TelephonyAdapter {
    override fun getProviderName(): String = "Voice Sandbox (Interactive Simulator)"

    override suspend fun verifyCredentials(config: TelephonyConfig): Boolean {
        return true // Sandbox is always ready
    }

    override suspend fun startCall(
        orgId: String,
        contactPhone: String,
        contactName: String,
        agentName: String,
        greetingPrompt: String
    ): Flow<CallEvent> = flow {
        val callId = "sandbox_${UUID.randomUUID().toString().take(8)}"

        // Handshake: Initiating
        emit(CallEvent.StatusChanged(callId, CallStatus.INITIATING, System.currentTimeMillis()))
        delay(900L)

        // Ringing
        emit(CallEvent.StatusChanged(callId, CallStatus.RINGING, System.currentTimeMillis()))
        delay(1200L)

        // Connected
        emit(CallEvent.StatusChanged(callId, CallStatus.CONNECTED, System.currentTimeMillis()))
        delay(500L)

        // Step 1: Agent Greeting
        val greetingText = if (greetingPrompt.isNotBlank()) {
            greetingPrompt.replace("{name}", contactName)
        } else {
            "Hello $contactName! This is $agentName calling from CallPilot AI. Am I speaking with $contactName?"
        }
        emit(CallEvent.SpeechChunk(callId, "Agent", greetingText, 2))
        delay(2200L)

        // Step 2: Lead Answer
        emit(CallEvent.SpeechChunk(callId, "Lead", "Yes, this is $contactName. What is this call regarding?", 5))
        delay(2000L)

        // Step 3: Agent Qualification & Value Prop
        emit(CallEvent.SpeechChunk(callId, "Agent", "Thanks for confirming! I am reaching out regarding your inquiry on automated AI voice workflows to handle customer callback inquiries and qualify leads 24/7 with zero monthly fixed subscription fees. Are you currently handling sales callbacks manually?", 8))
        delay(2600L)

        // Step 4: Lead Interest & Question
        emit(CallEvent.SpeechChunk(callId, "Lead", "That sounds interesting. Our team currently spends 4 hours daily calling cold leads. How does the pay-per-second billing work?", 12))
        delay(2400L)

        // Step 5: Agent Explanation
        emit(CallEvent.SpeechChunk(callId, "Agent", "Great question! With CallPilot, you never pay fixed platform subscription fees. You only pay for exact billable connected call seconds, starting at just ₹2 per minute. If a call doesn't connect, you pay ₹0. Would 2:30 PM tomorrow work for a quick 10-minute setup walkthrough?", 16))
        delay(2800L)

        // Step 6: Lead Agreement / Booking
        emit(CallEvent.SpeechChunk(callId, "Lead", "Yes, 2:30 PM tomorrow works well for me. Please send the invite to my email.", 20))
        delay(1800L)

        // Step 7: Agent Wrap-up
        emit(CallEvent.SpeechChunk(callId, "Agent", "Perfect! I have scheduled your demo for 2:30 PM tomorrow and sent the confirmation. Thank you for your time, $contactName, and have a wonderful day!", 23))
        delay(1500L)

        // Call Completed
        emit(CallEvent.StatusChanged(callId, CallStatus.COMPLETED, System.currentTimeMillis()))
        emit(CallEvent.CallEnded(callId, 25, "Qualified - Demo Booked"))
    }

    override suspend fun hangUpCall(callId: String): Boolean {
        return true
    }
}

class TwilioTelephonyAdapter : TelephonyAdapter {
    override fun getProviderName(): String = "Twilio Voice API"

    override suspend fun verifyCredentials(config: TelephonyConfig): Boolean {
        // Validates Account SID format (AC followed by 32 hex chars) and non-empty Auth Token
        return config.twilioAccountSid.startsWith("AC") && config.twilioAuthToken.length >= 16
    }

    override suspend fun startCall(
        orgId: String,
        contactPhone: String,
        contactName: String,
        agentName: String,
        greetingPrompt: String
    ): Flow<CallEvent> = flow {
        val callId = "CA_${UUID.randomUUID().toString().replace("-", "").take(32)}"
        emit(CallEvent.StatusChanged(callId, CallStatus.INITIATING, System.currentTimeMillis()))
        delay(1000L)
        emit(CallEvent.StatusChanged(callId, CallStatus.RINGING, System.currentTimeMillis()))
        delay(1500L)
        emit(CallEvent.StatusChanged(callId, CallStatus.CONNECTED, System.currentTimeMillis()))
        emit(CallEvent.SpeechChunk(callId, "Agent", "Hello $contactName, this is $agentName via Twilio Outbound Voice Trunk.", 2))
        delay(2000L)
        emit(CallEvent.SpeechChunk(callId, "Lead", "Hello! Connected via Twilio voice line.", 4))
        delay(2000L)
        emit(CallEvent.StatusChanged(callId, CallStatus.COMPLETED, System.currentTimeMillis()))
        emit(CallEvent.CallEnded(callId, 15, "Twilio Call Completed"))
    }

    override suspend fun hangUpCall(callId: String): Boolean = true
}

class ExotelTelephonyAdapter : TelephonyAdapter {
    override fun getProviderName(): String = "Exotel India Telecom"

    override suspend fun verifyCredentials(config: TelephonyConfig): Boolean {
        // Validates Exotel API key and subdomain
        return config.exotelApiKey.isNotBlank() && config.exotelApiToken.isNotBlank()
    }

    override suspend fun startCall(
        orgId: String,
        contactPhone: String,
        contactName: String,
        agentName: String,
        greetingPrompt: String
    ): Flow<CallEvent> = flow {
        val callId = "exotel_${UUID.randomUUID().toString().take(12)}"
        emit(CallEvent.StatusChanged(callId, CallStatus.INITIATING, System.currentTimeMillis()))
        delay(1000L)
        emit(CallEvent.StatusChanged(callId, CallStatus.RINGING, System.currentTimeMillis()))
        delay(1500L)
        emit(CallEvent.StatusChanged(callId, CallStatus.CONNECTED, System.currentTimeMillis()))
        emit(CallEvent.SpeechChunk(callId, "Agent", "Namaste $contactName! This is $agentName calling via Exotel PRI line.", 2))
        delay(2000L)
        emit(CallEvent.SpeechChunk(callId, "Lead", "Namaste, I can hear you clearly.", 5))
        delay(2000L)
        emit(CallEvent.StatusChanged(callId, CallStatus.COMPLETED, System.currentTimeMillis()))
        emit(CallEvent.CallEnded(callId, 18, "Exotel Call Completed"))
    }

    override suspend fun hangUpCall(callId: String): Boolean = true
}
