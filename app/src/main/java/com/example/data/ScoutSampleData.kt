package com.example.data

import com.example.model.HelpFaq
import com.example.model.LeaderContact
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus

object ScoutSampleData {

    val materials = listOf(
        ScoutMaterial(
            id = "mat_1",
            title = "Sandi & Kode Morse Internasional",
            category = "Sandi & Morse",
            summary = "Panduan lengkap kode morse menggunakan titik (.) dan strip (-) serta ragam sandi pramuka.",
            content = "Kode Morse diciptakan oleh Samuel F.B. Morse pada tahun 1835. Dalam Pramuka, Morse disampaikan melalui peluit, senter, bendera, atau tulisan tangan.\n\nSandi Kotak 1 menggunakan petak silang dua garis vertikal dan horizontal dengan pasangan dua huruf per kotak (huruf kedua menggunakan titik).\nSandi Kotak 2 berisi tiga huruf per kotak.\nSandi Rumput mengganti titik dengan rumput pendek dan strip dengan rumput tinggi.",
            details = listOf(
                "A: • —", "B: — • • •", "C: — • — •", "D: — • •", "E: •",
                "F: • • — •", "G: — — •", "H: • • • •", "I: • •", "J: • — — —",
                "K: — • —", "L: • — • •", "M: — —", "N: — •", "O: — — —",
                "P: • — — •", "Q: — — • —", "R: • — •", "S: • • •", "T: —",
                "U: • • —", "V: • • • —", "W: • — —", "X: — • • —", "Y: — • — —", "Z: — — • •"
            ),
            iconKey = "radio"
        ),
        ScoutMaterial(
            id = "mat_2",
            title = "Panduan Semaphore Lengkap A - Z",
            category = "Semaphore",
            summary = "Kirim pesan jarak jauh dengan sepasang bendera semaphore (45x45 cm) warna merah-kuning.",
            content = "Semaphore adalah cara mengirim berita dengan menggerakkan kedua tangan yang memegang bendera semaphore. Setiap posisi sudut (0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°) melambangkan huruf alfabet.\n\nAturan mengirim semaphore:\n1. Sikap sempurna menghadap penerima pesan.\n2. Bendera disilangkan di depan paha pada posisi istirahat.\n3. Ayunan lengan lurus, tegas, tanpa ditekuk pada siku.\n4. Beri jeda 1 detik untuk setiap pergantian huruf.",
            details = listOf(
                "Lingkaran 1: Huruf A sampai G (Tangan kanan bergerak bertahap, tangan kiri diam di bawah)",
                "Lingkaran 2: Huruf H sampai N (Kecuali J)",
                "Lingkaran 3: Huruf O sampai S",
                "Lingkaran 4: Huruf T, U, Y dan Angka/Batal",
                "Lingkaran 5: Huruf J dan V",
                "Lingkaran 6: Huruf W dan X",
                "Lingkaran 7: Huruf Z"
            ),
            iconKey = "flag"
        ),
        ScoutMaterial(
            id = "mat_3",
            title = "Tali Temali & Ikatan Pionering",
            category = "Tali Temali",
            summary = "Kuasai simpul dasar (simpul hidup, mati, pangkal, jangkar) dan ikatan palang untuk merakit menara.",
            content = "Tali temali merupakan keterampilan utama pramuka untuk mendirikan tenda, menara pantau, dan jembatan darurat (pionering).\n\nPerbedaan Simpul dan Ikatan:\n- Simpul: Hubungan antara tali dengan tali.\n- Ikatan: Hubungan antara tali dengan benda lain (tongkat atau kayu).",
            details = listOf(
                "Simpul Hidup: Mengikat tiang namun mudah dilepas kembali.",
                "Simpul Mati: Menyambung dua utas tali sama besar dalam keadaan kering.",
                "Simpul Pangkal: Mengawali dan mengakhiri ikatan pada tongkat.",
                "Simpul Jangkar: Membuat tandu darurat atau menalikan jangkar perahu.",
                "Simpul Nelayan (Kembar): Menyambung dua tali licin atau basah.",
                "Ikatan Palang: Mengikat dua tongkat yang bersilangan tegak lurus (90°).",
                "Ikatan Silang: Mengikat dua tongkat yang bersilangan membentuk sudut lancip/tumpul."
            ),
            iconKey = "link"
        ),
        ScoutMaterial(
            id = "mat_4",
            title = "P3K & Pertolongan Pertama di Lapangan",
            category = "P3K & Survival",
            summary = "Tata laksana darurat kecelakaan lapangan: balut bidai, gigitan ular, luka bakar, dan penanganan syok.",
            content = "Pertolongan Pertama Pada Kecelakaan (P3K) adalah tindakan cepat untuk menolong korban sebelum mendapatkan penanganan medis profesional.\n\nPrinsip Utama: P-A-T-U-T (Penolong Amankan Diri, Amankan Korban, Tandai Lokasi, Usahakan Bantuan Medis Segera, Tenangkan Korban).",
            details = listOf(
                "Patah Tulang: Lakukan fiksasi menggunakan bidai kayu minimal melewati dua persendian.",
                "Luka Bakar Ringan: Guyur dengan air mengalir selama 10-15 menit, jangan oleskan odol atau mentega.",
                "Pingsan / Heatstroke: Baringkan korban di tempat teduh, posisikan kaki lebih tinggi dari kepala, longgarkan pakaian kerah.",
                "Tandu Darurat: Rangkai 2 tongkat pramuka dan 2 utas tali atau mitela untuk evakuasi korban."
            ),
            iconKey = "medical"
        ),
        ScoutMaterial(
            id = "mat_5",
            title = "Sejarah Kepanduan & Sri Sultan HB IX",
            category = "Sejarah",
            summary = "Kisah Lord Robert Baden Powell dan kelahiran Gerakan Pramuka Indonesia lewat Keppres No. 238 Tahun 1961.",
            content = "Robert Stephenson Smyth Baden-Powell mendirikan gerakan kepanduan dunia pada tahun 1907 melalui perkemahan di Pulau Brownsea, Inggris. Bukunya yang terkenal adalah 'Scouting for Boys'.\n\nDi Indonesia, berbagai organisasi pandu disatukan menjadi Gerakan Pramuka pada 14 Agustus 1961 diresmikan oleh Presiden Soekarno. Sri Sultan Hamengkubuwono IX dinobatkan sebagai Bapak Pramuka Indonesia.",
            details = listOf(
                "1907: Perkemahan pertama di Brownsea Island oleh Baden-Powell.",
                "1912: Gerakan Pandu masuk ke Hindia Belanda (NIPV).",
                "1961 (14 Agustus): Peringatan Hari Pramuka Nasional setiap tahun.",
                "Lambang Tunas Kelapa: Diciptakan oleh Soenardjo Atmodipoerwo, melambangkan serbaguna, tahan uji, dan berakar kokoh."
            ),
            iconKey = "history"
        ),
        ScoutMaterial(
            id = "mat_6",
            title = "Kumpulan Lagu Pramuka & Yel-yel Regu",
            category = "Lagu & Yel",
            summary = "Lirik resmi Hymne Satya Darma Pramuka, Bagimu Negeri, dan lagu penyemangat di api unggun.",
            content = "Lagu dan yel-yel membangun jiwa korsa, persaudaraan, dan semangat gotong royong antar anggota regu kepanduan.",
            details = listOf(
                "Hymne Pramuka:\n'Kami Pramuka Indonesia, Manusia Pancasila. Satyaku kudarmakan, darmaku kubaktikan, agar jaya Indonesia, Indonesia tanah airku, kami jadi pandumu.'",
                "Api Unggun Sudah Menyala:\n'Api kita sudah menyala, api kita sudah menyala, api, api, api kita sudah menyala...'",
                "Berkemah (Di Sini Senang Di Sana Senang)",
                "Yel-yel Regu Garuda: Kuat, Cepat, Pantang Menyerah!"
            ),
            iconKey = "music"
        )
    )

