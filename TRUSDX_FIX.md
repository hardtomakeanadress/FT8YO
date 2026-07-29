# Verified (tr)uSDX USB-audio fix

Status: verified working on hardware on 2026-07-29.

Affected setup:

- FT8CN 0.93 / FT8YO
- `(tr)uSDX` audio over CAT
- Android phone connected by USB OTG
- CH340 USB serial interface (`1A86:7523`)

## The two required fixes

### 1. Fix the fragmented `US` stream marker

The original FT8CN parser assumed that the two-byte `US` audio-stream marker
would arrive in one Android USB callback. USB callback boundaries are
arbitrary, so the radio can deliver `U` in one callback and `S;` in the next.

FT8CN accumulated the command correctly, but then tried to remove two bytes
from only the current one-byte fragment. Java threw
`IllegalArgumentException: 2 > 1`, and FT8CN reported that communication with
the serial port had been lost.

FT8YO Fix 12 uses a persistent parser that keeps incomplete command bytes
between callbacks. The parser waits for the complete `US` marker before
switching to audio mode and never slices the current callback using lengths
from the accumulated command.

Regression tests cover:

- `U` followed by `S;` in separate callbacks
- audio following a fragmented marker
- CAT commands split across callbacks

### 2. Set `(tr)uSDX` Semi-QSK to OFF

For USB audio over CAT, set radio Menu 2.4 **Semi-QSK = OFF**. The official
`(tr)uSDX` USB-audio driver also identifies this as a required radio setting.
With Semi-QSK left ON, CAT control can continue working while the receiver
audio stream fails to restart correctly after transmissions.

After changing Semi-QSK, fully power-cycle the radio before reconnecting the
phone.

## Known-working setup

Configure the radio before connecting FT8YO:

1. Menu 2.4 **Semi-QSK = OFF**
2. Mode **USB**
3. Filter bandwidth **4k0**
4. Firmware **R2.00u or newer**, including R2.00x
5. TX Drive as required for the desired power; TX Drive 4 produced about 3.5 W
   in the verified test

In FT8YO, select the `(tr)uSDX`, USB serial connection, CAT control, and
115200 baud.

## Why Fix 12 returned to baseline

Diagnostic builds 2 through 11 tried serial-control-line overrides, continuous
receive changes, repeated RX recovery commands, transmit locking, and added
TX-to-RX timing. The uploaded traces showed that CAT remained alive and that
the radio received those recovery commands, but they did not restore RX audio
after TX.

Those experiments also changed behavior outside the original `2 > 1` parser
bug. Fix 12 removes them. Its radio-control path is the known-working upstream
FT8CN 0.93 implementation with only the persistent stream parser added.

## Verified build

[Download FT8YO 0.93 baseline parser Fix 12](https://raw.githubusercontent.com/hardtomakeanadress/FT8YO/main/downloads/FT8YO-0.93-trUSDX-baseline-parser-fix12-debug.apk)

- Android package: `com.bg7yoz.ft8cn.trusdxfix`
- Version: `0.93-trusdx-baseline-parser-fix12`
- SHA-256:
  `fa0b33e36361b67588fcdb60071c3bce08278909764ca70d3d7c2a51274eda73`
- Fix commit: `3cffe7e6ccf99f517c3279dbac94ca73495f0932`

Related reports:

- [FT8YO issue #1](https://github.com/hardtomakeanadress/FT8YO/issues/1)
- [Upstream FT8CN issue #123](https://github.com/N0BOY/FT8CN/issues/123)
- [Official `(tr)uSDX` protocol details](https://dl2man.de/5-trusdx-details/)
- [Official `(tr)uSDX` USB-audio driver](https://dl2man.de/wp-content/uploads/2022/01/wp.php/trusdx-audio.zip)
