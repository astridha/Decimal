package io.github.astridha.decimal

import kotlin.math.min
import io.github.astridha.decimal.Decimal.Companion.MAX_LONG_SIGNIFICANTS
//import System

private fun mantissaStringDisposableDecimalPlaces(mantissaString: String, decimals:Int): Int {
    val mantissaLength = mantissaString.length
    if (mantissaLength >= Decimal.MAX_LONG_SIGNIFICANTS) println("disposable: mantissaLength: $mantissaLength, decimals: $decimals => ${min(mantissaLength - (MAX_LONG_SIGNIFICANTS+1), decimals)}")
    if (mantissaLength > Decimal.MAX_LONG_SIGNIFICANTS) {
        return min(mantissaLength - MAX_LONG_SIGNIFICANTS, decimals)
    }
    if ((mantissaLength == Decimal.MAX_LONG_SIGNIFICANTS)
     and (mantissaString.compareTo(Decimal.MAX_LONG_VALUE_STRING) > 0)) {
        return min(mantissaLength - (MAX_LONG_SIGNIFICANTS+1), decimals)
    }
    return 0
}

@Throws(NumberFormatException::class, ArithmeticException::class)
internal fun mkDecimalParseOrNull (rawNumberString: String, orNull: Boolean) : Pair <Long, Int>? {
    val cleanedNumberString = rawNumberString.replace("_","").replace(" ","")

    val decimalNumberPattern = """(?<prefix>[+-])?(?<integer>[+-]?\d*)(?:\.(?<fraction>\d*))?(?:[Ee](?<exponent>[+-]?\d+))?"""
    val decimalNumberRegex = Regex(decimalNumberPattern)

    val match = decimalNumberRegex.matchEntire(cleanedNumberString)

    if (match == null) {

        if (orNull) return null
        if (Decimal.getThrowOnErrors()) throw NumberFormatException("INVALID DECIMAL FORMAT: \"$rawNumberString\"")
        return Pair(0, Decimal.ArithmeticErrors.NOT_A_NUMBER.ordinal)

    } else {
        println("\nNumberString: \"$cleanedNumberString\"")

        val exponent = (match.groups["exponent"]?.value ?: "0").toInt()

        val fractionString = (match.groups["fraction"]?.value ?: "").trimEnd('0')
        var decimalPlaces = fractionString.length

        val integerString = (match.groups["integer"]?.value ?: "").trimStart('0')
        val prefixString = match.groups["prefix"]?.value ?: ""

        var mantissaString = prefixString + integerString + fractionString
        decimalPlaces -= exponent                 // exponent calculates reverse, 0 - exponent = decimal places!

        // if necessary, truncate to Long (but only decimal digits) and condense again
        val disposableDecimalPlaces = mantissaStringDisposableDecimalPlaces(mantissaString, decimalPlaces)
        if (disposableDecimalPlaces > 0) {
            println("dispose $disposableDecimalPlaces")
            mantissaString = mantissaString.dropLast(disposableDecimalPlaces)
            decimalPlaces -= disposableDecimalPlaces
            // once again remove possible trailing 0s from decimals part
            while ((decimalPlaces > 0) and (mantissaString.last()=='0')) {
                mantissaString = mantissaString.dropLast(1)
                decimalPlaces--
            }

        }
        if ((mantissaString.length > Decimal.MAX_LONG_SIGNIFICANTS)
            or ((mantissaString.length == Decimal.MAX_LONG_SIGNIFICANTS)
            and (mantissaString.compareTo(Decimal.MAX_LONG_VALUE_STRING) > 0))) {

            if (orNull) return null
            if (Decimal.getThrowOnErrors()) throw ArithmeticException("DECIMAL OVERFLOW: \"$rawNumberString\"")
            return Pair(0, Decimal.ArithmeticErrors.OVERFLOW.ordinal)
            //return Pair(123456123L, 3)
        }

        if (mantissaString in listOf("+", "- ", "")) mantissaString += "0"
        println("mantissaString: \"$mantissaString\", decimalPlaces: $decimalPlaces")
        val mantissa: Long = mantissaString.toLong()

        return Pair(mantissa, decimalPlaces)

    }
}