    val activities = listOf(
        ScoutActivity(
            id = "act_1",
            title = "Latihan Rutin & Gladi Tali Temali",
            category = "Latihan Rutin",
            date = "Sabtu, 15 Nov 2026",
            time = "14:30 - 17:00 WIB",
            location = "Lapangan Pangkalan Gugus Depan",
            leader = "Kak Bambang Prasetyo (Pembina)",
            quota = 60,
            registeredCount = 48,
            status = "Akan Datang",
            description = "Pendalaman materi tali temali, simpul tiang, ikatan palang, dan simulasi pendirian tiang bendera pionering darurat. Wajib mengenakan seragam Pramuka lengkap dan membawa tongkat serta tambang."
        ),
        ScoutActivity(
            id = "act_2",
            title = "Perkemahan Sabtu Minggu (PERSAMI) Garuda",
            category = "Perkemahan",
            date = "22 - 23 Nov 2026",
            time = "07:00 WIB (2 Hari)",
            location = "Bumi Perkemahan Wana Wisata Pinus",
            leader = "Kak Siti Rahmawati, S.Pd",
            quota = 100,
            registeredCount = 92,
            status = "Akan Datang",
            description = "Kegiatan wajib tahunan untuk pelantikan kenaikan tingkat SKU Ramu ke Rakit, api unggun persaudaraan, penjelajahan halang rintang (wide game), dan pentas seni kepramukaan."
        ),
        ScoutActivity(
            id = "act_3",
            title = "Lomba Tingkat (LT-II) Kwartir Ranting",
            category = "Lomba",
            date = "05 Des 2026",
            time = "08:00 - 16:30 WIB",
            location = "Kompleks Gelora Pramuka Kwartir",
            leader = "Kak Hendra Kurniawan",
            quota = 40,
            registeredCount = 40,
            status = "Akan Datang",
            description = "Kompetisi antar regu berprestasi meliputi Sandi Morse, Semaphore cepat, Pertolongan Pertama P3K, Pionering kreatif, dan Cepat Tepat Pramuka Garuda."
        ),
        ScoutActivity(
            id = "act_4",
            title = "Bakti Sosial Bersih Lingkungan & Kali Bersih",
            category = "Bakti Sosial",
            date = "Minggu, 08 Nov 2026",
            time = "06:30 - 11:00 WIB",
            location = "Bantaran Sungai Garuda Jaya",
            leader = "Kak Dian Sastrowardoyo",
            quota = 80,
            registeredCount = 80,
            status = "Selesai",
            description = "Aksi nyata pengamalan Dasa Darma kedua: Cinta alam dan kasih sayang sesama manusia melalui pembersihan sampah dan penanaman 200 bibit pohon mahoni."
        )
    )

