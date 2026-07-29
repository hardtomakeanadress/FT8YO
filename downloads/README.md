# FT8YO fixed Android app

Download **FT8YO-0.93-trUSDX-rx-recovery-fix9-debug.apk** from this
directory.

This build enforces the official `(tr)uSDX` USB serial settings: 115200 baud,
8N1, DTR high, RTS low, and the documented `UA2;` speaker-off streaming
command. It also retries stream startup after the USB port becomes ready and
contains the fragmented stream-marker fix. Its Android package name is
`com.bg7yoz.ft8cn.trusdxfix`, so it can be installed alongside the original
FT8CN app.

USB audio over CAT requires `(tr)uSDX` firmware 2.00u or newer.

The app now creates a timestamped USB/CAT trace and automatically categorizes
where receive failed. After leaving receive running for at least 15 seconds,
open **Settings** and tap **Share USB diagnostic** to share the generated text
report.

The uploaded reports proved that USB audio reaches the decoder. This build
keeps continuous receive, prevents transmit audio from continuing after an
early stop, and fully resets the radio's receive streamer after each TX.

SHA-256:

```text
f1a930b598733815849ffa8e9faadfc86ea93c11feb5333401523b673dbf1ba0
```
