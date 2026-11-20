package ai.nextbillion.styles

import android.graphics.Color
import ai.nextbillion.maps.core.Style
import ai.nextbillion.maps.style.NGLWellKnownTileServer
import ai.nextbillion.maps.style.expressions.Expression.all
import ai.nextbillion.maps.style.expressions.Expression.eq
import ai.nextbillion.maps.style.expressions.Expression.get
import ai.nextbillion.maps.style.expressions.Expression.has
import ai.nextbillion.maps.style.expressions.Expression.interpolate
import ai.nextbillion.maps.style.expressions.Expression.linear
import ai.nextbillion.maps.style.expressions.Expression.literal
import ai.nextbillion.maps.style.expressions.Expression.neq
import ai.nextbillion.maps.style.expressions.Expression.stop
import ai.nextbillion.maps.style.expressions.Expression.toString
import ai.nextbillion.maps.style.expressions.Expression.zoom
import ai.nextbillion.maps.style.layers.Property
import ai.nextbillion.maps.style.layers.PropertyFactory.textAllowOverlap
import ai.nextbillion.maps.style.layers.PropertyFactory.textColor
import ai.nextbillion.maps.style.layers.PropertyFactory.textField
import ai.nextbillion.maps.style.layers.PropertyFactory.textFont
import ai.nextbillion.maps.style.layers.PropertyFactory.textHaloColor
import ai.nextbillion.maps.style.layers.PropertyFactory.textHaloWidth
import ai.nextbillion.maps.style.layers.PropertyFactory.textIgnorePlacement
import ai.nextbillion.maps.style.layers.PropertyFactory.textPadding
import ai.nextbillion.maps.style.layers.PropertyFactory.textSize
import ai.nextbillion.maps.style.layers.PropertyFactory.visibility
import ai.nextbillion.maps.style.layers.SymbolLayer
import ai.nextbillion.maps.style.sources.VectorSource

/**
 * Builds a house-number text layer from the vector sources already provided by a base style.
 * Provider-specific source IDs, tile layers, and feature fields are kept in [Schema].
 */
object HouseNumberLayer {
    const val ID = "demo-house-numbers"
    // This is a rendering threshold; address availability still depends on provider coverage.
    const val MIN_ZOOM = 17f

    /**
     * Call on the map's UI thread after every style-load event, including SDK-initiated reloads.
     * Repeated calls for the same style reuse the existing symbol layer.
     *
     * Returns null if the style is not loaded, has no supported vector source, or the layer ID
     * is already used by another layer type. Callers can display an unavailable state in these cases.
     */
    fun addTo(style: Style, tileServer: NGLWellKnownTileServer): SymbolLayer? {
        if (!style.isFullyLoaded) return null
        style.getLayer(ID)?.let { return it as? SymbolLayer }
        // A style source ID identifies a tile source; sourceLayer identifies a layer inside
        // those tiles; field identifies the feature property containing the displayed number.
        val schema = when (tileServer) {
            NGLWellKnownTileServer.NGLMapTiler -> Schema(
                "openmaptiles", "housenumber", "housenumber",
                arrayOf("Roboto Medium", "Noto Sans Regular")
            )
            NGLWellKnownTileServer.NGLTomTomOrbis -> Schema(
                "vectorTiles", "address_points", "number", arrayOf("Noto-Regular"), "house"
            )
            // TomTom map=2 stores address points among other place features; filter by category.
            NGLWellKnownTileServer.NGLTomTom -> Schema(
                "vectorTiles", "places", "number", arrayOf("Noto-Regular"), "address_point"
            )
        }
        // MapTiler uses different source IDs in street/dark and hybrid styles. Reuse the
        // vector source present in the loaded style instead of creating another tile request.
        val sourceIds = if (tileServer == NGLWellKnownTileServer.NGLMapTiler) {
            listOf("openmaptiles", "maptiler_planet")
        } else {
            listOf(schema.source)
        }
        val sourceId = sourceIds.firstOrNull { style.getSource(it) is VectorSource } ?: return null

        // Hide known built-in house-number layers to avoid duplicate labels and let the
        // example's visibility switch control the custom labels.
        listOf("housenumber", "House Number").forEach { id ->
            style.getLayer(id)?.setProperties(visibility(Property.NONE))
        }
        val layer = SymbolLayer(ID, sourceId)
            .withSourceLayer(schema.sourceLayer)
            .withProperties(
                // Numbers can be encoded as strings or numeric properties in vector tiles.
                textField(toString(get(schema.field))),
                // Font names must match the glyphs supplied by this provider's base style.
                textFont(schema.fonts),
                textSize(interpolate(linear(), zoom(), stop(17, 11), stop(20, 16))),
                textColor(Color.rgb(55, 65, 81)),
                textHaloColor(Color.WHITE),
                textHaloWidth(1.2f),
                // Keep collision detection enabled so dense address labels remain readable.
                textAllowOverlap(false),
                textIgnorePlacement(false),
                textPadding(2f)
            )
        layer.minZoom = MIN_ZOOM
        // TomTom schemas also require a category match to exclude other place features.
        // All providers exclude missing or empty number fields before creating labels.
        layer.setFilter(
            if (schema.category == null) {
                all(has(schema.field), neq(toString(get(schema.field)), literal("")))
            } else {
                all(
                    eq(get("category"), literal(schema.category)),
                    has(schema.field),
                    neq(toString(get(schema.field)), literal(""))
                )
            }
        )
        style.addLayer(layer)
        return layer
    }

    /** Changes visibility without removing the layer or reloading the base style. */
    fun setVisible(style: Style, visible: Boolean) {
        if (style.isFullyLoaded) {
            style.getLayer(ID)?.setProperties(visibility(if (visible) Property.VISIBLE else Property.NONE))
        }
    }

    private data class Schema(
        val source: String,
        val sourceLayer: String,
        val field: String,
        val fonts: Array<String>,
        val category: String? = null
    )
}
