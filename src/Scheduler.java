import java.util.ArrayList;
import java.util.List;

/**
 * Common interface for all scheduling algorithms.
 * quantum is only used by Round Robin; other schedulers can pass 0.
 */
public interface Scheduler {

    ScheduleResult schedule(List<Process> processes, int quantum);

    /**
     * Returns a fresh deep-copy of the process list so that every scheduler
     * works on its own isolated data without mutating the originals.
     */
    default List<Process> deepCopy(List<Process> processes) {
        List<Process> copy = new ArrayList<>();
        for (Process p : processes) copy.add(p.copy());
        return copy;
    }
}
