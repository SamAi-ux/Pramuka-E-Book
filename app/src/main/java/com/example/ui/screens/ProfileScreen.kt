package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.onGoogleSignInClicked
import com.example.model.AdminConfigProfile
import com.example.model.AdminRole
import com.example.model.AdminVerificationState
import com.example.model.InstitutionConfig
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.ui.components.InstitutionLogoImage
import com.example.ui.components.ProfilePhotoSelectorSection
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
import com.example.ui.components.SneatTopBar
import com.example.ui.components.scoutInputTextStyle
import com.example.ui.components.scoutTextFieldColors
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutBrownDark
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutGreenDark
import com.example.ui.theme.ScoutYellow
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatDanger
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary
import com.example.ui.theme.SneatWarning
import com.example.viewmodel.FirebaseSyncState
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(
    profile: ScoutProfile,
    userEmail: String = "",
    institutionConfig: InstitutionConfig = InstitutionConfig(),
    adminConfig: AdminConfigProfile = AdminConfigProfile(),
    isProUnlocked: Boolean = false,
    adminAccess: Boolean = false,
    syncState: FirebaseSyncState = FirebaseSyncState.LocalOnly,
    adminVerificationState: AdminVerificationState = AdminVerificationState.Unauthenticated,
    adminRecords: List<AdminRole> = emptyList(),
    currentFirebaseUid: String = "",
    adminActionFeedback: String? = null,
    adminLockError: String? = null,
    onNavigateToDashboard: () -> Unit = {},
    onCreateNewAccount: (
        name: String,
        email: String,
        school: String,
        gudep: String,
        rank: ScoutRank,
        phone: String
    ) -> Unit = { _, _, _, _, _, _ -> },
    onSyncToFirebase: () -> Unit = {},
    onFirebaseGoogleLinked: (FirebaseUser) -> Unit = {},
    onVerifyFirebaseAdmin: () -> Unit = {},
    onFirebaseAdminGoogleSignIn: (FirebaseUser) -> Unit = {},
    onGrantAdminRole: (targetUid: String, targetEmail: String) -> Unit = { _, _ -> },
    onClearAdminFeedback: () -> Unit = {},
    onOpenAdminSection: () -> Unit = {},
    onUpdateProfile: (ScoutProfile) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateAccountDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAdminDeniedDialog by remember { mutableStateOf(false) }
    var showManageAdminsDialog by remember { mutableStateOf(false) }
    var isLinkingGoogle by remember { mutableStateOf(false) }
    var isAuthenticatingAdmin by remember { mutableStateOf(false) }
    var linkError by remember { mutableStateOf<String?>(null) }
    var adminAuthLocalError by remember { mutableStateOf<String?>(null) }

    val isFirebaseAdminVerified = adminVerificationState is AdminVerificationState.Authorized
    val isAdminChecking = adminVerificationState is AdminVerificationState.Checking
    val isPembina = profile.rank == ScoutRank.PEMBINA
    val hasFirebaseUser = remember(syncState, userEmail, currentFirebaseUid, adminVerificationState) {
        currentFirebaseUid.isNotBlank() || runCatching {
            FirebaseApp.getApps(context).isNotEmpty() && Firebase.auth.currentUser != null
        }.getOrDefault(false)
    }
    val resolvedUid = remember(currentFirebaseUid, hasFirebaseUser) {
        currentFirebaseUid.ifBlank {
            runCatching {
                if (FirebaseApp.getApps(context).isNotEmpty()) Firebase.auth.currentUser?.uid.orEmpty() else ""
            }.getOrDefault("")
        }
    }

    val biodataRows = buildList {
        add("Nama Lengkap" to profile.name)
        if (userEmail.isNotBlank()) {
            add("Email Akun" to userEmail)
        }
        add("Asal Sekolah" to profile.school)
        add("Gugus Depan" to profile.gudep)
        add("Tingkatan" to profile.rank.title)
        add("Nomor Telepon" to profile.phone)
        add("Status Registrasi" to "Telah Terdaftar (Permanen)")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .testTag("profile_screen_container")
    ) {
        SneatTopBar(
            title = "Halaman Profil",
            subtitle = "Salam Pramuka • ${profile.rank.title}",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_scroll_list"),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Auto-Login Landing Hero Card: Foto Profil, Sapaan + Nama + Tingkatan,
            //    Tombol Masuk ke Dashboard, Tombol Buat Akun Baru (Pembina Only), Tombol Logout (All Users)
            item {
                SneatCard(
                    backgroundColor = SneatSurface,
                    borderColor = ScoutGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_landing_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ScoutGreenContainer)
                                    .border(2.5.dp, ScoutGreen, CircleShape)
                                    .clickable { showEditDialog = true }
                                    .testTag("profile_landing_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                ScoutAvatarImage(
                                    avatarId = profile.avatarId,
                                    customPhotoPath = profile.customPhotoPath,
                                    emojiSize = 34.sp,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_landing_greeting"),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Salam Pramuka, Selamat Datang!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScoutGreen
                                )
                                Text(
                                    text = profile.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SneatTextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SneatBadge(
                                        text = "Tingkatan: ${profile.rank.title}",
                                        backgroundColor = ScoutYellowContainer,
                                        textColor = ScoutBrown
                                    )
                                }
                            }
                        }

                        // Primary Action: Masuk ke Dashboard
                        Button(
                            onClick = onNavigateToDashboard,
                            colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_enter_dashboard")
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masuk ke Dashboard", fontWeight = FontWeight.Bold)
                        }

                        // Pembina-Only Action: Buat Akun Baru
                        if (isPembina) {
                            Button(
                                onClick = { showCreateAccountDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_create_new_account")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buat Akun Baru", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Always Visible for ALL Users: Logout
                        OutlinedButton(
                            onClick = { showLogoutConfirm = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SneatDanger.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_logout")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = SneatDanger,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout", color = SneatDanger, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Firebase Cloud Sync Card
            item {
                val syncIcon = when (syncState) {
                    is FirebaseSyncState.Synced -> Icons.Default.CloudDone
                    is FirebaseSyncState.Syncing -> Icons.Default.CloudSync
                    else -> Icons.Default.CloudOff
                }
                val syncTint = when (syncState) {
                    is FirebaseSyncState.Synced -> SneatSuccess
                    is FirebaseSyncState.Syncing -> ScoutBrown
                    is FirebaseSyncState.Error -> SneatDanger
                    FirebaseSyncState.LocalOnly -> SneatTextSecondary
                }
                val syncStatusText = when (syncState) {
                    is FirebaseSyncState.Synced -> syncState.message
                    is FirebaseSyncState.Syncing -> "Menyinkronkan data ke Firebase..."
                    is FirebaseSyncState.Error -> syncState.message
                    FirebaseSyncState.LocalOnly -> if (hasFirebaseUser) {
                        "Akun Firebase terhubung. Ketuk Sinkronkan untuk memperbarui data cloud."
                    } else {
                        "Mode lokal aktif. Hubungkan Akun Google untuk sinkronisasi ke Firebase."
                    }
                }

                SneatCard(
                    backgroundColor = SneatSurface,
                    borderColor = SneatBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_sync_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = syncIcon,
                                    contentDescription = "Status Sinkronisasi Firebase",
                                    tint = syncTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Sinkronisasi Cloud Firebase",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SneatTextPrimary
                                    )
                                    Text(
                                        text = syncStatusText,
                                        fontSize = 11.sp,
                                        color = syncTint
                                    )
                                }
                            }
                            if (syncState is FirebaseSyncState.Syncing || isLinkingGoogle) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = ScoutGreen
                                )
                            }
                        }

                        if (!linkError.isNullOrBlank()) {
                            Text(
                                text = linkError ?: "",
                                fontSize = 11.sp,
                                color = SneatDanger
                            )
                        }

                        if (hasFirebaseUser) {
                            OutlinedButton(
                                onClick = onSyncToFirebase,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_sync_firebase")
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = ScoutGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sinkronkan Data ke Firebase", color = ScoutGreen, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                enabled = !isLinkingGoogle,
                                onClick = {
                                    linkError = null
                                    isLinkingGoogle = true
                                    onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = { user ->
                                            isLinkingGoogle = false
                                            onFirebaseGoogleLinked(user)
                                        },
                                        onAuthError = { msg ->
                                            isLinkingGoogle = false
                                            linkError = msg
                                        },
                                        scope = scope,
                                        onAuthCancelled = {
                                            isLinkingGoogle = false
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_link_google_firebase")
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = ScoutBrown, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hubungkan Akun Google & Sinkronkan", color = ScoutBrown, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Digital Scout ID Card (KTA Pramuka Garuda)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutBrownDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kta_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ScoutGreenDark, ScoutBrownDark, ScoutGreen)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(ScoutYellow),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        InstitutionLogoImage(
                                            logoPath = institutionConfig.logoPath,
                                            contentDescription = "Logo ${institutionConfig.name}",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = institutionConfig.name.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = ScoutYellow,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = "KARTU TANDA ANGGOTA GARUDA",
                                            fontSize = 8.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                SneatBadge(
                                    text = "GARUDA",
                                    backgroundColor = ScoutYellow,
                                    textColor = ScoutBrown
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .border(1.5.dp, ScoutYellow, RoundedCornerShape(10.dp))
                                        .clickable { showEditDialog = true }
                                        .testTag("kta_avatar_box"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ScoutAvatarImage(
                                        avatarId = profile.avatarId,
                                        customPhotoPath = profile.customPhotoPath,
                                        emojiSize = 32.sp,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "NPA: ${profile.npa}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ScoutYellow
                                    )
                                    Text(
                                        text = institutionConfig.name,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "Gudep: ${institutionConfig.gudep}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = "QR Code KTA",
                                        tint = ScoutGreenDark,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tingkat: ${profile.rank.title} Garuda",
                                    fontSize = 10.sp,
                                    color = ScoutYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Status: Aktif Terverifikasi",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. 2-Column Achievement Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SneatStatCard(
                        title = "Progress SKU",
                        value = "${profile.skuProgress}%",
                        icon = Icons.Default.MilitaryTech,
                        iconBgColor = ScoutGreenContainer,
                        iconTint = ScoutGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SneatStatCard(
                        title = "Koleksi TKK",
                        value = "${profile.tkkCount} Lencana",
                        icon = Icons.Default.Star,
                        iconBgColor = ScoutYellowContainer,
                        iconTint = ScoutBrown,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Biodata Table Header + Table
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tabel Biodata Anggota",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ScoutGreen
                        )
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profil",
                                tint = ScoutGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    SneatTableContainer(modifier = Modifier.testTag("profile_biodata_table")) {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell(text = "Kolom Data", weight = 0.38f)
                            SneatTableColumnDivider(color = Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell(text = "Keterangan", weight = 0.62f)
                        }

                        biodataRows.forEachIndexed { index, (label, value) ->
                            SneatTableDataRow(index = index) {
                                SneatTableCell(weight = 0.38f) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScoutBrown
                                    )
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.62f) {
                                    Text(
                                        text = value,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SneatTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Secure Admin Authentication Flow inside Profile Section (Firebase Auth + /admins allowlist)
            item {
                val launchAdminGoogleSignIn = {
                    if (!isAuthenticatingAdmin) {
                        adminAuthLocalError = null
                        isAuthenticatingAdmin = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = { user ->
                                isAuthenticatingAdmin = false
                                onFirebaseAdminGoogleSignIn(user)
                            },
                            onAuthError = { msg ->
                                isAuthenticatingAdmin = false
                                adminAuthLocalError = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isAuthenticatingAdmin = false
                            }
                        )
                    }
                }

                val authBadgeText = when {
                    adminAccess -> "SESI ADMIN AKTIF"
                    isFirebaseAdminVerified -> "ADMIN TERVERIFIKASI"
                    isAdminChecking || isAuthenticatingAdmin -> "MEMERIKSA..."
                    adminVerificationState is AdminVerificationState.Denied -> "AKSES DITOLAK"
                    adminVerificationState is AdminVerificationState.Error -> "GAGAL VERIFIKASI"
                    else -> "BUTUH FIREBASE AUTH"
                }
                val authBadgeBg = when {
                    adminAccess -> SneatSuccess
                    isFirebaseAdminVerified -> ScoutGreen
                    isAdminChecking || isAuthenticatingAdmin -> ScoutBrown
                    adminVerificationState is AdminVerificationState.Denied ||
                        adminVerificationState is AdminVerificationState.Error -> SneatDanger
                    else -> SneatWarning
                }
                val cardBg = when {
                    isFirebaseAdminVerified -> ScoutGreenContainer
                    else -> SneatSurface
                }
                val cardBorder = when {
                    isFirebaseAdminVerified -> ScoutGreen
                    adminVerificationState is AdminVerificationState.Denied -> SneatDanger.copy(alpha = 0.6f)
                    else -> SneatBorder
                }

                SneatCard(
                    backgroundColor = cardBg,
                    borderColor = cardBorder,
                    onClick = {
                        when {
                            isFirebaseAdminVerified -> onOpenAdminSection()
                            adminVerificationState is AdminVerificationState.Denied -> showAdminDeniedDialog = true
                            hasFirebaseUser -> onVerifyFirebaseAdmin()
                            else -> launchAdminGoogleSignIn()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_admin_menu_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isFirebaseAdminVerified -> ScoutGreen
                                                adminVerificationState is AdminVerificationState.Denied -> Color(0xFFFEE2E2)
                                                else -> ScoutYellowContainer
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAdminChecking || isAuthenticatingAdmin) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .testTag("admin_auth_checking_indicator"),
                                            strokeWidth = 2.dp,
                                            color = ScoutBrown
                                        )
                                    } else {
                                        Icon(
                                            imageVector = when {
                                                adminAccess -> Icons.Default.LockOpen
                                                isFirebaseAdminVerified -> Icons.Default.VerifiedUser
                                                adminVerificationState is AdminVerificationState.Denied -> Icons.Default.ErrorOutline
                                                else -> Icons.Default.AdminPanelSettings
                                            },
                                            contentDescription = "Autentikasi Admin Firebase",
                                            tint = when {
                                                isFirebaseAdminVerified -> Color.White
                                                adminVerificationState is AdminVerificationState.Denied -> SneatDanger
                                                else -> ScoutBrown
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Dashboard Admin (Firebase Auth)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SneatTextPrimary
                                    )
                                    Text(
                                        text = when (val state = adminVerificationState) {
                                            is AdminVerificationState.Authorized ->
                                                "Personel berwenang: ${state.email}"
                                            is AdminVerificationState.Checking ->
                                                "Memverifikasi otoritas admin di Firebase Auth & Firestore..."
                                            is AdminVerificationState.Denied ->
                                                "Akun ${state.email} (${state.assignedRole}) belum memiliki izin admin."
                                            is AdminVerificationState.Error ->
                                                state.message
                                            AdminVerificationState.Unauthenticated ->
                                                "Terbatas hanya untuk personel berwenang via Firebase Auth."
                                        },
                                        fontSize = 11.sp,
                                        color = if (adminVerificationState is AdminVerificationState.Denied) SneatDanger else SneatTextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            SneatBadge(
                                text = authBadgeText,
                                backgroundColor = authBadgeBg,
                                textColor = Color.White,
                                modifier = Modifier.testTag("admin_auth_status_badge")
                            )
                        }

                        if (!adminActionFeedback.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScoutGreenContainer)
                                    .border(1.dp, ScoutGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("profile_admin_feedback_banner"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = adminActionFeedback,
                                    fontSize = 11.sp,
                                    color = ScoutGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onClearAdminFeedback,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Tutup notifikasi admin",
                                        tint = ScoutGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        val displayError = adminAuthLocalError
                            ?: (adminVerificationState as? AdminVerificationState.Denied)?.reason
                            ?: (adminVerificationState as? AdminVerificationState.Error)?.message
                            ?: adminLockError?.takeIf { !isFirebaseAdminVerified }
                        if (!displayError.isNullOrBlank() && !isFirebaseAdminVerified) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .padding(10.dp)
                                    .testTag("admin_auth_error_banner"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = SneatDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayError,
                                    fontSize = 11.sp,
                                    color = SneatDanger,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        if (resolvedUid.isNotBlank()) {
                            Text(
                                text = "Firebase UID: $resolvedUid",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SneatTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("profile_firebase_uid_text")
                            )
                        }

                        when {
                            isFirebaseAdminVerified -> {
                                Button(
                                    onClick = onOpenAdminSection,
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_open_admin_dashboard")
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (adminAccess) "Buka Dashboard Admin" else "Masuk Dashboard Admin Pro",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showManageAdminsDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ScoutGreen.copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_manage_admin_personnel")
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ScoutGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kelola Otoritas Personel Admin (${adminRecords.size})",
                                        color = ScoutGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            adminVerificationState is AdminVerificationState.Denied -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onVerifyFirebaseAdmin,
                                        enabled = !isAdminChecking,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_recheck_admin_auth")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ScoutGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Periksa Ulang", color = ScoutGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { launchAdminGoogleSignIn() },
                                        enabled = !isAuthenticatingAdmin,
                                        colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_switch_admin_google")
                                    ) {
                                        Text("Akun Admin Lain", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            else -> {
                                Button(
                                    onClick = {
                                        if (hasFirebaseUser) {
                                            onVerifyFirebaseAdmin()
                                        } else {
                                            launchAdminGoogleSignIn()
                                        }
                                    },
                                    enabled = !isAdminChecking && !isAuthenticatingAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_admin_google_auth")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (hasFirebaseUser) {
                                            "Verifikasi Otoritas Admin (Firebase)"
                                        } else {
                                            "Masuk Google & Verifikasi Admin"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SneatBadge(
                                text = "Firebase Auth + /admins",
                                backgroundColor = ScoutYellowContainer,
                                textColor = ScoutBrown
                            )
                            SneatBadge(
                                text = when {
                                    adminConfig.isProSemesterPaid -> "PRO 1 SEMESTER"
                                    isProUnlocked -> "PRO IKLAN AKTIF"
                                    else -> "2 OPSI PRO"
                                },
                                backgroundColor = SneatSurface,
                                textColor = ScoutGreen
                            )
                        }
                    }
                }
            }

            // 7. Edit Profile Button & Persistence Info
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_edit_profile")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ubah Data & Foto Profil", fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScoutYellowContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ℹ️ Catatan: Saat Anda membuka aplikasi kembali selama sesi masih aktif, Auto-Login langsung mengarahkan ke Halaman Profil ini. Data pendaftaran tetap tersimpan saat logout.",
                            fontSize = 11.sp,
                            color = ScoutBrown,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Create New Account Dialog (Pembina Only)
    if (showCreateAccountDialog) {
        CreateNewAccountByPembinaDialog(
            defaultSchool = institutionConfig.name,
            defaultGudep = institutionConfig.gudep,
            onDismiss = { showCreateAccountDialog = false },
            onCreate = { name, email, school, gudep, rank, phone ->
                onCreateNewAccount(name, email, school, gudep, rank, phone)
                showCreateAccountDialog = false
            }
        )
    }

    // Edit Profile Modal (includes manual Choose File / Gallery Photo picker)
    if (showEditDialog) {
        var editName by remember { mutableStateOf(profile.name) }
        var editSchool by remember { mutableStateOf(profile.school) }
        var editGudep by remember { mutableStateOf(profile.gudep) }
        var editRank by remember { mutableStateOf(profile.rank) }
        var editRankExpanded by remember { mutableStateOf(false) }
        var editPhone by remember { mutableStateOf(profile.phone) }
        var editAvatarId by remember { mutableIntStateOf(profile.avatarId) }
        var editCustomPhotoPath by remember { mutableStateOf(profile.customPhotoPath) }
        var editError by remember { mutableStateOf<String?>(null) }
        val editScrollState = rememberScrollState()
        val rankInteraction = remember { MutableInteractionSource() }
        LaunchedEffect(rankInteraction) {
            rankInteraction.interactions.collect { interaction ->
                if (interaction is PressInteraction.Release) {
                    editRankExpanded = !editRankExpanded
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SneatSurface,
            title = { Text("Ubah Data & Foto Profil", fontWeight = FontWeight.Bold, color = ScoutGreen) },
            text = {
                Column(modifier = Modifier.verticalScroll(editScrollState)) {
                    ProfilePhotoSelectorSection(
                        selectedAvatarId = editAvatarId,
                        customPhotoPath = editCustomPhotoPath,
                        onSelectPresetAvatar = { editAvatarId = it },
                        onCustomPhotoSelected = { path -> editCustomPhotoPath = path },
                        onClearCustomPhoto = { editCustomPhotoPath = "" },
                        persistedPhotoPath = profile.customPhotoPath
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; editError = null },
                        label = { Text("Nama Lengkap *") },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editSchool,
                        onValueChange = { editSchool = it; editError = null },
                        label = { Text("Asal Sekolah *") },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editGudep,
                        onValueChange = { editGudep = it; editError = null },
                        label = { Text("Nomor Gudep *") },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editRank.title,
                            onValueChange = {},
                            readOnly = true,
                            interactionSource = rankInteraction,
                            label = { Text("Tingkatan Pramuka") },
                            trailingIcon = {
                                IconButton(onClick = { editRankExpanded = !editRankExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Tingkatan")
                                }
                            },
                            textStyle = scoutInputTextStyle,
                            colors = scoutTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = editRankExpanded,
                            onDismissRequest = { editRankExpanded = false },
                            modifier = Modifier.background(SneatSurface)
                        ) {
                            ScoutRank.entries.forEach { rank ->
                                DropdownMenuItem(
                                    text = { Text("${rank.title} (${rank.ageGroup})", color = SneatTextPrimary) },
                                    onClick = {
                                        editRank = rank
                                        editRankExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Nomor HP") },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (editError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = editError ?: "",
                            fontSize = 11.sp,
                            color = SneatDanger,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            editName.isBlank() -> editError = "Nama lengkap tidak boleh kosong."
                            editSchool.isBlank() -> editError = "Asal sekolah tidak boleh kosong."
                            editGudep.isBlank() -> editError = "Nomor Gudep tidak boleh kosong."
                            else -> {
                                onUpdateProfile(
                                    profile.copy(
                                        name = editName.trim(),
                                        school = editSchool.trim(),
                                        gudep = editGudep.trim(),
                                        rank = editRank,
                                        phone = editPhone.trim().ifBlank { "-" },
                                        avatarId = editAvatarId,
                                        customPhotoPath = editCustomPhotoPath
                                    )
                                )
                                showEditDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = SneatSurface,
            title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold, color = SneatDanger) },
            text = {
                Text(
                    "Apakah Anda yakin ingin keluar dari aplikasi Pramuka Garuda? Data keanggotaan dan profil Anda tetap tersimpan dan tidak perlu mendaftar ulang saat masuk kembali.",
                    fontSize = 12.sp,
                    color = SneatTextPrimary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SneatDanger),
                    modifier = Modifier.testTag("btn_confirm_logout")
                ) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog when signed-in account does not hold Firebase Admin authority
    if (showAdminDeniedDialog) {
        val deniedState = adminVerificationState as? AdminVerificationState.Denied
        AlertDialog(
            onDismissRequest = { showAdminDeniedDialog = false },
            containerColor = SneatSurface,
            title = {
                Text(
                    "Akses Khusus Personel Admin",
                    fontWeight = FontWeight.Bold,
                    color = SneatDanger
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = deniedState?.reason
                            ?: "Akun Anda belum terdaftar sebagai personel admin yang sah pada Firebase Auth & koleksi /admins di Firestore.",
                        fontSize = 12.sp,
                        color = SneatTextPrimary,
                        lineHeight = 18.sp
                    )
                    if (resolvedUid.isNotBlank()) {
                        Text(
                            text = "UID Anda: $resolvedUid\nMinta admin utama untuk menambahkan UID & Email Anda di menu Kelola Otoritas Personel Admin.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SneatTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdminDeniedDialog = false
                        onVerifyFirebaseAdmin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen)
                ) {
                    Text("Periksa Ulang Otoritas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDeniedDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog for Authorized Firebase Admin to view & grant admin personnel authority inside Profile
    if (showManageAdminsDialog && isFirebaseAdminVerified) {
        ManageAdminPersonnelDialog(
            adminRecords = adminRecords,
            currentAdminUid = resolvedUid,
            onDismiss = { showManageAdminsDialog = false },
            onGrantAdmin = { targetUid, targetEmail ->
                onGrantAdminRole(targetUid, targetEmail)
                showManageAdminsDialog = false
            }
        )
    }
}

@Composable
private fun ManageAdminPersonnelDialog(
    adminRecords: List<AdminRole>,
    currentAdminUid: String,
    onDismiss: () -> Unit,
    onGrantAdmin: (targetUid: String, targetEmail: String) -> Unit
) {
    var targetUid by remember { mutableStateOf("") }
    var targetEmail by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SneatSurface,
        title = {
            Text(
                "Kelola Personel Admin (Firebase)",
                fontWeight = FontWeight.Bold,
                color = ScoutGreen
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .testTag("dialog_manage_admins"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Hanya akun yang terverifikasi di Firebase Auth dan terdaftar pada koleksi /admins Firestore yang dapat membuka Dashboard Admin.",
                    fontSize = 11.sp,
                    color = SneatTextSecondary,
                    lineHeight = 16.sp
                )

                if (currentAdminUid.isNotBlank()) {
                    Text(
                        text = "UID Admin Aktif: $currentAdminUid",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ScoutBrown
                    )
                }

                if (adminRecords.isNotEmpty()) {
                    SneatTableContainer {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell("Email & UID Admin", weight = 0.72f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Status", weight = 0.28f, alignment = Alignment.Center)
                        }
                        adminRecords.forEachIndexed { idx, record ->
                            SneatTableDataRow(index = idx) {
                                SneatTableCell(weight = 0.72f) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = record.email.ifBlank { "-" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SneatTextPrimary
                                        )
                                        Text(
                                            text = record.adminUid.ifBlank { record.id },
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = SneatTextSecondary
                                        )
                                    }
                                }
                                SneatTableColumnDivider()
                                SneatTableCell(weight = 0.28f, alignment = Alignment.Center) {
                                    SneatBadge(
                                        text = if (record.active) "AKTIF" else "NONAKTIF",
                                        backgroundColor = if (record.active) ScoutGreenContainer else Color(0xFFFEE2E2),
                                        textColor = if (record.active) ScoutGreen else SneatDanger
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Tambah Personel Admin Baru:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SneatTextPrimary
                )

                OutlinedTextField(
                    value = targetUid,
                    onValueChange = { targetUid = it; formError = null },
                    label = { Text("Firebase Auth UID Target *") },
                    placeholder = { Text("Contoh: xYz123AbC...") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_grant_admin_uid")
                )

                OutlinedTextField(
                    value = targetEmail,
                    onValueChange = { targetEmail = it; formError = null },
                    label = { Text("Email Google Target *") },
                    placeholder = { Text("pembina@gmail.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_grant_admin_email")
                )

                if (formError != null) {
                    Text(
                        text = formError ?: "",
                        fontSize = 11.sp,
                        color = SneatDanger,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanUid = targetUid.trim()
                    val cleanEmail = targetEmail.trim()
                    when {
                        !cleanUid.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$")) ->
                            formError = "UID harus 1–128 karakter alfanumerik, '-' atau '_'."
                        cleanEmail.length !in 3..200 || !cleanEmail.contains("@") ->
                            formError = "Masukkan alamat email Google yang valid."
                        else -> onGrantAdmin(cleanUid, cleanEmail)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                modifier = Modifier.testTag("btn_submit_grant_admin")
            ) {
                Text("Berikan Akses Admin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun CreateNewAccountByPembinaDialog(
    defaultSchool: String,
    defaultGudep: String,
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        email: String,
        school: String,
        gudep: String,
        rank: ScoutRank,
        phone: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var school by remember { mutableStateOf(defaultSchool) }
    var gudep by remember { mutableStateOf(defaultGudep) }
    var selectedRank by remember { mutableStateOf(ScoutRank.PENGGALANG) }
    var rankExpanded by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

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
                "Buat Akun Baru (Khusus Pembina)",
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
                    text = "Daftarkan akun anggota/bina damping baru ke daftar Pangkalan & sinkronkan ke Firebase.",
                    fontSize = 11.sp,
                    color = SneatTextSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; formError = null },
                    label = { Text("Nama Lengkap *") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("create_acc_name")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; formError = null },
                    label = { Text("Email Gmail / ID Anggota (Opsional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("create_acc_email")
                )
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("Sekolah / Pangkalan") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gudep,
                    onValueChange = { gudep = it },
                    label = { Text("Nomor Gudep") },
                    singleLine = true,
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRank.title,
                        onValueChange = {},
                        readOnly = true,
                        interactionSource = rankInteraction,
                        label = { Text("Tingkatan Pramuka") },
                        trailingIcon = {
                            IconButton(onClick = { rankExpanded = !rankExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Tingkatan")
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP / WhatsApp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (formError != null) {
                    Text(text = formError ?: "", fontSize = 11.sp, color = SneatDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        formError = "Nama lengkap wajib diisi."
                    } else {
                        onCreate(name, email, school, gudep, selectedRank, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                modifier = Modifier.testTag("btn_submit_create_acc")
            ) {
                Text("Simpan Akun Baru")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
