# Atlas

Atlas est un framework modulaire et une base de développement pour les plugins Paper, conçu pour Java 21 et Paper 1.21.10.

Le projet fournit des abstractions réutilisables et fortement typées autour des besoins courants du développement Paper/Bukkit : données joueur, base de données, configuration, messages, commandes, items personnalisés, interfaces d'inventaire, dialogs, planification de tâches et autres systèmes orientés gameplay.

Atlas est pensé comme un framework cohérent plutôt que comme une collection de classes utilitaires indépendantes. Les contrats publics sont regroupés sous `com.veloriastudio.atlas.api`, tandis que les implémentations spécifiques à Paper, Bukkit ou à la persistance restent sous `com.veloriastudio.atlas.internal`.

> Statut du projet : développement actif. L'API n'est pas encore considérée comme stable et peut évoluer pendant l'implémentation des modules restants.

## Prérequis

- Java 21
- Paper 1.21.10
- Maven
- MySQL
- Un serveur Paper compatible avec `api-version: 1.21.10`

## Stack technique

| Composant | Technologie |
| --- | --- |
| Langage | Java 21 |
| API serveur | Paper 1.21.10 |
| Build | Maven |
| Base de données | MySQL |
| Pool de connexions | HikariCP |
| Driver SQL | MySQL Connector/J |
| Texte | Adventure / MiniMessage |
| Commandes | Paper Brigadier |
| Persistance | Couche asynchrone avec migrations SQL |

## Objectifs

Atlas suit plusieurs principes d'architecture :

- fournir une API réutilisable pour les besoins courants des plugins Paper ;
- limiter l'exposition des détails d'implémentation Bukkit/Paper ;
- privilégier les API fortement typées ;
- exécuter la persistance hors du thread principal ;
- rendre explicite le cycle de vie des services ;
- éviter les classes génériques de type `Utils` ou les `Manager` surchargés ;
- séparer les contrats publics des implémentations internes ;
- favoriser la composition ;
- valider les entrées et états invalides le plus tôt possible.

## Fonctionnalités

| Module | Statut     | Description |
| --- |------------| --- |
| Bootstrap | Implémenté | Maven, lifecycle Paper, initialisation et arrêt des services |
| Player Data | Implémenté | Données joueur typées, persistantes ou transitoires |
| Database | Implémenté | MySQL, HikariCP, async, transactions et migrations |
| Configuration | Implémenté | Chargement, reload et accès typé |
| Messages | Implémenté | MiniMessage, placeholders, bundles et localisation |
| Commandes | Implémenté | Annotations, Brigadier, arguments, permissions, suggestions et aide |
| Items / PDC | Implémenté | ItemBuilder, PdcKey et ItemData |
| Custom Items | Implémenté | Registry, catégories, persistance, création, édition et suppression |
| GUI | Implémenté | Boutons, contexte de clic, remplissage, bordures et pagination |
| Dialog | Implémenté | DialogBuilder autour de l'API Dialog de Paper |
| Scheduler | Implémenté | Tâches sync/async, délais, répétitions et annulation |
| Async | Implémenté | Exécution basée sur `CompletableFuture` |
| Cooldowns | Implémenté | Abstractions de cooldown |
| Events | En cours   | Event utilities |
| Players | Prévu      | Player utilities |
| Worlds | Prévu      | Mondes, locations et téléportation |
| Entities | Prévu      | Entités et mobs |
| Recipes | Prévu      | Recettes et crafting |
| Gameplay UI | Prévu      | BossBars, scoreboards et UI gameplay |
| Effects | Prévu      | Particules, sons et effets |
| Serialization | Prévu      | Sérialisation et data utilities |
| Cache | Prévu      | Cache et outils de performance |
| Registries | Prévu      | API de registres et d'extensions |
| Hooks | Prévu      | Intégrations externes |
| Logging | Prévu      | Debug, logging et profiling |

## Architecture

```text
src/main/java/com/veloriastudio/atlas/
├── api/
│   ├── command/
│   ├── config/
│   ├── data/
│   ├── database/
│   ├── dialog/
│   ├── gui/
│   ├── item/
│   ├── message/
│   ├── pdc/
│   └── scheduler/
│
├── internal/
│   ├── command/
│   ├── config/
│   ├── data/
│   ├── database/
│   ├── gui/
│   ├── item/
│   ├── message/
│   └── scheduler/
│
└── AtlasPlugin.java
```

