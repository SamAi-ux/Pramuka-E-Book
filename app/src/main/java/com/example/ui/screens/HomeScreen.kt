package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutProfile
import com.example.ui.components.InstitutionLogoImage
import com.example.ui.components.ScoutAvatarImage
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatCard
import com.example.ui.components.SneatStatCard
import com.example.ui.components.SneatTableCell
import com.example.ui.components.SneatTableColumnDivider
import com.example.ui.components.SneatTableContainer
import com.example.ui.components.SneatTableDataRow
import com.example.ui.components.SneatTableHeaderCell
import com.example.ui.components.SneatTableHeaderRow
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutBrownContainer
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutGreenDark
import com.example.ui.theme.ScoutYellow
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatDanger
import com.example.ui.theme.SneatInfo
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary
import com.example.ui.theme.SneatWarning

@Composable
fun HomeScreen(
    profile: ScoutProfile,
    institutionConfig: InstitutionConfig,
    materialsCount: Int = 6,
    syllabiCount: Int = 4,
    activities: List<ScoutActivity>,
    announcements: List<ScoutAnnouncement>,
    onNavigateToTingkatan: () -> Unit,
    onNavigateToKegiatan: () -> Unit,
    onNavigateToMateri: () -> Unit,
    onNavigateToSilabus: () -> Unit,
    onNavigateToBantuan: () -> Unit,
    onNavigateToProfil: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .testTag("home_screen_container"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)
    ) {
        // Top Header with User Greeting & Identity
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ScoutGreenDark, ScoutGreen)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(ScoutYellow)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { onNavigateToProfil() },
                                contentAlignment = Alignment.Center
                            ) {
                                ScoutAvatarImage(
                                    avatarId = profile.avatarId,
                                    customPhotoPath = profile.customPhotoPath,
                                    emojiSize = 24.sp,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Salam Pramuka!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ScoutYellow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${institutionConfig.name} • Gudep ${institutionConfig.gudep}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, ScoutYellow, RoundedCornerShape(10.dp))
                                .clickable { onNavigateToTingkatan() }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "⚜️ ${profile.rank.title}",
                                color = ScoutYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Banner Card featuring dynamic Institution Logo & Gudep
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ScoutBrown),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(modifier = Modifier.height(126.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.pramuka_garuda_hero),
                                contentDescription = "Pramuka Garuda Hero Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.20f),
                                                ScoutGreenDark.copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ScoutYellow)
                                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                        .testTag("home_institution_logo"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    InstitutionLogoImage(
                                        logoPath = institutionConfig.logoPath,
                                        contentDescription = "Logo ${institutionConfig.name}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    SneatBadge(
                                        text = "GUDEP ${institutionConfig.gudep}",
                                        backgroundColor = ScoutYellow,
                                        textColor = ScoutBrown
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = institutionConfig.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Tingkatan Tertinggi Pandu Indonesia",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Announcement Banner
        if (announcements.isNotEmpty()) {
            item {
                val latest = announcements.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    SneatCard(
                        backgroundColor = if (latest.priority == "Mendesak") ScoutYellowContainer else ScoutGreenContainer,
                        borderColor = if (latest.priority == "Mendesak") ScoutYellow else ScoutGreen
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = if (latest.priority == "Mendesak") ScoutBrown else ScoutGreen,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = latest.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (latest.priority == "Mendesak") ScoutBrown else ScoutGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    SneatBadge(
                                        text = latest.priority,
                                        backgroundColor = if (latest.priority == "Mendesak") SneatDanger else SneatSuccess,
                                        textColor = Color.White
                                    )
                                }
                                Text(
                                    text = latest.content,
                                    fontSize = 11.sp,
                                    color = SneatTextPrimary,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3-Column Stat Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Ringkasan Aktivitas Pandu",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SneatTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SneatStatCard(
                        title = "Materi Belajar",
                        value = "$materialsCount Modul",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconBgColor = ScoutGreenContainer,
                        iconTint = ScoutGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SneatStatCard(
                        title = "Jadwal Giat",
                        value = "${activities.size} Giat",
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        iconBgColor = ScoutYellowContainer,
                        iconTint = ScoutBrown,
                        modifier = Modifier.weight(1f)
                    )
                    SneatStatCard(
                        title = "SKU Selesai",
                        value = "${profile.skuProgress}%",
                        icon = Icons.Default.MilitaryTech,
                        iconBgColor = ScoutBrownContainer,
                        iconTint = ScoutBrown,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Menu Utama in 2-Column Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Menu Utama Pramuka Garuda",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SneatTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Row 1: Tingkatan & Kegiatan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeGridMenuCard(
                        title = "Tingkatan Pramuka",
                        subtitle = "Siaga s.d. Bina Damping",
                        badgeText = "6 Golongan",
                        badgeBg = ScoutGreenContainer,
                        badgeColor = ScoutGreen,
                        icon = Icons.Default.MilitaryTech,
                        iconBg = ScoutGreenContainer,
                        iconTint = ScoutGreen,
                        onClick = onNavigateToTingkatan,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("menu_tingkatan_pramuka")
                    )
                    HomeGridMenuCard(
                        title = "Kegiatan Pramuka",
                        subtitle = "Latihan & Persami",
                        badgeText = "${activities.size} Jadwal",
                        badgeBg = ScoutYellowContainer,
                        badgeColor = ScoutBrown,
                        icon = Icons.Default.DateRange,
                        iconBg = ScoutYellowContainer,
                        iconTint = ScoutBrown,
                        onClick = onNavigateToKegiatan,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("menu_kegiatan_pramuka")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Materi & Pusat Bantuan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeGridMenuCard(
                        title = "Materi Pramuka",
                        subtitle = "Sandi, Morse, P3K",
                        badgeText = "$materialsCount Modul",
                        badgeBg = ScoutBrownContainer,
                        badgeColor = ScoutBrown,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconBg = ScoutBrownContainer,
                        iconTint = ScoutBrown,
                        onClick = onNavigateToMateri,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("menu_materi_pramuka")
                    )
                    HomeGridMenuCard(
                        title = "Pusat Bantuan",
                        subtitle = "FAQ & Pembina",
                        badgeText = "Gratis",
                        badgeBg = Color(0xFFE0F2FE),
                        badgeColor = SneatInfo,
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = SneatInfo,
                        onClick = onNavigateToBantuan,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("menu_pusat_bantuan")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Proker & Silabus + Profil & Akun (Dashboard Admin dipindahkan ke Fitur Akun)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeGridMenuCard(
                        title = "Proker & Silabus",
                        subtitle = "Kurikulum Semester",
                        badgeText = "$syllabiCount Proker",
                        badgeBg = ScoutYellowContainer,
                        badgeColor = ScoutBrown,
                        icon = Icons.Default.EventAvailable,
                        iconBg = ScoutYellowContainer,
                        iconTint = ScoutBrown,
                        onClick = onNavigateToSilabus,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("menu_proker_silabus")
                    )
                    HomeGridMenuCard(
                        title = "Profil & Akun",
                        subtitle = "KTA & Menu Pembina",
                        badgeText = profile.npa,
                        badgeBg = ScoutGreenContainer,
                        badgeColor = ScoutGreen,
                        icon = Icons.Default.Person,
                        iconBg = ScoutGreenContainer,
                        iconTint = ScoutGreen,
                        onClick = onNavigateToProfil,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("menu_profil_kta")
                    )
                }
            }
        }

        // Jadwal Kegiatan Table Section on Home
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tabel Jadwal Kegiatan Terdekat",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SneatTextPrimary
                        )
                    )
                    Text(
                        text = "Lihat Semua →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScoutGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToKegiatan() }
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SneatTableContainer(modifier = Modifier.testTag("home_activities_table")) {
                    SneatTableHeaderRow {
                        SneatTableHeaderCell("Kegiatan", weight = 1.6f)
                        SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                        SneatTableHeaderCell("Tanggal", weight = 1.1f)
                        SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                        SneatTableHeaderCell("Status", weight = 0.9f, alignment = Alignment.Center)
                    }

                    if (activities.isEmpty()) {
                        SneatTableDataRow(index = 0) {
                            SneatTableCell(weight = 1f, alignment = Alignment.Center) {
                                Text(
                                    text = "Belum ada jadwal kegiatan terdekat.",
                                    fontSize = 12.sp,
                                    color = SneatTextSecondary
                                )
                            }
                        }
                    } else {
                        activities.take(4).forEachIndexed { index, act ->
                            SneatTableDataRow(
                                index = index,
                                onClick = onNavigateToKegiatan
                            ) {
                                SneatTableCell(weight = 1.6f) {
                                    Column {
                                        Text(
                                            text = act.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SneatTextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = act.category,
                                            fontSize = 10.sp,
                                            color = ScoutBrown
                                        )
                                    }
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 1.1f) {
                                    Column {
                                        Text(
                                            text = act.date,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SneatTextPrimary
                                        )
                                        Text(
                                            text = act.location,
                                            fontSize = 10.sp,
                                            color = SneatTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.9f, alignment = Alignment.Center) {
                                    SneatBadge(
                                        text = if (act.isUserRegistered) "Terdaftar" else act.status,
                                        backgroundColor = if (act.isUserRegistered) SneatSuccess else if (act.status == "Selesai") Color(0xFFE5E7EB) else ScoutGreenContainer,
                                        textColor = if (act.isUserRegistered) Color.White else if (act.status == "Selesai") SneatTextSecondary else ScoutGreen
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
private fun HomeGridMenuCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = SneatBorder,
    containerBg: Color = SneatSurface
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                SneatBadge(
                    text = badgeText,
                    backgroundColor = badgeBg,
                    textColor = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = SneatTextPrimary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SneatTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
