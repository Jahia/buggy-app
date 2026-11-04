# buggy-app

A Jahia module that **deliberately injects performance faults** into a running Jahia so that support engineers
can practise diagnosing them (thread dumps, GC logs, heap dumps, Native Memory Tracking, `top -H`, …).
It is adapted from [ycrash buggyapp](https://github.com/ycrash/buggyapp).

> ⚠️ **Never install this module on a production or shared environment.** Every command refuses to run when Jahia
> is in production operating mode unless `--force` is given. Some scenarios (`dead-lock`, `memory-leak`) can only be
> recovered by restarting Jahia.

## Requirements

- Jahia 8.2.3.2 or later (Java 11 or 17)
- Access to the Karaf shell (`ssh -p 8101 jahia@localhost` or the Tools console)

## Build

```bash
mvn clean install
```

Deploy `target/buggy-app-<version>.jar` like any other Jahia module.

## Commands

All commands are in the `buggy-app` scope. Durations are in milliseconds; sizes are in bytes. Each command accepts
`-f` / `--force` to run even when Jahia is in production mode.

Blocking commands keep the shell busy for the whole duration. Pressing **Ctrl-C stops the scenario cleanly**.

| Command | Options (default) | What it does | Blocking | Recovers on its own |
|---|---|---|---|---|
| `buggy-app:cpu-spike` | `-d` (60000), `-n` threads (1) | Busy loops in `n` threads named `buggy-app-cpu-spike-*` | yes | yes |
| `buggy-app:heavy-io` | `-d` (60000), `-n` threads (1) | Each thread writes and reads its own temp file in a loop; files are deleted at the end | yes | yes |
| `buggy-app:memory-leak` | — | Fills a map on the shell thread until `OutOfMemoryError` | until OOM | the map becomes collectable after the OOM |
| `buggy-app:memory-leak-without-crash` | `-d` (60000) | Fills the heap up to ~99 % of `-Xmx` over the duration, holds it, then releases it | yes | yes |
| `buggy-app:metaspace-leak` | `-d` (60000) | Generates new classes with Javassist | yes | metaspace is released on module restart/refresh |
| `buggy-app:native-memory-leak` | `-d` (60000), `-s` step (60 MB), `-t` total (500 MB) | Allocates off-heap memory in `-s` steps up to `-t` over the duration, then frees it | no (background) | yes |
| `buggy-app:thread-leak` | `-d` (60000) | Starts 50 async HTTP clients, each with its own I/O dispatcher threads (one per core) | yes | yes |
| `buggy-app:dead-lock` | — | Two threads (`buggy-app-deadlock-A/B`) lock two monitors in opposite order | no | **no — restart Jahia** |

## What to look at

| Scenario | Where it shows |
|---|---|
| CPU spike | `top -H -p <pid>`, then match the native thread id (`nid`) in a thread dump |
| Heavy I/O | `iostat`, `iotop`; threads in `FileDispatcherImpl` / `Files.write` frames |
| Heap leaks | GC logs, `jcmd <pid> GC.class_histogram`, heap dump (`HashMap` retained by the leak thread) |
| Metaspace leak | `jcmd <pid> VM.metaspace`, `jcmd <pid> VM.classloader_stats`; thousands of `MetaspaceObject*` classes |
| Native memory leak | RSS growing while the heap is flat; with `-XX:NativeMemoryTracking=summary`, `jcmd <pid> VM.native_memory summary` (category *Other*) |
| Thread leak | Thread count growing in thread dumps (`I/O dispatcher` threads) |
| Deadlock | Thread dump section *Found one Java-level deadlock* |

## Tests

```bash
mvn test
```

The unit tests cover the harness: option validation, stopping on Ctrl-C (interruption), re-running scenarios,
clean-up of files, threads and native memory, and the production-mode guard. A JaCoCo report is generated in
`target/site/jacoco`.
