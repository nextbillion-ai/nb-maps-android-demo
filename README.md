# Nextbillion.AI Android Maps SDK DEMO App
*notes: for troubleshooting and bugs reporting, please reach out to dan@nextbillion.ai*

## Installing
before running this project, make sure you have configured your own API Key `nbmap_access_key` at `res/values/strings.xml`


## Migrating
If you want to migrate any of the implementations to your own project, please make sure you have added the correct dependencies with latest or expected version number
### Maps SDK
implementation 'ai.nextbillion:nb-maps-android:3.3.1'

### Nextbillion.AI APIs
implementation 'ai.nextbillion:nb-kits-api:0.0.2'

## Build requirements

- JDK 17, Android SDK 34, and Android 6.0 (API 23) or newer.
- The project uses Gradle 8.14.3, Android Gradle Plugin 8.12.2, and Kotlin 2.2.20 to match Maps SDK 3.3.1.
- Configure `nbmap_access_key` in `app/src/main/res/values/strings.xml` before running.
- Build with `./gradlew :app:assembleDebug`.

## House numbers example

Open **Style > House numbers** in the examples list. The map opens over a residential street in Amsterdam at zoom 19 and displays real address labels from the selected provider.

- Switch between TomTom, TomTom Orbis, and MapTiler.
- Toggle house numbers, compare zoom 16 and 19, and cycle through Bright, Dark, and Satellite styles.
- Labels are enabled from zoom 17, subject to address coverage and label collision avoidance.
- The status line reports the number of house-number features actually rendered in the visible map.

The implementation is in `HouseNumberActivity.kt` and `styles/HouseNumberLayer.kt`. Runtime layers are restored after every style load, including automatic SDK reloads. TomTom uses `places.number` with category `address_point`; Orbis uses `address_points.number` with category `house`; MapTiler uses `housenumber.housenumber`. MapTiler hybrid styles use the `maptiler_planet` source instead of `openmaptiles`.

To add labels to an existing map, register a style-load listener before setting the style:

```kotlin
mapView.getMapAsync { map ->
    mapView.addOnDidFinishLoadingStyleListener {
        map.style?.let { style ->
            HouseNumberLayer.addTo(style, Nextbillion.getWellKnownTileServer())
        }
    }
    map.setStyle(Style.getPredefinedStyle("Bright"))
}
```
