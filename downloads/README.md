# FT8YO fixed Android app

Download **FT8YO-0.93-trUSDX-baseline-parser-fix12-debug.apk** from this
directory.

This build uses the known-working upstream FT8CN 0.93 radio path with only the
fragmented `US` stream-marker parser fixed. The experimental serial overrides,
receive retries, TX locks, and timing changes from Fixes 2–11 have been removed.
Its Android package name is `com.bg7yoz.ft8cn.trusdxfix`, so it can be installed
alongside the original FT8CN app.

Set `(tr)uSDX` Menu 2.4 **Semi-QSK = OFF**, Mode **USB**, Filter BW **4k0**,
then fully power-cycle the radio before connecting the phone. Use firmware
R2.00x or newer.

SHA-256:

```text
fa0b33e36361b67588fcdb60071c3bce08278909764ca70d3d7c2a51274eda73
```
