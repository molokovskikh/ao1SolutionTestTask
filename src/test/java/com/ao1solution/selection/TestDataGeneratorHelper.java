package com.ao1solution.selection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ao1solution.Application.APP_NAME;
import static com.ao1solution.selection.ProductComparator.COMMA_SEPARATOR;

/**
 * <b>Test data generator</b>
 * Created by <i><b>s.molokovskikh</i></b> on 22.07.19.
 */
public class TestDataGeneratorHelper {


    //Short time test
    static final int ROWS_NUMBER = 10000;
    static final int FILES_NUMBER = 100;

    /*
    //Long time test
    static final int ROWS_NUMBER = 1000000;
    static final int FILES_NUMBER = 100000;
     */

    public static final float MIN_PRICE = 0.01f;

    private Queue<Integer> sameProductIdQueue = new ArrayDeque<>();
    private Queue<Float> sameProductPriceQueue = new ArrayDeque<>();

    private final Random random = new Random();

    public TestDataGeneratorHelper() {
    }

    void generateTestCsvFiles(Path csvFilesDir) throws IOException, InterruptedException {
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < FILES_NUMBER; i++) {
            tasks.add(() -> {
                Path tempFile = Files.createTempFile(csvFilesDir, APP_NAME, ".csv");
                generateTestCsvFile(tempFile);
                return null;
            });
        }
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        executorService.invokeAll(tasks);
    }

    private void generateTestCsvFile(Path tempFile) {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile)) {
            int k = random.nextInt(3) + 1;
            for (int i = 0; i < k * ROWS_NUMBER; i++) {
                bufferedWriter.write(generateTestProduct());
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateTestProduct() {

        float k = random.nextBoolean() ? 10 : 1;
        float price = random.nextFloat();

        price = price == 0 ? MIN_PRICE : price;
        price = price * 100 * k;

        int id = generateId();

        if (!sameProductPriceQueue.isEmpty())
            price = sameProductPriceQueue.poll();

        if (!sameProductIdQueue.isEmpty())
            id = sameProductIdQueue.poll();

        String name = String.format("Name%s", id);
        String condition = String.format("Condition%s", id);
        String state = String.format("State%s", id);

        String product = new StringBuilder()
                .append(id).append(COMMA_SEPARATOR)
                .append(name).append(COMMA_SEPARATOR)
                .append(condition).append(COMMA_SEPARATOR)
                .append(state).append(COMMA_SEPARATOR)
                .append(String.format("%.2f", price).replaceAll(",", "."))
                .toString();

        return product;
    }

    private int generateId() {
        int res = 0;
        while (res == 0)
            res = random.nextInt(10000);
        return res;
    }

    void setSameProductIdRetryLimit(int sameProductIdRetryLimit) {

        sameProductIdQueue.clear();
        sameProductPriceQueue.clear();

        int productId = generateId();
        float productPrice = MIN_PRICE;

        for (int i = 0; i < sameProductIdRetryLimit; i++) {
            sameProductIdQueue.add(productId);
            sameProductPriceQueue.add(productPrice);
            productPrice += MIN_PRICE;
        }

    }

    void clearCsvFilesDir(Path csvFilesDir) throws IOException {
        Files.walkFileTree(csvFilesDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return super.visitFile(file, attrs);
            }
        });
        Files.deleteIfExists(csvFilesDir);
    }
}
