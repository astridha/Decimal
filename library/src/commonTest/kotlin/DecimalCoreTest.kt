package io.github.astridha.decimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalCoreTest {

    @Test fun intConstructorTests() {
        assertEquals(
            "15",
            Decimal(15L).toString(),
            "intConstructor: 15L"
        )
        assertEquals(
            "16",
            (16).Dc.toString(),
            "Int.Dc Constructor: 16"
        )
        assertEquals(
            "17.5",
            (17.5).Dc.toString(),
            "Double.Dc Constructor: 17.5"
        )
        Decimal.setMinDecimals(2)
        assertEquals(
            "18.5001",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setMinDecimals(0) // default
        assertEquals(
            "18500",
            "18500.000".Dc.toString(),
            "String.Dc Constructor: 18500.000"
        )
        Decimal.setMaxDecimalPlaces(2)
        assertEquals(
            "18.5",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setMaxDecimalPlaces(0)
        assertEquals(
            "19",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setMaxDecimalPlaces(15) // default
    }

    @Test fun doubleConstructorTests() {

        Decimal.setMaxDecimalPlaces(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47).toString(),
            "DoubleConstructor: 100000.47"
        )
        assertEquals(
            "15.00000001",
            Decimal(15.00000001).toString(),
            "DoubleConstructor: 15"
        )
        assertEquals(
            "15.000000000001",
            Decimal(15.000000000001).toString(),
            "DoubleConstructor: 15 (d=12)"
        )
        assertEquals(
            "15.0000000000001",
            Decimal(15.0000000000001).toString(),
            "DoubleConstructor: 15 (d=13)"
        )
        assertEquals(
            "15.00000000000006",
            15.00000000000006.toString(),
            "15 Double (d=14) toString()"
        )
        assertEquals(
            "15.00000000000001",
            Decimal(15.00000000000001).toString(),
            "DoubleConstructor: 15 (d=14)"
        )
        Decimal.setMaxDecimalPlaces(5)
        assertEquals(
            "15.000001",
            15.000001.toString(),
            "15 Double (p=6) toString()"
        )
        assertEquals(
            "15",
            15.000001.Dc.toString(),  // 6 places when precision is 5!
            "15.000001 Double (d=6, p=5) toString()"
        )
        assertEquals(
            "15.00001",
            15.000009.Dc.toString(),  // 6 places when precision is 5!
            "15.000009 Double (d=6, p=5) toString()"
        )
        Decimal.setMaxDecimalPlaces(6)
        assertEquals(
            "15.000001",
            15.000001.Dc.toString(),  // 6 places when precision is 6!
            "15.000001 Double (d=6, p=6) toString()"
        )
        assertEquals(
            "15",
            Decimal(15.000000000000009).toString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=6)"
        )
        Decimal.setMaxDecimalPlaces(15)
        assertEquals(
            "15.000000000000009",
            Decimal(15.000000000000009).toString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=15)"
        )
        assertEquals(
            "15",
            Decimal(15).toString(),
            "intConstructor: 15"
        )
    }

    @Test fun floatConstructorTests() {

        Decimal.setMaxDecimalPlaces(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47F).toString(),
            "floatConstructor: 10000000.47"
        )
        assertEquals(
            "10000.47",
            (10000.47F).toDecimal().toString(),
            "Float.toDecimal(): 10000.47"
        )
        assertEquals(
            "15.3",
            (15.3F).toString(),
            "Float Test 15.3F"
        )
        assertEquals(
            "15.3",
            (15.3F).toDecimal().toString(),
            "float.toDecimal(): 15.3F"
        )

    }

    @Test fun stringConstructorTests() {
        assertEquals(
            null,
            "abc".toDecimalOrNull()?.toString(),
            "string \"abc\".toDecimalOrNull()"
        )
        assertFailsWith(
            NumberFormatException::class,
            "string  \"abc\".toDecimal()",
            {"abc".toDecimal().toString()}
        )
        assertFailsWith(
            NumberFormatException::class,
            "stringConstructor: abc",
            {Decimal("abc").toString()}
        )
         assertEquals(
            "123",
            Decimal("123").toString(),
            "stringConstructor: 123"
        )
        assertEquals(
            "123000",
            Decimal("123000").toString(),
            "stringConstructor: 123000"
        )
        assertEquals(
            "123",
            Decimal("123.000").toString(),
            "stringConstructor: 123.000"
        )
        assertEquals(
            "123.4",
            Decimal("123.4").toString(),
            "stringConstructor: 123.4 (no rounding defined, no decimals defined)"
        )
        assertEquals(
            "-123.004",
            Decimal("-123.004").toString(),
            "stringConstructor: -123.004"
        )
        assertEquals(
            "1.234",
            Decimal("1.234E0").toString(),
            "stringConstructor: 1.234E0"
        )
        assertEquals(
            "123.4",
            Decimal("1.234E2").toString(),
            "stringConstructor: 1.234E2"
        )
        assertEquals(
            "-123.4",
            Decimal("-1.234E2").toString(),
            "stringConstructor: -1.234E2"
        )
        assertEquals(
            "0.01234",
            Decimal("1.234E-2").toString(),
            "stringConstructor: 1.234E-2"
        )
        assertEquals(
            "0.12345678901234",
            Decimal("0.12345678901234").toString(),
            "stringConstructor: \"0.12345678901234\""
        )
        assertEquals(
            "0.123456789012346",
            Decimal("0.1234567890123456").toString(),
            "stringConstructor: \"0.1234567890123456\", with rounding"
        )
        assertEquals(
            "123456.12345678",
            Decimal("123456.1234567800000000").toString(),
            "stringConstructor: \"123456.1234567800000000\", with rounding"
        )
        Decimal.setMaxDecimalPlaces(3)
        assertEquals(
            "123456.123",
            Decimal("123456.1234567890123456").toString(),
            "stringConstructor: \"123456.1234567890123456\", with rounding to 3 dplc"
        )
        Decimal.setMaxDecimalPlaces(15)
        assertFailsWith(
            ArithmeticException::class,
            "stringConstructor: \"123456.1234567890123456\", with rounding to 15 dplc",
            {Decimal("123456.1234567890123456").toString()}
        )
        /* assertEquals(
            "123456.12345678901235",
            Decimal("123456.1234567890123456").toRawDecimalString(),
            "stringConstructor: \"123456.1234567890123456\", with rounding to 15 dplc"
        )
        */
    }

    @Test fun toPlainStringTests() {
        assertEquals(
            "123",
            Decimal(123L, 0, true).toString(),
            "toPlainString: +mantissa 123L, 0 places 0"
        )
        assertEquals(
            "1.24",
            Decimal(124L, 2, true).toString(),
            "toPlainString: +mantissa, 124L +places 2"
        )
        assertEquals(
            "12500",
            Decimal(125L, -2, true).toString(),
            "toPlainString: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-125",
            Decimal(-125L, 0, true).toString(),
            "toPlainString: -mantissa -125L, 0 places"
        )
        assertEquals(
            "-1.25",
            Decimal(-125L, +2, true).toString(),
            "toPlainString: -mantissa -125L, +places +2"
        )
        assertEquals(
            "12500",
            //Decimal(-125L, -2, true).toPlainString(),
            12500F.Dc.toString(),
            "toPlainString: -mantissa -125L, -places -2"
        )
    }

    @Test fun toScientificStringTests() {
        assertEquals(
            "1.23E2",
            Decimal(123L).toScientificString(),
            "toScientific: +mantissa, 0 places"
        )
        assertEquals(
            "1.24E-8",
            Decimal(124L, 10,true).toScientificString(),
            "toScientific: +mantissa, +places"
        )
        assertEquals(
            "1.25E4",
            Decimal(125L, -2, true).toScientificString(),
            "toScientific: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-1.25E2",
            Decimal(-125L).toScientificString(),
            "toScientific: -mantissa, 0places, '1.25E2'"
        )
        assertEquals(
            "-1.25E-8",
            Decimal(-125L, 10, true).toScientificString(),
            "toScientific: -mantissa, +places, '-1.25E-8'"
        )
        assertEquals(
            "-1.25E12",
            Decimal(-125L, -10, true).toScientificString(),
            "toScientific: -mantissa, -places, '1.25E12'"
        )

    }

    @Test fun toFormattedStringTests() {
        assertEquals(
            "1.000",
            Decimal(1L).toFormattedString(thousands = ',', decim = '.', minDecimalPlaces = 3),
            "toFormattedString: decim is fullstop (default)"
        )
        assertEquals(
            "1,000",
            Decimal(1L).toFormattedString(thousands = '.', decim = ',', minDecimalPlaces = 3),
            "toFormattedString: decim is comma"
        )
        assertFailsWith(
            IllegalArgumentException::class,
            "toFormattedString: identical thousands and decim are invalid",
            {Decimal(1L).toFormattedString(thousands = ',', decim = ',', minDecimalPlaces = 3)}
        )
        assertEquals(
            "1.000.000,000",
            Decimal(1000000L).toFormattedString(thousands = '.', decim = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
        assertEquals(
            "1.000.000,000",
            Decimal(1000000L).toFormattedString(thousands = '.', decim = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
        assertEquals(
            "1.234.567,000",
            Decimal(1234567L).toFormattedString(thousands = '.', decim = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
        assertEquals(
            "-1.234.567,000",
            Decimal(-1234567L).toFormattedString(thousands = '.', decim = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
    }
}
