# Wagyourtail Commons

This repository contains common files for the Wagyourtail's projects. including many useful utilities.

maven: `https://maven.wagyourtail.xyz/releases` or `https://maven.wagyourtail.xyz/snapshots`

## Components

### Java

#### Commons

`xyz.wagyourtail:commons:${version}`

This contains many java helper functions

#### Commons-Core

`xyz.wagyourtail:commons-core:${version}`

This contains java7 helper functions that are also used in `commons` and `commons-asm`

### Commons-ASM

`xyz.wagyourtail:commons-asm:${version}`

This contains asm helper functions. including a `ClassWriter` implementation that doesn't load classes.

#### Commons-Fs

`xyz.wagyourtail:commons-fs:${version}`

This contains a virtual file system implementation

#### Commons-Java7

`xyz.wagyourtail:commons-java7:${version}`

This contains many java7 helper functions

### Kotlin

#### Commons-Kt

`xyz.wagyourtail:commons-kt:${version}`

This contains many kotlin helper functions, plus:

- structured data readers/writers: `CharReader`, `ByteReader` (endian-aware
  takes, `copy(limit)` windows, bounded mark/reset on streams), `ByteWriter`,
  okio `BufferedSource`/`BufferedSink` adapters
- `commonskt.data` — the `StructuredData`/`Content` model (lazy zero-copy view
  nodes, parsed nodes, `BuildStringVisitor`/`BuildBytesVisitor`)
- `commonskt.parsers` — structured parsers built on the readers (constants,
  Json5, SemVer + ranges) — formerly `commons-parsers-kt`
- `commonskt.compress` helpers (JVM) — resolve when
  `org.apache.commons:commons-compress` is on the classpath — formerly
  `commons-compress-kt`
- `commons.kvision` components/state/vars (JS) — resolve when `io.kvision`
  is on the classpath — formerly `commons-kvision-kt`

Optional integrations above are `compileOnly`: they compile in for every
target, and consumers only need the dependency if they call those APIs.

#### Commons-Parsers

`xyz.wagyourtail:commons-parsers:${version}`

structured string parsers in Java (constants, Json5, SemVer + ranges) on
`CharReader`, migrated onto the `commons-core` `StructuredData` model

### Gradle

#### Commons-Gradle

`xyz.wagyourtail:commons-gradle:${version}`

This contains many gradle helper functions

* including a minimal implementation of ShadowJar
