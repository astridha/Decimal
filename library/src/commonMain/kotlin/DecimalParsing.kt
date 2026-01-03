package io.github.astridha.decimal

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

        val exponent = (match.groups["exponent"]?.value ?: "0").toInt()

        val fractionString = (match.groups["fraction"]?.value ?: "").trimEnd('0')
        var decimalPlaces = fractionString.length

        val integerString = (match.groups["integer"]?.value ?: "").trimStart('0')
        val prefixString = match.groups["prefix"]?.value ?: ""

        var mantissaString = prefixString + integerString + fractionString
        decimalPlaces -= exponent                 // exponent calculates reverse, 0 - exponent = decimal places!

        // truncate and condense if necessary and possible, but keep one digit for later rounding in desired mode
        val maxDecimalPlacesPlus1 = Decimal.getMaxDecimalPlaces()+1
        if (decimalPlaces > maxDecimalPlacesPlus1) {
            mantissaString = mantissaString.dropLast(decimalPlaces - maxDecimalPlacesPlus1)
            decimalPlaces = maxDecimalPlacesPlus1
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
        }

        if (mantissaString in listOf("+", "- ", "")) mantissaString += "0"
        println("\nNumberString: \"$cleanedNumberString\"")
        println("mantissaString: \"$mantissaString\", decimalPlaces: $decimalPlaces")
        val mantissa: Long = mantissaString.toLong()

        return Pair(mantissa, decimalPlaces)

    }
}
