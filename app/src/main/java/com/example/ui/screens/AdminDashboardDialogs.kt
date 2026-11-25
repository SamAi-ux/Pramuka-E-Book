package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoutMaterial
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus
import com.example.ui.components.scoutInputTextStyle
import com.example.ui.components.scoutTextFieldColors
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.SneatDanger
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun MaterialFormDialog(
    initialMaterial: ScoutMaterial?,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, summary: String, content: String, details: List<String>) -> Unit
) {
    val isEdit = initialMaterial != null
    var title by remember(initialMaterial) { mutableStateOf(initialMaterial?.title ?: "") }
    var category by remember(initialMaterial) { mutableStateOf(initialMaterial?.category ?: "Sandi & Morse") }
    var summary by remember(initialMaterial) { mutableStateOf(initialMaterial?.summary ?: "") }
    var content by remember(initialMaterial) { mutableStateOf(initialMaterial?.content ?: "") }
    var detailsText by remember(initialMaterial) {
        mutableStateOf(initialMaterial?.details?.joinToString("\n") ?: "")
    }
    var materialError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SneatSurface,
        title = {
            Text(
                text = if (isEdit) "Edit Modul Materi" else "Tambah Modul Materi",
                fontWeight = FontWeight.Bold,
                color = ScoutGreen
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; materialError = null },
                    label = { Text("Judul Materi *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_material_title")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (Sandi & Morse/Semaphore/Tali Temali/P3K & Survival/Sejarah/Lagu & Yel)") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_material_category")
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Ringkasan Singkat") },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_material_summary")
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Modul Lengkap") },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().height(96.dp).testTag("dialog_input_material_content")
                )
                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    label = { Text("Butir Poin Materi (1 poin per baris)") },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
                if (materialError != null) {
                    Text(text = materialError ?: "", fontSize = 11.sp, color = SneatDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        materialError = "Judul materi wajib diisi."
                    } else {
                        val parsedDetails = detailsText
                            .lines()
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        onSave(title.trim(), category.trim().ifBlank { "Materi Umum" }, summary.trim(), content.trim(), parsedDetails)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                modifier = Modifier.testTag("dialog_btn_save_material")
            ) {
                Text(if (isEdit) "Simpan Perubahan" else "Terbitkan Materi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun SyllabusFormDialog(
    initialSyllabus: SemesterSyllabus?,
    onDismiss: () -> Unit,
    onSave: (
        semester: String,
        rank: ScoutRank,
        programTitle: String,
        weekRange: String,
        competencyTarget: String,
        method: String,
        status: String
    ) -> Unit
) {
    val isEdit = initialSyllabus != null
    var semester by remember(initialSyllabus) {
        mutableStateOf(initialSyllabus?.semester ?: "Semester Ganjil 2026/2027")
    }
    var selectedRank by remember(initialSyllabus) {
        mutableStateOf(initialSyllabus?.rank ?: ScoutRank.PENGGALANG)
    }
    var rankExpanded by remember { mutableStateOf(false) }
    var programTitle by remember(initialSyllabus) { mutableStateOf(initialSyllabus?.programTitle ?: "") }
    var weekRange by remember(initialSyllabus) { mutableStateOf(initialSyllabus?.weekRange ?: "Minggu 1 - 4") }
    var competencyTarget by remember(initialSyllabus) { mutableStateOf(initialSyllabus?.competencyTarget ?: "") }
    var method by remember(initialSyllabus) { mutableStateOf(initialSyllabus?.method ?: "Praktik Lapangan & Diskusi") }
    var status by remember(initialSyllabus) { mutableStateOf(initialSyllabus?.status ?: "Terjadwal") }
    var syllabusError by remember { mutableStateOf<String?>(null) }

    val rankInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(rankInteraction) {
        rankInteraction.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                rankExpanded = !rankExpanded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SneatSurface,
        title = {
            Text(
                text = if (isEdit) "Edit Program Kerja & Silabus" else "Tambah Program Kerja / Silabus Semester",
                fontWeight = FontWeight.Bold,
                color = ScoutGreen
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pilih Semester:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoutBrown
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Semester Ganjil 2026/2027", "Semester Genap 2026/2027").forEach { preset ->
                        val active = semester == preset
                        OutlinedButton(
                            onClick = { semester = preset },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (active) ScoutGreenContainer else SneatSurface,
                                contentColor = if (active) ScoutGreen else SneatTextSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (preset.contains("Ganjil")) "Ganjil" else "Genap",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it; syllabusError = null },
                    label = { Text("Label Semester *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_syllabus_semester")
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRank.title,
                        onValueChange = {},
                        readOnly = true,
                        interactionSource = rankInteraction,
                        label = { Text("Golongan / Tingkatan Pramuka") },
                        trailingIcon = {
                            IconButton(onClick = { rankExpanded = !rankExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Golongan")
                            }
                        },
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = rankExpanded,
                        onDismissRequest = { rankExpanded = false },
                        modifier = Modifier.background(SneatSurface)
                    ) {
                        ScoutRank.entries.forEach { rank ->
                            DropdownMenuItem(
                                text = { Text("${rank.title} (${rank.ageGroup})", color = SneatTextPrimary) },
                                onClick = {
                                    selectedRank = rank
                                    rankExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = programTitle,
                    onValueChange = { programTitle = it; syllabusError = null },
                    label = { Text("Judul Program Kerja / Topik Silabus *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_syllabus_title")
                )
                OutlinedTextField(
                    value = weekRange,
                    onValueChange = { weekRange = it },
                    label = { Text("Jadwal / Minggu Pelaksanaan") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_syllabus_week")
                )
                OutlinedTextField(
                    value = competencyTarget,
                    onValueChange = { competencyTarget = it; syllabusError = null },
                    label = { Text("Target Kompetensi Pembelajaran *") },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_syllabus_target")
                )
                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("Metode Pembelajaran") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Status Program:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoutBrown
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Terjadwal", "Berjalan", "Selesai").forEach { st ->
                        val active = status == st
                        OutlinedButton(
                            onClick = { status = st },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (active) ScoutGreenContainer else SneatSurface,
                                contentColor = if (active) ScoutGreen else SneatTextSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (syllabusError != null) {
                    Text(text = syllabusError ?: "", fontSize = 11.sp, color = SneatDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        semester.isBlank() -> syllabusError = "Label semester wajib diisi."
                        programTitle.isBlank() -> syllabusError = "Judul program kerja wajib diisi."
                        competencyTarget.isBlank() -> syllabusError = "Target kompetensi wajib diisi."
                        else -> onSave(
                            semester.trim(),
                            selectedRank,
                            programTitle.trim(),
                            weekRange.trim().ifBlank { "Minggu 1 - 4" },
                            competencyTarget.trim(),
                            method.trim().ifBlank { "Praktik Lapangan" },
                            status
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                modifier = Modifier.testTag("dialog_btn_save_syllabus")
            ) {
                Text(if (isEdit) "Simpan Perubahan" else "Simpan Program Kerja")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAddMember: (name: String, school: String, gudep: String, rank: ScoutRank, skuLevel: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var gudep by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skuLevel by remember { mutableStateOf("Penggalang Ramu") }
    var memberError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SneatSurface,
        title = { Text("Tambah Anggota Baru", fontWeight = FontWeight.Bold, color = ScoutGreen) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; memberError = null },
                    label = { Text("Nama Lengkap *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("Asal Sekolah") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gudep,
                    onValueChange = { gudep = it },
                    label = { Text("Gugus Depan") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = skuLevel,
                    onValueChange = { skuLevel = it },
                    label = { Text("Tingkat SKU") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP / WhatsApp") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (memberError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = memberError ?: "", fontSize = 11.sp, color = SneatDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        memberError = "Nama lengkap wajib diisi."
                    } else {
                        onAddMember(
                            name.trim(),
                            school.ifBlank { "Gudep Garuda" },
                            gudep.ifBlank { "01.001" },
                            ScoutRank.PENGGALANG,
                            skuLevel,
                            phone.ifBlank { "-" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
            ) {
                Text("Simpan Anggota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAddActivity: (title: String, category: String, date: String, time: String, location: String, leader: String, quota: Int, desc: String) -> Unit
) {
    var actTitle by remember { mutableStateOf("") }
    var actCategory by remember { mutableStateOf("Latihan Rutin") }
    var actDate by remember { mutableStateOf("Sabtu Depan") }
    var actLocation by remember { mutableStateOf("Lapangan Utama Pangkalan") }
    var actDesc by remember { mutableStateOf("") }
    var activityError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SneatSurface,
        title = { Text("Tambah Agenda Kegiatan", fontWeight = FontWeight.Bold, color = ScoutGreen) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = actTitle,
                    onValueChange = { actTitle = it; activityError = null },
                    label = { Text("Nama Kegiatan *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actCategory,
                    onValueChange = { actCategory = it },
                    label = { Text("Kategori (Latihan/Perkemahan/Lomba)") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actDate,
                    onValueChange = { actDate = it },
                    label = { Text("Tanggal & Hari") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actLocation,
                    onValueChange = { actLocation = it },
                    label = { Text("Lokasi Kegiatan") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actDesc,
                    onValueChange = { actDesc = it },
                    label = { Text("Deskripsi Agenda") },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (activityError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = activityError ?: "", fontSize = 11.sp, color = SneatDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (actTitle.isBlank()) {
                        activityError = "Nama kegiatan wajib diisi."
                    } else {
                        onAddActivity(
                            actTitle.trim(),
                            actCategory,
                            actDate,
                            "14:00 - 17:00 WIB",
                            actLocation,
                            "Kakak Pembina",
                            50,
                            actDesc
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
            ) {
                Text("Jadwalkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onAddAnnouncement: (title: String, content: String, priority: String) -> Unit
) {
    var annTitle by remember { mutableStateOf("") }
    var annContent by remember { mutableStateOf("") }
    var annPriority by remember { mutableStateOf("Penting") }
    var annError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SneatSurface,
        title = { Text("Siarkan Warta Baru", fontWeight = FontWeight.Bold, color = ScoutGreen) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = annTitle,
                    onValueChange = { annTitle = it; annError = null },
                    label = { Text("Judul Pengumuman *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = annPriority,
                    onValueChange = { annPriority = it },
                    label = { Text("Tingkat Prioritas (Info/Penting/Mendesak)") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = annContent,
                    onValueChange = { annContent = it },
                    label = { Text("Isi Pengumuman") },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                if (annError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = annError ?: "", fontSize = 11.sp, color = SneatDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (annTitle.isBlank()) {
                        annError = "Judul pengumuman wajib diisi."
                    } else {
                        onAddAnnouncement(annTitle.trim(), annContent, annPriority)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
            ) {
                Text("Kirimkan Siaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
