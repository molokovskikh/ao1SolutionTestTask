package com.ao1solution;

import com.ao1solution.selection.CheaperProductSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements ApplicationRunner {

    public static final String APP_NAME = "csv-selection";
    private static final String CSV_FILES_DIR = "dir";
    private static final String RESULT_CSV_FILE = "output";
    private static final String DISPLAY_HELP = "Will great if you point dir with CSV files. \n\t" +
            "Usage: " + APP_NAME +
            " --" + CSV_FILES_DIR + " fullpathtocsvfilesdir" +
            " --" + RESULT_CSV_FILE + " resultcsvfile";


    @Autowired
    private CheaperProductSelectionService cheaperProductSelectionService;

    public static void main(String... args) {
        SpringApplication.run(Application.class,args);
    }

    @Override
    public void run(ApplicationArguments args) {

        if (!args.containsOption(CSV_FILES_DIR)) {
            System.out.println(DISPLAY_HELP);
            return;
        }

        String csvFilesDir = args.getOptionValues(CSV_FILES_DIR).get(0);
        String resultCsvFile = args.getOptionValues(RESULT_CSV_FILE).get(0);
        cheaperProductSelectionService.DoWork(csvFilesDir, resultCsvFile);

    }
}
