package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutGreenContainer
import com.example.ui.theme.SneatBg
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatDanger
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextPrimary
import com.example.ui.theme.SneatTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

val SCOUT_PRESET_AVATARS = listOf("🦅", "⚜️", "🏕️", "🧭")

/**
 * Copies & downscales an image picked from internal storage / Photo Picker into app-private filesDir
 * without requiring broad storage permissions.
 */
suspend fun savePickedProfileImage(
    context: Context,
    uri: Uri,
    keepExistingPath: String = "",
    filePrefix: String = "scout_avatar_"
): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
        } ?: error("Tidak dapat membuka berkas gambar dari perangkat.")

        if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) {
            error("Berkas yang dipilih bukan gambar yang valid.")
        }

        val maxDim = 1024
        var sampleSize = 1
        while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodedBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: error("Format gambar tidak dapat dibaca.")

        val orientation = resolver.openInputStream(uri)?.use { stream ->
            runCatching {
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    postRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    postRotate(270f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            }
        }

        val bitmap = try {
            if (!matrix.isIdentity) {
                Bitmap.createBitmap(
                    decodedBitmap,
                    0,
                    0,
                    decodedBitmap.width,
                    decodedBitmap.height,
                    matrix,
                    true
                ).also {
                    if (it != decodedBitmap) decodedBitmap.recycle()
                }
            } else {
                decodedBitmap
            }
        } catch (t: Throwable) {
            decodedBitmap.recycle()
            throw t
        }

        val usePng = bitmap.hasAlpha()
        val ext = if (usePng) "png" else "jpg"
        val targetFile = File(context.filesDir, "${filePrefix}${System.currentTimeMillis()}.$ext")
        try {
            FileOutputStream(targetFile).use { out ->
                val format = if (usePng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                val quality = if (usePng) 95 else 88
                val ok = bitmap.compress(format, quality, out)
                if (!ok) error("Gagal menyimpan gambar ke penyimpanan aplikasi.")
            }
        } catch (t: Throwable) {
            runCatching { targetFile.delete() }
            throw t
        } finally {
            bitmap.recycle()
        }

        // Clean up unused temporary files for the same prefix while preserving keepExistingPath & newly saved file
        context.filesDir.listFiles()?.forEach { file ->
            if (file.name.startsWith(filePrefix) &&
                file.absolutePath != targetFile.absolutePath &&
                file.absolutePath != keepExistingPath
            ) {
                runCatching { file.delete() }
            }
        }

        targetFile.absolutePath
    }
}

@Composable
fun InstitutionLogoImage(
    logoPath: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Logo Lembaga"
) {
    val hasLogo = remember(logoPath) {
        logoPath.isNotBlank() && File(logoPath).exists()
    }
    if (hasLogo) {
        AsyncImage(
            model = File(logoPath),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.scout_garuda_icon),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun ScoutAvatarImage(
    avatarId: Int,
    customPhotoPath: String,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 24.sp
) {
    val hasCustomPhoto = remember(customPhotoPath) {
        customPhotoPath.isNotBlank() && File(customPhotoPath).exists()
    }
    if (hasCustomPhoto) {
        AsyncImage(
            model = File(customPhotoPath),
            contentDescription = "Foto / Logo Profil",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val userEmoji = SCOUT_PRESET_AVATARS.getOrElse(avatarId - 1) { "⚜️" }
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(text = userEmoji, fontSize = emojiSize)
        }
    }
}

@Composable
fun ProfilePhotoSelectorSection(
    selectedAvatarId: Int,
    customPhotoPath: String,
    onSelectPresetAvatar: (Int) -> Unit,
    onCustomPhotoSelected: (String) -> Unit,
    onClearCustomPhoto: () -> Unit,
    modifier: Modifier = Modifier,
    persistedPhotoPath: String = "",
    sectionTitle: String = "Foto / Lambang Profil (Opsional):",
    manualBoxTitle: String = "Tambah Foto / Logo Manual dari Perangkat",
    manualBoxDescription: String = "Pilih berkas foto atau logo dari penyimpanan internal HP (PNG, JPG, WebP).",
    showPresetAvatars: Boolean = true,
    filePrefix: String = "scout_avatar_"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pickError by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val handlePickedUri: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            isImporting = true
            pickError = null
            scope.launch {
                val result = savePickedProfileImage(
                    context = context,
                    uri = uri,
                    keepExistingPath = persistedPhotoPath,
                    filePrefix = filePrefix
                )
                isImporting = false
                result
                    .onSuccess { savedPath -> onCustomPhotoSelected(savedPath) }
                    .onFailure { err ->
                        pickError = err.localizedMessage ?: "Gagal memuat gambar dari perangkat."
                    }
            }
        }
    }

    // Option 1: Choose File from internal device storage (SAF - zero permissions)
    val chooseFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = handlePickedUri
    )

    // Option 2: Android Photo Picker (Gallery - zero permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = handlePickedUri
    )

    val hasCustomPhoto = customPhotoPath.isNotBlank() && File(customPhotoPath).exists()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = sectionTitle,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = SneatTextPrimary
        )

        // Preset Scout Emblems + Quick Custom Photo Thumbnail
        if (showPresetAvatars || hasCustomPhoto) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showPresetAvatars) {
                    SCOUT_PRESET_AVATARS.forEachIndexed { index, emoji ->
                        val avatarId = index + 1
                        val isSelected = !hasCustomPhoto && selectedAvatarId == avatarId
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ScoutGreenContainer else SneatBg)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ScoutGreen else SneatBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    pickError = null
                                    onClearCustomPhoto()
                                    onSelectPresetAvatar(avatarId)
                                }
                                .testTag("avatar_option_$avatarId"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(ScoutGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Terpilih",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (hasCustomPhoto) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ScoutGreenContainer)
                            .border(2.dp, ScoutGreen, CircleShape)
                            .testTag("avatar_custom_preview"),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(customPhotoPath),
                            contentDescription = "Foto / Logo Kustom",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(ScoutGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Foto kustom aktif",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Manual Choose File & Gallery Picker Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SneatBg)
                .border(1.dp, SneatBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = manualBoxTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ScoutBrown
            )
            Text(
                text = manualBoxDescription,
                fontSize = 11.sp,
                color = SneatTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { chooseFileLauncher.launch(arrayOf("image/*")) },
                    enabled = !isImporting,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SneatSurface,
                        contentColor = ScoutGreen
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_choose_file_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Choose File dari perangkat",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isImporting) "Memuat..." else "Choose File",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isImporting,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SneatSurface,
                        contentColor = ScoutBrown
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_pick_gallery_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Pilih dari Galeri",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Galeri Foto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (hasCustomPhoto) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ScoutGreenContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Foto / logo manual aktif",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScoutGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onClearCustomPhoto,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_clear_custom_photo")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Hapus foto kustom",
                            tint = SneatDanger,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hapus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SneatDanger
                        )
                    }
                }
            }

            if (pickError != null) {
                Text(
                    text = pickError ?: "",
                    fontSize = 11.sp,
                    color = SneatDanger,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
