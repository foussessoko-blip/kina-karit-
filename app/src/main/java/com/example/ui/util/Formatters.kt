package com.example.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object FrenchFormatters {
    private val frenchLocale = Locale.FRENCH

    private val symbols = DecimalFormatSymbols(frenchLocale).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }

    private val integerFormat = DecimalFormat("#,##0", symbols)
    private val decimalFormat = DecimalFormat("#,##0.##", symbols)
    private val percentFormat = DecimalFormat("0.0", symbols)

    fun formatFcfa(amount: Double): String {
        val rounded = amount.roundToLong()
        return "${integerFormat.format(rounded)} FCFA"
    }

    fun formatFcfaSigned(amount: Double): String {
        val rounded = amount.roundToLong()
        val prefix = when {
            rounded > 0L -> "+"
            rounded < 0L -> "-"
            else -> ""
        }
        return "$prefix${integerFormat.format(abs(rounded))} FCFA"
    }

    fun formatQuantity(value: Double): String {
        return decimalFormat.format(value)
    }

    fun formatQuantityWithUnit(value: Double, unit: String): String {
        return "${decimalFormat.format(value)} $unit"
    }

    fun formatPercent(value: Double): String {
        return "${percentFormat.format(value)} %"
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", frenchLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", frenchLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatFileTimestamp(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun parseFrenchDouble(raw: String): Double? {
        val cleaned = raw.trim().replace(" ", "").replace(",", ".")
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()
    }
}
