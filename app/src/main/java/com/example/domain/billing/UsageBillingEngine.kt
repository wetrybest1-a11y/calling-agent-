package com.example.domain.billing

import com.example.data.models.BillingInvoice
import com.example.data.models.CallStatus
import com.example.data.models.Currency
import com.example.data.models.UsageLedgerEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil

data class BillingRateConfig(
    val inrRatePerMinutePaise: Long = 200L,     // ₹2.00 per minute
    val usdRatePerMinuteCents: Long = 5L,       // $0.05 per minute
    val minBillableIncrementSecs: Int = 1,      // Exact per-second billing
    val minCallDurationFloorSecs: Int = 0,      // No arbitrary connection penalty
    val telephonyPassThroughPaisePerMin: Long = 0L, // Included in AI rate by default
    val taxPercentage: Double = 18.0            // 18% GST (demonstrated transparently)
)

class UsageBillingEngine(
    var rateConfig: BillingRateConfig = BillingRateConfig()
) {

    /**
     * Calculates billable charge in the smallest currency unit (paise for INR, cents for USD).
     * Rule: If call was NOT answered or failed, charge is strictly 0!
     */
    fun calculateCallCharge(
        status: CallStatus,
        durationSecs: Int,
        currency: Currency
    ): CallChargeBreakdown {
        if (status != CallStatus.CONNECTED && status != CallStatus.COMPLETED) {
            return CallChargeBreakdown(
                actualSecs = durationSecs,
                billableSecs = 0,
                baseAiChargeUnits = 0L,
                telephonyChargeUnits = 0L,
                taxUnits = 0L,
                totalUnits = 0L
            )
        }

        val billableSecs = if (durationSecs <= 0) 0 else {
            // Apply minimum increment if configured
            val inc = rateConfig.minBillableIncrementSecs.coerceAtLeast(1)
            ceil(durationSecs.toDouble() / inc).toInt() * inc
        }

        val ratePerMinute = when (currency) {
            Currency.INR -> rateConfig.inrRatePerMinutePaise
            Currency.USD -> rateConfig.usdRatePerMinuteCents
        }

        // Exact per-second calculation: (billableSecs / 60.0) * ratePerMinute
        val aiChargeDouble = (billableSecs.toDouble() / 60.0) * ratePerMinute.toDouble()
        val baseAiUnits = ceil(aiChargeDouble).toLong()

        val telecomChargeUnits = 0L // Transparent ₹0 when bundled

        // Tax calculation: (18% GST for INR)
        val taxUnits = if (currency == Currency.INR && rateConfig.taxPercentage > 0.0) {
            ceil(baseAiUnits * (rateConfig.taxPercentage / 100.0)).toLong()
        } else 0L

        val totalUnits = baseAiUnits + telecomChargeUnits + taxUnits

        return CallChargeBreakdown(
            actualSecs = durationSecs,
            billableSecs = billableSecs,
            baseAiChargeUnits = baseAiUnits,
            telephonyChargeUnits = telecomChargeUnits,
            taxUnits = taxUnits,
            totalUnits = totalUnits
        )
    }

    fun formatCurrencyAmount(units: Long, currency: Currency): String {
        return when (currency) {
            Currency.INR -> {
                val rupees = units / 100.0
                String.format(Locale.getDefault(), "₹%.2f", rupees)
            }
            Currency.USD -> {
                val dollars = units / 100.0
                String.format(Locale.getDefault(), "$%.2f", dollars)
            }
        }
    }

    fun generateMonthlyInvoice(
        orgId: String,
        orgName: String,
        ledgerEntries: List<UsageLedgerEntry>,
        currency: Currency
    ): BillingInvoice {
        val dateFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val period = dateFmt.format(Date())

        val totalCalls = ledgerEntries.size
        val totalSecs = ledgerEntries.sumOf { it.billableDurationSecs }
        val billableMinutes = totalSecs / 60.0
        val totalAiUnits = ledgerEntries.sumOf { it.aiChargeUnits }
        val totalTaxUnits = ledgerEntries.sumOf { it.taxUnits }
        val totalNetUnits = ledgerEntries.sumOf { it.finalChargeUnits }

        return BillingInvoice(
            invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
            orgId = orgId,
            orgName = orgName,
            periodStart = "01 $period",
            periodEnd = "30 $period",
            totalCalls = totalCalls,
            billableMinutes = billableMinutes,
            aiCharges = totalAiUnits / 100.0,
            telecomCharges = 0.0,
            taxes = totalTaxUnits / 100.0,
            totalAmount = totalNetUnits / 100.0,
            currency = currency,
            status = "PAID"
        )
    }
}

data class CallChargeBreakdown(
    val actualSecs: Int,
    val billableSecs: Int,
    val baseAiChargeUnits: Long,
    val telephonyChargeUnits: Long,
    val taxUnits: Long,
    val totalUnits: Long
)
