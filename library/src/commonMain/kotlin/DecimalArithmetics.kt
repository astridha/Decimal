package io.github.astridha.smalldecimal

import io.github.astridha.smalldecimal.Decimal.Companion.MAX_MANTISSA_VALUE
import io.github.astridha.smalldecimal.Decimal.Companion.generateErrorDecimal
import io.github.astridha.smalldecimal.Decimal.Companion.isError
import kotlin.math.abs
import kotlin.math.sign

internal class DecimalArithmetics {

    public companion object {

        internal data class EqualizedDecimals(val thisMantissa: Long, val thatMantissa: Long, val commonDecimal: Int)

        private fun equalizeDecimals(thisM: Long, thisD: Int, thatM: Long, thatD: Int): EqualizedDecimals {
            // aligns both mantissas to a common decimal for further processing
            // error handling still missing!
            var thisMantissa = thisM
            var thisDecimals = thisD
            var thatMantissa = thatM
            var thatDecimals = thatD

            // error handling still missing!
            while (thisDecimals < thatDecimals) {
                thisMantissa *= 10
                thisDecimals++
            }
            while (thatDecimals < thisDecimals) {
                thatMantissa *= 10
                thatDecimals++
            }

            return EqualizedDecimals(thisMantissa, thatMantissa, thisDecimals)
        }


        private fun Long.isNegative() = (this.sign < 0)
        private fun Long.isPositive() = (this.sign >= 0)

        /***** operator plus (+) *****/

        internal fun arithmeticPlus(
            thisD: Decimal,
            other: Decimal,
            roundedDecimalPlaces: Int,
            roundingMode: Decimal.RoundingMode
        ): Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            val (thisMantissa, thisDecimals) = thisD.unpack64()
            if (thisMantissa == 0L) return other.clone()
            val (otherMantissa, otherDecimals) = other.unpack64()
            if (otherMantissa == 0L) return thisD.clone()
            val (equalizedThisMantissa, equalizedOtherMantissa, equalizedDecimals) = equalizeDecimals(
                thisMantissa,
                thisDecimals,
                otherMantissa,
                otherDecimals
            )
            println("Addition: this: $equalizedThisMantissa other: $equalizedOtherMantissa, sum: ${equalizedThisMantissa + equalizedOtherMantissa}")
            if (equalizedThisMantissa.isNegative() == equalizedOtherMantissa.isNegative()) {
                // addition might overflow!
                val space: Long = MAX_MANTISSA_VALUE - abs(equalizedThisMantissa)
                if (space <= abs(equalizedOtherMantissa)) {
                    return generateErrorDecimal(
                        Decimal.Error.ADD_OVERFLOW,
                        "$this + $other result does not fit into Decimal"
                    )
                }
            }
            val equalizedMantissaSum = equalizedThisMantissa + equalizedOtherMantissa
            val (roundedMantissa, roundedDecimals) = roundWithMode(
                equalizedMantissaSum,
                equalizedDecimals,
                roundedDecimalPlaces,
                roundingMode
            )
            return Decimal(roundedMantissa, roundedDecimals)
        }



        internal fun arithmeticMinus(
            thisD: Decimal,
            other: Decimal,
            roundedDecimalPlaces: Int,
            roundingMode: Decimal.RoundingMode
        ) : Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            val (thisMantissa, thisDecimals) = thisD.unpack64()
            if (thisMantissa == 0L) return other.clone()
            val (otherMantissa, otherDecimals) = other.unpack64()
            if (otherMantissa == 0L) return thisD.clone()
            val (equalizedThisMantissa, equalizedOtherMantissa, equalizedDecimals) = equalizeDecimals(
                thisMantissa,
                thisDecimals,
                otherMantissa,
                otherDecimals
            )
            println("Subtraction: this: $equalizedThisMantissa other: $equalizedOtherMantissa, diff: ${equalizedThisMantissa - equalizedOtherMantissa}")
            if (equalizedThisMantissa.isNegative() != equalizedOtherMantissa.isNegative()) {
                // subtraction is addition and might overflow!
                val space: Long = MAX_MANTISSA_VALUE - abs(equalizedThisMantissa)
                if (space <= abs(equalizedOtherMantissa)) {
                    return generateErrorDecimal(Decimal.Error.SUBTRACT_OVERFLOW, "$this - $other result does not fit into Decimal")
                }
            }
            val equalizedMantissaSum = equalizedThisMantissa - equalizedOtherMantissa
            val (roundedMantissa, roundedDecimals) = roundWithMode(
                equalizedMantissaSum,
                equalizedDecimals,
                roundedDecimalPlaces,
                roundingMode
            )
            return Decimal(roundedMantissa, roundedDecimals)
        }



    }
}