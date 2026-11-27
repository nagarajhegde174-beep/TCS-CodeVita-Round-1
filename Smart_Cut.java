import java.util.*;
import java.io.*;

public class Smart_Cut {
    static class Face {
        double a, b, c, d, tx, ty;
        double[][] poly;
    }

    static int n;
    static double[][] orig;
    static int best;
    static final int CAP = 9;
    static int[][] grpS = new int[CAP + 2][];
    static double[][][] imgS = new double[CAP + 2][][];
    static ArrayList<Face>[] facesS = new ArrayList[CAP + 2];
    static int[][] memoS = new int[CAP + 2][];

    static double area(double[][] p) {
        double s = 0;
        for (int i = 0; i < p.length; i++) {
            double[] u = p[i], v = p[(i + 1) % p.length];
            s += u[0] * v[1] - u[1] * v[0];
        }
        return s / 2;
    }

    static double[][] clip(double[][] poly, double ux, double uy, double k) {
        ArrayList<double[]> out = new ArrayList<double[]>();
        int m = poly.length;
        for (int i = 0; i < m; i++) {
            double[] cur = poly[i], prev = poly[(i + m - 1) % m];
            double gc = ux * cur[0] + uy * cur[1] + k;
            double gp = ux * prev[0] + uy * prev[1] + k;
            boolean ci = gc >= -1e-6, pi = gp >= -1e-6;
            if (ci) {
                if (!pi) out.add(inter(prev, cur, gp, gc));
                out.add(cur);
            } else if (pi) {
                out.add(inter(prev, cur, gp, gc));
            }
        }
        ArrayList<double[]> res = new ArrayList<double[]>();
        for (double[] p : out) {
            if (res.isEmpty() || dist(res.get(res.size() - 1), p) > 1e-6) res.add(p);
        }
        while (res.size() > 1 && dist(res.get(0), res.get(res.size() - 1)) <= 1e-6) res.remove(res.size() - 1);
        if (res.size() < 3) return null;
        return res.toArray(new double[0][]);
    }

    static double[] inter(double[] p, double[] q, double gp, double gc) {
        double t = gp / (gp - gc);
        return new double[]{p[0] + t * (q[0] - p[0]), p[1] + t * (q[1] - p[1])};
    }

    static double dist(double[] p, double[] q) {
        return Math.hypot(p[0] - q[0], p[1] - q[1]);
    }

    static final int INF = 1000;
    static int[] groups(double[][] img, int[] death, int d) {
        int[] g = new int[n];
        for (int i = 0; i < n; i++) {
            if (death[i] < d) { g[i] = -1; continue; }
            g[i] = i;
            for (int j = 0; j < i; j++) {
                if (g[j] != j) continue;
                if (same(img[i], img[j])) { g[i] = j; break; }
            }
        }
        return g;
    }

    static boolean same(double[] s, double[] t) {
        double e = 1e-3;
        boolean f = Math.abs(s[0] - t[0]) < e && Math.abs(s[1] - t[1]) < e
                && Math.abs(s[2] - t[2]) < e && Math.abs(s[3] - t[3]) < e;
        boolean r = Math.abs(s[0] - t[2]) < e && Math.abs(s[1] - t[3]) < e
                && Math.abs(s[2] - t[0]) < e && Math.abs(s[3] - t[1]) < e;
        return f || r;
    }

    static boolean valid(int d, int g) {
        if (memoS[d][g] != 0) return memoS[d][g] == 1;
        double[] s = imgS[d][g];
        double len = Math.hypot(s[2] - s[0], s[3] - s[1]);
        boolean ok = true;
        for (Face f : facesS[d]) {
            double px = s[0] - f.tx, py = s[1] - f.ty, qx = s[2] - f.tx, qy = s[3] - f.ty;
            double Px = f.a * px + f.c * py, Py = f.b * px + f.d * py;
            double Qx = f.a * qx + f.c * qy, Qy = f.b * qx + f.d * qy;
            double[][] R = f.poly;
            double sgn = area(R) >= 0 ? 1 : -1;
            double t0 = 0, t1 = 1;
            boolean empty = false;
            for (int i = 0; i < R.length && !empty; i++) {
                double[] u = R[i], v = R[(i + 1) % R.length];
                double ex = v[0] - u[0], ey = v[1] - u[1];
                double el = Math.hypot(ex, ey);
                double nx = -ey / el * sgn, ny = ex / el * sgn;
                double d0 = nx * (Px - u[0]) + ny * (Py - u[1]);
                double dd = nx * (Qx - Px) + ny * (Qy - Py);
                if (Math.abs(dd) < 1e-12) {
                    if (d0 < -1e-3) empty = true;
                } else {
                    double t = (-1e-3 - d0) / dd;
                    if (dd > 0) t0 = Math.max(t0, t); else t1 = Math.min(t1, t);
                }
            }
            if (empty || t1 - t0 <= 0) continue;
            double piece = (t1 - t0) * len;
            if (piece <= 1e-3) continue;
            if (piece < len - 1e-3) { ok = false; break; }
            boolean match = false;
            for (int i = 0; i < n && !match; i++) {
                if (grpS[d][i] != g) continue;
                double[] o = orig[i];
                double e = 1e-3;
                boolean a1 = Math.abs(o[0] - Px) < e && Math.abs(o[1] - Py) < e
                        && Math.abs(o[2] - Qx) < e && Math.abs(o[3] - Qy) < e;
                boolean a2 = Math.abs(o[0] - Qx) < e && Math.abs(o[1] - Qy) < e
                        && Math.abs(o[2] - Px) < e && Math.abs(o[3] - Py) < e;
                if (a1 || a2) match = true;
            }
            if (!match) { ok = false; break; }
        }
        memoS[d][g] = ok ? 1 : 2;
        return ok;
    }

