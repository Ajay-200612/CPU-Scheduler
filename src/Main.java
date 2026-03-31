import java.util.*;

/**
 * CPU Process Scheduling Simulator
 * ─────────────────────────────────
 * Algorithms  : FCFS | SJF (NP) | SRTF | Round Robin | Priority (NP)
 * Metrics     : CT | TAT | WT | RT | CPU Utilization | Throughput
 * Special     : Compare all algorithms side-by-side with best-algorithm marker
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    // ═══════════════════════════════════════════════════════
    //  Entry
    // ═══════════════════════════════════════════════════════

    public static void main(String[] args) {
        printBanner();

        boolean keepRunning = true;
        while (keepRunning) {
            List<Process> processes = inputProcesses();
            keepRunning = runMenu(processes);
        }

        System.out.println("\n  Thank you for using CPU Scheduling Simulator. Goodbye!");
    }

    // ═══════════════════════════════════════════════════════
    //  Process Input
    // ═══════════════════════════════════════════════════════

    private static List<Process> inputProcesses() {
        System.out.println("\n" + "─".repeat(54));
        System.out.print("  Enter number of processes: ");
        int n = readPositiveInt();

        List<Process> processes = new ArrayList<>();
        System.out.println("\n  Enter details for each process.");
        System.out.println("  (Priority: lower number = higher priority, e.g. 1 > 5)");
        System.out.println("  " + "─".repeat(52));

        for (int i = 1; i <= n; i++) {
            System.out.printf("%n  [ P%d ]%n", i);
            System.out.printf("    Arrival Time  : "); int at = readNonNegInt();
            System.out.printf("    Burst Time    : "); int bt = readPositiveInt();
            System.out.printf("    Priority      : "); int pr = readPositiveInt();
            processes.add(new Process(i, at, bt, pr));
        }

        System.out.println("\n  " + "─".repeat(52));
        System.out.printf("  %d process(es) loaded.%n", n);

        return processes;
    }

    // ═══════════════════════════════════════════════════════
    //  Algorithm Menu Loop
    // ═══════════════════════════════════════════════════════

    /** Returns true if the user wants to enter new processes, false to exit. */
    private static boolean runMenu(List<Process> processes) {
        while (true) {
            printMenu();
            System.out.print("  Enter choice: ");
            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    ResultPrinter.print(new FCFSScheduler().schedule(processes, 0));
                    break;

                case 2:
                    ResultPrinter.print(new SJFScheduler().schedule(processes, 0));
                    break;

                case 3:
                    ResultPrinter.print(new SRTFScheduler().schedule(processes, 0));
                    break;

                case 4: {
                    int q = askQuantum();
                    ResultPrinter.print(new RoundRobinScheduler().schedule(processes, q));
                    break;
                }

                case 5:
                    ResultPrinter.print(new PriorityNPScheduler().schedule(processes, 0));
                    break;

                case 6: {
                    int q = askQuantum();
                    compareAll(processes, q);
                    break;
                }

                case 7:
                    return true;   // re-enter process data

                case 0:
                    return false;  // exit

                default:
                    System.out.println("  Invalid choice. Please enter 0-7.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //  Compare All Algorithms
    // ═══════════════════════════════════════════════════════

    private static void compareAll(List<Process> processes, int quantum) {
        List<ScheduleResult> results = new ArrayList<>();
        results.add(new FCFSScheduler().schedule(processes, 0));
        results.add(new SJFScheduler().schedule(processes, 0));
        results.add(new SRTFScheduler().schedule(processes, 0));
        results.add(new RoundRobinScheduler().schedule(processes, quantum));
        results.add(new PriorityNPScheduler().schedule(processes, 0));

        // Find bests
        ScheduleResult bestTAT  = results.stream().min(Comparator.comparingDouble(ScheduleResult::avgTurnaroundTime)).get();
        ScheduleResult bestWT   = results.stream().min(Comparator.comparingDouble(ScheduleResult::avgWaitingTime)).get();
        ScheduleResult bestRT   = results.stream().min(Comparator.comparingDouble(ScheduleResult::avgResponseTime)).get();
        ScheduleResult bestUtil = results.stream().max(Comparator.comparingDouble(ScheduleResult::cpuUtilization)).get();

        System.out.println("\n" + "═".repeat(80));
        System.out.println("               ★  ALGORITHM COMPARISON SUMMARY  ★");
        System.out.println("═".repeat(80));
        System.out.printf("  %-30s %9s %9s %9s %9s%n",
                "Algorithm", "Avg TAT", "Avg WT", "Avg RT", "CPU Util");
        System.out.println("  " + "─".repeat(72));

        for (ScheduleResult r : results) {
            String tatMark  = r == bestTAT  ? "◄" : " ";
            String wtMark   = r == bestWT   ? "◄" : " ";
            String rtMark   = r == bestRT   ? "◄" : " ";
            String utilMark = r == bestUtil ? "◄" : " ";
            System.out.printf("  %-30s %7.2f%s  %7.2f%s  %7.2f%s  %6.1f%%%s%n",
                    r.algorithmName,
                    r.avgTurnaroundTime(), tatMark,
                    r.avgWaitingTime(),    wtMark,
                    r.avgResponseTime(),   rtMark,
                    r.cpuUtilization(),    utilMark);
        }

        System.out.println("  " + "─".repeat(72));
        System.out.println("  ◄ = Best in that column");
        System.out.println("═".repeat(80));

        System.out.print("\n  Show detailed results for each algorithm? (y/n): ");
        String ans = sc.nextLine().trim();
        if (ans.equalsIgnoreCase("y")) {
            for (ScheduleResult r : results) {
                ResultPrinter.print(r);
                System.out.print("  ── Press Enter for next algorithm ──");
                sc.nextLine();
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //  UI Helpers
    // ═══════════════════════════════════════════════════════

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔═══════════════════════════════════════════════════════╗");
        System.out.println("  ║           CPU PROCESS SCHEDULING SIMULATOR            ║");
        System.out.println("  ╠═══════════════════════════════════════════════════════╣");
        System.out.println("  ║  Algorithms : FCFS │ SJF │ SRTF │ RR │ Priority      ║");
        System.out.println("  ║  Metrics    : TAT  │ WT  │ RT   │ CPU Util │ Throughput║");
        System.out.println("  ╚═══════════════════════════════════════════════════════╝");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║           SELECT SCHEDULING ALGORITHM            ║");
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  1.  FCFS  (First Come First Serve)              ║");
        System.out.println("  ║  2.  SJF   (Shortest Job First — Non-Preemptive) ║");
        System.out.println("  ║  3.  SRTF  (Shortest Remaining Time — Preemptive)║");
        System.out.println("  ║  4.  Round Robin                                 ║");
        System.out.println("  ║  5.  Priority  (Non-Preemptive)                  ║");
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  6.  Compare All Algorithms                      ║");
        System.out.println("  ║  7.  Enter New Processes                         ║");
        System.out.println("  ║  0.  Exit                                        ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
    }

    private static int askQuantum() {
        System.out.print("  Enter Time Quantum for Round Robin: ");
        return readPositiveInt();
    }

    // ═══════════════════════════════════════════════════════
    //  Input Validation
    // ═══════════════════════════════════════════════════════

    private static int readPositiveInt() {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v > 0) return v;
                System.out.print("  Must be > 0. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("  Invalid — enter a whole number: ");
            }
        }
    }

    private static int readNonNegInt() {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= 0) return v;
                System.out.print("  Must be >= 0. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("  Invalid — enter a whole number: ");
            }
        }
    }

    private static int readMenuChoice() {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("  Invalid — enter a number (0-7): ");
            }
        }
    }
}
