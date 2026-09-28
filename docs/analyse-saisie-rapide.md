# Analyse : saisie rapide de plusieurs quêtes sur un même élément

Statut : analyse uniquement, rien n'est implémenté.
Date : 2026-09-28

## Problème

En mobilité (course, marche rapide), répondre à plusieurs quêtes sur le même élément coûte
beaucoup de taps : ouvrir la quête, répondre, rouvrir la suivante… Cas typiques :

| Élément | Quêtes fréquentes | Réponse « habituelle » |
|---|---|---|
| Passage piéton (`highway=crossing`) | `AddCrossingIsland`, `AddCrossingMarkings`, `AddTactilePavingCrosswalk`, `AddCrossingKerbHeight`, `AddCrossing` | pas d'îlot, marqué, podotactile oui, bordure abaissée |
| Rue | `AddSidewalk`, `AddRoadSurface`, `AddLanes`, `AddOneway`, `AddWayLit` | trottoir des 2 côtés, asphalte, voies marquées 1+1, éclairée |

Deux pistes : (A) un écran groupé avec valeurs par défaut, (B) la dictée vocale.

## Ce qui existe déjà dans SCEE

- **Enchaîner les quêtes d'un même élément** : réglage *UI → Show next quest for this element
  immediately* (`Prefs.SHOW_NEXT_QUEST_IMMEDIATELY`). Après une réponse, la quête suivante du
  même élément s'ouvre directement (`MainBottomSheetViewModel.submitEdit`,
  [MainBottomSheetViewModel.kt](../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/MainBottomSheetViewModel.kt)).
  → Gain immédiat sans développement : à activer si ce n'est pas déjà fait.
- **Quêtes proches / autres quêtes du même élément** affichées pendant qu'un formulaire est
  ouvert (`Prefs.SHOW_NEARBY_QUESTS`, `VisibleQuestsSource.getNearbyQuests`,
  `NearbyQuestsView.kt`).
- **Sélections récentes mises en avant** dans les formulaires à grille d'items.
- **Édition directe des tags** (mode expert, `Action.EditTags`).

Il manque : répondre à *toutes* les quêtes d'un élément en une seule validation.

## Architecture utile (découvertes)

### Une quête = un type + une réponse typée

`OsmElementQuestType<T>`
([OsmElementQuestType.kt](../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/osm/osmquests/OsmElementQuestType.kt))
sépare proprement :

- `Form(on: (QuestAction<T>) -> Unit, …)` : l'UI Compose, qui émet `Answer<T>(value)` ;
- `applyAnswerTo(answer: T, tags: Tags, geometry, timestampEdited)` : la logique qui transforme
  la réponse en tags, **sans UI**.

C'est le point clé : on peut appliquer une réponse sans afficher le formulaire, à condition de
savoir construire une valeur `T`.

Exemples de types `T` :

| Quête | Type de réponse |
|---|---|
| `AddCrossingIsland` | `Boolean` |
| `AddCrossingMarkings` | `Set<CrossingMarkings>` |
| `AddTactilePavingCrosswalk` | `TactilePavingCrosswalkAnswer` |
| `AddCrossingKerbHeight` | `KerbHeight` |
| `AddSidewalk` | `Sides<Sidewalk>` |
| `AddRoadSurface` | `Surface` (enum) |
| `AddLanes` | `LanesAnswer` (sealed, `@Serializable`) |
| `AddWayLit` | `WayLitOrIsStepsAnswer` |
| `AddOneway` | `OnewayAnswer` |

Beaucoup de réponses sont déjà `@Serializable` (kotlinx.serialization) ou des enums : on peut
donc les **stocker** comme valeurs par défaut dans les préférences. À vérifier quête par quête.

### Flux de soumission d'une réponse

1. `OsmQuestFormContainer.onAction` reçoit `Answer<T>`
   ([OsmQuestFormContainer.kt:131](../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/bottom_sheet/quest/OsmQuestFormContainer.kt)).
2. `StringMapChangesBuilder(element.tags)` → `questType.applyAnswerTo(...)` → `UpdateElementTagsAction`.
3. `MainBottomSheetViewModel.submitEdit` → `ElementEditsController.add(type, geometry, source, action, …)`.
4. À l'upload, un changeset par couple (type de quête, source)
   (`OpenChangesetsManager`, `OpenChangesetsDao`).

