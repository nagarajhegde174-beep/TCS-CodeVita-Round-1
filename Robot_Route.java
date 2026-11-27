import java.util.*;
import java.io.*;

public class Robot_Route {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append(' ');
        StringTokenizer st = new StringTokenizer(sb.toString());
        int N = Integer.parseInt(st.nextToken());
        int M = Integer.parseInt(st.nextToken());
        int B = Integer.parseInt(st.nextToken());
        int T = Integer.parseInt(st.nextToken());
        char[][] g = new char[N][];
        int sr = 0, sc = 0;
        for (int i = 0; i < N; i++) {
            g[i] = st.nextToken().toCharArray();
            for (int j = 0; j < M; j++) if (g[i][j] == 'S') { sr = i; sc = j; }
        }
        final int INF = Integer.MAX_VALUE / 2;
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        // ---------- Phase 1: before pickup ----------
        int[][][][] dist = new int[N][M][B + 1][2];
        for (int[][][] a : dist) for (int[][] b : a) for (int[] c : b) Arrays.fill(c, INF);
        int[][] pick = new int[B + 1][2];
        for (int[] p : pick) Arrays.fill(p, INF);

        PriorityQueue<int[]> pq = new PriorityQueue<int[]>(new Comparator<int[]>() {
            public int compare(int[] a, int[] b) { return a[0] - b[0]; }
        });
        dist[sr][sc][B][0] = 0;
        pq.add(new int[]{0, sr, sc, B});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int t = cur[0], r = cur[1], c = cur[2], b = cur[3];
            int par = t & 1;
            if (dist[r][c][b][par] != t) continue;
            // wait
            if (t + 1 < dist[r][c][b][(t + 1) & 1]) {
                dist[r][c][b][(t + 1) & 1] = t + 1;
                pq.add(new int[]{t + 1, r, c, b});
            }
            // charge
            if (g[r][c] == 'C' && b < B) {
                if (t + 1 < dist[r][c][B][(t + 1) & 1]) {
                    dist[r][c][B][(t + 1) & 1] = t + 1;
                    pq.add(new int[]{t + 1, r, c, B});
                }
            }
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr < 0 || nc < 0 || nr >= N || nc >= M) continue;
                char ch = g[nr][nc];
                if (ch == '#') continue;
                int bc = (ch == 'W') ? 2 : 1;
                int tc = (ch == 'X') ? 2 : 1;
                if (b < bc) continue;
                if (ch == 'G' && (t & 1) != 0) continue;
                int nb = b - bc, nt = t + tc;
                if (ch == 'P') {
                    if (nt < pick[nb][nt & 1]) pick[nb][nt & 1] = nt;
                    continue;
                }
                if (nt < dist[nr][nc][nb][nt & 1]) {
                    dist[nr][nc][nb][nt & 1] = nt;
                    pq.add(new int[]{nt, nr, nc, nb});
                }
            }
        }

        // ---------- Phase 2: after pickup ----------
        int pr = 0, pc = 0;
        for (int i = 0; i < N; i++) for (int j = 0; j < M; j++) if (g[i][j] == 'P') { pr = i; pc = j; }

        int B1 = B + 1;
        int[] dp = new int[(T + 1) * N * M * B1 * 4];
        Arrays.fill(dp, INF);
        // index: ((((clock*N + r)*M + c)*B1 + b)*2 + san)*2 + par
        for (int b = 0; b <= B; b++)
            for (int p = 0; p < 2; p++)
                if (pick[b][p] < INF) {
                    int idx = ((((0 * N + pr) * M + pc) * B1 + b) * 2 + 0) * 2 + p;
                    dp[idx] = Math.min(dp[idx], pick[b][p]);
                }

        int ans = INF;
        for (int clk = 0; clk <= T; clk++) {
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    for (int b = 0; b <= B; b++) {
                        for (int s = 0; s < 2; s++) {
                            for (int p = 0; p < 2; p++) {
                                int idx = ((((clk * N + r) * M + c) * B1 + b) * 2 + s) * 2 + p;
                                int pt = dp[idx];
                                if (pt >= INF) continue;
                                // wait
                                if (clk + 1 <= T) {
                                    int ni = ((((( clk + 1) * N + r) * M + c) * B1 + b) * 2 + s) * 2 + p;
                                    if (pt < dp[ni]) dp[ni] = pt;
                                    // charge
                                    if (g[r][c] == 'C') {
                                        int ci = ((((( clk + 1) * N + r) * M + c) * B1 + B) * 2 + s) * 2 + p;
                                        if (pt < dp[ci]) dp[ci] = pt;
                                    }
                                }
                                boolean even = ((p + clk) & 1) == 0;
                                for (int d = 0; d < 4; d++) {
                                    int nr = r + dr[d], nc = c + dc[d];
                                    if (nr < 0 || nc < 0 || nr >= N || nc >= M) continue;
                                    char ch = g[nr][nc];
                                    if (ch == '#' || ch == 'W') continue;
                                    int tc = (ch == 'X') ? 2 : 1;
                                    if (b < 1) continue;
                                    if (ch == 'G' && !even) continue;
                                    if (clk + tc > T) continue;
                                    if (ch == 'D') {
                                        if (s == 1) ans = Math.min(ans, pt + clk + tc);
                                        continue;
                                    }
                                    int ns = s;
                                    if (ch == 'H') ns = 1;
                                    int ni = ((((( clk + tc) * N + nr) * M + nc) * B1 + (b - 1)) * 2 + ns) * 2 + p;
                                    if (pt < dp[ni]) dp[ni] = pt;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.print(ans >= INF ? -1 : ans);
    }
}