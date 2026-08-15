package io.github.astridha.fix5decimal

//import io.github.astridha.fix5decimal.`_DecimalArithmetics_.kt_`.Companion.equalizeDecimals
import kotlin.Long
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmRecord
import kotlin.jvm.JvmStatic
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

//import kotlin.reflect.jvm.jvmName


public class Decimal : Number, Comparable<Decimal> {

    // no default constructor!
    // no init block!

    // 64-bit long mantissa, fix 5 decimal places:
    private var decimal64: Long = 0L


    public enum class RoundingMode {
        UP,
        DOWN,
        CEILING,
        FLOOR,
        HALF_UP,
        HALF_DOWN,
        HALF_EVEN,
        UNNECESSARY
    }

    /***********************  Secondary Constructors  ************************/



    @Throws(NumberFormatException::class, ArithmeticException::class)
    public constructor (
        rawNumberString: String,
        locale: Locale = autoLocale
    ) { // or explicit RoundingMode?
         val parsedMantissa: Long? = decimalParseOrNull(rawNumberString, autoRoundingMode, locale)
        if (parsedMantissa != null) {
            decimal64 = parsedMantissa
         } else {
            // no additional exception. this branch did already throw
            decimal64 = Decimal.MANTISSA_NAN_VALUE
        }
    }


    @Throws(ArithmeticException::class)
    public constructor (float: Float) : this(float.toString(), noLocale)

    @Throws(ArithmeticException::class)
    public constructor (double: Double) : this(double.toString(), noLocale)

    public constructor (other: Decimal) {
        decimal64 = other.decimal64   // or: clone()? difference?
    }

    internal constructor (mantissa: Long, hasAlready5Decimals: Boolean) {
        if (hasAlready5Decimals) decimal64 = mantissa
        else decimal64 = mantissa * FIX_DECIMAL_FACTOR
    }

    @Throws(ArithmeticException::class)
    public constructor (long: Long) {
        if (abs(long) > FIX_MAX_LONGCONSTRUCTOR) {
            // a single value will overflow
            throwError("$long cannot fit into a Decimal")
        }
        decimal64 = normalizeMantissaToFixedDecimalPlaces(long * FIX_DECIMAL_FACTOR, FIX_DECIMAL_PLACES)
    }

    // Provide constructors from all other signed types for Kotlin and JVM/Java.
    public constructor (byte: Byte) : this(byte.toLong())
    public constructor (short: Short) : this(short.toLong())
    public constructor (int: Int) : this(int.toLong())

    // Constructors from unsigned types would clash with those from signed types in JVM!
    // Because Java does not know unsigned numbers.
    // So -> no plain constructor for unsigned types!
    // Instead, see the work-around invoke expressions in the Companion object!

    /**************************** Packing / Unpacking Helper Methods  ********************************/

    internal fun unpack64(): Pair<Long, Int> {
        return Pair(decimal64, FIX_DECIMAL_PLACES)
    }

    internal fun normalizeMantissaToFixedDecimalPlaces(pMantissa: Long, pDecimals: Int): Long {
        var mantissa = pMantissa
        var decimals = pDecimals

        while (decimals < FIX_DECIMAL_PLACES) {
            mantissa *= 10
            decimals++
        }

        // no rounding! should add?
        while (decimals > FIX_DECIMAL_PLACES) {
            mantissa /= 10
            decimals--
        }
        return mantissa
    }

    public fun isNaN(): Boolean {
        return (decimal64 == MANTISSA_NAN_VALUE)
    }
    public fun isInfinite(): Boolean {
        return ((decimal64 == MANTISSA_POSITIVE_INFINITY_VALUE) || (decimal64 == MANTISSA_NEGATIVE_INFINITY_VALUE))
    }
    public fun isFinite(): Boolean {
        return !((isNaN()) || (isInfinite()))
    }

    public fun isError(): Boolean {
        return !(isFinite())
    }

    /*******************  Rounding functions  *********************************/

