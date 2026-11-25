package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoutMaterial
import com.example.model.SemesterSyllabus
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatCard
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
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatSurfaceVariant
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun MateriScreen(
    materials: List<ScoutMaterial>,
    syllabi: List<SemesterSyllabus> = emptyList(),
    initialTab: Int = 0,
    onBack: () -> Unit
) {
    var activeSectionTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedSemester by remember { mutableStateOf("Semua") }
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }

    val morseMap = remember {
        mapOf(
            'A' to "• —", 'B' to "— • • •", 'C' to "— • — •", 'D' to "— • •",
            'E' to "•", 'F' to "• • — •", 'G' to "— — •", 'H' to "• • • •",
            'I' to "• •", 'J' to "• — — —", 'K' to "— • —", 'L' to "• — • •",
            'M' to "— —", 'N' to "— •", 'O' to "— — —", 'P' to "• — — •",
            'Q' to "— — • —", 'R' to "• — •", 'S' to "• • •", 'T' to "—",
            'U' to "• • —", 'V' to "• • • —", 'W' to "• — —", 'X' to "— • • —",
            'Y' to "— • — —", 'Z' to "— — • •",
            '0' to "— — — — —", '1' to "• — — — —", '2' to "• • — — —",
            '3' to "• • • — —", '4' to "• • • • —", '5' to "• • • • •",
            '6' to "— • • • •", '7' to "— — • • •", '8' to "— — — • •",
            '9' to "— — — — •", ' ' to " / "
        )
    }

    fun convertToMorse(input: String): String {
        val normalized = input.trim().replace(Regex("\\s+"), " ")
        if (normalized.isEmpty()) return ""
        return normalized.uppercase().map { char ->
            morseMap[char] ?: "?"
        }.joinToString("  ")
    }

    // Interactive Morse Decoder / Encoder Tool state
    var inputScoutText by remember { mutableStateOf("PRAMUKA") }
    var translatedMorse by remember { mutableStateOf(convertToMorse("PRAMUKA")) }

    val baseCategories = listOf("Sandi & Morse", "Semaphore", "Tali Temali", "P3K & Survival", "Sejarah", "Lagu & Yel")
    val categories = remember(materials) {
        listOf("Semua") + (baseCategories + materials.map { it.category }.filter { it.isNotBlank() }).distinct()
    }

    val filteredMaterials = materials.filter {
        selectedCategory == "Semua" || it.category == selectedCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .testTag("materi_screen_container")
    ) {
        SneatTopBar(
            title = if (activeSectionTab == 0) "Materi Kepramukaan" else "Proker & Silabus Semester",
            subtitle = "Modul SKU/TKK & Program Kerja Pembelajaran",
            onBackClick = onBack
        )

        TabRow(
            selectedTabIndex = activeSectionTab,
            containerColor = SneatSurface,
            contentColor = ScoutGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeSectionTab]),
                    color = ScoutGreen,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = activeSectionTab == 0,
                onClick = { activeSectionTab = 0 },
                text = {
                    Text(
                        text = "Modul Materi (${materials.size})",
                        fontSize = 12.sp,
                        fontWeight = if (activeSectionTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeSectionTab == 0) ScoutGreen else SneatTextSecondary
                    )
                },
                modifier = Modifier.testTag("tab_materi_modul")
            )
            Tab(
                selected = activeSectionTab == 1,
                onClick = { activeSectionTab = 1 },
                text = {
                    Text(
                        text = "Proker & Silabus (${syllabi.size})",
                        fontSize = 12.sp,
                        fontWeight = if (activeSectionTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeSectionTab == 1) ScoutGreen else SneatTextSecondary
                    )
                },
                modifier = Modifier.testTag("tab_materi_silabus")
            )
        }

        if (activeSectionTab == 1) {
            val semesterOptions = remember(syllabi) {
                listOf("Semua") + syllabi.map { it.semester }.distinct()
            }
            val filteredSyllabi = syllabi.filter {
                selectedSemester == "Semua" || it.semester == selectedSemester
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SneatTableContainer(modifier = Modifier.testTag("member_syllabus_table")) {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell(text = "Semester & Golongan", weight = 0.34f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell(text = "Program Kerja & Target Silabus", weight = 0.66f)
                        }
                        if (filteredSyllabi.isEmpty()) {
                            SneatTableDataRow(index = 0) {
                                SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                                    Text(
                                        text = "Belum ada program kerja / silabus untuk semester ini.",
                                        fontSize = 12.sp,
                                        color = SneatTextSecondary
                                    )
                                }
                            }
                        } else {
                            filteredSyllabi.forEachIndexed { idx, syl ->
                                SneatTableDataRow(index = idx) {
                                    SneatTableCell(weight = 0.34f) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = syl.semester,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
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
                                    SneatTableCell(weight = 0.66f) {
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(
                                                text = syl.programTitle,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SneatTextPrimary
                                            )
                                            Text(
                                                text = syl.competencyTarget,
                                                fontSize = 11.sp,
                                                color = SneatTextSecondary,
                                                lineHeight = 15.sp
                                            )
                                            Text(
                                                text = "Metode: ${syl.method} • ${syl.status}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ScoutGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return@Column
        }

        // Categories Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("materi_category_row"),
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
                    modifier = Modifier.testTag("filter_materi_$cat")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("materi_catalog_list"),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Interactive Morse Converter Tool Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SneatSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ScoutGreen)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interactive_morse_tool")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ScoutGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Transform,
                                    contentDescription = null,
                                    tint = ScoutGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Konverter Sandi Morse Interaktif",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ScoutGreen
                                )
                                Text(
                                    text = "Ketik teks untuk melihat terjemahan kode morse",
                                    fontSize = 11.sp,
                                    color = SneatTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputScoutText,
                            onValueChange = {
                                inputScoutText = it
                                translatedMorse = convertToMorse(it)
                            },
                            label = { Text("Teks Input") },
                            placeholder = { Text("Ketik kata (misal: PRAJA MUDA KARANA)") },
                            singleLine = true,
                            textStyle = scoutInputTextStyle,
                            colors = scoutTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("input_morse_text")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SneatTableContainer {
                            SneatTableHeaderRow(backgroundColor = ScoutBrown) {
                                SneatTableHeaderCell(text = "Teks Asli", weight = 0.35f)
                                SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                                SneatTableHeaderCell(text = "Hasil Sandi Morse", weight = 0.65f)
                            }
                            SneatTableDataRow(index = 0) {
                                SneatTableCell(weight = 0.35f) {
                                    Text(
                                        text = inputScoutText.ifBlank { "-" }.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SneatTextPrimary
                                    )
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.65f) {
                                    Text(
                                        text = translatedMorse.ifBlank { "..." },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = ScoutBrown
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2-Column Grid of Material Modules
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Katalog Kolom Modul Materi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SneatTextPrimary
                        )
                    )
                    Text(
                        text = "${filteredMaterials.size} modul",
                        fontSize = 11.sp,
                        color = SneatTextSecondary
                    )
                }
            }

            val gridRows = filteredMaterials.chunked(2)
            items(gridRows) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { mat ->
                        val icon = when (mat.category) {
                            "Sandi & Morse" -> Icons.Default.Radio
                            "Semaphore" -> Icons.Default.Flag
                            "Tali Temali" -> Icons.Default.Link
                            "P3K & Survival" -> Icons.Default.HealthAndSafety
                            "Sejarah" -> Icons.Default.HistoryEdu
                            "Lagu & Yel" -> Icons.Default.LibraryMusic
                            else -> Icons.AutoMirrored.Filled.MenuBook
                        }
                        val isExpanded = expandedItems[mat.id] ?: false
                        SneatCard(
                            modifier = Modifier.weight(1f),
                            borderColor = if (isExpanded) ScoutGreen else SneatBorder,
                            onClick = { expandedItems[mat.id] = !isExpanded }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ScoutGreenContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = ScoutGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    SneatBadge(
                                        text = mat.category,
                                        backgroundColor = ScoutYellowContainer,
                                        textColor = ScoutBrown
                                    )
                                }
                                Text(
                                    text = mat.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SneatTextPrimary,
                                    lineHeight = 16.sp,
                                    maxLines = 2
                                )
                                Text(
                                    text = if (isExpanded) "Tutup detail ▲" else "Lihat tabel materi ▼",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ScoutGreen
                                )
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Full Structured Materials Table with Expandable 2-Column Detail Sub-Tables
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tabel Modul & Butir Materi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SneatTextPrimary
                    )
                )
            }

            item {
                SneatTableContainer(modifier = Modifier.testTag("materi_table")) {
                    SneatTableHeaderRow {
                        SneatTableHeaderCell(text = "No", weight = 0.12f, alignment = Alignment.Center)
                        SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                        SneatTableHeaderCell(text = "Modul & Ringkasan", weight = 0.56f)
                        SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                        SneatTableHeaderCell(text = "Kategori", weight = 0.32f, alignment = Alignment.Center)
                    }

                    if (filteredMaterials.isEmpty()) {
                        SneatTableDataRow(index = 0) {
                            SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                                Text(
                                    text = "Belum ada modul materi pada kategori ini.",
                                    fontSize = 12.sp,
                                    color = SneatTextSecondary
                                )
                            }
                        }
                    } else {
                        filteredMaterials.forEachIndexed { index, mat ->
                            val isExpanded = expandedItems[mat.id] ?: false
                            SneatTableDataRow(
                                index = index,
                                onClick = { expandedItems[mat.id] = !isExpanded }
                            ) {
                            SneatTableCell(weight = 0.12f, alignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ScoutGreen
                                )
                            }
                            SneatTableColumnDivider()
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
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            SneatTableColumnDivider()
                            SneatTableCell(weight = 0.32f, alignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SneatBadge(
                                        text = mat.category,
                                        backgroundColor = ScoutYellowContainer,
                                        textColor = ScoutBrown
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                        contentDescription = if (isExpanded) "Tutup" else "Buka",
                                        tint = ScoutGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SneatSurfaceVariant)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = mat.content,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = SneatTextPrimary
                                )

                                if (mat.details.isNotEmpty()) {
                                    SneatTableContainer {
                                        SneatTableHeaderRow(backgroundColor = ScoutBrown) {
                                            SneatTableHeaderCell(text = "Kolom Butir 1", weight = 0.5f)
                                            SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                                            SneatTableHeaderCell(text = "Kolom Butir 2", weight = 0.5f)
                                        }
                                        mat.details.chunked(2).forEachIndexed { subIdx, pair ->
                                            SneatTableDataRow(index = subIdx) {
                                                SneatTableCell(weight = 0.5f) {
                                                    Text(
                                                        text = pair[0],
                                                        fontSize = 11.sp,
                                                        color = SneatTextPrimary,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                                SneatTableColumnDivider()
                                                SneatTableCell(weight = 0.5f) {
                                                    Text(
                                                        text = pair.getOrNull(1) ?: "-",
                                                        fontSize = 11.sp,
                                                        color = SneatTextPrimary,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = SneatBorder, thickness = 1.dp)
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
