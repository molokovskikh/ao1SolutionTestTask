package com.ao1solution.selection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ao1solution.Application.APP_NAME;

/**
 * <b>Cheaper product selection service test</b>
 * Created by <i><b>s.molokovskikh</i></b> on 22.07.19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CheaperProductSelectionService.class, TestDataGeneratorHelper.class})
public class CheaperProductSelectionServiceTest {

    public static final int RECORDS_TOTAL = 1000;
    public static final int SAME_PRODUCT_ID_RETRY_LIMIT = 20;

    private Path csvFilesDir;
    private Path resultCsvFile;

    @Autowired
    private CheaperProductSelectionService cheaperProductSelectionService;

    @Autowired
    private TestDataGeneratorHelper testDataGeneratorHelper;

    @Before
    public void setUp() throws Exception {
        csvFilesDir = Files.createTempDirectory(APP_NAME);
        resultCsvFile = Files.createTempFile(APP_NAME, ".res");
        testDataGeneratorHelper.setSameProductIdRetryLimit(SAME_PRODUCT_ID_RETRY_LIMIT*2);
        testDataGeneratorHelper.generateTestCsvFiles(csvFilesDir);
    }

    @After
    public void tearDown() throws Exception {
        testDataGeneratorHelper.clearCsvFilesDir(csvFilesDir);
        testDataGeneratorHelper.clearCsvFilesDir(resultCsvFile);
    }


    @Test
    public void doWork() throws IOException {

        cheaperProductSelectionService.setRecordsTotal(RECORDS_TOTAL);
        cheaperProductSelectionService.setSameProductIdRetryLimit(SAME_PRODUCT_ID_RETRY_LIMIT);

        cheaperProductSelectionService.DoWork(csvFilesDir.toString(), resultCsvFile.toString());

        long realLineCount = Files.lines(resultCsvFile).count();
        Assert.assertEquals(RECORDS_TOTAL, realLineCount);

        Map<String, Long> productIdGroups = Files.lines(resultCsvFile)
                .map(line -> ProductComparator.getProductId(line))
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));

        for (String productId : productIdGroups.keySet()) {
            Long count = productIdGroups.get(productId);
            Assert.assertTrue("ProductId '" + productId + "'" +
                    " found in the result file given more than "+SAME_PRODUCT_ID_RETRY_LIMIT+" time ("+count+")",
                    count <= SAME_PRODUCT_ID_RETRY_LIMIT);
        }
    }

}
