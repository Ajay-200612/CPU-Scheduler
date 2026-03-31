import java.util.*;

/**
 * Shortest Remaining Time First — Preemptive SJF (SRTF)
 *
 * Event-driven simulation: instead of ticking one unit at a time, we jump
 * from event to event (next process arrival or current process completion).
 * This keeps the algorithm correct and fast even for large burst times.
 *
 * Tie in remaining time → earlier arrival time wins → lower PID wins.
 */
public class SRTFScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> processes, int quantum) {
        List<Process> procs     = deepCopy(processes);
        List<Process> remaining = new ArrayList<>(procs);
        List<GanttEntry> gantt  = new ArrayList<>();

        int time = 0;

        while (!remaining.isEmpty()) {
            final int t = time;

            List<Process> available = new ArrayList<>();
            for (Process p : remaining) {
                if (p.arrivalTime <= t) available.add(p);
            }

            if (available.isEmpty()) {
                // CPU idle — jump to next arrival
                int nextAt = remaining.stream()
                        .mapToInt(p -> p.arrivalTime).min().orElse(t + 1);
                appendGantt(gantt, -1, time, nextAt);
                time = nextAt;
                continue;
            }

            // Pick process with shortest remaining time
            Process current = available.stream()
                    .min(Comparator.comparingInt((Process x) -> x.remainingTime)
                                   .thenComparingInt(x -> x.arrivalTime)
                                   .thenComparingInt(x -> x.pid))
                    .get();

            // Record response time (first time process gets CPU)
            if (!current.responseRecorded) {
                current.responseTime    = time - current.arrivalTime;
                current.responseRecorded = true;
            }

            // Find the next arrival that could cause a preemption
            int nextArrival = remaining.stream()
                    .filter(p -> p.arrivalTime > t)
                    .mapToInt(p -> p.arrivalTime)
                    .min().orElse(Integer.MAX_VALUE);

            // Run current process until it finishes OR a new process arrives
            int runUntil = Math.min(time + current.remainingTime, nextArrival);
            int runFor   = runUntil - time;

            appendGantt(gantt, current.pid, time, runUntil);
            current.remainingTime -= runFor;
            time = runUntil;

            if (current.remainingTime == 0) {
                current.completionTime  = time;
                current.turnaroundTime  = current.completionTime - current.arrivalTime;
                current.waitingTime     = current.turnaroundTime - current.burstTime;
                remaining.remove(current);
            }
        }

        procs.sort(Comparator.comparingInt(p -> p.pid));
        return new ScheduleResult("SRTF (Preemptive SJF)", procs, gantt);
    }

    /**
     * Appends a Gantt entry, merging with the last segment when the same process
     * runs consecutively without interruption.
     */
    private void appendGantt(List<GanttEntry> gantt, int pid, int start, int end) {
        if (!gantt.isEmpty() && gantt.get(gantt.size() - 1).pid == pid) {
            gantt.get(gantt.size() - 1).end = end;
        } else {
            gantt.add(new GanttEntry(pid, start, end));
        }
    }
}
