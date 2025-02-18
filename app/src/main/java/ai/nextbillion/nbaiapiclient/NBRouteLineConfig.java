package ai.nextbillion.nbaiapiclient;

import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import ai.nextbillion.R;
import ai.nextbillion.maps.style.layers.Property;

public class NBRouteLineConfig {

    private final float lineWidth;
    private final String routeName;
    private final String lineColor;
    private final String lineCap;
    private final String lineJoin;
    @DrawableRes
    private final int originalIcon;
    @DrawableRes
    private final int destinationIcon;

    public NBRouteLineConfig(float lineWidth, String routeName, String lineColor, String lineCap, String lineJoin, int originalIcon, int destinationIcon) {
        this.lineWidth = lineWidth;
        this.routeName = routeName;
        this.lineColor = lineColor;
        this.lineCap = lineCap;
        this.lineJoin = lineJoin;
        this.originalIcon = originalIcon;
        this.destinationIcon = destinationIcon;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public String getLineColor() {
        return lineColor;
    }

    public String getLineCap() {
        return lineCap;
    }

    public String getLineJoin() {
        return lineJoin;
    }

    public int getOriginalIcon() {
        return originalIcon;
    }

    public int getDestinationIcon() {
        return destinationIcon;
    }

    public String getRouteName() {
        return routeName;
    }

    public static class Builder {
        private String routeName;
        private float lineWidth;
        private String lineColor;
        private String lineCap;
        private String lineJoin;
        @DrawableRes
        private int originalIcon = -1;
        @DrawableRes
        private int destinationIcon = -1;

        public Builder setRouteName(String routeName) {
            this.routeName = routeName;
            return this;
        }

        public Builder setLineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder setLineColor(String lineColor) {
            this.lineColor = lineColor;
            return this;
        }

        public Builder setLineColor(@ColorInt int lineColor) {
            if (lineColor < 0x1000000) {
                this.lineColor = String.format("#%06X", (0xFFFFFF & lineColor));
            } else {
                this.lineColor = String.format("#%08X", lineColor);
            }
            return this;
        }

        public Builder setLineCap(String lineCap) {
            this.lineCap = lineCap;
            return this;
        }

        public Builder setLineJoin(String lineJoin) {
            this.lineJoin = lineJoin;
            return this;
        }

        public Builder setOriginIcon(@DrawableRes int resId) {
            originalIcon = resId;
            return this;
        }

        public Builder setDestinationIcon(@DrawableRes int resId) {
            destinationIcon = resId;
            return this;
        }

        public NBRouteLineConfig build() {
            if (TextUtils.isEmpty(routeName)) {
                throw new RuntimeException("RouteName can not be null");
            }
            if (TextUtils.isEmpty(lineColor)) {
                lineColor = "#f54242";
            }
            if (TextUtils.isEmpty(lineCap)) {
                lineCap = Property.LINE_CAP_ROUND;
            }
            if (TextUtils.isEmpty(lineJoin)) {
                lineJoin = Property.LINE_JOIN_ROUND;
            }

            if (lineWidth < 0.01f) {
                lineWidth = 3.0f;
            }

            if (originalIcon == -1) {
                originalIcon = ai.nextbillion.maps.R.drawable.ic_route_origin;
            }

            if (destinationIcon == -1) {
                destinationIcon = ai.nextbillion.maps.R.drawable.nbmap_marker_icon_default;
            }

            return new NBRouteLineConfig(lineWidth, routeName, lineColor, lineCap, lineJoin, originalIcon, destinationIcon);
        }
    }
}
