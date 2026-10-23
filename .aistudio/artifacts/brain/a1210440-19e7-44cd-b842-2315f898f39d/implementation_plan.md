# CallPilot AI — AI Calling Agent CRM SaaS

A production-grade, multi-tenant AI Voice Calling CRM SaaS platform on Android. Businesses can configure AI voice agents, manage and segment leads, automate intelligent follow-up calling schedules, execute outbound and inbound voice campaigns, view real-time call transcripts and Gemini-powered analytics, and pay exclusively for billable call seconds/minutes with ₹0 fixed subscription fees.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural blueprint incorporates your confirmed preferences from Phase 1 alongside the SaaS CRM platform requirements. Review the key decisions below before proceeding to execution.

- **Confirmed: Telephony & Voice Architecture**: Dual-connector architecture supporting both **Twilio** and **Exotel** API adapters, paired with an **Interactive Voice Sandbox & Audio Simulator** driven by Gemini (`gemini-3.5-flash` for reasoning/dialogue extraction and speech synthesis) for zero-downtime development, testing, and deployment.
- **Confirmed: Multi-Currency & Gateway Billing**: Multi-currency engine with native support for both **Indian Rupee (INR ₹ / paise)** and **US Dollar (USD $ / cents)**, integrating Razorpay and Stripe transaction ledger workflows, prepaid wallet balance reservation/deduction, and transparent per-second metered billing.
- **Confirmed: Android Client Form Factor**: Full-featured SaaS console with responsive collapsible sidebar / navigation rail, tabbed CRM pipeline, visual node-based Voice Agent builder, Live Call simulation and monitoring station, and dedicated Super-Admin controls.

---

## 1. Overview & Core Concept

**CallPilot AI** is a usage-based SaaS platform built to eliminate fixed monthly software costs for sales and support teams. Companies only pay for the exact connected conversation seconds their AI calling agents consume.

### Target Personas & Workflows
1. **Super Admin**: Manages tenant organizations, configures telephony provider secrets (Twilio, Exotel) and AI models, sets per-minute and per-second billable rates, audits system health, and inspects verified revenue vs. infrastructure margins.
2. **Business Owner**: Configures company profile, invites team members, provisions prepaid calling credits, builds custom AI voice agents (scripts, objection handling, qualification rules), imports contact lists, and launches automated campaigns with smart calling hours and DND suppression.
3. **Sales Agent**: Monitors assigned lead pipelines, initiates on-demand single calls, reviews AI-generated call summaries and qualification scores, updates follow-up reminders, and logs manual notes.

---

## 2. User Experience & Visual Design

### Aesthetic Direction
- **Visual Tone**: Precision enterprise SaaS aesthetic. Clean, crisp light-gray surfaces (`#F8FAFC`, `#FFFFFF`) with rich navy typography (`#0F172A`), deep slate borders (`#E2E8F0`), vibrant Indigo (`#4F46E5`) and Violet (`#7C3AED`) brand accents, and subtle Emerald status indicators (`#10B981`) for live calls and connected states.
- **Surface Elevation**: Layered cards with subtle border outlines and soft tonal elevation (`1.dp` to `3.dp`), avoiding heavy shadows or generic AI styling.

### Navigation Architecture
- **Responsive Navigation Rail / Drawer**:
  - `Overview`: Real-time KPI metrics, daily call volume graphs, answer rate, live concurrent calls, and wallet balance.
  - `Contacts & Leads`: Searchable, filterable CRM table with CSV import, duplicate detection, tags, and timeline view.
  - `AI Voice Agents`: Agent gallery, node-based conversation flow editor, prompt configuration, and interactive test console.
  - `Call Campaigns`: Outbound campaign builder, calling time-windows, concurrent lines, and live execution tracker.
  - `Live Calling & History`: Active call monitor, call log table, audio player, full transcripts, and Gemini-extracted insights.
  - `Follow-ups & Tasks`: Automated scheduling queue, callback alarms, and retry policies.
  - `Usage & Billing`: Prepaid wallet, per-second ledger, top-up modal (INR/USD), and downloadable invoices.
  - `Admin Console`: Platform-wide tenant management, global rate configuration, and margin analytics.

