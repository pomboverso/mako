# Prefs Schema

## js

```js
{
  apps: {
    "com.rama.chipdefense_copper.debug": {
      label: "Chip Defense: Copper",
      groupId: 0,
    },
  },
  groups: [
    {
      // default / ungrouped apps
      id: 0,
      label: "---",
      visible: true,
      expanded: true,
    },
    {
      id: 1,
      label: "------- favorites",
    },
  ],
  settings: {
    apps: {
      search: true,
      icons: true,
    },

    groups: {
      headers: true,
      collapsible: true,
    },

    clock: {
      // format<none|default|24-hours|12-hours>
      format: "24-hours",
    },

    date: {
      visible: true,
      year_day: true,
    },

    battery: {
      visible: true,
      temperature: true,
      charge_status: true,
    },

    font: {
      //style<default|jersey|roboto|quicksand|montserrat>
      style: "jersey",
    },
  },
}
```

## Android

```
app:com.rama.chipdefense_copper.debug:label = "Chip Defense: Copper"
app:com.rama.chipdefense_copper.debug:group_id = 0

group:0:label = "---"
group:0:visible = true
group:0:expanded = true

group:1:label = "------- favorites"
group:1:visible = true
group:1:expanded = false

settings:apps:search = true
settings:apps:icons = true

settings:groups:headers = true
settings:groups:collapsible = true

settings:clock:format = "24-hours"
settings:clock:app = "com.fossify.clock"

settings:date:visible = true
settings:date:year_day = true

settings:battery:visible = true
settings:battery:temperature = true
settings:battery:charge_status = true

settings:font:style = "jersey"
```