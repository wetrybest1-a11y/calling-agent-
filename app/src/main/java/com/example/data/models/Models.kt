package com.example.data.models

import java.util.UUID

enum class UserRole {
    SUPER_ADMIN,
    BUSINESS_OWNER,
    SALES_AGENT
}

enum class Currency {
    INR,
    USD
}

enum class BillingMode {
    PREPAID,
    POSTPAID
}

data class Organization(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val currency: Currency = Currency.INR,
    val creditBalanceUnits: Long = 25000L, // in lowest unit (paise for INR, cents for USD) -> 25000 paise = ₹250.00
    val reservedUnits: Long = 0L,
    val billingMode: BillingMode = BillingMode.PREPAID,
    val spendingLimitMonthly: Long = 500000L, // ₹5,000.00
    val totalSpentLifetime: Long = 12450L,
    val activeTelephonyProvider: String = "Voice Sandbox (Interactive)",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LeadStatus {
    NEW,
    CONTACTED,
    INTERESTED,
    QUALIFIED,
    FOLLOW_UP_SCHEDULED,
    DO_NOT_CALL,
    CLOSED_WON,
    UNRESPONSIVE
}

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val orgId: String,
    val name: String,
    val phone: String,
    val email: String,
    val company: String,
    val leadSource: String = "Website",
    val status: LeadStatus = LeadStatus.NEW,
    val tags: List<String> = emptyList(),
    val customFields: Map<String, String> = emptyMap(),
    val notes: String = "",
    val totalCalls: Int = 0,
    val totalConnectedCalls: Int = 0,
    val totalTalkTimeSecs: Int = 0,
    val totalCostUnits: Long = 0L,
    val lastCallOutcome: String = "None",
    val lastContactedAt: Long? = null,
    val nextFollowUpAt: Long? = null,
    val isDndSuppressed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class FlowNode(
    val id: String,
    val type: String, // "start", "greeting", "question", "objection", "schedule", "end"
    val label: String,
    val promptContent: String,
    val nextNodeId: String? = null
)

data class VoiceAgent(
    val id: String = UUID.randomUUID().toString(),
    val orgId: String,
    val name: String,
    val description: String,
    val language: String = "English (Indian Accent)",
    val voiceTone: String = "Professional & Empathetic",
    val speakingSpeed: Float = 1.0f,
    val greetingScript: String,
    val systemPrompt: String,
    val qualificationQuestions: List<String> = emptyList(),
    val objectionHandlingRules: Map<String, String> = emptyMap(),
    val knowledgeBaseFaq: List<Pair<String, String>> = emptyList(),
    val maxCallDurationSecs: Int = 300,
    val silenceTimeoutSecs: Int = 5,
    val flowNodes: List<FlowNode> = emptyList(),
    val isPublished: Boolean = true,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CampaignStatus {
    DRAFT,
    SCHEDULED,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class Campaign(
    val id: String = UUID.randomUUID().toString(),
    val orgId: String,
    val name: String,
    val agentId: String,
    val contactIds: List<String>,
    val objective: String = "Lead Qualification & Callback Booking",
    val status: CampaignStatus = CampaignStatus.DRAFT,
    val concurrencyLimit: Int = 3,
    val maxAttemptsPerContact: Int = 3,
    val allowedCallingHourStart: Int = 9,  // 9 AM
    val allowedCallingHourEnd: Int = 19,   // 7 PM
    val callingTimezone: String = "Asia/Kolkata (IST)",
    val spendingLimitUnits: Long = 100000L, // ₹1,000.00 cap
    val totalCallsAttempted: Int = 0,
    val totalCallsConnected: Int = 0,
    val totalCallsFailed: Int = 0,
    val totalSpentUnits: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CallStatus {
    QUEUED,
    INITIATING,
    RINGING,
    CONNECTED,
    COMPLETED,
    BUSY,
    NO_ANSWER,
    REJECTED,
    FAILED
}

data class TranscriptMessage(
    val sender: String, // "Agent" or "Lead"
    val text: String,
    val timestampOffsetSecs: Int
)

data class CallRecord(
    val id: String = UUID.randomUUID().toString(),
    val orgId: String,
    val campaignId: String? = null,
    val agentId: String,
    val agentName: String,
    val contactId: String,
    val contactName: String,
    val contactPhone: String,
    val provider: String = "Voice Sandbox",
    val providerCallId: String = "call_${System.currentTimeMillis()}",
    val status: CallStatus = CallStatus.COMPLETED,
    val startedAt: Long = System.currentTimeMillis() - 125000L,
    val endedAt: Long = System.currentTimeMillis(),
    val actualDurationSecs: Int = 125,
    val billableDurationSecs: Int = 125,
    val ratePerMinuteUnits: Long = 200L, // 200 paise = ₹2.00
    val costUnits: Long = 417L, // (125/60)*200 = 417 paise = ₹4.17
    val transcript: List<TranscriptMessage> = emptyList(),
    val aiSummary: String = "",
    val qualificationScore: Int = 85, // 0-100
    val customerIntent: String = "High Purchase Intent",
    val objectionsRaised: List<String> = emptyList(),
    val followUpBooked: String? = null
)

enum class LedgerStatus {
    RESERVED,
    SETTLED,
    REFUNDED
}

data class UsageLedgerEntry(
    val id: String = UUID.randomUUID().toString(),
    val orgId: String,
    val callId: String,
    val provider: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actualDurationSecs: Int,
    val billableDurationSecs: Int,
    val ratePerMinuteUnits: Long,
    val aiChargeUnits: Long,
    val telephonyChargeUnits: Long = 0L,
    val taxUnits: Long = 0L,
    val finalChargeUnits: Long,
    val currency: Currency = Currency.INR,
    val status: LedgerStatus = LedgerStatus.SETTLED
)

enum class TaskStatus {
    PENDING,
    EXECUTED,
    CANCELLED
}

data class FollowUpTask(
    val id: String = UUID.randomUUID().toString(),
    val orgId: String,
    val contactId: String,
    val contactName: String,
    val contactPhone: String,
    val agentId: String,
    val scheduledAt: Long,
    val attemptNumber: Int = 1,
    val maxAttempts: Int = 3,
    val reason: String = "Callback requested during AI call",
    val status: TaskStatus = TaskStatus.PENDING
)

data class BillingInvoice(
    val invoiceNumber: String,
    val orgId: String,
    val orgName: String,
    val periodStart: String,
    val periodEnd: String,
    val totalCalls: Int,
    val billableMinutes: Double,
    val aiCharges: Double,
    val telecomCharges: Double,
    val taxes: Double,
    val totalAmount: Double,
    val currency: Currency,
    val status: String = "PAID",
    val dateIssued: Long = System.currentTimeMillis()
)
