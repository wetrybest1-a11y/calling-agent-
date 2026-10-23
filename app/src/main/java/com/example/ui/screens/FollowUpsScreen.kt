package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FollowUpTask
import com.example.data.models.TaskStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FollowUpsScreen(
    tasks: List<FollowUpTask>,
    onCompleteTask: (String) -> Unit,
    onCallTaskLead: (FollowUpTask) -> Unit
) {
    val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
    val completedTasks = tasks.filter { it.status == TaskStatus.EXECUTED }

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
                        text = "Automated Follow-ups & Callback Queue",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Scheduled AI callbacks and automated lead retry sequences",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        }

        // Info Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Violet50,
                border = androidx.compose.foundation.BorderStroke(1.dp, Violet600)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Violet600, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Autonomous Follow-up Engine", fontWeight = FontWeight.Bold, color = Violet600, style = MaterialTheme.typography.titleSmall)
                        Text("Tasks execute automatically during business hours (9 AM - 7 PM). Contacts that opted out to DND are skipped automatically.", style = MaterialTheme.typography.bodySmall, color = Slate700, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text("Pending Callbacks (${pendingTasks.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        if (pendingTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No pending callbacks in queue", color = Slate500)
                }
            }
        } else {
            items(pendingTasks) { task ->
                val dateFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val timeStr = dateFmt.format(Date(task.scheduledAt))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(task.contactName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(task.contactPhone, style = MaterialTheme.typography.bodySmall, color = Slate500)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Amber50
                            ) {
                                Text(
                                    text = "Due: $timeStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber500,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Reason: ${task.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onCompleteTask(task.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Mark Done")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onCallTaskLead(task) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Lead Now")
                            }
                        }
                    }
                }
            }
        }
    }
}