### Design Tokens & Layout Rules
- **Color Palette**:
  - Primary: `Indigo600` (`#4F46E5`), Primary Container: `Indigo50` (`#EEF2FF`)
  - Accent / Secondary: `Violet600` (`#7C3AED`)
  - Success: `Emerald600` (`#059669`), Error: `Rose600` (`#E11D48`), Warning: `Amber500` (`#F59E0B`)
  - Surface: `#FFFFFF`, Background: `#F8FAFC`, On-Surface: `#0F172A`, Outline: `#E2E8F0`
- **Typography**: Clean modern sans-serif scale (`Inter`/`Roboto`) with strict hierarchy (`HeadlineMedium` 28sp for dashboard counters, `TitleMedium` 16sp bold for card titles, `BodyMedium` 14sp for data tables).
- **Safe Area Insets**: Full edge-to-edge layout with `WindowInsets.safeDrawing` padding and navigation bar protection.
- **Accessibility**: Minimum interactive touch targets of `48.dp` across all buttons, dropdowns, and list action items.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Usage-Based Metered Billing vs. Fixed Subscriptions
- **Chosen Approach**: Transparent per-second ledger calculation (`Billable Amount = (Billable Seconds / 60) * Configured Rate`). Minimum connection increment (e.g. 15s/30s configurable) only applies to answered calls. Failed, busy, or unreached calls incur ₹0/$0 AI charge.
- **Rationale**: Direct alignment with the user's core business model. Provides full trust through verifiable ledger items and prevent customer churn caused by unused seat fees.

### Decision 2: Dual Telephony Adapters + Sandbox Voice Engine
- **Chosen Approach**: Clean `TelephonyAdapter` interface with implementations for **Twilio**, **Exotel**, and a high-fidelity **Interactive Audio Sandbox**. The sandbox simulates real call handshakes, audio playback, live speech input/output, and webhook callbacks.
- **Rationale**: Allows immediate end-to-end testing, demoing, and live customer usage within Android AI Studio without requiring upfront telecom KYC or paid carrier trunk line approvals, while maintaining 100% production-ready code for live Twilio/Exotel credentials.

### Decision 3: Gemini 3.5 Flash for Intelligent Reasoning & Extraction
- **Chosen Approach**: Use `gemini-3.5-flash` via secure REST / BuildConfig integration to drive the agent's real-time dialogue responses, lead qualification scoring (0-100), objection classification, structured entity extraction, and automated post-call CRM notes.
- **Rationale**: Ultra-low latency, high reasoning fidelity, and cost efficiency, perfectly matching telephony response requirements.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CALLPILOT AI ANDROID CRM                        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
               ┌────────────────────┴───────────────────┐
               ▼                                        ▼
    ┌──────────────────────┐                ┌──────────────────────┐
    │  Presentation Layer  │                │   Navigation Stack   │
    │  - Material 3 UI     │◄───────────────┤   - Drawer/Rail Nav  │
    │  - Interactive Flow  │                │   - BackHandler safe │
    │  - Live Call Screen  │                └──────────────────────┘
    └──────────┬───────────┘
               │ ViewModels & StateFlows
               ▼
    ┌───────────────────────────────────────────────────────────────┐
    │                       Domain & CRM Layer                      │
    │  - ContactRepository       - CampaignExecutionManager        │
    │  - VoiceAgentRepository    - MeteredUsageLedgerEngine         │
    │  - FollowUpSchedulerEngine - OrganizationAccessController    │
    └──────────┬────────────────────────────────┬───────────────────┘
               │                                │
               ▼                                ▼
    ┌──────────────────────┐        ┌──────────────────────────────┐
    │    Telephony Hub     │        │        AI Reasoning Hub      │
    │  - TwilioAdapter     │        │  - Gemini 3.5 Flash Client   │
    │  - ExotelAdapter     │        │  - Structured Lead Extractor │
    │  - VoiceSandboxEngine│        │  - Post-Call Summarizer      │
    └──────────┬───────────┘        └──────────────┬───────────────┘
               │                                   │
               ▼                                   ▼
    ┌──────────────────────────────────────────────────────────────┐
    │                   Data & Persistence Layer                   │
    │  - Local Database (Room): Contacts, Agents, Ledger, Calls   │
    │  - Secure Secret Management: BuildConfig (.env)             │
    │  - Transactional Wallet Store: Deductions & Reservations    │
    └──────────────────────────────────────────────────────────────┘
