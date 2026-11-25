package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.ProfilePhotoSelectorSection
import com.example.ui.components.scoutInputTextStyle
import com.example.ui.components.scoutTextFieldColors
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoutRank
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.ScoutYellow
import com.example.ui.theme.ScoutYellowContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary

@Composable
fun RegistrationScreen(
    onSubmit: (
        name: String,
        school: String,
        gudep: String,
        rank: ScoutRank,
        phone: String,
        avatarId: Int,
        customPhotoPath: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var gudep by remember { mutableStateOf("") }
    var selectedRank by remember { mutableStateOf(ScoutRank.PENGGALANG) }
    var phone by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableIntStateOf(1) } // 1, 2, 3, 4 preset avatar
    var customPhotoPath by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SneatBg)
            .imePadding()
            .testTag("registration_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Notice Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ScoutYellowContainer)
                    .border(1.dp, ScoutYellow, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚠️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pendaftaran Anggota (Hanya 1 Kali)",
                            fontWeight = FontWeight.Bold,
                            color = ScoutBrown,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Lengkapi biodata resmi kepanduan Anda. Data tersimpan permanen dan tidak akan hilang saat logout.",
                            color = ScoutBrown.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Form Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SneatSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SneatBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Formulir Pendaftaran Pramuka Garuda",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScoutGreen
                        )
                    )
                    Text(
                        text = "Isi formulir berikut dengan data yang valid",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SneatTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Avatar Picker (Preset Lambang + Choose File / Galeri Foto Manual)
                    ProfilePhotoSelectorSection(
                        selectedAvatarId = selectedAvatar,
                        customPhotoPath = customPhotoPath,
                        onSelectPresetAvatar = { selectedAvatar = it },
                        onCustomPhotoSelected = { path -> customPhotoPath = path },
                        onClearCustomPhoto = { customPhotoPath = "" }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. Nama Lengkap
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorText = null
                        },
                        label = { Text("Nama Lengkap *") },
                        placeholder = { Text("Contoh: Arif Wirawan") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ScoutGreen)
                        },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_full_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Asal Sekolah
                    OutlinedTextField(
                        value = school,
                        onValueChange = {
                            school = it
                            errorText = null
                        },
                        label = { Text("Asal Sekolah / Pangkalan *") },
                        placeholder = { Text("Contoh: SMP Negeri 1 Garuda") },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null, tint = ScoutGreen)
                        },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_school")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Gugus Depan
                    OutlinedTextField(
                        value = gudep,
                        onValueChange = {
                            gudep = it
                            errorText = null
                        },
                        label = { Text("Nomor Gugus Depan *") },
                        placeholder = { Text("Contoh: 01.001 - 01.002 Pangeran Diponegoro") },
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null, tint = ScoutGreen)
                        },
                        singleLine = true,
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_gudep")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Dropdown Tingkatan Pramuka
                    val dropdownInteraction = remember { MutableInteractionSource() }
                    LaunchedEffect(dropdownInteraction) {
                        dropdownInteraction.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                dropdownExpanded = !dropdownExpanded
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "${selectedRank.title} (${selectedRank.ageGroup})",
                            onValueChange = {},
                            readOnly = true,
                            interactionSource = dropdownInteraction,
                            label = { Text("Tingkatan Pramuka *") },
                            leadingIcon = {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ScoutYellow)
                            },
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Tingkatan")
                                }
                            },
                            textStyle = scoutInputTextStyle,
                            colors = scoutTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dropdown_tingkatan")
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(SneatSurface)
                        ) {
                            ScoutRank.entries.forEach { rank ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = rank.title,
                                                fontWeight = FontWeight.Bold,
                                                color = ScoutGreen
                                            )
                                            Text(
                                                text = "Rentang usia: ${rank.ageGroup}",
                                                fontSize = 11.sp,
                                                color = SneatTextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedRank = rank
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("rank_option_${rank.name}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Nomor HP (Opsional)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Nomor HP / WhatsApp (Opsional)") },
                        placeholder = { Text("Contoh: 08123456789") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = ScoutBrown)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = scoutInputTextStyle,
                        colors = scoutTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_phone")
                    )

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorText ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                errorText = "Nama lengkap wajib diisi."
                            } else if (school.isBlank()) {
                                errorText = "Asal sekolah / pangkalan wajib diisi."
                            } else if (gudep.isBlank()) {
                                errorText = "Nomor Gugus Depan wajib diisi."
                            } else {
                                onSubmit(
                                    fullName,
                                    school,
                                    gudep,
                                    selectedRank,
                                    phone,
                                    selectedAvatar,
                                    customPhotoPath
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("button_submit_registration"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScoutGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Simpan Data & Buka Beranda",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
