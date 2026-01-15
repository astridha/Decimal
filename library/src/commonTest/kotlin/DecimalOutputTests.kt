package io.github.astridha.smalldecimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalOutputTests {

    @Test fun toPlainStringTests() {
        assertEquals(
            "123",
            Decimal(123L, 0).toString(),
            "toPlainString: +mantissa 123L, 0 places 0"
        )
        assertEquals(
            "1.24",
            Decimal(124L, 2).toString(),
            "toPlainString: +mantissa, 124L +places 2"
        )
        assertEquals(
            "12500",
            Decimal(125L, -2).toString(),
            "toPlainString: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-125",
            Decimal(-125L, 0).toString(),
            "toPlainString: -mantissa -125L, 0 places"
        )
        assertEquals(
            "-1.25",
            Decimal(-125L, +2).toString(),
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
            Decimal(124L, 10).toScientificString(),
            "toScientific: +mantissa, +places"
        )
        assertEquals(
            "1.25E4",
            Decimal(125L, -2).toScientificString(),
            "toScientific: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-1.25E2",
            Decimal(-125L).toScientificString(),
            "toScientific: -mantissa, 0places, '1.25E2'"
        )
        assertEquals(
            "-1.25E-8",
            Decimal(-125L, 10).toScientificString(),
            "toScientific: -mantissa, +places, '-1.25E-8'"
        )
        assertEquals(
            "-1.25E12",
            Decimal(-125L, -10).toScientificString(),
            "toScientific: -mantissa, -places, '1.25E12'"
        )

    }

    @Test fun toFormattedStringTests() {
        assertEquals(
            "1.000",
            Decimal(1L).toFormattedString(thousandsDelimiter = ',', decimalDelimiter = '.', minDecimalPlaces = 3),
            "toFormattedString: decim is fullstop (default)"
        )
        assertEquals(
            "1,000",
            Decimal(1L).toFormattedString(thousandsDelimiter = '.', decimalDelimiter = ',', minDecimalPlaces = 3),
            "toFormattedString: decim is comma"
        )
        assertFailsWith(
            IllegalArgumentException::class,
            "toFormattedString: identical thousands and decim are invalid",
            {Decimal(1L).toFormattedString(thousandsDelimiter = ',', decimalDelimiter = ',', minDecimalPlaces = 3)}
        )
        assertEquals(
            "1.000.000,000",
            Decimal(1000000L).toFormattedString(thousandsDelimiter = '.', decimalDelimiter = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
        assertEquals(
            "1.000.000,000",
            Decimal(1000000L).toFormattedString(thousandsDelimiter = '.', decimalDelimiter = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
        assertEquals(
            "1.234.567,000",
            Decimal(1234567L).toFormattedString(thousandsDelimiter = '.', decimalDelimiter = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
        assertEquals(
            "-1.234.567,000",
            Decimal(-1234567L).toFormattedString(thousandsDelimiter = '.', decimalDelimiter = ',', minDecimalPlaces = 3),
            "toFormattedString: toFormattedString: thousands is fullstop and decim is comma"
        )
    }


}
