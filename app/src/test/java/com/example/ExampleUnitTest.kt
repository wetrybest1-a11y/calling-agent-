package com.example

import com.example.data.models.*
import com.example.data.repository.CallPilotRepository
import com.example.domain.billing.BillingRateConfig
import com.example.domain.billing.UsageBillingEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPerSecondBilling_connectedCall() {
        val billingEngine = UsageBillingEngine(
            rateConfig = BillingRateConfig(
                inrRatePerMinutePaise = 200L, // ₹2.00 per minute
                taxPercentage = 18.0
            )
        )

        // 30 seconds call
        val breakdown = billingEngine.calculateCallCharge(
            status = CallStatus.COMPLETED,
            durationSecs = 30,
            currency = Currency.INR
        )

        assertEquals(30, breakdown.billableSecs)
        // 30/60 * 200 = 100 paise (₹1.00)
        assertEquals(100L, breakdown.baseAiChargeUnits)
        // 18% GST on 100 paise = 18 paise
        assertEquals(18L, breakdown.taxUnits)
        assertEquals(118L, breakdown.totalUnits)
    }

    @Test
    fun testBilling_unansweredCall_costsZero() {
        val billingEngine = UsageBillingEngine()

        val breakdown = billingEngine.calculateCallCharge(
            status = CallStatus.NO_ANSWER,
            durationSecs = 20,
            currency = Currency.INR
        )

        assertEquals(0L, breakdown.totalUnits)
        assertEquals(0, breakdown.billableSecs)
    }

    @Test
    fun testBilling_failedCall_costsZero() {
        val billingEngine = UsageBillingEngine()

        val breakdown = billingEngine.calculateCallCharge(
            status = CallStatus.FAILED,
            durationSecs = 10,
            currency = Currency.INR
        )

        assertEquals(0L, breakdown.totalUnits)
        assertEquals(0, breakdown.billableSecs)
    }

    @Test
    fun testCsvImport_deduplicatesPhones() {
        val repo = CallPilotRepository()
        val initialCount = repo.contacts.value.size

        val csv = """name,phone,email,company
New Person,+91 99999 88888,new@test.com,New Co
Duplicate Person,+91 99999 88888,dup@test.com,Dup Co
Another Person,+91 99999 77777,another@test.com,Another Co"""

        val imported = repo.importContactsFromCsv(csv, "org_test")
        assertEquals(2, imported)
        assertEquals(initialCount + 2, repo.contacts.value.size)
    }

    @Test
    fun testDndSuppression_toggle() {
        val repo = CallPilotRepository()
        val contact = repo.contacts.value.first { !it.isDndSuppressed }

        repo.toggleContactDnd(contact.id)
        val updated = repo.contacts.value.first { it.id == contact.id }

        assertTrue(updated.isDndSuppressed)
        assertEquals(LeadStatus.DO_NOT_CALL, updated.status)
    }
}
