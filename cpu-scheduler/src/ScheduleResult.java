import java.util.List;

/**
 * Holds all output produced by a scheduling algorithm:
 *   - the algorithm name
 *   - per-process results (CT, TAT, WT, RT)
 *   - the Gantt chart sequence
 * Also computes aggregate statistics on demand.
 */
public class ScheduleResult {
    public final String        algorithmName;
    public final List<Process> processes;
    public final List<GanttEntry> gantt;

    public ScheduleResult(String algorithmName,
                          List<Process> processes,
                          List<GanttEntry> gantt) {
        this.algorithmName = algorithmName;
        this.processes     = processes;
        this.gantt         = gantt;
    }

    public double avgTurnaroundTime() {
        return processes.stream().mapToInt(p -> p.turnaroundTime).average().orElse(0);
    }

    public double avgWaitingTime() {
        return processes.stream().mapToInt(p -> p.waitingTime).average().orElse(0);
    }

    public double avgResponseTime() {
        return processes.stream().mapToInt(p -> p.responseTime).average().orElse(0);
    }

    /** Percentage of time the CPU was actually executing a process. */
    public double cpuUtilization() {
        if (gantt.isEmpty()) return 0;
        int busyTime  = processes.stream().mapToInt(p -> p.burstTime).sum();
        int totalTime = gantt.get(gantt.size() - 1).end - gantt.get(0).start;
        return totalTime == 0 ? 0 : (double) busyTime / totalTime * 100;
    }

    /** Processes completed per unit time. */
    public double throughput() {
        if (gantt.isEmpty()) return 0;
        int totalTime = gantt.get(gantt.size() - 1).end - gantt.get(0).start;
        return totalTime == 0 ? 0 : (double) processes.size() / totalTime;
    }
}
