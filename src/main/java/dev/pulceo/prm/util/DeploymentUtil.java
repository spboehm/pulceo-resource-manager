package dev.pulceo.prm.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public class DeploymentUtil {

    public static String templateBootStrapPnaScript(List<String> exportStatements) throws IOException {
        // TODO: validate export statements
        String fileContentAsString = new String(Files.readAllBytes(Path.of("src/main/resources/bootstrap-pna.sh")));
        String joinedString = fileContentAsString.replace("# {{ EXPORT_PNA_CREDENTIALS }}", String.join("\n", exportStatements));
        return Base64.getEncoder().encodeToString(joinedString.getBytes());
    }

}