Le package `api` contient les contrats et types destinés aux consommateurs du framework.

Le package `internal` contient les implémentations spécifiques à Paper, Bukkit, MySQL et aux mécanismes internes d'Atlas.

## Player Data

Le système Player Data fournit une gestion typée des données joueur basée sur les UUID.

### API principale

- `PlayerData`
- `PlayerDataKey<T>`
- `PlayerDataKeyRegistry`
- `PlayerDataService`
- `DataKeyId`
- `DataPersistence`
- `DataCodec<T>`

Exemple conceptuel :

```java
PlayerDataKey<Integer> coins = registry.registerPersistent(
        "coins",
        Integer.class,
        codec
);

PlayerData data = playerDataService.findLoaded(playerId)
        .orElseThrow();

data.set(coins, 250);

int value = data.get(coins)
        .orElse(0);
```

Les données persistantes et transitoires sont explicitement séparées. Une clé persistante possède un codec permettant à la couche de stockage de sérialiser sa valeur sans connaître le type métier.

Le service expose notamment :

```java
Optional<PlayerData> findLoaded(UUID playerId);

CompletableFuture<PlayerData> load(UUID playerId);

CompletableFuture<Void> flush(UUID playerId);

CompletableFuture<Void> flushAll();
```

Lorsqu'un joueur quitte le serveur, ses données sont sauvegardées avant leur déchargement. Lors de l'arrêt du plugin, un `flushAll()` final est exécuté avant la fermeture de la base de données.

## Base de données

Atlas fournit une abstraction asynchrone autour de l'accès SQL afin d'éviter de disperser JDBC dans l'ensemble du projet.

### API principale

- `Database`
- `DatabaseService`
- `DatabaseTransaction`
- `TransactionCallback`
- `RowMapper<T>`
- `MySqlConfig`

L'implémentation MySQL utilise HikariCP pour le pool de connexions.

Les opérations retournent des `CompletableFuture`, ce qui permet de garder les traitements bloquants hors du thread principal.

### Transactions

Les opérations qui doivent être atomiques peuvent être exécutées via `DatabaseTransaction`.

### Migrations

Atlas possède un système de migrations SQL versionnées. Les migrations actuelles gèrent notamment :

- les données joueur ;
- les custom items persistants.

Les migrations sont exécutées au démarrage avant les services dépendants.

## Configuration

L'API de configuration encapsule les configurations YAML de Bukkit derrière des accesseurs typés.

### API principale

- `Config`
- `ConfigService`

Fonctionnalités :

- chargement depuis le dossier du plugin ;
- copie des ressources intégrées lorsqu'elles sont absentes ;
- reload ;
- accès typé aux chaînes, booléens et nombres ;
- récupération générique avec validation du type ;
- validation des chemins de configuration.

Exemple :

```yaml
database:
  host: "localhost"
  port: 3306
  name: "atlas"
  username: "root"
  password: ""
```

Les identifiants réels de production doivent rester dans la configuration locale du serveur et ne doivent pas être commit dans le dépôt.

## Messages

Atlas utilise Adventure et MiniMessage.

### API principale

- `MessageService`
- `MessageBundle`
- `MessageBundleService`
- `LocalizedMessages`
- `MessagePlaceholders`
- `CommonMessages`

Les bundles sont chargés depuis des fichiers tels que :

```text
src/main/resources/messages/fr.yml
src/main/resources/messages/en.yml
```

Les locales sont normalisées et peuvent retomber sur leur langue principale. Par exemple, `en-US` et `en_US` peuvent être résolus vers `en`.

Les placeholders reposent sur les `TagResolver` de MiniMessage.

## Commandes

Atlas fournit un framework de commandes basé sur Brigadier et l'API Paper.

### API principale

- `AtlasCommand`
- `AtlasSubcommand`
- `CommandArguments`
- `CommandContext`
- `CommandService`
- `CommandSuggestionProvider`
- `CommandSuggestionContext`

Les métadonnées sont déclarées via :

