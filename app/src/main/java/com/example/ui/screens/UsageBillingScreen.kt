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
import com.example.domain.billing.UsageBillingEngine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UsageBillingScreen(
    currentOrg: Organization?,
    ledgerEntries: List<UsageLedgerEntry>,
    billingEngine: UsageBillingEngine,
    onRecharge: (Long, String) -> Unit
) {
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }

    val currency = currentOrg?.currency ?: Currency.INR
    val balanceStr = billingEngine.formatCurrencyAmount(currentOrg?.creditBalanceUnits ?: 0L, currency)
    val totalSpentStr = billingEngine.formatCurrencyAmount(currentOrg?.totalSpentLifetime ?: 0L, currency)
    val totalBillableSecs = ledgerEntries.sumOf { it.billableDurationSecs }
    val totalBillableMins = String.format(Locale.getDefault(), "%.1f", totalBillableSecs / 60.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        text = "Usage Ledger & Billing",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Transparent pay-as-you-use metering • ₹0 monthly subscription fees",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                Button(
                    onClick = { showTopUpDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    modifier = Modifier.testTag("add_credits_btn")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Credits", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Wallet Balance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE PREPAID WALLET BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald50.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Metered per-second",
                                color = Emerald600,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = balanceStr,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Slate700)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Consumption", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(totalSpentStr, fontWeight = FontWeight.Bold, color = PureWhite, style = MaterialTheme.typography.titleMedium)
                        }

                        Column {
                            Text("Billable Minutes", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text("${totalBillableMins} mins", fontWeight = FontWeight.Bold, color = PureWhite, style = MaterialTheme.typography.titleMedium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Billing Mode", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(currentOrg?.billingMode?.name ?: "PREPAID", fontWeight = FontWeight.Bold, color = Indigo200, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        // Configured Pricing Rule Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currency == Currency.INR) "Active Rate: ₹2.00 per connected minute" else "Active Rate: $0.05 per connected minute",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currency == Currency.INR) "3.33 paise per billable second • Unanswered calls cost ₹0.00" else "0.083 cents per billable second • Unanswered calls cost $0.00",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    OutlinedButton(
                        onClick = { showInvoiceDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Invoice")
                    }
                }
            }
        }

        // Usage Ledger Table Header
        item {
            Text(
                text = "Itemized Usage Ledger (${ledgerEntries.size} entries)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (ledgerEntries.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No billing events recorded yet. Connect a call to see live deductions.", color = Slate500)
                }
            }
        } else {
            items(ledgerEntries) { entry ->
                val dateFmt = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault())
                val timeStr = dateFmt.format(Date(entry.timestamp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Indigo50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Call Usage • ${entry.billableDurationSecs}s connected",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "$timeStr • Provider: ${entry.provider}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val amountStr = billingEngine.formatCurrencyAmount(entry.finalChargeUnits, currency)
                            Text(
                                text = "-$amountStr",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Rose600
                            )
                            Text(
                                text = entry.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTopUpDialog) {
        TopUpCreditsModal(
            currency = currency,
            onDismiss = { showTopUpDialog = false },
            onConfirm = { amountUnits, gateway ->
                onRecharge(amountUnits, gateway)
                showTopUpDialog = false
            }
        )
    }

    if (showInvoiceDialog && currentOrg != null) {
        val invoice = billingEngine.generateMonthlyInvoice(
            orgId = currentOrg.id,
            orgName = currentOrg.name,
            ledgerEntries = ledgerEntries,
            currency = currency
        )
        InvoiceDetailModal(
            invoice = invoice,
            onDismiss = { showInvoiceDialog = false }
        )
    }
}

@Composable
fun TopUpCreditsModal(
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (Long, String) -> Unit
) {
    val presetAmounts = if (currency == Currency.INR) listOf(200L, 500L, 1000L, 2500L)
    else listOf(10L, 25L, 50L, 100L)

    var selectedAmount by remember { mutableLongStateOf(presetAmounts[1]) }
    var selectedGateway by remember { mutableStateOf(if (currency == Currency.INR) "Razorpay" else "Stripe") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Add Calling Credits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prepaid credits never expire. Deducted only when calls connect.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Text("Select Recharge Amount", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amt ->
                        val symbol = if (currency == Currency.INR) "₹" else "$"
                        FilterChip(
                            selected = selectedAmount == amt,
                            onClick = { selectedAmount = amt },
                            label = { Text("$symbol$amt", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Gateway Selector
                Text("Select Payment Gateway", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = selectedGateway == "Razorpay",
                        onClick = { selectedGateway = "Razorpay" },
                        label = { Text("Razorpay (UPI / NetBanking / Cards)") }
                    )
                    FilterChip(
                        selected = selectedGateway == "Stripe",
                        onClick = { selectedGateway = "Stripe" },
                        label = { Text("Stripe (Global Cards)") }
                    )
                }

                // Tax breakdown preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Calling Credits: ", style = MaterialTheme.typography.bodySmall)
                            val symbol = if (currency == Currency.INR) "₹" else "$"
                            Text("$symbol$selectedAmount.00", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        if (currency == Currency.INR) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GST (18% Input Credit): ", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                val gst = selectedAmount * 0.18
                                Text(String.format("₹%.2f", gst), style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val units = selectedAmount * 100L // convert to paise or cents
                            onConfirm(units, selectedGateway)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                    ) {
                        Text("Pay & Add Credits", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailModal(
    invoice: BillingInvoice,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TAX INVOICE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(invoice.invoiceNumber, style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Emerald50) {
                        Text("PAID", color = Emerald600, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                HorizontalDivider(color = Slate200)

                Text("Billed To: ${invoice.orgName}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text("Billing Period: ${invoice.periodStart} - ${invoice.periodEnd}", style = MaterialTheme.typography.bodySmall, color = Slate500)

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Connected Calls: ", style = MaterialTheme.typography.bodySmall)
                            Text("${invoice.totalCalls}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Billable Talk Time: ", style = MaterialTheme.typography.bodySmall)
                            Text(String.format("%.2f mins", invoice.billableMinutes), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("AI Calling Services: ", style = MaterialTheme.typography.bodySmall)
                            val symbol = if (invoice.currency == Currency.INR) "₹" else "$"
                            Text("$symbol${String.format("%.2f", invoice.aiCharges)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fixed Platform Monthly Fee: ", style = MaterialTheme.typography.bodySmall, color = Emerald600)
                            Text("₹0.00 (Free Forever)", fontWeight = FontWeight.Bold, color = Emerald600, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Applicable Taxes: ", style = MaterialTheme.typography.bodySmall)
                            val symbol = if (invoice.currency == Currency.INR) "₹" else "$"
                            Text("$symbol${String.format("%.2f", invoice.taxes)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Net Amount Billed: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    val symbol = if (invoice.currency == Currency.INR) "₹" else "$"
                    Text("$symbol${String.format("%.2f", invoice.totalAmount)}", fontWeight = FontWeight.Bold, color = Indigo600, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                ) {
                    Text("Close Invoice")
                }
            }
        }
    }
}
