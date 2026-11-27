import java.io.*;

public class Rainy_Campus {
    static DataInputStream in;
    static byte[] buf = new byte[1 << 16];
    static int bufLen = 0, bufPtr = 0;

    static int readByte() throws IOException {
        if (bufPtr == bufLen) {
            bufLen = in.read(buf, 0, buf.length);
            bufPtr = 0;
            if (bufLen <= 0) return -1;
        }
        return buf[bufPtr++];
    }

    static int nextInt() throws IOException {
        int c = readByte();
        while (c != -1 && (c < '0' || c > '9')) c = readByte();
        int r = 0;
        while (c >= '0' && c <= '9') {
            r = r * 10 + (c - '0');
            c = readByte();
        }
        return r;
    }

    public static void main(String[] args) throws IOException {
        in = new DataInputStream(new BufferedInputStream(System.in, 1 << 16));
        int N = nextInt(), P = nextInt(), M = nextInt();

        int T = Math.max(M - 1, 1);
        int[][] dep = new int[T][N + 1];
        int[][] arr = new int[T][N + 1];

        for (int p = 0; p < P; p++) {
            int prev = 0;
            for (int j = 0; j < M; j++) {
                int b = nextInt();
                if (b == 0) continue;
                if (prev != 0 && prev != b) {
                    dep[j - 1][prev]++;
                    arr[j - 1][b]++;
                }
                prev = b;
            }
        }

        long ans = 0;
        for (int b = 1; b <= N; b++) {
            int D = 0, A = 0, best = 0;
            for (int t = 0; t < M - 1; t++) {
                D += dep[t][b];
                int val = D - A;
                if (val > best) best = val;
                A += arr[t][b];
            }
            ans += best;
        }
        System.out.print(ans);
    }
}