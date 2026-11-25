package com.example.model

enum class ScoutRank(val title: String, val ageGroup: String, val colorHex: Long) {
    SIAGA("Siaga", "7 - 10 Tahun", 0xFF10B981),
    PENGGALANG("Penggalang", "11 - 15 Tahun", 0xFFEF4444),
    PENEGAK("Penegak", "16 - 20 Tahun", 0xFFF59E0B),
    PANDEGA("Pandega", "21 - 25 Tahun", 0xFF8B5CF6),
    PEMBINA("Pembina", "Dewasa / Pelatih", 0xFF0B6E4F),
    BINA_DAMPING("Bina Damping", "Pembina Pendamping", 0xFF604020);

    companion object {
        fun fromString(value: String): ScoutRank {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) }
                ?: PENGGALANG
        }

        val adminEligibleRanks: List<ScoutRank>
            get() = listOf(PEMBINA, BINA_DAMPING)
    }
}

data class ScoutProfile(
    val name: String = "Kakak Pramuka",
    val school: String = "Pangkalan Gugus Depan Garuda",
    val gudep: String = "01.001 - 01.002",
    val rank: ScoutRank = ScoutRank.PENGGALANG,
    val phone: String = "08123456789",
    val avatarId: Int = 1,
    val customPhotoPath: String = "",
    val npa: String = "PG-2026-0891",
    val skuProgress: Int = 75,
    val tkkCount: Int = 8,
    val isGaruda: Boolean = true
)

data class InstitutionConfig(
    val name: String = "Pangkalan SMP Negeri 1 Garuda",
    val gudep: String = "01.001 - 01.002",
    val logoPath: String = ""
)

data class AdminConfigProfile(
    val fullName: String = "",
    val rank: ScoutRank = ScoutRank.PEMBINA,
    val gudep: String = "",
    val institutionName: String = "",
    val photoPath: String = "",
    val pin: String = "",
    val isInitialized: Boolean = false,
    val isProSemesterPaid: Boolean = false,
    val proSemesterLabel: String = ""
)

data class SemesterSyllabus(
    val id: String,
    val semester: String, // e.g., "Semester Ganjil 2026/2027", "Semester Genap 2026/2027"
    val rank: ScoutRank,
    val programTitle: String,
    val weekRange: String,
    val competencyTarget: String,
    val method: String,
    val status: String = "Terjadwal"
)

data class ScoutMaterial(
    val id: String,
    val title: String,
    val category: String, // Sandi & Morse, Semaphore, Tali Temali, P3K, Sejarah, Lagu
    val summary: String,
    val content: String,
    val details: List<String> = emptyList(),
    val iconKey: String = "book"
)

data class ScoutActivity(
    val id: String,
    val title: String,
    val category: String, // Latihan Rutin, Perkemahan, Lomba, Bakti Sosial
    val date: String,
    val time: String,
    val location: String,
    val leader: String,
    val quota: Int,
    val registeredCount: Int,
    val status: String, // Akan Datang, Berlangsung, Selesai
    val description: String,
    val isUserRegistered: Boolean = false
)

data class ScoutAnnouncement(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val priority: String = "Penting", // Info, Penting, Mendesak
    val author: String = "Kwartir Ranting"
)

data class ScoutMember(
    val id: String,
    val name: String,
    val school: String,
    val gudep: String,
    val rank: ScoutRank,
    val skuLevel: String,
    val joinDate: String,
    val phone: String,
    val verified: Boolean = true
)

data class HelpFaq(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)

data class LeaderContact(
    val id: String,
    val name: String,
    val role: String,
    val gudep: String,
    val phone: String,
    val email: String,
    val availability: String
)
