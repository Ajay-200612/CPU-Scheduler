# CPU Process Scheduling Simulator

A console-based Java application that simulates five classic CPU scheduling algorithms. You enter a set of processes, pick an algorithm, and the simulator runs the full schedule — drawing a Gantt chart and computing turnaround time, waiting time, response time, CPU utilization, and throughput for every process. A built-in comparison mode runs all five algorithms on the same input and highlights the winner in each metric column.

---

## Table of Contents

- [Features](#features)
- [Algorithms](#algorithms)
- [Metrics Computed](#metrics-computed)
- [Project Structure](#project-structure)
- [How to Compile and Run](#how-to-compile-and-run)
- [Usage Guide](#usage-guide)
- [Sample Output](#sample-output)
- [Design Notes](#design-notes)
- [Requirements](#requirements)

---

## Features

- **5 scheduling algorithms** in a single application
- **Gantt chart** rendered in the terminal with proportional segment widths and a live timeline
- **Per-process table** showing all seven metrics side by side
- **Summary statistics** block after every run (avg TAT, avg WT, avg RT, CPU utilization, throughput)
- **Compare All mode** — runs every algorithm on the same input and marks the best value in each column with ◄
- **Input validation** — rejects non-numeric input, negative burst times, and zero burst times with a clear re-prompt
- **Idle CPU detection** — gaps between process arrivals are shown as IDLE segments in the Gantt chart
- **Consistent tie-breaking** — all algorithms break ties by arrival time, then by process ID

---

## Algorithms

| # | Algorithm | Type | Key Characteristic |
|---|-----------|------|--------------------|
| 1 | **FCFS** — First Come First Serve | Non-Preemptive | Processes run in order of arrival; simple but prone to convoy effect |
| 2 | **SJF** — Shortest Job First | Non-Preemptive | Always picks the shortest available job; optimal average wait time for NP algorithms |
| 3 | **SRTF** — Shortest Remaining Time First | Preemptive | Preempts current process if a shorter one arrives; globally optimal average wait time |
| 4 | **Round Robin** | Preemptive | Each process gets a fixed time quantum; fair and good for interactive systems |
| 5 | **Priority** — Non-Preemptive | Non-Preemptive | Runs the highest-priority ready process; lower number = higher priority |

---

## Metrics Computed

| Metric | Formula | Description |
|--------|---------|-------------|
| **CT** — Completion Time | — | Clock time when the process finishes |
| **TAT** — Turnaround Time | CT − Arrival Time | Total time in the system from submission to finish |
| **WT** — Waiting Time | TAT − Burst Time | Time spent waiting in the ready queue |
| **RT** — Response Time | First CPU − Arrival Time | Time until the process first gets the CPU |
| **CPU Utilization** | (Busy Time / Total Time) × 100 | Percentage of time the CPU was running a process |
| **Throughput** | Processes / Total Time | How many processes finish per unit of time |

---

## Project Structure

```
cpu-scheduler/
│
├── src/                            # All Java source files
│   │
│   ├── Process.java                # Data model for one process
│   │                               #   Fields: pid, arrivalTime, burstTime, priority
│   │                               #   Computed: completionTime, turnaroundTime,
│   │                               #             waitingTime, responseTime
│   │                               #   Internal: remainingTime, responseRecorded
│   │                               #   Method:   copy() — deep copy for isolation
│   │
│   ├── GanttEntry.java             # One segment in the Gantt chart
│   │                               #   Fields:  pid (-1 = IDLE), start, end
│   │                               #   Methods: duration(), label()
│   │
│   ├── ScheduleResult.java         # Output of a scheduling run
│   │                               #   Holds:    algorithmName, process list, Gantt list
│   │                               #   Computes: avgTAT, avgWT, avgRT,
│   │                               #             cpuUtilization, throughput
│   │
│   ├── Scheduler.java              # Interface implemented by all schedulers
│   │                               #   Method:  schedule(processes, quantum)
│   │                               #   Default: deepCopy(list) — fresh copy per run
│   │
│   ├── FCFSScheduler.java          # FCFS implementation
│   │                               #   Sorts by arrival time, then PID
│   │                               #   Inserts IDLE entries for CPU gaps
│   │
│   ├── SJFScheduler.java           # SJF Non-Preemptive implementation
│   │                               #   At each CPU-free point, picks smallest BT
│   │                               #   among all arrived-but-unfinished processes
│   │
│   ├── SRTFScheduler.java          # SRTF Preemptive implementation
│   │                               #   Event-driven: jumps to next arrival or
│   │                               #   completion rather than ticking unit-by-unit
│   │                               #   Merges consecutive same-process Gantt entries
│   │
│   ├── RoundRobinScheduler.java    # Round Robin implementation
│   │                               #   Uses LinkedList as ready queue
│   │                               #   Newly arrived processes enqueue before
│   │                               #   the preempted process re-queues
│   │
│   ├── PriorityNPScheduler.java    # Priority Non-Preemptive implementation
│   │                               #   Lower priority number = higher priority
│   │                               #   Tie: earliest arrival wins, then lowest PID
│   │
│   ├── ResultPrinter.java          # All console output — nothing else
│   │                               #   printGanttChart() — proportional box-drawing chart
│   │                               #   printTable()      — per-process metrics table
│   │                               #   printSummary()    — aggregate statistics block
│   │
│   └── Main.java                   # Entry point and application shell
│                                   #   printBanner(), printMenu()
│                                   #   inputProcesses() — collects and validates input
│                                   #   runMenu()        — dispatches to schedulers
│                                   #   compareAll()     — runs all 5, marks winners
│                                   #   askQuantum()     — prompts for RR quantum
│                                   #   readPositiveInt(), readNonNegInt() — validators
│
├── compile.sh                      # Compiles src/*.java → out/
├── run.sh                          # Compiles if needed, then runs Main
└── README.md                       # This file
```

### Dependency diagram

```
Main
 ├── uses ──► Process                  (input / output model)
 ├── uses ──► FCFSScheduler      ─┐
 ├── uses ──► SJFScheduler        │
 ├── uses ──► SRTFScheduler       ├── all implement ──► Scheduler
 ├── uses ──► RoundRobinScheduler │                     └── returns ──► ScheduleResult
 ├── uses ──► PriorityNPScheduler ─┘                          ├── contains ──► Process (list)
 └── uses ──► ResultPrinter                                    └── contains ──► GanttEntry (list)
```

Each scheduler receives the original process list, makes a **deep copy** via `Scheduler.deepCopy()`, runs the simulation on the copy, and returns a `ScheduleResult`. The original list is never modified — this is what makes the compare-all feature work correctly across all five runs.

---

## How to Compile and Run

### Prerequisites

- **Java JDK 8 or newer** (needs `javac`, not just `java`)
- Works on Linux, macOS, and Windows

### Linux / macOS

```bash
cd cpu-scheduler
chmod +x compile.sh run.sh
./compile.sh        # compiles all .java files into out/
./run.sh            # runs the simulator
```

### Windows (Command Prompt)

```cmd
cd cpu-scheduler
mkdir out
javac -d out src\*.java
java -cp out Main
```

### Manual (any OS)

```bash
javac -d out src/*.java
java -cp out Main
```

---

## Usage Guide

### Step 1 — Enter processes

```
Enter number of processes: 4

  [ P1 ]
    Arrival Time  : 0
    Burst Time    : 8
    Priority      : 3
```

**Priority note:** lower number = higher priority. Priority 1 runs before priority 5.

### Step 2 — Pick an algorithm

```
  ╔══════════════════════════════════════════════════╗
  ║           SELECT SCHEDULING ALGORITHM            ║
  ╠══════════════════════════════════════════════════╣
  ║  1.  FCFS  (First Come First Serve)              ║
  ║  2.  SJF   (Shortest Job First — Non-Preemptive) ║
  ║  3.  SRTF  (Shortest Remaining Time — Preemptive)║
  ║  4.  Round Robin                                 ║
  ║  5.  Priority  (Non-Preemptive)                  ║
  ╠══════════════════════════════════════════════════╣
  ║  6.  Compare All Algorithms                      ║
  ║  7.  Enter New Processes                         ║
  ║  0.  Exit                                        ║
  ╚══════════════════════════════════════════════════╝
```

Round Robin (option 4) will also prompt for a time quantum.

### Step 3 — Read the output

Each run shows three sections:

1. **Gantt chart** — proportional timeline with process labels and clock ticks underneath
2. **Process table** — AT, BT, Priority, CT, TAT, WT, RT for every process
3. **Summary** — averages, CPU utilization, throughput

### Compare All (option 6)

Runs all five algorithms, prints a consolidated table, and marks the best value in each column with **◄**. Optionally prints the full detail view (Gantt + table + summary) for each algorithm in sequence.

---

## Sample Output

```
════════════════════════════════════════════════════════════════════════════════
  Algorithm: FCFS
════════════════════════════════════════════════════════════════════════════════

  GANTT CHART:
  ┌──────────────┬───────┬────────────────┬─────────┐
  │      P1      │  P2   │       P3       │   P4    │
  └──────────────┴───────┴────────────────┴─────────┘
  0              8      12              21         26

  PROCESS TABLE:
  PID    AT    BT    PR    CT     TAT    WT     RT
  ──────────────────────────────────────────────────
  P1     0     8     3     8      8      0      0
  P2     1     4     1     12     11     7      7
  P3     2     9     4     21     19     10     10
  P4     3     5     2     26     23     18     18
  ──────────────────────────────────────────────────

  SUMMARY STATISTICS:
  ──────────────────────────────────────────
  Average Turnaround Time :    15.25 ms
  Average Waiting Time    :     8.75 ms
  Average Response Time   :     8.75 ms
  CPU Utilization         :   100.00 %
  Throughput              :   0.1538 proc/ms
  ──────────────────────────────────────────
```

---

## Design Notes

**Why event-driven SRTF?**
Ticking one unit at a time is correct but wasteful when burst times are large. The event-driven approach jumps directly to the next arrival or completion event, making the algorithm fast and the simulation easy to trace in a debugger.

**Why deep copy before every scheduler run?**
The compare-all mode runs all five schedulers on the same data. Without deep copying, the first scheduler would corrupt the `remainingTime`, `completionTime`, and other mutable fields on the Process objects, and every subsequent scheduler would start with garbage data.

**Why lower priority number = higher priority?**
This matches the convention used in most operating systems and the majority of OS textbooks.

**Why merge consecutive same-process Gantt entries in SRTF?**
When no preemption happens at a new arrival, the simulator would otherwise produce two adjacent segments for the same process. Merging them keeps the Gantt chart clean and readable.

**Why sort by arrival then PID for tie-breaking?**
Using PID as the final tiebreaker makes the output deterministic — the same input always produces the same output regardless of how the JVM happens to order equal elements internally.

---

## Requirements

| Requirement | Detail |
|-------------|--------|
| Java | JDK 8 or newer (needs `javac`) |
| OS | Linux, macOS, Windows |
| Terminal | Any terminal with UTF-8 support for box-drawing characters |
| Memory | Minimal — pure console application, no GUI |
| External Libraries | None — standard Java library only |
