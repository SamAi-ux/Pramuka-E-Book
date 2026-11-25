package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ScoutBrown
import com.example.ui.theme.ScoutGreen
import com.example.ui.theme.ScoutYellow
import com.example.ui.theme.SneatBorder
import com.example.ui.theme.SneatSurface
import com.example.ui.theme.SneatTextSecondary
import com.example.viewmodel.AppScreen

@Composable
fun ScoutBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show on main navigable screens
    val mainScreens = listOf(
        AppScreen.HOME,
        AppScreen.TINGKATAN,
        AppScreen.KEGIATAN,
        AppScreen.MATERI,
        AppScreen.PUSAT_BANTUAN,
        AppScreen.PROFIL
    )

    if (currentScreen !in mainScreens) return

    NavigationBar(
        modifier = modifier
            .border(
                width = 1.dp,
                color = SneatBorder,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .testTag("scout_bottom_nav"),
        containerColor = SneatSurface,
        windowInsets = NavigationBarDefaults.windowInsets,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Beranda"
                )
            },
            label = {
                Text(
                    text = "Beranda",
                    fontSize = 10.sp,
                    fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScoutGreen,
                selectedTextColor = ScoutGreen,
                indicatorColor = ScoutYellow.copy(alpha = 0.35f),
                unselectedIconColor = SneatTextSecondary,
                unselectedTextColor = SneatTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.TINGKATAN,
            onClick = { onNavigate(AppScreen.TINGKATAN) },
            icon = {
                Icon(
                    Icons.Default.MilitaryTech,
                    contentDescription = "Tingkatan"
                )
            },
            label = {
                Text(
                    text = "Tingkatan",
                    fontSize = 10.sp,
                    fontWeight = if (currentScreen == AppScreen.TINGKATAN) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScoutGreen,
                selectedTextColor = ScoutGreen,
                indicatorColor = ScoutYellow.copy(alpha = 0.35f),
                unselectedIconColor = SneatTextSecondary,
                unselectedTextColor = SneatTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_tingkatan")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.KEGIATAN,
            onClick = { onNavigate(AppScreen.KEGIATAN) },
            icon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Kegiatan"
                )
            },
            label = {
                Text(
                    text = "Kegiatan",
                    fontSize = 10.sp,
                    fontWeight = if (currentScreen == AppScreen.KEGIATAN) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScoutGreen,
                selectedTextColor = ScoutGreen,
                indicatorColor = ScoutYellow.copy(alpha = 0.35f),
                unselectedIconColor = SneatTextSecondary,
                unselectedTextColor = SneatTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_kegiatan")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.MATERI,
            onClick = { onNavigate(AppScreen.MATERI) },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Materi"
                )
            },
            label = {
                Text(
                    text = "Materi",
                    fontSize = 10.sp,
                    fontWeight = if (currentScreen == AppScreen.MATERI) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScoutGreen,
                selectedTextColor = ScoutGreen,
                indicatorColor = ScoutYellow.copy(alpha = 0.35f),
                unselectedIconColor = SneatTextSecondary,
                unselectedTextColor = SneatTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_materi")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PUSAT_BANTUAN,
            onClick = { onNavigate(AppScreen.PUSAT_BANTUAN) },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Bantuan"
                )
            },
            label = {
                Text(
                    text = "Bantuan",
                    fontSize = 10.sp,
                    fontWeight = if (currentScreen == AppScreen.PUSAT_BANTUAN) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScoutGreen,
                selectedTextColor = ScoutGreen,
                indicatorColor = ScoutYellow.copy(alpha = 0.35f),
                unselectedIconColor = SneatTextSecondary,
                unselectedTextColor = SneatTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_bantuan")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFIL,
            onClick = { onNavigate(AppScreen.PROFIL) },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profil"
                )
            },
            label = {
                Text(
                    text = "Profil",
                    fontSize = 10.sp,
                    fontWeight = if (currentScreen == AppScreen.PROFIL) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScoutGreen,
                selectedTextColor = ScoutGreen,
                indicatorColor = ScoutYellow.copy(alpha = 0.35f),
                unselectedIconColor = SneatTextSecondary,
                unselectedTextColor = SneatTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_profil")
        )
    }
}
