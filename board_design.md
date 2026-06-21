# University Monopoly — Game Rules & Board Prompt

## Board Structure

**Total cells: 36** (9 per side including corners)

| Type | Count |
|---|---|
| Corners | 4 |
| Chance | 4 (one per side) |
| Shuttle Stations | 4 (one per side) |
| Properties | 24 |
| **Total** | **36** |

---

## Economy

- **Starting money: ₪2,000,000 per player**
- **Airport toll: 5% of starting money = ₪100,000** (paid to the bank each time a player uses the Airport teleport)
- **Rent values:** follow standard Monopoly rent scaling, adjusted proportionally to the ₪2,000,000 starting amount
- **Salary (passing Start):** standard Monopoly salary, scaled to match economy

---

## Win Conditions

A player wins immediately upon achieving **any one** of the following:

| Condition | Description |
|---|---|
| 🚉 **Shuttle Monopoly** | Own all 4 Shuttle Stations |
| 🏘️ **3 Monopolies** | Own all properties in any 3 color groups |
| 📍 **Full Row** | Own all properties on any one side of the board (excluding corners and non-property cells) |

---

## Corner Cells

| Position | Name | Rule |
|---|---|---|
| Bottom-right | **Start** | Collect salary when passing |
| Bottom-left | **Jail** | Landing here keeps you stuck for 3 turns, OR pay fine, OR roll doubles to escape |
| Top-left | **Airport** | Pay ₪100,000 → teleport to any unowned property or shuttle station, or one you already own |
| Top-right | **World Championships** | Special multiplier cell — see rules below |

> ⚠️ Landing on Jail is the only way to be sent there. There is no "Go to Jail" mechanic.

---

## Shuttle Stations (4 total — Railroad equivalent)

One per side. Owning all 4 wins the game instantly.

1. Engineering Station
2. Highway 4 Station
3. Tommy Station
4. 507 Station

Rent scales with number of stations owned (standard railroad rent curve).

---

## Properties (24 total)

### 🟤 Brown — cheapest (3)
1. Malas
2. Optics
3. Linear Algebra

### 🔵 Light Blue (3)
4. Calculus 1
5. Calculus 2
6. Memphis 3

### 🩷 Pink (3)
7. Data Structures 1
8. Data Structures 2
9. Automata & Computability

### 🟠 Orange (3)
10. Computer Networks
11. Intro to Circuits
12. Circuit Design

### 🔴 Red (3)
13. Electricity & Magnetism
14. Mechanics
15. Probability

### 🟡 Yellow (3)
16. Radar Systems (MADAR)
17. Thermodynamics
18. Algebraic Structures

### 🟢 Green (3)
19. Complex Analysis
20. Calculus 3 (advanced track)
21. Assembler (x86)

### 🔵 Dark Blue — most expensive (3)
22. Operating Systems *(with Shapir HaMelech)*
23. OOP *(with Shapir HaMelech)*
24. Software Engineering *(with Shapir HaMelech)*

> Total: 3+3+3+3+3+3+3+3 = **24 properties** ✅

---

## Special Rules

### Jail
- Landing on the Jail cell means you are **stuck for 3 turns**
- To escape early: pay a fixed fine, OR roll doubles on your turn
- No other mechanic sends a player to Jail

### Airport (Free Travel)
- On landing: **pay ₪100,000 to the bank**, then teleport to any shuttle station OR any property that is unowned or already yours
- You **cannot** teleport to a property owned by another player
- The fee is mandatory — if you cannot pay, you cannot use the Airport and your turn ends

### World Championships
- When a player lands on World Championships, they **must choose one of their own properties** to place the WC effect on — you **cannot** place it on an unowned property, a shuttle station, or anything else
- The **global multiplier starts at ×2 and increases by +1 every time** World Championships is activated:
  - 1st activation → chosen property charges ×2 rent
  - 2nd activation → chosen property charges ×3 rent
  - 3rd activation → ×4 rent
  - and so on...
- The multiplier counter is **shared globally** — it increments regardless of which player activates it
- **Only one property in the entire game carries the WC effect at any time**
- You **can place the effect on the same property again** — this removes the old multiplier and replaces it with the new (higher) one
- If placed on a different property, the previous property loses the effect
- The effect on a property **only disappears when it is overwritten** by a new WC activation — landing on the marked property and paying rent does NOT remove the effect; it stays until replaced
- You **can** choose a property that already has houses or hotels
- The owner of the marked property collects the multiplied rent from any player who lands on it

---

## Board Layout

Clockwise from bottom-right, 9 cells per side (corners shared between sides):

**Bottom side** (Start → Jail):
Start → Malas → Optics → Linear Algebra → Chance → Calculus 1 → Calculus 2 → Memphis 3 → Jail

**Left side** (Jail → Airport):
Jail → Engineering Station → Data Structures 1 → Chance → Data Structures 2 → Automata → Computer Networks → Intro to Circuits → Airport

**Top side** (Airport → World Championships):
Airport → Circuit Design → Highway 4 Station → Electricity & Magnetism → Chance → Mechanics → Probability → Radar Systems → World Championships

**Right side** (World Championships → Start):
World Championships → Thermodynamics → Algebraic Structures → Tommy Station → Complex Analysis → Chance → Calculus 3 → Assembler → Start

> ⚠️ Operating Systems, OOP, Software Engineering, Advanced Math are not placed above — the layout needs 1 more cell per side to fit all 24. Adjust by adding one property per side to reach 9 non-corner cells, or redistribute groups. Final layout should be verified before rendering.