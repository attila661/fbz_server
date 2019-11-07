package com.mdb;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import static com.mdb.ServerMain.workdir;
import static com.mdb.Utilities.readFileToStringHun;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;

@Slf4j
public class HandlerV2Ping implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = h.getRequestBody();
            byte[] buf = new byte[4096];
            for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }
            RandomAccessFile f;
            f = new RandomAccessFile("v2ping-post", "rw");
            f.write(out.toByteArray());
            f.close();

            writeStringToFile(new File("v2ping-head"),
                              h.getRequestHeaders()
                                  .entrySet()
                                  .stream()
                                  .map(
                                      e -> e.getKey() + ": " + e.getValue().stream().collect(Collectors.joining(" , ")))
                                  .collect(Collectors.joining("\n"))
            );

            String q = h.getRequestURI().getQuery();
            JSONObject j = new JSONObject();
            switch (q) {
                case "1":
                    log.info("String constant from java code:");
                    dump("Kódszám");
                    break;
                case "2":
                    log.info("String constant from cikk.inf file, readFileToString:");
                    dump(readFileToString(new File(workdir + "/" + "cikk.inf")).split("\n")[31].substring(22, 29));
                    break;
                case "3":
                    log.info("String constant from java code to JSON and toString:");
                    j.put("key", "Kódszám");
                    dump(j.toString());
                    break;
                case "4":
                    log.info("String constant from cikk.inf file to JSON and toString:");
                    j.put("key",
                          readFileToString(new File(workdir + "/" + "cikk.inf")).split("\n")[31].substring(22, 29));
                    dump(j.toString());
                    break;
                case "5":
                    log.info("String constant from cikk.inf file, BufferedReader:");
                    BufferedReader reader = new BufferedReader(new FileReader(workdir + "/" + "cikk.inf"));
                    String line = "";
                    int n = 0;
                    while ((line = reader.readLine()) != null) {
                        if (n == 31) {
                            break;
                        }
                        n++;
                    }
                    reader.close();
                    dump(line.substring(22, 29));
                    break;
                case "6":
                    log.info("String constant from cikk.inf file, readFileToStringHun:");
                    dump(readFileToStringHun(workdir + "/" + "cikk.inf").split("\n")[31].substring(22, 29));
                    break;
                case "7":
                    log.info("String constant from chr file, readFileToStringHun:");
                    dump(readFileToStringHun(workdir + "/" + "chr"));
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder out = new StringBuilder();
        out.append("{\n"
                   + "  \"server\": \"fbz_server\",\n"
                   + "  \"charset\": \"áÁéÉíÍóÓöÖőŐúÚüÜűŰ\"\n"
                   + "}\n");

//        RandomAccessFile f = new RandomAccessFile("devdata/chr", "r");
//        int l = (int) f.length();
//        byte[] data = new byte[l + 1];
//        f.read(data, 0, l);
//        f.close();
//        ByteBuffer bb = ByteBuffer.wrap(data);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//        String o = IntStream.range(0, 18)
//            .mapToObj(i -> "case " +
//                           (
//                               bb.getChar(i * 2) + 0
//                           ) +
//                           ": c='" +
//                           out.toString().substring(42, 60).substring(i, i + 1) + "';break;"
//            )
//            .collect(Collectors.joining("\n"));
//        System.out.println(o);

        new ResponseSender(h, out, 200);
    }

    private void dump(String s) throws UnsupportedEncodingException {
        log.info(s);
        byte[] b = s.getBytes();
        for (int i = 0; i < b.length; i++) {
            log.info(String.format("b %3d %3d", i, b[i] & 0xFF));
        }
        byte[] u = s.getBytes("UTF-16");
        for (int i = 0; i < u.length; i++) {
            log.info(String.format("u %3d %3d", i, u[i] & 0xFF));
        }
    }

}