Conséquence : pour N quêtes validées d'un coup, le plus simple et le plus sûr est de créer
**N edits successifs** (un par type de quête). Les commentaires de changeset, l'historique et
l'annulation restent identiques à aujourd'hui.

### Réglages par quête

`QuestType.hasQuestSettings` + `@Composable QuestSettings(...)`
([QuestType.kt](../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/quest/QuestType.kt)),
avec des réglages différents par preset. C'est l'endroit naturel pour configurer la « réponse
par défaut » d'une quête.

## Piste A : écran groupé avec réponses par défaut (recommandée)

### UX proposée

1. Tap sur un élément qui a ≥ 2 quêtes « rapides » → une feuille **« Réponse rapide »** liste
   chaque quête sur une ligne : icône + valeur par défaut pré-remplie (chip).
2. Tap sur une chip → fait défiler les 2–4 valeurs courantes (ex. bordure : abaissée / affleurante / haute).
3. Appui long ou bouton « … » → ouvre le formulaire complet habituel pour cette quête.
4. Gros bouton **« Tout valider »** → applique toutes les lignes.
5. Les quêtes sans valeur par défaut restent à traiter ensuite par l'enchaînement existant.

### Contraintes de conception

- Code **propre et isolé** du code de base (SCEE suit StreetComplete amont : chaque ligne
  modifiée dans un fichier existant est un conflit de merge potentiel).
- **Activable par un paramètre, pour chaque type de quête.** Désactivé par défaut : sans
  activation, le comportement de l'app est strictement inchangé.

### Organisation du code

Tout le nouveau code vit dans un seul package, sans modifier les classes de quêtes existantes :

```
app/src/commonMain/kotlin/de/westnordost/streetcomplete/quickanswer/
├── QuickAnswerDefinition.kt      // contrat d'une définition (voir ci-dessous)
├── QuickAnswerRegistry.kt        // type de quête -> définition, ordre d'affichage
├── QuickAnswerPrefs.kt           // lecture/écriture des réglages (activé, défaut)
├── QuickAnswerController.kt      // trouve les quêtes de l'élément, applique N réponses (sans UI)
├── QuickAnswerModule.kt          // module Koin
├── ui/
│   ├── QuickAnswersSheet.kt      // la feuille « Réponses rapides »
│   └── QuickAnswerSettings.kt    // bouton ⚡ + dialogue dans Sélection des quêtes
└── definitions/
    ├── CrossingDefinitions.kt    // îlot, marquage, feux, podotactile, bordure
    └── StreetDefinitions.kt      // trottoir, surface, marquage des voies, voies, sens unique, éclairage
```

Tests : `app/src/commonTest/.../quickanswer/` (`CrossingQuickAnswersTest`, `StreetQuickAnswersTest`).

### État (étape 2)

- Implémenté : package complet + 5 définitions « passage piéton » (`AddCrossingIsland`,
  `AddCrossingMarkings`, `AddCrossingSignals`, `AddTactilePavingCrosswalk`,
  `AddCrossingKerbHeight`). `AddCrossing` est exclu : il porte sur un nœud sans tags, les autres
  quêtes n'existent qu'après sa réponse.
- Hors package : `CommonModule.kt` (+2 lignes), `QuestSelectionRow.kt` (+2),
  `MainBottomSheet.kt` (+24), chaînes EN/FR dans `strings_ee.xml` (+6 chacune), une icône.
- Comportement : la feuille remplace le formulaire quand la quête touchée et au moins une autre
  quête du même élément ont la réponse rapide activée. Chaque bouton de valeur fait défiler
  les options puis « Ignorer ». La flèche ouvre le formulaire complet de cette quête.
- Si le réglage *marquage étendu* de `AddCrossingMarkings` est actif, les options deviennent
  des types de marquage (zébra, lignes, échelle, tirets, aucun), car « oui » n'y est pas une
  réponse finale.

**Choix clé : un registre externe plutôt qu'une interface implémentée par les quêtes.**
Les quêtes (`AddCrossingIsland`, etc.) ne sont pas touchées ; une définition les référence
par leur classe. Avantage : zéro diff dans `quests/`, merges amont sans conflit, et on retire
la fonctionnalité en supprimant un package + quelques lignes d'accroche.