    static int[] setMasks;
    static int setCnt;
    static HashMap<Integer,Integer> memoCover;

    static int cover(int unc) {
        if (unc == 0) return 0;
        Integer c = memoCover.get(unc);
        if (c != null) return c;
        int e = Integer.numberOfTrailingZeros(unc);
        int bestc = INF;
        for (int k = 0; k < setCnt; k++) {
            if ((setMasks[k] & (1 << e)) == 0) continue;
            int r = 1 + cover(unc & ~setMasks[k]);
            if (r < bestc) bestc = r;
        }
        memoCover.put(unc, bestc);
        return bestc;
    }

    static int minCuts(int depth) {
        HashSet<Integer> uniq = new HashSet<Integer>();
        for (int st = 0; st <= depth; st++) {
            for (int g = 0; g < n; g++) {
                if (grpS[st][g] != g) continue;
                if (!valid(st, g)) continue;
                int m = 0;
                for (int i = 0; i < n; i++) if (grpS[st][i] == g) m |= 1 << i;
                uniq.add(m);
            }
        }
        ArrayList<Integer> L = new ArrayList<Integer>(uniq);
        ArrayList<Integer> keep = new ArrayList<Integer>();
        for (int x : L) {
            boolean dom = false;
            for (int y : L) if (y != x && (x & y) == x) { dom = true; break; }
            if (!dom) keep.add(x);
        }
        setCnt = keep.size();
        setMasks = new int[setCnt];
        for (int i = 0; i < setCnt; i++) setMasks[i] = keep.get(i);
        memoCover = new HashMap<Integer,Integer>();
        return cover((1 << n) - 1);
    }

