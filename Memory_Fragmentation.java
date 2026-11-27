import java.util.*;
import java.io.*;

public class Memory_Fragmentation {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder all = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            all.append(line).append(' ');
        }
        StringTokenizer st = new StringTokenizer(all.toString());

        int N = Integer.parseInt(st.nextToken());
        int Q = Integer.parseInt(st.nextToken());

        int[] mem = new int[N];            // 0 = free, otherwise process ID
        int[] start = new int[201];        // start index (0-based) per ID
        int[] len = new int[201];          // current length; > 0 means active
        boolean[] dead = new boolean[201]; // released everything, can't be reused
        StringBuilder out = new StringBuilder();

        for (int q = 0; q < Q; q++) {
            String op = st.nextToken();

            if (op.equals("ALLOCATE")) {
                int id = Integer.parseInt(st.nextToken());
                int size = Integer.parseInt(st.nextToken());
                if (dead[id]) continue;

                if (len[id] > 0) {                       // extend in place
                    int s = start[id] + len[id];
                    int e = s + size;
                    if (e <= N) {
                        boolean ok = true;
                        for (int k = s; k < e; k++) {
                            if (mem[k] != 0) { ok = false; break; }
                        }
                        if (ok) {
                            for (int k = s; k < e; k++) mem[k] = id;
                            len[id] += size;
                        }
                    }
                } else {                                 // new process: First Fit
                    int i = 0;
                    while (i < N) {
                        if (mem[i] == 0) {
                            int j = i;
                            while (j < N && mem[j] == 0) j++;
                            if (j - i >= size) {
                                for (int k = i; k < i + size; k++) mem[k] = id;
                                start[id] = i;
                                len[id] = size;
                                break;
                            }
                            i = j;
                        } else {
                            i++;
                        }
                    }
                }

            } else if (op.equals("FREE")) {
                int id = Integer.parseInt(st.nextToken());
                int size = Integer.parseInt(st.nextToken());
                if (len[id] == 0 || size > len[id]) continue;

                int end = start[id] + len[id];
                for (int k = end - size; k < end; k++) mem[k] = 0;
                len[id] -= size;
                if (len[id] == 0) dead[id] = true;

            } else if (op.equals("COMPACT")) {
                List<Integer> order = new ArrayList<Integer>();
                for (int id = 1; id <= 200; id++) {
                    if (len[id] > 0) order.add(id);
                }
                final int[] st0 = start;
                Collections.sort(order, new Comparator<Integer>() {
                    public int compare(Integer x, Integer y) {
                        return st0[x] - st0[y];
                    }
                });
                Arrays.fill(mem, 0);
                int pos = 0;
                for (int id : order) {
                    start[id] = pos;
                    for (int k = pos; k < pos + len[id]; k++) mem[k] = id;
                    pos += len[id];
                }

            } else { // REPORT
                int total = 0, largest = 0, blocks = 0;
                int i = 0;
                while (i < N) {
                    if (mem[i] == 0) {
                        int j = i;
                        while (j < N && mem[j] == 0) j++;
                        int sz = j - i;
                        total += sz;
                        blocks++;
                        if (sz > largest) largest = sz;
                        i = j;
                    } else {
                        i++;
                    }
                }
                out.append(total).append(' ').append(largest).append(' ').append(blocks).append('\n');
            }
        }
        System.out.print(out.toString().trim());
    }
}