    val announcements = listOf(
        ScoutAnnouncement(
            id = "ann_1",
            title = "Pendaftaran Seleksi Pramuka Garuda Angkatan 2026",
            content = "Dibuka pendaftaran seleksi Pramuka Garuda bagi penggalang terap dan penegak laksana yang telah menuntaskan SKU dan minimal 10 TKK wajib.",
            date = "Hari ini, 10:00 WIB",
            priority = "Mendesak",
            author = "Kwartir Cabang"
        ),
        ScoutAnnouncement(
            id = "ann_2",
            title = "Jadwal Ujian Syarat Kecakapan Umum (SKU) Mingguan",
            content = "Pengujian SKU dapat dilakukan setiap hari Jumat sore setelah kegiatan belajar mengajar dengan Kakak Pembina pendamping.",
            date = "Kemarin",
            priority = "Info",
            author = "Dewan Penggalang"
        )
    )

    val members = listOf(
        ScoutMember(
            id = "mem_1",
            name = "Arif Wirawan",
            school = "SMP Negeri 1 Garuda",
            gudep = "01.001",
            rank = ScoutRank.PENGGALANG,
            skuLevel = "Penggalang Terap",
            joinDate = "14 Ags 2024",
            phone = "0812-3456-7890",
            verified = true
        ),
        ScoutMember(
            id = "mem_2",
            name = "Bintang Nusantara",
            school = "SMA Negeri 2 Teladan",
            gudep = "01.003",
            rank = ScoutRank.PENEGAK,
            skuLevel = "Penegak Laksana",
            joinDate = "10 Jan 2025",
            phone = "0857-1122-3344",
            verified = true
        ),
        ScoutMember(
            id = "mem_3",
            name = "Citra Lestari",
            school = "SD Negeri Percobaan",
            gudep = "02.015",
            rank = ScoutRank.SIAGA,
            skuLevel = "Siaga Tata",
            joinDate = "01 Mei 2025",
            phone = "0813-9988-7766",
            verified = true
        ),
        ScoutMember(
            id = "mem_4",
            name = "Dimas Daniswara",
            school = "Universitas Gadjah Sakti",
            gudep = "05.101",
            rank = ScoutRank.PANDEGA,
            skuLevel = "Pandega Tingkat 2",
            joinDate = "14 Ags 2022",
            phone = "0821-4455-6677",
            verified = true
        ),
        ScoutMember(
            id = "mem_5",
            name = "Kak Bambang Prasetyo",
            school = "Pusdiklatcab",
            gudep = "01.001",
            rank = ScoutRank.PEMBINA,
            skuLevel = "Pelatih KML",
            joinDate = "14 Ags 2015",
            phone = "0811-2233-4455",
            verified = true
        )
    )

