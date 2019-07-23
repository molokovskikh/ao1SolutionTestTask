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

/**
 * <b>Cheaper product selection service</b>
 * Created by <i><b>s.molokovskikh</i></b> on 22.07.19.
 */
@Service
public class CheaperProductSelectionService {

    Logger logger = LoggerFactory.getLogger(CheaperProductSelectionService.class);

    private final List<String> topProducts = new ArrayList();

    @Value("${app.same-product-id-retry-limit:20}")
    private Integer sameProductIdRetryLimit;

    @Value("${app.records-total:1000}")
    private Integer recordsTotal;


    public void DoWork(String csvFilesDir, String resultCsvFile) {

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            List<Callable<Collection<String>>> invokerList = Files.list(Paths.get(csvFilesDir))
                    .map(path -> handleCSVFile(path))
                    .collect(Collectors.toList());

            List<Future<Collection<String>>> sortedProductLists = forkJoinPool.invokeAll(invokerList);

            sortedProductLists.forEach(collectionFuture -> {
                try {
                    Collection<String> productsPart = collectionFuture.get();
                    topProducts.addAll(productsPart);
                } catch (ExecutionException | InterruptedException e) {
                    logger.debug(e.getMessage());
                }
            });


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

            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(resultCsvFile))) {
                topProducts.stream()
                        .sorted(new ProductComparator())
                        .filter(sameProductIdRetryLimitFilter)
                        .limit(recordsTotal)
                        .forEach(product -> {
                            try {
                                bufferedWriter.write(product);
                                bufferedWriter.newLine();
                            } catch (IOException e) {
                                logger.debug(e.getMessage());
                            }
                        });
            }

        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }


    private Callable<Collection<String>> handleCSVFile(Path pathCSVFile) {
        return () -> {
            List<String> list = Files.lines(pathCSVFile)
                    .parallel()
                    .sorted(new ProductComparator())
                    .distinct()
                    .limit(recordsTotal)
                    .collect(Collectors.toList());
            logger.debug("File handled '{}'", pathCSVFile);
            return list;
        };
    }

    void setSameProductIdRetryLimit(Integer sameProductIdRetryLimit) {
        this.sameProductIdRetryLimit = sameProductIdRetryLimit;
    }

    void setRecordsTotal(Integer recordsTotal) {
        this.recordsTotal = recordsTotal;
    }
}
