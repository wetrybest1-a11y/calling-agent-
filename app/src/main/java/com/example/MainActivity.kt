package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.data.repository.CallPilotRepository
import com.example.ui.components.CallPilotTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    CONTACTS("Contacts & Leads", Icons.Default.Contacts),
    AGENTS("AI Voice Agents", Icons.Default.SmartToy),
    CAMPAIGNS("Call Campaigns", Icons.Default.Campaign),
    LIVE_CALL("Live Voice Calling", Icons.Default.PhoneInTalk),
    CALL_HISTORY("Call History", Icons.Default.History),
    FOLLOW_UPS("Follow-ups & Tasks", Icons.Default.Schedule),
    BILLING("Usage & Billing", Icons.Default.AccountBalanceWallet),
    INTEGRATIONS("Integrations", Icons.Default.CloudQueue),
    SUPER_ADMIN("Super-Admin Console", Icons.Default.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {

    private val repository = CallPilotRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CallPilotTheme {
                CallPilotApp(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallPilotApp(repository: CallPilotRepository) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember { mutableStateOf(AppDestination.OVERVIEW) }

    // Pre-selected parameters for live calling
    var activeContactForCall by remember { mutableStateOf<Contact?>(null) }
    var activeAgentForCall by remember { mutableStateOf<VoiceAgent?>(null) }

    // Repository reactive states
    val currentRole by repository.currentUserRole.collectAsStateWithLifecycle()
    val currentOrg by repository.currentOrg.collectAsStateWithLifecycle()
    val allOrgs by repository.organizations.collectAsStateWithLifecycle()
    val contacts by repository.contacts.collectAsStateWithLifecycle()
    val voiceAgents by repository.voiceAgents.collectAsStateWithLifecycle()
    val campaigns by repository.campaigns.collectAsStateWithLifecycle()
    val callRecords by repository.callRecords.collectAsStateWithLifecycle()
    val ledgerEntries by repository.ledgerEntries.collectAsStateWithLifecycle()
    val followUpTasks by repository.followUpTasks.collectAsStateWithLifecycle()
    val telephonyConfig by repository.telephonyConfig.collectAsStateWithLifecycle()

    // Android Predictive Back Navigation
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppDestination.OVERVIEW) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentScreen = AppDestination.OVERVIEW
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Drawer Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Indigo600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CallPilot",
                                style = MaterialTheme.typography.titleLarge,
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
                        Text(
                            text = "Usage-Based Calling CRM",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Items
                AppDestination.values().forEach { destination ->
                    // Gating: Only show Super Admin if user has SUPER_ADMIN role or selects it
                    val isSuperAdmin = currentRole == UserRole.SUPER_ADMIN
                    if (destination != AppDestination.SUPER_ADMIN || isSuperAdmin) {
                        val isSelected = currentScreen == destination
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Indigo600 else Slate600
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Indigo600 else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                currentScreen = destination
                                coroutineScope.launch { drawerState.close() }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Indigo50
                            ),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .testTag("nav_item_${destination.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Drawer Footer: ₹0 Subscription Promise
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PriceCheck, contentDescription = null, tint = Emerald600)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Zero Fixed Subscription",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald600
                            )
                            Text(
                                text = "Pay only for actual connected call seconds.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CallPilotTopBar(
                    currentOrg = currentOrg,
                    allOrgs = allOrgs,
                    currentRole = currentRole,
                    onSelectOrg = { orgId -> repository.selectOrganization(orgId) },
                    onRoleToggle = { role -> repository.setUserRole(role) },
                    onTopUpClick = { currentScreen = AppDestination.BILLING },
                    onOpenNavDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppDestination.OVERVIEW -> {
                        OverviewDashboardScreen(
                            currentOrg = currentOrg,
                            contacts = contacts,
                            voiceAgents = voiceAgents,
                            campaigns = campaigns,
                            callRecords = callRecords,
                            onNavigateToLiveCall = { currentScreen = AppDestination.LIVE_CALL },
                            onNavigateToContacts = { currentScreen = AppDestination.CONTACTS },
                            onNavigateToAgents = { currentScreen = AppDestination.AGENTS },
                            onNavigateToBilling = { currentScreen = AppDestination.BILLING },
                            onViewCallDetail = { currentScreen = AppDestination.CALL_HISTORY }
                        )
                    }
                    AppDestination.CONTACTS -> {
                        ContactsScreen(
                            currentOrg = currentOrg,
                            contacts = contacts,
                            onAddContact = { repository.addContact(it) },
                            onImportCsv = { csv -> repository.importContactsFromCsv(csv, currentOrg?.id ?: "org_default") },
                            onToggleDnd = { id -> repository.toggleContactDnd(id) },
                            onInitiateCallToContact = { contact ->
                                activeContactForCall = contact
                                currentScreen = AppDestination.LIVE_CALL
                            }
                        )
                    }
                    AppDestination.AGENTS -> {
                        VoiceAgentsScreen(
                            voiceAgents = voiceAgents,
                            onSaveAgent = { repository.saveVoiceAgent(it) },
                            onTestAgent = { agent ->
                                activeAgentForCall = agent
                                currentScreen = AppDestination.LIVE_CALL
                            }
                        )
                    }
                    AppDestination.CAMPAIGNS -> {
                        CampaignManagerScreen(
                            currentOrg = currentOrg,
                            campaigns = campaigns,
                            voiceAgents = voiceAgents,
                            contacts = contacts,
                            onSaveCampaign = { repository.saveCampaign(it) },
                            onUpdateStatus = { id, status -> repository.updateCampaignStatus(id, status) }
                        )
                    }
                    AppDestination.LIVE_CALL -> {
                        LiveCallConsoleScreen(
                            currentOrg = currentOrg,
                            contacts = contacts,
                            voiceAgents = voiceAgents,
                            preSelectedContact = activeContactForCall,
                            preSelectedAgent = activeAgentForCall,
                            billingEngine = repository.billingEngine,
                            onCallCompleted = { record, breakdown ->
                                repository.recordCallCompletion(record, breakdown)
                            },
                            onNavigateBack = { currentScreen = AppDestination.OVERVIEW }
                        )
                    }
                    AppDestination.CALL_HISTORY -> {
                        CallHistoryScreen(
                            currentOrg = currentOrg,
                            callRecords = callRecords,
                            onViewDetail = { /* Handled in dialog */ }
                        )
                    }
                    AppDestination.FOLLOW_UPS -> {
                        FollowUpsScreen(
                            tasks = followUpTasks,
                            onCompleteTask = { repository.completeFollowUp(it) },
                            onCallTaskLead = { task ->
                                val contact = contacts.find { it.id == task.contactId }
                                if (contact != null) {
                                    activeContactForCall = contact
                                    currentScreen = AppDestination.LIVE_CALL
                                }
                            }
                        )
                    }
                    AppDestination.BILLING -> {
                        UsageBillingScreen(
                            currentOrg = currentOrg,
                            ledgerEntries = ledgerEntries,
                            billingEngine = repository.billingEngine,
                            onRecharge = { amtUnits, gateway ->
                                repository.rechargeWallet(amtUnits, gateway)
                            }
                        )
                    }
                    AppDestination.INTEGRATIONS -> {
                        IntegrationsScreen(telephonyConfig = telephonyConfig)
                    }
                    AppDestination.SUPER_ADMIN -> {
                        SuperAdminScreen(
                            organizations = allOrgs,
                            billingEngine = repository.billingEngine,
                            telephonyConfig = telephonyConfig,
                            onToggleOrgStatus = { repository.toggleOrgStatus(it) },
                            onUpdateRates = { inr, usd -> repository.updateGlobalRates(inr, usd) },
                            onUpdateTelephonyConfig = { repository.updateTelephonyConfig(it) }
                        )
                    }
                }
            }
        }
    }
}