    static void dfs(int depth, double[][] shape, double[][] img, ArrayList<Face> faces, int[] death) {
        imgS[depth] = img;
        facesS[depth] = faces;
        grpS[depth] = groups(img, death, depth);
        memoS[depth] = new int[n];
        int total = depth + minCuts(depth);
        if (total < best) best = total;
        if (depth >= CAP || depth + 2 >= best) return;

        int m = shape.length;
        double cx = 0, cy = 0;
        for (double[] v : shape) { cx += v[0]; cy += v[1]; }
        cx /= m; cy /= m;

        ArrayList<double[]> dirs = new ArrayList<double[]>();
        ArrayList<double[]> cands = new ArrayList<double[]>();
        for (int i = 0; i < m; i++) {
            cands.add(new double[]{shape[i][0] - cx, shape[i][1] - cy});
            double mx = (shape[i][0] + shape[(i + 1) % m][0]) / 2 - cx;
            double my = (shape[i][1] + shape[(i + 1) % m][1]) / 2 - cy;
            cands.add(new double[]{mx, my});
        }
        for (double[] cd : cands) {
            double l = Math.hypot(cd[0], cd[1]);
            if (l < 1e-9) continue;
            double dx = cd[0] / l, dy = cd[1] / l;
            boolean dup = false;
            for (double[] e : dirs) if (Math.abs(e[0] * dy - e[1] * dx) < 1e-6) { dup = true; break; }
            if (dup) continue;
            boolean sym = true;
            for (int i = 0; i < m && sym; i++) {
                double wx = shape[i][0] - cx, wy = shape[i][1] - cy;
                double dp = dx * wx + dy * wy;
                double rx = cx + 2 * dp * dx - wx, ry = cy + 2 * dp * dy - wy;
                boolean f = false;
                for (int j = 0; j < m; j++)
                    if (Math.abs(shape[j][0] - rx) < 1e-3 && Math.abs(shape[j][1] - ry) < 1e-3) { f = true; break; }
                if (!f) sym = false;
            }
            if (sym) dirs.add(new double[]{dx, dy});
        }

        for (double[] dd : dirs) {
            double dx = dd[0], dy = dd[1];
            double nx = -dy, ny = dx;
            double[] side = new double[n];
            int[] ndeath = death.clone();
            for (int i = 0; i < n; i++) {
                if (death[i] < depth) { side[i] = 1; continue; }
                double[] s = img[i];
                double sp = nx * (s[0] - cx) + ny * (s[1] - cy);
                double sq = nx * (s[2] - cx) + ny * (s[3] - cy);
                if (Math.abs(sp) < 1e-3 && Math.abs(sq) < 1e-3) ndeath[i] = depth;
                else if ((sp > 1e-3 && sq < -1e-3) || (sp < -1e-3 && sq > 1e-3)) ndeath[i] = depth;
                side[i] = sp + sq;
            }
            double[][] nimg = new double[n][4];
            for (int i = 0; i < n; i++) {
                double[] s = img[i];
                if (ndeath[i] < depth + 1 || side[i] >= 0) nimg[i] = s.clone();
                else {
                    for (int k = 0; k < 2; k++) {
                        double wx = s[2 * k] - cx, wy = s[2 * k + 1] - cy;
                        double dp = dx * wx + dy * wy;
                        nimg[i][2 * k] = cx + 2 * dp * dx - wx;
                        nimg[i][2 * k + 1] = cy + 2 * dp * dy - wy;
                    }
                }
            }
            double[][] nshape = clip(shape, nx, ny, -(nx * cx + ny * cy));
            if (nshape == null) continue;
            double r11 = 1 - 2 * nx * nx, r12 = -2 * nx * ny, r22 = 1 - 2 * ny * ny;
            ArrayList<Face> nf = new ArrayList<Face>();
            for (Face f : faces) {
                double mx = f.a * nx + f.c * ny, my = f.b * nx + f.d * ny;
                double k = nx * (f.tx - cx) + ny * (f.ty - cy);
                double[][] plus = clip(f.poly, mx, my, k);
                if (plus != null && Math.abs(area(plus)) > 1e-6) {
                    Face g = new Face();
                    g.a = f.a; g.b = f.b; g.c = f.c; g.d = f.d; g.tx = f.tx; g.ty = f.ty;
                    g.poly = plus;
                    nf.add(g);
                }
                double[][] minus = clip(f.poly, -mx, -my, -k);
                if (minus != null && Math.abs(area(minus)) > 1e-6) {
                    Face g = new Face();
                    g.a = r11 * f.a + r12 * f.c;
                    g.b = r11 * f.b + r12 * f.d;
                    g.c = r12 * f.a + r22 * f.c;
                    g.d = r12 * f.b + r22 * f.d;
                    double ux = f.tx - cx, uy = f.ty - cy;
                    g.tx = r11 * ux + r12 * uy + cx;
                    g.ty = r12 * ux + r22 * uy + cy;
                    g.poly = minus;
                    nf.add(g);
                }
            }
            dfs(depth + 1, nshape, nimg, nf, ndeath);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append(' ');
        StringTokenizer st = new StringTokenizer(sb.toString());
        int N = Integer.parseInt(st.nextToken());
        double[][] seg = new double[N][4];
        ArrayList<double[]> pts = new ArrayList<double[]>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 4; j++) seg[i][j] = Double.parseDouble(st.nextToken());
            pts.add(new double[]{seg[i][0], seg[i][1]});
            pts.add(new double[]{seg[i][2], seg[i][3]});
        }
        Collections.sort(pts, new Comparator<double[]>() {
            public int compare(double[] a, double[] b) {
                if (a[0] != b[0]) return Double.compare(a[0], b[0]);
                return Double.compare(a[1], b[1]);
            }
        });
        int P = pts.size();
        double[][] h = new double[2 * P][];
        int k = 0;
        for (int i = 0; i < P; i++) {
            while (k >= 2 && cross(h[k - 2], h[k - 1], pts.get(i)) <= 1e-6) k--;
            h[k++] = pts.get(i);
        }
        for (int i = P - 2, t = k + 1; i >= 0; i--) {
            while (k >= t && cross(h[k - 2], h[k - 1], pts.get(i)) <= 1e-6) k--;
            h[k++] = pts.get(i);
        }
        k--;
        double[][] hull = new double[k][];
        for (int i = 0; i < k; i++) hull[i] = h[i];

        ArrayList<double[]> req = new ArrayList<double[]>();
        for (int i = 0; i < N; i++) {
            if (isCorner(hull, seg[i][0], seg[i][1]) && isCorner(hull, seg[i][2], seg[i][3])) continue;
            req.add(seg[i]);
        }
        n = req.size();
        orig = new double[n][];
        for (int i = 0; i < n; i++) orig[i] = req.get(i);
        best = n;
        if (n == 0) { System.out.print(0); return; }

        double[][] img = new double[n][];
        for (int i = 0; i < n; i++) img[i] = orig[i].clone();
        Face f = new Face();
        f.a = 1; f.b = 0; f.c = 0; f.d = 1; f.tx = 0; f.ty = 0;
        f.poly = hull;
        ArrayList<Face> faces = new ArrayList<Face>();
        faces.add(f);
        int[] death0 = new int[n]; Arrays.fill(death0, INF);
        dfs(0, hull, img, faces, death0);
        System.out.print(best);
    }

    static boolean isCorner(double[][] hull, double x, double y) {
        for (double[] v : hull) if (Math.abs(v[0] - x) < 1e-3 && Math.abs(v[1] - y) < 1e-3) return true;
        return false;
    }

    static double cross(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }
}
