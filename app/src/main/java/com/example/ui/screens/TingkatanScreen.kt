package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoutRank
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatCard
import com.example.ui.components.SneatTableCell
import com.example.ui.components.SneatTableColumnDivider
import com.example.ui.components.SneatTableContainer
import com.example.ui.components.SneatTableDataRow
import com.example.ui.components.SneatTableHeaderCell
import com.example.ui.components.SneatTableHeaderRow
import com.example.ui.components.SneatTopBar
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun TingkatanScreen(
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default Penggalang
    val ranks = ScoutRank.entries
    val currentRank = ranks[selectedTabIndex]

    val checkedItems = remember { mutableStateMapOf<String, Boolean>() }

    val rankData = when (currentRank) {
        ScoutRank.SIAGA -> RankDetail(
            age = "7 - 10 Tahun",
            colorName = "Hijau Daun",
            levels = listOf("Siaga Mula", "Siaga Bantu", "Siaga Tata", "Pramuka Garuda"),
            satyaDarma = "Dwisatya & Dwidarma",
            satyaDarmaContent = "Dwisatya:\n1. Menjalankan kewajibanku terhadap Tuhan Yang Maha Esa, Negara Kesatuan Republik Indonesia, dan menurut aturan keluarga.\n2. Setiap hari berbuat kebaikan.\n\nDwidarma:\n1. Siaga berbakti pada ayah dan ibundanya.\n2. Siaga berani dan tidak putus asa.",
            skuPoints = listOf(
                "Rajin beribadah sesuai agama masing-masing",
                "Tahu dan dapat menyanyikan lagu Indonesia Raya",
                "Dapat memberi salam pramuka secara benar",
                "Mengenal simpul mati dan simpul hidup",
                "Selalu hemat, cermat, dan bersikap jujur"
            )
        )
        ScoutRank.PENGGALANG -> RankDetail(
            age = "11 - 15 Tahun",
            colorName = "Merah Api Semangat",
            levels = listOf("Penggalang Ramu", "Penggalang Rakit", "Penggalang Terap", "Pramuka Garuda"),
            satyaDarma = "Trisatya & Dasadarma",
            satyaDarmaContent = "Trisatya:\nDemi kehormatanku aku berjanji akan bersungguh-sungguh:\n1. Menjalankan kewajibanku terhadap Tuhan, Negara Kesatuan Republik Indonesia dan mengamalkan Pancasila.\n2. Menolong sesama hidup dan mempersiapkan diri membangun masyarakat.\n3. Menepati Dasadarma.\n\nDasadarma: 1. Takwa kepada Tuhan Yang Maha Esa; 2. Cinta alam dan kasih sayang sesama manusia; 3. Patriot yang sopan dan kesatria; 4. Patuh dan suka bermusyawarah; 5. Rela menolong dan tabah; 6. Rajin, terampil dan gembira; 7. Hemat, cermat dan bersahaja; 8. Disiplin, berani dan setia; 9. Bertanggung jawab dan dapat dipercaya; 10. Suci dalam pikiran, perkataan dan perbuatan.",
            skuPoints = listOf(
                "Dapat menjelaskan makna Trisatya dan Dasadarma",
                "Dapat mengirim dan menerima kode Morse dan Semaphore",
                "Dapat membuat simpul pangkal, jangkar, dan ikatan palang",
                "Mampu melakukan pertolongan pertama (balut bidai P3K)",
                "Dapat membaca kompas dan menentukan arah mata angin",
                "Telah menempuh minimal 5 TKK Tingkat Purwa"
            )
        )
        ScoutRank.PENEGAK -> RankDetail(
            age = "16 - 20 Tahun",
            colorName = "Kuning Emas Keagungan",
            levels = listOf("Penegak Bantara", "Penegak Laksana", "Pramuka Garuda"),
            satyaDarma = "Trisatya & Dasadarma Penegak",
            satyaDarmaContent = "Satuan Penegak menggunakan sistem Ambalan dengan Dewan Ambalan terpilih. Moto: Berani, Mandiri, dan Berkarya untuk Nusa dan Bangsa.",
            skuPoints = listOf(
                "Aktif dalam kepengurusan Dewan Ambalan Penegak",
                "Dapat memimpin kegiatan penjelajahan alam terbuka",
                "Mampu membuat proyek teknologi tepat guna kepramukaan",
                "Memiliki TKK Wajib Tingkat Madya & Utama",
                "Telah mengikuti gladian pemimpin sangga (DIANPINSA)"
            )
        )
        ScoutRank.PANDEGA -> RankDetail(
            age = "21 - 25 Tahun",
            colorName = "Coklat Tua Kematangan",
            levels = listOf("Pandega Tingkat Dasar", "Pandega Madya", "Pramuka Garuda"),
            satyaDarma = "Bina Diri, Bina Satuan, Bina Masyarakat",
            satyaDarmaContent = "Pandega berkedudukan di Racana pada perguruan tinggi atau kwartir, berorientasi pada pengabdian masyarakat, riset, dan kepemimpinan profesional.",
            skuPoints = listOf(
                "Menjadi konsultan/rekan bina bagi satuan Siaga/Penggalang",
                "Merancang program pengabdian masyarakat berkelanjutan",
                "Menguasai manajemen organisasi dan tata laksana perkemahan"
            )
        )
        ScoutRank.PEMBINA -> RankDetail(
            age = "Dewasa / Pelatih",
            colorName = "Ungu / Hijau Pin Emas",
            levels = listOf("Kursus Mahir Dasar (KMD)", "Kursus Mahir Lanjutan (KML)", "Pelatih KPD & KPL"),
            satyaDarma = "Ing Ngarso Sung Tulodo, Ing Madyo Mangun Karso, Tut Wuri Handayani",
            satyaDarmaContent = "Pembina berperan mendampingi, menginspirasi, dan melindungi peserta didik dengan sistem among dan keteladanan watak kesatria.",
            skuPoints = listOf(
                "Telah lulus Ijazah KMD / KML Kwartir Cabang",
                "Mampu menyusun Rencana Membina (RM) mingguan",
                "Memahami manajemen resiko dan keamanan kegiatan lapangan"
            )
        )
        ScoutRank.BINA_DAMPING -> RankDetail(
            age = "Pembina Pendamping",
            colorName = "Coklat Pin Pendamping",
            levels = listOf("Orientasi Kepramukaan", "Kursus Pembina Mahir Dasar", "Sertifikat Bina Damping"),
            satyaDarma = "Sistem Among & Pendampingan Regu/Sangga",
            satyaDarmaContent = "Bina Damping mendampingi peserta didik dan membantu Pembina dalam pelaksanaan latihan rutin, perkemahan, pengisian SKU, serta pengelolaan program kerja Gudep.",
            skuPoints = listOf(
                "Mendampingi latihan mingguan regu/sangga secara aktif",
                "Membantu verifikasi butir SKU & TKK peserta didik",
                "Berkoordinasi dengan Pembina dalam penyusunan silabus semester"
            )
        )
    }

    val completedCount = rankData.skuPoints.count { checkedItems["${currentRank.name}_$it"] == true }
    val progressPercent = if (rankData.skuPoints.isNotEmpty()) {
        (completedCount.toFloat() / rankData.skuPoints.size.toFloat())
    } else 1f

    val comparisonRows = listOf(
        Triple(ScoutRank.SIAGA, "Dwisatya / Dwidarma", "Mula, Bantu, Tata"),
        Triple(ScoutRank.PENGGALANG, "Trisatya / Dasadarma", "Ramu, Rakit, Terap"),
        Triple(ScoutRank.PENEGAK, "Trisatya / Dasadarma", "Bantara, Laksana"),
        Triple(ScoutRank.PANDEGA, "Trisatya / Dasadarma", "Pandega, Garuda"),
        Triple(ScoutRank.PEMBINA, "Sistem Among", "KMD, KML, KPD, KPL"),
        Triple(ScoutRank.BINA_DAMPING, "Pendampingan Satuan", "Orientasi, KMD, Pendamping")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .testTag("tingkatan_screen_container")
    ) {
        SneatTopBar(
            title = "Tingkatan Pramuka",
            subtitle = "Tabel Golongan & Syarat Kecakapan",
            onBackClick = onBack
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SneatSurface,
            contentColor = ScoutGreen,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ScoutGreen,
                    height = 3.dp
                )
            }
        ) {
            ranks.forEachIndexed { index, rank ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = rank.title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) ScoutGreen else SneatTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_rank_${rank.name}")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tabel Perbandingan 6 Golongan Pramuka
            item {
                Column {
                    Text(
                        text = "Tabel Golongan Kepramukaan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SneatTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SneatTableContainer(modifier = Modifier.testTag("tingkatan_comparison_table")) {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell("Golongan", weight = 1.0f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Usia", weight = 0.85f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Jenjang Tingkat", weight = 1.25f)
                        }
                        comparisonRows.forEachIndexed { index, (rank, code, levelText) ->
                            val isSelected = rank == currentRank
                            SneatTableDataRow(
                                index = index,
                                onClick = { selectedTabIndex = ranks.indexOf(rank) }
                            ) {
                                SneatTableCell(weight = 1.0f) {
                                    Column {
                                        Text(
                                            text = rank.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ScoutGreen else SneatTextPrimary
                                        )
                                        Text(
                                            text = code,
                                            fontSize = 9.sp,
                                            color = ScoutBrown,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.85f) {
                                    Text(
                                        text = rank.ageGroup,
                                        fontSize = 11.sp,
                                        color = SneatTextPrimary
                                    )
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 1.25f) {
                                    Text(
                                        text = levelText,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ScoutGreen else SneatTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Detail Golongan Aktif dalam Kolom
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SneatSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SneatBorder)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ScoutGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = ScoutGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Golongan ${currentRank.title}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ScoutGreen
                                        )
                                    )
                                    Text(
                                        text = "Usia: ${rankData.age}",
                                        fontSize = 12.sp,
                                        color = SneatTextSecondary
                                    )
                                }
                            }
                            SneatBadge(
                                text = rankData.colorName,
                                backgroundColor = ScoutYellowContainer,
                                textColor = ScoutBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Jenjang Kenaikan Tingkat (Grid 2 Kolom):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SneatTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2-Column Grid for Levels
                        rankData.levels.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { level ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ScoutGreenContainer)
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = level,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ScoutGreen
                                        )
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 3. Kode Kehormatan Card
            item {
                SneatCard {
                    Column {
                        Text(
                            text = "Kode Kehormatan: ${rankData.satyaDarma}",
                            fontWeight = FontWeight.Bold,
                            color = ScoutBrown,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rankData.satyaDarmaContent,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = SneatTextPrimary
                        )
                    }
                }
            }

            // 4. Tabel Simulasi Checklist Syarat SKU
            item {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tabel Syarat Kecakapan Umum (SKU)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ScoutGreen
                        )
                        Text(
                            text = "$completedCount / ${rankData.skuPoints.size} Terpenuhi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SneatSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ScoutGreen,
                        trackColor = ScoutGreenContainer,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SneatTableContainer(modifier = Modifier.testTag("sku_checklist_table")) {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell("No", weight = 0.35f, alignment = Alignment.Center)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Butir Syarat SKU", weight = 2.0f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Status", weight = 0.65f, alignment = Alignment.Center)
                        }

                        rankData.skuPoints.forEachIndexed { index, point ->
                            val key = "${currentRank.name}_$point"
                            val isChecked = checkedItems[key] == true

                            SneatTableDataRow(
                                index = index,
                                onClick = { checkedItems[key] = !isChecked }
                            ) {
                                SneatTableCell(weight = 0.35f, alignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SneatTextPrimary
                                    )
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 2.0f) {
                                    Text(
                                        text = point,
                                        fontSize = 12.sp,
                                        color = if (isChecked) ScoutGreen else SneatTextPrimary,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.65f, alignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (isChecked) "Terpenuhi" else "Belum",
                                        tint = if (isChecked) ScoutGreen else SneatBorder,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private data class RankDetail(
    val age: String,
    val colorName: String,
    val levels: List<String>,
    val satyaDarma: String,
    val satyaDarmaContent: String,
    val skuPoints: List<String>
)
