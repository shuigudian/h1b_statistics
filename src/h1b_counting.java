import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

class h1b_couting {
    public static void main(String[] args) {
        String input_name = args[0];
        final int topK = 10;
        int numCerified = 0;

        List<String> records = readFile(input_name);
        Map<String, Integer> header = new HashMap<>();

        String[] statistics = new String[] { "SOC_NAME", "WORKSITE_STATE" }; // default version
        String caseStatusKey = "CASE_STATUS";

        if (input_name.indexOf("2014") != -1) {
            statistics = new String[] { "LCA_CASE_SOC_NAME", "LCA_CASE_WORKLOC1_STATE" };
            caseStatusKey = "STATUS";
        }

        Map<String, String> outputMap = new HashMap<>();
        outputMap.put(statistics[0], "TOP_OCCUPATIONS");
        outputMap.put(statistics[1], "TOP_STATES");

        Map<String, String> outputFile = new HashMap<>();
        outputFile.put(statistics[0], args[1]);
        outputFile.put(statistics[1], args[2]);

        Map<String, Map<String, Integer>> map = new HashMap<>();
        for (String s : statistics) {
            map.put(s, new HashMap<>());
        }

        for (int i = 0; i < records.size(); ++i) {
            String[] fields = records.get(i).split(";");

            if (i == 0) {
                for (int j = 0; j < fields.length; ++j) {
                    header.put(fields[j], j);
                }
            } else {
                String caseStatus = fields[header.get(caseStatusKey)];

                if (caseStatus.equals("CERTIFIED")) {
                    numCerified += 1;
                    for (String s : statistics) {
                        String key = fields[header.get(s)];
                        if (!key.isEmpty()) {
                            if (key.charAt(0) == '\"' && key.length() > 2) {
                                key = key.substring(1, key.length() - 1);
                            }
                            map.get(s).put(key, map.get(s).getOrDefault(key, 0) + 1);
                        }
                    }
                }
            }
        }

        for (String s : statistics) {
            PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>(
                    (o1, o2) -> Integer.compare(o1.getValue(), o2.getValue()) == 0 ? o1.getKey().compareTo(o2.getKey())
                            : Integer.compare(o2.getValue(), o1.getValue()));

            for (Map.Entry<String, Integer> item : map.get(s).entrySet()) {
                maxHeap.add(item);
            }

            String filename = outputFile.get(s);
            DecimalFormat df = new DecimalFormat("0.0");

            try {
                File fout = new File(filename);
                FileOutputStream fos = new FileOutputStream(fout);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                String headerString = outputMap.get(s) + ";NUMBER_CERTIFIED_APPLICATIONS;PERCENTAGE";
                bw.write(headerString);
                bw.newLine();

                for (int k = 0; k < topK && !maxHeap.isEmpty(); ++k) {
                    String key = maxHeap.remove().getKey();
                    int num = map.get(s).get(key);
                    double percentage = num / (double) numCerified * 100;
                    String output = key + ";" + num + ";" + df.format(percentage) + "%";
                    bw.write(output);
                    bw.newLine();
                }
                bw.close();
            } catch (Exception e) {
                System.err.format("Exception occurred trying to read '%s'.", filename);
                e.printStackTrace();
            }
        }
    }

    private static List<String> readFile(String filename) {
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
            return records;
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }
}