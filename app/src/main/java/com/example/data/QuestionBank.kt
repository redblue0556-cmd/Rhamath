package com.example.data

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

class Mulberry32(seed: Int) {
    private var state: Int = seed

    fun nextDouble(): Double {
        state += 0x6D2B79F5
        var t = (state xor (state ushr 15)) * (1 or state)
        t = (t + ((t xor (t ushr 7)) * (61 or t))) xor t
        val unsigned = (t xor (t ushr 14)).toLong() and 0xFFFFFFFFL
        return unsigned.toDouble() / 4294967296.0
    }
}

object QuestionBank {
    const val QUESTIONS_PER_CHAPTER = 100

    val chapters: List<ChapterInfo> = listOf(
        ChapterInfo(0, "Aljabar & Nilai Fungsi Dasar", "Linier, kuadrat, f(a), toko buku & ojek", "🛒"),
        ChapterInfo(1, "Operasi Aljabar Fungsi", "Jumlah, selisih, kali, bagi fungsi", "➕"),
        ChapterInfo(2, "Fungsi Komposisi", "Dua & tiga fungsi (f∘g)", "🔗"),
        ChapterInfo(3, "Fungsi Invers & Pecahan", "Invers linier & rasional", "🔄"),
        ChapterInfo(4, "Geometri Lingkaran & Busur", "Keliling, luas, panjang busur", "⭕")
    )

    private val concepts: List<List<String>> = listOf(
        listOf(
            "Fungsi adalah aturan yang memasangkan setiap masukan \$x\$ dengan tepat satu hasil \$f(x)\$. Pada soal cerita, kata \"per ...\" berarti DIKALIKAN, sedangkan \"awal / tetap\" berarti DITAMBAHKAN.",
            "Menghitung \$f(k)\$ artinya mengganti SEMUA huruf \$x\$ dengan angka \$k\$ (pakai kurung), lalu menghitung sesuai urutan operasi.",
            "Biaya = bagian tetap + bagian yang berubah mengikuti jarak. Ini fungsi linier \$y=mx+c\$: \$m\$ = tarif per km, \$c\$ = tarif awal.",
            "Fungsi kuadrat memuat \$x^2\$. Cara substitusinya sama, tetapi angka dipangkatkan dulu, baru dikali koefisien.",
            "Mencari \$a\$ dari \$f(a)\$ sama dengan menyelesaikan persamaan linier: samakan rumus dengan nilainya, pindahkan konstanta, lalu bagi."
        ),
        listOf(
            "Penjumlahan fungsi: \$(f+g)(x)=f(x)+g(x)\$. Hitung nilai masing-masing fungsi di titik yang sama, lalu jumlahkan.",
            "Pengurangan fungsi: \$(f-g)(x)=f(x)-g(x)\$. Urutan penting, \$f-g\$ berbeda dengan \$g-f\$.",
            "Perkalian fungsi: \$(f\\cdot g)(x)=f(x)\\cdot g(x)\$. Hitung nilai tiap fungsi dulu, baru dikalikan.",
            "Pembagian fungsi: \$\\left(\\frac fg\\right)(x)=\\frac{f(x)}{g(x)}\$ dengan syarat penyebut \$g(x)\\neq0\$.",
            "Keuntungan = pendapatan − biaya. Jumlah produk yang terjual dimasukkan ke \$x\$ pada fungsi keuntungan."
        ),
        listOf(
            "Komposisi \$(f\\circ g)(x)=f(g(x))\$: hasil fungsi DALAM (\$g\$) menjadi masukan fungsi LUAR (\$f\$).",
            "\$(g\\circ f)(x)=g(f(x))\$: sekarang \$f\$ yang berada di dalam, jadi kerjakan \$f\$ lebih dulu.",
            "Mesin bilangan adalah fungsi. Tahap-tahapnya dikerjakan berurutan, persis seperti komposisi fungsi.",
            "Untuk mencari rumus komposisi, substitusikan seluruh rumus fungsi dalam ke setiap \$x\$ pada fungsi luar, lalu sederhanakan (distribusi).",
            "Komposisi tiga fungsi dikerjakan dari yang PALING DALAM ke luar: \$h\$ dulu, lalu \$g\$, lalu \$f\$."
        ),
        listOf(
            "Invers membalik kerja fungsi: jika \$f(x)=y\$ maka \$f^{-1}(y)=x\$.",
            "Rumus invers: tulis \$y=f(x)\$, isolasi \$x\$ (lawan operasi satu per satu), lalu tukar nama peubah \$y\\to x\$.",
            "Invers pecahan linear: jika \$f(x)=\\frac{ax+b}{cx+d}\$ maka \$f^{-1}(x)=\\frac{-dx+b}{cx-a}\$.",
            "Nilai invers dapat dicari tanpa rumus: cari \$x\$ sehingga \$f(x)\$ sama dengan nilai yang diminta, lalu selesaikan persamaannya.",
            "Invers dipakai untuk pertanyaan terbalik, yaitu dari hasil (biaya) kembali ke masukan (jarak)."
        ),
        listOf(
            "Keliling lingkaran \$K=\\pi d=2\\pi r\$. Satuannya sama dengan satuan panjang (cm atau m).",
            "Luas lingkaran \$L=\\pi r^2\$. Satuannya kuadrat (cm² atau m²). Dengan \$\\pi=\\frac{22}{7}\$, pilih jari-jari kelipatan 7 agar mudah dicoret.",
            "Panjang busur adalah SEBAGIAN keliling: \$\\frac{\\theta}{360^\\circ}\\times2\\pi r\$.",
            "Satu lingkaran penuh = \$360^\\circ\$. Dibagi \$n\$ bagian sama besar, setiap sudut pusat = \$\\frac{360^\\circ}{n}\$.",
            "Pagar yang mengelilingi taman bundar sama dengan keliling lingkaran: \$K=\\pi d\$."
        )
    )

    private val idLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    private val bankCache = mutableMapOf<Int, List<Question>>()

    fun fmtNum(x: Double): String {
        val rounded = round(x * 100.0) / 100.0
        val asLong = rounded.toLong()
        return if (abs(rounded - asLong.toDouble()) < 1e-9) {
            asLong.toString()
        } else {
            String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
        }
    }