```

### Core Data Models
1. **Organization**: `id`, `name`, `currency` (INR/USD), `creditBalancePaiseOrCents`, `billingMode` (Prepaid/Postpaid), `spendingLimit`, `createdDate`.
2. **Contact**: `id`, `orgId`, `name`, `phone`, `email`, `company`, `status` (New, Contacted, Qualified, Closed, DND), `tags`, `totalCalls`, `totalTalkTimeSecs`, `totalSpend`, `dndConsent`.
3. **VoiceAgent**: `id`, `orgId`, `name`, `language`, `voiceTone`, `speed`, `greetingScript`, `systemPrompt`, `qualificationQuestions`, `objectionHandling`, `maxDurationSecs`, `flowNodesJson`.
4. **Campaign**: `id`, `orgId`, `name`, `agentId`, `status` (Draft, Running, Paused, Completed), `contactIds`, `allowedTimeWindows`, `concurrencyLimit`, `maxAttemptsPerContact`, `budgetCap`.
5. **CallRecord**: `id`, `orgId`, `campaignId`, `agentId`, `contactId`, `direction` (Outbound/Inbound), `status` (Queued, Ringing, Connected, Completed, NoAnswer, Busy, Failed), `startedAt`, `endedAt`, `durationSecs`, `billableDurationSecs`, `costAmount`, `transcriptJson`, `summary`, `qualificationScore`.
6. **UsageLedgerEntry**: `id`, `orgId`, `callId`, `provider`, `ratePerMinute`, `actualSecs`, `billableSecs`, `aiCharge`, `telephonyCharge`, `taxAmount`, `finalCharge`, `status` (Reserved, Settled, Refunded).
7. **FollowUpTask**: `id`, `orgId`, `contactId`, `agentId`, `scheduledTimestamp`, `attemptNumber`, `reason`, `status` (Pending, Executed, Cancelled).

---

## 5. Implementation Stages & Planned Sequence

1. **Project Setup & Platform Identity**:
   - Update `metadata.json` (`CallPilot AI`), `strings.xml`, `settings.gradle.kts`, and configure unique `applicationId`.
   - Configure Gradle dependencies (Retrofit, Kotlinx Serialization, Room, Icons Extended).
   - Generate custom adaptive launcher icon and hero assets.
2. **Design System & M3 Theme**:
   - Implement `Color.kt`, `Theme.kt`, `Type.kt` with modern enterprise SaaS tokens, responsive Navigation Rail/Bar, and container widths.
3. **Core CRM & Contact Management**:
   - Build Contact Directory, Search, Tagging, CSV Parser/Importer, Contact Details profile, and DND compliance guard.
4. **AI Voice Agent Builder**:
   - Build Agent Studio with script generator, tone/speed controllers, FAQ knowledge base, and visual conversation flow editor.
5. **Real-Time Calling Engine & Voice Sandbox**:
   - Implement `TelephonyAdapter` with Twilio and Exotel interfaces.
   - Implement interactive Voice Sandbox with live speech recognition/synthesis and Gemini-powered conversation turns.
   - Implement in-call HUD with live transcript streaming, duration counter, and mute/hang-up controls.
6. **Campaign & Follow-up Automation Engine**:
   - Build multi-lead Campaign wizard with timezone-aware time window checks, concurrency throttling, and retry scheduler.
   - Build Follow-up Task Manager with automated callback timers and CRM task statuses.
7. **Usage Ledger & Multi-Currency Wallet**:
   - Implement exact integer arithmetic for billing (paise / cents).
   - Build Wallet overview, top-up modal with Razorpay/Stripe checkout simulation, transaction ledger, and itemized invoice generator.
8. **Super-Admin Console & Analytics**:
   - Implement tenant switcher, global rate manager (per-second rates for INR and USD), provider status monitors, and financial gross-margin analytics.
9. **Verification & Testing**:
   - Run Robolectric tests on usage ledger deduction, contact CSV parsing, and call state transitions.
   - Compile and verify the complete applet.
