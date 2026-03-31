import java.util.*;

/**
 * First Come First Serve (FCFS) — Non-Preemptive
 * Processes are served strictly in order of arrival time.
 * Tie in arrival time is broken by PID (lower PID wins).
 */
public class FCFSScheduler implements Scheduler {

    @Override
    public ScheduleResult schedule(List<Process> processes, int quantum) {
        List<Process> procs = deepCopy(processes);

        // Sort by arrival time, then by PID for consistent tie-breaking
        procs.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                             .thenComparingInt(p -> p.pid));

        List<GanttEntry> gantt = new ArrayList<>();
        int time = 0;

        for (Process p : procs) {
            // CPU idle gap
            if (time < p.arrivalTime) {
                gantt.add(new GanttEntry(-1, time, p.arrivalTime));
                time = p.arrivalTime;
            }
            p.responseTime = time - p.arrivalTime;
            gantt.add(new GanttEntry(p.pid, time, time + p.burstTime));
            time += p.burstTime;
            p.completionTime  = time;
            p.turnaroundTime  = p.completionTime - p.arrivalTime;
            p.waitingTime     = p.turnaroundTime - p.burstTime;
        }

        procs.sort(Comparator.comparingInt(p -> p.pid));
        return new ScheduleResult("FCFS", procs, gantt);
    }
}
