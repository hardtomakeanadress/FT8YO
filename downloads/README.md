# FT8YO fixed Android app

Download **FT8YO-0.93-trUSDX-continuous-audio-fix6-debug.apk** from this
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

The uploaded fix-5 report proved that USB audio reached the decoder, but the
old two-second `RX`/frequency poll repeatedly interrupted it. Fix 6 removes
that poll so the decoder receives a continuous sample stream.

SHA-256:

```text
ac49664047bd452bf8b72dc3c4e89f32a2700997e3484e66ab8cd5af232cff6d
```