- `@DescribeCommand`
- `@DescribeSubcommand`
- `@Subcommands`

Le système prend notamment en charge :

- entiers ;
- décimaux ;
- booléens ;
- mots ;
- chaînes ;
- greedy strings ;
- greedy strings optionnelles ;
- joueurs ;
- types Brigadier personnalisés ;
- suggestions dynamiques ;
- permissions ;
- commandes réservées aux joueurs ;
- aliases ;
- aide générée ;
- sous-commandes.

Commandes actuellement présentes :

```text
/itemeditor
/ie
/museum
/museum <category-path>
```

Permissions utilisées :

```text
atlas.item.create
atlas.museum
```

## Items et PDC

### ItemBuilder

`ItemBuilder` fournit une API fluide autour des `ItemStack`.

Fonctionnalités :

- matériau ;
- quantité ;
- nom ;
- lore ;
- enchantements ;
- flags ;
- glint ;
- unbreakable ;
- taille maximale du stack ;
- données persistantes.

### Persistent Data Container

L'accès typé au PDC repose sur :

- `PdcKey<T>`
- `ItemData`

Le type Bukkit associé à une valeur est porté directement par la clé, ce qui limite les incohérences au moment de la lecture et de l'écriture.

## Custom Items

Atlas prend en charge les custom items définis dans le code ainsi que les custom items persistants.

### API principale

- `CustomItem`
- `CustomItemCategory`
- `CustomItemRegistry`
- `DynamicCustomItemService`
- `StoredCustomItem`

Chaque custom item possède un `NamespacedKey`.

Cette identité est également écrite dans le PDC de l'`ItemStack`, ce qui permet de retrouver le `CustomItem` enregistré depuis un item en jeu.

Les custom items persistants peuvent être :

- créés ;
- modifiés ;
- supprimés ;
- chargés depuis MySQL au démarrage.

Les opérations concurrentes sur un même identifiant sont sérialisées afin d'éviter les races entre création, modification et suppression.

Les templates d'items sont clonés aux frontières de l'API pour limiter les mutations involontaires.

### Catégories

Les catégories utilisent des chemins validés :

```text
weapon
weapon/sword
tools/mining
```

Le museum peut filtrer une catégorie ainsi que ses descendants.

## Custom Item Museum

Le projet contient une interface de gestion des custom items.

Fonctionnalités :

- navigation paginée ;
- filtrage par catégorie ;
- clic gauche pour recevoir un item ;
- clic droit pour modifier un item éditable ;
- touche de drop pour demander sa suppression ;
- GUI de confirmation ;
- Dialog Paper pour l'édition.

L'éditeur permet de modifier :

- catégorie ;
- nom ;
- lore ;
- glint ;
- unbreakable ;
- taille maximale du stack.

Les contraintes de composants Minecraft sont prises en compte. Un item damageable est limité à une taille maximale de stack de `1`.

## GUI

### API principale

- `Gui`
- `GuiBuilder`
- `GuiButton`
- `GuiClickContext`
- `PaginatedGui<T>`

Fonctionnalités :

- titre configurable ;
- inventaire de 1 à 6 lignes ;
- boutons par slot ;
- fill ;
- border ;
- callbacks de clic ;
- clonage défensif des items ;
- pagination ;
- navigation configurable ;
- slots de contenu personnalisés.

`AtlasGuiListener` gère le dispatch des clics en interne.

## Dialog API

`DialogBuilder` encapsule l'API Dialog de Paper.

Les inputs actuellement pris en charge sont :

- texte ;
- texte multiligne ;
- booléen ;
- nombre ;
- `DialogInput` Paper brut ;
- composants de body ;
- confirmation ;
- annulation.

Exemple :

```java
DialogBuilder.create(Component.text("Modifier l'item"))
        .text("display_name", Component.text("Nom"), initialName)
        .textArea("lore", Component.text("Lore"), initialLore)
        .toggle("glint", Component.text("Glint"), false)
        .number("max_stack", Component.text("Taille du stack"), 1, 99, 64, 1)
        .confirm(Component.text("Sauvegarder"), (response, audience) -> {
            // Traitement
        })
        .cancel(Component.text("Annuler"))
        .open(player);
```

Le builder valide notamment :

