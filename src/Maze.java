import java.util.*;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        int n = w.cells();
        DisjointSet dsu = new DisjointSet(n);
        int[] temp = new int[Math.max(0, n - 1)];
        int count = 0;
        int[] a = w.a();
        int[] b = w.b();
        for (int k = 0; k < w.count(); k++) {
            if (dsu.union(a[k], b[k])) {
                temp[count++] = k;
            }
        }
        return count == temp.length ? temp : Arrays.copyOf(temp, count);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        int n = w.cells();
        int m = w.count();
        int[] a = w.a();
        int[] b = w.b();
        int[] cost = w.cost();

        long[] packed = new long[m];
        for (int k = 0; k < m; k++) {
            packed[k] = (((long) cost[k]) << 32) | (k & 0xFFFFFFFFL);
        }
        Arrays.sort(packed);

        DisjointSet dsu = new DisjointSet(n);
        int[] temp = new int[Math.max(0, n - 1)];
        int count = 0;
        for (int i = 0; i < m && count < n - 1; i++) {
            int k = (int) packed[i];
            if (dsu.union(a[k], b[k])) {
                temp[count++] = k;
            }
        }
        return count == temp.length ? temp : Arrays.copyOf(temp, count);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n <= 1) {
            return 0;
        }
        int m = w.count();
        int[] a = w.a();
        int[] b = w.b();
        int[] cost = w.cost();

        int[] degree = new int[n];
        for (int k = 0; k < m; k++) {
            degree[a[k]]++;
            degree[b[k]]++;
        }
        int[][] adj = new int[n][];
        for (int u = 0; u < n; u++) {
            adj[u] = new int[degree[u]];
        }
        int[] filled = new int[n];
        for (int k = 0; k < m; k++) {
            int u = a[k];
            int v = b[k];
            adj[u][filled[u]++] = k;
            adj[v][filled[v]++] = k;
        }

        boolean[] inMst = new boolean[n];
        PriorityQueue<Long> pq = new PriorityQueue<>();
        inMst[0] = true;
        int visitedCount = 1;
        for (int k : adj[0]) {
            int next = a[k] ^ b[k]; // since one endpoint is 0, a[k] ^ b[k] gives the other endpoint
            pq.add((((long) cost[k]) << 32) | (next & 0xFFFFFFFFL));
        }

        long totalCost = 0;
        while (!pq.isEmpty() && visitedCount < n) {
            long top = pq.poll();
            int u = (int) top;
            if (inMst[u]) {
                continue;
            }
            int wCost = (int) (top >>> 32);
            inMst[u] = true;
            visitedCount++;
            totalCost += wCost;

            for (int k : adj[u]) {
                int v = a[k] == u ? b[k] : a[k];
                if (!inMst[v]) {
                    pq.add((((long) cost[k]) << 32) | (v & 0xFFFFFFFFL));
                }
            }
        }
        return totalCost;
    }
}
