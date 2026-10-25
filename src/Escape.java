import java.util.*;

/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        int cols = w.cols();
        int[][] open = Graphs.open(w, broken);

        int deadEnds = 0;
        for (int u = 0; u < n; u++) {
            if (open[u].length == 1) {
                deadEnds++;
            }
        }

        if (n == 0) {
            return new Result(new int[0], 0, 0);
        }

        // Order each cell's open neighbours as: up (u - cols), right (u + 1), down (u + cols), left (u - 1)
        int[][] ordered = new int[n][];
        for (int u = 0; u < n; u++) {
            int[] nbrs = open[u];
            int[] sorted = new int[nbrs.length];
            int idx = 0;
            int[] candidates = {u - cols, u + 1, u + cols, u - 1};
            for (int cand : candidates) {
                for (int v : nbrs) {
                    if (v == cand) {
                        sorted[idx++] = v;
                        break;
                    }
                }
            }
            ordered[u] = sorted;
        }

        boolean[] visited = new boolean[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        int[] nextIdx = new int[n];
        int[] stack = new int[n];
        int top = 0;

        stack[top++] = 0;
        visited[0] = true;
        int visitedCount = 1;
        int target = n - 1;

        while (top > 0) {
            int u = stack[top - 1];
            if (u == target) {
                break;
            }
            int[] nbrs = ordered[u];
            if (nextIdx[u] < nbrs.length) {
                int v = nbrs[nextIdx[u]++];
                if (!visited[v]) {
                    visited[v] = true;
                    visitedCount++;
                    parent[v] = u;
                    stack[top++] = v;
                }
            } else {
                top--;
            }
        }

        if (!visited[target]) {
            return new Result(new int[0], visitedCount, deadEnds);
        }

        int len = 0;
        for (int cur = target; cur != -1; cur = parent[cur]) {
            len++;
        }
        int[] path = new int[len];
        int pos = len - 1;
        for (int cur = target; cur != -1; cur = parent[cur]) {
            path[pos--] = cur;
        }

        return new Result(path, visitedCount, deadEnds);
    }
}
