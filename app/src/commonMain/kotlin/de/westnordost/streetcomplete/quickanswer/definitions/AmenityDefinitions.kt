package de.westnordost.streetcomplete.quickanswer.definitions

import de.westnordost.streetcomplete.quests.accepts_cards.AddAcceptsCards
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.baby_changing_table.BabyChangingTableAnswer
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer
import de.westnordost.streetcomplete.quests.bench_material.AddBenchMaterial
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability
import de.westnordost.streetcomplete.quests.diet_type.DietAvailabilityAnswer
import de.westnordost.streetcomplete.quests.parking_access.AddBikeParkingAccess
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess
import de.westnordost.streetcomplete.quests.seating.AddSeating
import de.westnordost.streetcomplete.quests.seating.Seating
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.toilets_fee.ToiletFee
import de.westnordost.streetcomplete.quests.toilets_fee.ToiletFeeAnswer
import de.westnordost.streetcomplete.quests.toilets_fee.ToiletFeeForNonCustomers
import de.westnordost.streetcomplete.quickanswer.QuickAnswerDefinition
import de.westnordost.streetcomplete.quickanswer.QuickOption
import de.westnordost.streetcomplete.resources.*

/* The wheelchair access quests are left out on purpose: their answer carries the descriptions
 * entered in the form in a mutable property, so a quick answer could re-use a previously entered
 * one. */

/** Quick answers for quests about shops, restaurants and similar places */
val shopQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddAcceptsCash::class) { yesNoOptions },
    QuickAnswerDefinition(AddAcceptsCards::class) { cardOptions },
    QuickAnswerDefinition(AddSeating::class) { seatingOptions },
    QuickAnswerDefinition(AddToiletAvailability::class) { yesNoOptions },
    QuickAnswerDefinition(AddVegetarian::class) { vegetarianOptions },
    QuickAnswerDefinition(AddVegan::class) { veganOptions },
)

/** Quick answers for quests about bicycle parkings */
val bikeParkingQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddBikeParkingType::class) { bikeParkingTypeOptions },
    QuickAnswerDefinition(AddBikeParkingCover::class) { noYesOptions },
    QuickAnswerDefinition(AddBikeParkingAccess::class) { parkingAccessOptions },
)

/** Quick answers for quests about public toilets */
val toiletsQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddToiletsFee::class) { toiletsFeeOptions },
    QuickAnswerDefinition(AddBabyChangingTable::class) { babyChangingTableOptions },
)

/** Quick answers for quests about benches */
val benchQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddBenchBackrest::class) { benchBackrestOptions },
    QuickAnswerDefinition(AddBenchMaterial::class) { benchMaterialOptions },
)

private val noYesOptions = yesNoOptions.reversed()

private val cardOptions = listOf(
    QuickOption("both", CardAcceptance.DEBIT_AND_CREDIT, Res.string.quick_answer_cards_both),
    QuickOption("none", CardAcceptance.NEITHER_DEBIT_NOR_CREDIT, Res.string.quick_answer_cards_none),
    QuickOption("debit", CardAcceptance.DEBIT_CARDS_ONLY, Res.string.quick_answer_cards_debit),
    QuickOption("credit", CardAcceptance.CREDIT_CARDS_ONLY, Res.string.quick_answer_cards_credit),
)

private val seatingOptions = listOf(
    QuickOption("both", Seating.INDOOR_AND_OUTDOOR, Res.string.quick_answer_seating_both),
    QuickOption("indoor", Seating.ONLY_INDOOR, Res.string.quick_answer_seating_indoor),
    QuickOption("outdoor", Seating.ONLY_OUTDOOR, Res.string.quick_answer_seating_outdoor),
    QuickOption("takeaway", Seating.TAKEAWAY_ONLY, Res.string.quick_answer_seating_takeaway),
)

private val vegetarianOptions = listOf<QuickOption<DietAvailabilityAnswer>>(
    QuickOption("yes", DietAvailability.YES, Res.string.quest_generic_hasFeature_yes),
    QuickOption("no", DietAvailability.NO, Res.string.quest_generic_hasFeature_no),
    QuickOption("only", DietAvailability.ONLY, Res.string.quest_hasFeature_only),
)

private val veganOptions = listOf<QuickOption<DietAvailabilityAnswer>>(
    QuickOption("no", DietAvailability.NO, Res.string.quest_generic_hasFeature_no),
    QuickOption("yes", DietAvailability.YES, Res.string.quest_generic_hasFeature_yes),
    QuickOption("only", DietAvailability.ONLY, Res.string.quest_hasFeature_only),
)

private val bikeParkingTypeOptions = listOf(
    QuickOption(BikeParkingType.STANDS.name, BikeParkingType.STANDS, Res.string.quick_answer_bike_parking_stands),
    QuickOption(BikeParkingType.WALL_LOOPS.name, BikeParkingType.WALL_LOOPS, Res.string.quick_answer_bike_parking_wall_loops),
    QuickOption(BikeParkingType.SHED.name, BikeParkingType.SHED, Res.string.quest_bicycle_parking_type_shed),
    QuickOption(BikeParkingType.LOCKERS.name, BikeParkingType.LOCKERS, Res.string.quest_bicycle_parking_type_locker),
    QuickOption(BikeParkingType.TWO_TIER.name, BikeParkingType.TWO_TIER, Res.string.quest_bicycle_parking_type_two_tier),
)

private val parkingAccessOptions = listOf(
    QuickOption("yes", ParkingAccess.YES, Res.string.quest_access_yes),
    QuickOption("customers", ParkingAccess.CUSTOMERS, Res.string.quick_answer_access_customers),
    QuickOption("private", ParkingAccess.PRIVATE, Res.string.quick_answer_access_private),
)

private val toiletsFeeOptions = listOf<QuickOption<ToiletFeeAnswer>>(
    QuickOption("no", ToiletFee(false), Res.string.quest_generic_hasFeature_no),
    QuickOption("yes", ToiletFee(true), Res.string.quest_generic_hasFeature_yes),
    QuickOption("non_customers", ToiletFeeForNonCustomers, Res.string.quick_answer_toilets_fee_non_customers),
)

private val babyChangingTableOptions = listOf(
    QuickOption("no", BabyChangingTableAnswer.NO, Res.string.quest_generic_hasFeature_no),
    QuickOption("yes", BabyChangingTableAnswer.YES, Res.string.quest_generic_hasFeature_yes),
)

private val benchBackrestOptions = listOf(
    QuickOption("yes", BenchBackrestAnswer.YES, Res.string.quest_generic_hasFeature_yes),
    QuickOption("no", BenchBackrestAnswer.NO, Res.string.quest_generic_hasFeature_no),
)

private val benchMaterialOptions = listOf(
    QuickOption(BenchMaterial.WOOD.name, BenchMaterial.WOOD, Res.string.quest_material_wood),
    QuickOption(BenchMaterial.METAL.name, BenchMaterial.METAL, Res.string.quest_material_metal),
    QuickOption(BenchMaterial.CONCRETE.name, BenchMaterial.CONCRETE, Res.string.quest_material_concrete),
    QuickOption(BenchMaterial.STONE.name, BenchMaterial.STONE, Res.string.quest_material_stone),
    QuickOption(BenchMaterial.PLASTIC.name, BenchMaterial.PLASTIC, Res.string.quest_material_plastic),
)