```kotlin
/** Réponses rapides pour un type de quête, défini hors de la classe de quête. */
interface QuickAnswerDefinition<T> {
    val questType: KClass<out OsmElementQuestType<T>>
    /** 2 à 4 réponses courantes, dans l'ordre de défilement */
    fun options(element: Element, countryInfo: CountryInfo): List<QuickOption<T>>
    /** clé stable de chaque option, pour mémoriser le défaut dans les préférences */
    fun key(answer: T): String
}

data class QuickOption<T>(val answer: T, val label: StringResource)
```

- Les libellés réutilisent les chaînes **déjà traduites** des formulaires : pas de nouvelle
  traduction (sauf quelques chaînes pour l'écran lui-même).
- Clé d'option en `String` plutôt que sérialisation JSON : lisible dans l'export des réglages,
  robuste si une classe de réponse change en amont.

### Paramètres

| Réglage | Où | Clé de préférence |
|---|---|---|
| Activer la réponse rapide pour ce type de quête | dialogue de la quête dans *Sélection des quêtes* | `${questPrefix}qs_${name}_quick_answer` |
| Réponse par défaut pour ce type | même dialogue | `${questPrefix}qs_${name}_quick_answer_default` |

- Suit la convention existante `qs_<quest_name>_<something>` et `questPrefix(prefs)` de
  [QuestSettings.kt](../app/src/commonMain/kotlin/de/westnordost/streetcomplete/quests/QuestSettings.kt) :
  réglages **par preset** automatiquement quand l'option « réglages par preset » est active, et
  inclus dans l'import/export existant.
- Pas de réglage global supplémentaire : la fonction est active dès qu'au moins un type de
  quête l'est.

### Points d'accroche dans le code existant (seules modifications hors du package)

1. `MainBottomSheetViewModel.showOsmQuest` : si l'élément porte ≥ 2 quêtes visibles dont la
   réponse rapide est activée → ouvrir `ShownBottomSheet.QuickAnswers` au lieu du formulaire.
   Une condition + un appel au registre.
2. `ShownBottomSheet` : un nouveau cas `QuickAnswers(element, geometry, quests)`.
3. `MainBottomSheet.kt` : une branche `is ShownBottomSheet.QuickAnswers -> QuickAnswersSheet(...)`.
4. `QuestSelectionRow.kt` : afficher le réglage « Réponse rapide » pour les types présents dans
   le registre (icône ou entrée dans le dialogue existant).
5. `StreetCompleteApplication` / `Koin.kt` : ajouter `quickAnswerModule`.

Estimation : ~30 à 50 lignes modifiées au total hors du nouveau package.

### Soumission (`QuickAnswerSubmitter`)

Pour chaque ligne validée, dans l'ordre :

1. relire l'élément **avec les edits déjà appliqués** (`MapDataWithEditsSource`) ;
2. vérifier que la quête est toujours applicable (`isApplicableTo`) — une réponse peut en rendre
   une autre caduque ;
3. `StringMapChangesBuilder` → `questType.applyAnswerTo(...)` → `UpdateElementTagsAction` ;
4. `ElementEditsController.add(...)` : un edit par type de quête, donc changesets, historique
   et annulation identiques au fonctionnement actuel.

Classe sans UI → testable unitairement (réponse par défaut → tags attendus).

### Déploiement progressif

D'abord les 5 quêtes « passage piéton », puis les 5 quêtes « rue ».

### Taille estimée

- Infra (interface, feuille, stockage, boucle de soumission) : ~500–800 lignes Kotlin.
- Par quête : ~20–40 lignes (liste de réponses + libellé).
- Tests : un test unitaire par quête (réponse par défaut → tags attendus) + un test de la boucle.

### Risques

- **Qualité des données** : des défauts favorisent les réponses sans regarder. Parade : jamais
  d'envoi automatique, valeurs visibles avant « Tout valider », contrôle de proximité
  (`SurveyChecker`) déjà présent.
- **Dépendances entre quêtes** : gérées par la revalidation `isApplicableTo` à chaque étape.
- **Quêtes à réponse dépendant de l'élément** (ex. `AddLanes` sur une rue à sens unique) :
  `quickAnswers` reçoit l'élément et peut filtrer.
- **Divergence avec StreetComplete amont** : code isolé dans le package `quickanswer`, aucune
  classe de quête modifiée, 5 points d'accroche → conflits de merge limités. Si une classe de
  réponse change en amont, seule la définition correspondante casse à la compilation.

### État (étape 3)

- 6 définitions « rue » : `AddSidewalk` (des deux côtés / aucun / séparé), `AddRoadSurface`
  (asphalte, pavés, béton, pavés taillés), `AddLaneMarkings` (oui / non), `AddLanes`
  (1 par sens, 2 par sens, une seule voie), `AddOneway` (pas à sens unique), `AddWayLit`
  (oui / non).
- `AddLanes` n'apparaît qu'après `lane_markings=yes` : elle arrive donc dans une deuxième feuille,
  après la réponse à `AddLaneMarkings`.
- Nouveau filtre optionnel `appliesTo(element)` dans une définition : `AddLanes` est exclue de la
  feuille sur les sens uniques (« 1 par sens » n'y a pas de sens).
- Le sens unique ne propose que « pas à sens unique » : la direction dépend du sens de dessin de
  la voie, à voir sur la carte dans le formulaire complet.
- Filtre dans *Sélection des quêtes* → menu ⋮ → « Montrer seulement les quêtes à réponse
  rapide ». Modifie `QuestSelectionViewModel.kt` (+6), `QuestSelectionScreen.kt` (+3),
  `QuestSelectionTopAppBar.kt` (+12, paramètres optionnels).

### État (étape 4)

- Réponse par défaut **par preset** : déjà assurée par `questPrefix` (réglages par preset actifs).
- Nouvelle case par type de quête : « Utiliser la dernière réponse donnée comme réponse par
  défaut » (`qs_<quête>_quick_answer_remember_last`).
- `QuickAnswerLastAnswerRecorder` écoute les edits (`ElementEditsSource.Listener`) : aucune
  modification du formulaire normal. Pour retrouver l'option, chaque option est rejouée sur les
  tags d'avant l'edit ; celle qui produit exactement les mêmes changements devient le défaut.
  Une réponse du formulaire complet compte donc aussi ; une réponse sans option rapide (ex.
  podotactile « incorrect ») ne change rien.
- Le recorder s'enregistre lui-même comme écouteur. Il est injecté dans `ApplicationInitializer`,
  donc créé au démarrage de l'app, mais pas via `createdAtStart` : `startKoin` précède
  l'initialisation de `Prefs.preferences`, et l'app plantait au lancement.

## Piste B : dictée vocale

### Principe

Sélectionner un élément, dire « pas d'îlot, marqué, podotactile, bordure abaissée », l'app
remplit les réponses.

### Faisabilité technique

- Reconnaissance : `android.speech.SpeechRecognizer` (code `androidMain`, pas d'iOS). Mode
  hors ligne via `SpeechRecognizer.createOnDeviceSpeechRecognizer` (Android 12+/13+).
- Aucune reconnaissance vocale n'existe aujourd'hui dans le projet ; il faudrait ajouter la
  permission `RECORD_AUDIO` (SCEE en demande déjà deux de plus que SC, cf. README).
- **Point bloquant F-Droid** : sur un téléphone dégooglisé, aucun service de reconnaissance
  n'est souvent installé. Alternative embarquée (ex. Vosk, modèle FR ~50 Mo) → taille d'APK et
  licence à évaluer.
- Analyse du texte : faire correspondre la phrase aux réponses. Astuce réaliste : comparer le
  texte reconnu aux **libellés déjà traduits** des réponses rapides de la piste A (mêmes
  chaînes que l'UI), avec un matching tolérant. Pas besoin de grammaire par langue.

### Limites

- En courant : souffle, bruit du vent/trafic → taux d'erreur élevé ; il faut un écran de
  confirmation (donc on revient à la piste A pour valider).
- Vocabulaire ambigu entre quêtes (« oui », « non ») : il faut nommer la quête (« îlot non »).

### Conclusion

La dictée n'est pas une alternative à A mais une **couche au-dessus** : elle a besoin de la
même liste de réponses rapides et de la même boucle de soumission groupée.

## Recommandation et ordre de travail

1. **Tout de suite, sans code** : activer *Show next quest for this element immediately*.
2. **Piste A, MVP passages piétons** : package `quickanswer` (registre, réglages, submitter,
   feuille) + 5 définitions.
3. **Piste A, rues** : `AddSidewalk`, `AddRoadSurface`, `AddLanes`, `AddOneway`, `AddWayLit`.
4. **Défauts mémorisés** par preset + option « dernière réponse ».
5. **Piste B (optionnelle)** : bouton micro dans la feuille « Réponse rapide », matching sur les
   libellés existants, confirmation visuelle obligatoire.
