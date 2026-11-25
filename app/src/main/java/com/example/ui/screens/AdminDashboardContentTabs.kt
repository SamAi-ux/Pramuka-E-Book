package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminConfigProfile
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.SemesterSyllabus
import com.example.ui.components.InstitutionLogoImage
import com.example.ui.components.ProfilePhotoSelectorSection
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatTableCell
import com.example.ui.components.SneatTableColumnDivider
import com.example.ui.components.SneatTableContainer
import com.example.ui.components.SneatTableDataRow
import com.example.ui.components.SneatTableHeaderCell
import com.example.ui.components.SneatTableHeaderRow
import com.example.ui.components.scoutInputTextStyle
import com.example.ui.components.scoutTextFieldColors
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatDanger
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun InstitutionIdentityTab(
    institutionConfig: InstitutionConfig,
    onSaveInstitution: (name: String, gudep: String, logoPath: String) -> Unit
) {
    var instName by remember(institutionConfig.name) { mutableStateOf(institutionConfig.name) }
    var instGudep by remember(institutionConfig.gudep) { mutableStateOf(institutionConfig.gudep) }
    var instLogoPath by remember(institutionConfig.logoPath) { mutableStateOf(institutionConfig.logoPath) }
    var formError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SneatSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ScoutGreen)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Logo Lembaga, Nama Lembaga & Nomor Gudep",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScoutGreen
                    )

                    // Live Preview Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ScoutGreenContainer)
                            .border(1.dp, ScoutGreen, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SneatSurface)
                                .border(1.5.dp, ScoutGreen, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            InstitutionLogoImage(
                                logoPath = instLogoPath,
                                contentDescription = "Pratinjau Logo Lembaga",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SneatBadge(
                                text = "PRATINJAU DI APLIKASI",
                                backgroundColor = ScoutGreen,
                                textColor = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = instName.ifBlank { "Nama Lembaga" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SneatTextPrimary
                            )
                            Text(
                                text = "Nomor Gudep: ${instGudep.ifBlank { "-" }}",
                                fontSize = 11.sp,
                                color = ScoutBrown,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    ProfilePhotoSelectorSection(
                        selectedAvatarId = 1,
                        customPhotoPath = instLogoPath,
                        onSelectPresetAvatar = {},
                        onCustomPhotoSelected = { path -> instLogoPath = path },
                        onClearCustomPhoto = { instLogoPath = "" },
                        persistedPhotoPath = institutionConfig.logoPath,
                        sectionTitle = "Logo Resmi Lembaga di Dalam Aplikasi:",
                        manualBoxTitle = "Upload Logo Lembaga (Choose File / Galeri)",
                        manualBoxDescription = "Logo lembaga yang dipilih akan tampil pada header Beranda dan Kartu Tanda Anggota (KTA).",
                        showPresetAvatars = false,
                        filePrefix = "institution_logo_"
                    )

                    OutlinedTextField(
                        value = instName,
                        onValueChange = { instName = it; formError = null },
                        label = { Text("Nama Lembaga / Pangkalan *") },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("input_inst_name")
                    )

                    OutlinedTextField(
                        value = instGudep,
                        onValueChange = { instGudep = it; formError = null },
                        label = { Text("Nomor Gudep *") },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("input_inst_gudep")
                    )

                    if (formError != null) {
                        Text(text = formError ?: "", fontSize = 11.sp, color = SneatDanger)
                    }

                    Button(
                        onClick = {
                            when {
                                instName.isBlank() -> formError = "Nama lembaga wajib diisi."
                                instGudep.isBlank() -> formError = "Nomor Gudep wajib diisi."
                                else -> onSaveInstitution(instName.trim(), instGudep.trim(), instLogoPath)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_institution")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Logo, Gudep & Nama Lembaga", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMaterialsTab(
    materials: List<ScoutMaterial>,
    onAddClick: () -> Unit,
    onEditClick: (ScoutMaterial) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kelola & Edit Materi (${materials.size})",
                    fontWeight = FontWeight.Bold,
                    color = SneatTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_material")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Materi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            SneatTableContainer(modifier = Modifier.testTag("admin_materials_table")) {
                SneatTableHeaderRow {
                    SneatTableHeaderCell(text = "Judul & Ringkasan", weight = 0.56f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Kategori", weight = 0.26f, alignment = Alignment.Center)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Edit", weight = 0.18f, alignment = Alignment.Center)
                }

                if (materials.isEmpty()) {
                    SneatTableDataRow(index = 0) {
                        SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                            Text(
                                text = "Belum ada modul materi. Ketuk Tambah Materi untuk memulai.",
                                fontSize = 11.sp,
                                color = SneatTextSecondary
                            )
                        }
                    }
                } else {
                    materials.forEachIndexed { index, mat ->
                        SneatTableDataRow(index = index) {
                            SneatTableCell(weight = 0.56f) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = mat.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SneatTextPrimary
                                    )
                                    Text(
                                        text = mat.summary,
                                        fontSize = 10.sp,
                                        color = SneatTextSecondary,
                                        maxLines = 2
                                    )
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.26f, alignment = Alignment.Center) {
                                SneatBadge(
                                    text = mat.category,
                                    backgroundColor = ScoutYellowContainer,
                                    textColor = ScoutBrown
                                )
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.18f, alignment = Alignment.Center) {
                                IconButton(
                                    onClick = { onEditClick(mat) },
                                    modifier = Modifier.testTag("btn_edit_material_${mat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Materi ${mat.title}",
                                        tint = ScoutGreen
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

@Composable
fun AdminSyllabusTab(
    syllabi: List<SemesterSyllabus>,
    onAddClick: () -> Unit,
    onEditClick: (SemesterSyllabus) -> Unit
) {
    val semesterOptions = remember(syllabi) {
        listOf("Semua") + syllabi.map { it.semester }.distinct()
    }
    var selectedSemester by remember { mutableStateOf("Semua") }
    val filtered = syllabi.filter { selectedSemester == "Semua" || it.semester == selectedSemester }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Program Kerja & Silabus Semester (${filtered.size})",
                    fontWeight = FontWeight.Bold,
                    color = SneatTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_syllabus")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Proker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(semesterOptions) { sem ->
                    val active = selectedSemester == sem
                    FilterChip(
                        selected = active,
                        onClick = { selectedSemester = sem },
                        label = {
                            Text(
                                text = sem,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScoutGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            SneatTableContainer(modifier = Modifier.testTag("admin_syllabus_table")) {
                SneatTableHeaderRow {
                    SneatTableHeaderCell(text = "Semester & Golongan", weight = 0.30f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Program Kerja & Target", weight = 0.52f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Aksi", weight = 0.18f, alignment = Alignment.Center)
                }

                if (filtered.isEmpty()) {
                    SneatTableDataRow(index = 0) {
                        SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                            Text(
                                text = "Belum ada program kerja untuk semester ini.",
                                fontSize = 11.sp,
                                color = SneatTextSecondary
                            )
                        }
                    }
                } else {
                    filtered.forEachIndexed { index, syl ->
                        SneatTableDataRow(index = index) {
                            SneatTableCell(weight = 0.30f) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = syl.semester,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = ScoutBrown
                                    )
                                    SneatBadge(
                                        text = syl.rank.title,
                                        backgroundColor = ScoutYellowContainer,
                                        textColor = ScoutBrown
                                    )
                                    Text(
                                        text = syl.weekRange,
                                        fontSize = 10.sp,
                                        color = SneatTextSecondary
                                    )
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.52f) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = syl.programTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SneatTextPrimary
                                    )
                                    Text(
                                        text = syl.competencyTarget,
                                        fontSize = 10.sp,
                                        color = SneatTextSecondary,
                                        lineHeight = 14.sp
                                    )
                                    Text(
                                        text = "Metode: ${syl.method} • ${syl.status}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ScoutGreen
                                    )
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.18f, alignment = Alignment.Center) {
                                IconButton(
                                    onClick = { onEditClick(syl) },
                                    modifier = Modifier.testTag("btn_edit_syllabus_${syl.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Silabus ${syl.programTitle}",
                                        tint = ScoutGreen
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

@Composable
fun AdminActivitiesTab(
    activities: List<ScoutActivity>,
    onAddClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tabel Agenda Kegiatan",
                    fontWeight = FontWeight.Bold,
                    color = SneatTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_activity")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Kegiatan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            SneatTableContainer(modifier = Modifier.testTag("admin_activities_table")) {
                SneatTableHeaderRow {
                    SneatTableHeaderCell(text = "Kegiatan & Lokasi", weight = 0.44f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Tanggal", weight = 0.28f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Kuota & Status", weight = 0.28f, alignment = Alignment.Center)
                }

                if (activities.isEmpty()) {
                    SneatTableDataRow(index = 0) {
                        SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                            Text(
                                text = "Belum ada agenda kegiatan terdaftar.",
                                fontSize = 11.sp,
                                color = SneatTextSecondary
                            )
                        }
                    }
                } else {
                    activities.forEachIndexed { index, act ->
                        SneatTableDataRow(index = index) {
                            SneatTableCell(weight = 0.44f) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(text = act.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SneatTextPrimary)
                                    Text(text = act.location, fontSize = 10.sp, color = SneatTextSecondary)
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.28f) {
                                Text(text = act.date, fontSize = 11.sp, color = ScoutBrown, fontWeight = FontWeight.Medium)
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.28f, alignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "${act.registeredCount}/${act.quota}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SneatTextPrimary)
                                    SneatBadge(text = act.status, backgroundColor = ScoutGreenContainer, textColor = ScoutGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnnouncementsTab(
    announcements: List<ScoutAnnouncement>,
    onAddClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tabel Warta Pengumuman",
                    fontWeight = FontWeight.Bold,
                    color = SneatTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_announcement")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Pengumuman", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            SneatTableContainer(modifier = Modifier.testTag("admin_announcements_table")) {
                SneatTableHeaderRow {
                    SneatTableHeaderCell(text = "Judul & Isi Warta", weight = 0.54f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Prioritas", weight = 0.24f, alignment = Alignment.Center)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Waktu", weight = 0.22f, alignment = Alignment.Center)
                }

                if (announcements.isEmpty()) {
                    SneatTableDataRow(index = 0) {
                        SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                            Text(
                                text = "Belum ada warta pengumuman.",
                                fontSize = 11.sp,
                                color = SneatTextSecondary
                            )
                        }
                    }
                } else {
                    announcements.forEachIndexed { index, ann ->
                        SneatTableDataRow(index = index) {
                            SneatTableCell(weight = 0.54f) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(text = ann.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SneatTextPrimary)
                                    Text(text = ann.content, fontSize = 10.sp, color = SneatTextSecondary, lineHeight = 14.sp)
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.24f, alignment = Alignment.Center) {
                                SneatBadge(
                                    text = ann.priority,
                                    backgroundColor = if (ann.priority == "Mendesak") SneatDanger else SneatSuccess,
                                    textColor = Color.White
                                )
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.22f, alignment = Alignment.Center) {
                                Text(text = ann.date, fontSize = 10.sp, color = SneatTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMembersAndPinTab(
    members: List<ScoutMember>,
    adminConfig: AdminConfigProfile,
    onAddMemberClick: () -> Unit,
    onToggleMemberVerified: (String) -> Unit,
    onUpdateAdminPin: (String) -> Unit,
    onLockAdminAccess: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tabel Anggota Terdaftar (${members.size})",
                    fontWeight = FontWeight.Bold,
                    color = SneatTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = onAddMemberClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_member")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Anggota", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            SneatTableContainer(modifier = Modifier.testTag("admin_members_table")) {
                SneatTableHeaderRow {
                    SneatTableHeaderCell(text = "Anggota & Sekolah", weight = 0.42f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Gudep & SKU", weight = 0.36f)
                    SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                    SneatTableHeaderCell(text = "Verifikasi", weight = 0.22f, alignment = Alignment.Center)
                }

                if (members.isEmpty()) {
                    SneatTableDataRow(index = 0) {
                        SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                            Text(
                                text = "Belum ada anggota terdaftar.",
                                fontSize = 11.sp,
                                color = SneatTextSecondary
                            )
                        }
                    }
                } else {
                    members.forEachIndexed { index, member ->
                        SneatTableDataRow(index = index) {
                            SneatTableCell(weight = 0.42f) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SneatTextPrimary)
                                    SneatBadge(text = member.rank.title, backgroundColor = ScoutYellowContainer, textColor = ScoutBrown)
                                    Text(text = member.school, fontSize = 10.sp, color = SneatTextSecondary)
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.36f) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(text = member.gudep, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ScoutGreen)
                                    Text(text = member.skuLevel, fontSize = 10.sp, color = ScoutBrown)
                                    if (member.phone.isNotBlank()) {
                                        Text(text = member.phone, fontSize = 10.sp, color = SneatTextSecondary)
                                    }
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.22f, alignment = Alignment.Center) {
                                IconButton(onClick = { onToggleMemberVerified(member.id) }) {
                                    Icon(
                                        imageVector = if (member.verified) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = "Status Verifikasi",
                                        tint = if (member.verified) SneatSuccess else SneatDanger
                                    )
                                }
                            }
                        }
                    }
                }
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Keamanan Akun Admin & PIN Akses",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScoutGreen
                    )

                    SneatTableContainer {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell(text = "Parameter Admin", weight = 0.40f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell(text = "Data Tersimpan", weight = 0.60f)
                        }
                        AdminSessionDetailRow(0, "Nama Admin", adminConfig.fullName.ifBlank { "-" })
                        AdminSessionDetailRow(1, "Tingkatan", adminConfig.rank.title)
                        AdminSessionDetailRow(2, "Gudep", adminConfig.gudep.ifBlank { "-" })
                        AdminSessionDetailRow(3, "Status Pro", if (adminConfig.isProSemesterPaid) adminConfig.proSemesterLabel.ifBlank { "Pro 1 Semester" } else "Pro Tayangan Iklan")
                    }

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it; pinError = null },
                        label = { Text("Perbarui PIN Akses Dashboard") },
                        placeholder = { Text("Minimal 4 karakter/angka") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("input_update_admin_pin")
                    )

                    if (pinError != null) {
                        Text(text = pinError ?: "", fontSize = 11.sp, color = SneatDanger)
                    }

                    Button(
                        onClick = {
                            if (newPin.trim().length < 4) {
                                pinError = "PIN baru minimal 4 karakter/angka."
                            } else {
                                onUpdateAdminPin(newPin.trim())
                                newPin = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_update_admin_pin")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan PIN Akses Baru", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onLockAdminAccess,
                        colors = ButtonDefaults.buttonColors(containerColor = SneatDanger),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_lock_admin_again")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kunci & Keluar Sesi Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSessionDetailRow(index: Int, label: String, value: String) {
    SneatTableDataRow(index = index) {
        SneatTableCell(weight = 0.40f) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScoutBrown)
        }
        SneatTableColumnDivider()
        SneatTableCell(weight = 0.60f) {
            Text(text = value, fontSize = 11.sp, color = SneatTextPrimary)
        }
    }
}
