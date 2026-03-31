import java.util.*;

/**
 * Round Robin (RR) — Preemptive
 *
 * Each process runs for at most `quantum` units. Processes that arrive during
 * the current quantum are added to the ready queue at the moment the quantum
 * expires, BEFORE the current process is re-queued (standard convention).
 *
 * Tie in arrival → lower PID enters queue first.
 */
public class RoundRobinScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> processes, int quantum) {
        List<Process> procs = deepCopy(processes);
        // Sort input once by arrival time (then PID) to control queue order
        procs.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                             .thenComparingInt(p -> p.pid));

        List<GanttEntry> gantt = new ArrayList<>();
        Queue<Process>   ready = new LinkedList<>();

        int n         = procs.size();
        int idx       = 0;         // next process to enter queue from sorted list
        int time      = 0;
        int completed = 0;

        // Seed queue with processes that arrive at or before time 0
        while (idx < n && procs.get(idx).arrivalTime <= time) {
            ready.add(procs.get(idx++));
        }

        while (completed < n) {
            if (ready.isEmpty()) {
                // CPU idle — jump to the next arrival
                int nextAt = procs.get(idx).arrivalTime;
                gantt.add(new GanttEntry(-1, time, nextAt));
                time = nextAt;
                while (idx < n && procs.get(idx).arrivalTime <= time) {
                    ready.add(procs.get(idx++));
                }
                continue;
            }

            Process p = ready.poll();

            // Record first-time CPU access
            if (!p.responseRecorded) {
                p.responseTime    = time - p.arrivalTime;
                p.responseRecorded = true;
            }

            int runFor = Math.min(quantum, p.remainingTime);
            gantt.add(new GanttEntry(p.pid, time, time + runFor));
            time             += runFor;
            p.remainingTime  -= runFor;

            // Enqueue processes that arrived during this time slice
            while (idx < n && procs.get(idx).arrivalTime <= time) {
                ready.add(procs.get(idx++));
            }

            if (p.remainingTime == 0) {
                p.completionTime  = time;
                p.turnaroundTime  = p.completionTime - p.arrivalTime;
                p.waitingTime     = p.turnaroundTime - p.burstTime;
                completed++;
            } else {
                // Process not done — push back to end of ready queue
                ready.add(p);
            }
        }

        procs.sort(Comparator.comparingInt(p -> p.pid));
        return new ScheduleResult("Round Robin (Q=" + quantum + ")", procs, gantt);
    }
}
