# Ajul — projet académique Java et JavaFX

Ajul est une adaptation locale du jeu de société **Azul**, développée dans le cadre du projet de programmation **CS108 à l’EPFL**. Le jeu accueille **deux à quatre joueurs**, humains ou contrôlés par une intelligence artificielle.

Le dépôt présente le moteur de jeu, la représentation des états, une interface JavaFX et un joueur automatique fondé sur **Monte Carlo Tree Search (MCTS)**. Il s’agit d’une application de bureau : elle ne s’exécute pas dans un navigateur.

## Fonctionnalités présentes

- Distribution des tuiles entre les fabriques et le centre de la table.
- Choix des tuiles, lignes de préparation, mur et pénalités du plancher.
- Gestion des tours, des manches, du score et des bonus de fin de partie.
- Interface JavaFX avec déplacement et animation des tuiles.
- Joueurs humains et joueurs MCTS dans une même partie ; le lancement actuel utilise 100 000 itérations par décision de l’IA.
- Graine aléatoire configurable pour reproduire la distribution initiale d’une partie.

Le moteur utilise des représentations compactes par opérations binaires, des états mutables pour jouer/simuler et des instantanés immuables pour l’interface.

## Organisation du dépôt

| Chemin | Rôle |
| --- | --- |
| [`src/ch/epfl/ajul/`](src/ch/epfl/ajul/) | Règles communes, configuration des joueurs, tuiles, points et classement |
| [`src/ch/epfl/ajul/gamestate/`](src/ch/epfl/ajul/gamestate/) | Coups et états du jeu ; sous-dossier `packed/` pour les représentations compactes |
| [`src/ch/epfl/ajul/intarray/`](src/ch/epfl/ajul/intarray/) | Tableaux d’entiers en lecture seule, mutables et immuables |
| [`src/ch/epfl/ajul/mcts/`](src/ch/epfl/ajul/mcts/) | Recherche arborescente et sélection heuristique des coups |
| [`src/ch/epfl/ajul/gui/`](src/ch/epfl/ajul/gui/) | Interface et animations JavaFX ; point d’entrée `Main.java` |
| [`resources/ajul.css`](resources/ajul.css) | Feuille de style de l’interface |
| [`test/ch/epfl/ajul/`](test/ch/epfl/ajul/) | Tests JUnit et simulations de parties |
| [`src/ch/epfl/sigcheck/`](src/ch/epfl/sigcheck/) | Vérifications de signatures des étapes du cours |
| [`src/ch/epfl/cs108/Submit.java`](src/ch/epfl/cs108/Submit.java) | Utilitaire de remise du cours, indépendant du lancement du jeu |
| [`Ajul.iml`](Ajul.iml) et [`src/module-info.java`](src/module-info.java) | Configuration IntelliJ et déclaration du module Java `Ajul` |

Les notes [`REVISION_ETAPES_1_6.md`](REVISION_ETAPES_1_6.md) et [`rapport_conformite_etape7.md`](rapport_conformite_etape7.md) documentent des étapes intermédiaires. Elles ne constituent pas un rapport de validation automatique de la version actuelle.

## Prérequis

- **JDK 24 ou supérieur** avec **JavaFX 26**. La compilation a été vérifiée avec OpenJDK 25.0.2 et JavaFX 26. [JavaFX 26 exige au minimum JDK 24](https://openjfx.io/highlights/26/).
- Le SDK JavaFX correspondant au système utilisé, notamment les modules `javafx.base`, `javafx.graphics` et `javafx.controls`.
- **JUnit Jupiter 5.14.0** pour compiler les tests ; cette version est référencée dans `Ajul.iml`.

Le dépôt ne contient pas de projet Maven/Gradle ni les bibliothèques JavaFX/JUnit. Le fichier IntelliJ référence certaines bibliothèques de l’environnement d’origine : leurs chemins doivent être configurés sur une autre machine.

## Lancer le jeu

### Dans IntelliJ IDEA

1. Ouvrir le projet et sélectionner un JDK compatible.
2. Configurer `src` comme dossier de sources, `test` comme dossier de tests et `resources` comme dossier de ressources.
3. Ajouter le SDK JavaFX 26 au module `Ajul` et vérifier les bibliothèques de test si nécessaire.
4. Créer une configuration d’exécution pour **`ch.epfl.ajul.gui.Main`**.
5. Fournir deux à quatre noms de joueurs comme arguments. Exemple : `Alice _Bot --seed=demo`.

Un nom commençant par `_` désigne une IA : `_Bot` affiche le joueur « Bot ». Un nom sans ce préfixe désigne un joueur humain. L’option `--seed=...` est facultative.

Exemples d’arguments :

```text
Alice Bob
Alice _Bot --seed=demo
Alice Bob _Bot1 _Bot2
```

### En ligne de commande sous Windows / PowerShell

Depuis la racine du dépôt, avec le JDK compatible disponible dans le `PATH` et le SDK JavaFX décompressé :

```powershell
$javaFxLib = "C:/chemin/vers/javafx-sdk-26/lib"
$sourceFiles = Get-ChildItem -LiteralPath src -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force -Path out/classes | Out-Null
javac -encoding UTF-8 --module-path $javaFxLib -d out/classes @sourceFiles
Copy-Item -Path resources/* -Destination out/classes
java --module-path "$javaFxLib;out/classes" --module Ajul/ch.epfl.ajul.gui.Main Alice _Bot --seed=demo
```

Le chemin du SDK est un exemple à adapter. Le nom du dossier Git peut changer, mais les packages `ch.epfl.ajul`, le module `Ajul`, les noms des classes Java et la ressource `ajul.css` doivent rester cohérents.

## Tests et état de validation

Le dossier `test/` rassemble des tests JUnit Jupiter sur les règles, les structures compactes, les états et l’IA. Les simulations de `AjulTUISimulationTest` couvrent également des parties à deux, trois et quatre joueurs.

Lors de la préparation de cette publication :

- La compilation de **toutes les sources de `src/`**, y compris l’interface JavaFX, a réussi avec OpenJDK 25.0.2 et JavaFX 26.
- Les chemins des packages et les noms des classes/fichiers Java actifs ont été vérifiés.
- La compilation de la suite complète de tests échoue actuellement dans `test/ch/epfl/ajul/mcts/MctsNodeTest.java` : ce test appelle `totalPoints()`, une méthode absente de la classe `MctsNode` actuelle. La suite complète n’a donc pas été exécutée et n’est pas présentée comme réussie.
- `test/ch/epfl/ajul/PlayerTest.java` contient uniquement du code commenté et ne définit pas de test actif.
- Le fonctionnement interactif de toutes les parties n’a pas été testé dans le cadre de cette préparation.

## Utilitaire de remise et crédits

`Submit.java` est conservé comme outil du cours ; il n’est pas nécessaire pour compiler ou jouer. Aucun jeton personnel de remise n’est fourni dans le dépôt public. Il peut lire les variables d’environnement locales `CS108_TOKEN_1` et `CS108_TOKEN_2` ; ne jamais enregistrer ces valeurs dans Git. Ne l’exécuter que pour une remise autorisée, avec sa propre configuration locale.

Le code du projet porte les contributions de **Hedi Fourati** et **Fahd Ben Ismail**. Les noms des auteurs et les attributions présentes dans les supports du cours sont conservés. Les identifiants étudiants et les anciens jetons de remise ont été retirés de l’historique publié.

Aucune licence supplémentaire n’a été ajoutée : le code et les éléments pédagogiques conservent leurs attributions d’origine.
