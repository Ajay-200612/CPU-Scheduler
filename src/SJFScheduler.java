import java.util.*;

/**
 * Shortest Job First — Non-Preemptive (SJF)
 * At each decision point (CPU becomes free), the arrived process with the
 * smallest burst time is selected.  Ties are broken by arrival time, then PID.
 */
public class SJFScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> processes, int quantum) {
        List<Process> procs     = deepCopy(processes);
        List<Process> remaining = new ArrayList<>(procs);
        List<GanttEntry> gantt  = new ArrayList<>();

        int time = 0;

        while (!remaining.isEmpty()) {
            final int t = time;

            // Collect all processes that have already arrived
            List<Process> available = new ArrayList<>();
            for (Process p : remaining) {
                if (p.arrivalTime <= t) available.add(p);
            }

            if (available.isEmpty()) {
                // No process ready — jump CPU to nearest arrival
                int nextAt = remaining.stream()
                        .mapToInt(p -> p.arrivalTime).min().orElse(t + 1);
                gantt.add(new GanttEntry(-1, time, nextAt));
                time = nextAt;
                continue;
            }

            // Pick shortest burst; tie → earliest arrival; tie → lowest PID
            Process p = available.stream()
                    .min(Comparator.comparingInt((Process x) -> x.burstTime)
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
        return new ScheduleResult("SJF (Non-Preemptive)", procs, gantt);
    }
}
