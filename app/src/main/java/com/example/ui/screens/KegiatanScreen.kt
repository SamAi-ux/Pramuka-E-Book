package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoutActivity
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatStatCard
import com.example.ui.components.SneatTableCell
import com.example.ui.components.SneatTableColumnDivider
import com.example.ui.components.SneatTableContainer
import com.example.ui.components.SneatTableDataRow
import com.example.ui.components.SneatTableHeaderCell
import com.example.ui.components.SneatTableHeaderRow
import com.example.ui.components.SneatTopBar
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutBrownContainer
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatInfo
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun KegiatanScreen(
    activities: List<ScoutActivity>,
    onToggleRegister: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedActivityForDetail by remember { mutableStateOf<ScoutActivity?>(null) }

    val baseCategories = listOf("Latihan Rutin", "Perkemahan", "Lomba", "Bakti Sosial")
    val categories = remember(activities) {
        listOf("Semua") + (baseCategories + activities.map { it.category }.filter { it.isNotBlank() }).distinct()
    }

    val filteredActivities = activities.filter {
        selectedCategory == "Semua" || it.category == selectedCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .testTag("kegiatan_screen_container")
    ) {
        SneatTopBar(
            title = "Kegiatan Pramuka",
            subtitle = "Agenda Latihan, Perkemahan & Perlombaan",
            onBackClick = onBack
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScoutGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("chip_category_$cat")
                )
            }
        }

        // Activities Grid + Table
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2x2 Summary Columns
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SneatStatCard(
                            title = "Total Agenda",
                            value = "${activities.size}",
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            iconBgColor = ScoutGreenContainer,
                            iconTint = ScoutGreen,
                            modifier = Modifier.weight(1f)
                        )
                        SneatStatCard(
                            title = "Sudah Terdaftar",
                            value = "${activities.count { it.isUserRegistered }}",
                            icon = Icons.Default.Check,
                            iconBgColor = ScoutYellowContainer,
                            iconTint = ScoutBrown,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SneatStatCard(
                            title = "Agenda Aktif",
                            value = "${activities.count { it.status != "Selesai" }}",
                            icon = Icons.Default.DateRange,
                            iconBgColor = ScoutBrownContainer,
                            iconTint = ScoutBrown,
                            modifier = Modifier.weight(1f)
                        )
                        SneatStatCard(
                            title = "Total Kuota",
                            value = "${activities.sumOf { it.quota }}",
                            icon = Icons.Default.Group,
                            iconBgColor = Color(0xFFE0F2FE),
                            iconTint = SneatInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Table Header Label
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tabel Jadwal Kegiatan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SneatTextPrimary
                        )
                    )
                    Text(
                        text = "${filteredActivities.size} agenda • Ketuk baris untuk detail",
                        fontSize = 11.sp,
                        color = SneatTextSecondary
                    )
                }
            }

            // Structured Table of Activities
            item {
                SneatTableContainer(modifier = Modifier.testTag("kegiatan_table")) {
                    SneatTableHeaderRow {
                        SneatTableHeaderCell(text = "Kegiatan", weight = 0.38f)
                        SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                        SneatTableHeaderCell(text = "Jadwal & Lokasi", weight = 0.34f)
                        SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                        SneatTableHeaderCell(
                            text = "Kuota & Aksi",
                            weight = 0.28f,
                            alignment = Alignment.Center
                        )
                    }

                    if (filteredActivities.isEmpty()) {
                        SneatTableDataRow(index = 0) {
                            SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                                Text(
                                    text = "Belum ada kegiatan untuk kategori ini.",
                                    fontSize = 12.sp,
                                    color = SneatTextSecondary
                                )
                            }
                        }
                    } else {
                        filteredActivities.forEachIndexed { index, activity ->
                            SneatTableDataRow(
                                index = index,
                                modifier = Modifier.testTag("activity_card_${activity.id}"),
                                onClick = { selectedActivityForDetail = activity }
                            ) {
                                SneatTableCell(weight = 0.38f) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = activity.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SneatTextPrimary,
                                            lineHeight = 15.sp
                                        )
                                        SneatBadge(
                                            text = activity.category,
                                            backgroundColor = ScoutYellowContainer,
                                            textColor = ScoutBrown
                                        )
                                    }
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.34f) {
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = activity.date,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = ScoutGreen
                                        )
                                        Text(
                                            text = activity.time,
                                            fontSize = 10.sp,
                                            color = SneatTextSecondary
                                        )
                                        Text(
                                            text = activity.location,
                                            fontSize = 10.sp,
                                            color = ScoutBrown,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.28f, alignment = Alignment.Center) {
                                    val isFinished = activity.status == "Selesai"
                                    val isFull = !activity.isUserRegistered && activity.registeredCount >= activity.quota
                                    val canClick = activity.isUserRegistered || (!isFinished && !isFull)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${activity.registeredCount}/${activity.quota}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SneatTextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = { onToggleRegister(activity.id) },
                                            enabled = canClick,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = when {
                                                    activity.isUserRegistered -> SneatSuccess
                                                    isFinished || isFull -> Color(0xFF9CA3AF)
                                                    else -> ScoutGreen
                                                },
                                                disabledContainerColor = Color(0xFFD6CEC2),
                                                disabledContentColor = SneatTextSecondary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("btn_register_activity_${activity.id}")
                                        ) {
                                            when {
                                                activity.isUserRegistered -> {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Ikut", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                isFinished -> {
                                                    Text("Selesai", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                isFull -> {
                                                    Text("Penuh", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                else -> {
                                                    Text("Daftar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
        }
    }

    // Detail Dialog
    selectedActivityForDetail?.let { act ->
        val isFinished = act.status == "Selesai"
        val isFull = !act.isUserRegistered && act.registeredCount >= act.quota
        val canClick = act.isUserRegistered || (!isFinished && !isFull)

        AlertDialog(
            onDismissRequest = { selectedActivityForDetail = null },
            containerColor = com.example.ui.theme.SneatSurface,
            title = {
                Text(
                    text = act.title,
                    fontWeight = FontWeight.Bold,
                    color = ScoutGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SneatBadge(
                        text = act.category,
                        backgroundColor = ScoutYellowContainer,
                        textColor = ScoutBrown
                    )
                    Text(
                        text = act.description,
                        fontSize = 12.sp,
                        color = SneatTextSecondary,
                        lineHeight = 18.sp
                    )
                    SneatTableContainer {
                        DetailTableEntry(index = 0, label = "Waktu", value = "${act.date} (${act.time})")
                        DetailTableEntry(index = 1, label = "Lokasi", value = act.location)
                        DetailTableEntry(index = 2, label = "Pembina", value = act.leader)
                        DetailTableEntry(index = 3, label = "Kuota", value = "${act.registeredCount} dari ${act.quota} terisi")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleRegister(act.id)
                        selectedActivityForDetail = null
                    },
                    enabled = canClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (act.isUserRegistered) SneatSuccess else ScoutGreen
                    )
                ) {
                    Text(
                        when {
                            act.isUserRegistered -> "Batalkan Pendaftaran"
                            isFinished -> "Kegiatan Selesai"
                            isFull -> "Kuota Penuh"
                            else -> "Daftar Kegiatan Ini"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedActivityForDetail = null }) {
                    Text("Tutup", color = SneatTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DetailTableEntry(index: Int, label: String, value: String) {
    SneatTableDataRow(index = index) {
        SneatTableCell(weight = 0.34f) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ScoutBrown
            )
        }
        SneatTableColumnDivider()
        SneatTableCell(weight = 0.66f) {
            Text(
                text = value,
                fontSize = 11.sp,
                color = SneatTextPrimary
            )
        }
    }
}
