package com.hos.happy.plants.feature.garden.components

data class PlantTriviaItem(
    val category: String,
    val icon: String,
    val title: String,
    val fact: String,
    val practicalTip: String
)

object PlantTriviaData {
    val items = listOf(
        PlantTriviaItem(
            category = "Nighttime Oxygen",
            icon = "🌙",
            title = "Bedroom Air Purifiers",
            fact = "Snake Plants (Sansevieria) and Aloe Vera perform Crassulacean Acid Metabolism (CAM) — they absorb CO₂ and release oxygen during the night instead of during the day.",
            practicalTip = "Place a Snake Plant or Aloe on your bedroom nightstand for cleaner air while you sleep."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "☀️",
            title = "The Solar Collectors",
            fact = "Plant leaves contain chlorophyll pigments that absorb blue and red light wavelengths, but reflect green wavelengths — which is why most plants appear green to human eyes.",
            practicalTip = "Dust your houseplants monthly so dust particles do not block light absorption by chlorophyll."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "🍃",
            title = "Stomata: Leaf Breathing Pores",
            fact = "Leaves have microscopic mouth-like pores called stomata that open and close using guard cells to balance gas exchange and water transpiration.",
            practicalTip = "Avoid spraying heavy oils or waxes on leaves during hot afternoons to keep stomata breathing freely."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "💡",
            title = "Phototropism in Action",
            fact = "Plants grow toward light thanks to an auxin hormone that concentrates on the shaded side of the stem, elongating cells there and bending the stem toward the sun.",
            practicalTip = "Rotate your potted plants a quarter turn each week to maintain even, symmetrical growth."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "💧",
            title = "Guttation: Morning Droplets",
            fact = "Water beads found on leaf tips in the morning are often guttation, not dew. When soil moisture is high and transpiration is low at night, root pressure pushes water out through special glands called hydathodes.",
            practicalTip = "Guttation is normal, but if droplets deposit white crusts, your tap water may have excess mineral salts."
        ),
        PlantTriviaItem(
            category = "Nighttime Oxygen",
            icon = "🌵",
            title = "Cactus Night Respiration",
            fact = "Like snake plants, many desert cacti only open their stomata during cool desert nights to conserve water, storing CO₂ as malic acid until daylight arrives.",
            practicalTip = "Water desert cacti early in the morning so excess moisture evaporates before nightfall."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "🌈",
            title = "Variegated Leaves Need More Light",
            fact = "The white, cream, or pink areas of variegated leaves lack chlorophyll entirely and cannot photosynthesize, leaving the green sections to support the whole plant.",
            practicalTip = "Give variegated plants brighter indirect light than solid green varieties to prevent reversion."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "🌿",
            title = "Autumn Leaf Secrets",
            fact = "The vibrant reds, oranges, and yellows of autumn foliage were present in the leaf all summer, hidden by green chlorophyll that breaks down when days get shorter.",
            practicalTip = "Indoor ficus and deciduous plants may drop older yellowing leaves naturally as autumn daylight shortens."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "💧",
            title = "Water as an Electron Donor",
            fact = "During photosynthesis, plants split water molecules (H₂O) into oxygen and hydrogen protons. Every breath of oxygen on Earth was once part of a water molecule inside a plant.",
            practicalTip = "Use room-temperature filtered or rested water to protect delicate root hair membranes."
        ),
        PlantTriviaItem(
            category = "Photosynthesis",
            icon = "💨",
            title = "Indoor Transpiration Humidity",
            fact = "Over 95% of the water absorbed by houseplant roots evaporates through their leaves via transpiration, naturally humidifying dry indoor apartments.",
            practicalTip = "Group plants close together to create an elevated humidity micro-climate for tropical species."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🌿",
            title = "Why Monstera Has Holes",
            fact = "The splits in Monstera deliciosa leaves are called fenestrations. In wild rainforests, they let hurricane winds pass through and allow sunlight to filter down to lower leaves.",
            practicalTip = "Giving your Monstera brighter indirect light signals the plant to produce larger leaves with splits."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🧗",
            title = "Pothos Wild Giants",
            fact = "In wild rainforests, the common Money Plant (Pothos) climbs tall tree trunks and develops massive leaves over 3 feet wide, looking nothing like tabletop pots.",
            practicalTip = "Train your Pothos onto a moist moss pole or wooden trellis to encourage larger adult leaves."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🌧️",
            title = "Drip Tips on Tropical Leaves",
            fact = "Many rainforest plants like Ficus and Philodendron have elongated leaf tips called drip tips that rapidly shed tropical rainwater, preventing fungal growth.",
            practicalTip = "Ensure good air circulation around indoor plants to replicate the drying breeze of rainforests."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🪵",
            title = "Aerial Roots: Tree Huggers",
            fact = "Monstera and orchids produce aerial roots that grab humidity from the air and anchor the plant to host tree bark rather than soil.",
            practicalTip = "Tuck stray Monstera aerial roots back into the potting mix or mist them regularly with soft water."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🌺",
            title = "Epiphytic Life in the Canopies",
            fact = "Epiphytes like Staghorn Ferns, Bromeliads, and Phalaenopsis orchids grow on tree branches without taking nutrients from the tree, surviving on leaf debris and rainfall.",
            practicalTip = "Mount epiphytes on cork bark with sphagnum moss or pot them in chunky, airy orchid bark."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🦚",
            title = "Velvet Leaves: Light Traps",
            fact = "Shade-dwelling rainforest plants like Anthurium clarinervium develop velvety leaf textures with microscopic lens-shaped cells that capture dim understory light.",
            practicalTip = "Keep velvety-leaved plants away from harsh direct sunlight, which can burn their delicate light-capturing cells."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🛡️",
            title = "Wax Coatings Against Mold",
            fact = "Rubber plants (Ficus elastica) produce thick glossy cuticles of natural wax that prevent tropical pathogens from penetrating leaf cells and reduce water loss.",
            practicalTip = "Wipe Rubber Tree leaves with a damp microfiber cloth to remove dust and restore their glossy shield."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🪴",
            title = "Bromeliad Central Tanks",
            fact = "Bromeliads form central water cups with overlapping leaf bases that hold water and harbor tiny aquatic ecosystems of tree frogs and insects in the wild.",
            practicalTip = "Keep the central cup of your indoor bromeliad filled with fresh rainwater, flushing it weekly."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🦋",
            title = "Prayer Plant Nyctinasty",
            fact = "Maranta and Calathea fold their leaves upright at night like praying hands. This movement (nyctinasty) may reduce water loss, shed excess dew, or deter pests.",
            practicalTip = "Calatheas need consistent humidity above 50% and pure water to prevent crispy leaf borders."
        ),
        PlantTriviaItem(
            category = "Rainforest",
            icon = "🌱",
            title = "Fern Spores Under Leaves",
            fact = "Ferns do not produce seeds or flowers. Instead, they reproduce via microscopic spores housed in brown clusters called sori on the undersides of their fronds.",
            practicalTip = "Do not scrape off brown dots lined up neatly under fern fronds — they are healthy reproductive spores."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "🥔",
            title = "ZZ Plant Tuber Superpower",
            fact = "Underneath the potting soil, ZZ Plants (Zamioculcas zamiifolia) store water in potato-like tuber rhizomes, enabling them to survive up to 4 months of drought.",
            practicalTip = "Overwatering is the only true way to harm a ZZ Plant. Always wait until the potting soil is 100% bone dry."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "💧",
            title = "Sansevieria Cellular Water Tanks",
            fact = "Snake plant leaves consist of special succulent parenchyma tissue packed with mucilage water gel that sustains the plant across months of zero rainfall.",
            practicalTip = "Pot Snake Plants in heavy terracotta pots with gritty succulent soil to prevent root rot."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "☀️",
            title = "Cactus Spines are Modified Leaves",
            fact = "Cactus spines are actually evolved leaves that lost their photosynthetic green tissue to prevent water evaporation, provide shade, and defend against thirsty animals.",
            practicalTip = "Desert cacti require south-facing windows with 4–6 hours of direct sun to thrive indoors."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "✨",
            title = "Farina: Natural Plant Sunscreen",
            fact = "The powdery white or pastel film on succulents like Echeveria and Graptopetalum is farina (epicuticular wax), which acts as waterproof sunscreen.",
            practicalTip = "Never rub or spray oil on succulents; touching farina rubs it off permanently on that leaf."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "🦔",
            title = "Cactus Accordion Ribs",
            fact = "The vertical ridges on columnar cacti act like an accordion, expanding outward when rain arrives and contracting during droughts without tearing stem tissue.",
            practicalTip = "When your cactus ribs look slightly sunken, it is ready for a thorough, deep drenching."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "🪨",
            title = "Living Stones: Lithops Camouflage",
            fact = "Lithops succulents mimic desert pebbles in South Africa to hide from grazing herbivores, with translucent top windows that channel sunlight down into underground stems.",
            practicalTip = "Water Lithops only a few times per year, and never during their winter leaf-splitting phase."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "🌿",
            title = "Jade Plant Longevity",
            fact = "Crassula ovata (Jade Plant) can live for over 70 years, passing down through family generations and developing thick, gnarled bonsai-like woody trunks.",
            practicalTip = "Prune top branches in spring to stimulate branching and thicken the main jade trunk."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "🌵",
            title = "String of Pearls Bead Windows",
            fact = "Senecio rowleyanus has spherical pea-shaped leaves to minimize surface area exposed to scorching sun, featuring a narrow translucent strip to funnel light inside.",
            practicalTip = "Water String of Pearls from the bottom saucer to prevent moisture from rotting delicate stems."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "🪴",
            title = "Haworthia Translucent Windows",
            fact = "Haworthia cooperi features swollen leaves with clear, crystalline tips that allow sunlight into the interior of the leaf while the body remains buried in desert sand.",
            practicalTip = "Haworthias prefer bright indirect light over scorching direct afternoon sun."
        ),
        PlantTriviaItem(
            category = "Desert",
            icon = "💧",
            title = "Succulent Propagation From One Leaf",
            fact = "Many succulent leaves carry undifferentiated meristematic cells at their base, allowing a single detached leaf to sprout both roots and an entire new plant clone.",
            practicalTip = "Let succulent leaves callous over for 3 days on dry soil before misting their base."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🍄",
            title = "Mycorrhizal Wood Wide Web",
            fact = "Over 90% of land plants form symbiotic relationships with mycorrhizal fungi, trading plant sugars for fungal water and minerals across vast underground networks.",
            practicalTip = "Incorporate beneficial mycorrhizal fungi into potting mix when repotting root-bound plants."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🌱",
            title = "Root Hairs: The Real Absorbers",
            fact = "Almost all water and nutrient absorption happens through microscopic single-cell root hairs, which can die within minutes if completely dried or drowned without oxygen.",
            practicalTip = "Always ensure potting soil contains chunky perlite or pumice to supply oxygen to root hairs."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🪱",
            title = "Worm Castings Gold",
            fact = "Earthworm castings contain beneficial soil microbes, natural humic acid, and slow-release nitrogen that strengthen plant immune systems against pests.",
            practicalTip = "Top-dress your potted houseplants with 1–2 tablespoons of worm castings each spring."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🏺",
            title = "Terracotta Pot Breathing Action",
            fact = "Unglazed terracotta clay is naturally porous, allowing oxygen to reach plant roots and drawing excess moisture away from soggy soil walls.",
            practicalTip = "Use terracotta pots for moisture-sensitive plants like cacti, succulents, and hoyas."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🌿",
            title = "Root Bound Stress Signals",
            fact = "When plant roots circle tightly around the inner edge of a pot, water runs straight down the sides without wetting the root core, causing chronic wilting.",
            practicalTip = "Gently loosen and untangle outer circling roots before stepping up one pot size."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🧂",
            title = "Mineral Salt Buildup Signs",
            fact = "White crusty deposits on soil surface or pot rims are evaporated mineral salts from tap water and synthetic fertilizers that can scorch delicate roots.",
            practicalTip = "Flush potting soil with generous filtered water twice a year until it drains freely out the bottom."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🍂",
            title = "Soil Aeration With Chopsticks",
            fact = "Over time, indoor watering compacts potting soil into a dense brick, starving roots of vital oxygen and creating anaerobic pockets.",
            practicalTip = "Gently poke a wooden chopstick into the top 2 inches of soil monthly to aerate compacted mix."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🌰",
            title = "Rhizomes vs Tubers vs Bulbs",
            fact = "Underground plant storage takes different evolutionary forms: Caladiums have tubers, Peace Lilies have rhizomes, and Amaryllis grow true layered bulbs.",
            practicalTip = "Never bury tuber or rhizome tops too deep; keep them level with the soil surface."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🪴",
            title = "Why Drainage Holes Are Mandatory",
            fact = "Without drainage holes, stagnant water pools at the bottom of the pot, suffocating roots and fostering Pythium root rot fungus within days.",
            practicalTip = "Always use pots with drainage holes, or use a plastic nursery pot inside a decorative cachepot."
        ),
        PlantTriviaItem(
            category = "Underground",
            icon = "🌾",
            title = "Perlite vs Vermiculite Differences",
            fact = "Perlite is popped volcanic glass that improves drainage and aeration, while vermiculite is expanded mica that holds onto moisture and nutrients.",
            practicalTip = "Add perlite to cactus and aroid mixes, but use vermiculite for moisture-loving ferns."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "🗣️",
            title = "Talking to Plants Really Works",
            fact = "Research confirms talking softly to houseplants benefits them: humans exhale carbon dioxide (CO₂) to fuel photosynthesis, and sound waves stimulate plant growth.",
            practicalTip = "Spend 1–2 minutes each morning checking on your plants and inspecting leaf undersides."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "📻",
            title = "Plants Hear Running Water",
            fact = "Botanical acoustic experiments show pea seedlings can detect acoustic vibrations of running water inside pipes and steer their root growth directly toward the sound.",
            practicalTip = "Consistent bottom-watering encourages deeper, stronger root networks toward moisture."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "⚠️",
            title = "Volatile Alarm Chemicals (VOCs)",
            fact = "When caterpillars chew on a leaf, the damaged plant releases volatile organic compounds into the air, signaling nearby plants to boost defensive tannins.",
            practicalTip = "Quarantine any pest-infested plant immediately to prevent pest spread to neighboring greens."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "🦗",
            title = "Attracting Pest Bodyguards",
            fact = "Some plants emit specific chemical distress pheromones when under attack that summon predator insects like ladybugs and parasitic wasps to eat the pests.",
            practicalTip = "Introduce natural beneficial predators like ladybugs for organic indoor pest management."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "⚡",
            title = "Electrical Impulses in Plants",
            fact = "Plants transmit electrical action potentials through their vascular phloem when touched, wounded, or stressed, operating much like a primitive nervous system.",
            practicalTip = "Handle newly transplanted roots gently to minimize cellular electrical shock."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "🎵",
            title = "Music Stimulates Seed Germination",
            fact = "Studies indicate harmonic sound frequencies between 115Hz and 250Hz stimulate cellular cytoplasm streaming and boost seed germination rates.",
            practicalTip = "Playing classical or acoustic music near your indoor seedlings may encourage vigorous sprouting."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "👥",
            title = "Kin Recognition in Root Networks",
            fact = "Plants grown next to genetic siblings share soil resources politely, whereas plants grown next to unrelated strangers compete aggressively with defensive roots.",
            practicalTip = "Propagated sibling cuttings from the same mother plant often grow harmoniously in shared pots."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "☀️",
            title = "Circadian Clock in Houseplants",
            fact = "Plants maintain an internal 24-hour circadian clock regulated by photoreceptor proteins, preparing enzyme production before sunrise each day.",
            practicalTip = "Keep indoor plant lighting schedules consistent by using a simple 12-hour plug-in timer."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "🌡️",
            title = "Thermal Sensing in Plant Cells",
            fact = "Plants detect temperature fluctuations down to 1°C using specialized phytochrome proteins, triggering cellular antifreeze sugars during sudden cold snaps.",
            practicalTip = "Keep houseplants away from cold air-conditioner blasts and drafty winter windowpanes."
        ),
        PlantTriviaItem(
            category = "Senses",
            icon = "👃",
            title = "Sensing Ripening Ethylene Gas",
            fact = "Plants sense atmospheric ethylene gas released by ripening fruit, which can trigger premature flower dropping and leaf yellowing in sensitive houseplants.",
            practicalTip = "Keep ornamental blooming plants like Orchids and Bromeliads away from fruit bowls of bananas."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🌸",
            title = "Orchid Lip Landing Strips",
            fact = "Orchid flowers feature a modified lower petal called a labellum (lip), which serves as an aerodynamic landing strip specifically designed for insect pollinators.",
            practicalTip = "Keep blooming orchids away from drafty vents to prolong flower display for up to 3 months."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🎨",
            title = "Ultraviolet Bullseyes on Petals",
            fact = "Many flower petals that look solid yellow or white to humans exhibit vivid ultraviolet target patterns visible only to bees and butterflies.",
            practicalTip = "Place outdoor flowering planters in sunny spots where pollinators can easily spot them."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🍫",
            title = "Chocolate-Scented Flowers",
            fact = "The Mexican Chocolate Cosmos flower (Cosmos atrosanguineus) emits a genuine rich cocoa scent produced by natural vanillin and organic aroma compounds.",
            practicalTip = "Choose scented flowering plants like Hoyas or Jasmine to naturally perfume indoor rooms."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🤍",
            title = "Peace Lily White Spathes",
            fact = "The white petal-like structure on a Peace Lily is not a true flower, but a modified leaf called a spathe that shields the spadix flower spike inside.",
            practicalTip = "Cut spent brown spathes at the base of the stem to direct energy into fresh blooms."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🦇",
            title = "Bat-Pollinated Night Bloomers",
            fact = "Night-blooming flowers like the Queen of the Night cactus bloom exclusively after dark, producing musky fragrances and white petals visible to nocturnal bats.",
            practicalTip = "Watch for Queen of the Night cactus buds in midsummer — each giant flower lasts only a single night."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🐝",
            title = "Buzz Pollination Requirement",
            fact = "Plants like tomatoes and eggplants will not drop their pollen for ordinary breezes; they require bees to vibrate flight muscles at middle-C frequency.",
            practicalTip = "Gently shake tomato stems or use an electric toothbrush on flowers when growing indoor tomatoes."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🍂",
            title = "Hydrangea Color Relies on Soil pH",
            fact = "Hydrangea macrophylla flowers change color based on aluminum availability: acidic soil (< 6.0) produces blue blooms, while alkaline soil produces pink blooms.",
            practicalTip = "Add coffee grounds or sulfur to lower soil pH for blue blooms; add garden lime for pinks."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🌺",
            title = "Anthurium Heart-Shaped Spathes",
            fact = "Anthuriums produce brightly colored, waxy spathes in red, pink, or white that remain fresh for months, making them one of the longest-lasting cut flowers.",
            practicalTip = "Place Anthuriums in bright, warm indirect light with high humidity to encourage continuous blooms."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "🪴",
            title = "Hoya Porcelain Wax Flowers",
            fact = "Hoyas produce clusters of star-shaped, porcelain-like flowers that release sweet drops of nectar and heavenly aromas during the evening.",
            practicalTip = "Never prune off bare flowering spurs (peduncles) on Hoyas; new flowers re-emerge from the same spot."
        ),
        PlantTriviaItem(
            category = "Flowers",
            icon = "❄️",
            title = "Winter Blooming Christmas Cactus",
            fact = "Schlumbergera (Christmas Cactus) requires shorter days (12–14 hours of total darkness) and cool nights (15°C) to initiate its colorful winter flower buds.",
            practicalTip = "Give Holiday Cacti uninterrupted darkness in a spare room every evening starting in October."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🌲",
            title = "Ginkgo Biloba: 270 Million Years Old",
            fact = "Ginkgo biloba is a true living fossil that survived the dinosaur extinction virtually unchanged, with single trees living over 1,500 years in nature.",
            practicalTip = "Ginkgo bonsai make hardy, drought-tolerant balcony trees with stunning gold autumn leaves."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🦕",
            title = "Horsetail: Prehistoric Giants",
            fact = "Equisetum (Horsetail) covered ancient prehistoric swamps as 100-foot-tall trees during the Carboniferous era, and still grows today with high silica stems.",
            practicalTip = "Potted horsetail reeds add architectural vertical lines to modern minimalist apartment decor."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🌴",
            title = "Cycads Survived the Dinosaurs",
            fact = "Cycads like the Sago Palm (Cycas revoluta) dominated prehistoric landscapes 280 million years ago, pre-dating modern flowering plants and conifers.",
            practicalTip = "Sago Palms grow slowly; protect their crown from standing water to prevent rot."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🪵",
            title = "Wollemi Pine Discovery",
            fact = "Thought extinct for 2 million years, living Wollemi Pines were discovered in a remote Australian gorge in 1994, with wild specimens closely guarded.",
            practicalTip = "Keep rare prehistoric conifers in cool, airy, well-lit spaces away from dry furnace heat."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🌿",
            title = "Clubmoss Fossil Coal Origins",
            fact = "Modern diminutive clubmosses are descendants of colossal 130-foot Lycophyte trees whose ancient fossilized forests formed Earth’s major coal deposits.",
            practicalTip = "Clubmosses love terrariums with high moisture, acidic soil, and indirect light."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "💧",
            title = "Mosses: The First Land Colonizers",
            fact = "Bryophytes (mosses) were among the first plants to move from oceans to land over 450 million years ago, lacking vascular veins and absorbing water directly.",
            practicalTip = "Mist live terrarium moss with distilled water or rainwater, as tap minerals cause browning."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🌱",
            title = "Ferns Predate Flowers by 200M Years",
            fact = "Ferns populated the planet roughly 360 million years ago, long before bees, butterflies, or flowering plants evolved.",
            practicalTip = "Ferns crave consistency: avoid drying their root ball or placing them next to drafts."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🌲",
            title = "Dawn Redwood: The Lazarus Tree",
            fact = "Metasequoia (Dawn Redwood) was known only through Mesozoic fossils until a tiny living grove was rediscovered in central China in 1944.",
            practicalTip = "Dawn Redwoods make rewarding deciduous bonsai that love moist, acidic soil and full sun."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🏜️",
            title = "Welwitschia: The Two-Leaf Wonder",
            fact = "Welwitschia mirabilis in the Namib Desert produces only two true leaves that grow continuously throughout its entire 2,000-year lifespan.",
            practicalTip = "Water rare desert specimens strictly from below in extra-deep containers to accommodate taproots."
        ),
        PlantTriviaItem(
            category = "Living Fossils",
            icon = "🪵",
            title = "Tree Ferns of Ancient Forests",
            fact = "Dicksonia antarctica tree ferns have fibrous trunks composed of aerial roots surrounding a thin stem, retaining the exact anatomy of Jurassic forests.",
            practicalTip = "Water tree ferns by pouring water directly into the fibrous crown and down the trunk."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🏢",
            title = "The 45-Degree Light Dropoff",
            fact = "Indoor light intensity drops by over 50% for every 2 feet you move away from a window due to the inverse-square law of light physics.",
            practicalTip = "Place sun-loving plants directly on the windowsill, saving interior tables for low-light tolerant varieties."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "💨",
            title = "AC Drafts vs Tropical Foliage",
            fact = "Cold blasts from air conditioners shock tropical houseplants, causing sudden leaf drop, blackened edges, and stalled growth.",
            practicalTip = "Keep houseplants at least 6 feet away from direct air conditioning vents and heat registers."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🪟",
            title = "Window Glass Filters UV Light",
            fact = "Modern double-pane home window glass blocks up to 95% of ultraviolet radiation, allowing plants to tolerate bright window light without sunburn.",
            practicalTip = "Acclimate indoor houseplants gradually to direct outdoor sun to prevent intense UV scorching."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🌆",
            title = "Balcony Wind Desiccation",
            fact = "High-rise apartment balconies experience wind speeds 3 times stronger than ground level, rapidly drying plant foliage and stripping soil moisture.",
            practicalTip = "Cluster balcony pots together and use heavier ceramic pots that won’t tip over in gusts."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🧼",
            title = "Dust Suffocates City Houseplants",
            fact = "Urban apartment dust clogs leaf stomata and reduces photosynthesis by up to 35%, making plants look dull and slow-growing.",
            practicalTip = "Give large-leaved plants like Fiddle Leaf Figs a gentle lukewarm shower every couple of months."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🏺",
            title = "Bottom Watering Prevents Fungus Gnats",
            fact = "Fungus gnats lay eggs only in the top inch of damp soil. Bottom watering keeps the top layer dry while saturating roots below.",
            practicalTip = "Place pots in a tray of water for 30 minutes, then drain away all excess standing water."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🪴",
            title = "Plastic vs Terracotta in Apartments",
            fact = "Plastic pots retain moisture longer in dry, heated winter apartments, while terracotta allows faster drying in humid monsoon seasons.",
            practicalTip = "Choose pot material to match your home humidity: plastic for dry rooms, terracotta for damp spaces."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🌿",
            title = "Ficus Lyrata Dislikes Relocation",
            fact = "Fiddle Leaf Figs (Ficus lyrata) adapt their leaf orientation to their light source. Moving or rotating them frequently causes leaf drop.",
            practicalTip = "Pick a bright, draft-free spot for your Fiddle Leaf Fig and leave it there permanently."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "💧",
            title = "City Tap Water Chlorine Off-Gassing",
            fact = "Letting municipal tap water sit in an open pitcher for 24 hours dissipates volatile chlorine, making it gentler on sensitive roots.",
            practicalTip = "For chloramines and fluoride, use a basic carbon filter or collect natural rainwater."
        ),
        PlantTriviaItem(
            category = "City Homes",
            icon = "🌡️",
            title = "Thermal Mass of Large Planters",
            fact = "Large planters hold a greater volume of soil, which buffers roots against rapid indoor temperature swings between night and day.",
            practicalTip = "Group smaller plants into a single large planter to stabilize root temperatures."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🪤",
            title = "Venus Flytrap Electrical Counting",
            fact = "A Venus Flytrap leaf only snaps shut if a trigger hair is stimulated twice within 20 seconds, and only releases enzymes after 5 triggers to verify live prey.",
            practicalTip = "Never poke flytrap traps with fingers — closing empty traps drains vital energy reserves."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🏺",
            title = "Pitcher Plant Digestive Acid",
            fact = "Nepenthes tropical pitcher plants produce slippery peristome rims and fluid with digestive enzymes matching human stomach acid (pH 2.0).",
            practicalTip = "Never pour tap water or fertilizer into carnivorous pitchers; keep humidity high so they produce fluid."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "✨",
            title = "Sundew Glistening Mucilage Traps",
            fact = "Drosera (Sundews) sparkle with sticky droplets of digestive mucilage on red tentacles that ensnare mosquitoes and curl around prey.",
            practicalTip = "Sundews are nature’s best organic defense against fruit flies and fungus gnats in apartments."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🎈",
            title = "Bladderwort Vacuum Traps",
            fact = "Utricularia (Bladderwort) traps underwater micro-organisms in 1/1000th of a second using the fastest active mechanical suction trap in the plant kingdom.",
            practicalTip = "Grow aquatic bladderworts in shallow trays of pure distilled water and peat moss."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "💀",
            title = "Pitcher Plant Living Commensals",
            fact = "Certain crab spiders and mosquito larvae live unharmed inside Nepenthes digestive fluid, eating caught insects and helping the plant break down food.",
            practicalTip = "Carnivorous plants must be potted strictly in nutrient-poor peat and perlite with zero fertilizer."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🚫",
            title = "Zero Fertilizer Rule for Carnivores",
            fact = "Carnivorous plants evolved traps specifically because they grow in nutrient-starved bogs; feeding synthetic fertilizer to their roots burns them fatally.",
            practicalTip = "Water all carnivorous plants exclusively with pure rainwater, distilled water, or reverse osmosis water."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🌸",
            title = "Tall Flowers Protect Pollinators",
            fact = "Venus Flytraps and Pitcher Plants grow their flowers on extraordinarily tall stalks far above their traps so they do not accidentally eat their pollinators.",
            practicalTip = "Carnivorous flowers can be left to bloom if the plant is receiving strong direct sunlight."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🦇",
            title = "Pitcher Plant Bat Refuges",
            fact = "The Bornean pitcher plant Nepenthes hemsleyana provides a roosting cavity for Hardwicke’s woolly bats, trading shelter for nitrogen-rich bat guano.",
            practicalTip = "Provide warm, bright indirect light and high humidity to encourage large pitchers."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "🌿",
            title = "Butterwort Sticky Leaves (Pinguicula)",
            fact = "Pinguicula features soft succulent leaves covered in greasy glandular hairs that trap gnats like living flypaper, producing bright purple orchid-like flowers.",
            practicalTip = "Pinguiculas thrive on bright windowsills and eradicate fungus gnats naturally."
        ),
        PlantTriviaItem(
            category = "Carnivorous",
            icon = "💧",
            title = "The Water Tray Method",
            fact = "Unlike most houseplants that rot in standing water, bog plants like Venus Flytraps and Sundews must sit in a shallow dish of water permanently.",
            practicalTip = "Keep 0.5 to 1 inch of distilled water in your carnivorous plant saucer at all times."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🌱",
            title = "Water Propagation Root Types",
            fact = "Roots grown in water have a different cellular structure (aerenchyma) optimized for water, requiring gentle acclimatization when moved into potting mix.",
            practicalTip = "Keep potting soil consistently moist for the first 2 weeks after potting water-rooted cuttings."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🪴",
            title = "The Magic Plant Node",
            fact = "Roots and new stems emerge exclusively from nodes (the swollen junctions where leaves attach to stems). Stem cuttings without a node cannot produce new plants.",
            practicalTip = "Always ensure your cutting includes at least one healthy node submerged in water or moss."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🌾",
            title = "Sphagnum Moss Rooting Medium",
            fact = "Live New Zealand sphagnum moss possesses natural antiseptic properties and an ideal air-to-water ratio that encourages rapid root development with minimal rot.",
            practicalTip = "Use damp sphagnum moss inside a humidity dome for stubborn or rare aroid cuttings."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🥔",
            title = "Air Layering for Giant Stems",
            fact = "Air layering allows you to root a tall leggy Rubber Tree or Monstera stem while it is still attached to the mother plant, guaranteeing 100% propagation success.",
            practicalTip = "Wrap a notched stem with moist sphagnum moss and plastic wrap until roots fill the pouch."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "💨",
            title = "Cinnamon: Natural Antifungal Powder",
            fact = "Household ground cinnamon contains cinnamaldehyde, a natural fungicide that prevents bacterial rot on freshly pruned stems and rooting cuttings.",
            practicalTip = "Dust freshly cut plant stems with ground cinnamon to seal wounds against fungal spores."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🧪",
            title = "Willow Water Rooting Hormone",
            fact = "Willow branches contain high concentrations of indolebutyric acid (IBA) and salicylic acid; soaking willow twigs creates a natural, potent rooting hormone.",
            practicalTip = "Soak chopped willow twigs in boiling water overnight, cool, and use as rooting liquid for cuttings."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🌰",
            title = "Avocado Pit Water Suspension",
            fact = "Suspending an avocado pit with toothpicks over water allows the seed to split its cotyledons and send a taproot down while keeping the top dry.",
            practicalTip = "Pinch off the top leaves of your avocado seedling once it reaches 6 inches to promote bushy branching."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🎁",
            title = "Pilea: The Pass-It-On Plant",
            fact = "The Chinese Money Plant (Pilea peperomioides) was not originally sold in commercial shops, but spread worldwide by friends sharing rooted stem pups.",
            practicalTip = "Share baby Pilea pups with friends and family to carry on this wonderful green tradition."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🍂",
            title = "Sansevieria Leaf Cutting Mutations",
            fact = "Propagating variegated Snake Plants from leaf cuttings results in solid green plants because the yellow margin mutation exists only in crown meristem tissue.",
            practicalTip = "To keep the yellow margins on Snake Plants, propagate by dividing underground rhizomes."
        ),
        PlantTriviaItem(
            category = "Propagation",
            icon = "🌴",
            title = "Coconut: The Floating Ocean Seed",
            fact = "A coconut is a giant waterproof seed whose fibrous husk (coir) provides buoyancy to drift across oceans for 100 days before germinating on distant beaches.",
            practicalTip = "Coconut coir is an eco-friendly, sustainable substitute for peat moss in potting mixes."
        ),
    )
}
