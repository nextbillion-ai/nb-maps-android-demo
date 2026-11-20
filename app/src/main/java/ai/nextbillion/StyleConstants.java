package ai.nextbillion;

import ai.nextbillion.maps.core.Style;

/**
 * Resolves predefined styles for the active tile server and lets the SDK apply authentication.
 * Resolve on each call: a cached URL could still point to the previous provider after a switch.
 */
public final class StyleConstants {
    private StyleConstants() { }

    public static String getBright() {
        return Style.getPredefinedStyle("Bright");
    }

    public static String getDark() {
        return Style.getPredefinedStyle("Dark");
    }

    public static String getSatellite() {
        return Style.getPredefinedStyle("Satellite");
    }
}
