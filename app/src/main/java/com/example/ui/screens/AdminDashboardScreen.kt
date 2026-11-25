package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.model.AdminConfigProfile
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus
import com.example.ui.components.ScoutAvatarImage
import com.example.ui.components.SneatBadge
import com.example.ui.components.SneatStatCard
import com.example.ui.components.SneatTopBar
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutBrownContainer
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatInfo
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun AdminDashboardScreen(
    institutionConfig: InstitutionConfig,
    adminConfig: AdminConfigProfile,
    members: List<ScoutMember>,
    materials: List<ScoutMaterial>,
    syllabi: List<SemesterSyllabus>,
    activities: List<ScoutActivity>,
    announcements: List<ScoutAnnouncement>,
    adminActionFeedback: String?,
    onSyncToFirebase: () -> Unit = {},
    onUpdateInstitution: (name: String, gudep: String, logoPath: String) -> Unit,
    onAddMaterial: (title: String, category: String, summary: String, content: String, details: List<String>) -> Unit,
    onUpdateMaterial: (id: String, title: String, category: String, summary: String, content: String, details: List<String>) -> Unit,
    onAddSyllabus: (semester: String, rank: ScoutRank, programTitle: String, weekRange: String, competencyTarget: String, method: String, status: String) -> Unit,
    onUpdateSyllabus: (id: String, semester: String, rank: ScoutRank, programTitle: String, weekRange: String, competencyTarget: String, method: String, status: String) -> Unit,
    onAddActivity: (title: String, category: String, date: String, time: String, location: String, leader: String, quota: Int, desc: String) -> Unit,
    onAddAnnouncement: (title: String, content: String, priority: String) -> Unit,
    onAddMember: (name: String, school: String, gudep: String, rank: ScoutRank, skuLevel: String, phone: String) -> Unit,
    onToggleMemberVerified: (String) -> Unit,
    onUpdateAdminPin: (String) -> Unit,
    onClearAdminFeedback: () -> Unit,
    onLockAdminAccess: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var showMaterialDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<ScoutMaterial?>(null) }

    var showSyllabusDialog by remember { mutableStateOf(false) }
    var editingSyllabus by remember { mutableStateOf<SemesterSyllabus?>(null) }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .imePadding()
            .testTag("admin_dashboard_container")
    ) {
        SneatTopBar(
            title = "Dashboard Admin Pro",
            subtitle = "${institutionConfig.name} • Gudep ${institutionConfig.gudep}",
            onBackClick = onBack,
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(ScoutYellowContainer)
                            .border(1.dp, ScoutGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ScoutAvatarImage(
                            avatarId = 1,
                            customPhotoPath = adminConfig.photoPath,
                            emojiSize = 16.sp,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    SneatBadge(
                        text = if (adminConfig.isProSemesterPaid) "PRO 1 SEM" else "PRO IKLAN",
                        backgroundColor = ScoutGreenContainer,
                        textColor = ScoutGreen
                    )
                }
            }
        )

        // Admin Identity Bar + Cloud Sync Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SneatSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${adminConfig.fullName.ifBlank { "Admin Pembina" }} (${adminConfig.rank.title})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SneatTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.TextButton(
                onClick = onSyncToFirebase,
                modifier = Modifier.testTag("btn_admin_sync_firebase")
            ) {
                Text(
                    text = "Sinkron Firebase",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoutGreen
                )
            }
        }

        // Feedback Banner
        if (adminActionFeedback != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutGreenContainer),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ScoutGreen)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("admin_feedback_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = adminActionFeedback,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScoutGreen,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearAdminFeedback, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup pesan", tint = ScoutGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Sneat 2x2 Metric Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SneatStatCard(
                    title = "Total Anggota",
                    value = "${members.size}",
                    icon = Icons.Default.Group,
                    iconBgColor = ScoutGreenContainer,
                    iconTint = ScoutGreen,
                    modifier = Modifier.weight(1f)
                )
                SneatStatCard(
                    title = "Modul Materi",
                    value = "${materials.size}",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
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
                    title = "Proker & Silabus",
                    value = "${syllabi.size}",
                    icon = Icons.Default.EventAvailable,
                    iconBgColor = ScoutBrownContainer,
                    iconTint = ScoutBrown,
                    modifier = Modifier.weight(1f)
                )
                SneatStatCard(
                    title = "Agenda Kegiatan",
                    value = "${activities.size}",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    iconBgColor = Color(0xFFE0F2FE),
                    iconTint = SneatInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val tabs = listOf(
            "Lembaga & Logo",
            "Kelola Materi",
            "Proker & Silabus",
            "Kegiatan",
            "Pengumuman",
            "Anggota & PIN"
        )
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SneatSurface,
            contentColor = ScoutGreen,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ScoutGreen,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) ScoutGreen else SneatTextSecondary,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> InstitutionIdentityTab(
                institutionConfig = institutionConfig,
                onSaveInstitution = onUpdateInstitution
            )
            1 -> AdminMaterialsTab(
                materials = materials,
                onAddClick = {
                    editingMaterial = null
                    showMaterialDialog = true
                },
                onEditClick = { mat ->
                    editingMaterial = mat
                    showMaterialDialog = true
                }
            )
            2 -> AdminSyllabusTab(
                syllabi = syllabi,
                onAddClick = {
                    editingSyllabus = null
                    showSyllabusDialog = true
                },
                onEditClick = { syl ->
                    editingSyllabus = syl
                    showSyllabusDialog = true
                }
            )
            3 -> AdminActivitiesTab(
                activities = activities,
                onAddClick = { showAddActivityDialog = true }
            )
            4 -> AdminAnnouncementsTab(
                announcements = announcements,
                onAddClick = { showAddAnnouncementDialog = true }
            )
            5 -> AdminMembersAndPinTab(
                members = members,
                adminConfig = adminConfig,
                onAddMemberClick = { showAddMemberDialog = true },
                onToggleMemberVerified = onToggleMemberVerified,
                onUpdateAdminPin = onUpdateAdminPin,
                onLockAdminAccess = onLockAdminAccess
            )
        }
    }

    if (showMaterialDialog) {
        MaterialFormDialog(
            initialMaterial = editingMaterial,
            onDismiss = { showMaterialDialog = false },
            onSave = { title, category, summary, content, details ->
                val current = editingMaterial
                if (current == null) {
                    onAddMaterial(title, category, summary, content, details)
                } else {
                    onUpdateMaterial(current.id, title, category, summary, content, details)
                }
                showMaterialDialog = false
            }
        )
    }

    if (showSyllabusDialog) {
        SyllabusFormDialog(
            initialSyllabus = editingSyllabus,
            onDismiss = { showSyllabusDialog = false },
            onSave = { semester, rank, programTitle, weekRange, competencyTarget, method, status ->
                val current = editingSyllabus
                if (current == null) {
                    onAddSyllabus(semester, rank, programTitle, weekRange, competencyTarget, method, status)
                } else {
                    onUpdateSyllabus(current.id, semester, rank, programTitle, weekRange, competencyTarget, method, status)
                }
                showSyllabusDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAddMember = { name, school, gudep, rank, skuLevel, phone ->
                onAddMember(name, school, gudep, rank, skuLevel, phone)
                showAddMemberDialog = false
            }
        )
    }

    if (showAddActivityDialog) {
        AddActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onAddActivity = { title, category, date, time, location, leader, quota, desc ->
                onAddActivity(title, category, date, time, location, leader, quota, desc)
                showAddActivityDialog = false
            }
        )
    }

    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            onDismiss = { showAddAnnouncementDialog = false },
            onAddAnnouncement = { title, content, priority ->
                onAddAnnouncement(title, content, priority)
                showAddAnnouncementDialog = false
            }
        )
    }
}
