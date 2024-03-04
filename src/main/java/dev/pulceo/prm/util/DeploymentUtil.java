package dev.pulceo.prm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeploymentUtil {

    public static String templateBootStrapPnaScript(List<String> exportStatements) throws IOException {
        // TODO: validate export statements
        try (InputStream inputStream = DeploymentUtil.class.getResourceAsStream("/bootstrap-pna.sh");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String fileContentAsString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            String joinedString = fileContentAsString.replace("# {{ EXPORT_PNA_CREDENTIALS }}", String.join("\n", exportStatements));
            return Base64.getEncoder().encodeToString(joinedString.getBytes());
        } catch (IOException e) {
            throw new IOException("Error reading bootstrap-pna.sh", e);
        }
    }

    public static String createRandomName(String namePrefix) {
        String root = UUID.randomUUID().toString().replace("-", "");
        long millis = Calendar.getInstance().getTimeInMillis();
        long datePart = millis % 10000000L;
        return namePrefix + root.toLowerCase().substring(0, 3) + datePart;
    }

}
