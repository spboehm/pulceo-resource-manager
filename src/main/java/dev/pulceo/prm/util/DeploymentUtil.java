package dev.pulceo.prm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
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

}
