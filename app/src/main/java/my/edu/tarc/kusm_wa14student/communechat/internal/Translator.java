package my.edu.tarc.kusm_wa14student.communechat.internal;

/**
 * Created by Xeosz on 28-Sep-17.
 */

public class Translator {
    //Turn Hex to ASCll For example : 31 turn to 1
    static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    //Turn ASCll to Hex For example : 1 turn to 31
    static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }
}
