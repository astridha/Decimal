package io.github.astridha.smalldecimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ArithmeticAssignOperatorsTest {

    @Test fun opAssignAssignTests() {
        var d0: Decimal
        var d1: Decimal
        var d2: Decimal
        d0 = 3.Dc
        d1 = 12.Dc
        d2 = d1
        d1 += 3
        assertEquals(
            "3 15 12",
            d0.toString()+" "+d1.toString()+" "+d2.toString(),
            "operator = (d2 shouldn't change when d1 is changed)"
        )

        assertEquals(  // this is not Overflow!
            "${Decimal.MAX_MANTISSA_VALUE}",
            (Decimal.MAX_MANTISSA_VALUE.Dc).toString(),
            "plain (${Decimal.MAX_MANTISSA_VALUE.Dc})"
        )
        assertFailsWith(  // this is Overflow and must trow!
            ArithmeticException::class,
            "plain (${(Decimal.MAX_MANTISSA_VALUE+1)}.Dc)",
            {(Decimal.MAX_MANTISSA_VALUE+1).Dc}
        )
        assertEquals(  // this is not Overflow!
            "${Decimal.MAX_MANTISSA_VALUE}",
            "${Decimal.MAX_MANTISSA_VALUE}".Dc.toString(),
            """"plain (${"${Decimal.MAX_MANTISSA_VALUE}".Dc})"""
        )
        assertFailsWith(  // this is Overflow and must trow!
            ArithmeticException::class,
            "plain (${(Decimal.MAX_MANTISSA_VALUE+1)}.Dc)",
            {"${(Decimal.MAX_MANTISSA_VALUE+1)}".Dc}
        )

    }

    @Test fun opPlusAssignTests() {
        var d: Decimal
        d=12.Dc
        d += 3.Dc
        assertEquals(
            "15",
            d.toString(),
            "operator (12.Dc += 3.Dc)"
        )
        d=12.Dc
        d += 3
        assertEquals(
            "15",
            d.toString(),
            "operator (12.Dc += 3)"
        )
        d=12.468.Dc
        d += 3
        assertEquals(
            "15.468",
            d.toString(),
            "operator (12.468Dc += 3)"
        )
        d=12.468.Dc
        d += 3.1111
        assertEquals(
            "15.5791",
            d.toString(),
            "operator (12.468Dc += 3.1111)"
        )
        assertEquals(
            "0.3",
            (0.1.Dc + 0.1.Dc + 0.1.Dc).toString(),
            "operator ((0.1.Dc + 0.1.Dc + 0.1.Dc) = 0.3)"
        )
        assertEquals(
            "0.001",
            (0.1.Dc * 0.1.Dc * 0.1.Dc).toString(),
            "operator ((0.1.Dc + 0.1.Dc + 0.1.Dc) = 0.3)"
        )
    }

    @Test
    fun opMinusAssignTests() {
        var d: Decimal
        d=12.Dc
        d -= 3.Dc
        assertEquals(
            "9",
            d.toString(),
            "operator (12.Dc -= 3.Dc)"
        )
        d=12.Dc
        d -= 3
        assertEquals(
            "9",
            d.toString(),
            "operator (12.Dc -= 3)"
        )
    }


    @Test
    fun opTimesAssignTests() {
        var d: Decimal
        d=12.Dc
        d *= 3.Dc
        assertEquals(
            "36",
            d.toString(),
            "operator (12.Dc *= 3.Dc)"
        )
        d=12.Dc
        d *= 3
        assertEquals(
            "36",
            d.toString(),
            "operator (12.Dc *= 3)"
        )
    }

    @Test fun opDivAssignTests() {
        var d: Decimal
        d=12.Dc
        d /= 3.Dc
        assertEquals(
            "4",
            d.toString(),
            "operator (12.Dc /= 3.Dc)"
        )
        d=12.Dc
        d /= 3
        assertEquals(
            "4",
            d.toString(),
            "operator (12.Dc /= 3)"
        )
        d=1.Dc
        d /= 3
        assertEquals(
            "0.333333333333333",
            d.toString(),
            "operator (1.Dc /= 3)"
        )
        d=2.Dc
        d /= 3
        assertEquals(
            "0.666666666666667",
            d.toString(),
            "operator (2.Dc /= 3)"
        )
    }


    @Test fun opRemAssignTests() {
        var d: Decimal
        d=12.Dc
        d %= 3.Dc
        assertEquals(
            "0",
            d.toString(),
            "operator (12.Dc %= 3)"
        )
        d=12.Dc
        d %= 3
        assertEquals(
            "0",
            d.toString(),
            "operator (12.Dc %= 3)"
        )
    }

    @Test fun opNumberAssignTests() {
        var l:Long
        var i:Int
        var c:Short

        assertEquals(
            "3",
            3.Dc.toLong().toString(),
            "3.Dc.toLong().toString()"
        )
        assertEquals(
            "3",
            3.3.Dc.toLong().toString(),
            "3.3.Dc.toLong().toString() must be 3"
        )
        assertEquals(
            "-33",
            (-33.9).Dc.toLong().toString(),
            "-33.9.Dc.toLong().toString() must be -33"
        )
        assertEquals(
            "-34",
            (-33.9).Dc.toLong(Decimal.RoundingMode.HALF_UP).toString(),
            "-33.9.Dc.toLong().toString() must be -33"
        )
        assertEquals(
            -34,
            (-33.9).Dc.toLong(Decimal.RoundingMode.HALF_UP),
            "-33.9.Dc.toLong().toString() must be -33"
        )
        assertEquals(
            (-34).toUShort(),
            (-33.9).Dc.toUShort(Decimal.RoundingMode.HALF_UP),
            "-33.9.Dc.toUShort().toString() must be -33"
        )

    }
}