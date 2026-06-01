# VectorServer

The **relay** of the [Distributed Chess System](../). Serves chess **move vectors** over the
network to the board — currently **dummy placeholders** while the SOM pipeline (test3 → test4)
is wired in. The architecture is in place; the intelligence is incoming.

## What it does

A small set of Java socket roles:

- `vec.VectorServer` — the hub; serves vectors over a `ServerSocket` (**port 12345**).
- `vec.VectorClientServer` — combined client/server bridge (ports **12345 → 5010**).
- `vec.VectorClient` — connects to the hub (**port 5010**) and consumes vectors.
- `vec.VectorClientServerDummy` — serves **placeholder** vectors so the whole distributed loop
  runs end-to-end before the real source exists.
- `vec.DummyTileListener` / `vec.DummyTurnListener` — stand-ins for the board's feedback
  channels (**ports 5022 / 5023**).

Swap the dummy source for the test3 → test4 output and the same wiring carries real moves.

## Dependencies

- **JDK 21**, **Maven**. Standard library + sockets.
- `pom.xml` references sibling modules (**BoardTemplate**, **test4**, **SOM**) — local Maven
  artifacts it integrates with. If a clean build can't resolve them, `mvn install` those
  siblings first.

## Build & run

```bash
mvn compile

# the server (dummy vectors)
java -cp target/classes vec.VectorServer

# a consumer, in another terminal
java -cp target/classes vec.VectorClient

# or the all-in-one dummy hub:
java -cp target/classes vec.VectorClientServerDummy
```

## Structure

```
VectorServer/
├── pom.xml                          JDK 21; references BoardTemplate / test4 / SOM
└── src/main/java/vec/
    ├── VectorServer.java            hub server (port 12345)            [main]
    ├── VectorClientServer.java      client/server bridge (12345/5010)  [main]
    ├── VectorClient.java            consumer (port 5010)               [main]
    ├── VectorClientServerDummy.java placeholder hub                    [main]
    ├── DummyTileListener.java       board tile feedback stand-in (5022)
    └── DummyTurnListener.java       board turn feedback stand-in (5023)
```

---

*Part of the Distributed Chess System: it carries the move from the SOM layers to the board.*
