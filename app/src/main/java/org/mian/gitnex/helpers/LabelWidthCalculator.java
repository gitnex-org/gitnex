package org.mian.gitnex.helpers;

/**
 * Author M M Arif
 */

public class LabelWidthCalculator {

    public static int customWidth(int labelLength) {

        int width = 33;

        if(labelLength == 20) {
            width = ((width * labelLength) - 150);
        }
        else if(labelLength == 19) {
            width = ((width * labelLength) - 140);
        }
        else if(labelLength == 18) {
            width = ((width * labelLength) - 130);
        }
        else if(labelLength == 17) {
            width = ((width * labelLength) - 120);
        }
        else if(labelLength == 16) {
            width = ((width * labelLength) - 110);
        }
        else if(labelLength == 15) {
            width = ((width * labelLength) - 100);
        }
        else if(labelLength == 14) {
            width = ((width * labelLength) - 90);
        }
        else if(labelLength == 13) {
            width = ((width * labelLength) - 80);
        }
        else if(labelLength == 12) {
            width = ((width * labelLength) - 70);
        }
        else if(labelLength == 11) {
            width = ((width * labelLength) - 60);
        }
        else if(labelLength == 10) {
            width = ((width * labelLength) - 50);
        }
        else if(labelLength == 9) {
            width = ((width * labelLength) - 40);
        }
        else if(labelLength == 8) {
            width = ((width * labelLength) - 30);
        }
        else if(labelLength == 7) {
            width = ((width * labelLength) - 20);
        }
        else if(labelLength == 6) {
            width = ((width * labelLength) - 10);
        }
        else if(labelLength == 5) {
            width = ((width * labelLength) - 10);
        }
        else if(labelLength == 4) {
            width = ((width * labelLength) - 10);
        }
        else if(labelLength == 3) {
            width = ((width * labelLength) - 10);
        }
        else if(labelLength == 2) {
            width = ((width * labelLength));
        }
        else {
            width = (width * labelLength - 5);
        }

        return width;

    }

}
