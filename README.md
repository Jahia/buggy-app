# buggy-app

A Jahia module that **deliberately injects performance faults** into a running Jahia so that support engineers
can practise diagnosing them (thread dumps, GC logs, heap dumps, `top -H`, `pmap`, …).
It is adapted from [ycrash buggyapp](https://github.com/ycrash/buggyapp).

The training exercise built on this module is described in Confluence:
[Performance analysis tips - Ref ISREC08.A12855](https://jahia-confluence.atlassian.net/wiki/spaces/SUP/pages/468444/Performance+analysis+tips+-+Ref+ISREC08.A12855).

> ⚠️ **Never install this module on a production or shared environment.** Commands log a warning when Jahia runs in
> production operating mode, but still run (the Jahia Cloud training environment is in production mode). Some
> scenarios (`dead-lock`, `memory-leak`) can only be recovered by restarting Jahia.

## Requirements

- Jahia 8.2.3.2 or later (Java 11 or 17)
- Access to the Karaf console (`/modules/tools/karaf.jsp`, or `ssh -p 8101`)
- No JVM or OSGi configuration is needed: the former `org.osgi.framework.bootdelegation` addition of
  `jdk.internal.ref` for `native-memory-leak` is no longer required.

## Build

```bash
mvn clean install
```

Deploy `target/buggy-app-<version>.jar` like any other Jahia module.

## Commands

All commands are in the `buggy-app` scope. Durations are in milliseconds; sizes are in bytes.

Blocking commands keep the console busy for the whole duration. In an SSH Karaf shell, **Ctrl-C stops the
scenario cleanly**.

Behaviour of the other ways to run a command (measured on Jahia 8.2.3.2):

- **`/modules/tools/karaf.jsp`**: the page stops waiting after about 10 seconds (`Shell command has timed out` in
  the log), but the command **keeps running** until the end of its duration. A long scenario such as
  `buggy-app:cpu-spike -d 300000` therefore works; the page just returns before it is over.
- **Provisioning API (`karafCommand`)**: the command is interrupted after about 1 second, which stops the scenario
  (as Ctrl-C would). Use it only for the background scenarios (`native-memory-leak`, `background-noise`) or
  short checks.
- With `memory-leak`, the JVM option `-XX:+HeapDumpOnOutOfMemoryError` of the Jahia images writes the heap dump in
  the working directory of the Tomcat process (`/tmp/java_pid<PID>.hprof` in the Docker images).

| Command | Options (default) | What it does | Blocking | Recovers on its own |
|---|---|---|---|---|
| `buggy-app:cpu-spike` | `-d` (60000), `-n` threads (1) | Busy loops in `n` threads | yes | yes |
| `buggy-app:heavy-io` | `-d` (60000), `-n` threads (1) | Each thread writes and reads its own temp file in a loop; files are deleted at the end; a worker stops at its first I/O error | yes | yes |
| `buggy-app:memory-leak` | — | Fills a map on the console thread until `OutOfMemoryError` (or Ctrl-C) | until OOM | the map becomes collectable after the OOM |
| `buggy-app:memory-leak-without-crash` | `-d` (60000) | Fills the heap up to ~99 % of `-Xmx` over the duration, holds it, then releases it | yes | yes |
| `buggy-app:metaspace-leak` | `-d` (60000) | Generates new classes with Javassist | yes | metaspace is released on module restart/refresh |
| `buggy-app:native-memory-leak` | `-d` (60000), `-s` step (60 MB), `-t` total (500 MB), `--stop` | Memory-maps and loads a temp file in `-s` steps up to `-t` over the duration, then unmaps it and deletes the file. One leak at a time; `--stop` ends it early | no (background) | yes |
| `buggy-app:thread-leak` | `-d` (60000) | Starts 50 async HTTP clients, each with its own `I/O dispatcher` threads (one per core) | yes | yes |
| `buggy-app:dead-lock` | — | Two threads lock two class monitors in opposite order | no | **no — restart Jahia** |
| `buggy-app:blocked-app` | `-d` (60000), `-n` threads (10) | One thread holds a lock for the whole duration; the `n-1` others are `BLOCKED` waiting for it | yes | yes |
| `buggy-app:background-noise` | `-d` (600000), `-p` producers (5), `-c` clear interval (10000), `--stop` | Healthy but busy activity: producer threads allocate ~1 KB objects continuously, a consumer releases them every `-c` ms (heap sawtooth, steady GC). Start it before another scenario so the problem is not alone on an idle JVM. One instance at a time | no (background) | yes |
| `buggy-app:db-connection-leak` | `-d` (60000), `-n` connections (0 = whole pool) | Borrows connections from Jahia's database pool without returning them: the target is reached at half of the duration, then held. Once the pool is exhausted, Jahia requests needing the database wait and fail with *Timeout: Pool empty* | yes | yes: every connection is returned at the end |

Worker threads intentionally keep the JVM default names (`Thread-N`): the exercise asks trainees to find the culprit
from `top -H` / `iotop` and match the native thread id in a thread dump.

## Tests

```bash
mvn test
```

The unit tests cover the harness: option validation, stopping on Ctrl-C (interruption), re-running scenarios, and
clean-up of files, threads and mapped memory. A JaCoCo report is generated in `target/site/jacoco`.
