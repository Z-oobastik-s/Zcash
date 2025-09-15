# 💰 Zcash Plugin
### *The Ultimate Minecraft Currency Drop System*

![Version](https://img.shields.io/badge/version-1.0.0-brightgreen)
![MC Version](https://img.shields.io/badge/minecraft-1.21.8-blue)
![Platform](https://img.shields.io/badge/platform-Paper-orange)
![License](https://img.shields.io/badge/license-Study%20Only-red)

---

## 📋 **Table of Contents**
- [🎯 Overview](#-overview)
- [✨ Features](#-features)
- [📦 Installation](#-installation)
- [⚙️ Configuration](#️-configuration)
- [🌍 World Management](#-world-management)
- [🌐 Multi-Language Support](#-multi-language-support)
- [📊 Statistics System](#-statistics-system)
- [🛠️ Commands](#️-commands)
- [🔐 Permissions](#-permissions)
- [🎨 Visual Effects](#-visual-effects)
- [🔧 Economy Integration](#-economy-integration)
- [📈 Performance Optimization](#-performance-optimization)
- [🐛 Troubleshooting](#-troubleshooting)
- [📞 Support](#-support)
- [📄 License](#-license)

---

## 🎯 **Overview**

**Zcash** is a powerful and highly customizable Minecraft plugin that adds an immersive currency drop system to your server. When players kill mobs or break blocks, they can receive currency that automatically gets added to their economy balance through Vault or EssentialsX integration.

### **Why Choose Zcash?**
- 🎮 **Player Engagement**: Rewards players for active gameplay
- 💎 **Highly Customizable**: Configure drops for every mob and block
- 🌍 **Multi-World Support**: Control which worlds the plugin works in
- 🌐 **Multi-Language**: Support for Russian, English, and Chinese
- 📊 **Advanced Statistics**: Track player earnings with auto-reset
- 🎨 **Visual Effects**: Beautiful holograms and particle effects
- ⚡ **Performance Optimized**: Smart item stacking and cleanup
- 🔧 **Easy Integration**: Works with Vault and EssentialsX

---

## ✨ **Features**

### 🎯 **Core Features**
- **Dynamic Currency Drops**: Configurable drops from mobs and blocks
- **Economy Integration**: Seamless Vault/EssentialsX integration
- **Smart Item Management**: Automatic stacking and cleanup
- **Visual Feedback**: Holograms showing currency amounts
- **Sound Effects**: Configurable pickup sounds
- **Multi-Notification Types**: Chat, Actionbar, Bossbar, Title notifications

### 🌍 **World Management**
- **Flexible World Control**: Whitelist, blacklist, or enable in all worlds
- **Per-World Configuration**: Different settings for different worlds
- **Smart Detection**: Automatic world validation

### 📊 **Statistics & Analytics**
- **Player Tracking**: Monitor earnings from mobs and blocks
- **Auto-Reset System**: Daily, weekly, or monthly resets
- **Persistent Storage**: YAML-based statistics storage
- **Detailed Reports**: View comprehensive player statistics

### 🎨 **Visual & Audio**
- **Custom Holograms**: Show currency amounts above dropped items
- **Particle Effects**: Eye-catching visual feedback
- **Sound Integration**: Configurable pickup sounds
- **Distance-Based Visibility**: Hide items beyond certain distance

### ⚡ **Performance Optimization**
- **Item Stacking**: Multiple currency items stack into one
- **Auto-Despawn**: Configurable despawn timers
- **Memory Efficient**: Lightweight YAML storage
- **Optimized Calculations**: Smart performance algorithms

---

## 📦 **Installation**

### **Requirements**
- **Minecraft**: 1.21.8+
- **Server**: Paper (recommended) or Spigot
- **Java**: 17+
- **Dependencies**: Vault (optional), EssentialsX (optional)

### **Installation Steps**

1. **Download the Plugin**
   ```
   Download Zcash-1.0.0.jar from releases
   ```

2. **Install Dependencies**
   ```
   Download and install Vault plugin
   Download and install EssentialsX (optional)
   ```

3. **Deploy the Plugin**
   ```
   Place Zcash-1.0.0.jar in your server's plugins/ folder
   Restart your server
   ```

4. **Configure the Plugin**
   ```
   Edit plugins/Zcash/config.yml
   Edit plugins/Zcash/messages.yml (optional)
   Reload with /zcash reload
   ```

---

## ⚙️ **Configuration**

### **Main Configuration (config.yml)**

#### **General Settings**
```yaml
general:
  language: "ru"                    # Language: ru, en, zh
  default_amount: "1-2"             # Default drop amount
  default_chance: 100               # Default drop chance (1-100)
  unknown_mob_amount: 1             # Fallback amount for unconfigured mobs
```

#### **Economy Integration**
```yaml
economy:
  use_vault: true                   # Use Vault for economy
  fallback_to_essentials: true      # Fallback to EssentialsX if Vault fails
```

#### **Visual Settings**
```yaml
visual:
  display_item: "GOLD_NUGGET"       # Item shown when currency drops
  hologram_text: "&6{amount} ⛃"    # Text above dropped currency
  hologram_height: 0.3              # Height above item (blocks)
```

#### **Sound Effects**
```yaml
sounds:
  pickup_sound: "ENTITY_AXOLOTL_SWIM"  # Sound when picking up currency
  pickup_volume: 1.0                    # Volume (0.0-1.0)
  pickup_pitch: 1.0                     # Pitch (0.5-2.0)
```

### **Mob Configuration**
```yaml
mobs:
  zombie:
    amount: "1-3"                   # Drop 1-3 currency
    chance: 75                      # 75% chance to drop
  skeleton:
    amount: "2-4"
    chance: 50
  # Add more mobs as needed
```

### **Block Configuration**
```yaml
blocks:
  diamond_ore:
    amount: "5-10"
    chance: 100
  coal_ore:
    amount: "1-2"
    chance: 25
  # Add more blocks as needed
```

---

## 🌍 **World Management**

Control where the plugin works with flexible world management:

### **Configuration Options**
```yaml
worlds:
  mode: "all"                       # Mode: all, whitelist, blacklist
  list:                            # List of worlds
    - "world"
    - "world_nether"
    - "world_the_end"
  show_disabled_messages: false     # Show messages when disabled
```

### **Mode Types**
- **`all`**: Plugin works in all worlds
- **`whitelist`**: Only works in specified worlds
- **`blacklist`**: Works everywhere except specified worlds

### **Examples**
```yaml
# Only work in survival worlds
worlds:
  mode: "whitelist"
  list:
    - "world"
    - "survival"

# Don't work in creative world
worlds:
  mode: "blacklist"
  list:
    - "creative"
```

---

## 🌐 **Multi-Language Support**

Zcash supports multiple languages with easy switching:

### **Supported Languages**
- 🇷🇺 **Russian** (messages.yml) - Default
- 🇺🇸 **English** (messages_en.yml)
- 🇨🇳 **Chinese** (messages_zh.yml)

### **Language Switching**
1. **Via Config**: Change `language` in config.yml
2. **Via Command**: Use `/zcash language` for GUI selection
3. **Via GUI**: Beautiful interface with flag icons

### **Custom Translations**
Create your own language file:
```yaml
# messages_custom.yml
pickup: "<gradient:#C51BF1:#C49BFB>Your custom message {amount}</gradient>"
# Add all required messages
```

Then set: `language: "custom"`

---

## 📊 **Statistics System**

Track player earnings with advanced statistics:

### **Features**
- **Separate Tracking**: Mobs vs blocks earnings
- **Auto-Reset**: Configurable reset intervals
- **Persistent Storage**: YAML-based storage
- **Detailed Reports**: Comprehensive statistics

### **Configuration**
```yaml
statistics:
  enabled: true                     # Enable statistics tracking
  reset_enabled: true               # Enable auto-reset
  reset_interval: "daily"           # daily, weekly, monthly
  reset_time: "00:00"              # Time to reset (HH:MM)
  broadcast_reset: true             # Announce resets
```

### **Viewing Statistics**
```bash
/zcash stats                        # Your statistics
/zcash stats PlayerName             # Other player's statistics
```

---

## 🛠️ **Commands**

### **Main Commands**

| Command | Description | Permission |
|---------|-------------|------------|
| `/zcash` | Show help menu | `zcash.use` |
| `/zcash help` | Display command help | `zcash.use` |
| `/zcash reload` | Reload configuration | `zcash.reload` |
| `/zcash give <player> <amount>` | Give currency to player | `zcash.give` |
| `/zcash stats [player]` | View statistics | `zcash.stats` |
| `/zcash language` | Open language selection GUI | `zcash.use` |

### **Command Examples**
```bash
# Give 100 currency to player Steve
/zcash give Steve 100

# View your own statistics
/zcash stats

# View another player's statistics
/zcash stats Notch

# Reload plugin configuration
/zcash reload

# Open language selection menu
/zcash language
```

### **Tab Completion**
All commands support intelligent tab completion:
- Player names for `/zcash give` and `/zcash stats`
- Subcommands for `/zcash`
- Amount suggestions for `/zcash give`

---

## 🔐 **Permissions**

### **Permission Nodes**

| Permission | Description | Default |
|------------|-------------|---------|
| `zcash.use` | Basic plugin usage | `true` |
| `zcash.reload` | Reload configuration | `op` |
| `zcash.give` | Give currency to players | `op` |
| `zcash.stats` | View statistics | `true` |
| `zcash.stats.others` | View other players' stats | `op` |
| `zcash.admin` | All admin permissions | `op` |

### **Permission Groups**
```yaml
# Example permission setup
groups:
  player:
    permissions:
      - zcash.use
      - zcash.stats
  
  moderator:
    permissions:
      - zcash.use
      - zcash.stats
      - zcash.stats.others
  
  admin:
    permissions:
      - zcash.admin
```

---

## 🎨 **Visual Effects**

### **Holograms**
- **Dynamic Text**: Shows currency amount above items
- **Customizable**: Full color and formatting support
- **Performance Optimized**: Efficient hologram management
- **Auto-Cleanup**: Automatic removal when items despawn

### **Hologram Configuration**
```yaml
visual:
  hologram_text: "&6{amount} ⛃"    # Hologram text format
  hologram_height: 0.3              # Height above item
  hologram_visible_distance: 15     # Visibility range
```

### **Supported Placeholders**
- `{amount}` - Currency amount
- `{player}` - Player name (in messages)
- `{source}` - Drop source (MOB/BLOCK)

### **Color Support**
- **Legacy Colors**: `&c`, `&a`, `&b`, etc.
- **Hex Colors**: `&#FF5555`, `&#00FF00`
- **Gradients**: `<gradient:#FF0000:#00FF00>text</gradient>`

---

## 🔧 **Economy Integration**

### **Supported Plugins**
- **Vault**: Primary integration method
- **EssentialsX**: Direct integration fallback
- **Custom**: Easy to extend for other economy plugins

### **Integration Priority**
1. **Vault** (if available and enabled)
2. **EssentialsX** (if Vault fails and fallback enabled)
3. **None** (commands still execute, but no money given)

### **Configuration**
```yaml
economy:
  use_vault: true                   # Try Vault first
  fallback_to_essentials: true      # Use EssentialsX if Vault fails
```

### **Custom Commands**
Execute custom commands when currency is picked up:
```yaml
commands:
  enable-commands: true
  pickup:
    - "eco give {player} {amount}"
    - "give {player} cookie 1"
    - "broadcast {player} found {amount} coins!"
```

---

## 📈 **Performance Optimization**

### **Item Stacking**
- **Smart Stacking**: Multiple currency items combine automatically
- **Distance-Based**: Items within 2 blocks stack together
- **Performance Boost**: Reduces entity count significantly

### **Auto-Despawn**
```yaml
optimization:
  enable_despawn: true              # Enable auto-despawn
  despawn_time: 300                 # Despawn after 5 minutes
```

### **Visibility Control**
```yaml
optimization:
  enable_distance_hiding: true      # Hide distant items
  hide_distance: 15                 # Hide beyond 15 blocks
```

### **Memory Management**
- **YAML Storage**: Lightweight alternative to SQLite
- **Efficient Caching**: Smart data caching mechanisms
- **Cleanup Tasks**: Automatic cleanup of expired data

---

## 🐛 **Troubleshooting**

### **Common Issues**

#### **Currency Not Dropping**
```yaml
# Check these settings:
- Mob/block configured in config.yml?
- Drop chance > 0?
- Player has permission?
- World enabled for plugin?
```

#### **Economy Not Working**
```yaml
# Verify:
- Vault installed and loaded?
- EssentialsX available (if using fallback)?
- Economy provider working?
- Commands executing properly?
```

#### **Holograms Not Showing**
```yaml
# Check:
- Hologram text configured?
- Player within visible distance?
- World enabled for plugin?
- Client-side optimization mods interfering?
```

### **Debug Mode**
Enable debug mode for detailed logging:
```yaml
debug:
  show_drops: true                  # Show drop messages to players
  log_economy: true                 # Log economy transactions
  log_statistics: true              # Log statistics operations
```

### **Performance Issues**
```yaml
# Optimize performance:
optimization:
  enable_stacking: true             # Reduce entity count
  enable_despawn: true              # Auto-cleanup items
  enable_distance_hiding: true      # Hide distant items
  max_items_per_location: 5         # Limit items per location
```

---

## 📞 **Support**

### **Get Help**
- 📱 **Telegram**: [https://t.me/Zoobastiks](https://t.me/Zoobastiks)
- 📧 **Direct Contact**: Contact via Telegram for fastest response
- 🐛 **Bug Reports**: Report issues with detailed reproduction steps
- 💡 **Feature Requests**: Suggest new features and improvements

### **Before Contacting Support**
1. **Check this README**: Most questions are answered here
2. **Verify Configuration**: Ensure your config.yml is correct
3. **Check Logs**: Look for error messages in console
4. **Test Permissions**: Verify player permissions are correct
5. **Update Plugin**: Ensure you're using the latest version

### **When Reporting Issues**
Please provide:
- **Server Version**: Paper/Spigot version
- **Plugin Version**: Zcash version
- **Error Logs**: Full error messages from console
- **Configuration**: Your config.yml and messages.yml
- **Steps to Reproduce**: Detailed steps to recreate the issue

---

## 📄 **License**

### **License Terms**
```
Copyright © 2025 Zoobastiks. All rights reserved.

STUDY ONLY LICENSE

Permission is hereby granted to study and examine this software 
for educational purposes only.

PROHIBITED ACTIVITIES:
❌ Commercial use or redistribution
❌ Copying, modification, or derivative works
❌ Resale, sublicensing, or profit generation
❌ Reverse engineering for competitive purposes
❌ Claiming ownership or authorship

PERMITTED ACTIVITIES:
✅ Personal study and learning
✅ Educational analysis and research
✅ Non-commercial examination of code structure

This software is provided "AS IS" without warranty of any kind.
The author shall not be liable for any damages arising from the use
of this software.

For licensing inquiries or commercial use requests, 
contact: https://t.me/Zoobastiks
```

### **Third-Party Libraries**
This plugin uses the following open-source libraries:
- **Paper API**: Licensed under MIT License
- **Vault API**: Licensed under LGPL License
- **Kotlin Standard Library**: Licensed under Apache 2.0 License

---

## 🎉 **Final Words**

Thank you for choosing **Zcash**! This plugin represents hours of careful development and testing to provide you with the best possible currency drop experience for your Minecraft server.

### **Key Benefits Recap**
- 🎮 **Enhanced Gameplay**: Rewards active players with currency
- 🛠️ **Easy Setup**: Intuitive configuration and installation
- 🌍 **Flexible Control**: World-based and permission-based management
- 📊 **Detailed Analytics**: Comprehensive statistics and reporting
- 🎨 **Beautiful Effects**: Eye-catching visual and audio feedback
- ⚡ **Optimized Performance**: Smart algorithms for server efficiency

### **Stay Updated**
Join our Telegram channel for:
- 📢 Plugin updates and announcements
- 💡 Tips and configuration examples
- 🤝 Community support and discussions
- 🐛 Bug reports and feature requests

**Telegram**: [https://t.me/Zoobastiks](https://t.me/Zoobastiks)

---

*Made with ❤️ by Zoobastiks | © 2025 All Rights Reserved*

---

## 📋 **Quick Reference Card**

### **Essential Commands**
```bash
/zcash                              # Main help
/zcash reload                       # Reload config
/zcash give <player> <amount>       # Give currency
/zcash stats                        # View statistics
/zcash language                     # Change language
```

### **Important Files**
```
plugins/Zcash/
├── config.yml                     # Main configuration
├── messages.yml                    # Russian messages
├── messages_en.yml                 # English messages
├── messages_zh.yml                 # Chinese messages
└── statistics.yml                  # Player statistics
```

### **Key Permissions**
```
zcash.use                          # Basic usage
zcash.admin                        # Full admin access
zcash.give                         # Give currency
zcash.reload                       # Reload config
```

### **Support Links**
- **Telegram**: [https://t.me/Zoobastiks](https://t.me/Zoobastiks)
- **Author**: Zoobastiks
- **Year**: 2025
- **Version**: 1.0.0
