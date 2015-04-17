package eu.earthobservatory.testsuite.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author efthymis
 */
public class TestFileLoader {
    private final static String TEST_PATH_TEMPLATE = "/%s/%s/";


    private final String testName;
    private final String testPackage;
    private File testFolder;
    private String datasetFile;
    private List<String> queries;
    private List<String> results;

    public TestFileLoader(Class<? extends TemplateTest> testClass, List<String> queries,
        List<String> results) {
        this.queries = queries;
        this.results = results;
        testName = testClass.getSimpleName();
        testPackage = testClass.getPackage().getName()
                .substring(this.getClass().getPackage().getName().lastIndexOf('.') + 1);
    }

    public void loadFolder() throws URISyntaxException {
        testFolder =
            new File(this.getClass()
                .getResource(String.format(TEST_PATH_TEMPLATE, testPackage, testName)).toURI());

        String[] files = testFolder.list();

        for (String file : files) {
            if (file.endsWith(".nt") || file.endsWith(".nq")) {
                datasetFile =
                    File.separator + testPackage + File.separator + testName + File.separator + file;
            } else if (file.endsWith(".rq")) {
                queries.add(
                    File.separator + testPackage + File.separator + testName + File.separator + file);
                results.add(
                    File.separator + testPackage + File.separator + testName + File.separator + file
                        .substring(0, file.length() - 3) + ".srx");
            }
        }
    }

    public String loadedDataSet() {
        return datasetFile;
    }
}
