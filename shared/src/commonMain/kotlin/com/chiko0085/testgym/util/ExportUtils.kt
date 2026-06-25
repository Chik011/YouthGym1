package com.chiko0085.testgym.util

import com.chiko0085.testgym.model.Member
import com.chiko0085.testgym.formatEpochToDate

// Generator HTML laporan pendapatan
fun generateRevenueHtml(members: List<Member>, filterRange: String = "Semua Waktu"): String {
    val sortedMembers = members.sortedBy { it.joinDate }
    var cumulativeRevenue = 0.0

    val htmlBuilder = StringBuilder()
    htmlBuilder.append("""
        <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <style>
                table { border-collapse: collapse; width: 100%; font-family: sans-serif; }
                th { background-color: #3B82F6; color: white; border: 1px solid #ddd; padding: 12px; text-align: left; }
                td { border: 1px solid #ddd; padding: 10px; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .total-row { background-color: #1E293B; color: white; font-weight: bold; }
                .header-title { font-size: 24px; color: #3B82F6; font-weight: bold; margin-bottom: 20px; text-align: center; }
            </style>
        </head>
        <body>
            <div class="header-title">LAPORAN PENDAPATAN YOUTH GYM ($filterRange)</div>
            <table>
                <thead>
                    <tr>
                        <th>No</th>
                        <th>ID Member</th>
                        <th>Nama Member</th>
                        <th>Paket</th>
                        <th>Tanggal Transaksi</th>
                        <th>Pendapatan</th>
                        <th>Total Akumulasi</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    sortedMembers.forEachIndexed { index, m ->
        cumulativeRevenue += (m.pricePaid ?: 0.0)
        val dateStr = formatEpochToDate(m.joinDate).split(" ").take(3).joinToString(" ")
        htmlBuilder.append("""
            <tr>
                <td>${index + 1}</td>
                <td>${m.id}</td>
                <td>${m.name}</td>
                <td>${m.packageName}</td>
                <td>$dateStr</td>
                <td>Rp ${formatRupiah(m.pricePaid ?: 0.0)}</td>
                <td>Rp ${formatRupiah(cumulativeRevenue)}</td>
            </tr>
        """.trimIndent())
    }

    htmlBuilder.append("""
                </tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="6" style="text-align: right;">TOTAL PENDAPATAN AKHIR</td>
                        <td>Rp ${formatRupiah(cumulativeRevenue)}</td>
                    </tr>
                </tfoot>
            </table>
        </body>
        </html>
    """.trimIndent())

    return htmlBuilder.toString()
}
