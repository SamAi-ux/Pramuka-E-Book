package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScoutSampleData
import com.example.model.LeaderContact
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatStatCard
import com.example.ui.components.SneatTableCell
import com.example.ui.components.SneatTableColumnDivider
import com.example.ui.components.SneatTableContainer
import com.example.ui.components.SneatTableDataRow
import com.example.ui.components.SneatTableHeaderCell
import com.example.ui.components.SneatTableHeaderRow
import com.example.ui.components.SneatTopBar
import com.example.ui.components.scoutInputTextStyle
import com.example.ui.components.scoutTextFieldColors
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatInfo
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatSurfaceVariant
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun PusatBantuanScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: FAQ, 1: Kontak Pembina, 2: Laporan Masalah
    val expandedFaqs = remember { mutableStateMapOf<String, Boolean>() }
    val context = LocalContext.current

    // Contact modal
    var activeContactDialog by remember { mutableStateOf<LeaderContact?>(null) }

    // Report form
    var reportName by remember { mutableStateOf("") }
    var reportSubject by remember { mutableStateOf("") }
    var reportMessage by remember { mutableStateOf("") }
    var reportError by remember { mutableStateOf<String?>(null) }
    var reportSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .imePadding()
            .testTag("pusat_bantuan_screen_container")
    ) {
        SneatTopBar(
            title = "Pusat Bantuan",
            subtitle = "Layanan Terbuka Bebas Akses Untuk Semua Anggota",
            onBackClick = onBack
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SneatSurface,
            contentColor = ScoutGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ScoutGreen,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "FAQ Pramuka",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) ScoutGreen else SneatTextSecondary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_faq")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Kontak Pembina",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) ScoutGreen else SneatTextSecondary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_kontak_pembina")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "Laporan Masalah",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 2) ScoutGreen else SneatTextSecondary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_laporan_masalah")
            )
        }

        when (selectedTab) {
            0 -> {
                // FAQ Table
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = SneatInfo)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tabel tanya jawab resmi seputar kepramukaan & syarat Pramuka Garuda. Ketuk baris untuk melihat jawaban.",
                                    fontSize = 11.sp,
                                    color = SneatTextPrimary
                                )
                            }
                        }
                    }

                    item {
                        SneatTableContainer(modifier = Modifier.testTag("faq_table")) {
                            SneatTableHeaderRow {
                                SneatTableHeaderCell(text = "Kategori", weight = 0.28f)
                                SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                                SneatTableHeaderCell(text = "Pertanyaan & Jawaban", weight = 0.72f)
                            }

                            ScoutSampleData.faqs.forEachIndexed { index, faq ->
                                val isExpanded = expandedFaqs[faq.id] ?: false
                                SneatTableDataRow(
                                    index = index,
                                    onClick = { expandedFaqs[faq.id] = !isExpanded }
                                ) {
                                    SneatTableCell(weight = 0.28f) {
                                        SneatBadge(
                                            text = faq.category,
                                            backgroundColor = ScoutYellowContainer,
                                            textColor = ScoutBrown
                                        )
                                    }
                                    SneatTableColumnDivider()
                                    SneatTableCell(weight = 0.72f) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = faq.question,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = SneatTextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = ScoutGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            AnimatedVisibility(visible = isExpanded) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(SneatSurfaceVariant)
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        text = faq.answer,
                                                        fontSize = 11.sp,
                                                        lineHeight = 16.sp,
                                                        color = SneatTextPrimary
                                                    )
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
            1 -> {
                // Kontak Pembina Table
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Tabel Direktori Pembina Siaga",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SneatTextPrimary
                        )
                    }

                    item {
                        SneatTableContainer(modifier = Modifier.testTag("contacts_table")) {
                            SneatTableHeaderRow {
                                SneatTableHeaderCell(text = "Pembina & Jabatan", weight = 0.38f)
                                SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                                SneatTableHeaderCell(text = "Pangkalan & Siaga", weight = 0.34f)
                                SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                                SneatTableHeaderCell(text = "Kontak", weight = 0.28f, alignment = Alignment.Center)
                            }

                            ScoutSampleData.leaderContacts.forEachIndexed { index, leader ->
                                SneatTableDataRow(index = index) {
                                    SneatTableCell(weight = 0.38f) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = leader.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = SneatTextPrimary
                                            )
                                            SneatBadge(
                                                text = leader.role,
                                                backgroundColor = ScoutYellowContainer,
                                                textColor = ScoutBrown
                                            )
                                        }
                                    }
                                    SneatTableColumnDivider()
                                    SneatTableCell(weight = 0.34f) {
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(
                                                text = leader.gudep,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ScoutGreen
                                            )
                                            Text(
                                                text = leader.availability,
                                                fontSize = 10.sp,
                                                color = SneatTextSecondary,
                                                lineHeight = 13.sp
                                            )
                                        }
                                    }
                                    SneatTableColumnDivider()
                                    SneatTableCell(weight = 0.28f, alignment = Alignment.Center) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { activeContactDialog = leader },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Call,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Hubungi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { activeContactDialog = leader },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Email,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Surel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Laporan Masalah / Form Bantuan
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 2-Column SLA Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SneatStatCard(
                                title = "Waktu Respon",
                                value = "< 24 Jam",
                                icon = Icons.Default.SupportAgent,
                                iconBgColor = ScoutGreenContainer,
                                iconTint = ScoutGreen,
                                modifier = Modifier.weight(1f)
                            )
                            SneatStatCard(
                                title = "Status Akses",
                                value = "Gratis",
                                icon = Icons.Default.CheckCircle,
                                iconBgColor = ScoutYellowContainer,
                                iconTint = ScoutBrown,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SneatSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(SneatBorder)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Formulir Laporan / Pertanyaan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ScoutGreen
                                )
                                Text(
                                    text = "Kirim masukan, pertanyaan seputar SKU, atau kendala aplikasi ke Dewan Pembina.",
                                    fontSize = 11.sp,
                                    color = SneatTextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = reportName,
                                    onValueChange = {
                                        reportName = it
                                        reportError = null
                                    },
                                    label = { Text("Nama Pengirim") },
                                    placeholder = { Text("Nama lengkap Anda") },
                                    singleLine = true,
                                    textStyle = scoutInputTextStyle,
                                    colors = scoutTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().testTag("input_report_name")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = reportSubject,
                                    onValueChange = {
                                        reportSubject = it
                                        reportError = null
                                    },
                                    label = { Text("Perihal / Topik *") },
                                    placeholder = { Text("Misal: Kendala Ujian SKU TKK") },
                                    singleLine = true,
                                    textStyle = scoutInputTextStyle,
                                    colors = scoutTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().testTag("input_report_subject")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = reportMessage,
                                    onValueChange = {
                                        reportMessage = it
                                        reportError = null
                                    },
                                    label = { Text("Isi Pesan / Laporan *") },
                                    placeholder = { Text("Tuliskan secara jelas kendala atau pertanyaan Anda...") },
                                    textStyle = scoutInputTextStyle,
                                    colors = scoutTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_report_message")
                                )

                                if (reportError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = reportError ?: "",
                                        fontSize = 11.sp,
                                        color = com.example.ui.theme.SneatDanger,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        if (reportSubject.isBlank()) {
                                            reportError = "Perihal / topik laporan wajib diisi."
                                        } else if (reportMessage.isBlank()) {
                                            reportError = "Isi pesan / laporan tidak boleh kosong."
                                        } else {
                                            reportError = null
                                            reportSuccessDialog = true
                                            reportMessage = ""
                                            reportSubject = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_send_report"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim ke Dewan Pembina", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Contact Dialog
    activeContactDialog?.let { leader ->
        AlertDialog(
            onDismissRequest = { activeContactDialog = null },
            containerColor = SneatSurface,
            icon = {
                Icon(
                    Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = ScoutGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = leader.name,
                    fontWeight = FontWeight.Bold,
                    color = ScoutGreen,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SneatTableContainer {
                        ContactDetailRow(index = 0, label = "Peran", value = leader.role)
                        ContactDetailRow(index = 1, label = "Pangkalan", value = leader.gudep)
                        ContactDetailRow(index = 2, label = "Telepon", value = leader.phone)
                        ContactDetailRow(index = 3, label = "Email", value = leader.email)
                    }
                    Text(
                        text = "Silakan hubungi pada jam siaga untuk konsultasi pembinaan dan pengujian SKU.",
                        fontSize = 11.sp,
                        color = SneatTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val dialNumber = leader.phone.replace(Regex("[^+0-9]"), "")
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dialNumber"))
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Telepon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${leader.email}"))
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeContactDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
                ) {
                    Text("Tutup")
                }
            }
        )
    }

    // Report Sent Success Dialog
    if (reportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { reportSuccessDialog = false },
            containerColor = SneatSurface,
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SneatSuccess,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text("Laporan Berhasil Terkirim!", fontWeight = FontWeight.Bold, color = ScoutGreen)
            },
            text = {
                Text(
                    "Terima kasih atas laporan Anda. Kakak Pembina Gugus Depan akan merespons pesan Anda dalam waktu 1x24 jam.",
                    fontSize = 12.sp,
                    color = SneatTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { reportSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
                ) {
                    Text("Selesai")
                }
            }
        )
    }
}

@Composable
private fun ContactDetailRow(index: Int, label: String, value: String) {
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
