import java.util.List;

/**
 * Handles all console output for a ScheduleResult:
 *   1. Gantt chart with proportional segment widths
 *   2. Per-process table (AT, BT, Priority, CT, TAT, WT, RT)
 *   3. Summary statistics (Avg TAT, Avg WT, Avg RT, CPU Util, Throughput)
 */
public class ResultPrinter {

    private static final int LINE_WIDTH = 80;

    // ─────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────

    public static void print(ScheduleResult result) {
        String title = "  Algorithm: " + result.algorithmName;
        System.out.println("\n" + "═".repeat(LINE_WIDTH));
        System.out.println(title);
        System.out.println("═".repeat(LINE_WIDTH));

        printGanttChart(result.gantt);
        printTable(result.processes);
        printSummary(result);
    }

    // ─────────────────────────────────────────────────────────
    //  Gantt Chart
    // ─────────────────────────────────────────────────────────

    private static void printGanttChart(List<GanttEntry> gantt) {
        System.out.println("\n  GANTT CHART:");

        if (gantt.isEmpty()) {
            System.out.println("  (empty)");
            return;
        }

        int n             = gantt.size();
        int totalDuration = gantt.get(n - 1).end - gantt.get(0).start;

        // ── compute segment widths proportionally ──────────────
        // Available inner width (chars between outer ┌ and ┐, minus n−1 separators)
        // We target chartContentWidth chars distributed across all segments.
        int chartContentWidth = LINE_WIDTH - 2 - (n - 1) - 2; // subtract prefix + separators + borders
        if (chartContentWidth < n * 4) chartContentWidth = n * 4; // absolute minimum

        int[] w    = new int[n];
        int   used = 0;
        if (totalDuration == 0) {
            for (int i = 0; i < n; i++) w[i] = 4;
        } else {
            for (int i = 0; i < n - 1; i++) {
                w[i] = Math.max(4, chartContentWidth * gantt.get(i).duration() / totalDuration);
                used += w[i];
            }
            w[n - 1] = Math.max(4, chartContentWidth - used);
        }

        // ── precompute separator positions (relative to after "  ") ──
        // positions[i]  = column of the left border/separator of segment i
        // positions[n]  = column of the closing right border
        // Layout: ┌ w[0] ┬ w[1] ┬ ... ┬ w[n-1] ┐
        //         0      1+w[0]  ...
        int[] pos = new int[n + 1];
        pos[0] = 0;
        for (int i = 1; i <= n; i++) {
            pos[i] = pos[i - 1] + 1 + w[i - 1];
        }

        // ── top border ────────────────────────────────────────
        StringBuilder top = new StringBuilder("  ┌");
        for (int i = 0; i < n; i++) {
            top.append("─".repeat(w[i]));
            top.append(i < n - 1 ? "┬" : "┐");
        }
        System.out.println(top);

        // ── labels (centered in each segment) ─────────────────
        StringBuilder lbl = new StringBuilder("  │");
        for (int i = 0; i < n; i++) {
            String label = gantt.get(i).label();
            int    pad   = w[i] - label.length();
            int    lp    = pad / 2;
            int    rp    = pad - lp;
            lbl.append(" ".repeat(Math.max(0, lp)))
               .append(label)
               .append(" ".repeat(Math.max(0, rp)))
               .append("│");
        }
        System.out.println(lbl);

        // ── bottom border ─────────────────────────────────────
        StringBuilder bot = new StringBuilder("  └");
        for (int i = 0; i < n; i++) {
            bot.append("─".repeat(w[i]));
            bot.append(i < n - 1 ? "┴" : "┘");
        }
        System.out.println(bot);

        // ── timeline numbers ──────────────────────────────────
        // Number at pos[i] for segment start, number at pos[n] for last end
        StringBuilder tl      = new StringBuilder("  ");
        int           currCol = 0;
        for (int i = 0; i <= n; i++) {
            int    target = pos[i];
            String num    = String.valueOf(
                    i < n ? gantt.get(i).start : gantt.get(n - 1).end);
            int spaces = target - currCol;
            if (spaces > 0) tl.append(" ".repeat(spaces));
            tl.append(num);
            currCol = target + num.length();
        }
        System.out.println(tl);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────
    //  Process Table
    // ─────────────────────────────────────────────────────────

    private static void printTable(List<Process> processes) {
        String header = String.format("  %-5s  %-4s  %-4s  %-4s  %-5s  %-5s  %-5s  %-5s",
                "PID", "AT", "BT", "PR", "CT", "TAT", "WT", "RT");
        String sep    = "  " + "─".repeat(50);

        System.out.println("  PROCESS TABLE:");
        System.out.println(header);
        System.out.println(sep);

        for (Process p : processes) {
            System.out.printf("  %-5s  %-4d  %-4d  %-4d  %-5d  %-5d  %-5d  %-5d%n",
                    "P" + p.pid,
                    p.arrivalTime,
                    p.burstTime,
                    p.priority,
                    p.completionTime,
                    p.turnaroundTime,
                    p.waitingTime,
                    p.responseTime);
        }
        System.out.println(sep);
        System.out.println("  AT=Arrival Time | BT=Burst Time | PR=Priority | CT=Completion Time");
        System.out.println("  TAT=Turnaround Time | WT=Waiting Time | RT=Response Time");
    }

    // ─────────────────────────────────────────────────────────
    //  Summary Statistics
    // ─────────────────────────────────────────────────────────

    private static void printSummary(ScheduleResult r) {
        System.out.println("\n  SUMMARY STATISTICS:");
        System.out.println("  " + "─".repeat(42));
        System.out.printf("  Average Turnaround Time : %8.2f ms%n", r.avgTurnaroundTime());
        System.out.printf("  Average Waiting Time    : %8.2f ms%n", r.avgWaitingTime());
        System.out.printf("  Average Response Time   : %8.2f ms%n", r.avgResponseTime());
        System.out.printf("  CPU Utilization         : %7.2f %%%n",  r.cpuUtilization());
        System.out.printf("  Throughput              : %8.4f proc/ms%n", r.throughput());
        System.out.println("  " + "─".repeat(42));
    }
}
