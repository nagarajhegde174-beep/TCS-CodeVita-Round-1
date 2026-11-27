import java.util.*;
import java.io.*;

public class Snake_Game {
    static class S {
        long cost; int[] body; int ld, pc, fm, pm, km, ph;
        S(long c, int[] b, int ld, int pc, int fm, int pm, int km, int ph) {
            this.cost = c; this.body = b; this.ld = ld; this.pc = pc;
            this.fm = fm; this.pm = pm; this.km = km; this.ph = ph;
        }
    }

    static int C;

    static long key(S s) {
        int[] b = s.body;
        long k = b.length;
        k = (k << 6) | b[0];
        for (int i = 1; i < b.length; i++) {
            int d = dirOf(b[i - 1], b[i]);
            k = (k << 2) | d;
        }
        k = (k << 2) | s.ld;
        k = (k << 2) | s.pc;
        k = (k << 3) | s.fm;
        k = (k << 2) | s.pm;
        k = (k << 3) | s.km;
        k = (k << 2) | s.ph;
        return k;
    }

    static int dirOf(int a, int b) {
        int ar = a / C, ac = a % C, br = b / C, bc = b % C;
        if (br < ar) return 0;
        if (br > ar) return 1;
        if (bc < ac) return 2;
        return 3;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append(' ');
        StringTokenizer st = new StringTokenizer(sb.toString());
        int R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        int period = Integer.parseInt(st.nextToken());
        int sc = Integer.parseInt(st.nextToken());
        int tc = Integer.parseInt(st.nextToken());
        int mc = Integer.parseInt(st.nextToken());
        char[][] g = new char[R][];
        for (int i = 0; i < R; i++) g[i] = st.nextToken().toCharArray();
        int k = Integer.parseInt(st.nextToken());
        int[] body = new int[k];
        for (int i = 0; i < k; i++) {
            int r = Integer.parseInt(st.nextToken()) - 1;
            int c = Integer.parseInt(st.nextToken()) - 1;
            body[i] = r * C + c;
        }

        int[] fid = new int[R * C], pid = new int[R * C];
        Arrays.fill(fid, -1);
        Arrays.fill(pid, -1);
        int nf = 0, np = 0, exit = -1;
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                char ch = g[r][c];
                if (ch == 'f') fid[r * C + c] = nf++;
                else if (ch == 'p') pid[r * C + c] = np++;
                else if (ch == 'E') exit = r * C + c;
            }
        int fullF = (1 << nf) - 1;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        final long M = 1000000L;

        int ld0 = dirOf(body[1], body[0]);
        S init = new S(0, body, ld0, 0, 0, 0, 0, 0);

        PriorityQueue<S> pq = new PriorityQueue<S>(new Comparator<S>() {
            public int compare(S a, S b) { return Long.compare(a.cost, b.cost); }
        });
        HashMap<Long, Long> best = new HashMap<Long, Long>();
        best.put(key(init), 0L);
        pq.add(init);

        while (!pq.isEmpty()) {
            S s = pq.poll();
            Long bc0 = best.get(key(s));
            if (bc0 != null && bc0 < s.cost) continue;
            if (s.body[0] == exit && s.fm == fullF) {
                System.out.print((s.cost / M) + " " + (s.cost % M));
                return;
            }
            int len = s.body.length;
            int hr = s.body[0] / C, hc = s.body[0] % C;
            for (int d = 0; d < 4; d++) {
                if (d == (s.ld ^ 1)) continue;
                if (s.pc > 0 && d != s.ld) continue;
                int nr = hr + dr[d], nc = hc + dc[d];
                if (nr < 0 || nc < 0 || nr >= R || nc >= C) continue;
                char ch = g[nr][nc];
                if (ch == '#') continue;
                if (ch >= 'A' && ch <= 'C' && (s.km & (1 << (ch - 'A'))) == 0) continue;
                int nph = (s.ph + 1) % period;
                if (ch >= '0' && ch <= '3' && (ch - '0') == nph) continue;
                int target = nr * C + nc;
                int fi = fid[target];
                boolean food = fi >= 0 && (s.fm & (1 << fi)) == 0;
                boolean bad = false;
                for (int i = 0; i < len; i++) {
                    if (s.body[i] == target) {
                        if (i == len - 1 && !food) continue;
                        bad = true;
                        break;
                    }
                }
                if (bad) continue;

                int nlen = food ? len + 1 : len;
                int[] nb = new int[nlen];
                nb[0] = target;
                for (int i = 1; i < nlen; i++) nb[i] = s.body[i - 1];
                int fm = s.fm, pm = s.pm, km = s.km, pc = s.pc;
                if (food) fm |= (1 << fi);
                if (ch >= 'a' && ch <= 'c') km |= (1 << (ch - 'a'));
                int pi = pid[target];
                if (pi >= 0 && (pm & (1 << pi)) == 0) {
                    pm |= (1 << pi);
                    pc = 2;
                    if (nb.length > 2) nb = Arrays.copyOf(nb, nb.length - 1);
                } else if (pc > 0) {
                    pc--;
                }
                long e = (d == s.ld) ? sc : tc;
                if (ch == '~') e += mc;
                long nc2 = s.cost + e * M + 1;
                S ns = new S(nc2, nb, d, pc, fm, pm, km, nph);
                long kk = key(ns);
                Long old = best.get(kk);
                if (old == null || nc2 < old) {
                    best.put(kk, nc2);
                    pq.add(ns);
                }
            }
        }
        System.out.print(-1);
    }
}
