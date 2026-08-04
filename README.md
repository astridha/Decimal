# A Small Fixed-Comma Decimal Type for KMP Multiplatform

This platform-independent library offers a fixed-size **Decimal** class with 5 decimal places and a predictive footprint of 64 Bit.  

Made for Kotlin Multiplatform.

The **Decimal** class implements Number and Comparable interfaces, with a 64-Bit footprint.   
It supports math binary operators **+**, **-**, <b>*</b>, **/**, and **%**, as well as unary operators **+**, **-**, **++**, and **--**.  
Comparing like **<**, **>**, **<=**, **>=**, or **==** is also supported.


## Characteristics   

The footprint of a **Decimal** instance corresponds to the 64 bit size of Long or Double data types.

Its representable value ranges from -92_233_720_368_547.75808 to +92_233_720_368_547.75807 .  

So, representable values of 92 trillions with 5 decimal places are supported.

These characteristics are probably sufficient for most day-to-day requirements.


### Convenient usage

#### No verbose type or class declaration

The Decimal type can be used much idiomatically like any other numeric type, just with the extension *".Dc"*.  
Simply use this like *5.Dc* or *17.48.Dc*.  
Or use a numeric String constructor like *"1228573.68".Dc* or *"12_28_573.68".Dc*.  
The latter avoids the Float/Double inaccuracy problems which might arise with big numbers.

#### Common Arithmetical Operators are working

Use arithmetical operators conveniently, like  
*(7.5.Dc + 8.5.Dc) / 3.Dc*

## How to start

### Configure before use
Before using, initialize the standard output format as well as the automatic rounding behavior.
``` kotlin
Decimal.initLocale(Decimal.Locale(null, '.', 2)) 

Decimal.initRounding(Decimal.RoundingMode.HALF_UP)
```
This example will configure the rounding mode to automatic commercial rounding.   
Moreover, using toString() or in String interpolation, all Decimals will be printed without a thousands separator, the decimal separator is a dot, and 
the fractional part is shown with at least two decimal places.

### The automatic local formatting for input and output
When parsing or printing the **Decimal** type with ``` .toString()``` , a local formatting can automatically be applied.

It describes an optional grouping (i.e., thousands) separator, the decimal separator
and the minimum number of decimal places that will be printed.
A data class describing the local formatting can be used for configuring this.  
``` kotlin
public data class Decimal.Locale(groupSeparator: Char?,ar, minDecimalPlaces: Int)
```  
Configuring is done with a setLocale() call:  
``` Decimal.initLocale (locale: Decimal.Locale) ```   

---


### Setting local Decimal formatting

#### initLocale (groupingSeparator: Char?, decimalSeparator: Char, minDecimalPlaces: Int)
Configures how the Decimal will be formatted to with  **toString()**.
Sets grouping separator and decimal separator, and the number of minimum decimal places.  
The supported range is from zero to any positive value.   
0 is the default value and means there are no printed mandatory decimal places.  
If this setting sets more decimal places than the Decimal value has, the remaining decimal places are filled with "0"s.  
If there are more decimal places than ```minDecimalPlaces```, they are all shown
``` kotlin initLocale (Decimal.Locale(',', '.', 2))```  means that the grouping separator is a comma, decimal point is a dot, and at least two decimal places will be shown.  
``` kotlin initLocale (Decimal.Locale(null, '.', 0))```  means that no grouping separator is used, the decimal point is a comma, and only non-zero decimal places will be shown.

#### Applying formatting explicitly

A differently formatted string can be generated with  
``` kotlin Decimal.Format (Decimal.Locale(',', '.', 2)): String```  
like   
``` kotlin mystring = myvalue.Format (Decimal.Locale('\'', '.', 2))``` 



### Rounding

#### The automatic rounding
This is used when getting back to 5 decimal places after multiplication or division.

A decision must be made which rounding method shall be applied for this purpose.

Configuring is done with a initRounding() call:  
``` Decimal.initRounding (rounding: Decimal.RoundingMode) ```   
See below for details.

#### Rounding Modes
There are the same eight Rounding modes as in BigDecimal:

``` kotlin
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
``` 

The automatic rounding mode, when unchanged, is *HALF_UP* (commercial rounding).


### Applying rounding explicitly

The rounding modes can be explicitly used in scale():
``` kotlin
Decimal.scale(desiredprecision: Int, roundingMode: RoundingMode): Decimal
```

Furthermore, there are four standard rounding functions that round to whole values (no decimal places):
``` kotlin
trunc(): Decimal  // RoundingMode.DOWN

floor(): Decimal  // RoundingMode.FLOOR

ceil(): Decimal  // RoundingMode.CEILING

round(): Decimal  // RoundingMode.HALF_EVEN
``` 

These functions are also usable with desired precision other than 0 (whole values):
``` kotlin
trunc(desiredprecision: Int): Decimal  // RoundingMode.DOWN

floor(desiredprecision: Int): Decimal  // RoundingMode.FLOOR

ceil(desiredprecision: Int): Decimal  // RoundingMode.CEILING

round(desiredprecision: Int): Decimal  // RoundingMode.HALF_EVEN
``` 



--------

### Usage (not yet active!)

Dependencies in build.gradle.kts:
``` kotlin
dependencies {
    // ...
    implementation("io.github.astridha:fix5decimal:0.8.5")
}
```

Import in source files:
``` kotlin
import io.github.astridha.fix5decimal.*
```

-----

### setThrowOnErrors(Boolean)
configures whether ArithmeticExceptions are thrown when NaN, overflows etc. occur.
