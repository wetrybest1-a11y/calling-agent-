package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Contact
import com.example.data.models.Currency
import com.example.data.models.LeadStatus
import com.example.data.models.Organization
import com.example.ui.components.LeadStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    currentOrg: Organization?,
    contacts: List<Contact>,
    onAddContact: (Contact) -> Unit,
    onImportCsv: (String) -> Int,
    onToggleDnd: (String) -> Unit,
    onInitiateCallToContact: (Contact) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var selectedContactForDetail by remember { mutableStateOf<Contact?>(null) }

    val filteredContacts = contacts.filter { contact ->
        val matchesSearch = contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phone.contains(searchQuery, ignoreCase = true) ||
                contact.company.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "QUALIFIED" -> contact.status == LeadStatus.QUALIFIED
            "INTERESTED" -> contact.status == LeadStatus.INTERESTED
            "NEW" -> contact.status == LeadStatus.NEW
            "DND" -> contact.isDndSuppressed || contact.status == LeadStatus.DO_NOT_CALL
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Indigo600,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Contact", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_contact_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header with Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Contacts & CRM Pipeline",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${contacts.size} total contacts in workspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                FilledTonalButton(
                    onClick = { showCsvDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("import_csv_button")
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import CSV")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, phone or company...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "ALL" to "All (${contacts.size})",
                    "QUALIFIED" to "Qualified (${contacts.count { it.status == LeadStatus.QUALIFIED }})",
                    "INTERESTED" to "Interested (${contacts.count { it.status == LeadStatus.INTERESTED }})",
                    "NEW" to "New Leads (${contacts.count { it.status == LeadStatus.NEW }})",
                    "DND" to "DND Opt-Out (${contacts.count { it.isDndSuppressed }})"
                )

                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Indigo50,
                            selectedLabelColor = Indigo600
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contact List
            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            tint = Slate300,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "No contacts in this category" else "No matching contacts found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredContacts) { contact ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContactForDetail = contact }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (contact.isDndSuppressed) Rose50 else Indigo50),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = contact.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (contact.isDndSuppressed) Rose600 else Indigo600
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = contact.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${contact.company} • ${contact.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate500
                                            )
                                        }
                                    }

                                    LeadStatusBadge(contact.status)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Actions: Call Lead & DND Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = Slate400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${contact.totalCalls} calls (${contact.totalTalkTimeSecs}s talk time)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // DND toggle button
                                        IconButton(
                                            onClick = { onToggleDnd(contact.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (contact.isDndSuppressed) Icons.Default.DoDisturbOn else Icons.Default.DoDisturbOff,
                                                contentDescription = "Toggle DND Consent",
                                                tint = if (contact.isDndSuppressed) Rose600 else Slate400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Call Now button (strictly disabled if DND)
                                        Button(
                                            onClick = { onInitiateCallToContact(contact) },
                                            enabled = !contact.isDndSuppressed,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Indigo600,
                                                disabledContainerColor = Slate200
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (contact.isDndSuppressed) "DND Opted Out" else "AI Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        AddContactDialog(
            orgId = currentOrg?.id ?: "org_default",
            onDismiss = { showAddDialog = false },
            onSave = { newContact ->
                onAddContact(newContact)
                showAddDialog = false
            }
        )
    }

    // CSV Import Dialog
    if (showCsvDialog) {
        CsvImportDialog(
            onDismiss = { showCsvDialog = false },
            onConfirmImport = { csvText ->
                onImportCsv(csvText)
                showCsvDialog = false
            }
        )
    }

    // Contact Details Modal
    selectedContactForDetail?.let { contact ->
        ContactDetailDialog(
            contact = contact,
            currency = currentOrg?.currency ?: Currency.INR,
            onDismiss = { selectedContactForDetail = null }
        )
    }
}

@Composable
fun AddContactDialog(
    orgId: String,
    onDismiss: () -> Unit,
    onSave: (Contact) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var email by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var leadSource by remember { mutableStateOf("Manual Entry") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_name_field")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (with Country Code)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_phone_field")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.length > 5) {
                        onSave(
                            Contact(
                                orgId = orgId,
                                name = name,
                                phone = phone,
                                email = email.ifBlank { "lead@example.com" },
                                company = company.ifBlank { "Independent" },
                                leadSource = leadSource
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Save Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    onConfirmImport: (String) -> Unit
) {
    var sampleCsvText by remember {
        mutableStateOf(
            """name,phone,email,company
Sunil Mehta,+91 98220 11223,sunil@mehtalogistics.in,Mehta Logistics
Deepa Rao,+91 99887 76655,deepa@cloudmatrix.com,CloudMatrix
Arjun Singhania,+91 98450 99887,arjun@singhaniacapital.com,Singhania Capital
Kavita Sen,+91 97110 33445,kavita@edutechlead.org,EduTech Global"""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bulk Contact Import (CSV)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Paste or edit CSV records below (Columns: name, phone, email, company). Duplicates will be automatically suppressed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                OutlinedTextField(
                    value = sampleCsvText,
                    onValueChange = { sampleCsvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmImport(sampleCsvText) },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Import Leads")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ContactDetailDialog(
    contact: Contact,
    currency: Currency,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(contact.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(contact.company, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    }
                    LeadStatusBadge(contact.status)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text("Phone: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(contact.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        Text("Email: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(contact.email, style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        Text("Total Calls: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text("${contact.totalCalls} (${contact.totalTalkTimeSecs}s talk time)", style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        Text("Last Call Outcome: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(contact.lastCallOutcome, style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        Text("DND Status: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(if (contact.isDndSuppressed) "Opted Out (Calls Blocked)" else "Active / Permitted", color = if (contact.isDndSuppressed) Rose600 else Emerald600, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
