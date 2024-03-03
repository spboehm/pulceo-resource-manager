package dev.pulceo.prm.util;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeploymentUtilTests {

    @Test
    public void testReadBootstrapPNA() throws IOException {
        // given
        List<String> exportStatements = new ArrayList<>();
        exportStatements.add("export 1=1");
        exportStatements.add("export 2=2");
        exportStatements.add("export 3=3");

        // when
        String result = DeploymentUtil.templateBootStrapPnaScript(exportStatements);

        // then


    }

}
