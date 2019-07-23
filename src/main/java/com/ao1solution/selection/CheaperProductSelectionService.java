package com.ao1solution.selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <b>Cheaper product selection service</b>
 * Created by <i><b>s.molokovskikh</i></b> on 22.07.19.
 */
@Service
public class CheaperProductSelectionService {

    private final Logger logger = LoggerFactory.getLogger(CheaperProductSelectionService.class);

    @Value("${app.same-product-id-retry-limit:20}")
    private Integer sameProductIdRetryLimit;

    @Value("${app.records-total:1000}")
    private Integer recordsTotal;

    private final ProductComparator productComparator = new ProductComparator();

    public void DoWork(String csvFilesDir, String resultCsvFile) {

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            List<Callable<Collection<String>>> sortTasksList = Files.list(Paths.get(csvFilesDir))
                    .map(path -> handleCSVFile(path))
                    .collect(Collectors.toList());

            List<Future<Collection<String>>> sortedProductLists = forkJoinPool.invokeAll(sortTasksList);

            List<String> allSortedProducts = getAllSortedProducts(sortedProductLists);

            Stream<String> finalProductsList = allSortedProducts.stream()
                    .sorted(productComparator)
                    .filter(getSameProductIdRetryLimitFilter())
                    .limit(recordsTotal);

            writeResult(finalProductsList, resultCsvFile);

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.debug(e.getMessage());
        }

    }

    private Callable<Collection<String>> handleCSVFile(Path pathCSVFile) {
        return () -> {
            List<String> list = Files.lines(pathCSVFile)
                    .parallel()
                    .sorted(productComparator)
                    .distinct()
                    .limit(recordsTotal)
                    .collect(Collectors.toList());
            logger.debug("File handled '{}'", pathCSVFile);
            return list;
        };
    }

    private Predicate<? super String> getSameProductIdRetryLimitFilter() {

        Map<String, Integer> sameProductIdRetryLimitMap = new HashMap<>();

        Predicate<String> sameProductIdRetryLimitFilter = line -> {
            String productId = ProductComparator.getProductId(line);
            Integer count = sameProductIdRetryLimitMap.get(productId);
            count = Objects.isNull(count) ? 1 : count + 1;
            if (count > sameProductIdRetryLimit) {
                return false;
            }
            sameProductIdRetryLimitMap.put(productId, count);
            return true;
        };
        return sameProductIdRetryLimitFilter;
    }

    private List<String> getAllSortedProducts(List<Future<Collection<String>>> sortedProductLists)
            throws ExecutionException, InterruptedException {
        List<String> result = new ArrayList<>();
        for (Future<Collection<String>> sortedProduct : sortedProductLists) {
            Collection<String> productsPart = sortedProduct.get();
            result.addAll(productsPart);
        }
        return result;
    }

    private void writeResult(Stream<String> finalProductsList, String resultCsvFile) throws IOException {

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(resultCsvFile))) {
            finalProductsList
                    .forEach(product -> {
                        try {
                            bufferedWriter.write(product);
                            bufferedWriter.newLine();
                        } catch (IOException e) {
                            logger.debug(e.getMessage());
                        }
                    });
        }
    }




    void setSameProductIdRetryLimit(Integer sameProductIdRetryLimit) {
        this.sameProductIdRetryLimit = sameProductIdRetryLimit;
    }

    void setRecordsTotal(Integer recordsTotal) {
        this.recordsTotal = recordsTotal;
    }
}
