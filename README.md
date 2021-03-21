# NfcIntentProxy

**WARNING:** This app is currently a prototype. It does only the minimal stuff and no edge cases / unaccounted for scenarios are handled. Use at your own risk.

## What is this?

**TL;DR:** This enables using existing supported read-only NFC tags as regular tags for automation with Home Assistant companion on Android.

Slightly longer version:

Home Assistant's [Tags](https://www.home-assistant.io/integrations/tag/) support is awesome, but requires writable NFC tags. This little app enables the use of read-only NFC tags for the same purpose.
Currently only tags that contain a specific URL (that no one probably cares about) are supported. That is so the app doesn't _eat_ intents that are meant for other apps / the browser.

## Supported tags
- Philips Sonicare brush heads with NFC chips that publish these URLs:
    - `https://www.philips.com/nfcbrushheadtap`
    - `https://www.philips.com`

## Why?

Because recycling existing read-only NFC tags is awesome. It doesn't matter that writable tags are cheap.

Also, I don't have any writable NFC tags and ordering a whole package just for those didn't sound good. When I was throwing out my old toothbrush head and tearing it apart to separate the materials, I came upon the idea for this small thing.

Also, I have no prior experience with mobile / Android / Kotlin development or NFC so I thought this would be a fun one-day project. -- René

## How do I use it?

1. Your Home Assistant Android companion version must be at least `2021.2.1`.
2. Install this app. Compile yourself or use an `.apk` from the _Releases_ page.
3. Go to your HASS companion app settings and in sensors, in [_Last Update Sensor_](https://companion.home-assistant.io/docs/core/sensors/#last-update-trigger-sensor) settings, add the intent `cz.renekliment.nfcintentproxy.TAG_READ`.
4. Restart your HASS companion app.
5. Now after you scan one of the supported NFC tags with your unlocked phone, the proper `android.intent_received` event should be received in your Home Assistant instance. You might have to select the app in a pop-up when multiple apps register the same kind of NDEF (URL). You can select to always use this app to handle these intents and then the pop-up should not appear anymore. Selecting to always use this app is safe because it only registers for specific URLs and you can always reset it in your phone settings. You can subscribe to the event in _Developer Settings / Events_ for debug. The event will have the `tag_id` attribute that you can use to identify the tag. You can use this event directly in your automations.

## Integrating with Tags in Home Assistant if you like things fancy

If you would like your tags to appear in _Configuration/Tags_ and create automations with them,
you can follow these steps:
 
**For each tag:**
1. Find out the tag ID from the `android.intent_received` event.
2. Create a tag manually with that ID.

**Once:**
Create an automation that creates a proper `tag_scanned` event from the `android.intent_received` event:

Trigger:
```yaml
platform: event
event_type: android.intent_received
event_data:
  intent: cz.renekliment.nfcintentproxy.TAG_READ
```
    
Action:
```yaml
event: tag_scanned
event_data:
  tag_id: '{{ trigger.event.data.tag_id }}'
  device_id: '{{ trigger.event.data.device_id }}'
```