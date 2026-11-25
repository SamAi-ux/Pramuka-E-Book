package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.RewardedAdState
import com.example.model.AdminConfigProfile
import com.example.model.AdminVerificationState
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.ui.components.ProfilePhotoSelectorSection
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
import com.example.ui.theme.ScoutYellow
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatDanger
import com.example.ui.theme.SneatSuccess
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun AdminLockScreen(
    profile: ScoutProfile,
    isProUnlocked: Boolean,
    adminConfig: AdminConfigProfile,
    lockError: String?,
    adminVerificationState: AdminVerificationState = AdminVerificationState.Authorized(
        uid = "",
        email = "",
        displayName = ""
    ),
    rewardedAdState: RewardedAdState = RewardedAdState.Idle,
    isUsingTestAdIds: Boolean = true,
    adNotice: String? = null,
    onWatchRewardedAd: () -> Unit = {},
    onRetryLoadAd: () -> Unit = {},
    onRetryFirebaseVerification: () -> Unit = {},
    onUnlockWithPayment: () -> Unit,
    onSubmitFirstSetup: (
        fullName: String,
        rank: ScoutRank,
        gudep: String,
        institutionName: String,
        photoPath: String,
        pin: String
    ) -> Unit,
    onVerifyKeywordAccess: (nameInput: String, rankInput: ScoutRank, pinInput: String) -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .imePadding()
            .testTag("admin_lock_screen_container")
    ) {
        SneatTopBar(
            title = "Akses Dashboard Admin",
            subtitle = "Khusus Pembina & Bina Damping (Pro Gate)",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Emblem
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(if (isProUnlocked) ScoutGreenContainer else ScoutYellowContainer)
                            .border(
                                width = 3.dp,
                                color = if (isProUnlocked) ScoutGreen else ScoutYellow,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                !isProUnlocked -> Icons.Default.WorkspacePremium
                                !adminConfig.isInitialized -> Icons.Default.AdminPanelSettings
                                else -> Icons.Default.Lock
                            },
                            contentDescription = "Status Akses Dashboard Admin",
                            tint = if (isProUnlocked) ScoutGreen else ScoutBrown,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when {
                            !isProUnlocked -> "Aktivasi Akses Dashboard Pro"
                            !adminConfig.isInitialized -> "Pendaftaran Awal Admin Pembina"
                            else -> "Verifikasi Kata Kunci Dashboard Admin"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SneatTextPrimary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            !isProUnlocked -> "Dashboard Admin hanya dapat dibuka dengan memilih salah satu dari 2 opsi akses Pro di bawah ini."
                            !adminConfig.isInitialized -> "Karena baru pertama kali masuk Dashboard Admin, silakan lengkapi data diri lembaga dan buat PIN akses Anda."
                            else -> "Masukkan kata kunci Nama, Tingkatan Pramuka, dan PIN Akses Dashboard untuk membuka panel pengelolaan."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SneatTextSecondary,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Step 0: Defense-in-depth Firebase Auth Admin Gate
            if (adminVerificationState !is AdminVerificationState.Authorized) {
                item {
                    SneatCard(
                        backgroundColor = SneatSurface,
                        borderColor = SneatDanger.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_lock_firebase_required_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Akses Admin Dibatasi",
                                    tint = SneatDanger,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verifikasi Firebase Auth Diperlukan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SneatDanger
                                )
                            }
                            Text(
                                text = when (adminVerificationState) {
                                    is AdminVerificationState.Checking ->
                                        "Sedang memverifikasi otoritas admin di Firebase Auth & Firestore..."
                                    is AdminVerificationState.Denied ->
                                        adminVerificationState.reason
                                    is AdminVerificationState.Error ->
                                        adminVerificationState.message
                                    AdminVerificationState.Unauthenticated ->
                                        "Dashboard Admin hanya dapat diakses personel berwenang yang masuk melalui Firebase Auth di Fitur Akun / Profil."
                                    is AdminVerificationState.Authorized -> ""
                                },
                                fontSize = 12.sp,
                                color = SneatTextSecondary,
                                lineHeight = 17.sp
                            )
                            Button(
                                onClick = onRetryFirebaseVerification,
                                enabled = adminVerificationState !is AdminVerificationState.Checking,
                                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_admin_lock_retry_firebase")
                            ) {
                                Text("Periksa Ulang Otoritas Firebase", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (!isProUnlocked) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Opsi 1: Tonton Iklan (Google AdMob RewardedAd)
                        SneatCard(
                            backgroundColor = SneatSurface,
                            borderColor = ScoutGreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pro_option_ad_card")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = ScoutGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Opsi 1: Melihat Iklan AdMob",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ScoutGreen
                                        )
                                    }
                                    SneatBadge(
                                        text = when (rewardedAdState) {
                                            RewardedAdState.Ready -> if (isUsingTestAdIds) "ADMOB UJI SIAP" else "ADMOB SIAP"
                                            RewardedAdState.Loading -> "MEMUAT..."
                                            RewardedAdState.Showing -> "TAYANG"
                                            is RewardedAdState.Failed -> "MUAT ULANG"
                                            RewardedAdState.Idle -> "GRATIS"
                                        },
                                        backgroundColor = if (rewardedAdState is RewardedAdState.Failed) {
                                            Color(0xFFFEE2E2)
                                        } else {
                                            ScoutGreenContainer
                                        },
                                        textColor = if (rewardedAdState is RewardedAdState.Failed) {
                                            SneatDanger
                                        } else {
                                            ScoutGreen
                                        }
                                    )
                                }

                                Text(
                                    text = "Tonton tayangan iklan video Google AdMob Rewarded hingga selesai untuk membuka akses User Dashboard Pro pada sesi ini.",
                                    fontSize = 12.sp,
                                    color = SneatTextSecondary,
                                    lineHeight = 17.sp
                                )

                                if (rewardedAdState is RewardedAdState.Failed || !adNotice.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(10.dp)
                                            .testTag("admob_status_notice"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = SneatDanger,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (rewardedAdState is RewardedAdState.Failed) {
                                                rewardedAdState.message
                                            } else {
                                                adNotice.orEmpty()
                                            },
                                            fontSize = 11.sp,
                                            color = SneatDanger,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                val isAdBusy = rewardedAdState is RewardedAdState.Loading ||
                                    rewardedAdState is RewardedAdState.Showing

                                Button(
                                    onClick = {
                                        when (rewardedAdState) {
                                            RewardedAdState.Ready -> onWatchRewardedAd()
                                            else -> onRetryLoadAd()
                                        }
                                    },
                                    enabled = !isAdBusy,
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_unlock_ad_pro")
                                ) {
                                    if (isAdBusy) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (rewardedAdState is RewardedAdState.Showing) {
                                                "Menayangkan Iklan AdMob..."
                                            } else {
                                                "Memuat Iklan AdMob..."
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (rewardedAdState) {
                                                RewardedAdState.Ready -> "Tonton Iklan AdMob & Buka Pro"
                                                is RewardedAdState.Failed -> "Muat Ulang Iklan AdMob"
                                                else -> "Siapkan Iklan AdMob"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Opsi 2: Berlangganan Pro Rp 20.000 / 1 Semester
                        SneatCard(
                            backgroundColor = ScoutYellowContainer,
                            borderColor = ScoutBrown,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pro_option_paid_card")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = ScoutBrown,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Opsi 2: Berlangganan Pro",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ScoutBrown
                                        )
                                    }
                                    SneatBadge(
                                        text = "Rp 20.000 / 1 SEMESTER",
                                        backgroundColor = ScoutBrown,
                                        textColor = Color.White
                                    )
                                }

                                Text(
                                    text = "Aktifkan langganan Pro penuh selama 1 semester tanpa menonton iklan hanya dengan biaya Rp 20.000.",
                                    fontSize = 12.sp,
                                    color = SneatTextPrimary,
                                    lineHeight = 17.sp
                                )

                                Button(
                                    onClick = { showPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoutBrown),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_unlock_paid_pro")
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Berlangganan Pro Rp 20.000", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else if (!adminConfig.isInitialized) {
                // Step 2A: First-time Admin Onboarding Form
                item {
                    FirstTimeAdminSetupCard(
                        defaultRank = if (profile.rank in ScoutRank.adminEligibleRanks) profile.rank else ScoutRank.PEMBINA,
                        defaultName = profile.name,
                        defaultGudep = profile.gudep,
                        defaultInstitution = profile.school,
                        defaultPhotoPath = profile.customPhotoPath,
                        isPaidPro = adminConfig.isProSemesterPaid,
                        onSubmit = onSubmitFirstSetup
                    )
                }
            } else {
                // Step 2B: Keyword + PIN Verification Form
                item {
                    KeywordVerificationCard(
                        savedAdmin = adminConfig,
                        lockError = lockError,
                        onClearError = onClearError,
                        onVerify = onVerifyKeywordAccess
                    )
                }
            }

            // Summary Rules Table
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tabel Ketentuan Akses Dashboard Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SneatTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SneatTableContainer(modifier = Modifier.testTag("admin_policy_table")) {
                        SneatTableHeaderRow {
                            SneatTableHeaderCell("Ketentuan", weight = 0.34f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Rincian Aturan", weight = 0.66f)
                        }
                        AdminRuleRow(0, "Hak Peran", "Hanya Pembina & Bina Damping melalui Fitur Akun")
                        AdminRuleRow(1, "Opsi Pro", "1) Iklan AdMob Rewarded  •  2) Langganan Rp 20.000 / 1 Semester")
                        AdminRuleRow(2, "Kata Kunci", "Wajib cocok: Nama, Tingkatan Pramuka & PIN Akses")
                    }
                }
            }

            // Back Button
            item {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_back_from_lock"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SneatBorder)
                ) {
                    Text(
                        text = "Kembali ke Fitur Akun",
                        color = SneatTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }


    // Dialog: Subscription Payment Rp 20.000 / 1 Semester
    if (showPaymentDialog) {
        var selectedMethod by remember { mutableStateOf("QRIS Instan") }
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            containerColor = SneatSurface,
            title = {
                Text(
                    "Konfirmasi Langganan Pro",
                    fontWeight = FontWeight.Bold,
                    color = ScoutBrown
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SneatTableContainer {
                        SneatTableHeaderRow(backgroundColor = ScoutBrown) {
                            SneatTableHeaderCell("Detail Paket", weight = 0.42f)
                            SneatTableColumnDivider(Color.White.copy(alpha = 0.25f))
                            SneatTableHeaderCell("Keterangan", weight = 0.58f)
                        }
                        AdminRuleRow(0, "Paket", "User Dashboard Pro Admin")
                        AdminRuleRow(1, "Masa Aktif", "1 Semester Penuh")
                        AdminRuleRow(2, "Total Biaya", "Rp 20.000")
                    }

                    Text(
                        text = "Pilih Metode Pembayaran:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SneatTextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("QRIS Instan", "E-Wallet", "Transfer Bank").forEach { method ->
                            val active = selectedMethod == method
                            OutlinedButton(
                                onClick = { selectedMethod = method },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (active) ScoutGreenContainer else SneatSurface,
                                    contentColor = if (active) ScoutGreen else SneatTextSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            ) {
                                Text(method, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentDialog = false
                        onUnlockWithPayment()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                    modifier = Modifier.testTag("btn_confirm_paid_pro")
                ) {
                    Text("Bayar Rp 20.000 & Aktifkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun FirstTimeAdminSetupCard(
    defaultRank: ScoutRank,
    defaultName: String,
    defaultGudep: String,
    defaultInstitution: String,
    defaultPhotoPath: String,
    isPaidPro: Boolean,
    onSubmit: (
        fullName: String,
        rank: ScoutRank,
        gudep: String,
        institutionName: String,
        photoPath: String,
        pin: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(defaultName) }
    var selectedRank by remember { mutableStateOf(defaultRank) }
    var rankExpanded by remember { mutableStateOf(false) }
    var gudep by remember { mutableStateOf(defaultGudep) }
    var institutionName by remember { mutableStateOf(defaultInstitution) }
    var photoPath by remember { mutableStateOf(defaultPhotoPath) }
    var avatarId by remember { mutableIntStateOf(1) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var setupError by remember { mutableStateOf<String?>(null) }

    val rankInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(rankInteraction) {
        rankInteraction.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                rankExpanded = !rankExpanded
            }
        }
    }

    SneatCard(
        backgroundColor = SneatSurface,
        borderColor = ScoutGreen,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_first_setup_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Isi Data Diri Admin (Pertama Kali)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ScoutGreen,
                    modifier = Modifier.weight(1f)
                )
                SneatBadge(
                    text = if (isPaidPro) "PRO 1 SEMESTER" else "PRO IKLAN",
                    backgroundColor = ScoutGreenContainer,
                    textColor = ScoutGreen
                )
            }

            Text(
                text = "Lengkapi identitas pembina/lembaga, upload foto profil (Choose File), dan buat PIN akses Dashboard Admin.",
                fontSize = 11.sp,
                color = SneatTextSecondary
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; setupError = null },
                label = { Text("Nama Lengkap *") },
                singleLine = true,
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_setup_name")
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedRank.title,
                    onValueChange = {},
                    readOnly = true,
                    interactionSource = rankInteraction,
                    label = { Text("Tingkatan Pramuka (Peran Admin) *") },
                    trailingIcon = {
                        IconButton(onClick = { rankExpanded = !rankExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Tingkatan Admin")
                        }
                    },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dropdown_admin_setup_rank")
                )
                DropdownMenu(
                    expanded = rankExpanded,
                    onDismissRequest = { rankExpanded = false },
                    modifier = Modifier.background(SneatSurface)
                ) {
                    ScoutRank.adminEligibleRanks.forEach { rank ->
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
                value = gudep,
                onValueChange = { gudep = it; setupError = null },
                label = { Text("Nomor Gudep *") },
                placeholder = { Text("Contoh: 01.001 - 01.002") },
                singleLine = true,
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_setup_gudep")
            )

            OutlinedTextField(
                value = institutionName,
                onValueChange = { institutionName = it; setupError = null },
                label = { Text("Nama Lembaga / Pangkalan *") },
                placeholder = { Text("Contoh: SMP Negeri 1 Garuda") },
                singleLine = true,
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_setup_institution")
            )

            ProfilePhotoSelectorSection(
                selectedAvatarId = avatarId,
                customPhotoPath = photoPath,
                onSelectPresetAvatar = { avatarId = it },
                onCustomPhotoSelected = { path -> photoPath = path },
                onClearCustomPhoto = { photoPath = "" },
                sectionTitle = "Upload Foto Profil Admin *",
                manualBoxTitle = "Upload Foto Profil (Choose File / Galeri)",
                manualBoxDescription = "Pilih berkas foto profil Pembina/Bina Damping dari perangkat internal.",
                showPresetAvatars = true,
                filePrefix = "admin_photo_"
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it; setupError = null },
                label = { Text("Buat PIN Akses Dashboard Admin *") },
                placeholder = { Text("Minimal 4 angka/karakter") },
                singleLine = true,
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                trailingIcon = {
                    IconButton(onClick = { showPin = !showPin }) {
                        Icon(
                            imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Tampilkan/Sembunyikan PIN"
                        )
                    }
                },
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_setup_pin")
            )

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { confirmPin = it; setupError = null },
                label = { Text("Konfirmasi PIN Akses *") },
                singleLine = true,
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_setup_pin_confirm")
            )

            if (setupError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SneatDanger, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = setupError ?: "", fontSize = 11.sp, color = SneatDanger, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = {
                    when {
                        fullName.isBlank() -> setupError = "Nama lengkap wajib diisi."
                        gudep.isBlank() -> setupError = "Nomor Gudep wajib diisi."
                        institutionName.isBlank() -> setupError = "Nama lembaga wajib diisi."
                        pin.trim().length < 4 -> setupError = "PIN akses minimal 4 karakter/angka."
                        pin.trim() != confirmPin.trim() -> setupError = "Konfirmasi PIN tidak sama."
                        else -> {
                            onSubmit(
                                fullName.trim(),
                                selectedRank,
                                gudep.trim(),
                                institutionName.trim(),
                                photoPath,
                                pin.trim()
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_submit_admin_setup")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Data & Masuk Dashboard Admin", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KeywordVerificationCard(
    savedAdmin: AdminConfigProfile,
    lockError: String?,
    onClearError: () -> Unit,
    onVerify: (nameInput: String, rankInput: ScoutRank, pinInput: String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var rankInput by remember { mutableStateOf(ScoutRank.PEMBINA) }
    var rankExpanded by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }

    val rankInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(rankInteraction) {
        rankInteraction.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                rankExpanded = !rankExpanded
            }
        }
    }

    SneatCard(
        backgroundColor = SneatSurface,
        borderColor = ScoutGreen,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_keyword_gate_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = ScoutGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kata Kunci Akses Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScoutGreen
                    )
                }
                SneatBadge(
                    text = if (savedAdmin.isProSemesterPaid) "PRO 1 SEMESTER" else "PRO IKLAN",
                    backgroundColor = ScoutGreenContainer,
                    textColor = ScoutGreen
                )
            }

            Text(
                text = "Lembaga terdaftar: ${savedAdmin.institutionName} (Gudep ${savedAdmin.gudep}). Masukkan 3 kata kunci di bawah untuk membuka Dashboard Admin.",
                fontSize = 11.sp,
                color = SneatTextSecondary
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it; onClearError() },
                label = { Text("Kata Kunci 1: Nama Lengkap Admin *") },
                singleLine = true,
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_key_name")
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rankInput.title,
                    onValueChange = {},
                    readOnly = true,
                    interactionSource = rankInteraction,
                    label = { Text("Kata Kunci 2: Tingkatan Pramuka *") },
                    trailingIcon = {
                        IconButton(onClick = { rankExpanded = !rankExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Tingkatan")
                        }
                    },
                    textStyle = scoutInputTextStyle,
                    colors = scoutTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dropdown_admin_key_rank")
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
                                rankInput = rank
                                rankExpanded = false
                                onClearError()
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = pinInput,
                onValueChange = { pinInput = it; onClearError() },
                label = { Text("Kata Kunci 3: PIN Akses Dashboard *") },
                singleLine = true,
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                trailingIcon = {
                    IconButton(onClick = { showPin = !showPin }) {
                        Icon(
                            imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Tampilkan/Sembunyikan PIN"
                        )
                    }
                },
                textStyle = scoutInputTextStyle,
                colors = scoutTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_key_pin")
            )

            if (lockError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(10.dp)
                        .testTag("admin_lock_error_box"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SneatDanger, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = lockError, fontSize = 11.sp, color = SneatDanger, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = { onVerify(nameInput, rankInput, pinInput) },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_verify_admin_access")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifikasi & Buka Dashboard Admin", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdminRuleRow(index: Int, label: String, value: String) {
    SneatTableDataRow(index = index) {
        SneatTableCell(weight = 0.36f) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ScoutBrown
            )
        }
        SneatTableColumnDivider()
        SneatTableCell(weight = 0.64f) {
            Text(
                text = value,
                fontSize = 11.sp,
                color = SneatTextPrimary
            )
        }
    }
}
