---
title: Java Version
layout: docs
category: troubleshooting
applicable_to: 1.x,2.x,3.x
---

As Beacon evolved, so has its Java runtime requirements. This table shows the respective minimum
and (potentially) maximum supported runtime versions for each respective application release:

| Beacon Version                | Minimum Runtime | Maximum Runtime | Comments                                            |
|:-----------------------------:|:---------------:|:---------------:|----------------------------------------------------:|
| 3.0.0-alpha.5                 | 17 (LTS)        |                 |                                                     |
| 3.0.0-alpha.4                 | 16              |                 |                                                     |
| 3.0.0-alpha.1 - 3.0.0-alpha.3 | 11 (LTS)        | 14              | OpenJFX crashes on newer runtime versions           |
| 2.4.0                         | 9               | 14              | OpenJFX crashes on newer runtime versions           |
| 1.x                           | 8               | 8               | Potentially incompatible with modern Java versions  |
{: .ui .celled .table }

Please note, that the only currently supported release of the JDK is [Adoptium]. Other runtimes
may also work but have not been validated.

[Adoptium]: https://adoptium.net/?variant=openjdk17
