I've launched a website that automates code generation!
https://shb2-dev.github.io/cargo_rocket_generator/

I'm new to modding. This mod may contain many bugs. I would really appreciate it if you could submit a pull request.

This mod adds cargo rockets to *Ad Astra* and, when used in conjunction with *CC:tweaked*, allows for the automation of interplanetary item transport.

# Added Features
## Added particle and sound, and rework launch/landing animation
Use ad astra's sound and particle to expression
<br>It can now take off and land more smoothly than before.
<br>Also, a bug where the rocket would land on blocks without collision detection has been fixed.
## New rocket type
Added three new tiers of rockets.
<br>Each tier has a different maximum number of planets it can reach.
<br>The same range limitations apply as for AdAstra's rockets
<br>With the addition of rocket tiers, fuel and energy consumption now increases depending on the destination
## Added New useful tag
This mod add itemtag `denied_in_launch_pad` to prevent to use specified item in launch pad (e.g. shulker box)
<br>By using this tag,you can prevent illegal transport technic.
## customizable the difficulty level and fuel efficiency for your destination 
Change `config/ad_astra_cargo_rockets.json` to customize target destination cost and fuel efficient.

---

# Fluid & Energy Connection

The launchpad has two separate fluid tanks and supports Forge energy (FE).

| Side | Function |
|------|----------|
| **Bottom** | Fuel tank (input only) |
| **Top / Sides** | Cargo fluid tank (input & output) |
| **Any side** | Forge Energy (FE) input |

- Connect fuel pipes to the **bottom** face to supply rocket fuel (e.g. `ad_astra:fuel`, `ad_astra:cryo_fuel`).
- Connect fluid pipes to the **top or side** faces to transport cargo liquids.
- You can also right-click with a bucket to fill either tank (bottom face = fuel, other faces = cargo).

---

## Added Useful CC's Function
## 📥 `loadAllItems([filter])`
Moves all items from the launchpad's inventory to the rocket's inventory.
<br>If you specify an item ID, you can move only that item.

## 📤 `unloadAllItems([filter])`
Moves all items from the rocket's inventory to the launchpad's inventory.
<br>If you specify an item ID, you can move only that item.

### Parameters
- `filter`_?_ (string) : Name of the item to load or unload (e.g. "minecraft:cobblestone")




# Rocket Launchpad Lua API

This API allows you to control a rocket launchpad from a CC:Tweaked computer. It provides functions to launch rockets, manage inventories, and check energy levels.

---

## Connecting
You must connect the computer to the central block of the launch pad to access the below methods.
Connecting to the outer blocks will allow you to access the generic inventory methods.

---

## 📦 Inventory Slot Indexing
- All inventory slot indexes in Lua start at **1**, matching CC:Tweaked's conventions.

---

## 🧨 `launch(planet)`
Attempts to launch a rocket to the specified planet. (See `getValidDestinations`)

### Parameters
- `planet` (string): The name of the destination planet.

### Errors
- `"No rocket found"` – No rocket is on the launchpad.
- `"<planet> is not a valid planet"` – The specified planet name is invalid.
- `"Not enough energy to launch"` – The launchpad lacks sufficient energy.
- `"<planet> is too high of a tier for this rocket"` – The rocket tier is too low for the destination.


---
## 📥 `moveItemsFromRocketToLaunchPad(rocketSlot, launchPadSlot)`
Moves an item from the rocket's inventory to the launchpad's inventory.

### Parameters
- `rocketSlot` (int): Slot in the rocket's inventory.
- `launchPadSlot` (int): Slot in the launchpad's inventory.

### Errors
- `"No rocket found"`
- `"Destination full"`
- `"Invalid slot"`

---

## 📤 `moveItemsFromLaunchPadToRocket(launchPadSlot, rocketSlot)`
Moves an item from the launchpad's inventory to the rocket's inventory.

### Parameters
- `launchPadSlot` (int): Slot in the launchpad's inventory.
- `rocketSlot` (int): Slot in the rocket's inventory.

### Errors
- `"No rocket found"`
- `"Destination full"`
- `"Invalid slot"`

---

## ⚡ `getEnergyRequiredForLaunch()`
Returns the amount of energy required to launch the rocket.

### Returns
- `int`: Energy required.

---

## 🔋 `getEnergy()`
Returns the current stored energy in the launchpad.

### Returns
- `long`: Current energy.

---

## 🔋 `getMaxEnergy()`
Returns the maximum energy capacity of the launchpad.

### Returns
- `long`: Maximum energy.

---

## 🌍 `getValidDestinations()`
Returns a table of valid destination planet names, with the key being the planet and the value being the required rocket tier to reach it.

### Returns
- `table<string, int>`: Table with the key being the planet and the value being the required rocket tier to reach it.

---

## 📦 `listLaunchPadInventory()`
Returns the current non-empty inventory of the launchpad.

### Returns
- `table<int, table>`: A table mapping slot indexes to item tables with:
    - `name` (string): Display name.
    - `id` (string): Registry ID.
    - `count` (int): Stack size.
    - `max_count` (int): Maximum stack size.

---

## 📥 `listLaunchPadInputSlotIndexes()`
Lists which slot indexes are considered input slots.
These are the slots hoppers and other item transportation mods can insert into.

### Returns
- `int[]`: List of input slot indexes (1-based).

---

## 📤 `listLaunchPadOutputSlotIndexes()`
Lists which slot indexes are considered output slots.
These are the slots hoppers and other item transportation mods can extract from.

### Returns
- `int[]`: List of output slot indexes (1-based).

---

## 🚀 `isRocketPresent()`
Checks whether a rocket is present on the launchpad.

### Returns
- `boolean`: `true` if a rocket is present, `false` otherwise.

---

## 🚀 `listRocketInventory()`
Returns the current non-empty inventory of the rocket.

### Returns
- `table<int, table>|nil`: Table mapping slot indexes to item data if rocket is present, or `nil` if no rocket is found. Item tables contain:
    - `name` (string): Display name.
    - `id` (string): Registry ID.
    - `count` (int): Stack size.
    - `max_count` (int): Maximum stack size.

---

## 🛢️ `getFuel()`
Returns the current fuel amount in the launchpad's fuel tank.

### Returns
- `int`: Current fuel in mB.

---

## 🛢️ `getMaxFuel()`
Returns the maximum fuel capacity of the launchpad's fuel tank.

### Returns
- `int`: Maximum fuel in mB.

---

## 🛢️ `getFuelType()`
Returns the fluid ID of the fuel currently in the fuel tank.

### Returns
- `string`: Fluid registry ID (e.g. `"ad_astra:fuel"`), or `"empty"` if the tank is empty.

---

## 🧪 `getCargoFluid()`
Returns the current amount of cargo fluid in the launchpad's cargo tank.

### Returns
- `int`: Current cargo fluid in mB.

---

## 🧪 `getMaxCargoFluid()`
Returns the maximum cargo fluid capacity.

### Returns
- `int`: Maximum cargo fluid in mB.

---

## 🧪 `getCargoFluidType()`
Returns the fluid ID of the cargo fluid currently in the cargo tank.

### Returns
- `string`: Fluid registry ID, or `"empty"` if the tank is empty.

---

## Credit
Fork Source: Ad-Astra-Cargo-Rockets-Unofficial by ChiyahaRe 
URL: https://github.com/ChiyahaRe/Ad-Astra-Cargo-Rockets-Unofficial
fork of the original fork Source: Ad Astra Cargo Rockets by BillBodkin
URL: https://modrinth.com/mod/ad-astra-cargo-rockets
