package de.einfachesache.proxymanager.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MOTDUtils {

    // Breiten aller Zeichen in Minecraft-Standardfont (ohne Bold; Quelle: Community-Docs)
    private static final Map<Character, Integer> CHAR_WIDTHS = new HashMap<>();
    private static final int centerPx = 147;

    static {
        String letters =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +    // 26
                "abcdefghijklmnopqrstuvwxyz" +    // 26
                "0123456789" +                    // 10
                ".,'\"!<>[]{}()§█_? " ;// 18 Sonderzeichen inkl. Space

        int[] widths = {
                // A–Z (26)
                5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
                // a–z (26)
                5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
                // 0–9 (10)
                4,5,1,1,1,3,3,4,4,3,
                // Sonderzeichen inkl. Space (18)
                // .  ,  '  "  !  <  >  [ ]  {  }  (  )  §  █  _  ?  (Space)
                3, 3, 5, 4, 4, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,    4
        };

        for (int i = 0; i < letters.length(); i++) {
            CHAR_WIDTHS.put(letters.charAt(i), widths[i]);
        }
    }

    public static String getCenteredMessage(String message) {
        boolean bold = false;
        int messagePxSize = 0;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == '§' && i + 1 < message.length()) {
                char code = message.charAt(++i);
                bold = (code == 'l' || code == 'L');
                continue;
            }
            int charWidth = CHAR_WIDTHS.getOrDefault(c, CHAR_WIDTHS.get(' '));
            messagePxSize += charWidth + 1 + (bold ? 1 : 0);
        }
        int toCompensate = centerPx - (messagePxSize / 2);
        int spaceWidth = CHAR_WIDTHS.get(' ') + 1; // 4px + 1px Abstand
        int spaceCount = Math.max(0, toCompensate / spaceWidth);
        return String.join("", Collections.nCopies(spaceCount, " ")) + message;
    }
}
