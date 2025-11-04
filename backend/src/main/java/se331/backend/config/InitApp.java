package se331.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se331.backend.dao.NewsDao;
import se331.backend.entity.Comment;
import se331.backend.entity.News;
import se331.backend.entity.Vote;
import se331.backend.security.user.Role;
import se331.backend.security.user.User;
import se331.backend.security.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class InitApp implements CommandLineRunner {

    @Autowired
    private NewsDao newsDao;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        initDefaultUsers();

        // สร้าง News
        if (newsDao.findAll().isEmpty()) {
            System.out.println("No news found. Initializing sample data...");
            initSampleData();
        } else {
            System.out.println("Database already populated (News).");
        }
    }

    private void initDefaultUsers() {
        System.out.println("Initializing default users...");
        seedUserIfMissing(
                "admin",
                "admin@email.com",
                "Admin",
                "User",
                "admin",
                "https://i.pinimg.com/1200x/d2/05/1e/d2051eb63b2bf4874459b1f6bf0f971b.jpg",
                List.of(Role.ROLE_ADMIN) // Admin มีเพียง ROLE_ADMIN
        );
        seedUserIfMissing(
                "member",
                "member@email.com",
                "Member",
                "User",
                "member",
                "https://i.pinimg.com/1200x/68/aa/2e/68aa2e4ff3a5e2151cf7403c6a245589.jpg",
                List.of(Role.ROLE_MEMBER)
        );
        seedUserIfMissing(
                "reader",
                "reader@email.com",
                "Reader",
                "User",
                "reader",
                "https://i.pinimg.com/1200x/62/4e/15/624e15247e6dfdf2cdddd6093e6ee96e.jpg",
                List.of(Role.ROLE_READER) // Reader มี Role เดียว
        );
        System.out.println("Default users initialization check complete.");
    }

    private void seedUserIfMissing(
            String username,
            String email,
            String firstname,
            String lastname,
            String rawPassword,
            String profileImage,
            List<Role> roles // รับเป็น List
    ) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            System.out.println("User '" + username + "' already exists.");
            return;
        }

        // ถ้ายังไม่มี ให้สร้างใหม่
        User user = User.builder()
                .username(username)
                .email(email)
                .firstname(firstname)
                .lastname(lastname)
                .password(passwordEncoder.encode(rawPassword))
                .profileImage(profileImage)
                .roles(roles)
                .enabled(true)
                .build();

        userRepository.save(user);
        System.out.println("Created user: " + username + " with roles: " + roles);
    }

    private void initSampleData() {
        // --- News 1: (Fake News) Telepathy ---
        News news1 = News.builder()
                .topic("First Human-Brain-to-Computer Interface Allows Telepathy")
                .shortDetail("A new brain-computer interface (BCI) has enabled the first successful two-way telepathic communication between two human subjects, allowing them to transmit thoughts and feelings directly to each other's minds.")
                .fullDetail("In a development that blurs the line between science fiction and reality, a team of neuroscientists from Neuralink has unveiled a groundbreaking brain-computer interface (BCI) that facilitates direct, two-way thought communication between two human subjects. The device, an evolution of their existing implant technology, translates neural signals into a digital format, which is then transmitted wirelessly and decoded by a second implant. In a recent trial, two volunteers, a pianist and a painter, were able to 'share' the complex thoughts and emotions associated with their art, creating a 'mental canvas' of collaboration. Dr. Anya Sharma, the lead researcher, stated, 'This is a monumental step forward for communication, empathy, and creativity. We're on the verge of a new form of human connection.' The technology is still in its early stages, with a limited range and some initial cognitive side effects, but the potential applications are staggering, from assisting individuals with communication disabilities to a new era of human-to-human interaction. Ethical concerns, however, have been raised about privacy, mental security, and the potential for misuse of such a powerful tool.")
                .image("https://i.extremetech.com/imagery/content-types/01tn1UGdz4J59gFIJ2nHQsk/images-5.png")
                .reporter("Amara Lee")
                .dateTime(Instant.parse("2025-02-20T09:00:00Z"))
                .realVotes(981)  // 984 (total) - 3 (from comments)
                .fakeVotes(125) // 126 (total) - 1 (from comment)
                .build();

        Comment c1_1 = Comment.builder().username("Ben Carter").text("This is amazing! Telepathy is finally a reality.").image(null).time(Instant.parse("2025-02-20T10:30:00Z")).vote(Vote.REAL).news(news1).build();
        Comment c1_2 = Comment.builder().username("Elara Vance").text("Sounds like something straight out of a sci-fi movie. I'm skeptical.").image("https://cdn3.emoji.gg/emojis/69193-hmm.png").time(Instant.parse("2025-02-20T11:00:00Z")).vote(Vote.FAKE).news(news1).build();
        Comment c1_3 = Comment.builder().username("David Chen").text("Neuralink has been working on this for years. It's a plausible next step.").image("https://i.extremetech.com/imagery/content-types/03mCQ5vLeiY0bwlpGIEsE2N/hero-image.fit_lim.v1678673437.jpg").time(Instant.parse("2025-02-20T11:45:00Z")).vote(Vote.REAL).news(news1).build();
        Comment c1_4 = Comment.builder().username("Olivia Rodriguez").text("The details are too specific to be fake. I'm with the real voters on this one.").image(null).time(Instant.parse("2025-02-20T12:00:00Z")).vote(Vote.REAL).news(news1).build();

        news1.getComments().add(c1_1);
        news1.getComments().add(c1_2);
        news1.getComments().add(c1_3);
        news1.getComments().add(c1_4);
        newsDao.save(news1);

        // --- News 2: (Fake News) Crypto Surge ---
        News news2 = News.builder()
                .topic("Global Crypto Market Surges as Major Countries Approve Bitcoin ETFs")
                .shortDetail("Massive growth hits the crypto sector after the US, EU, and Japan simultaneously approve spot Bitcoin ETFs, triggering a wave of investments from institutional funds worldwide.")
                .fullDetail("In what industry experts are calling a historic moment for digital assets, financial regulators in the United States, European Union, and Japan all approved the launch of spot Bitcoin Exchange Traded Funds (ETFs) on the same day. This regulatory green light has paved the way for trillions of dollars in institutional investment, driving Bitcoin's price over $120,000 for the first time. Investors have flocked to the new instruments, citing ease of access and regulatory clarity as key factors behind the surge. While crypto advocates celebrate the mainstreaming of digital assets, some critics warn of heightened volatility and systemic risks to the global financial system. Janet Lee, an analyst at FinGlobal, noted, 'Wall Street is now fully in the game. This changes everything for Bitcoin and other top cryptocurrencies.'")
                .image("https://i0.wp.com/images.media-outreach.com/655104/image_1.jpeg?ssl=1")
                .reporter("Julian Frost")
                .dateTime(Instant.parse("2024-04-09T14:30:00Z"))
                .realVotes(209) // 211 (total) - 2 (from comments)
                .fakeVotes(600) // 602 (total) - 2 (from comments)
                .build();

        Comment c2_1 = Comment.builder().username("Liam Kennett").text("This is the moment we've been waiting for. Crypto to the moon!").image(null).time(Instant.parse("2024-04-10T15:00:00Z")).vote(Vote.REAL).news(news2).build();
        Comment c2_2 = Comment.builder().username("Muhammad Faisal").text("It's a huge step but also a huge risk. Regulation is a double-edged sword.").image(null).time(Instant.parse("2024-04-11T15:30:00Z")).vote(Vote.REAL).news(news2).build();
        Comment c2_3 = Comment.builder().username("Ryan Wilson").text("I doubt Bitcoin will ever be truly stable, no matter how many ETFs you launch.").image("https://i.pinimg.com/736x/41/59/3f/41593fd8337223718a974773966817f4.jpg").time(Instant.parse("2024-04-12T16:00:00Z")).vote(Vote.FAKE).news(news2).build();
        Comment c2_4 = Comment.builder().username("George Adebayo").text("Just wait for the next crash. They never learn.").image(null).time(Instant.parse("2024-05-02T17:00:00Z")).vote(Vote.FAKE).news(news2).build();

        news2.getComments().add(c2_1);
        news2.getComments().add(c2_2);
        news2.getComments().add(c2_3);
        news2.getComments().add(c2_4);
        newsDao.save(news2);

        // --- News 3: (Equal News) Pyramid Aura ---
        News news3 = News.builder()
                .topic("Mysterious 'Aura' Appears Around the Great Pyramids of Giza")
                .shortDetail("A brilliant, golden-hued 'aura' has been observed surrounding the Great Pyramids, a phenomenon that has no scientific explanation and is drawing global attention from scientists and spiritualists alike.")
                .fullDetail("In a spectacle that has captivated the world, a faint but distinct golden aura has materialized around the Great Pyramids of Giza. The phenomenon, which is visible to the naked eye and has been captured by countless cameras, has no scientific explanation. The 'aura' does not appear to be a reflection of light, a meteorological event, or a known atmospheric effect. Scientists from the Egyptian Ministry of Antiquities and a team of international physicists are on site to study the anomaly, with some speculating that it could be a previously unknown form of electromagnetic energy. Spiritualists and alternative historians, however, are claiming that the aura is a sign of ancient technology reawakening or a cosmic alignment. The event has led to a surge in tourism to the site, as well as a flurry of debate about the true purpose and origin of the pyramids. The mystery deepens as the aura has shown no signs of dissipating since its first appearance.")
                .image("https://c02.purpledshub.com/uploads/sites/41/2024/12/giza-egyptian-pyramids.jpg")
                .reporter("Robert Clark")
                .dateTime(Instant.parse("2023-06-09T11:00:00Z"))
                .realVotes(347) // 350 (total) - 3 (from comments)
                .fakeVotes(348) // 350 (total) - 2 (from comments)
                .build();

        Comment c3_1 = Comment.builder().username("Ahmed Khalil").text("As someone living in Cairo, I can confirm people are really talking about this everywhere. It feels surreal.").image("https://i.pinimg.com/736x/b1/53/3e/b1533ede63fbf6ceb1a56e3da8ebc801.jpg").time(Instant.parse("2023-06-09T16:30:00Z")).vote(Vote.REAL).news(news3).build();
        Comment c3_2 = Comment.builder().username("Maya Johnson").text("This could just be a clever publicity stunt to boost tourism. I’m not buying it yet.").image(null).time(Instant.parse("2023-07-09T17:10:00Z")).vote(Vote.FAKE).news(news3).build();
        Comment c3_3 = Comment.builder().username("Kenji Nakamura").text("If it turns out to be some kind of electromagnetic field, it might rewrite history books.").image(null).time(Instant.parse("2023-08-09T18:05:00Z")).vote(Vote.REAL).news(news3).build();
        Comment c3_4 = Comment.builder().username("Elena Rossi").text("I saw videos of this on social media—still not sure if they’re edited or real, but it gave me chills.").image(null).time(Instant.parse("2024-09-09T19:20:00Z")).vote(Vote.FAKE).news(news3).build();
        Comment c3_5 = Comment.builder().username("Carlos Mendes").text("Maybe the pyramids are finally revealing secrets that humanity was never meant to ignore.").image("https://i.pinimg.com/736x/18/e3/ad/18e3ad7a432d41a6e2a57d1523e81c73.jpg").time(Instant.parse("2024-10-09T20:45:00Z")).vote(Vote.REAL).news(news3).build();

        news3.getComments().add(c3_1);
        news3.getComments().add(c3_2);
        news3.getComments().add(c3_3);
        news3.getComments().add(c3_4);
        news3.getComments().add(c3_5);
        newsDao.save(news3);

        // --- News 4: (Real News) Germany Coalition ---
        News news4 = News.builder()
                .topic("Historic Coalition Government Forms in Germany Amid Political Tension")
                .shortDetail("After months of a political deadlock, Germany's three largest parties have agreed to form a new coalition government, ending weeks of uncertainty and market volatility.")
                .fullDetail("On July 21, 2025, Germany’s Social Democratic Party (SPD), Christian Democratic Union (CDU), and the Greens announced a landmark agreement to form a tri-party coalition government. This comes after prolonged negotiations following the inconclusive federal election in April, where no party secured a parliamentary majority. The coalition marks the first time in German history that the country will be led by a chancellor backed by both the SPD and CDU, with Green Party leaders promising aggressive climate targets and economic reforms. However, the new alliance faces significant challenges: protests have erupted over social policy compromises, and there is skepticism over whether the coalition can manage Germany’s foreign relations amid ongoing tensions with Russia and debates over the European Union’s economic direction. Chancellor-designate Lara Stein stated, 'This coalition represents a new era for Germany. Our united focus is stability, ambition, and a sustainable future for all.' Global markets responded positively, but some political analysts warn that internal disagreements could threaten the coalition's longevity.")
                .image("https://www.reuters.com/resizer/v2/SHXKSOBZNNAWXIXNJE64LG746Y.JPG?auth=a455eb4cbe4fcdece1e217865c41af307a7a5bbd465042852e2e2fbe635ddc42&width=960&quality=80")
                .reporter("Clara Schultz")
                .dateTime(Instant.parse("2025-07-21T09:00:00Z"))
                .realVotes(386) // 388 (total) - 2 (from comments)
                .fakeVotes(105) // 106 (total) - 1 (from comment)
                .build();

        Comment c4_1 = Comment.builder().username("Andreas Müller").text("A coalition like this is unprecedented in Germany. I hope it brings needed stability.").image(null).time(Instant.parse("2025-07-21T11:13:00Z")).vote(Vote.REAL).news(news4).build();
        Comment c4_2 = Comment.builder().username("Greta Davids").text("Lots of promises—but will anything really change for ordinary people?").image("https://i.redd.it/dn2o2gq2f6sc1.jpeg").time(Instant.parse("2025-07-22T08:34:00Z")).vote(Vote.FAKE).news(news4).build();
        Comment c4_3 = Comment.builder().username("Felix Schmidt").text("Relieved that the political gridlock is over. Markets will probably recover quickly.").image(null).time(Instant.parse("2025-07-23T15:51:00Z")).vote(Vote.REAL).news(news4).build();

        news4.getComments().add(c4_1);
        news4.getComments().add(c4_2);
        news4.getComments().add(c4_3);
        newsDao.save(news4);

        // --- News 5: (Fake News) Blue Whale ---
        News news5 = News.builder()
                .topic("Rare Blue Whale Spotted in the Thames River, London")
                .shortDetail("A full-sized blue whale has been sighted swimming in the River Thames near Canary Wharf, baffling marine biologists and local residents who have never seen such a large marine mammal this far up the river.")
                .fullDetail("The city of London was in a state of stunned disbelief today as a massive blue whale, the largest animal on Earth, was seen surfacing in the River Thames. The whale, estimated to be over 25 meters long, was first spotted by commuters on a ferry and quickly drew a crowd of onlookers along the riverbank. Authorities have cordoned off the area and a team of marine biologists from the Zoological Society of London is on site to assess the situation. According to Dr. Eleanor Vance, the whale is in good health but seems disoriented. 'We've never seen a blue whale this far into an estuary,' she stated. 'It’s an extremely rare and concerning event.' While the cause of the whale's presence is unknown, some are speculating that it could be a result of the unusual feeding patterns caused by recent climate change. The event has captured global media attention and has sparked a debate about the health of the world's oceans and their impact on marine life. The rescue effort to guide the whale back to the open sea is underway.")
                .image("https://www.thesun.co.uk/wp-content/uploads/2019/10/dk-comp-whale.jpg?strip=all&quality=100&w=1500&h=1000&crop=1")
                .reporter("Linh Tran")
                .dateTime(Instant.parse("2025-08-05T18:00:00Z"))
                .realVotes(27)  // 27 (total) - 0 (from comments)
                .fakeVotes(497) // 500 (total) - 3 (from comments)
                .build();

        Comment c5_1 = Comment.builder().username("Chloe Thompson").text("A blue whale? In the Thames? This is absolutely, 100% fake. The river isn't deep enough.").image(null).time(Instant.parse("2025-08-05T19:00:00Z")).vote(Vote.FAKE).news(news5).build();
        Comment c5_2 = Comment.builder().username("Alessandro Greco").text("I'm a Londoner and I can tell you this is a total fabrication. It would be impossible for such a large animal to navigate the river.").image(null).time(Instant.parse("2025-08-10T20:00:00Z")).vote(Vote.FAKE).news(news5).build();
        Comment c5_3 = Comment.builder().username("Natalia Kowalczyk").text("The Thames is surprisingly deep in some spots, but a blue whale is still a stretch. I'm torn, but leaning fake.").image("https://i.pinimg.com/736x/fe/0f/5a/fe0f5ad9c6653d9c858c23611c61e156.jpg").time(Instant.parse("2025-08-15T21:00:00Z")).vote(Vote.FAKE).news(news5).build();

        news5.getComments().add(c5_1);
        news5.getComments().add(c5_2);
        news5.getComments().add(c5_3);
        newsDao.save(news5);

        // --- News 6: (Real News) Hatsune Miku --- (ID 12 from JSON)
        News news6 = News.builder()
                .topic("Hatsune Miku World Tour 2024 Thrills Fans Across Continents")
                .shortDetail("Virtual superstar Hatsune Miku headlines a sold-out world tour, featuring live performances in the US, Europe, and Asia.")
                .fullDetail("Hatsune Miku, the iconic virtual singer, is once again captivating audiences worldwide with her highly anticipated 'Hatsune Miku World Tour 2024.' The tour has drawn massive crowds in cities like Los Angeles, London, Paris, and Tokyo. Combining cutting-edge hologram technology with a live band, Miku performs her most popular hits and new songs, creating a surreal concert experience. Fans from diverse backgrounds gather to celebrate the vocaloid phenomenon, waving glowsticks and singing along. Organizers report sold-out venues and record-breaking ticket sales. The tour not only highlights Miku's unending popularity but also demonstrates the growing cultural impact of virtual performers in the global music industry.")
                .image("https://arc-anglerfish-washpost-prod-washpost.s3.amazonaws.com/public/A4QFGWUAOUI6RJR7PNOSVOT2YU.jpg")
                .reporter("Keita Nakamura")
                .dateTime(Instant.parse("2024-03-21T15:00:00Z"))
                .realVotes(262) // 267 (total) - 5 (from comments)
                .fakeVotes(16)  // 17 (total) - 1 (from comment)
                .build();

        Comment c6_1 = Comment.builder().username("MikuFanatic").text("The best concert of my life! Thank you Miku and the whole crew!").image("https://media.tenor.com/0JieZ0bTRZcAAAAM/hatsune-miku-excited.gif").time(Instant.parse("2024-07-19T23:18:00Z")).vote(Vote.REAL).news(news6).build();
        Comment c6_2 = Comment.builder().username("VocaloidLover").text("Actually saw this live in Paris—unforgettable! Miku’s vocals and hologram are next-level.").image("https://i.pinimg.com/originals/1c/af/0b/1caf0b512af317e438d17a84bea6403d.gif").time(Instant.parse("2024-07-20T09:40:00Z")).vote(Vote.REAL).news(news6).build();
        Comment c6_3 = Comment.builder().username("ClassicRockGuy").text("Amazing technology, but I still prefer real bands. Still, the visuals were impressive.").image(null).time(Instant.parse("2024-07-20T12:27:00Z")).vote(Vote.FAKE).news(news6).build();
        Comment c6_4 = Comment.builder().username("GlowstickQueen").text("I cried during 'Senbonzakura.' Everyone was waving turquoise glowsticks in sync. Goosebumps! 💙").image("https://www.ecns.cn/hd/2019/07/25/80dafd28d107439d891406235dfdbf98.jpg").time(Instant.parse("2024-07-20T13:42:00Z")).vote(Vote.REAL).news(news6).build();
        Comment c6_5 = Comment.builder().username("AnimeUnity").text("Never thought I’d see thousands of people singing along to a virtual idol. Only Miku can do it!").image(null).time(Instant.parse("2024-07-20T14:12:00Z")).vote(Vote.REAL).news(news6).build();
        Comment c6_6 = Comment.builder().username("CosplayAddict").text("So many great cosplays! Went as Luka but Miku stole the show as always.").image("https://cdn.bsky.app/img/feed_thumbnail/plain/did:plc:lepzk6xbz5poyv3wvdedwdeh/bafkreifixm6o67f4pn3aw7rd6b6ofwuzslsgv5w7wpge4rhqztoitsr6ju@jpeg").time(Instant.parse("2024-07-20T16:59:00Z")).vote(Vote.REAL).news(news6).build();

        news6.getComments().add(c6_1);
        news6.getComments().add(c6_2);
        news6.getComments().add(c6_3);
        news6.getComments().add(c6_4);
        news6.getComments().add(c6_5);
        news6.getComments().add(c6_6);
        newsDao.save(news6);

        // --- News 7: (Real News) Zealandia --- (ID 6 from JSON)
        News news7 = News.builder()
                .topic("Scientists Discover an Entirely New Continent in the Pacific Ocean")
                .shortDetail("A massive, previously unknown landmass has been discovered in the South Pacific Ocean, leading geologists to declare it the world's eighth continent, 'Zealandia.'")
                .fullDetail("Geologists have made a staggering discovery that will rewrite global maps: a new continent has been found submerged beneath the South Pacific Ocean. Known as 'Zealandia,' the landmass spans an area of nearly 5 million square kilometers, about half the size of Australia. For centuries, it was considered a collection of islands, including New Zealand and New Caledonia, but a recent survey using advanced seafloor mapping technology revealed a single, contiguous continental crust. Dr. David Evans, a geophysicist from the expedition, stated, 'The scientific evidence is irrefutable. We have found the world's eighth continent, hidden in plain sight.' The discovery has major implications for a wide range of scientific fields, from plate tectonics to marine biology. While the majority of the continent is submerged, its discovery provides a new frontier for scientific exploration and a deeper understanding of Earth's geological history.")
                .image("https://i.ytimg.com/vi/bMncyN_C-pQ/maxresdefault.jpg")
                .reporter("James Miller")
                .dateTime(Instant.parse("2025-07-22T14:00:00Z"))
                .realVotes(267) // 270 (total) - 3 (from comments)
                .fakeVotes(118) // 120 (total) - 2 (from comments)
                .build();

        Comment c7_1 = Comment.builder().username("Chen Wei").text("This has been a known theory for years, and now it's been confirmed. This is real.").image(null).time(Instant.parse("2025-07-23T15:00:00Z")).vote(Vote.REAL).news(news7).build();
        Comment c7_2 = Comment.builder().username("Jean Baptiste Lefevre").text("@David Chen bro they just unlocked DLC content for Earth 🌍🤣").image("https://i.redd.it/7b7kib2wc8kf1.gif").time(Instant.parse("2025-07-24T16:30:03Z")).vote(Vote.REAL).news(news7).build();
        Comment c7_3 = Comment.builder().username("Priya Singh").text("A new continent? How could we have missed something so big? Sounds like a conspiracy theory.").image("https://i.pinimg.com/736x/7e/27/01/7e2701369dd69134dd16387eb1b4f1a2.jpg").time(Instant.parse("2025-07-25T17:00:00Z")).vote(Vote.FAKE).news(news7).build();
        Comment c7_4 = Comment.builder().username("Mark Stevens").text("@Elara Vance 😂 imagine waking up and realizing there’s been a whole continent hiding underwater like ‘surprise, I live here too!’").image("https://i.pinimg.com/736x/4a/db/ca/4adbcab4d78e7a4296b85ab2756248ea.jpg").time(Instant.parse("2025-07-25T18:10:00Z")).vote(Vote.FAKE).news(news7).build();
        Comment c7_5 = Comment.builder().username("Tamara Ivanović").text("This is a legitimate scientific discovery. It's been in the news for a few years, but now it's official.").image(null).time(Instant.parse("2025-07-26T23:00:23Z")).vote(Vote.REAL).news(news7).build();

        news7.getComments().add(c7_1);
        news7.getComments().add(c7_2);
        news7.getComments().add(c7_3);
        news7.getComments().add(c7_4);
        news7.getComments().add(c7_5);
        newsDao.save(news7);

        // --- News 8: (Equal News) Woolly Mammoth --- (ID 8 from JSON)
        News news8 = News.builder()
                .topic("Scientists Successfully Clone a Woolly Mammoth for Reintroduction")
                .shortDetail("A team of geneticists has announced the birth of a healthy woolly mammoth calf, a groundbreaking achievement in de-extinction, using gene-editing technology and an elephant surrogate.")
                .fullDetail("In a historic scientific breakthrough, a research team from Colossal Biosciences has successfully brought a woolly mammoth back to life. The calf, named 'Yuka' after a famous Siberian mammoth fossil, was born via a surrogate elephant mother after a 22-month gestation period. The project involved editing elephant DNA with genes from a preserved woolly mammoth specimen, allowing the embryo to develop with the mammoth's characteristic cold-resistant traits, such as thick hair and a layer of fat. The goal of the project is not just to revive the species but to reintroduce them to the Arctic tundra, where their grazing habits could help restore the ecosystem and combat climate change by preventing permafrost thaw. The announcement has been met with both excitement and controversy. Animal rights activists have condemned the experiment, citing ethical concerns for the surrogate elephant and the potential for unintended ecological consequences. However, the scientific community is hailing the event as a monumental step forward in genetic engineering, with implications for preserving endangered species and even reversing the effects of extinction.")
                .image("https://cdn.hswstatic.com/gif/shutterstock-2480178585.jpg")
                .reporter("Dr. Aris Thorne")
                .dateTime(Instant.parse("2025-01-05T18:00:00Z"))
                .realVotes(148) // 150 (total) - 2 (from comments)
                .fakeVotes(149) // 150 (total) - 1 (from comment)
                .build();

        Comment c8_1 = Comment.builder().username("Bongani Dlamini").text("This is incredible! Bringing back a species to help the planet is a fantastic idea.").image(null).time(Instant.parse("2025-01-05T19:00:00Z")).vote(Vote.REAL).news(news8).build();
        Comment c8_2 = Comment.builder().username("Marcus Evans").text("Clone a mammoth? Sounds like a plot for a new Jurassic Park movie. It's a fake story.").image(null).time(Instant.parse("2025-01-05T20:00:00Z")).vote(Vote.FAKE).news(news8).build();
        Comment c8_3 = Comment.builder().username("Natalia Kowalczyk").text("The science on this is actually progressing. Colossal has been in the news for a while.").image("https://miro.medium.com/v2/resize:fit:1240/1*xMVXjADYsAWRO4eT92BSdw.png").time(Instant.parse("2025-01-05T21:00:00Z")).vote(Vote.REAL).news(news8).build();

        news8.getComments().add(c8_1);
        news8.getComments().add(c8_2);
        news8.getComments().add(c8_3);
        newsDao.save(news8);

        // --- News 9: (Real News) Water Filter --- (ID 9 from JSON)
        News news9 = News.builder()
                .topic("Scientists Create an 'Energy-Positive' Water Filter That Cleans Water While Producing Electricity")
                .shortDetail("A team of MIT researchers has developed a new type of water filtration system that purifies water and simultaneously generates a small, usable amount of electrical power, offering a solution to both clean water and energy poverty.")
                .fullDetail("A revolutionary new technology could tackle two of the world's most pressing problems at once. Researchers at the Massachusetts Institute of Technology (MIT) have unveiled a prototype for a water filter that uses a novel process called 'bioremediation-induced electrogenesis.' The device contains a special membrane with genetically engineered microorganisms that consume waterborne pollutants. As they break down the contaminants, they release electrons, which are captured by a series of tiny electrodes, creating a low but continuous electrical current. A single household-sized unit can purify enough drinking water for a family of four while generating enough power to charge a smartphone or run a small LED lamp. Dr. Anya Sharma, the lead researcher on the project, stated, 'This is a game-changer for developing nations and disaster zones. It provides clean water and a source of power where neither is available.' The team has filed for a patent and is seeking partnerships to scale up production, with a goal of making the devices affordable and accessible worldwide within the next five years.")
                .image("https://assets.newatlas.com/dims4/default/11c1f6f/2147483647/strip/true/crop/1129x753+0+47/resize/1200x800!/quality/90/?url=https%3A%2F%2Fnewatlas-brightspot.s3.amazonaws.com%2F15%2F2a%2Fffd25de243328aec792121190be9%2Fquqkgpovzlbhrvzchpsltemzmk.jpg")
                .reporter("Chiara Romano")
                .dateTime(Instant.parse("2025-06-25T14:00:00Z"))
                .realVotes(178) // 180 (total) - 2 (from comments)
                .fakeVotes(89)  // 90 (total) - 1 (from comment)
                .build();

        Comment c9_1 = Comment.builder().username("Magnus Kristensen").text("Wow, this sounds like a real technological leap forward. I hope this gets implemented quickly.").image(null).time(Instant.parse("2025-06-25T15:00:00Z")).vote(Vote.REAL).news(news9).build();
        Comment c9_2 = Comment.builder().username("Marta Fernandes").text("It seems a bit like a perpetual motion machine. Generating energy from cleaning water? That's not how physics works.").image("https://preview.redd.it/the-definition-of-wet-is-a-problem-v0-hi9sk5tadq1f1.jpeg?width=640&crop=smart&auto=webp&s=87abaaf1e5c5145b929b5dc15bdc21b703f3c199").time(Instant.parse("2025-06-25T16:00:00Z")).vote(Vote.FAKE).news(news9).build();
        Comment c9_3 = Comment.builder().username("Tamara Ivanović").text("It's not perpetual motion. It's using the energy stored in the pollutants. It's plausible!").image(null).time(Instant.parse("2025-06-25T17:00:00Z")).vote(Vote.REAL).news(news9).build();

        news9.getComments().add(c9_1);
        news9.getComments().add(c9_2);
        news9.getComments().add(c9_3);
        newsDao.save(news9);

        // --- News 10: (Equal News) Alien Signal --- (ID 10 from JSON)
        News news10 = News.builder()
                .topic("Alien Signal Confirmed as 'First Contact' from Kepler-186f")
                .shortDetail("The SETI Institute has officially confirmed that a repeating radio signal from a distant exoplanet, Kepler-186f, is an intelligently designed message, marking humanity's first confirmed contact with extraterrestrial life.")
                .fullDetail("The silence of the cosmos was broken today with a historic announcement from the SETI Institute. After decades of listening, they have verified that a recurring radio signal originating from the exoplanet Kepler-186f, located 500 light-years away, is an artificial, intelligently designed message. The signal, which repeats a complex mathematical sequence and a simple, universal prime number sequence, was first detected in 2024 but was only recently confirmed after rigorous peer review. Dr. Eleanor Vance, a lead astrophysicist on the project, stated, 'This is the most profound discovery in human history. We are not alone.' The global community is now grappling with the implications of this 'First Contact' event, with governments, scientists, and religious leaders forming a coalition to determine the appropriate response. The message does not appear to be a threat, but a simple 'hello,' inviting humanity to engage. The revelation has sparked a global conversation about our place in the universe and the potential for a new era of interstellar cooperation.")
                .image("https://www.nasa.gov/wp-content/uploads/2023/03/kepler186f_artistconcept_2.jpg?resize=1024,575")
                .reporter("Robert Clark")
                .dateTime(Instant.parse("2024-11-29T16:00:00Z"))
                .realVotes(108) // 110 (total) - 2 (from comments)
                .fakeVotes(108) // 110 (total) - 2 (from comments)
                .build();

        Comment c10_1 = Comment.builder().username("Aisha Karim").text("A signal from another planet? This is a huge story that would be everywhere. It's a total fake.").image(null).time(Instant.parse("2024-11-29T17:00:00Z")).vote(Vote.FAKE).news(news10).build();
        Comment c10_2 = Comment.builder().username("Samuel Osei").text("The details are intriguing. The SETI project is real, and they've been searching for this for a long time.").image(null).time(Instant.parse("2024-11-29T17:30:00Z")).vote(Vote.REAL).news(news10).build();
        Comment c10_3 = Comment.builder().username("Emily Davis").text("If this was real, the entire world would be in a panic. I've seen nothing about this on any major news channel. It's not real.").image("https://i.ytimg.com/vi/DBhs676nka4/maxresdefault.jpg").time(Instant.parse("2024-11-29T18:00:00Z")).vote(Vote.FAKE).news(news10).build();
        Comment c10_4 = Comment.builder().username("Jasper Tan").text("I hope it's real! This would change everything.").image(null).time(Instant.parse("2024-11-29T19:00:00Z")).vote(Vote.REAL).news(news10).build();

        news10.getComments().add(c10_1);
        news10.getComments().add(c10_2);
        news10.getComments().add(c10_3);
        news10.getComments().add(c10_4);
        newsDao.save(news10);

        // --- News 11: (Fake News) The Great Silence ---
        News news11 = News.builder()
                .topic("The 'Great Silence': All Birds on Earth Stop Chirping Simultaneously for 24 Hours")
                .shortDetail("A bizarre and unsettling global event has occurred, where all bird species on the planet ceased all vocalizations for exactly 24 hours, baffling scientists and the public alike.")
                .fullDetail("A strange and unsettling phenomenon has captured the world's attention. Beginning at 12:00 UTC on [2 days ago], every known bird species on Earth, from sparrows to eagles, ceased all forms of vocalization. The silence, which lasted for precisely 24 hours before birds began to sing again at the exact same time, was a chilling experience for billions. Ornithologists, zoologists, and physicists are scrambling for an explanation. Theories range from a previously unknown atmospheric phenomenon to a coordinated biological response, but no concrete evidence has been found. The synchronized nature of the event is what is most puzzling to the scientific community, as it defies all known biological and physical laws. Governments are urging people to remain calm, but the 'Great Silence' has left many questioning the stability of the natural world.")
                .image("https://electricliterature.com/wp-content/uploads/2016/10/ganapathy-kumar-93498-unsplash.jpg")
                .reporter("Amara Lee")
                .dateTime(Instant.parse("2025-04-23T11:00:00Z"))
                .realVotes(5)   // 5 (total) - 0 (from comments)
                .fakeVotes(192) // 195 (total) - 3 (from comments)
                .build();

        Comment c11_1 = Comment.builder().username("Ben Carter").text("I can hear birds right now. This is a total lie.").image(null).time(Instant.parse("2025-04-23T12:00:00Z")).vote(Vote.FAKE).news(news11).build();
        Comment c11_2 = Comment.builder().username("Seoyeon Kim").text("This would be on every news channel, and I haven't heard a single thing. It's a fake story.").image("https://ecdn.teacherspayteachers.com/thumbitem/-The-Great-Silence-by-Ted-Chiang-Comprehension-Questions--7339996-1634283963/original-7339996-1.jpg").time(Instant.parse("2025-04-23T13:00:00Z")).vote(Vote.FAKE).news(news11).build();
        Comment c11_3 = Comment.builder().username("Sarah Smith").text("It’s a powerful metaphor for our environmental crises, but not a real event.").image("https://i.ytimg.com/vi/VPnR97b6aGs/maxresdefault.jpg").time(Instant.parse("2025-04-23T14:00:00Z")).vote(Vote.FAKE).news(news11).build();

        news11.getComments().add(c11_1);
        news11.getComments().add(c11_2);
        news11.getComments().add(c11_3);
        newsDao.save(news11);

        // --- News 12: (Fake News) Cambodia Pink Gas --- (ID 13 from JSON)
        News news12 = News.builder()
                .topic("Cambodia Alleges Thai Military Aircraft Dropped ‘Pink Gas’ in Border Villages, Claims Civilian Casualties")
                .shortDetail("Cambodian officials accuse Thailand of using chemical weapons from military planes over disputed border areas. Dramatic images of a pink chemical cloud, shared widely online, intensify tensions.")
                .fullDetail("The Cambodian government has accused the Royal Thai Air Force of dropping a toxic pink chemical over several villages near the An Ses region, known in Thailand as Chong An Ma, Ubon Ratchathani, and Phnom Kmoach. A viral image circulating on Cambodian and Thai social media platforms shows an unidentified aircraft – bearing a Thai flag – releasing a vivid pink substance. The Cambodian Ministry of Defense, through spokesperson Lt. Gen. Mali Socheata, claimed that the operation led to civilian injuries and possible fatalities. Even the Cambodian Embassy in Bulgaria and Chanmony Pich (wife of PM Hun Manet) have shared the image and condemning statements. At a press conference on July 28, Lt. Gen. Mali Socheata publicly alleged the use of 'poisonous gas' by Thai forces in what authorities described as 'an act of chemical warfare against our people.' However, fact-checking by AFP and multiple independent analysts have debunked these claims. The viral image is in fact a photo taken in Palisades, California, this January, depicting a US firefighting aircraft dropping red fire retardant during a wildfire. The image was originally shot by a Reuters journalist. Colonel Pattana Phanmongkhon from Thailand’s Defense Intelligence Department formally briefed international diplomats on August 1, reiterating that Thailand strictly abides by the Chemical Weapons Convention (CWC), has never used chemical agents in military actions, and condemned the allegations as baseless war propaganda. Thai officials called for calm and vigilance against misinformation designed to inflame tensions.")
                .image("https://lh7-rt.googleusercontent.com/docsz/AD_4nXeuatshS8Whcs5J5_aluivv837pksEQJnJ8FWAQ4f9zfLgKxIXD8Z7F2CKZmPNFPRmmIH3FdzMXK7dt_5JsuNuN02PVhjeBPfokEDFQ9FgBwO5cItTP5oz9cyvxVajNnW_P6FVqUObQ20cCQcw6Ugc?key=bEIM4Zcxw2E2RVLQMFAABw") // Note: Original image URL seems broken, using placeholder
                .reporter("Ahmed Al-Sabah")
                .dateTime(Instant.parse("2025-07-29T10:00:00Z"))
                .realVotes(33)  // 34 (total) - 1 (from comment)
                .fakeVotes(161) // 167 (total) - 6 (from comments)
                .build();

        Comment c12_1 = Comment.builder().username("Jessica Miller").text("I saw this exact photo from a wildfire in California! Totally fake news.").image("https://ichef.bbci.co.uk/ace/ws/800/cpsprodpb/da0d/live/d02c9ba0-711d-11f0-af20-030418be2ca5.jpg.webp").time(Instant.parse("2025-07-29T12:00:00Z")).vote(Vote.FAKE).news(news12).build();
        Comment c12_2 = Comment.builder().username("Alex Chen").text("Groundless claims without evidence. Thailand would never use chemical weapons.").image(null).time(Instant.parse("2025-07-29T12:17:00Z")).vote(Vote.FAKE).news(news12).build();
        Comment c12_3 = Comment.builder().username("Ekaterina Sokolova").text("Fact check: That plane is American, and it's firefighting—not warfare.").image("https://files.wp.thaipbs.or.th/verify/2025/07/messageImage_1753678906579%E0%B9%81%E0%B8%9A%E0%B8%9A%E0%B9%80%E0%B8%97%E0%B8%B5%E0%B8%A2%E0%B8%9A-1024x409.jpg").time(Instant.parse("2025-07-29T14:00:00Z")).vote(Vote.FAKE).news(news12).build();
        Comment c12_4 = Comment.builder().username("Sirilak Srisuk").text("War propaganda at its finest. Don’t fall for obvious fakes.").image("https://media.makeameme.org/created/i-dont-5b7961.jpg").time(Instant.parse("2025-07-29T16:11:00Z")).vote(Vote.FAKE).news(news12).build();
        Comment c12_5 = Comment.builder().username("CambodiaNews").text("Our people deserve answers. Why is there a pink cloud over An Ses? We deserve transparency!").image(null).time(Instant.parse("2025-07-29T17:03:00Z")).vote(Vote.REAL).news(news12).build();
        Comment c12_6 = Comment.builder().username("BorderWatcherTH").text("Thailand has signed the CWC. There’s zero chance of such an attack.").image(null).time(Instant.parse("2025-07-29T17:28:00Z")).vote(Vote.FAKE).news(news12).build();
        Comment c12_7 = Comment.builder().username("Annonymous555").text("Don’t be manipulated by doctored images. Always check the source.").image("https://i.ytimg.com/vi/N4JtDpvx158/maxresdefault.jpg").time(Instant.parse("2025-07-29T18:00:00Z")).vote(Vote.FAKE).news(news12).build();

        news12.getComments().add(c12_1);
        news12.getComments().add(c12_2);
        news12.getComments().add(c12_3);
        news12.getComments().add(c12_4);
        news12.getComments().add(c12_5);
        news12.getComments().add(c12_6);
        news12.getComments().add(c12_7);
        newsDao.save(news12);

        // --- News 13: (Real News) Second Sun --- (ID 14 from JSON)
        News news13 = News.builder()
                .topic("The 'Second Sun' Appears Over China, Baffles Astronomers")
                .shortDetail("A bright, star-like object has appeared in the sky over Beijing, drawing global attention and leaving astronomers perplexed as it remains a constant presence day and night.")
                .fullDetail("Residents of Beijing and surrounding areas woke up to a shocking sight: a second, smaller 'sun' in the sky. The object, which is about one-tenth the size of the moon, is extremely bright and has remained in a stationary position for the past 48 hours. It is visible both during the day and night and does not appear to be a comet, a meteor, or any known celestial body. Astronomers are baffled, with some speculating that it could be a highly reflective, previously unknown planet, while others suggest it could be a secret military satellite. The Chinese government has issued a statement urging calm, but has provided no official explanation. The appearance of the 'second sun' has sparked a flurry of online theories, from alien visitations to an elaborate government hoax. The event is being closely monitored by observatories worldwide as scientists race to understand this unprecedented cosmic event.")
                .image("https://metro.co.uk/wp-content/uploads/2023/07/4003353_second_sun-dc5b.jpg?quality=90&strip=all")
                .reporter("Chen Wei")
                .dateTime(Instant.parse("2022-10-15T12:00:00Z"))
                .realVotes(83) // 85 (total) - 2 (from comments)
                .fakeVotes(48) // 50 (total) - 2 (from comments)
                .build();

        Comment c13_1 = Comment.builder().username("Siti Nurhaliza").text("This is ridiculous. It's probably just a lens flare or a high-altitude balloon.").image(null).time(Instant.parse("2022-10-13T14:00:00Z")).vote(Vote.FAKE).news(news13).build();
        Comment c13_2 = Comment.builder().username("Sarah Jones").text("My friends in Beijing have posted about this. It's real, and it's creepy.").image(null).time(Instant.parse("2022-10-13T14:30:00Z")).vote(Vote.REAL).news(news13).build();
        Comment c13_3 = Comment.builder().username("Ryan Wilson").text("A new celestial body? That would be the biggest story of the decade. I haven't seen anything on CNN or BBC. It's fake.").image("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRk-2Hh84b83f4S3wKrj3IkT3SOuVselu_LMQ&s").time(Instant.parse("2022-10-13T15:00:00Z")).vote(Vote.FAKE).news(news13).build();
        Comment c13_4 = Comment.builder().username("Maya Ben-David").text("I've seen some videos. It looks like a bright star, but it's not on any star chart.").image(null).time(Instant.parse("2022-10-13T16:00:00Z")).vote(Vote.REAL).news(news13).build();

        news13.getComments().add(c13_1);
        news13.getComments().add(c13_2);
        news13.getComments().add(c13_3);
        news13.getComments().add(c13_4);
        newsDao.save(news13);

        // --- News 14: (Fake News) Anti-Gravity --- (ID 7 from JSON)
        News news14 = News.builder()
                .topic("Ancient Tomb Discovered with Advanced 'Anti-Gravity' Technology")
                .shortDetail("Archaeologists have found an ancient tomb in Egypt that contains what appears to be a working 'anti-gravity' device, a find that could rewrite human history and physics.")
                .fullDetail("In a discovery that would shock the world, a team of archaeologists working in the Valley of the Kings has unearthed a tomb belonging to an unknown pharaoh. Inside, among the usual artifacts, they found a small, perfectly preserved metallic device with strange hieroglyphs. When activated with a specific frequency, the device emits a field that repels objects from the ground, effectively nullifying gravity. The device, which is powered by an unknown, self-sustaining energy source, is unlike anything ever seen. A team of physicists has confirmed that the device works, but they are unable to explain how. 'This challenges our most fundamental understanding of physics,' said Dr. Elena Petrova, a leading expert on ancient technology. The discovery has prompted a global conversation about humanity's past and the possibility of lost, advanced civilizations. While some believe it's a revolutionary discovery, others are skeptical, citing the lack of credible evidence and the impossibility of such a device existing thousands of years ago.")
                .image("https://media.cnn.com/api/v1/images/stellar/prod/170718150803-intball-cute-japanese-drone-02.jpg")
                .reporter("Samuel Osei")
                .dateTime(Instant.parse("2022-12-22T10:00:00Z"))
                .realVotes(49)  // 50 (total) - 1 (from comment)
                .fakeVotes(288) // 290 (total) - 2 (from comments)
                .build();

        Comment c14_1 = Comment.builder().username("Liam Kennedy").text("Anti-gravity from ancient Egypt? This is too ridiculous to be taken seriously. Total fantasy.").image(null).time(Instant.parse("2022-12-22T12:00:00Z")).vote(Vote.FAKE).news(news14).build();
        Comment c14_2 = Comment.builder().username("Charlotte Andersson").text("It's a huge claim, but I'm intrigued. Maybe there's a kernel of truth to the stories of ancient technology.").image(null).time(Instant.parse("2023-01-23T13:00:00Z")).vote(Vote.REAL).news(news14).build();
        Comment c14_3 = Comment.builder().username("Tatenda Chikafu").text("Where are the photos and videos of this 'device'? No evidence, no reality. It's fake.").image("https://i.pinimg.com/736x/c3/4d/9e/c34d9e20542586ae447c8e526d261076.jpg").time(Instant.parse("2024-02-24T14:00:00Z")).vote(Vote.FAKE).news(news14).build();

        news14.getComments().add(c14_1);
        news14.getComments().add(c14_2);
        news14.getComments().add(c14_3);
        newsDao.save(news14);

        // --- News 15: (Equal News) Rome Plane Crash --- (ID 15 from JSON)
        News news15 = News.builder()
                .topic("Passenger Plane Crashes Near Rome, Miraculously All Survive")
                .shortDetail("A commercial jet with 178 people on board crash lands in a suburban field outside Rome. All passengers and crew are rescued without fatalities, stunning rescuers and aviation experts.")
                .fullDetail("In an astonishing turn of events, a passenger jet carrying 178 people crash landed in a field near Fiumicino, just outside of Rome, early Friday morning. The flight, operated by ItalAirways, reported engine failure shortly after take-off from Leonardo da Vinci International Airport and attempted to return, but was forced to make an emergency landing in an open field. Emergency crews responded swiftly, extinguishing a small blaze and assisting passengers from the wreckage. Astonishingly, there were no fatalities, with only minor injuries reported. Aviation analysts are hailing the pilot's quick thinking and professionalism for preventing a major tragedy. The Italian Civil Aviation Authority (ENAC) has launched an investigation to determine the cause of the engine failure. Survivors expressed deep gratitude to the crew and rescue teams, with many calling their escape 'a miracle.' The incident has sparked renewed discussion on airline safety procedures and crisis response.")
                .image("https://www.aljazeera.com/wp-content/uploads/2024/12/2024-12-29T043142Z_599823962_RC2SYBA8AGCT_RTRMADP_3_SOUTHKOREA-CRASH-1735451419.jpg?fit=1170%2C780&quality=80")
                .reporter("Olivia Turner")
                .dateTime(Instant.parse("2024-06-21T15:00:00Z"))
                .realVotes(176) // 179 (total) - 3 (from comments)
                .fakeVotes(178) // 179 (total) - 1 (from comment)
                .build();

        Comment c15_1 = Comment.builder().username("Simone Conti").text("Can't believe everyone survived! The pilot deserves a medal.").image(null).time(Instant.parse("2024-06-21T16:00:00Z")).vote(Vote.REAL).news(news15).build();
        Comment c15_2 = Comment.builder().username("Amina Cissé").text("Is this real? But isn't it crash in Korea and no one survives?").image(null).time(Instant.parse("2024-06-21T17:00:00Z")).vote(Vote.FAKE).news(news15).build();
        Comment c15_3 = Comment.builder().username("Chloe Thompson").text("Just saw videos from the scene online. The field is full of rescue workers, everyone looks safe! Amazing outcome.").image("https://i.ytimg.com/vi/HBSpAdd2VR0/maxresdefault.jpg").time(Instant.parse("2024-06-21T18:00:00Z")).vote(Vote.REAL).news(news15).build();
        Comment c15_4 = Comment.builder().username("StefanoR").text("@Chloe ThompsonAs, That's not even the same plane!").image(null).time(Instant.parse("2024-06-21T19:00:00Z")).vote(Vote.REAL).news(news15).build();

        news15.getComments().add(c15_1);
        news15.getComments().add(c15_2);
        news15.getComments().add(c15_3);
        news15.getComments().add(c15_4);
        newsDao.save(news15);

        // --- News 16: (Real News) Fusion Energy --- (ID 16 from JSON)
        News news16 = News.builder()
                .topic("Breakthrough in Fusion Energy: A 'Net Energy Gain' Achieved for the First Time")
                .shortDetail("Scientists at the National Ignition Facility (NIF) have successfully created a fusion reaction that generated more energy than was used to start it, a historic milestone that could lead to clean, limitless power.")
                .fullDetail("The long-awaited promise of fusion energy has taken a giant leap forward. Researchers at the National Ignition Facility (NIF) in California have confirmed that a recent experiment produced 1.5 times more energy from the fusion reaction than the amount of laser energy used to ignite it. This 'net energy gain' is a monumental breakthrough that has eluded scientists for decades and has been dubbed the 'holy grail' of clean energy. The experiment involved firing 192 powerful lasers at a tiny pellet of hydrogen isotopes, creating a brief, superheated plasma state that fused atoms together. This result demonstrates the fundamental feasibility of fusion as a power source, paving the way for the development of commercial fusion power plants. While a fully functioning power plant is still decades away, the achievement has energized the scientific community and attracted billions in new funding. This discovery could completely revolutionize the global energy landscape, providing a safe, carbon-free alternative to fossil fuels and nuclear fission.")
                .image("https://media-cldnry.s-nbcnews.com/image/upload/rockcms/2022-12/221213-Lawrence-Livermore-National-Laboratory-al-1149-adf4a7.jpg")
                .reporter("Sofía Mendoza")
                .dateTime(Instant.parse("2025-08-01T10:00:00Z"))
                .realVotes(83) // 85 (total) - 2 (from comments)
                .fakeVotes(39) // 40 (total) - 1 (from comment)
                .build();

        Comment c16_1 = Comment.builder().username("Jean-Pierre Laurent").text("This is huge! A massive step toward a sustainable future.").image(null).time(Instant.parse("2025-08-01T11:00:00Z")).vote(Vote.REAL).news(news16).build();
        Comment c16_2 = Comment.builder().username("Riko Yamamoto").text("They've been saying this for decades. I'll believe it when I can plug my phone into a fusion reactor.").image(null).time(Instant.parse("2025-08-01T12:00:00Z")).vote(Vote.FAKE).news(news16).build();
        Comment c16_3 = Comment.builder().username("Sarah Jones").text("This is a real project at Lawrence Livermore National Laboratory. This isn't a hoax.").image("https://newenergytimes.com/v2/sr/iter/public/NEWSCI-20221212.PNG").time(Instant.parse("2025-08-01T13:00:00Z")).vote(Vote.REAL).news(news16).build();

        news16.getComments().add(c16_1);
        news16.getComments().add(c16_2);
        news16.getComments().add(c16_3);
        newsDao.save(news16);

        // --- News 17: (Real News) Thailand Coalition --- (ID 21 from JSON)
        News news17 = News.builder()
                .topic("Historic Coalition Government Formed in Thailand After Months of Deadlock")
                .shortDetail("After months without a functioning government, Thailand's leading parties announce a landmark coalition led by both liberal and conservative leaders.")
                .fullDetail("Bangkok, Thailand—In a dramatic turn for Thai politics, two longstanding rival parties—the Progressive Alliance and the National Conservative Front—have announced a coalition government to break a historic parliamentary deadlock. The arrangement makes Thailand's first-ever co-prime ministership, with each leader sharing executive powers for alternating six-month periods. Key posts, such as Defense, Education, and Finance, will be divided between the parties. Reactions have been mixed; many praise the unprecedented unity, while critics fear policy gridlock. The coalition agreement prioritizes education reform, rural infrastructure, and constitutional review. Some international observers speculate this could stabilize the region, though protests have already appeared in major cities. The coming months will test the durability of Thailand’s bold new experiment in shared leadership.")
                .image("https://i.ytimg.com/vi/gha-K8Kgb3k/maxresdefault.jpg")
                .reporter("Suwat S.")
                .dateTime(Instant.parse("2023-10-01T09:30:00Z"))
                .realVotes(90) // 92 (total) - 2 (from comments)
                .fakeVotes(11) // 13 (total) - 2 (from comments)
                .build();

        Comment c17_1 = Comment.builder().username("Pimwipa J.").text("I'm hopeful! Maybe shared power means real change for once.").image(null).time(Instant.parse("2023-10-01T10:00:00Z")).vote(Vote.REAL).news(news17).build();
        Comment c17_2 = Comment.builder().username("Chatchai K.").text("Impossible to reconcile their policies. This won't last the year.").image(null).time(Instant.parse("2023-10-01T10:40:00Z")).vote(Vote.FAKE).news(news17).build();
        Comment c17_3 = Comment.builder().username("William Morris").text("Interesting for regional stability. Let's see if this experiment inspires neighbors.").image("https://media.tenor.com/NQgKo4V-sREAAAAe/interesting-batman.png").time(Instant.parse("2023-10-01T11:10:00Z")).vote(Vote.REAL).news(news17).build();
        Comment c17_4 = Comment.builder().username("Natnicha P.").text("Another political gimmick. Wake me up when they deliver on reforms.").image(null).time(Instant.parse("2023-10-01T11:50:00Z")).vote(Vote.FAKE).news(news17).build();

        news17.getComments().add(c17_1);
        news17.getComments().add(c17_2);
        news17.getComments().add(c17_3);
        news17.getComments().add(c17_4);
        newsDao.save(news17);

        // --- News 18: (Fake News) Underwater Structure --- (ID 18 from JSON)
        News news18 = News.builder()
                .topic("Mysterious Underwater Structure Discovered off the Coast of Thailand")
                .shortDetail("Divers have uncovered a vast, ancient structure off the coast of Phang Nga, believed to predate known Southeast Asian civilizations.")
                .fullDetail("A team of Thai marine archaeologists and international divers have stumbled upon a massive geometric stone formation lying 30 meters underwater off the coast of Phang Nga, Thailand. The structure, covering more than two football fields, consists of rectangular stones stacked in a sophisticated pattern, with carvings that have never been seen in the region before. Preliminary carbon dating of coral growth suggests the formation could be at least 4,000 years old, which would rewrite the current understanding of ancient Southeast Asian societies. The Thai Department of Fine Arts has launched an urgent study while local villagers flock to the area, believing it could be linked to mythical lost kingdoms. Online speculation ranges from Atlantis-like lost cities to evidence of ancient extraterrestrial contact. Researchers urge patience as more scientific data is gathered, but excitement among historians and the public is skyrocketing.")
                .image("https://img.freepik.com/premium-photo/exploring-ancient-civilization-through-underwater-ruins-concept-underwater-archaeology-ancient-civilizations-excavation-techniques-submerged-artifacts-historical-discoveries_864588-42182.jpg")
                .reporter("Thanawat K.")
                .dateTime(Instant.parse("2025-02-26T10:00:00Z"))
                .realVotes(26) // 28 (total) - 2 (from comments)
                .fakeVotes(85) // 87 (total) - 2 (from comments)
                .build();

        Comment c18_1 = Comment.builder().username("Somsak Dee").text("Absolutely incredible! As a diver, I've never seen anything like this before.").image(null).time(Instant.parse("2025-02-26T11:00:00Z")).vote(Vote.REAL).news(news18).build();
        Comment c18_2 = Comment.builder().username("Arisa T.").text("Wow, could this be the remains of Suvarnabhumi? Hope the scientists reveal more soon!").image(null).time(Instant.parse("2025-02-26T11:40:00Z")).vote(Vote.REAL).news(news18).build();
        Comment c18_3 = Comment.builder().username("Kevin Lee").text("Every year there's a new 'lost city' story. I'll believe it when I see peer-reviewed data.").image("https://i.pinimg.com/564x/1a/e8/bc/1ae8bcdcef865fe7a50e34148c8cb1ce.jpg").time(Instant.parse("2025-02-26T12:00:00Z")).vote(Vote.FAKE).news(news18).build();
        Comment c18_4 = Comment.builder().username("Maya Chan").text("มั่ว").image("https://factcheck.afp.com/sites/default/files/styles/image_in_article/public/medias/factchecking/g2/2025-03/65f666cd613f8eed6bc934f3b36b8f8b-en.jpeg?itok=GI33oA2I").time(Instant.parse("2025-02-26T13:00:00Z")).vote(Vote.FAKE).news(news18).build();

        news18.getComments().add(c18_1);
        news18.getComments().add(c18_2);
        news18.getComments().add(c18_3);
        news18.getComments().add(c18_4);
        newsDao.save(news18);

        // --- News 19: (Fake News) Time Slip Japan --- (ID 17 from JSON)
        News news19 = News.builder()
                .topic("First-Ever 'Time Slip' Event Confirmed in Rural Japan")
                .shortDetail("A remote village in Japan has mysteriously reverted to its 1950s appearance for a period of five minutes, with residents and technology from that era briefly appearing before vanishing without a trace.")
                .fullDetail("A small, isolated village in the mountainous Gifu prefecture of Japan became the site of a bizarre temporal anomaly. For five minutes, starting at 14:00 JST, the entire village, along with its inhabitants and all modern technology, was replaced by a perfect recreation of its 1950s self. Residents from the past, dressed in traditional clothing, were seen going about their daily lives, oblivious to the modern world. One local resident, a 70-year-old man, recounted seeing his deceased grandmother and a bustling, long-gone market square. When the five minutes were up, everything reverted to normal, with no physical evidence left behind. The Japanese government has launched an investigation, but has so far been unable to explain the 'time slip.' Theories range from a previously unknown geological or atmospheric event to a mass hallucination, but the consistent testimonies from hundreds of villagers have made a simple explanation difficult to accept. The event has reignited interest in quantum physics and temporal mechanics, but remains one of the most baffling paranormal events of the modern era.")
                .image("https://m.media-amazon.com/images/I/51iLLAL8ioL._UF1000,1000_QL80_.jpg")
                .reporter("Sakura Kobayashi")
                .dateTime(Instant.parse("2018-11-25T02:00:00Z"))
                .realVotes(15)  // 15 (total) - 0 (from comments)
                .fakeVotes(102) // 105 (total) - 3 (from comments)
                .build();

        Comment c19_1 = Comment.builder().username("Jack Phillips").text("A 'time slip'? This is pure fiction. There's no way this is a real news story.").image("https://m.media-amazon.com/images/I/71E2zQwxXyL._UF1000,1000_QL80_.jpg").time(Instant.parse("2018-11-25T03:00:00Z")).vote(Vote.FAKE).news(news19).build();
        Comment c19_2 = Comment.builder().username("Omar Abdelrahman").text("I can't believe anyone would fall for this. It's a creative story, but it's not real.").image(null).time(Instant.parse("2018-11-25T04:00:00Z")).vote(Vote.FAKE).news(news19).build();
        Comment c19_3 = Comment.builder().username("Clarisse Ravelojaona").text("The details are so specific, it almost makes it sound real. But the science is just not there.").image(null).time(Instant.parse("2018-11-25T05:00:00Z")).vote(Vote.FAKE).news(news19).build();

        news19.getComments().add(c19_1);
        news19.getComments().add(c19_2);
        news19.getComments().add(c19_3);
        newsDao.save(news19);

        // --- News 20: (Fake News) Ghibli Sequel --- (ID 20 from JSON)
        News news20 = News.builder()
                .topic("Rumor: Legendary Anime Studio Ghibli Secretly Working on a ‘Spirited Away’ Sequel")
                .shortDetail("Speculation is spreading online that Studio Ghibli is developing a top-secret sequel to its beloved classic ‘Spirited Away,’ with returning staff and new characters.")
                .fullDetail("Anime communities are abuzz after several social media posts claimed that Studio Ghibli has quietly started production on a sequel to the blockbuster animation ‘Spirited Away.’ According to anonymous insiders cited on Japanese forums, the studio’s legendary director Hayao Miyazaki and key original staff members have been seen having meetings behind closed doors. Alleged concept art and cryptic messages on Ghibli’s official website have fueled the speculation further. The rumor suggests that the new film would follow Chihiro, now a teenager, revisiting the spirit world with new adventures and a mysterious new character. Studio Ghibli has not confirmed nor denied these reports, and anime news outlets are urging fans to await official statements. The story has sparked intense debate, excitement, and skepticism across global anime fandoms.")
                .image("https://f.ptcdn.info/797/041/000/o5mkk67wd0ZVOV343aJ-o.jpg")
                .reporter("Kenji Tanaka")
                .dateTime(Instant.parse("2024-11-14T11:00:00Z"))
                .realVotes(52) // 54 (total) - 2 (from comments)
                .fakeVotes(94) // 96 (total) - 2 (from comments)
                .build();

        Comment c20_1 = Comment.builder().username("Jessica Miller").text("There’s no way Miyazaki would revisit Spirited Away. This sounds totally fake.").image(null).time(Instant.parse("2024-11-14T12:00:00Z")).vote(Vote.FAKE).news(news20).build();
        Comment c20_2 = Comment.builder().username("Alex Chen").text("I want to believe! It’s the sequel we all dreamed of.").image("https://media.tenor.com/3bnLV4ZFtv0AAAAe/anime-boy.png").time(Instant.parse("2024-11-15T07:40:00Z")).vote(Vote.REAL).news(news20).build();
        Comment c20_3 = Comment.builder().username("Milena Petrova").text("Every year there's a Ghibli sequel rumor. I won't believe it until there's a trailer.").image(null).time(Instant.parse("2024-11-15T09:25:00Z")).vote(Vote.FAKE).news(news20).build();
        Comment c20_4 = Comment.builder().username("Ying Yue").text("Even if it’s just a rumor, it got everyone talking! Ghibli magic never fades.").image(null).time(Instant.parse("2024-11-15T11:09:00Z")).vote(Vote.REAL).news(news20).build();

        news20.getComments().add(c20_1);
        news20.getComments().add(c20_2);
        news20.getComments().add(c20_3);
        news20.getComments().add(c20_4);
        newsDao.save(news20);

// --- News 21: (Equal News) Billie Eilish --- (ID 23 from JSON)
        News news21 = News.builder()
                .topic("Billie Eilish Tops Global Charts with Surprise Album ‘Reverie’")
                .shortDetail("Pop sensation Billie Eilish makes history as her surprise album ‘Reverie’ debuts at #1 in over 50 countries.")
                .fullDetail("Music fans around the world woke up to a pleasant surprise as multi-Grammy-winning artist Billie Eilish dropped her third studio album ‘Reverie’ without prior announcement. Released digitally at midnight, the album quickly shot to the top of streaming charts in the US, UK, Japan, Thailand, and many more, breaking first-day listening records on several platforms. Critics praise the album’s haunting melodies and deeply personal lyrics. Billie’s management revealed that the album’s rollout was intended to connect with fans directly, skipping traditional media hype. Social media erupted with joy and excitement, and some industry experts predict ‘Reverie’ could be a frontrunner at next year’s major music awards.")
                .image("https://s.isanook.com/jo/0/ui/486/2430997/billieeilish_179147992_3885411088212257_2311196626026262132_n.jpg")
                .reporter("Sofie Poulsen")
                .dateTime(Instant.parse("2024-06-01T07:00:00Z"))
                .realVotes(75) // 77 (total) - 2 (from comments)
                .fakeVotes(76) // 77 (total) - 1 (from comment)
                .build();

        Comment c21_1 = Comment.builder().username("Tarek Salah").text("Her music always surprises me. ‘Reverie’ is her best yet!").image(null).time(Instant.parse("2024-06-02T09:24:00Z")).vote(Vote.REAL).news(news21).build();
        Comment c21_2 = Comment.builder().username("Zoey Adams").text("I listened to the whole album all night. Every track is a masterpiece.").image(null).time(Instant.parse("2024-06-03T12:08:00Z")).vote(Vote.REAL).news(news21).build();
        Comment c21_3 = Comment.builder().username("Maria Papadaki").text("It’s just hype. I don’t get why people are so obsessed.").image(null).time(Instant.parse("2024-06-04T15:39:00Z")).vote(Vote.FAKE).news(news21).build();

        news21.getComments().add(c21_1);
        news21.getComments().add(c21_2);
        news21.getComments().add(c21_3);
        newsDao.save(news21);

        // --- News 22: (Fake News) Ancient Ice Core --- (ID 22 from JSON)
        News news22 = News.builder()
                .topic("Ancient Ice Core Reveals Evidence of Advanced Prehistoric Civilization")
                .shortDetail("A scientific team drilling in Antarctica has discovered an ice core containing highly structured, non-organic materials, suggesting the presence of an advanced, intelligent civilization predating human history.")
                .fullDetail("A team of glaciologists and archaeologists working in a remote part of Antarctica has made a discovery that could fundamentally change our understanding of human history. They have extracted an ice core from deep within the continent that contains highly complex, non-organic structures. These intricate, micro-engineered artifacts are encased in ice that is estimated to be over 2 million years old. The structures, which are made of a metal alloy that does not exist in nature, bear no resemblance to anything found in the fossil record. Dr. Aris Thorne, a lead researcher, stated, 'This is irrefutable evidence of a civilization that existed on Earth long before our own. The structures are far too complex to be a natural formation.' The discovery has been met with both excitement and skepticism, with some claiming it's an elaborate hoax. The government of Antarctica has put a moratorium on all drilling until the origin and purpose of the artifacts can be determined. The finding, if confirmed, would force a complete re-evaluation of human origins and the timeline of life on Earth.")
                .image("https://www.earth.com/assets/_next/image/?url=https%3A%2F%2Fcff2.earth.com%2Fuploads%2F2016%2F08%2F31072358%2Fancient-ice-core-antarctica_1big_ap.jpg&w=1200&q=75")
                .reporter("Julian Frost")
                .dateTime(Instant.parse("2025-04-21T15:00:00Z"))
                .realVotes(35) // 35 (total) - 0 (from comments)
                .fakeVotes(47) // 50 (total) - 3 (from comments)
                .build();

        Comment c22_1 = Comment.builder().username("Patricia Gomez").text("This is ridiculous. It's probably just a strange geological formation. Fake.").image(null).time(Instant.parse("2025-04-21T16:00:00Z")).vote(Vote.FAKE).news(news22).build();
        Comment c22_2 = Comment.builder().username("Jonas Braun").text("Ancient civilization in Antarctica? That's a classic sci-fi trope. I'm not buying it.").image(null).time(Instant.parse("2025-04-21T17:00:00Z")).vote(Vote.FAKE).news(news22).build();
        Comment c22_3 = Comment.builder().username("Jan Kowalski").text("I'm a geologist, and I can tell you that a metal alloy in a 2-million-year-old ice core is a physical impossibility.").image("https://media.tenor.com/DUHB3rClTaUAAAAM/no-pernalonga.gif").time(Instant.parse("2025-04-21T18:00:00Z")).vote(Vote.FAKE).news(news22).build();

        news22.getComments().add(c22_1);
        news22.getComments().add(c22_2);
        news22.getComments().add(c22_3);
        newsDao.save(news22);

        // --- News 23: (Real News) The Bad Guys 2 --- (ID 24 from JSON)
        News news23 = News.builder()
                .topic("‘The Bad Guys 2’ Smashes Box Office Records Upon Release")
                .shortDetail("The beloved animated heist crew is back! ‘The Bad Guys 2’ debuts with stunning visuals and hilarious new adventures, becoming the highest-grossing animated film of 2025’s summer.")
                .fullDetail("DreamWorks Animation's ‘The Bad Guys 2’ has taken theaters by storm, raking in over $300 million worldwide during its opening weekend. Picking up where the original left off, the sequel sees Mr. Wolf, Mr. Snake, and their lovable crew facing an even more cunning nemesis. Critics praise the movie’s witty script, vibrant animation, and clever plot twists. Fans, both young and old, are especially enthusiastic about the new characters and the film's positive messages about friendship and redemption. ‘The Bad Guys 2’ is being lauded for its fresh approach and clever callbacks to the first film, ensuring it appeals to both returning viewers and newcomers.")
                .image("https://static1.srcdn.com/wordpress/wp-content/uploads/2025/08/01744261_poster_w780.jpg?q=49&fit=contain&w=480&dpr=2")
                .reporter("Mr.wolf #1 fan")
                .dateTime(Instant.parse("2025-08-07T07:30:00Z"))
                .realVotes(116) // 120 (total) - 4 (from comments)
                .fakeVotes(5)   // 5 (total) - 0 (from comments)
                .build();

        Comment c23_1 = Comment.builder().username("FilmFanatic44").text("Absolutely loved the sequel! The animation is even better this time, and the jokes were spot on. Five stars from me!").image(null).time(Instant.parse("2025-08-11T09:10:00Z")).vote(Vote.REAL).news(news23).build();
        Comment c23_2 = Comment.builder().username("Mia_movielover").text("The new villain was actually pretty scary for a kids’ film, but it made the story super engaging. My kids and I had a blast!").image(null).time(Instant.parse("2025-08-17T09:30:00Z")).vote(Vote.REAL).news(news23).build();
        Comment c23_3 = Comment.builder().username("CinephileJoe").text("It’s rare that a sequel surpasses the original, but The Bad Guys 2 does it effortlessly. Highly recommend for the whole family!").image("https://i.pinimg.com/1200x/2e/1b/11/2e1b112993b4539896bfe7d372a6f319.jpg").time(Instant.parse("2025-08-24T10:00:00Z")).vote(Vote.REAL).news(news23).build();
        Comment c23_4 = Comment.builder().username("AnimatedAddict").text("The pacing dragged a bit in the middle, but overall it was funny and charming. Worth seeing in theaters.").image(null).time(Instant.parse("2025-08-27T10:25:00Z")).vote(Vote.REAL).news(news23).build();

        news23.getComments().add(c23_1);
        news23.getComments().add(c23_2);
        news23.getComments().add(c23_3);
        news23.getComments().add(c23_4);
        newsDao.save(news23);

        // --- News 24: (Real News) UFO Chiang Mai --- (ID 25 from JSON)
        News news24 = News.builder()
                .topic("ไขปริศนา UFO ลอยเหนือท้องฟ้าเชียงใหม่ สดร.มั่นใจเป็น 'โคมลอย' ไม่ใช่จานบิน")
                .shortDetail("กลายเป็นที่ฮือฮาในโลกโซเชียล หลังมีนักศึกษามหาวิทยาลัยแห่งหนึ่ง ได้บันทึกภาพทั้งคลิปและภาพนิ่งเป็นวัตถุลึกลับดูคล้ายกับจานบินบนท้องฟ้า บริเวณด้านหน้าดอยสุเทพ อ.เมือง จ.เชียงใหม่")
                .fullDetail("สถาบันวิจัยดาราศาสตร์ยืนยันว่าวัตถุปริศนาบนฟ้าเชียงใหม่ 'แค่ข่าวโคมลอย' กลายเป็นที่ฮือฮาในโลกโซเชียล หลังมีนักศึกษามหาวิทยาลัยแห่งหนึ่งบันทึกคลิปและภาพนิ่งของวัตถุลึกลับคล้ายจานบินเหนือดอยสุเทพ ซึ่งถูกแชร์ออกไปกว้างขวางในโลกออนไลน์ บางคนเชื่อว่าเป็น UFO ขณะที่บางคนคาดว่าเป็นโคมลอยหรือลูกโป่งขนาดใหญ่ หลังตรวจสอบพบว่าภาพดังกล่าวถ่ายจากอาคารสูงบริเวณแยกรินคำในช่วงบ่ายวันที่ 8 ตุลาคม 2563 เห็นวัตถุสีดำลอยเหนือดอยสุเทพในเขตควบคุมการบิน ซึ่งวัตถุลอยอยู่นานจนผู้ถ่ายหยุดถ่าย ภายหลังทาง สดร. ตรวจสอบพบว่าลักษณะการลอย การเคลื่อนที่และสีของวัตถุ สอดคล้องกับโคมลอยและไม่ใช่ UFO โดยเป็นความบังเอิญที่มุมภาพคล้ายจานบิน สรุปว่าเป็น ‘ข่าวโคมลอย’ ที่สร้างความฮือฮาเท่านั้น")
                .image("https://s.isanook.com/ns/0/ud/1654/8270282/ufo2.jpg?ip/crop/w1200h700/q80/webp")
                .reporter("ข่าวสดร. – ทีมข่าวไทยรัฐ")
                .dateTime(Instant.parse("2023-10-09T11:00:00Z"))
                .realVotes(72) // 74 (total) - 2 (from comments)
                .fakeVotes(19) // 21 (total) - 2 (from comments)
                .build();

        Comment c24_1 = Comment.builder().username("SkywatcherCMU").text("ตอนแรกที่เห็นก็แปลกใจมาก เหมือน UFO จริง ๆ ขอบคุณ สดร. ที่ชี้แจงให้ชัดเจนครับ").image(null).time(Instant.parse("2023-10-09T12:05:00Z")).vote(Vote.REAL).news(news24).build();
        Comment c24_2 = Comment.builder().username("หมัดดาวเหนือ").text("ผมยังสงสัยอยู่นะว่าบางทีมันอาจจะมีอะไรมากกว่านั้นก็ได้ ใครจะรู้!").image(null).time(Instant.parse("2021-11-01T03:40:00Z")).vote(Vote.FAKE).news(news24).build();
        Comment c24_3 = Comment.builder().username("pim_pim").text("เห็นในโซเชียลแชร์กันหนักมาก สุดท้ายก็เฉลยว่าเป็นแค่โคมลอยนี่เอง โล่งใจค่ะ").image(null).time(Instant.parse("2023-10-09T12:30:00Z")).vote(Vote.REAL).news(news24).build();
        Comment c24_4 = Comment.builder().username("นักล่าท้องฟ้า").text("ข่าวเก่าละโว้ยยยย").image("https://i.imgflip.com/b0xre.jpg").time(Instant.parse("2025-08-27T04:35:00Z")).vote(Vote.FAKE).news(news24).build();

        news24.getComments().add(c24_1);
        news24.getComments().add(c24_2);
        news24.getComments().add(c24_3);
        news24.getComments().add(c24_4);
        newsDao.save(news24);

        // --- News 25: (Real/Fake Mix) AI Education (TH) --- (ID 26 from JSON)
        News news25 = News.builder()
                .topic("AI กับการศึกษายุคใหม่: ไทยพัฒนาเยาวชนอาเซียนใช้เทคโนโลยีแก้ปัญหาสิ่งแวดล้อม")
                .shortDetail("ศธ.ไทยจัดค่ายเยาวชนอาเซียน นำ AI ใช้ในแฮกกาธอนแก้ปัญหาสิ่งแวดล้อม เปิดพื้นที่สร้างสรรค์เทคโนโลยีในโรงเรียน")
                .fullDetail("กระทรวงศึกษาธิการไทยเปิดค่ายเยาวชนอาเซียน AYC 2025 ดึง AI เป็นธีมหลัก เด็กไทย-อาเซียนพัฒนาโครงการสิ่งแวดล้อม เช่น ระบบวิเคราะห์ขยะอัจฉริยะ ใช้ AI ช่วยเรียนรู้ข้ามพรมแดนและร่วมแข่งขัน hackathon สร้างแรงบันดาลใจสู่โลกอนาคตให้เยาวชนไทย[1]")
                .image("https://moe360.blog/wp-content/uploads/2025/06/line_album_2568-06-17_ayc-2025_250617_22.jpg?w=1024")
                .reporter("ทีมข่าว MOE360")
                .dateTime(Instant.parse("2025-06-17T10:00:00Z"))
                .realVotes(29) // 32 (total) - 3 (from comments)
                .fakeVotes(44) // 45 (total) - 1 (from comment)
                .build();

        Comment c25_1 = Comment.builder().username("Orn2545").text("กิจกรรมแนวใหม่แบบนี้ควรมีทุกปี เด็กไทยจะได้เรียนรู้กับเพื่อนประเทศอื่น").image(null).time(Instant.parse("2025-06-17T11:05:00Z")).vote(Vote.REAL).news(news25).build();
        Comment c25_2 = Comment.builder().username("แม่ของเด็กห้อง 6/1").text("AI ใช้ได้แต่ต้องควบคุม ถ้าใช้ไม่ถูกจะกลายเป็นอุปสรรคแทนที่จะเป็นเครื่องมือ").image(null).time(Instant.parse("2025-06-17T11:30:00Z")).vote(Vote.REAL).news(news25).build();
        Comment c25_3 = Comment.builder().username("AntiHypeman").text("พูดเรื่อง AI ได้แต่งบส่งเด็กไปต่างประเทศจริงๆ มีแค่ไม่กี่โรงเรียนใหญ่ รายเล็กอดเหมือนเดิม").image(null).time(Instant.parse("2025-06-17T12:00:00Z")).vote(Vote.FAKE).news(news25).build();
        Comment c25_4 = Comment.builder().username("PoomAI").text("AI ต่อยอดนวัตกรรม เด็กต่างจังหวัดก็ควรเข้าถึงให้เป็นธรรม").image("https://moe360.blog/wp-content/uploads/2025/06/line_album_2568-06-17_ayc-2025_250617_24.jpg?w=1024").time(Instant.parse("2025-06-17T12:45:00Z")).vote(Vote.REAL).news(news25).build();

        news25.getComments().add(c25_1);
        news25.getComments().add(c25_2);
        news25.getComments().add(c25_3);
        news25.getComments().add(c25_4);
        newsDao.save(news25);

        // --- News 26: (Equal News) Lottery (TH) --- (ID 27 from JSON)
        News news26 = News.builder()
                .topic("หวยออกเลขท้ายซ้ำ 3 งวดติด ชาวเน็ตวิจารณ์สนั่น")
                .shortDetail("สร้างเสียงแตก! ลอตเตอรี่ไทยงวด 1 ก.ค., 16 ก.ค. และ 1 ส.ค. 68 เลขท้าย 2 ตัวออกซ้ำ ชาวเน็ตบางส่วนถล่มหนัก")
                .fullDetail("เลขท้าย 2 ตัวสลากกินแบ่งรัฐบาลออกซ้ำ 3 งวดติด (xx, xx, xx) ผู้ถูกรางวัลดีใจ แต่คนเล่นบางส่วนตั้งข้อสงสัย กองสลากฯ ยืนยันกระบวนการโปร่งใส แต่ชาวเน็ตจำนวนมากไม่ปักใจเชื่อ ดราม่าหนักในโซเชียล")
                .image("https://static.thairath.co.th/media/dFQROr7oWzulq5Fa6rBl3hbon4vW4lvDZt1cQeyvmTmwghtq2qbrHZydAPqIRwfUEwQ.webp")
                .reporter("ศิวะพร อรรถชัย")
                .dateTime(Instant.parse("2025-08-01T18:01:00Z"))
                .realVotes(13) // 15 (total) - 2 (from comments)
                .fakeVotes(13) // 15 (total) - 2 (from comments)
                .build();

        Comment c26_1 = Comment.builder().username("lottohungry").text("ซื้อทุกงวดไม่เคยถูก เจอแบบนี้เอือมสุด").image("https://static.thairath.co.th/media/dFQROr7oWzulq5Fa5yMomHxKe768tdYpvQsrBJPpejbGCsgv0aw1LSw7JUTm4Df12tC.webp").time(Instant.parse("2025-08-01T18:30:00Z")).vote(Vote.FAKE).news(news26).build();
        Comment c26_2 = Comment.builder().username("statisticsman").text("เลขซ้ำแบบนี้นานๆ เจอที ความน่าจะเป็นมันก็มี").image(null).time(Instant.parse("2025-08-01T18:40:00Z")).vote(Vote.REAL).news(news26).build();
        Comment c26_3 = Comment.builder().username("กลุ่มส่องกองสลาก").text("โปร่งใสจริงหรือเปล่า ขอให้มีสื่อกลางตรวจสอบ").image(null).time(Instant.parse("2025-08-01T19:00:00Z")).vote(Vote.FAKE).news(news26).build();
        Comment c26_4 = Comment.builder().username("PP").text("บังเอิญแรง แต่ก็เป็นไปได้").image(null).time(Instant.parse("2025-08-01T19:11:00Z")).vote(Vote.REAL).news(news26).build();

        news26.getComments().add(c26_1);
        news26.getComments().add(c26_2);
        news26.getComments().add(c26_3);
        news26.getComments().add(c26_4);
        newsDao.save(news26);

        // --- News 27: (Real News) ThaiTails (TH) --- (ID 28 from JSON)
        News news27 = News.builder()
                .topic("ThaiTails 2025 งานรวมพลแฟน Furry ใหญ่สุดในไทยกลับมาแล้ว!")
                .shortDetail("ThaiTails 2025 รวมศิลปินและแฟน Furry จากทั่วโลก ที่กรุงเทพฯ พร้อมเวิร์กช็อป นิทรรศการ และประกวดชุดคอสตูมสุดสร้างสรรค์")
                .fullDetail("งาน ThaiTails Convention 2025 จัดขึ้น ณ โรงแรมแกรนด์ริชมอนด์ใจกลางเมืองนนทบุรี รวมเหล่าศิลปิน Furry Art, นักคอสตูม และกลุ่มแฟน Furry มากกว่า 7 ประเทศทั่วโลก มีกิจกรรมไฮไลต์ ได้แก่ เวิร์กช็อปวาด Furry, นิทรรศการผลงานศิลปะ, การประกวดชุดมาสคอตสุดอลังการและแฟชั่นโชว์ รายได้ส่วนหนึ่งสมทบทุนช่วยเหลือสัตว์จรจัด งานนี้ได้รับความสนใจจากวงการครีเอเตอร์และแฟนวัฒนธรรมป๊อปแนวมนุษย์สัตว์เป็นพิเศษ และถือเป็นการสร้างเครือข่ายศิลปิน Furry ใหม่ระดับโลกในปี 2025")
                .image("https://scontent.fbkk29-4.fna.fbcdn.net/v/t39.30808-6/485324172_627653066647110_3387189200412230754_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=127cfc&_nc_eui2=AeHOwKFwxANI6i-HxCSfoLCuNuTufPT21RA25O589PbVEBuQkGiTNq3ZqcTMgsF5EkdZPz9KQwUlpvJgDm1Vc3ol&_nc_ohc=W3FWdrBet2IQ7kNvwGoErTA&_nc_oc=AdkN9nch7b9BPG0rws-CKZSd6JKimADf1cjO_cm7izl5tqNcnmGoXUuaxl_VNYNlcLs&_nc_zt=23&_nc_ht=scontent.fbkk29-4.fna&_nc_gid=wSMJsnxbE7Du_CmlnKIXdA&oh=00_AfUk_K43WQyWCTQT4euf0qPxm9d7hzmsjSAwJLG_C8uvxw&oe=68B4A393")
                .reporter("https://2025.thaitails.net/")
                .dateTime(Instant.parse("2025-03-21T12:00:00Z"))
                .realVotes(105) // 108 (total) - 3 (from comments)
                .fakeVotes(54)  // 54 (total) - 0 (from comments)
                .build();

        Comment c27_1 = Comment.builder().username("GintanFurry").text("งานสนุกมาก อยากมาอีกจัง").image("https://scontent.fbkk29-5.fna.fbcdn.net/v/t39.30808-6/481993187_616673434411740_9138297182783178136_n.jpg?_nc_cat=107&ccb=1-7&_nc_sid=f727a1&_nc_eui2=AeGk1fnGLEew2wlyNKLUyv3-6xPg_LRxT0vrE-D8tHFPSx5ud1r5BJrYfvwZ3vAz1up_A2yjfIUaEe4cNHYPp309&_nc_ohc=59PyzeX53ysQ7kNvwEwLIrT&_nc_oc=AdlzB73UqPxSEvJQXkm3HNX5cmQmvJHlLfODBDFhHgLt6GlrnUWifeZd_R7yYygMRKU&_nc_zt=23&_nc_ht=scontent.fbkk29-5.fna&_nc_gid=wgvlSnJv_fAlKY1OwnAbJA&oh=00_AfVdHYV-yfTBWUtbXpj86a7nZ3Q-45hMPNBFJCyCPdYCHg&oe=68B497A9").time(Instant.parse("2025-03-23T13:23:00Z")).vote(Vote.REAL).news(news27).build();
        Comment c27_2 = Comment.builder().username("Nviek5").text("เป็นนักวาดเองงับ อย่าลืมมาอุดหนุนกันด้วยน้าาาา").image("https://scontent.fbkk29-1.fna.fbcdn.net/v/t39.30808-6/464940361_8579169998784671_1916576025435492256_n.jpg?_nc_cat=100&ccb=1-7&_nc_sid=0b6b33&_nc_eui2=AeGqQn76SGwT084Z-TII-Y6KJwiwo5SHo7EnCLCjlIejsXiRgoDEMILxGmE_y_BkysdO-CNSMkz9iZ-ZmkNAXRL3&_nc_ohc=4NW3uZIkaZIQ7kNvwFvWGiK&_nc_oc=AdkYmcd_A2Im8ZZX_vz6XThD2KjYoLnpqjMYAZ9d38J1-rNCA5PDdw8kN1N9g_ujVRk&_nc_zt=23&_nc_ht=scontent.fbkk29-1.fna&_nc_gid=KFaYP1ocnugYKDo09JgE4g&oh=00_AfW5s4VsklibrOUmmEBjR3OIqD93zK6WO3Z1sPR5qHAmQA&oe=68B49564").time(Instant.parse("2025-08-15T14:20:00Z")).vote(Vote.REAL).news(news27).build();
        Comment c27_3 = Comment.builder().username("KuttoKapibara").text("UwU").image("https://scontent.fbkk29-9.fna.fbcdn.net/v/t39.30808-6/519948790_30554946324120280_2478172569738428992_n.jpg?_nc_cat=104&ccb=1-7&_nc_sid=127cfc&_nc_eui2=AeFAAU2wriwlqDVMGQeHj3DLN32uWJDIceU3fa5YkMhx5UYmMNq8j2KY2RvAD08Nl3vWF5YfTbwjihfZ-a-Gci_p&_nc_ohc=6Y5hgKm2x2QQ7kNvwHLtrlu&_nc_oc=Adn9T5pEIQRMPA8A9uBauKsmlpmEPXgjh9vgzxjxf9l6Gq4QgCNvHXBwKehPuF4R6tA&_nc_zt=23&_nc_ht=scontent.fbkk29-9.fna&_nc_gid=fcnJeqr5YQnvTGVFI6aeSQ&oh=00_AfWZymkdumgWNZNuHCzNi8E7H4v4DYSpXmsBGKlaBAXVqw&oe=68B49B8D").time(Instant.parse("2025-08-15T14:35:00Z")).vote(Vote.REAL).news(news27).build();

        news27.getComments().add(c27_1);
        news27.getComments().add(c27_2);
        news27.getComments().add(c27_3);
        newsDao.save(news27);

        // --- News 28: (Fake News) GTA VI (TH) --- (ID 29 from JSON)
        News news28 = News.builder()
                .topic("เปิดตัว 'Grand Theft Auto VI' ประกาศวันวางจำหน่ายปี 2025")
                .shortDetail("Rockstar Games เผยตัวอย่าง GTA VI อย่างเป็นทางการพร้อมยืนยันวันวางจำหน่ายในปี 2025")
                .fullDetail("Rockstar Games ปล่อยเทรลเลอร์แรกเกม Grand Theft Auto VI สร้างกระแสฮือฮาทั่วโลก และประกาศวันวางจำหน่ายอย่างเป็นทางการในปี 2025 แฟนเกมตื่นเต้นกับโลกโอเพ่นเวิลด์ที่ใหญ่กว่าเดิม ตัวละครใหม่ และงานกราฟิกระดับท็อป พร้อมคาดว่าจะเป็นเกมที่ได้รับความนิยมและยอดขายสูงสุดเป็นประวัติการณ์ของซีรีส์")
                .image("https://www.iphone-droid.net/wp-content/uploads/2025/07/gta-vi-could-run-at-60fps-on-ps5-pro-1170x658.jpg")
                .reporter("GAME FOCUS")
                .dateTime(Instant.parse("2025-06-05T21:00:00Z"))
                .realVotes(25) // 27 (total) - 2 (from comments)
                .fakeVotes(63) // 65 (total) - 2 (from comments)
                .build();

        Comment c28_1 = Comment.builder().username("gtavifan").text("รอเล่น GTA VI มาเป็นสิบปี ในที่สุดก็ประกาศแล้ว เย้!").image(null).time(Instant.parse("2025-06-05T21:30:00Z")).vote(Vote.REAL).news(news28).build();
        Comment c28_2 = Comment.builder().username("เกมเมอร์ปริมณฑล").text("เอาจริงปะ เค้าประกาศเลื่อนมาตลอด จะทันจริงไหมปี 2025").image(null).time(Instant.parse("2025-06-05T22:00:00Z")).vote(Vote.FAKE).news(news28).build();
        Comment c28_3 = Comment.builder().username("LuciaLover").text("โลเคชั่นใหม่ ตัวเอกหญิงก็มา รอตั้งแต่เด็ก ตอนนี้ทำงานแล้ว!").image(null).time(Instant.parse("2025-06-05T22:11:00Z")).vote(Vote.REAL).news(news28).build();
        Comment c28_4 = Comment.builder().username("losssantos894").text("ได้ข่าวว่าเลื่อนอีกแล้วนะ ไปปีหน้าเลยอะ TwT").image("https://scontent.fbkk29-9.fna.fbcdn.net/v/t39.30808-6/494521531_1214166926835622_7859417866830018947_n.jpg?stp=dst-jpg_p526x296_tt6&_nc_cat=104&ccb=1-7&_nc_sid=127cfc&_nc_eui2=AeFf96G5LFxqxwB-l5eu9aw9-Jh1olYIRHX4mHWiVghEdc5XBcF4fbdqECq8ZcVxcmcOd8OiNWieILEVJ3_Cr2ax&_nc_ohc=sxTNid1kG88Q7kNvwGyvefR&_nc_oc=Adm6UDNEDQA5JT3wrMxendaRDD5bBgWX4SE2FbG-edclLS_tjydIDOFYmXRt3i1CTag&_nc_zt=23&_nc_ht=scontent.fbkk29-9.fna&_nc_gid=bNnfHOsQ8zuZRKaVzdZzmA&oh=00_AfWEfmjqxBZaMCkW_a1oO9nI8GmIJDZxiP3pD02UiczetQ&oe=68B49C83").time(Instant.parse("2025-06-05T22:44:00Z")).vote(Vote.FAKE).news(news28).build();

        news28.getComments().add(c28_1);
        news28.getComments().add(c28_2);
        news28.getComments().add(c28_3);
        news28.getComments().add(c28_4);
        newsDao.save(news28);

        // --- News 29: (Equal News) Hospital Robot (TH) --- (ID 30 from JSON)
        News news29 = News.builder()
                .topic("เปิดตัวหุ่นยนต์ขนส่งในโรงพยาบาลวชิรพยาบาล ลดภาระงานบุคลากร")
                .shortDetail("หุ่นยนต์ขนส่งเวชภัณฑ์อัจฉริยะเริ่มงานจริงที่โรงพยาบาลวชิร เผยช่วยลดเวลาทำงานบุคลากรลง 25%")
                .fullDetail("โรงพยาบาลวชิรพยาบาลเปิดใช้งานหุ่นยนต์ขนส่งเวชภัณฑ์อัจฉริยะ ส่งเวชภัณฑ์-อาหาร-แฟ้มข้อมูลในตัวอาคาร ช่วยลดงานซ้ำซ้อน เดินทางด้วยเซ็นเซอร์-คุมผ่านแอป ช่วยลดเวลาทำงานบุคลากรลง 25% และลดการสัมผัสข้ามบุคลากร เหมาะกับยุคโควิดและหลังโควิด[3]")
                .image("https://sustainability.pttgcgroup.com/storage/projects/covid-19/shared-robot/image-004.jpg")
                .reporter("BLT Bangkok")
                .dateTime(Instant.parse("2025-03-12T11:10:00Z"))
                .realVotes(39) // 42 (total) - 3 (from comments)
                .fakeVotes(41) // 42 (total) - 1 (from comment)
                .build();

        Comment c29_1 = Comment.builder().username("พี่พยาบาล").text("ช่วยลดรอบเดินเอกสารในอาคารได้จริง งานเบาลงเยอะ").image(null).time(Instant.parse("2025-03-12T12:01:00Z")).vote(Vote.REAL).news(news29).build();
        Comment c29_2 = Comment.builder().username("worrybot").text("หุ่นยนต์จะมาแทนคนในงานไหนบ้างก็รอดูอนาคต").image(null).time(Instant.parse("2025-03-12T12:42:00Z")).vote(Vote.FAKE).news(news29).build();
        Comment c29_3 = Comment.builder().username("watcharobot").text("ระบบยังมี error บ้างเวลาคนเดินตัดหน้า แต่โดยรวมผ่าน").image(null).time(Instant.parse("2025-03-12T13:10:00Z")).vote(Vote.REAL).news(news29).build();
        Comment c29_4 = Comment.builder().username("จิตราภรณ์").text("เทคโนโลยีกับสุขภาพต้องเดินไปด้วยกัน อะไรที่ช่วยงานหนักถือว่าดีมาก ๆ").image(null).time(Instant.parse("2025-03-12T14:01:00Z")).vote(Vote.REAL).news(news29).build();

        news29.getComments().add(c29_1);
        news29.getComments().add(c29_2);
        news29.getComments().add(c29_3);
        news29.getComments().add(c29_4);
        newsDao.save(news29);

        // --- News 30: (Real News) Border Clash (TH) --- (ID 31 from JSON)
        News news30 = News.builder()
                .topic("ข่าวด่วน: เปิดฉากยิง! ปะทะเดือดชายแดนไทย-กัมพูชา สูญเสียทั้งทหารและพลเรือน")
                .shortDetail("รายงานเหตุการณ์ 24 กรกฎาคม 2568 ทหารกัมพูชาเปิดฉากยิง-ยิงจรวดโจมตีหลายจุดตามแนวชายแดนไทย มีผู้เสียชีวิตและบาดเจ็บจำนวนมาก")
                .fullDetail("บันทึกเหตุการณ์การปะทะชายแดนไทย-กัมพูชา วันที่ 24 กรกฎาคม 2568 จากกรมกิจการชายแดนทหาร ระบุเมื่อเวลา 07.45–12.21 น. ทหารกัมพูชาใช้อาวุธครบมือและจรวด BM-21 โจมตีพื้นที่ปราสาทตาเมือน, อำเภอบ้านกรวด (บุรีรัมย์), กาบเชิง (สุรินทร์), น้ำยืนและกันทรลักษ์ (อุบลฯ/ศรีสะเกษ) ทำให้พลเรือนเสียชีวิต 13 ราย ทหารไทยเสียชีวิต 1 นาย และบาดเจ็บรวมเกือบ 60 ราย เหตุการณ์ทำให้ประชาชนหนีออกจากพื้นที่จำนวนมาก ขณะนี้ประเทศไทยเรียกร้องกัมพูชาหยุดละเมิดหลักมนุษยธรรมสากล และขอยืนยันยืนหยัดปกป้องอธิปไตยชาติอย่างถึงที่สุด")
                .image("https://i.ytimg.com/vi/grbrvUWs2Nw/maxresdefault.jpg")
                .reporter("ทีมข่าวความมั่นคง")
                .dateTime(Instant.parse("2025-07-24T21:05:00Z"))
                .realVotes(59) // 62 (total) - 3 (from comments)
                .fakeVotes(28) // 29 (total) - 1 (from comment)
                .build();

        Comment c30_1 = Comment.builder().username("TH_MilitaryNews").text("ขอให้ทหารไทยปลอดภัย ช่วยแยกแยะข่าวลือออกจากข้อเท็จจริงกันด้วย").image(null).time(Instant.parse("2025-07-25T09:12:00Z")).vote(Vote.REAL).news(news30).build();
        Comment c30_2 = Comment.builder().username("Nui_CK").text("เพื่อนบ้านกันไม่น่าทำกันแบบนี้เลย").image(null).time(Instant.parse("2025-07-25T10:41:00Z")).vote(Vote.REAL).news(news30).build();
        Comment c30_3 = Comment.builder().username("crossborderfan").text("หรือจะมีเบื้องหลังทางการเมืองระหว่างประเทศก็ไม่รู้").image(null).time(Instant.parse("2025-07-26T11:53:00Z")).vote(Vote.FAKE).news(news30).build();
        Comment c30_4 = Comment.builder().username("SaKaeoFirst").text("คนในพื้นที่กังวลสุด ๆ ขอให้ทุกฝ่ายใจเย็น").image(null).time(Instant.parse("2025-07-27T15:22:00Z")).vote(Vote.REAL).news(news30).build();

        news30.getComments().add(c30_1);
        news30.getComments().add(c30_2);
        news30.getComments().add(c30_3);
        news30.getComments().add(c30_4);
        newsDao.save(news30);
    }
}
