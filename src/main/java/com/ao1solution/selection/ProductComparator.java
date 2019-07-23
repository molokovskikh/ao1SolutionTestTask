package com.ao1solution.selection;

import java.util.Objects;

/**
 * <b>Product comparator</b>
 * Created by <i><b>s.molokovskikh</i></b> on 22.07.19.
 */
public class ProductComparator implements java.util.Comparator<String> {

    public static final String COMMA_SEPARATOR = "\t";

    @Override
    public int compare(String line1, String line2) {
        int compareResult = compareProductsByPrice(line1, line2);
        compareResult = (compareResult != 0) ? compareResult : compareProductsById(line1, line1);
        return compareResult;
    }

    private int compareProductsByPrice(String line1, String line2) {
        int res = Float.compare(getProductPrice(line1), getProductPrice(line2));
        return res;
    }

    private float getProductPrice(String line) {
        String[] rowData = line.split(COMMA_SEPARATOR);
        float price = parsePrice(rowData[4]);
        return price;
    }

    private int compareProductsById(String line1, String line2) {
        String productId1 = getProductId(line1);
        String productId2 = getProductId(line2);
        return productId1.compareTo(productId2);
    }

    public static String getProductId(String line) {
        String[] rowData = line.split(COMMA_SEPARATOR);
        String productId = rowData[0];
        return productId;
    }

    private float parsePrice(String priceStr) {
        float res = 0;
        if (Objects.nonNull(priceStr))
            priceStr = priceStr.replace(',', '.');
        try {
            res = Float.parseFloat(priceStr);
        } catch (NumberFormatException e) {

        }
        return res;
    }
}