    fun fmtId(x: Double): String {
        val rounded = round(x * 100.0) / 100.0
        val nf = NumberFormat.getNumberInstance(idLocale).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
        return nf.format(rounded)
    }

    fun rp(x: Double): String = "Rp${fmtId(x)}"
    fun rp(x: Int): String = rp(x.toDouble())
    private fun M(s: String): String = "\$$s\$"
    private fun deg(x: Double): String = M("${fmtNum(x)}^\\circ")
    private fun mt(u: String): (Double) -> String = { x -> M("${fmtNum(x)}\\text{ $u}") }

    private fun lin(p: Int, q: Int): String {
        val xPart = when (p) {
            1 -> "x"
            -1 -> "-x"
            0 -> ""
            else -> "${p}x"
        }
        val qPart = when {
            q > 0 -> if (xPart.isEmpty()) "$q" else "+$q"
            q < 0 -> "$q"
            else -> if (xPart.isEmpty()) "0" else ""
        }
        return xPart + qPart
    }

    private class RngScope(private val rng: Mulberry32) {
        fun ri(a: Int, b: Int): Int = a + floor(rng.nextDouble() * (b - a + 1)).toInt()
        fun <T> pick(list: List<T>): T = list[ri(0, list.size - 1)]
        fun <T> sh(list: List<T>): List<T> {
            val copy = list.toMutableList()
            for (i in copy.size - 1 downTo 1) {
                val j = ri(0, i)
                val tmp = copy[i]
                copy[i] = copy[j]
                copy[j] = tmp
            }
            return copy
        }

        fun mkN(
            c: Double,
            ex: List<Double> = emptyList(),
            f: (Double) -> String = { M(fmtNum(it)) }
        ): Pair<List<String>, Int> {
            val s = mutableListOf(c)
            val fallback = sh(listOf(c + 1, c - 1, c + 2, c - 2, c + 3, c - 3, c + 10, c - 10))
            for (v in ex + fallback) {
                val alreadyExists = s.any { abs(it - v) < 1e-6 }
                if (s.size < 5 && !alreadyExists && (c < 0 || v >= 0)) {
                    s.add(v)
                }
            }
            s.sort()
            val idx = s.indexOfFirst { abs(it - c) < 1e-6 }.coerceAtLeast(0)
            return s.map(f) to idx
        }

        fun mkN(
            c: Int,
            ex: List<Int> = emptyList(),
            f: (Double) -> String = { M(fmtNum(it)) }
        ): Pair<List<String>, Int> = mkN(c.toDouble(), ex.map { it.toDouble() }, f)

        fun mkX(c: String, w: List<String>): Pair<List<String>, Int> {
            val s = mutableListOf(c)
            for (v in w) {
                if (!s.contains(v)) s.add(v)
            }
            var i = 1
            while (s.size < 5) {
                s.add("$c+$i")
                i++
            }
            val shuffled = sh(s)
            return shuffled.map { M(it) } to shuffled.indexOf(c)
        }
    }

    private const val PI_TEX = "\\pi=\\frac{22}{7}"

    private fun qOf(
        ch: Int,
        tIdx: Int,
        q: String,
        optsAndAns: Pair<List<String>, Int>,
        d: List<String>,
        f: String,
        s: List<String>,
        tip: String
    ): Question = Question(
        chapterIndex = ch,
        prompt = q,
        options = optsAndAns.first,
        correctIndex = optsAndAns.second,
        given = d,
        formula = f,
        steps = s,
        tip = tip,
        concept = concepts[ch][tIdx]
    )

