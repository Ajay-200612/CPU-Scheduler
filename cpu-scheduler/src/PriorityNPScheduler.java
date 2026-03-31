import java.util.*;

/**
 * Priority Scheduling — Non-Preemptive
 *
 * Convention: lower priority number = higher priority (e.g. priority 1 runs
 * before priority 5).  Once a process starts, it runs to completion.
 *
 * Tie in priority → earlier arrival time wins → lower PID wins.
 */
public class PriorityNPScheduler implements Scheduler {

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
                int nextAt = remaining.stream()
                        .mapToInt(p -> p.arrivalTime).min().orElse(t + 1);
                gantt.add(new GanttEntry(-1, time, nextAt));
                time = nextAt;
                continue;
            }

            // Pick highest priority (lowest number)
            Process p = available.stream()
                    .min(Comparator.comparingInt((Process x) -> x.priority)
                                   .thenComparingInt(x -> x.arrivalTime)
                                   .thenComparingInt(x -> x.pid))
                    .get();

            p.responseTime   = time - p.arrivalTime;
            gantt.add(new GanttEntry(p.pid, time, time + p.burstTime));
            time            += p.burstTime;
            p.completionTime = time;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime    = p.turnaroundTime - p.burstTime;
            remaining.remove(p);
        }

        procs.sort(Comparator.comparingInt(p -> p.pid));
        return new ScheduleResult("Priority (Non-Preemptive)", procs, gantt);
    }
}
