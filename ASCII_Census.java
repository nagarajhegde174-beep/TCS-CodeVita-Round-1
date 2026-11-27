import java.util.*;
import java.io.*;

public class ASCII_Census {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append(' ');
        StringTokenizer st = new StringTokenizer(sb.toString());
        int R = Integer.parseInt(st.nextToken());
        int C = Integer.parseInt(st.nextToken());
        char[][] g = new char[R][];
        for (int i = 0; i < R; i++) g[i] = st.nextToken().toCharArray();

        int[][] hRun = new int[R][C + 1];
        int[][] vRun = new int[R + 1][C];
        for (int r = 0; r < R; r++)
            for (int c = C - 1; c >= 0; c--)
                hRun[r][c] = (g[r][c] == '-' || g[r][c] == '+') ? hRun[r][c + 1] + 1 : 0;
        for (int c = 0; c < C; c++)
            for (int r = R - 1; r >= 0; r--)
                vRun[r][c] = (g[r][c] == '|' || g[r][c] == '+') ? vRun[r + 1][c] + 1 : 0;

        List<int[]> rects = new ArrayList<int[]>();
        for (int r1 = 0; r1 < R; r1++) {
            for (int c1 = 0; c1 < C; c1++) {
                if (g[r1][c1] != '+') continue;
                int hmax = hRun[r1][c1];
                int vmax = vRun[r1][c1];
                for (int c2 = c1 + 1; c2 < c1 + hmax; c2++) {
                    if (g[r1][c2] != '+') continue;
                    int need = c2 - c1 + 1;
                    for (int r2 = r1 + 1; r2 < r1 + vmax; r2++) {
                        if (g[r2][c1] != '+' || g[r2][c2] != '+') continue;
                        if (hRun[r2][c1] < need) continue;
                        if (vRun[r1][c2] < r2 - r1 + 1) continue;
                        rects.add(new int[]{r1, c1, r2, c2});
                    }
                }
            }
        }

        int count = 0;
        for (int r = 0; r + 2 < R; r++) {
            for (int c = 1; c + 1 < C; c++) {
                if (g[r][c] != 'O') continue;
                if (g[r + 1][c - 1] != '/' || g[r + 1][c] != '|' || g[r + 1][c + 1] != '\\') continue;
                if (g[r + 2][c - 1] != '/' || g[r + 2][c + 1] != '\\') continue;
                for (int[] q : rects) {
                    if (q[0] < r && r + 2 < q[2] && q[1] < c - 1 && c + 1 < q[3]) {
                        count++;
                        break;
                    }
                }
            }
        }
        System.out.print(count);
    }
}
