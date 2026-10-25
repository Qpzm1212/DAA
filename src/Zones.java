import java.util.*;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[] from = s.from();
        int[] to = s.to();
        int[][] outSlides = Graphs.slidesFrom(s);
        int[][] inSlides = Graphs.slidesInto(s);

        boolean[] visited = new boolean[n];
        int[] nextIdx = new int[n];
        int[] stack = new int[n];
        int[] finishOrder = new int[n];
        int finishCount = 0;

        for (int start = 0; start < n; start++) {
            if (!visited[start]) {
                int top = 0;
                visited[start] = true;
                stack[top++] = start;
                while (top > 0) {
                    int u = stack[top - 1];
                    int[] slidesOut = outSlides[u];
                    if (nextIdx[u] < slidesOut.length) {
                        int edgeIdx = slidesOut[nextIdx[u]++];
                        int v = to[edgeIdx];
                        if (!visited[v]) {
                            visited[v] = true;
                            stack[top++] = v;
                        }
                    } else {
                        top--;
                        finishOrder[finishCount++] = u;
                    }
                }
            }
        }

        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zoneCount = 0;

        for (int i = finishCount - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (zoneOf[start] == -1) {
                int top = 0;
                zoneOf[start] = zoneCount;
                stack[top++] = start;
                while (top > 0) {
                    int u = stack[--top];
                    for (int edgeIdx : inSlides[u]) {
                        int prev = from[edgeIdx];
                        if (zoneOf[prev] == -1) {
                            zoneOf[prev] = zoneCount;
                            stack[top++] = prev;
                        }
                    }
                }
                zoneCount++;
            }
        }

        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        if (zoneCount == 0 || s.n() == 0) {
            return new long[]{0, 0};
        }
        int m = s.count();
        int[] from = s.from();
        int[] to = s.to();
        int[] time = s.time();

        int[] outDegree = new int[zoneCount];
        for (int i = 0; i < m; i++) {
            int zx = zoneOf[from[i]];
            int zy = zoneOf[to[i]];
            if (zx != zy) {
                outDegree[zx]++;
            }
        }

        int[][] zoneOut = new int[zoneCount][];
        int[][] zoneCost = new int[zoneCount][];
        for (int z = 0; z < zoneCount; z++) {
            zoneOut[z] = new int[outDegree[z]];
            zoneCost[z] = new int[outDegree[z]];
        }

        int[] filled = new int[zoneCount];
        for (int i = 0; i < m; i++) {
            int zx = zoneOf[from[i]];
            int zy = zoneOf[to[i]];
            if (zx != zy) {
                int idx = filled[zx]++;
                zoneOut[zx][idx] = zy;
                zoneCost[zx][idx] = time[i];
            }
        }

        long[] chainLen = new long[zoneCount];
        Arrays.fill(chainLen, 1L);

        long inf = Long.MAX_VALUE / 4;
        long[] dist = new long[zoneCount];
        Arrays.fill(dist, inf);
        int startZone = zoneOf[0];
        int exitZone = zoneOf[s.n() - 1];
        dist[startZone] = 0;

        long maxChain = 0;
        for (int z = 0; z < zoneCount; z++) {
            if (chainLen[z] > maxChain) {
                maxChain = chainLen[z];
            }
            int[] targets = zoneOut[z];
            int[] costs = zoneCost[z];
            long d = dist[z];
            long cLen = chainLen[z];
            for (int j = 0; j < targets.length; j++) {
                int nextZ = targets[j];
                int w = costs[j];
                if (cLen + 1 > chainLen[nextZ]) {
                    chainLen[nextZ] = cLen + 1;
                }
                if (d != inf && d + w < dist[nextZ]) {
                    dist[nextZ] = d + w;
                }
            }
        }

        return new long[]{maxChain, dist[exitZone]};
    }
}
