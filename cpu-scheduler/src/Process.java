/**
 * Represents a process with all scheduling-related fields.
 * AT  = Arrival Time
 * BT  = Burst Time
 * CT  = Completion Time
 * TAT = Turnaround Time  (CT - AT)
 * WT  = Waiting Time     (TAT - BT)
 * RT  = Response Time    (first CPU time - AT)
 */
public class Process {
    public int pid;
    public int arrivalTime;
    public int burstTime;
    public int priority;

    // Computed by scheduler
    public int completionTime;
    public int turnaroundTime;
    public int waitingTime;
    public int responseTime;

    // Internal algorithm state
    public int  remainingTime;
    public boolean responseRecorded;

    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid            = pid;
        this.arrivalTime    = arrivalTime;
        this.burstTime      = burstTime;
        this.priority       = priority;
        this.remainingTime  = burstTime;
        this.responseTime   = -1;
        this.responseRecorded = false;
    }

    /** Deep copy — each scheduler works on a fresh copy so original data is untouched. */
    public Process copy() {
        return new Process(pid, arrivalTime, burstTime, priority);
    }
}
