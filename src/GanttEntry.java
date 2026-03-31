/**
 * One segment in the Gantt chart.
 * pid == -1 means the CPU was idle during [start, end].
 */
public class GanttEntry {
    public int pid;
    public int start;
    public int end;

    public GanttEntry(int pid, int start, int end) {
        this.pid   = pid;
        this.start = start;
        this.end   = end;
    }

    public int duration() {
        return end - start;
    }

    public String label() {
        return pid == -1 ? "IDLE" : "P" + pid;
    }
}
