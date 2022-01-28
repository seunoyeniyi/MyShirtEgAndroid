package com.myshirt.eg.handler;

import java.text.DecimalFormat;

public class PriceFormatter {

    public static String format(String price) {
        DecimalFormat formatterDecimal = new DecimalFormat("#,###.00");
        DecimalFormat formatterWhole = new DecimalFormat("#,###");
        if (!price.isEmpty()) {
            double doublePrice = Double.parseDouble(price);
            //if is a whole number else decimal number
            if (doublePrice % 1 == 0) {
                return formatterWhole.format(doublePrice);
            } else {
                //if decimal start with 0.xxx
                if (price.substring(0, 2).equals("0."))
                    return "0" + formatterDecimal.format(doublePrice);
                else
                    return formatterDecimal.format(doublePrice);
            }
        } else {
            return "0";
        }
    }
    public static String format(Double price) {
        DecimalFormat formatterDecimal = new DecimalFormat("#,###.00");
        DecimalFormat formatterWhole = new DecimalFormat("#,###");

        //if is a whole number else decimal number
        if (price % 1 == 0) {
            return formatterWhole.format(price);
        } else {
            //if decimal start with 0.xxx
            if (String.valueOf(price).substring(0, 2).equals("0."))
                return "0" + formatterDecimal.format(price);
            else
                return formatterDecimal.format(price);
        }

    }
}