    val faqs = listOf(
        HelpFaq(
            id = "faq_1",
            question = "Apa itu Pramuka Garuda dan bagaimana cara mencapainya?",
            answer = "Pramuka Garuda adalah tingkatan tertinggi dalam setiap golongan Pramuka (Siaga, Penggalang, Penegak, Pandega). Syaratnya adalah telah menyelesaikan tingkatan SKU tertinggi di golongannya, memiliki minimal 10 TKK (termasuk 5 wajib), mampu berbahasa internasional/komputer, dan menjadi teladan bagi lingkungan.",
            category = "Pramuka Garuda"
        ),
        HelpFaq(
            id = "faq_2",
            question = "Bagaimana alur penyelesaian Syarat Kecakapan Umum (SKU)?",
            answer = "Anggota mempelajari setiap butir SKU, mempraktikkannya, lalu meminta pengujian kepada Pembina atau Pembina Pembantu. Setelah dinyatakan lulus seluruh butir, akan diadakan Upacara Pelantikan Kenaikan Tingkat.",
            category = "SKU & SKK"
        ),
        HelpFaq(
            id = "faq_3",
            question = "Apa beda Dwisatya / Dwidarma dengan Trisatya / Dasadarma?",
            answer = "Dwisatya dan Dwidarma adalah janji serta ketentuan moral khusus untuk Pramuka Siaga (usia 7-10 tahun). Sedangkan Trisatya dan Dasadarma diperuntukkan bagi Pramuka Penggalang, Penegak, Pandega, dan Anggota Dewasa.",
            category = "Kode Kehormatan"
        ),
        HelpFaq(
            id = "faq_4",
            question = "Bagaimana cara membuka Dashboard Admin?",
            answer = "Dashboard Admin tersedia di dalam menu Akun/Profil khusus bagi Pembina dan Bina Damping. Akses Pro dibuka dengan menonton iklan atau berlangganan Pro Rp 20.000/1 semester, lalu diverifikasi dengan kata kunci Nama, Tingkatan Pramuka, dan PIN Akses.",
            category = "Aplikasi & Akses"
        )
    )

    val leaderContacts = listOf(
        LeaderContact(
            id = "lead_1",
            name = "Kak Bambang Prasetyo, M.Pd",
            role = "Ketua Gugus Depan 01.001 - 01.002",
            gudep = "Pangkalan SMPN 1 Garuda",
            phone = "+62 812-3456-7890",
            email = "bambang.pramuka@kwarcab.org",
            availability = "Senin - Sabtu (13:00 - 18:00 WIB)"
        ),
        LeaderContact(
            id = "lead_2",
            name = "Kak Siti Rahmawati, S.Pd",
            role = "Pembina Penggalang Putri",
            gudep = "Pangkalan SMPN 1 Garuda",
            phone = "+62 813-8877-6655",
            email = "siti.rahma@kwarcab.org",
            availability = "Rabu - Minggu (09:00 - 16:00 WIB)"
        ),
        LeaderContact(
            id = "lead_3",
            name = "Kak Hendra Kurniawan",
            role = "Anandika Kwartir Ranting",
            gudep = "Kwartir Ranting Garuda",
            phone = "+62 857-9900-1122",
            email = "hendra.kwarran@pramuka.id",
            availability = "Setiap Hari Latihan (14:00 - 17:00 WIB)"
        )
    )

    val syllabi = listOf(
        SemesterSyllabus(
            id = "syl_1",
            semester = "Semester Ganjil 2026/2027",
            rank = ScoutRank.PENGGALANG,
            programTitle = "Pelantikan & Pendalaman Trisatya, Dasadarma, serta Sandi Morse",
            weekRange = "Minggu 1 - 4",
            competencyTarget = "SKU Penggalang Ramu Butir 1 - 8",
            method = "Praktik Lapangan & Permainan Regu",
            status = "Berjalan"
        ),
        SemesterSyllabus(
            id = "syl_2",
            semester = "Semester Ganjil 2026/2027",
            rank = ScoutRank.PENGGALANG,
            programTitle = "Pionering Menara Kaki Tiga & Pertolongan Pertama (P3K)",
            weekRange = "Minggu 5 - 8",
            competencyTarget = "SKU Rakit & TKK P3K Tingkat Purwa",
            method = "Demonstrasi & Simulasi Tandu Darurat",
            status = "Terjadwal"
        ),
        SemesterSyllabus(
            id = "syl_3",
            semester = "Semester Genap 2026/2027",
            rank = ScoutRank.PENEGAK,
            programTitle = "Penjelajahan Kompas, Peta Pita & Gladian Pemimpin Sangga",
            weekRange = "Minggu 1 - 6",
            competencyTarget = "SKU Penegak Bantara & TKK Navigasi",
            method = "Wide Game & Kemah Bakti",
            status = "Terjadwal"
        ),
        SemesterSyllabus(
            id = "syl_4",
            semester = "Semester Genap 2026/2027",
            rank = ScoutRank.SIAGA,
            programTitle = "Bina Karakter Dwisatya, Simpul Dasar & Lagu Kepanduan",
            weekRange = "Minggu 1 - 4",
            competencyTarget = "SKU Siaga Mula - Bantu",
            method = "Permainan Edukatif & Bernyanyi",
            status = "Terjadwal"
        )
    )
}