    public fun ceil(desiredDecimals: Int): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(
            mantissa,
            decimals,
            desiredDecimals,
            RoundingMode.CEILING
        )
        return Decimal(newMantissa, true)
    }

    public fun ceil(): Decimal = ceil(0)


    public fun floor(desiredDecimals: Int): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(
            mantissa,
            decimals,
            desiredDecimals,
            RoundingMode.FLOOR
        )
        return Decimal(newMantissa, true)
    }

    public fun floor(): Decimal = floor(0)

    public fun truncate(desiredDecimals: Int): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(
            mantissa,
            decimals,
            desiredDecimals,
            RoundingMode.DOWN
        )
        return Decimal(newMantissa, true)
    }

    public fun truncate(): Decimal = truncate(0)


    public fun round(desiredDecimals: Int): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(
            mantissa,
            decimals,
            desiredDecimals,
            RoundingMode.HALF_EVEN
        )
        return Decimal(newMantissa, true)
    }

    public fun round(): Decimal = round(0)


    //@JvmOverloads
    public fun scale(desiredDecimals: Int, roundingMode: RoundingMode): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val roundingDecimals = min(FIX_DECIMAL_PLACES, desiredDecimals)
        val (newMantissa, newDecimals) = roundWithMode(
            mantissa,
            decimals,
            roundingDecimals,
            roundingMode
        )
        return Decimal(newMantissa, true)
    }

    public fun scale(): Decimal =
        scale(desiredDecimals = FIX_DECIMAL_PLACES, roundingMode = autoRoundingMode)

    public fun scale(desiredDecimals: Int): Decimal = scale(desiredDecimals, roundingMode = autoRoundingMode)
    public fun scale(roundingMode: RoundingMode): Decimal = scale(FIX_DECIMAL_PLACES, roundingMode)

    /*******************  Operator Overloads  ******************/

    /***  unary operators ***/

    /*** operator unaryPlus (+) , unaryMinus (-) and not() (!) ***/

    public operator fun unaryPlus(): Decimal = this // or: clone()?

    public operator fun unaryMinus(): Decimal {
        if (isNaN()) return clone()
        return Decimal(0 - decimal64, true)
    }

    public operator fun not(): Boolean = (decimal64 == 0L)


    /***** operator unaryIncrement (++) , unaryDecrement (--)  *****/

    @Throws(ArithmeticException::class)
    public operator fun inc(): Decimal {
        if (isNaN()) return clone()
        val incremented_decimal64 = decimal64 + FIX_DECIMAL_FACTOR
        if (incremented_decimal64 < decimal64) {
            return throwError(" Incrementing $this: result does not fit into Decimal")
        }
        return Decimal(incremented_decimal64, true)
    }

    @Throws(ArithmeticException::class)
    public operator fun dec(): Decimal {
        if (isNaN()) return clone()
        val decremented_decimal64 = decimal64 - FIX_DECIMAL_FACTOR
        if ((decremented_decimal64 > decimal64) || (decremented_decimal64 == MANTISSA_NAN_VALUE)) {
            return throwError(" Decrementing $this: result does not fit into Decimal")
        }
        return Decimal(decremented_decimal64, true)
    }

    /*********************  Arithmetic operator overloads  **************************/

    private fun Long.isNegative() = (this.sign < 0)
    private fun Long.isPositive() = (this.sign >= 0)

    /***** operator plus (+) *****/

    @Throws(ArithmeticException::class)
    public operator fun plus(other: Decimal): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticPlus(this, other)
    }

    public operator fun plus(otherDouble: Double): Decimal = plus(otherDouble.toDecimal())
    public operator fun plus(otherFloat: Float): Decimal = plus(otherFloat.toDecimal())
    public operator fun plus(otherLong: Long): Decimal = plus(otherLong.toDecimal())
    public operator fun plus(otherInt: Int): Decimal = plus(otherInt.toDecimal())
    public operator fun plus(otherShort: Short): Decimal = plus(otherShort.toDecimal())
    public operator fun plus(otherByte: Byte): Decimal = plus(otherByte.toDecimal())
    public operator fun plus(otherULong: ULong): Decimal = plus(otherULong.toDecimal())
    public operator fun plus(otherUInt: UInt): Decimal = plus(otherUInt.toDecimal())
    public operator fun plus(otherUShort: UShort): Decimal = plus(otherUShort.toDecimal())
    public operator fun plus(otherUByte: UByte): Decimal = plus(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun plus(other: Decimal, rounding: Rounding): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticPlus(this, other, rounding)
    }

    public fun plus(otherDouble: Double, rounding: Rounding): Decimal = plus(otherDouble.toDecimal(), rounding)
    public fun plus(otherFloat: Float, rounding: Rounding): Decimal = plus(otherFloat.toDecimal(), rounding)
    public fun plus(otherLong: Long, rounding: Rounding): Decimal = plus(otherLong.toDecimal(), rounding)
    public fun plus(otherInt: Int, rounding: Rounding): Decimal = plus(otherInt.toDecimal(), rounding)
    public fun plus(otherShort: Short, rounding: Rounding): Decimal = plus(otherShort.toDecimal(), rounding)
    public fun plus(otherByte: Byte, rounding: Rounding): Decimal = plus(otherByte.toDecimal(), rounding)
    public fun plus(otherULong: ULong, rounding: Rounding): Decimal = plus(otherULong.toDecimal(), rounding)
    public fun plus(otherUInt: UInt, rounding: Rounding): Decimal = plus(otherUInt.toDecimal(), rounding)
    public fun plus(otherUShort: UShort, rounding: Rounding): Decimal = plus(otherUShort.toDecimal(), rounding)
    public fun plus(otherUByte: UByte, rounding: Rounding): Decimal = plus(otherUByte.toDecimal(), rounding)


    /***** operator minus (-) *****/

    @Throws(ArithmeticException::class)
    public operator fun minus(other: Decimal): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticMinus(this, other)
    }

    public operator fun minus(otherDouble: Double): Decimal = minus(otherDouble.toDecimal())
    public operator fun minus(otherFloat: Float): Decimal = minus(otherFloat.toDecimal())
    public operator fun minus(otherLong: Long): Decimal = minus(otherLong.toDecimal())
    public operator fun minus(otherInt: Int): Decimal = minus(otherInt.toDecimal())
    public operator fun minus(otherShort: Short): Decimal = minus(otherShort.toDecimal())
    public operator fun minus(otherByte: Byte): Decimal = minus(otherByte.toDecimal())
    public operator fun minus(otherULong: ULong): Decimal = minus(otherULong.toDecimal())
    public operator fun minus(otherUInt: UInt): Decimal = minus(otherUInt.toDecimal())
    public operator fun minus(otherUShort: UShort): Decimal = minus(otherUShort.toDecimal())
    public operator fun minus(otherUByte: UByte): Decimal = minus(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun minus(other: Decimal, rounding: Rounding): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticMinus(this, other, rounding)
    }

    public fun minus(otherDouble: Double, rounding: Rounding): Decimal = minus(otherDouble.toDecimal(), rounding)
    public fun minus(otherFloat: Float, rounding: Rounding): Decimal = minus(otherFloat.toDecimal(), rounding)
    public fun minus(otherLong: Long, rounding: Rounding): Decimal = minus(otherLong.toDecimal(), rounding)
    public fun minus(otherInt: Int, rounding: Rounding): Decimal = minus(otherInt.toDecimal(), rounding)
    public fun minus(otherShort: Short, rounding: Rounding): Decimal = minus(otherShort.toDecimal(), rounding)
    public fun minus(otherByte: Byte, rounding: Rounding): Decimal = minus(otherByte.toDecimal(), rounding)
    public fun minus(otherULong: ULong, rounding: Rounding): Decimal = minus(otherULong.toDecimal(), rounding)
    public fun minus(otherUInt: UInt, rounding: Rounding): Decimal = minus(otherUInt.toDecimal(), rounding)
    public fun minus(otherUShort: UShort, rounding: Rounding): Decimal = minus(otherUShort.toDecimal(), rounding)
    public fun minus(otherUByte: UByte, rounding: Rounding): Decimal = minus(otherUByte.toDecimal(), rounding)


    /***** operator times (*) *****/

    @Throws(ArithmeticException::class)
    public operator fun times(other: Decimal): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticTimes(this, other)
    }

    public operator fun times(otherDouble: Double): Decimal = times(otherDouble.toDecimal())
    public operator fun times(otherFloat: Float): Decimal = times(otherFloat.toDecimal())
    public operator fun times(otherLong: Long): Decimal = times(otherLong.toDecimal())
    public operator fun times(otherInt: Int): Decimal = times(otherInt.toDecimal())
    public operator fun times(otherShort: Short): Decimal = times(otherShort.toDecimal())
    public operator fun times(otherByte: Byte): Decimal = times(otherByte.toDecimal())
    public operator fun times(otherULong: ULong): Decimal = times(otherULong.toDecimal())
    public operator fun times(otherUInt: UInt): Decimal = times(otherUInt.toDecimal())
    public operator fun times(otherUShort: UShort): Decimal = times(otherUShort.toDecimal())
    public operator fun times(otherUByte: UByte): Decimal = times(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun times(other: Decimal, rounding: Rounding): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticTimes(this, other, rounding)
    }

    public fun times(otherDouble: Double, rounding: Rounding): Decimal = times(otherDouble.toDecimal(), rounding)
    public fun times(otherFloat: Float, rounding: Rounding): Decimal = times(otherFloat.toDecimal(), rounding)
    public fun times(otherLong: Long, rounding: Rounding): Decimal = times(otherLong.toDecimal(), rounding)
    public fun times(otherInt: Int, rounding: Rounding): Decimal = times(otherInt.toDecimal(), rounding)
    public fun times(otherShort: Short, rounding: Rounding): Decimal = times(otherShort.toDecimal(), rounding)
    public fun times(otherByte: Byte, rounding: Rounding): Decimal = times(otherByte.toDecimal(), rounding)
    public fun times(otherULong: ULong, rounding: Rounding): Decimal = times(otherULong.toDecimal(), rounding)
    public fun times(otherUInt: UInt, rounding: Rounding): Decimal = times(otherUInt.toDecimal(), rounding)
    public fun times(otherUShort: UShort, rounding: Rounding): Decimal = times(otherUShort.toDecimal(), rounding)
    public fun times(otherUByte: UByte, rounding: Rounding): Decimal = times(otherUByte.toDecimal(), rounding)


    /***** operator div (/) *****/

    @Throws(ArithmeticException::class)
    public operator fun div(other: Decimal): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticDiv(this, other)
    }

    public operator fun div(otherDouble: Double): Decimal = div(otherDouble.toDecimal())
    public operator fun div(otherFloat: Float): Decimal = div(otherFloat.toDecimal())
    public operator fun div(otherLong: Long): Decimal = div(otherLong.toDecimal())
    public operator fun div(otherInt: Int): Decimal = div(otherInt.toDecimal())
    public operator fun div(otherShort: Short): Decimal = div(otherShort.toDecimal())
    public operator fun div(otherByte: Byte): Decimal = div(otherByte.toDecimal())
    public operator fun div(otherULong: ULong): Decimal = div(otherULong.toDecimal())
    public operator fun div(otherUInt: UInt): Decimal = div(otherUInt.toDecimal())
    public operator fun div(otherUShort: UShort): Decimal = div(otherUShort.toDecimal())
    public operator fun div(otherUByte: UByte): Decimal = div(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun div(other: Decimal, rounding: Rounding): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticDiv(this, other, rounding)
    }

    public fun div(otherDouble: Double, rounding: Rounding): Decimal = div(otherDouble.toDecimal(), rounding)
    public fun div(otherFloat: Float, rounding: Rounding): Decimal = div(otherFloat.toDecimal(), rounding)
    public fun div(otherLong: Long, rounding: Rounding): Decimal = div(otherLong.toDecimal(), rounding)
    public fun div(otherInt: Int, rounding: Rounding): Decimal = div(otherInt.toDecimal(), rounding)
    public fun div(otherShort: Short, rounding: Rounding): Decimal = div(otherShort.toDecimal(), rounding)
    public fun div(otherByte: Byte, rounding: Rounding): Decimal = div(otherByte.toDecimal(), rounding)
    public fun div(otherULong: ULong, rounding: Rounding): Decimal = div(otherULong.toDecimal(), rounding)
    public fun div(otherUInt: UInt, rounding: Rounding): Decimal = div(otherUInt.toDecimal(), rounding)
    public fun div(otherUShort: UShort, rounding: Rounding): Decimal = div(otherUShort.toDecimal(), rounding)
    public fun div(otherUByte: UByte, rounding: Rounding): Decimal = div(otherUByte.toDecimal(), rounding)


    /************* operator rem (%) ************/

    @Throws(ArithmeticException::class)
    public operator fun rem(other: Decimal): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticRem(this, other)
    }

    public operator fun rem(otherDouble: Double): Decimal = rem(otherDouble.toDecimal())
    public operator fun rem(otherFloat: Float): Decimal = rem(otherFloat.toDecimal())
    public operator fun rem(otherLong: Long): Decimal = rem(otherLong.toDecimal())
    public operator fun rem(otherInt: Int): Decimal = rem(otherInt.toDecimal())
    public operator fun rem(otherShort: Short): Decimal = rem(otherShort.toDecimal())
    public operator fun rem(otherByte: Byte): Decimal = rem(otherByte.toDecimal())
    public operator fun rem(otherULong: ULong): Decimal = rem(otherULong.toDecimal())
    public operator fun rem(otherUInt: UInt): Decimal = rem(otherUInt.toDecimal())
    public operator fun rem(otherUShort: UShort): Decimal = rem(otherUShort.toDecimal())
    public operator fun rem(otherUByte: UByte): Decimal = rem(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun rem(other: Decimal, rounding: Rounding): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticRem(this, other, rounding)
    }

    public fun rem(otherDouble: Double, rounding: Rounding): Decimal = rem(otherDouble.toDecimal(), rounding)
    public fun rem(otherFloat: Float, rounding: Rounding): Decimal = rem(otherFloat.toDecimal(), rounding)
    public fun rem(otherLong: Long, rounding: Rounding): Decimal = rem(otherLong.toDecimal(), rounding)
    public fun rem(otherInt: Int, rounding: Rounding): Decimal = rem(otherInt.toDecimal(), rounding)
    public fun rem(otherShort: Short, rounding: Rounding): Decimal = rem(otherShort.toDecimal(), rounding)
    public fun rem(otherByte: Byte, rounding: Rounding): Decimal = rem(otherByte.toDecimal(), rounding)
    public fun rem(otherULong: ULong, rounding: Rounding): Decimal = rem(otherULong.toDecimal(), rounding)
    public fun rem(otherUInt: UInt, rounding: Rounding): Decimal = rem(otherUInt.toDecimal(), rounding)
    public fun rem(otherUShort: UShort, rounding: Rounding): Decimal = rem(otherUShort.toDecimal(), rounding)
    public fun rem(otherUByte: UByte, rounding: Rounding): Decimal = rem(otherUByte.toDecimal(), rounding)


    /************ infix operator mod (mod) ***********/

    @Throws(ArithmeticException::class)
    public infix fun mod(other: Decimal): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticMod(this, other)
    }

    public infix fun mod(otherDouble: Double): Decimal = mod(otherDouble.toDecimal())
    public infix fun mod(otherFloat: Float): Decimal = mod(otherFloat.toDecimal())
    public infix fun mod(otherLong: Long): Decimal = mod(otherLong.toDecimal())
    public infix fun mod(otherInt: Int): Decimal = mod(otherInt.toDecimal())
    public infix fun mod(otherShort: Short): Decimal = mod(otherShort.toDecimal())
    public infix fun mod(otherByte: Byte): Decimal = mod(otherByte.toDecimal())
    public infix fun mod(otherULong: ULong): Decimal = mod(otherULong.toDecimal())
    public infix fun mod(otherUInt: UInt): Decimal = mod(otherUInt.toDecimal())
    public infix fun mod(otherUShort: UShort): Decimal = mod(otherUShort.toDecimal())
    public infix fun mod(otherUByte: UByte): Decimal = mod(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun mod(other: Decimal, rounding: Rounding): Decimal {
        return `_DecimalArithmetics_.kt_`.arithmeticMod(this, other, rounding)
    }

    public fun mod(otherDouble: Double, rounding: Rounding): Decimal = mod(otherDouble.toDecimal(), rounding)
    public fun mod(otherFloat: Float, rounding: Rounding): Decimal = mod(otherFloat.toDecimal(), rounding)
    public fun mod(otherLong: Long, rounding: Rounding): Decimal = mod(otherLong.toDecimal(), rounding)
    public fun mod(otherInt: Int, rounding: Rounding): Decimal = mod(otherInt.toDecimal(), rounding)
    public fun mod(otherShort: Short, rounding: Rounding): Decimal = mod(otherShort.toDecimal(), rounding)
    public fun mod(otherByte: Byte, rounding: Rounding): Decimal = mod(otherByte.toDecimal(), rounding)
    public fun mod(otherULong: ULong, rounding: Rounding): Decimal = mod(otherULong.toDecimal(), rounding)
    public fun mod(otherUInt: UInt, rounding: Rounding): Decimal = mod(otherUInt.toDecimal(), rounding)
    public fun mod(otherUShort: UShort, rounding: Rounding): Decimal = mod(otherUShort.toDecimal(), rounding)
    public fun mod(otherUByte: UByte, rounding: Rounding): Decimal = mod(otherUByte.toDecimal(), rounding)


    /**********************  Other Math functions ***************************/

    // still missing: pow, pow(n), pow(Dc), sqrt

    public fun abs(): Decimal {
        val (mantissa, decimals) = unpack64()
        return Decimal(abs(mantissa), true)
    }

    public val absoluteValue: Decimal
        get() = abs()

    public val sign: Decimal
        get() = Decimal(decimal64.sign)

    public val numDecimalPlaces: Int
        get() = (decimal64 and FIX_DECIMAL_PLACES.toLong()).toInt()


    /**********************  Converting to Standard Numeric Types ***************************/

    private fun truncatedValue(): Long {
        if (isError()) {
            throwError("cannot convert NaN to an integer value")
            return 0L
        }
        return decimal64 / FIX_DECIMAL_FACTOR
    }

    private fun roundedMantissa(roundingMode: RoundingMode): Long {
        if (isError()) {
            throwError("cannot round NaN to an integer value")
            return 0L
        }
        val (mantissa, decimals) = unpack64()
        if (decimals == 0) return mantissa  // nothing to do
        val (rounded, _) = roundWithMode(mantissa, decimals, FIX_DECIMAL_PLACES, autoRoundingMode)
        return rounded
    }


    public override fun toDouble(): Double = this.toRawString().toDouble()
    public override fun toFloat(): Float = this.toRawString().toFloat()
    public override fun toLong(): Long = truncatedValue()
    public override fun toInt(): Int = truncatedValue().toInt()
    public override fun toShort(): Short = truncatedValue().toShort()
    public override fun toByte(): Byte = truncatedValue().toByte()
    public fun toULong(): ULong = truncatedValue().toULong()
    public fun toUInt(): UInt = truncatedValue().toUInt()
    public fun toUShort(): UShort = truncatedValue().toUShort()
    public fun toUByte(): UByte = truncatedValue().toUByte()
    public fun toLong(roundingMode: RoundingMode): Long = roundedMantissa(roundingMode)
    public fun toInt(roundingMode: RoundingMode): Int = roundedMantissa(roundingMode).toInt()
    public fun toShort(roundingMode: RoundingMode): Short = roundedMantissa(roundingMode).toShort()
    public fun toByte(roundingMode: RoundingMode): Byte = roundedMantissa(roundingMode).toByte()
    public fun toULong(roundingMode: RoundingMode): ULong = roundedMantissa(roundingMode).toULong()
    public fun toUInt(roundingMode: RoundingMode): UInt = roundedMantissa(roundingMode).toUInt()
    public fun toUShort(roundingMode: RoundingMode): UShort = roundedMantissa(roundingMode).toUShort()
    public fun toUByte(roundingMode: RoundingMode): UByte = roundedMantissa(roundingMode).toUByte()
    public fun roundToLong(): Long = roundedMantissa(RoundingMode.CEILING)
    public fun roundToInt(): Int = roundedMantissa(RoundingMode.CEILING).toInt()


    /********************  Unformatted or formatted Output to human-readable Strings  ****************************/


    public fun toRawString(): String {
        if (isError()) return DECIMAL_NAN_AS_STRING
        val (mantissa, decimals) = unpack64()
        return toRawString(mantissa)
    }

    public fun toScientificString(): String {
        if (isError()) return DECIMAL_NAN_AS_STRING
        val (mantissa, decimals) = unpack64()
        if (mantissa == 0L) return "0E0"
        var decimalString: String
        val prefix: String
        when {
            (mantissa < 0) -> {
                decimalString = (0L - mantissa).toString(10); prefix = "-"
            }

            else -> {
                decimalString = mantissa.toString(10); prefix = ""
            }
        }

        val adjustedExp = (decimalString.count() - 1) - decimals
        if (decimalString.count() > 1) decimalString =
            decimalString.take(1) + '.' + decimalString.substring(1).trimEnd('0')


        return prefix + decimalString + 'E' + adjustedExp.toString(10)
    }

    public override fun toString(): String {
        return toString(autoLocale)
    }

    public fun toString(displayFormat: Locale): String {
        if (isError()) return DECIMAL_NAN_AS_STRING
        // inserts grouping delimiters between groups of 3 digits dynamically and adds the minimum of decimal places
        // i.e., needs no formatting string and supports no overall minimum width; but no India lakh/crore format
        val groupingSeparatorString = displayFormat.groupingSeparator?.toString() ?: ""
        val decimalsSeparatorString = displayFormat.decimalSeparator.toString()
        val minDecimalPlaces = displayFormat.minDecimalPlaces
        var rawString = this.toRawString()
        var integerPart: String
        var decimalPart: String
        val decimalPosition = rawString.indexOf(".")
        if (decimalPosition >= 0) {
            integerPart = rawString.take(decimalPosition)
            decimalPart = rawString.substring(decimalPosition + 1)
        } else {
            integerPart = rawString
            decimalPart = ""
        }

        rawString = integerPart.reversed()
            .chunked(3)
            .joinToString(groupingSeparatorString)
            .reversed()
        if (decimalPosition >= 0) {
            rawString = buildString {
                append(rawString)
                append(decimalsSeparatorString)
                append(decimalPart)
            }
        }

        if (minDecimalPlaces > 0) {
            val decimals = decimalPart.length
            val missingPlaces = minDecimalPlaces - decimals
            if (decimals <= 0) rawString += decimalsSeparatorString
            if (missingPlaces > 0) rawString += ("0".repeat(missingPlaces))
        }

        return rawString
    }


    @JvmRecord
    public data class Rounding(val decimalPlaces: Int, val roundingMode: RoundingMode) {
        public constructor (decimalPlaces: Int) : this(decimalPlaces, autoRoundingMode)
        public constructor (roundingMode: RoundingMode) : this(FIX_DECIMAL_PLACES, roundingMode)

        init {
            require(decimalPlaces >= (0 - FIX_DECIMAL_PLACES)) { "decimal places must be greater or equal -$FIX_DECIMAL_PLACES" }
            require(decimalPlaces <= FIX_DECIMAL_PLACES) { "decimal places must not be be greater than $FIX_DECIMAL_PLACES, is: $decimalPlaces" }
        }
    }

    @JvmRecord
    public data class Locale(val groupingSeparator: Char?, val decimalSeparator: Char, val minDecimalPlaces: Int) {
        public constructor (decimalSeparator: Char, minDecimalPlaces: Int) : this(
            null,
            decimalSeparator,
            minDecimalPlaces
        )

        init {
            if (groupingSeparator != null) {
                require((groupingSeparator != decimalSeparator)) { "Grouping separator and decimal separator may not be equal '$groupingSeparator'" }
            }
            require(minDecimalPlaces >= 0) { "Decimal places must be greater or equal 0" }
        }
    }


    /***********  Comparable interface, and equality operators  *************/

    /*****  Clone / Copy Functions  *****/

    public fun clone(): Decimal {
        val (mantissa, decimals) = unpack64()
        return Decimal(mantissa, true)
    }

    public fun copy(): Decimal = this.clone() // which one is better?

    /*****  Compare Functions  *****/

    public override operator fun compareTo(other: Decimal): Int {
        // how to handle NaN?
        // currently, it is lower than any valid value. Ok?
        return when {
            (this.decimal64 == other.decimal64) -> 0
            (this.decimal64 > other.decimal64) -> 1
            (this.decimal64 < other.decimal64) -> -1
            else -> 0
        }
    }

    public override operator fun equals(other: Any?): Boolean =
        ((other != null) && (other is Decimal) && (this.decimal64 == other.decimal64))

    public override fun hashCode(): Int {
        return ((this.decimal64 ushr 32).toInt() xor (this.decimal64 and 0x00000000FFFFFFFFL).toInt())

    }


    /***************************  Companion Object  **************************************/

    public companion object {

        // Simulating constructors out of unsigned numerical types
        // Only for Kotlin, hidden from Java, which would clash with signed constructors!
        public operator fun invoke(ubyte: UByte): Decimal = Decimal(ubyte.toLong())
        public operator fun invoke(ushort: UShort): Decimal = Decimal(ushort.toLong())
        public operator fun invoke(uint: UInt): Decimal = Decimal(uint.toLong())
        public operator fun invoke(ulong: ULong): Decimal = Decimal(ulong.toLong())


        internal fun mkDecimalOrNull(numberString: String, locale: Locale = autoLocale): Decimal? {
            val decimalMantissa: Long? = decimalParseOrNull(numberString, RoundingMode.DOWN, locale)
             return if (decimalMantissa != null) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(
                    decimalMantissa,
                    FIX_DECIMAL_PLACES,
                    FIX_DECIMAL_PLACES
                )
                Decimal(roundedMantissa, true)
            } else {
                null
            }
        }


        public const val FIX_DECIMAL_PLACES: Int = 5
        public const val FIX_DECIMAL_FACTOR: Long = 100000

        internal const val MANTISSA_MAX_VALUE: Long = Long.MAX_VALUE-1
        private const val MANTISSA_MIN_VALUE: Long = Long.MIN_VALUE + 2
        private const val MANTISSA_NAN_VALUE: Long = Long.MIN_VALUE
        private const val MANTISSA_POSITIVE_INFINITY_VALUE: Long = Long.MAX_VALUE
        private const val MANTISSA_NEGATIVE_INFINITY_VALUE: Long = Long.MIN_VALUE + 1
        private const val MANTISSA_ZERO_VALUE: Long = 0L
        private const val MANTISSA_ONE_VALUE: Long = FIX_DECIMAL_FACTOR

        private const val FIX_MAX_LONGCONSTRUCTOR: Long = MANTISSA_MAX_VALUE / FIX_DECIMAL_FACTOR

        private const val MAX_DECIMAL_SIGNIFICANTS: Int = 14
        // public const val MAX_DECIMAL_MANTISSA_AS_STRING: String = "576460752303423486"
        private const val MAX_LONG_SIGNIFICANTS: Int = 19
        private const val SAFE_LONG_SIGNIFICANTS: Int = MAX_LONG_SIGNIFICANTS - 1
        private const val MAX_MANTISSA_AS_STRING: String = "9223372036854775806"
        private const val MIN_MANTISSA_AS_STRING: String = "-9223372036854775806" // reserve ...808 for NAN and 807 for infinities

        private const val DECIMAL_NAN_AS_STRING: String = "NOT A NUMBER"
        private const val DECIMAL_POSITIVE_INFINITY_AS_STRING: String = "+INF"
        private const val DECIMAL_NEGATIVE_INFINITY_AS_STRING: String = "-INF"
        private const val DECIMAL_ZERO_AS_STRING: String = "0"


        @JvmField
        public val ONE: Decimal = Decimal(MANTISSA_ONE_VALUE, true)
        //@get:JvmName("ONE")

        @JvmField
        public val NaN: Decimal = Decimal(MANTISSA_NAN_VALUE, true)
        //@get:JvmName("NaN")

        @JvmField
        public val MAX_VALUE: Decimal = Decimal(MANTISSA_MAX_VALUE, true)
        //@get:JvmName("MAX_VALUE")

        @JvmField
        public val MIN_VALUE: Decimal = Decimal(MANTISSA_MIN_VALUE, true)
        //@get:JvmName("MIN_VALUE")

        @JvmField
        public val POSITIVE_INFINITY: Decimal = Decimal(MANTISSA_POSITIVE_INFINITY_VALUE, true)
        //@get:JvmName("POSITIVE_INFINITY")

        @JvmField
        public val NEGATIVE_INFINITY: Decimal = Decimal(MANTISSA_NEGATIVE_INFINITY_VALUE, true)
        //@get:JvmName("NEGATIVE_INFINITY")

        // static (common) variables and functions

        // throw exceptions, or encode error numbers in Decimal?
        internal var shallThrowOnError: Boolean = true
        @JvmStatic
        public fun setThrowOnErrors(shallThrow: Boolean) {
            shallThrowOnError = shallThrow
        }

        @JvmStatic
        public fun getThrowOnErrors(): Boolean = shallThrowOnError

        internal var autoRoundingMode: RoundingMode = RoundingMode.HALF_UP
        internal var autoDecimalPlaces: Int = FIX_DECIMAL_PLACES

        // for parsing and printing in local format
        internal var autoLocale: Locale = Locale(null, '.', 0)

        // for parsing in Float and Double constructors (via string constructor)
        internal val noLocale: Locale = Locale(null, '.', 0)

        @JvmStatic
        public fun setLocale(locale: Locale) {
            autoLocale = locale
        }

        // for automatic rounding
        //internal var autoRoundingConfig.decimalPlaces: Int = MAX_DECIMAL_PLACES /* 0 - 15 */

        @JvmStatic
        public fun initRoundingMode(roundingMode: RoundingMode) {
            autoRoundingMode = roundingMode
        }


        //@JvmField
        @get:JvmName("roundingMode")
        public var roundingMode: RoundingMode = autoRoundingMode
            //get() = autoRounding.roundingMode
            private set


        @JvmStatic
        public fun getRoundingMode(): RoundingMode = autoRoundingMode

        /**************************   Parsing a string   **************************/

        private fun convertToNormalizedNumberString(rawNumberString: String, locale: Decimal.Locale): String {
            // in decimal strings, change local decimal separator to '.' and remove grouping separator completely
            var numberString = rawNumberString
            // filter some commonly embedded chars that were inserted for readability
            if (locale.decimalSeparator != '_') numberString = numberString.filterNot { it == '_' }
            if (locale.decimalSeparator != ' ') numberString = numberString.filterNot { it == ' ' }

            val isScientificString = numberString.contains('E', true)
            if ((!isScientificString) || (!(numberString.contains('.')))) {
                // this is either a decimal string, which must always be translated from local to normalized,
                // or a scientific string, which we only can translate if it has no period point.
                // (as scientific strings might not follow the local convention and still use period decimal point!)
                if (locale.groupingSeparator != null) numberString = numberString.filterNot { it == locale.groupingSeparator }
                if (locale.decimalSeparator != '.') numberString = numberString.replace(locale.decimalSeparator, '.')

            }
            return numberString
        }


        internal fun decimalParseOrNull(
            rawNumberString: String,
            roundingMode: Decimal.RoundingMode,
            locale: Decimal.Locale
        ): Long? {
            var numberString = convertToNormalizedNumberString(rawNumberString, locale)

            val decimalNumberPattern =
                """(?<prefix>[+-])?(?<integer>[+-]?\d*)(?:\.(?<fraction>\d*))?(?:[Ee](?<exponent>[+-]?\d+))?"""
            val decimalNumberRegex = Regex(decimalNumberPattern)
            val match = decimalNumberRegex.matchEntire(numberString)
            if (match == null) {
                throwError("\"$rawNumberString\" is not a number", true)
                return null
            }

            val exponent = (match.groups["exponent"]?.value ?: "0").toInt()
            val prefixString = match.groups["prefix"]?.value ?: ""
            val integerString = (match.groups["integer"]?.value ?: "").trimStart('0')
            val fractionString = (match.groups["fraction"]?.value ?: "").trimEnd('0')

            // parse and toLong() - with simple rounding only place 6
            // after that we will always have 5 decimal places.

            var decimalPlaces = fractionString.length
            decimalPlaces -= exponent

            var mantissaString = integerString + fractionString

            var lastDigit: Char = '0'
            var roundOffset: Int = 0

            // remove disposable places or add missing places

            if (decimalPlaces < FIX_DECIMAL_PLACES) {
                mantissaString = mantissaString.padEnd(mantissaString.length + (FIX_DECIMAL_PLACES - decimalPlaces), '0')
            } else if (decimalPlaces > FIX_DECIMAL_PLACES) {
                mantissaString = mantissaString.dropLast((decimalPlaces - FIX_DECIMAL_PLACES) - 1)
                lastDigit = mantissaString.last()
                mantissaString = mantissaString.dropLast(1)
            }
            decimalPlaces = FIX_DECIMAL_PLACES

            // still too long for Long?
            val mantissaLength = mantissaString.length
            if ((mantissaLength > Decimal.MAX_LONG_SIGNIFICANTS)
                || ((mantissaLength == Decimal.MAX_LONG_SIGNIFICANTS)
                        and (mantissaString.compareTo(Decimal.MAX_MANTISSA_AS_STRING) > 0))
            ) {
                throwError("\"$rawNumberString\" cannot fit into a Decimal", true)
                return null
            }

            mantissaString = prefixString + mantissaString
            if (mantissaString in listOf("+", "- ", "")) mantissaString += "0"
            var mantissa: Long = mantissaString.toLong()

            // only simple commercial rounding for now, perhaps consider rounding mode later
            if (lastDigit > '4') {
                roundOffset = if (mantissa < 0) -1 else 1
            }

            return mantissa
        }




        // private var autoFormatString: String = "#,###,###,##0.00"
        // ??? important for India: lakh/crore system? otherwise toFormattedString() is sufficient

        /***************************  Simple output core routine   ***************************/

        internal fun toRawString(mantissa: Long): String { // no local, decimals cut 0!
            if (mantissa == MANTISSA_NAN_VALUE) return DECIMAL_NAN_AS_STRING
            var decimalString: String
            val prefix: String
            when {
                (mantissa < 0) -> {
                    decimalString = (0L - mantissa).toString(10); prefix = "-"
                }

                else -> {
                    decimalString = mantissa.toString(10); prefix = ""
                }
            }

            var missingDecimals = decimalString.count() - FIX_DECIMAL_PLACES
            if (missingDecimals <= 0) { // more than significant digits! prepend zeros!
                decimalString = "0" + "0".repeat(0 - missingDecimals) + decimalString
                missingDecimals = 1
            }
            decimalString = decimalString.take(missingDecimals) + '.' + decimalString.substring(missingDecimals)

            decimalString = decimalString.trimEnd('0').trimEnd('.')


            return prefix + decimalString
        }


        /**************************** Static Error Handling  ********************************/

        // If shallThrowOnError is false, errors are embedded into decimal places instead, while mantissa is 0
        // see this below:

        // better inline for a more clear stack trace?
        @Suppress("NOTHING_TO_INLINE")
        @Throws(NumberFormatException::class, ArithmeticException::class)
        internal inline fun throwError(info: String, isNumberFormatException: Boolean = false): Decimal {
            val errorText = "${if (isNumberFormatException) "NumberFormatException" else "ArithemticException"}: $info"
            if (shallThrowOnError) throw if (isNumberFormatException) NumberFormatException(errorText) else ArithmeticException(
                errorText
            )
            // else: what? how to display an error?
            return Decimal.NaN
        }

    }  // end of the companion object
}





