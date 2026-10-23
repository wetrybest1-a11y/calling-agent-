package com.example.data.repository

import com.example.data.models.*
import com.example.domain.billing.BillingRateConfig
import com.example.domain.billing.UsageBillingEngine
import com.example.domain.telephony.TelephonyConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CallPilotRepository {

    val billingEngine = UsageBillingEngine()

    // 1. Current Active Session State
    private val _currentUserRole = MutableStateFlow(UserRole.BUSINESS_OWNER)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _telephonyConfig = MutableStateFlow(TelephonyConfig())
    val telephonyConfig: StateFlow<TelephonyConfig> = _telephonyConfig.asStateFlow()

    // 2. Multi-tenant Organizations
    private val _organizations = MutableStateFlow<List<Organization>>(emptyList())
    val organizations: StateFlow<List<Organization>> = _organizations.asStateFlow()

    private val _currentOrg = MutableStateFlow<Organization?>(null)
    val currentOrg: StateFlow<Organization?> = _currentOrg.asStateFlow()

    // 3. CRM Contacts
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // 4. AI Voice Agents
    private val _voiceAgents = MutableStateFlow<List<VoiceAgent>>(emptyList())
    val voiceAgents: StateFlow<List<VoiceAgent>> = _voiceAgents.asStateFlow()

    // 5. Campaigns
    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    val campaigns: StateFlow<List<Campaign>> = _campaigns.asStateFlow()

    // 6. Call History
    private val _callRecords = MutableStateFlow<List<CallRecord>>(emptyList())
    val callRecords: StateFlow<List<CallRecord>> = _callRecords.asStateFlow()

    // 7. Usage Ledger
    private val _ledgerEntries = MutableStateFlow<List<UsageLedgerEntry>>(emptyList())
    val ledgerEntries: StateFlow<List<UsageLedgerEntry>> = _ledgerEntries.asStateFlow()

    // 8. Follow-up Tasks
    private val _followUpTasks = MutableStateFlow<List<FollowUpTask>>(emptyList())
    val followUpTasks: StateFlow<List<FollowUpTask>> = _followUpTasks.asStateFlow()

    // 9. Active Call Session HUD
    private val _activeCallHUD = MutableStateFlow<ActiveCallHUDState?>(null)
    val activeCallHUD: StateFlow<ActiveCallHUDState?> = _activeCallHUD.asStateFlow()

    init {
        seedInitialDemoData()
    }

    private fun seedInitialDemoData() {
        val org1 = Organization(
            id = "org_zenith_ai",
            name = "Zenith Dynamics India",
            currency = Currency.INR,
            creditBalanceUnits = 34500L, // ₹345.00
            billingMode = BillingMode.PREPAID,
            spendingLimitMonthly = 500000L,
            totalSpentLifetime = 15850L
        )
        val org2 = Organization(
            id = "org_apex_global",
            name = "Apex Cloud Solutions Inc.",
            currency = Currency.USD,
            creditBalanceUnits = 6500L, // $65.00
            billingMode = BillingMode.PREPAID,
            spendingLimitMonthly = 100000L,
            totalSpentLifetime = 3200L
        )

        _organizations.value = listOf(org1, org2)
        _currentOrg.value = org1

        // Seed Voice Agent
        val agent1 = VoiceAgent(
            id = "agent_priya_sales",
            orgId = org1.id,
            name = "Priya - B2B Outbound Specialist",
            description = "Empathetic, clear English speaking agent with Indian accent specialized in SaaS lead qualification and demo booking.",
            language = "English (Indian Accent)",
            voiceTone = "Professional, Warm & Engaging",
            speakingSpeed = 1.0f,
            greetingScript = "Hello {name}! This is Priya from CallPilot AI. Am I speaking with {name}?",
            systemPrompt = "You are Priya, an executive outreach specialist at CallPilot AI. Your goal is to qualify leads and secure a 10-minute demo walkthrough. Never be pushy, address objections about pricing clearly by emphasizing ₹0 monthly subscription and pay-only-for-connected-seconds.",
            qualificationQuestions = listOf(
                "How many outbound calls does your team make daily?",
                "Are you currently paying fixed monthly software seat licenses?",
                "Would 2:30 PM tomorrow work for a 10-minute setup walkthrough?"
            ),
            objectionHandlingRules = mapOf(
                "Too busy" to "I completely understand! That's why CallPilot automates callbacks. Can I drop a 60-second summary on WhatsApp instead?",
                "Is there a monthly fee?" to "Absolutely zero! You never pay a fixed monthly subscription. You only pay for the exact connected conversation seconds you consume."
            ),
            knowledgeBaseFaq = listOf(
                "How much does it cost?" to "Calling starts at just ₹2 per connected minute (under 3.4 paise per second). Unanswered calls are ₹0.",
                "Can you integrate with our CRM?" to "Yes, CallPilot supports webhooks, CSV exports, and direct CRM sync."
            ),
            flowNodes = listOf(
                FlowNode("node_1", "start", "Start Call", "Dial contact number"),
                FlowNode("node_2", "greeting", "Intro & Verification", "Verify identity and introduce CallPilot", "node_3"),
                FlowNode("node_3", "question", "Discovery Question", "Inquire about current callback workflow", "node_4"),
                FlowNode("node_4", "objection", "Pricing Transparency", "Highlight pay-per-second and ₹0 subscription", "node_5"),
                FlowNode("node_5", "schedule", "Book Demo Walkthrough", "Secure confirmation for tomorrow at 2:30 PM", "node_6"),
                FlowNode("node_6", "end", "End Call Gracefully", "Confirm email calendar invite and hang up")
            ),
            isPublished = true,
            version = 2
        )

        val agent2 = VoiceAgent(
            id = "agent_alex_support",
            orgId = org1.id,
            name = "Alex - Inbound Support & Callback",
            description = "Fast, efficient AI agent for answering inbound product inquiries and scheduling callbacks.",
            language = "English (Neutral)",
            voiceTone = "Helpful & Direct",
            greetingScript = "Thanks for calling CallPilot support. How can I assist your business today?",
            systemPrompt = "Handle customer questions about wallet recharges, telephony integration, and campaign scheduling.",
            isPublished = true,
            version = 1
        )

        _voiceAgents.value = listOf(agent1, agent2)

        // Seed Contacts
        val c1 = Contact(
            id = "c_rohit_verma",
            orgId = org1.id,
            name = "Rohit Verma",
            phone = "+91 98765 43210",
            email = "rohit.verma@techcorp.in",
            company = "TechCorp Infotech",
            leadSource = "Inbound Website",
            status = LeadStatus.QUALIFIED,
            tags = listOf("High Value", "Bangalore", "SaaS"),
            totalCalls = 2,
            totalConnectedCalls = 2,
            totalTalkTimeSecs = 185,
            totalCostUnits = 617L,
            lastCallOutcome = "Qualified - Demo Booked",
            lastContactedAt = System.currentTimeMillis() - 86400000L
        )

        val c2 = Contact(
            id = "c_ananya_sharma",
            orgId = org1.id,
            name = "Ananya Sharma",
            phone = "+91 98111 22334",
            email = "ananya@growthscale.co",
            company = "GrowthScale Digital",
            leadSource = "CSV Import",
            status = LeadStatus.INTERESTED,
            tags = listOf("Agency", "Delhi NCR"),
            totalCalls = 1,
            totalConnectedCalls = 1,
            totalTalkTimeSecs = 74,
            totalCostUnits = 247L,
            lastCallOutcome = "Callback Requested",
            lastContactedAt = System.currentTimeMillis() - 43200000L,
            nextFollowUpAt = System.currentTimeMillis() + 86400000L
        )

        val c3 = Contact(
            id = "c_vikram_patel",
            orgId = org1.id,
            name = "Vikram Patel",
            phone = "+91 97234 56789",
            email = "vikram@fintechventures.io",
            company = "Fintech Ventures",
            leadSource = "LinkedIn Ads",
            status = LeadStatus.NEW,
            tags = listOf("Finance", "Mumbai"),
            totalCalls = 0,
            totalConnectedCalls = 0,
            totalTalkTimeSecs = 0,
            totalCostUnits = 0L,
            lastCallOutcome = "None"
        )

        val c4 = Contact(
            id = "c_priyanka_nair",
            orgId = org1.id,
            name = "Priyanka Nair",
            phone = "+91 94470 12345",
            email = "pnair@keralaretail.com",
            company = "Kerala Retail Chains",
            leadSource = "Referral",
            status = LeadStatus.DO_NOT_CALL,
            tags = listOf("Retail", "Kochi"),
            isDndSuppressed = true,
            lastCallOutcome = "Opted Out - DND Registered"
        )

        _contacts.value = listOf(c1, c2, c3, c4)

        // Seed Campaigns
        val camp1 = Campaign(
            id = "camp_q4_enterprise",
            orgId = org1.id,
            name = "Q4 Enterprise Voice Inquiries",
            agentId = agent1.id,
            contactIds = listOf(c1.id, c2.id, c3.id),
            status = CampaignStatus.RUNNING,
            concurrencyLimit = 3,
            totalCallsAttempted = 14,
            totalCallsConnected = 11,
            totalCallsFailed = 3,
            totalSpentUnits = 2840L
        )

        _campaigns.value = listOf(camp1)

        // Seed Initial Call Records & Ledger
        val call1 = CallRecord(
            id = "call_rec_101",
            orgId = org1.id,
            campaignId = camp1.id,
            agentId = agent1.id,
            agentName = agent1.name,
            contactId = c1.id,
            contactName = c1.name,
            contactPhone = c1.phone,
            provider = "Voice Sandbox",
            status = CallStatus.COMPLETED,
            startedAt = System.currentTimeMillis() - 7200000L,
            endedAt = System.currentTimeMillis() - 7075000L,
            actualDurationSecs = 125,
            billableDurationSecs = 125,
            ratePerMinuteUnits = 200L,
            costUnits = 417L,
            transcript = listOf(
                TranscriptMessage("Agent", "Hello Rohit! This is Priya calling from CallPilot AI.", 2),
                TranscriptMessage("Lead", "Yes Priya, tell me what this is regarding.", 5),
                TranscriptMessage("Agent", "We provide automated AI calling agents with zero monthly subscription where you only pay per connected second.", 9),
                TranscriptMessage("Lead", "Sounds great! Let's book a demo for 2:30 PM tomorrow.", 16),
                TranscriptMessage("Agent", "Done! Demo invite sent to rohit.verma@techcorp.in.", 21)
            ),
            aiSummary = "Qualified prospect with strong interest in eliminating fixed seat licenses. Scheduled 10-minute setup walkthrough for tomorrow at 2:30 PM.",
            qualificationScore = 94,
            customerIntent = "High Purchase Intent",
            objectionsRaised = emptyList(),
            followUpBooked = "Tomorrow at 2:30 PM"
        )

        _callRecords.value = listOf(call1)

        val ledger1 = UsageLedgerEntry(
            id = "ldg_001",
            orgId = org1.id,
            callId = call1.id,
            provider = "Voice Sandbox",
            actualDurationSecs = 125,
            billableDurationSecs = 125,
            ratePerMinuteUnits = 200L,
            aiChargeUnits = 417L,
            telephonyChargeUnits = 0L,
            taxUnits = 75L,
            finalChargeUnits = 492L,
            currency = Currency.INR,
            status = LedgerStatus.SETTLED
        )

        _ledgerEntries.value = listOf(ledger1)

        // Seed Follow-up Task
        val task1 = FollowUpTask(
            id = "task_001",
            orgId = org1.id,
            contactId = c2.id,
            contactName = c2.name,
            contactPhone = c2.phone,
            agentId = agent1.id,
            scheduledAt = System.currentTimeMillis() + 86400000L,
            reason = "Lead requested callback after team meeting to review pay-as-you-use pricing",
            status = TaskStatus.PENDING
        )

        _followUpTasks.value = listOf(task1)
    }

    // Role & Org Switchers
    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    fun selectOrganization(orgId: String) {
        val org = _organizations.value.find { it.id == orgId }
        if (org != null) {
            _currentOrg.value = org
        }
    }

    fun updateTelephonyConfig(config: TelephonyConfig) {
        _telephonyConfig.value = config
    }

    // Contacts CRUD & CSV Import
    fun addContact(contact: Contact) {
        _contacts.value = listOf(contact) + _contacts.value
    }

    fun updateContact(updated: Contact) {
        _contacts.value = _contacts.value.map { if (it.id == updated.id) updated else it }
    }

    fun toggleContactDnd(contactId: String) {
        _contacts.value = _contacts.value.map {
            if (it.id == contactId) {
                val newDnd = !it.isDndSuppressed
                it.copy(
                    isDndSuppressed = newDnd,
                    status = if (newDnd) LeadStatus.DO_NOT_CALL else LeadStatus.NEW
                )
            } else it
        }
    }

    fun importContactsFromCsv(csvText: String, orgId: String): Int {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.size <= 1) return 0

        val existingPhones = _contacts.value.map { it.phone.replace(Regex("[^0-9+]"), "") }.toSet()
        val imported = mutableListOf<Contact>()

        for (line in lines.drop(1)) {
            val parts = line.split(",").map { it.trim().trim('\"') }
            if (parts.size >= 2) {
                val name = parts[0]
                val phone = parts[1]
                val cleanPhone = phone.replace(Regex("[^0-9+]"), "")

                // Prevent duplicates
                if (cleanPhone !in existingPhones && imported.none { it.phone.replace(Regex("[^0-9+]"), "") == cleanPhone }) {
                    val email = parts.getOrNull(2) ?: "contact@example.com"
                    val company = parts.getOrNull(3) ?: "Imported Enterprise"
                    imported.add(
                        Contact(
                            orgId = orgId,
                            name = name,
                            phone = phone,
                            email = email,
                            company = company,
                            leadSource = "CSV Import",
                            status = LeadStatus.NEW
                        )
                    )
                }
            }
        }

        _contacts.value = imported + _contacts.value
        return imported.size
    }

    // Voice Agent Operations
    fun saveVoiceAgent(agent: VoiceAgent) {
        val current = _voiceAgents.value
        val index = current.indexOfFirst { it.id == agent.id }
        if (index >= 0) {
            _voiceAgents.value = current.toMutableList().also { it[index] = agent }
        } else {
            _voiceAgents.value = listOf(agent) + current
        }
    }

    // Campaign Operations
    fun saveCampaign(campaign: Campaign) {
        val current = _campaigns.value
        val index = current.indexOfFirst { it.id == campaign.id }
        if (index >= 0) {
            _campaigns.value = current.toMutableList().also { it[index] = campaign }
        } else {
            _campaigns.value = listOf(campaign) + current
        }
    }

    fun updateCampaignStatus(campaignId: String, newStatus: CampaignStatus) {
        _campaigns.value = _campaigns.value.map {
            if (it.id == campaignId) it.copy(status = newStatus) else it
        }
    }

    // Wallet Operations & Deductions
    fun rechargeWallet(amountUnits: Long, paymentProvider: String) {
        val org = _currentOrg.value ?: return
        val updated = org.copy(
            creditBalanceUnits = org.creditBalanceUnits + amountUnits
        )
        _currentOrg.value = updated
        _organizations.value = _organizations.value.map { if (it.id == org.id) updated else it }
    }

    fun recordCallCompletion(
        record: CallRecord,
        breakdown: com.example.domain.billing.CallChargeBreakdown
    ) {
        // Add call record
        _callRecords.value = listOf(record) + _callRecords.value

        // Update contact statistics
        _contacts.value = _contacts.value.map {
            if (it.id == record.contactId) {
                it.copy(
                    totalCalls = it.totalCalls + 1,
                    totalConnectedCalls = if (record.status == CallStatus.COMPLETED) it.totalConnectedCalls + 1 else it.totalConnectedCalls,
                    totalTalkTimeSecs = it.totalTalkTimeSecs + record.actualDurationSecs,
                    totalCostUnits = it.totalCostUnits + breakdown.totalUnits,
                    lastCallOutcome = if (record.qualificationScore >= 75) "Qualified - Demo Booked" else "Call Completed",
                    lastContactedAt = System.currentTimeMillis(),
                    status = if (record.qualificationScore >= 75) LeadStatus.QUALIFIED else it.status
                )
            } else it
        }

        // Add ledger entry
        val org = _currentOrg.value
        if (org != null) {
            val entry = UsageLedgerEntry(
                orgId = org.id,
                callId = record.id,
                provider = record.provider,
                actualDurationSecs = breakdown.actualSecs,
                billableDurationSecs = breakdown.billableSecs,
                ratePerMinuteUnits = record.ratePerMinuteUnits,
                aiChargeUnits = breakdown.baseAiChargeUnits,
                telephonyChargeUnits = breakdown.telephonyChargeUnits,
                taxUnits = breakdown.taxUnits,
                finalChargeUnits = breakdown.totalUnits,
                currency = org.currency,
                status = LedgerStatus.SETTLED
            )
            _ledgerEntries.value = listOf(entry) + _ledgerEntries.value

            // Deduct from wallet balance
            val updatedOrg = org.copy(
                creditBalanceUnits = (org.creditBalanceUnits - breakdown.totalUnits).coerceAtLeast(0L),
                totalSpentLifetime = org.totalSpentLifetime + breakdown.totalUnits
            )
            _currentOrg.value = updatedOrg
            _organizations.value = _organizations.value.map { if (it.id == org.id) updatedOrg else it }
        }
    }

    // Active Call HUD Management
    fun setActiveCallHUD(hudState: ActiveCallHUDState?) {
        _activeCallHUD.value = hudState
    }

    // Follow-up Task Operations
    fun scheduleFollowUp(task: FollowUpTask) {
        _followUpTasks.value = listOf(task) + _followUpTasks.value
    }

    fun completeFollowUp(taskId: String) {
        _followUpTasks.value = _followUpTasks.value.map {
            if (it.id == taskId) it.copy(status = TaskStatus.EXECUTED) else it
        }
    }

    // Super Admin Controls
    fun toggleOrgStatus(orgId: String) {
        _organizations.value = _organizations.value.map {
            if (it.id == orgId) it.copy(isActive = !it.isActive) else it
        }
        if (_currentOrg.value?.id == orgId) {
            _currentOrg.value = _organizations.value.find { it.id == orgId }
        }
    }

    fun updateGlobalRates(newInrRate: Long, newUsdRate: Long) {
        billingEngine.rateConfig = billingEngine.rateConfig.copy(
            inrRatePerMinutePaise = newInrRate,
            usdRatePerMinuteCents = newUsdRate
        )
    }
}

data class ActiveCallHUDState(
    val callId: String,
    val contactName: String,
    val contactPhone: String,
    val agentName: String,
    val status: CallStatus,
    val elapsedSecs: Int,
    val currentTranscript: List<TranscriptMessage>,
    val isMuted: Boolean = false
)
