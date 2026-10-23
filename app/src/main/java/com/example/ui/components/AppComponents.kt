package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallPilotTopBar(
    currentOrg: Organization?,
    allOrgs: List<Organization>,
    currentRole: UserRole,
    onSelectOrg: (String) -> Unit,
    onRoleToggle: (UserRole) -> Unit,
    onTopUpClick: () -> Unit,
    onOpenNavDrawer: () -> Unit
) {
    var showOrgDropdown by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Nav Drawer Menu & Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenNavDrawer,
                        modifier = Modifier.testTag("nav_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Indigo600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CallPilot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Indigo600,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Indigo50)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Organization selector dropdown trigger
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showOrgDropdown = true }
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = currentOrg?.name ?: "Select Workspace",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Organization",
                                tint = Slate500,
                                modifier = Modifier.size(16.dp)
                            )

                            DropdownMenu(
                                expanded = showOrgDropdown,
                                onDismissRequest = { showOrgDropdown = false }
                            ) {
                                allOrgs.forEach { org ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(org.name, fontWeight = if (org.id == currentOrg?.id) FontWeight.Bold else FontWeight.Normal)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = org.currency.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Slate500
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectOrg(org.id)
                                            showOrgDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Right: Role Switcher & Wallet Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Badge
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when (currentRole) {
                                UserRole.SUPER_ADMIN -> Violet50
                                UserRole.BUSINESS_OWNER -> Indigo50
                                UserRole.SALES_AGENT -> Slate100
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (currentRole) {
                                    UserRole.SUPER_ADMIN -> Violet600
                                    UserRole.BUSINESS_OWNER -> Indigo600
                                    UserRole.SALES_AGENT -> Slate300
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showRoleDropdown = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val roleText = when (currentRole) {
                                    UserRole.SUPER_ADMIN -> "Super Admin"
                                    UserRole.BUSINESS_OWNER -> "Owner"
                                    UserRole.SALES_AGENT -> "Sales Rep"
                                }
                                Text(
                                    text = roleText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentRole) {
                                        UserRole.SUPER_ADMIN -> Violet600
                                        UserRole.BUSINESS_OWNER -> Indigo600
                                        UserRole.SALES_AGENT -> Slate700
                                    }
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Role",
                                    tint = Slate600,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false }
                        ) {
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (role) {
                                                UserRole.SUPER_ADMIN -> "Super Admin (Platform Owner)"
                                                UserRole.BUSINESS_OWNER -> "Business Owner"
                                                UserRole.SALES_AGENT -> "Sales Agent / Member"
                                            },
                                            fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onRoleToggle(role)
                                        showRoleDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Wallet Balance Chip
                    if (currentOrg != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Emerald50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onTopUpClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("wallet_balance_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val balanceStr = when (currentOrg.currency) {
                                    Currency.INR -> String.format("₹%.2f", currentOrg.creditBalanceUnits / 100.0)
                                    Currency.USD -> String.format("$%.2f", currentOrg.creditBalanceUnits / 100.0)
                                }
                                Text(
                                    text = balanceStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald600
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Top up",
                                    tint = Emerald600,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun LeadStatusBadge(status: LeadStatus) {
    val (bgColor, textColor, label) = when (status) {
        LeadStatus.NEW -> Triple(Cyan50, Cyan500, "New Lead")
        LeadStatus.CONTACTED -> Triple(Indigo50, Indigo600, "Contacted")
        LeadStatus.INTERESTED -> Triple(Amber50, Amber500, "Interested")
        LeadStatus.QUALIFIED -> Triple(Emerald50, Emerald600, "Qualified")
        LeadStatus.FOLLOW_UP_SCHEDULED -> Triple(Violet50, Violet600, "Follow-up")
        LeadStatus.DO_NOT_CALL -> Triple(Rose50, Rose600, "DND / Opt-out")
        LeadStatus.CLOSED_WON -> Triple(Emerald100, Emerald600, "Won")
        LeadStatus.UNRESPONSIVE -> Triple(Slate100, Slate500, "Unresponsive")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun CallStatusBadge(status: CallStatus) {
    val (bgColor, textColor, label) = when (status) {
        CallStatus.QUEUED -> Triple(Slate100, Slate500, "Queued")
        CallStatus.INITIATING -> Triple(Amber50, Amber500, "Initiating")
        CallStatus.RINGING -> Triple(Cyan50, Cyan500, "Ringing")
        CallStatus.CONNECTED -> Triple(Emerald50, Emerald600, "Connected")
        CallStatus.COMPLETED -> Triple(Indigo50, Indigo600, "Completed")
        CallStatus.BUSY -> Triple(Amber50, Amber500, "Busy")
        CallStatus.NO_ANSWER -> Triple(Rose50, Rose600, "No Answer")
        CallStatus.REJECTED -> Triple(Rose50, Rose600, "Rejected")
        CallStatus.FAILED -> Triple(Rose50, Rose600, "Failed")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
