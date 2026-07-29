# FT8YO fixed Android app

Download **FT8YO-0.93-trUSDX-timed-rx-fix11-debug.apk** from this
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
early stop, and uses the working Linux driver's 100 ms drain interval followed
by three back-to-back receive commands.

SHA-256:

```text
8b9d5acb6f5f706b2b271c5e8510ccf91349e31863224e9d381144d23957f236
```
