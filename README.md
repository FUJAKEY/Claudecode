# 🏗️ Schematics Builder

**Auto-build schematics with AI precision! Layer by layer construction with automatic resource fetching.**

**Автоматическая постройка схематик с ИИ точностью! Послойное строительство с автоматическим сбором ресурсов.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5-green.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-36.2.42-orange.svg)](https://files.minecraftforge.net)
[![License](https://img.shields.io/badge/License-Antigravity%20OSL-blue.svg)](LICENSE.md)

---

## 📋 Table of Contents / Содержание

- [English Documentation](#-english-documentation)
- [Русская документация](#-русская-документация)

---

# 🇬🇧 English Documentation

## ✨ Features

- ✅ **Schematic Support** - Load `.schematic` (MCEdit/WorldEdit) and `.litematic` (Litematica) files
- ✅ **Layer-by-Layer Building** - Intelligent construction from bottom to top
- ✅ **Resource Chest System** - Link chests as material storage, AI fetches automatically
- ✅ **Auto-Pathfinding** - Player walks to chests and returns to build site
- ✅ **Progress Tracking** - See completion percentage and current layer
- ✅ **Rotation & Positioning** - Rotate 90° and set custom origin point
- ✅ **Pause/Resume** - Pause building anytime and continue later
- ✅ **Creative & Survival** - Works in both modes (survival needs materials)

## 📥 Installation

1. Install [Minecraft Forge 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html)
2. Download `schematicsbuilder-x.x.x.jar` from [Releases](https://github.com/FUJAKEY/Claudecode/releases)
3. Put the JAR file in your `mods` folder
4. Launch Minecraft with Forge profile

## 🚀 Quick Start

### Step 1: Add Schematics
Put your `.schematic` or `.litematic` files in the `schematics/` folder (created in game directory on first run).

### Step 2: Link Resource Chests (Survival)
Place chests with building materials near your build site.
```
Look at chest → /schem chest link
```

### Step 3: Load Schematic
```
/schem list                    # See available schematics
/schem load mybuilding.litematic   # Load a schematic
```

### Step 4: Position
```
/schem pos      # Set origin to your current position
/schem rotate   # Rotate 90 degrees (optional)
```

### Step 5: Build!
```
/schem build    # Start auto-building!
```
Or press **B** key.

## 📜 Commands

| Command | Description |
|---------|-------------|
| `/schem list` | List available schematic files |
| `/schem load <filename>` | Load a schematic file |
| `/schem pos` | Set build origin to current position |
| `/schem rotate` | Rotate schematic 90° clockwise |
| `/schem build` | Start auto-building |
| `/schem pause` | Pause/resume building |
| `/schem stop` | Stop building completely |
| `/schem status` | Show current build status |
| `/schem help` | Show all commands |

### Chest Commands

| Command | Description |
|---------|-------------|
| `/schem chest link` | Link looked-at chest as resource storage |
| `/schem chest unlink` | Unlink looked-at chest |
| `/schem chest list` | List all linked chests |
| `/schem chest clear` | Unlink all chests |

## ⌨️ Keybindings

| Key | Action |
|-----|--------|
| **O** | Open schematics menu |
| **B** | Start building |
| **N** | Stop building |
| **,** | Pause/Resume |
| **[** / **]** | Rotate schematic |

## 🤖 How Auto-Fetching Works

1. When building and inventory runs out of needed blocks
2. AI finds the nearest linked chest containing that block
3. Player automatically walks to the chest
4. Takes up to 64 items from the chest
5. Returns to build position
6. Continues building!

## 💡 Tips

- **Place chests close** to the build site for faster fetching
- **Fill chests beforehand** with all required materials
- **In Creative mode** resources are unlimited, no chests needed
- **Stay nearby** so chunks remain loaded during AFK building
- **Use Spawn Chunks** for builds that need to continue while you're far away

---

# 🇷🇺 Русская документация

## ✨ Возможности

- ✅ **Поддержка схематик** - Загрузка `.schematic` (MCEdit/WorldEdit) и `.litematic` (Litematica)
- ✅ **Послойное строительство** - Умная постройка снизу вверх
- ✅ **Система ресурсных сундуков** - Привяжи сундуки, ИИ сам берёт ресурсы
- ✅ **Авто-перемещение** - Персонаж сам ходит к сундукам и возвращается
- ✅ **Отслеживание прогресса** - Видно процент и текущий слой
- ✅ **Поворот и позиция** - Поворот на 90° и установка точки начала
- ✅ **Пауза/Продолжить** - Останови в любой момент
- ✅ **Креатив и Выживание** - Работает в обоих режимах

## 📥 Установка

1. Установи [Minecraft Forge 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html)
2. Скачай `schematicsbuilder-x.x.x.jar` из [Releases](https://github.com/FUJAKEY/Claudecode/releases)
3. Положи JAR в папку `mods`
4. Запусти Minecraft с профилем Forge

## 🚀 Быстрый старт

### Шаг 1: Добавь схематики
Положи `.schematic` или `.litematic` файлы в папку `schematics/` (создаётся автоматически).

### Шаг 2: Привяжи сундуки с ресами (Выживание)
Поставь сундуки с материалами рядом со стройкой.
```
Посмотри на сундук → /schem chest link
```

### Шаг 3: Загрузи схематику
```
/schem list                        # Список доступных
/schem load mybuilding.litematic   # Загрузить
```

### Шаг 4: Позиционирование
```
/schem pos      # Установить начало на текущую позицию
/schem rotate   # Повернуть на 90° (опционально)
```

### Шаг 5: Строй!
```
/schem build    # Начать автопостройку!
```
Или нажми клавишу **B**.

## 📜 Команды

| Команда | Описание |
|---------|----------|
| `/schem list` | Список схематик |
| `/schem load <файл>` | Загрузить схематику |
| `/schem pos` | Установить позицию начала |
| `/schem rotate` | Повернуть на 90° |
| `/schem build` | Начать стройку |
| `/schem pause` | Пауза/продолжить |
| `/schem stop` | Остановить стройку |
| `/schem status` | Показать статус |
| `/schem help` | Показать команды |

### Команды сундуков

| Команда | Описание |
|---------|----------|
| `/schem chest link` | Привязать сундук |
| `/schem chest unlink` | Отвязать сундук |
| `/schem chest list` | Список привязанных |
| `/schem chest clear` | Отвязать все |

## ⌨️ Клавиши

| Клавиша | Действие |
|---------|----------|
| **O** | Открыть меню схематик |
| **B** | Начать стройку |
| **N** | Остановить стройку |
| **,** | Пауза/Продолжить |
| **[** / **]** | Повернуть схематику |

## 🤖 Как работает авто-сбор

1. Когда в инвентаре заканчивается нужный блок
2. ИИ находит ближайший сундук с этим блоком
3. Персонаж автоматически идёт к сундуку
4. Берёт до 64 предметов
5. Возвращается на стройку
6. Продолжает строить!

## 💡 Советы

- **Ставь сундуки близко** — быстрее ходить
- **Заполняй заранее** — чтобы стройка не останавливалась
- **В Креативе** — ресурсы не нужны
- **Стой рядом** — чтобы чанки были загружены при AFK
- **Spawn Chunks** — стройка продолжится даже если уйдёшь далеко

---

## 📁 File Structure

```
minecraft/
├── mods/
│   └── schematicsbuilder-1.1.0.jar
└── schematics/           ← Put your schematics here!
    ├── castle.schematic
    ├── house.litematic
    └── tower.schematic
```

## 🛠️ Building from Source

```bash
git clone https://github.com/FUJAKEY/Claudecode.git
cd Claudecode
git checkout schematics
./gradlew build
```

JAR will be in `build/libs/`.

## 📜 License / Лицензия

[Antigravity Open Source License v1.0](LICENSE.md)

## 🤝 Contributing

Issues and Pull Requests are welcome!

---

**Made with ❤️ by FUJAKEY**