- les valeurs nulles ;
- les clés dupliquées ;
- les plages numériques invalides ;
- les nombres non finis ;
- les boutons obligatoires.

La clé exacte `id` est interdite car elle est réservée par le système de callback local de Paper.

## Scheduler

Le module Scheduler est actuellement en cours d'implémentation.

L'API publique utilise `Duration` plutôt que des ticks.

Direction actuelle :

```java
ScheduledTask run(Runnable task);

ScheduledTask runAsync(Runnable task);

ScheduledTask runLater(Duration delay, Runnable task);

ScheduledTask runLaterAsync(Duration delay, Runnable task);

ScheduledTask repeat(
        Duration delay,
        Duration period,
        Runnable task
);

ScheduledTask repeatAsync(
        Duration delay,
        Duration period,
        Runnable task
);
```

`ScheduledTask` encapsule une tâche planifiée sans exposer directement `BukkitTask`.

La conversion `Duration -> ticks` reste un détail interne.

La partie Async du même chapitre fournit séparément une API basée sur `CompletableFuture`, afin de distinguer la planification Bukkit de l'exécution asynchrone avec résultat.

## Cycle de vie

`AtlasPlugin` orchestre l'ordre d'initialisation et de fermeture des services.

Ordre général au démarrage :

```text
Configuration
    ↓
Base de données
    ↓
Migrations
    ↓
Player Data
    ↓
Messages
    ↓
Custom Items
    ↓
Listeners
    ↓
Commandes
```

À l'arrêt, les données joueur sont sauvegardées avant la fermeture des connexions SQL.

## Build

Cloner le dépôt puis lancer Maven :

```bash
git clone https://github.com/Tablelkea/Atlas.git
cd Atlas
mvn clean package
```

Le JAR généré est disponible dans :

```text
target/
```

Le Maven Shade Plugin inclut les dépendances nécessaires à la couche base de données dans le JAR final. Paper reste une dépendance `provided`.

## Installation

1. Compiler Atlas avec Maven.
2. Copier le JAR généré dans le dossier `plugins` du serveur Paper.
3. Démarrer le serveur une première fois.
4. Configurer MySQL dans `plugins/Atlas/config.yml`.
5. Vérifier que la base existe et que l'utilisateur dispose des permissions nécessaires.
6. Redémarrer le serveur.

Atlas exécute automatiquement ses migrations SQL au démarrage.

## Principes de développement

Les nouveaux modules doivent respecter les conventions suivantes :

- exposer les contrats dans `api` ;
- conserver les détails Paper/Bukkit dans `internal` ;
- valider les entrées publiques tôt ;
- utiliser `Optional` pour représenter une absence normale ;
- utiliser `CompletableFuture` pour les opérations asynchrones ;
- ne pas bloquer le thread principal avec la base de données ;
- éviter l'état global mutable ;
- favoriser la composition ;
- rendre explicite le propriétaire des ressources ;
- cloner les objets Bukkit mutables aux frontières d'API ;
- ne pas exposer inutilement JDBC, HikariCP ou `BukkitTask`.

## Roadmap

1. Bootstrap et lifecycle
2. Core Data API et PlayerData
3. Database API et persistance
4. Configuration et messages
5. Commandes et permissions
6. Items, custom items et PDC
7. GUI, inventaires et pagination
8. Dialog API et DialogBuilder
9. Scheduler, async et cooldowns
10. Events et event utilities
11. Players et player utilities
12. Worlds, locations et teleportation
13. Entities et mob utilities
14. Recipes et crafting
15. BossBars, scoreboards et gameplay UI
16. Particles, sounds et effects
17. Serialization et data utilities
18. Cache et performance utilities
19. Registries et extension API
20. Hooks et intégrations externes
21. Logging, debug et profiling
22. Architecture finale, documentation et polish

Les chapitres 1 à 8 sont implémentés. Le chapitre 9 est actuellement en développement.

## Dépôt

Code source :

https://github.com/Tablelkea/Atlas

## Stabilité de l'API

Atlas est actuellement un projet en développement.

Les API publiques, packages, constructeurs et frontières entre services peuvent encore évoluer avant une première version stable. Le projet ne doit donc pas encore être considéré comme une bibliothèque garantissant la compatibilité ascendante.
