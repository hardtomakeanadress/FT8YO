# FT8YO fixed Android app

Download **FT8YO-0.93-trUSDX-official-USB-fix4-debug.apk** from this directory.

This build enforces the official `(tr)uSDX` USB serial settings: 115200 baud,
8N1, DTR high, RTS low, and the documented `UA2;` speaker-off streaming
command. It also retries stream startup after the USB port becomes ready and
contains the fragmented stream-marker fix. Its Android package name is
`com.bg7yoz.ft8cn.trusdxfix`, so it can be installed alongside the original
FT8CN app.

USB audio over CAT requires `(tr)uSDX` firmware 2.00u or newer.

SHA-256:

```text
bd0b81ef7b8fb3e7e2f80effc837212c8159a406d35b2c961c3abe7eaddc21b9
```