    private fun buildTemplate(ch: Int, t: Int, v: Boolean, rng: RngScope): Question = with(rng) {
        when (ch) {
            0 -> when (t) {
                0 -> {
                    val p = if (v) pick(listOf(2000, 2500, 3000, 3500, 4000, 4500, 6000, 7500)) else 5000
                    val it = if (v) pick(listOf("pensil", "pulpen", "penghapus", "buku gambar", "penggaris", "spidol")) else "buku tulis"
                    qOf(
                        ch, t,
                        "Sebuah toko alat tulis menjual $it dengan harga ${rp(p)} per buah. Jika banyak $it yang dibeli dinyatakan dengan \$x\$ dan total harga dinyatakan dengan \$f(x)\$, fungsi yang tepat adalah ....",
                        mkX("f(x)=${p}x", listOf("f(x)=x+$p", "f(x)=$p-x", "f(x)=x-$p", "f(x)=$p+x^2")),
                        listOf("Harga satuan = ${rp(p)}", "Banyak barang = \$x\$"),
                        "\\text{total}=\\text{harga satuan}\\times\\text{banyak}",
                        listOf(
                            "\\text{total} = \\text{harga satuan}\\times\\text{banyak} ;; f(x) = $p\\times x || Setiap barang harganya ${rp(p)}, jadi untuk \$x\$ barang totalnya $p dikali \$x\$.",
                            "f(x) = ${p}x || Tulis dalam bentuk paling ringkas. Hubungannya perkalian, bukan penjumlahan.",
                            "f(2) = $p(2) ;; f(2) = ${2 * p} || Uji dengan \$x=2\$: hasilnya ${rp(2 * p)}, sama dengan 2 barang × ${rp(p)} ✓."
                        ),
                        "Kata \"per buah\" = KALI. Kata \"awal/tambahan\" = JUMLAH."
                    )
                }
                1 -> {
                    val a = if (v) ri(2, 9) else 3
                    val b = if (v) ri(2, 15) else 5
                    val k = if (v) ri(2, 12) else 4
                    val c = a * k + b
                    qOf(
                        ch, t,
                        "Diketahui fungsi \$f(x)=${lin(a, b)}\$. Nilai \$f($k)\$ adalah ....",
                        mkN(c, listOf(a * k - b, a + b + k, (a + b) * k)),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$x=$k\$"),
                        "f(x)=ax+b\\Rightarrow f(k)=ak+b",
                        listOf(
                            "f($k) = $a($k)+$b || Ganti setiap \$x\$ dengan $k, tulis dalam kurung.",
                            "f($k) = ${a * k}+$b || Kerjakan perkalian lebih dulu: $a × $k = ${a * k}.",
                            "f($k) = $c || Jumlahkan: ${a * k} + $b = $c. Jadi nilai \$f($k)\$ adalah $c."
                        ),
                        "Kali dulu sebelum tambah (urutan operasi)."
                    )
                }
                2 -> {
                    val A = if (v) pick(listOf(5000, 6000, 7500, 8000, 9000, 10000, 12000)) else 7000
                    val B = if (v) pick(listOf(2000, 2500, 3500, 4000, 4500, 5000)) else 3000
                    qOf(
                        ch, t,
                        "Sebuah ojek online menetapkan tarif awal ${rp(A)} dan tarif tambahan ${rp(B)} setiap kilometer. Jika jarak perjalanan adalah \$x\$ kilometer, fungsi biaya perjalanan adalah ....",
                        mkX("f(x)=${B}x+$A", listOf("f(x)=${A}x+$B", "f(x)=${A + B}x", "f(x)=$A-${B}x", "f(x)=$B-${A}x")),
                        listOf("Tarif awal = ${rp(A)} (tetap)", "Tarif per km = ${rp(B)}", "Jarak = \$x\$ km"),
                        "f(x)=\\text{tarif per km}\\cdot x+\\text{tarif awal}",
                        listOf(
                            "\\text{biaya} = \\text{tarif per km}\\times x+\\text{tarif awal} || Biaya terdiri dari bagian yang berubah mengikuti jarak dan bagian tetap.",
                            "\\text{bagian jarak} = ${B}x ;; \\text{bagian tetap} = $A || Tarif tambahan ${rp(B)} dikali jarak \$x\$ km. Tarif awal ${rp(A)} hanya dibayar satu kali.",
                            "f(x) = ${B}x+$A || Gabungkan kedua bagian.",
                            "f(1) = $B(1)+$A ;; f(1) = ${A + B} || Uji \$x=1\$ km: biaya ${rp(A + B)} = tarif awal + 1 km ✓."
                        ),
                        "Tarif awal = konstanta, tarif per km = koefisien \$x\$."
                    )
                }
                3 -> {
                    val a = if (v) ri(2, 5) else 2
                    val b = if (v) ri(2, 6) else 3
                    val c = if (v) ri(1, 7) else 1
                    val k = if (v) ri(2, 4) else 2
                    val r = a * k * k - b * k + c
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${a}x^2-${b}x+$c\$. Nilai \$f($k)\$ adalah ....",
                        mkN(r, listOf(a * k * k + b * k + c, a * k * k - b * k - c, a * k - b * k + c)),
                        listOf("\$f(x)=${a}x^2-${b}x+$c\$", "\$x=$k\$"),
                        "f(x)=ax^2+bx+c",
                        listOf(
                            "f($k) = $a($k)^2-$b($k)+$c || Ganti setiap \$x\$ dengan $k.",
                            "f($k) = $a(${k * k})-${b * k}+$c ;; f($k) = ${a * k * k}-${b * k}+$c || Pangkat lebih dulu: $k² = ${k * k}. Lalu kali koefisien: $a×${k * k}=${a * k * k} dan $b×$k=${b * k}.",
                            "f($k) = ${a * k * k - b * k}+$c ;; f($k) = $r || Kurangi dari kiri ke kanan: ${a * k * k}−${b * k}=${a * k * k - b * k}, lalu tambah $c. Hasilnya $r."
                        ),
                        "Hitung pangkat PERTAMA, lalu kali, lalu tambah/kurang."
                    )
                }
                else -> {
                    val a = if (v) ri(2, 6) else 3
                    val b = if (v) ri(2, 9) else 6
                    val x = if (v) ri(2, 9) else 6
                    val nVal = a * x - b
                    qOf(
                        ch, t,
                        "Diketahui fungsi \$f(x)=${a}x-$b\$. Jika \$f(a)=$nVal\$, nilai \$a\$ adalah ....",
                        mkN(x, listOf(x + 1, x - 1, nVal)),
                        listOf("\$f(x)=${a}x-$b\$", "\$f(a)=$nVal\$"),
                        "f(a)=${a}a-$b",
                        listOf(
                            "f(a) = $nVal ;; ${a}a-$b = $nVal || Samakan \$f(a)\$ dengan nilai yang diketahui pada soal.",
                            "${a}a = $nVal+$b ;; ${a}a = ${nVal + b} || Pindahkan −$b ke ruas kanan, tandanya berubah menjadi +$b.",
                            "a = \\frac{${nVal + b}}{$a} ;; a = $x || Bagi kedua ruas dengan $a.",
                            "f($x) = $a($x)-$b ;; f($x) = $nVal || Cek: hasilnya $nVal, sama dengan soal ✓."
                        ),
                        "Cek balik: \$f($x)=$nVal\$ ✓"
                    )
                }
            }

            1 -> when (t) {
                0 -> {
                    val a = ri(2, 6)
                    val b = ri(1, 9)
                    val c = ri(1, 5)
                    val d = ri(1, 9)
                    val k = ri(2, 6)
                    val r = (a + c) * k + b + d
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, d)}\$. Nilai \$(f+g)($k)\$ adalah ....",
                        mkN(r, listOf(a * k + b - (c * k + d), (a * k + b) * (c * k + d) % 97 + 1)),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, d)}\$"),
                        "(f+g)(x)=f(x)+g(x)",
                        listOf(
                            "f($k) = $a($k)+$b ;; f($k) = ${a * k}+$b ;; f($k) = ${a * k + b} || Hitung \$f($k)\$ lebih dulu: ganti \$x\$ dengan $k.",
                            "g($k) = $c($k)+$d ;; g($k) = ${c * k}+$d ;; g($k) = ${c * k + d} || Lakukan hal yang sama untuk \$g\$.",
                            "(f+g)($k) = f($k)+g($k) ;; (f+g)($k) = ${a * k + b}+${c * k + d} ;; (f+g)($k) = $r || Jumlahkan kedua hasil."
                        ),
                        "Boleh juga jumlahkan fungsinya dulu: \$(f+g)(x)=${lin(a + c, b + d)}\$."
                    )
                }
                1 -> {
                    val a = ri(3, 8)
                    val b = ri(1, 9)
                    val c = ri(1, 3)
                    val d = ri(1, 9)
                    val k = ri(2, 7)
                    val r = (a * k + b) - (c * k + d)
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, d)}\$. Nilai \$(f-g)($k)\$ adalah ....",
                        mkN(r, listOf((c * k + d) - (a * k + b), (a * k + b) + (c * k + d))),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, d)}\$"),
                        "(f-g)(x)=f(x)-g(x)",
                        listOf(
                            "f($k) = $a($k)+$b ;; f($k) = ${a * k}+$b ;; f($k) = ${a * k + b} || Hitung \$f($k)\$ lebih dulu: ganti \$x\$ dengan $k.",
                            "g($k) = $c($k)+$d ;; g($k) = ${c * k}+$d ;; g($k) = ${c * k + d} || Lakukan hal yang sama untuk \$g\$.",
                            "(f-g)($k) = f($k)-g($k) ;; (f-g)($k) = ${a * k + b}-${c * k + d} ;; (f-g)($k) = $r || Kurangkan: hasil \$f\$ dikurangi hasil \$g\$. Urutannya tidak boleh tertukar."
                        ),
                        "Urutan pengurangan penting: \$f-g\\neq g-f\$."
                    )
                }
                2 -> {
                    val a = ri(2, 5)
                    val b = ri(1, 6)
                    val c = ri(1, 4)
                    val d = ri(1, 6)
                    val k = ri(2, 5)
                    val r = (a * k + b) * (c * k + d)
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, d)}\$. Nilai \$(f\\times g)($k)\$ adalah ....",
                        mkN(r, listOf((a * k + b) + (c * k + d), a * c * k * k + b * d)),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, d)}\$"),
                        "(f\\cdot g)(x)=f(x)\\cdot g(x)",
                        listOf(
                            "f($k) = $a($k)+$b ;; f($k) = ${a * k}+$b ;; f($k) = ${a * k + b} || Hitung \$f($k)\$ lebih dulu: ganti \$x\$ dengan $k.",
                            "g($k) = $c($k)+$d ;; g($k) = ${c * k}+$d ;; g($k) = ${c * k + d} || Lakukan hal yang sama untuk \$g\$.",
                            "(f\\cdot g)($k) = f($k)\\times g($k) ;; (f\\cdot g)($k) = ${a * k + b}\\times${c * k + d} ;; (f\\cdot g)($k) = $r || Kalikan kedua hasil."
                        ),
                        "Hitung nilai tiap fungsi dulu, baru kalikan."
                    )
                }
                3 -> {
                    val a = ri(1, 4)
                    val c = ri(1, 3)
                    val k = ri(3, 8)
                    val mm = ri(2, 5)
                    val q = ri(2, 5)
                    val dd = mm - c * k
                    val b = q * mm - a * k
                    val bSigned = if (b < 0) "$b" else "+$b"
                    val ddSigned = if (dd < 0) "$dd" else "+$dd"
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, dd)}\$. Nilai \$\\left(\\frac{f}{g}\\right)($k)\$ adalah ....",
                        mkN(q, listOf(mm, q * mm, q + 1)),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, dd)}\$"),
                        "\\left(\\frac fg\\right)(x)=\\frac{f(x)}{g(x)},\\ g(x)\\neq0",
                        listOf(
                            "f($k) = $a($k)$bSigned ;; f($k) = ${a * k + b} || Hitung \$f($k)\$ lebih dulu.",
                            "g($k) = $c($k)$ddSigned ;; g($k) = $mm || Periksa penyebut: $mm ≠ 0, jadi pembagian boleh dilakukan.",
                            "\\left(\\frac fg\\right)($k) = \\frac{${a * k + b}}{$mm} ;; \\left(\\frac fg\\right)($k) = $q || Bagi hasil \$f\$ dengan hasil \$g\$."
                        ),
                        "Pastikan penyebut \$g(x)\\neq 0\$."
                    )
                }
                else -> {
                    val p = if (v) pick(listOf(20000, 25000, 40000, 50000)) else 50000
                    val q = if (v) pick(listOf(100000, 150000, 250000)) else 200000
                    val x = if (v) ri(6, 15) else 10
                    val r = p * x - q
                    qOf(
                        ch, t,
                        "Sebuah perusahaan mencatat keuntungan dengan fungsi \$f(x)=${p}x-$q\$, dengan \$x\$ menyatakan jumlah produk yang terjual. Jika perusahaan berhasil menjual $x produk, keuntungan yang diperoleh adalah ....",
                        mkN(r, listOf(p * x + q, p * x, r - p)) { rp(it) },
                        listOf("\$f(x)=${p}x-$q\$", "\$x=$x\$ produk"),
                        "\\text{untung}=\\text{pendapatan}-\\text{biaya}",
                        listOf(
                            "f(x) = ${p}x-$q ;; f($x) = $p($x)-$q || Ganti \$x\$ dengan jumlah produk yang terjual, yaitu $x.",
                            "f($x) = ${p * x}-$q || Kalikan dulu: $p × $x = ${p * x}.",
                            "f($x) = $r || Kurangi biaya $q. Keuntungan yang diperoleh ${rp(r)}."
                        ),
                        "Soal cerita: tulis fungsi dulu, baru substitusi."
                    )
                }
            }

            2 -> when (t) {
                0 -> {
                    val a = if (v) ri(2, 5) else 2
                    val b = if (v) ri(1, 6) else 3
                    val c = if (v) ri(1, 3) else 1
                    val dRaw = ri(-5, 5)
                    val d = if (v) (if (dRaw == 0) 2 else dRaw) else -1
                    val k = if (v) ri(2, 7) else 4
                    val g = c * k + d
                    val r = a * g + b
                    val dSigned = if (d < 0) "$d" else "+$d"
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, d)}\$. Nilai \$(f\\circ g)($k)\$ adalah ....",
                        mkN(r, listOf(a * k + b + c * k + d, c * (a * k + b) + d, g)),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, d)}\$", "\$x=$k\$"),
                        "(f\\circ g)(x)=f(g(x))",
                        listOf(
                            "g($k) = $c($k)$dSigned ;; g($k) = $g || Fungsi dalam (\$g\$) dikerjakan lebih dulu.",
                            "(f\\circ g)($k) = f(g($k)) ;; (f\\circ g)($k) = f($g) || Hasil \$g\$ yaitu $g menjadi masukan untuk \$f\$.",
                            "f($g) = $a($g)+$b ;; f($g) = ${a * g}+$b ;; f($g) = $r || Hitung \$f($g)\$."
                        ),
                        "\$f\\circ g\$: kerjakan \$g\$ DULU (dalam), lalu \$f\$."
                    )
                }
                1 -> {
                    val a = ri(2, 4)
                    val b = ri(1, 6)
                    val c = ri(1, 3)
                    val d = ri(1, 6)
                    val k = ri(2, 6)
                    val fk = a * k + b
                    val r = c * fk + d
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, d)}\$. Nilai \$(g\\circ f)($k)\$ adalah ....",
                        mkN(r, listOf(a * (c * k + d) + b, fk, c * k + d)),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, d)}\$", "\$x=$k\$"),
                        "(g\\circ f)(x)=g(f(x))",
                        listOf(
                            "f($k) = $a($k)+$b ;; f($k) = $fk || Fungsi dalam sekarang adalah \$f\$, kerjakan lebih dulu.",
                            "(g\\circ f)($k) = g(f($k)) ;; (g\\circ f)($k) = g($fk) || Hasil \$f\$ yaitu $fk menjadi masukan untuk \$g\$.",
                            "g($fk) = $c($fk)+$d ;; g($fk) = ${c * fk}+$d ;; g($fk) = $r || Hitung \$g($fk)\$."
                        ),
                        "\$g\\circ f\\neq f\\circ g\$ — urutan menentukan hasil."
                    )
                }
                2 -> {
                    val mm = if (v) ri(2, 6) else 4
                    val nn = if (v) ri(1, 9) else 2
                    val i = if (v) ri(2, 9) else 6
                    val r = mm * i - nn
                    qOf(
                        ch, t,
                        "Suatu mesin mengubah bilangan masukan dengan aturan setiap bilangan dikalikan $mm kemudian dikurang $nn. Jika masukan mesin adalah $i, hasil keluarannya adalah ....",
                        mkN(r, listOf(mm * (i - nn), mm * i + nn, r + mm)),
                        listOf("Aturan: \$\\times$mm\$ lalu \$-$nn\$", "Masukan = $i"),
                        "f(x)=${mm}x-$nn",
                        listOf(
                            "f(x) = ${mm}x-$nn || Aturan mesin: kalikan $mm, kemudian kurangi $nn.",
                            "f($i) = $mm($i)-$nn || Masukkan bilangan $i ke mesin.",
                            "f($i) = ${mm * i}-$nn ;; f($i) = $r || Kali dulu ($mm×$i=${mm * i}), baru kurangi $nn. Hasil keluaran $r."
                        ),
                        "Ikuti urutan kalimat: KALI dulu, baru KURANG."
                    )
                }
                3 -> {
                    val a = ri(2, 5)
                    val b = ri(1, 6)
                    val c = ri(2, 4)
                    val d = ri(1, 7)
                    val w = listOf(
                        lin(a * c, a * d - b),
                        lin(a + c, b + d),
                        lin(a * c, c * b + d),
                        lin(a * c, a * d + b + 1)
                    )
                    val r = lin(a * c, a * d + b)
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$ dan \$g(x)=${lin(c, d)}\$. Rumus \$(f\\circ g)(x)\$ adalah ....",
                        mkX(r, w),
                        listOf("\$f(x)=${lin(a, b)}\$", "\$g(x)=${lin(c, d)}\$"),
                        "(f\\circ g)(x)=f(g(x))",
                        listOf(
                            "(f\\circ g)(x) = f(g(x)) ;; (f\\circ g)(x) = f(${lin(c, d)}) || Masukkan seluruh \$g(x)\$ ke setiap \$x\$ pada \$f\$.",
                            "(f\\circ g)(x) = $a(${lin(c, d)})+$b || Tulis \$f\$ dengan \$x\$ diganti: $a dikali seluruh isi kurung.",
                            "(f\\circ g)(x) = ${a * c}x+${a * d}+$b || Distribusi: $a×${c}x = ${a * c}x dan $a×$d = ${a * d}.",
                            "(f\\circ g)(x) = $r || Jumlahkan suku konstanta: ${a * d}+$b = ${a * d + b}."
                        ),
                        "Distribusikan koefisien ke SEMUA suku di dalam kurung."
                    )
                }
                else -> {
                    val p = ri(1, 5)
                    val mm = ri(2, 4)
                    val q = ri(1, 6)
                    val k = ri(1, 6)
                    val r = (k + p) * mm - q
                    qOf(
                        ch, t,
                        "Diketahui \$h(x)=x+$p\$, \$g(x)=${mm}x\$, dan \$f(x)=x-$q\$. Nilai \$(f\\circ g\\circ h)($k)\$ adalah ....",
                        mkN(r, listOf(k * mm + p - q, (k - q) * mm + p, r + q)),
                        listOf("\$h(x)=x+$p\$", "\$g(x)=${mm}x\$", "\$f(x)=x-$q\$"),
                        "(f\\circ g\\circ h)(x)=f(g(h(x)))",
                        listOf(
                            "h($k) = $k+$p ;; h($k) = ${k + p} || Mulai dari fungsi paling dalam, yaitu \$h\$.",
                            "g(${k + p}) = $mm(${k + p}) ;; g(${k + p}) = ${mm * (k + p)} || Hasil \$h\$ masuk ke \$g\$.",
                            "f(${mm * (k + p)}) = ${mm * (k + p)}-$q ;; f(${mm * (k + p)}) = $r || Hasil \$g\$ masuk ke \$f\$ (paling luar). Hasil akhir $r."
                        ),
                        "Kerjakan dari fungsi PALING KANAN ke kiri."
                    )
                }
            }

            3 -> when (t) {
                0 -> {
                    val a = ri(2, 5)
                    val b = ri(1, 8)
                    val x = ri(2, 9)
                    val k = a * x + b
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=${lin(a, b)}\$. Nilai \$f^{-1}($k)\$ adalah ....",
                        mkN(x, listOf(a * k + b, k - b, x + 1)),
                        listOf("\$f(x)=${lin(a, b)}\$"),
                        "f^{-1}(k)=x\\iff f(x)=k",
                        listOf(
                            "f(x) = $k ;; ${a}x+$b = $k || Invers artinya mencari \$x\$ yang menghasilkan $k.",
                            "${a}x = $k-$b ;; ${a}x = ${k - b} || Pindahkan +$b ke ruas kanan menjadi −$b.",
                            "x = \\frac{${k - b}}{$a} ;; x = $x || Bagi dengan $a. Jadi \$f^{-1}($k)=$x\$.",
                            "f($x) = $a($x)+$b ;; f($x) = $k || Cek: hasilnya $k ✓."
                        ),
                        "Invers = \"balikkan\" proses: kurang dulu, lalu bagi."
                    )
                }
                1 -> {
                    val a = ri(2, 6)
                    val b = ri(1, 9)
                    qOf(
                        ch, t,
                        "Invers dari fungsi \$f(x)=${a}x+$b\$ adalah \$f^{-1}(x)=\$ ....",
                        mkX(
                            "\\frac{x-$b}{$a}",
                            listOf(
                                "\\frac{x+$b}{$a}",
                                "\\frac{x}{$a}+$b",
                                "${a}x-$b",
                                "\\frac{$a}{x-$b}"
                            )
                        ),
                        listOf("\$f(x)=${a}x+$b\$"),
                        "y=ax+b\\Rightarrow x=\\frac{y-b}{a}",
                        listOf(
                            "y = ${a}x+$b || Misalkan \$f(x)=y\$.",
                            "${a}x = y-$b || Pindahkan +$b ke ruas kanan menjadi −$b.",
                            "x = \\frac{y-$b}{$a} || Bagi kedua ruas dengan $a.",
                            "f^{-1}(x) = \\frac{x-$b}{$a} || Ganti nama peubah \$y\$ menjadi \$x\$."
                        ),
                        "Cek: \$f(f^{-1}(x))=x\$."
                    )
                }
                2 -> {
                    var a: Int
                    var b: Int
                    var c: Int
                    var d: Int
                    do {
                        a = ri(1, 4)
                        b = ri(1, 6)
                        c = ri(1, 3)
                        d = ri(1, 5)
                    } while (a * d - b * c == 0)
                    fun mk(p1: Int, q1: Int, p2: Int, q2: Int) = "\\frac{${lin(p1, q1)}}{${lin(p2, q2)}}"
                    val ans = mk(-d, b, c, -a)
                    val cxTerm = if (c == 1) "xy" else "${c}xy"
                    val dyTerm = if (d == 1) "y" else "${d}y"
                    val cyTerm = if (c == 1) "y" else "${c}y"
                    val axTerm = if (a == 1) "x" else "${a}x"
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=\\frac{${lin(a, b)}}{${lin(c, d)}}\$. Rumus \$f^{-1}(x)\$ adalah ....",
                        mkX(
                            ans,
                            listOf(
                                mk(d, b, c, -a),
                                mk(-d, b, c, a),
                                mk(-d, -b, c, -a),
                                mk(d, -b, -c, a)
                            )
                        ),
                        listOf("\$f(x)=\\frac{${lin(a, b)}}{${lin(c, d)}}\$"),
                        "y=\\frac{ax+b}{cx+d}\\Rightarrow x=\\frac{-dy+b}{cy-a}",
                        listOf(
                            "y = \\frac{${lin(a, b)}}{${lin(c, d)}} || Misalkan \$f(x)=y\$ terlebih dahulu.",
                            "y(${lin(c, d)}) = ${lin(a, b)} ;; $cxTerm+$dyTerm = ${lin(a, b)} || Kalikan silang penyebut \$(${lin(c, d)})\$ ke ruas kiri lalu jabarkan.",
                            "$cxTerm-$axTerm = -$dyTerm+$b || Kumpulkan semua suku yang memuat \$x\$ di ruas kiri.",
                            "x($cyTerm-$a) = -$dyTerm+$b || Faktorkan \$x\$ di ruas kiri.",
                            "x = \\frac{-$dyTerm+$b}{$cyTerm-$a} ;; f^{-1}(x) = $ans || Bagi kedua ruas dengan \$($cyTerm-$a)\$, lalu ganti \$y\$ menjadi \$x\$."
                        ),
                        "Untuk cek cepat: bentuk \$\\frac{ax+b}{cx+d}\$ inversnya \$\\frac{-dx+b}{cx-a}\$ (tukar \$a\$ dan \$d\$ lalu beri minus)."
                    )
                }
                3 -> {
                    val c = ri(1, 5)
                    val mm = ri(1, 4)
                    val q = ri(2, 5)
                    val b = mm * (q - 1) - c
                    val x0 = c + mm
                    val bSigned = if (b < 0) "$b" else "+$b"
                    qOf(
                        ch, t,
                        "Diketahui \$f(x)=\\frac{${lin(1, b)}}{x-$c}\$. Nilai \$f^{-1}($q)\$ adalah ....",
                        mkN(x0, listOf(q, c * q + b, x0 + 2)),
                        listOf("\$f(x)=\\frac{${lin(1, b)}}{x-$c}\$", "Cari \$f^{-1}($q)\$"),
                        "y=\\frac{x+b}{x-c}\\Rightarrow x=\\frac{cy+b}{y-1}",
                        listOf(
                            "\\frac{${lin(1, b)}}{x-$c} = $q || Cari \$x\$ sehingga \$f(x)=$q\$.",
                            "${lin(1, b)} = $q(x-$c) || Kalikan silang: kedua ruas dikali \$(x-$c)\$.",
                            "${lin(1, b)} = ${q}x-${q * c} || Distribusikan $q ke dalam kurung.",
                            "${q}x-x = ${q * c}$bSigned ;; ${q - 1}x = ${q * c + b} || Kumpulkan suku \$x\$ di kiri dan konstanta di kanan.",
                            "x = \\frac{${q * c + b}}{${q - 1}} ;; x = $x0 || Bagi dengan ${q - 1}. Jadi \$f^{-1}($q)=$x0\$."
                        ),
                        "Kalikan silang untuk membuang pecahan."
                    )
                }
                else -> {
                    val A = if (v) ri(4, 9) * 1000 else 7000
                    val B = if (v) pick(listOf(2000, 2500, 3000, 4000)) else 3000
                    val x0 = ri(2, 12)
                    val T = A + B * x0
                    qOf(
                        ch, t,
                        "Biaya ojek online dinyatakan dengan \$f(x)=${B}x+$A\$ (rupiah), dengan \$x\$ jarak dalam km. Jika Dina membayar ${rp(T)}, jarak perjalanannya adalah .... km",
                        mkN(x0, listOf(x0 + 1, x0 - 1, round(T.toDouble() / B.toDouble()).toInt())),
                        listOf("\$f(x)=${B}x+$A\$", "Biaya = ${rp(T)}"),
                        "x=f^{-1}(T)=\\frac{T-$A}{$B}",
                        listOf(
                            "${B}x+$A = $T || Biaya total ${rp(T)} disamakan dengan fungsi biaya.",
                            "${B}x = $T-$A ;; ${B}x = ${T - A} || Pindahkan tarif awal $A ke ruas kanan.",
                            "x = \\frac{${T - A}}{$B} ;; x = $x0 || Bagi dengan tarif per km. Jarak perjalanan $x0 km."
                        ),
                        "Invers dipakai untuk \"mundur\": dari biaya ke jarak."
                    )
                }
            }

            else -> when (t) {
                0 -> {
                    val d = if (v) 7 * ri(3, 40) else 70
                    val r = 22 * d / 7
                    val obj = if (v) pick(listOf("roda sepeda", "roda gerobak", "roda mainan", "roda motor", "roda becak", "roda troli")) else "roda sepeda"
                    qOf(
                        ch, t,
                        "Sebuah $obj memiliki diameter $d cm. Keliling roda tersebut adalah .... (gunakan \$\\pi=\\frac{22}{7}\$)",
                        mkN(r.toDouble(), listOf(r / 2.0, r * 2.0, (r + 10).toDouble(), (r - 10).toDouble()), mt("cm")),
                        listOf("\$d=$d\$ cm", "\$$PI_TEX\$"),
                        "K=\\pi d",
                        listOf(
                            "K = \\pi d ;; K = \\frac{22}{7}\\times$d || Diketahui diameter $d cm.",
                            "K = 22\\times\\frac{$d}{7} ;; K = 22\\times${d / 7} || Coret 7 dengan $d: $d÷7 = ${d / 7}.",
                            "K = $r || 22 × ${d / 7} = $r cm."
                        ),
                        "Diameter → \$K=\\pi d\$. Jari-jari → \$K=2\\pi r\$."
                    )
                }
                1 -> {
                    val r = if (v) 7 * ri(1, 9) else 7
                    val L = 22 * r * r / 7
                    val o = if (v && ri(0, 1) == 1) "meja" else pick(listOf("taman", "kolam", "lapangan"))
                    val unitText = if (o == "meja") "cm" else "meter"
                    val unitTex = if (o == "meja") "cm^2" else "m^2"
                    qOf(
                        ch, t,
                        "Sebuah $o berbentuk lingkaran memiliki jari-jari $r $unitText. Luas $o tersebut adalah .... (gunakan \$\\pi=\\frac{22}{7}\$)",
                        mkN(L, listOf(L + 10, L - 10, 2 * L, 22 * r), mt(unitTex)),
                        listOf("\$r=$r\$", "\$$PI_TEX\$"),
                        "L=\\pi r^2",
                        listOf(
                            "L = \\pi r^2 ;; L = \\frac{22}{7}\\times$r^2 || Luas memakai kuadrat jari-jari.",
                            "L = \\frac{22}{7}\\times${r * r} || $r² = ${r * r}.",
                            "L = 22\\times${r * r / 7} || ${r * r}÷7 = ${r * r / 7} (dicoret).",
                            "L = $L || 22 × ${r * r / 7} = $L. Satuannya kuadrat."
                        ),
                        "Luas pakai \$r^2\$ (satuan kuadrat), keliling pakai \$r\$."
                    )
                }
                2 -> {
                    val k = ri(1, 9)
                    val r = 7 * k
                    val tDeg = pick(listOf(30, 45, 60, 90, 120, 180, 270))
                    val L = tDeg.toDouble() / 360.0 * 2.0 * 22.0 / 7.0 * r.toDouble()
                    qOf(
                        ch, t,
                        "Sebuah juring lingkaran berjari-jari $r cm memiliki sudut pusat \$$tDeg^\\circ\$. Panjang busurnya adalah .... (gunakan \$\\pi=\\frac{22}{7}\$)",
                        mkN(L, listOf(2.0 * L, L / 2.0, L + 5.0, (22 * r * r / 7).toDouble()), mt("cm")),
                        listOf("\$r=$r\$ cm", "\$\\theta=$tDeg^\\circ\$"),
                        "\\text{busur}=\\frac{\\theta}{360^\\circ}\\times2\\pi r",
                        listOf(
                            "K = 2\\pi r ;; K = 2\\times\\frac{22}{7}\\times$r ;; K = ${44 * k} || Keliling lebih dulu: 2×22×$k = ${44 * k}.",
                            "\\text{busur} = \\frac{$tDeg}{360}\\times K || Busur adalah sebagian keliling, sebesar perbandingan sudut $tDeg° terhadap 360°.",
                            "\\text{busur} = \\frac{$tDeg}{360}\\times${44 * k} ;; \\text{busur} = ${fmtNum(L)} || Kalikan. Panjang busur ${fmtNum(L)} cm."
                        ),
                        "Busur = pecahan sudut × keliling."
                    )
                }
                3 -> {
                    val nn = if (v) pick(listOf(3, 4, 5, 6, 9, 10, 12, 15, 18, 20, 24, 36)) else 8
                    val a = 360.0 / nn.toDouble()
                    val fd = if (v) pick(listOf("pizza", "kue tart", "martabak manis", "pai apel")) else "pizza"
                    qOf(
                        ch, t,
                        "Sebuah $fd berbentuk lingkaran dipotong menjadi $nn bagian sama besar. Besar sudut pusat setiap potongan $fd adalah ....",
                        mkN(a, listOf(360.0 / (nn + 1), 360.0 / (nn - 1), 2.0 * a, a / 2.0), ::deg),
                        listOf("Satu lingkaran = \$360^\\circ\$", "Dibagi $nn sama besar"),
                        "\\theta=\\frac{360^\\circ}{n}",
                        listOf(
                            "\\theta = \\frac{360^\\circ}{n} ;; \\theta = \\frac{360^\\circ}{$nn} || Satu $fd penuh = 360°, dibagi $nn bagian sama besar.",
                            "\\theta = ${fmtNum(a)}^\\circ || Hasil pembagian.",
                            "$nn\\times${fmtNum(a)}^\\circ = 360^\\circ || Cek: jumlah semua potongan harus 360° ✓."
                        ),
                        "Satu putaran penuh selalu 360°."
                    )
                }
                else -> {
                    val d = if (v) 7 * ri(2, 40) else 28
                    val r = 22 * d / 7
                    val ob = if (v) pick(listOf("taman", "kolam", "lapangan")) else "taman"
                    qOf(
                        ch, t,
                        "Sebuah $ob berbentuk lingkaran mempunyai diameter $d meter. Panjang pagar yang diperlukan untuk mengelilingi $ob tersebut adalah .... (gunakan \$\\pi=\\frac{22}{7}\$)",
                        mkN(r.toDouble(), listOf(r / 2.0, 2.0 * r, (r + 11).toDouble(), (r - 11).toDouble()), mt("meter")),
                        listOf("\$d=$d\$ m", "\$$PI_TEX\$"),
                        "K=\\pi d",
                        listOf(
                            "K = \\pi d || Pagar yang mengelilingi $ob sama dengan keliling lingkaran.",
                            "K = \\frac{22}{7}\\times$d ;; K = 22\\times${d / 7} || Coret 7: $d÷7 = ${d / 7}.",
                            "K = $r || Panjang pagar $r meter."
                        ),
                        "\"Mengelilingi\" = keliling."
                    )
                }
            }
        }
    }

    fun getChapterQuestions(ch: Int): List<Question> {
        val safeCh = ch.coerceIn(0, chapters.size - 1)
        return bankCache.getOrPut(safeCh) {
            val seen = HashSet<String>()
            val list = ArrayList<Question>(QUESTIONS_PER_CHAPTER)
            for (i in 0 until QUESTIONS_PER_CHAPTER) {
                var q: Question
                var attempt = 0
                do {
                    val seed = safeCh * 10000 + i * 7 + 13 + attempt * 1009
                    q = buildTemplate(safeCh, i % 5, i >= 5, RngScope(Mulberry32(seed)))
                    attempt++
                } while (seen.contains(q.prompt) && attempt < 100)
                seen.add(q.prompt)
                list.add(q)
            }
            list
        }
    }

    fun getBankQuestion(ch: Int, index: Int): Question {
        val list = getChapterQuestions(ch)
        return list[index.coerceIn(0, list.lastIndex)]
    }

    fun generateRandomQuestion(chapterFilter: Int, seed: Int = (System.nanoTime() xor 0x5A5A5A5A).toInt()): Question {
        val rng = RngScope(Mulberry32(seed))
        val ch = if (chapterFilter < 0) rng.ri(0, chapters.lastIndex) else chapterFilter.coerceIn(0, chapters.lastIndex)
        val t = rng.ri(0, 4)
        return buildTemplate(ch, t, true, rng)
    }

    fun splitTopLevelEquals(text: String): List<String> {
        val parts = mutableListOf<String>()
        var depth = 0
        val current = StringBuilder()
        for (ch in text) {
            if (ch == '{') depth++
            else if (ch == '}') depth = (depth - 1).coerceAtLeast(0)
            if (ch == '=' && depth == 0) {
                parts.add(current.toString())
                current.clear()
            } else {
                current.append(ch)
            }
        }
        parts.add(current.toString())
        return parts
    }

    fun buildSolutionBreakdown(q: Question): SolutionBreakdown {
        data class MutableChain(val left: String, val rows: MutableList<String>, val isRaw: Boolean = false)

        val chains = mutableListOf<MutableChain>()
        var cur: MutableChain? = null

        for (step in q.steps) {
            val leftPart = step.split("||").firstOrNull().orEmpty()
            val lines = leftPart.split(";;")
            for (rawLine in lines) {
                val line = rawLine.trim()
                val eqParts = splitTopLevelEquals(line)
                if (eqParts.size < 2) {
                    chains.add(MutableChain(line, mutableListOf(), isRaw = true))
                    cur = null
                } else {
                    val lf = eqParts.first().trim()
                    val rhs = eqParts.drop(1).joinToString("=").trim()
                    if (cur != null && (lf.isEmpty() || lf == cur.left)) {
                        cur.rows.add(rhs)
                    } else {
                        cur = MutableChain(lf, mutableListOf(rhs))
                        chains.add(cur)
                    }
                }
            }
        }

        val defs = mutableMapOf<String, String>()
        val defRegex = Regex("^\\$([a-z])\\(x\\)=(.+)\\$$")
        for (d in q.given) {
            val m = defRegex.matchEntire(d.trim())
            if (m != null) {
                defs[m.groupValues[1]] = m.groupValues[2]
            }
        }

        val callRegex = Regex("^([a-z])\\(-?\\d+\\)$")
        val finalChains = chains.mapIndexed { ci, chain ->
            if (chain.isRaw) {
                SolutionChain(chain.left, emptyList(), isRaw = true)
            } else {
                val rows = chain.rows.toMutableList()
                var left = chain.left
                val callMatch = callRegex.matchEntire(left)
                if (callMatch != null && defs.containsKey(callMatch.groupValues[1])) {
                    rows.add(0, defs.getValue(callMatch.groupValues[1]))
                    left = "${callMatch.groupValues[1]}(x)"
                }
                if (ci == chains.lastIndex && rows.isNotEmpty()) {
                    rows[rows.lastIndex] = "\\underline{\\underline{${rows.last()}}}"
                }
                SolutionChain(left, rows, isRaw = false)
            }
        }

        val explanations = q.steps.mapNotNull { step ->
            val parts = step.split("||")
            if (parts.size > 1) parts[1].trim().takeIf { it.isNotEmpty() } else null
        }

        return SolutionBreakdown(finalChains, explanations)
    }
}
