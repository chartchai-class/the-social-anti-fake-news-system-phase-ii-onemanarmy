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
        // เพื่อให้มี User ก่อนสร้าง News
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
        // --- News 1: (Real News) AI in Medicine ---
        News news1 = News.builder()
                .topic("AI Model Detects Lung Cancer Earlier Than Human Radiologists")
                .shortDetail("A new study shows an AI algorithm successfully identified lung cancer nodules in CT scans with 94% accuracy, often months before human experts.")
                .fullDetail("Researchers from a leading tech institute have developed an artificial intelligence model that demonstrates a significant leap in early-stage lung cancer detection. The study, published in Nature Medicine, involved training the AI on over 45,000 chest CT scans. The model was then tested against a panel of six experienced radiologists. The AI not only matched their accuracy but also identified malignant nodules on scans that were previously marked as 'clear' or 'benign' by human doctors, effectively detecting the disease an average of 8 months earlier in some patients. This breakthrough could lead to earlier treatment and dramatically improve survival rates for the world's deadliest cancer.")
                .image("https://example.com/images/ai_scan.jpg")
                .reporter("Sonia Gupta")
                .dateTime(Instant.parse("2025-10-20T08:00:00Z"))
                .realVotes(198) // Total Real = 198 + 2 (from comments) = 200
                .fakeVotes(3)   // Total Fake = 3 + 1 (from comment) = 4
                .build();

        Comment c1_1 = Comment.builder().username("admin").text("This is incredible news for healthcare. Early detection is key.").image(null).time(Instant.parse("2025-10-20T09:15:00Z")).vote(Vote.REAL).news(news1).build();
        Comment c1_2 = Comment.builder().username("member").text("As a doctor, I'm excited to see how this integrates into our workflow.").image(null).time(Instant.parse("2025-10-20T10:30:00Z")).vote(Vote.REAL).news(news1).build();
        Comment c1_3 = Comment.builder().username("reader").text("I don't trust AI with my health. What if it makes a mistake?").image(null).time(Instant.parse("2025-10-20T11:00:00Z")).vote(Vote.FAKE).news(news1).build();

        news1.getComments().add(c1_1);
        news1.getComments().add(c1_2);
        news1.getComments().add(c1_3);
        newsDao.save(news1);

        // --- News 2: (Fake News) "Miracle" Diet ---
        News news2 = News.builder()
                .topic("New 'Moon Dust' Diet Guarantees 10kg Weight Loss in 3 Days")
                .shortDetail("A viral social media trend claims a new diet based on 'moon dust' powder detoxifies the body and melts fat instantly. Experts are skeptical.")
                .fullDetail("A product called 'Lunar Lipo' or 'Moon Dust' is sweeping across TikTok, promoted by influencers who claim mixing the sparkling powder with water and drinking it three times a day will lead to a 10kg weight loss in just 72 hours. The promoters claim the dust is 'ethically sourced meteor fragments' that 'vibrate at a frequency to dissolve fat cells.' Medical professionals have issued strong warnings, stating there is zero scientific basis for these claims. Dr. Helen Price, a registered dietitian, warned, 'This is not only false but potentially dangerous. We have no idea what is in this powder. Rapid weight loss like this is almost always water weight and is not sustainable or healthy.'")
                .image("https://example.com/images/moon_dust.jpg")
                .reporter("Chris Wong")
                .dateTime(Instant.parse("2025-10-19T14:30:00Z"))
                .realVotes(2)   // Total Real = 2 + 1 (from comment) = 3
                .fakeVotes(146) // Total Fake = 146 + 2 (from comments) = 148
                .build();

        Comment c2_1 = Comment.builder().username("admin").text("This is clearly a scam and dangerous. We are flagging this as fake.").image(null).time(Instant.parse("2025-10-19T15:00:00Z")).vote(Vote.FAKE).news(news2).build();
        Comment c2_2 = Comment.builder().username("reader").text("My cousin tried this and got very sick. This is 100% fake!").image(null).time(Instant.parse("2025-10-19T16:00:00Z")).vote(Vote.FAKE).news(news2).build();
        Comment c2_3 = Comment.builder().username("reader").text("idk my friend said it worked for her").image(null).time(Instant.parse("2025-10-19T17:00:00Z")).vote(Vote.REAL).news(news2).build();

        news2.getComments().add(c2_1);
        news2.getComments().add(c2_2);
        news2.getComments().add(c2_3);
        newsDao.save(news2);

        // --- News 3: (Equal News) Local Policy Debate ---
        News news3 = News.builder()
                .topic("City Council Proposes Ban on Street Food Vendors in Old Town")
                .shortDetail("The Chiang Mai city council has proposed a new regulation to ban all street food vendors from the main Old Town moat roads, citing traffic and hygiene concerns.")
                .fullDetail("A heated debate has begun after the city council introduced a motion to clear all street food vendors from roads immediately surrounding the Old Town moat. Proponents of the ban, including several local business groups, argue that the stalls cause severe traffic congestion during peak hours and raise concerns about public hygiene and waste management. However, opponents argue this is an attack on the city's culture and the livelihood of hundreds of families. 'Street food is the heart of Chiang Mai,' said one vendor. 'This ban will destroy our heritage.' The council is scheduled to vote on the proposal next month after a period of public consultation.")
                .image("https://example.com/images/cm_street_food.jpg")
                .reporter("Jirapat S.")
                .dateTime(Instant.parse("2025-10-18T11:00:00Z"))
                .realVotes(49) // Total Real = 49 + 1 = 50
                .fakeVotes(49) // Total Fake = 49 + 1 = 50
                .build();

        Comment c3_1 = Comment.builder().username("member").text("This isn't fake news, I saw the council meeting agenda. Whether it's a good *idea* is another story, but the report is real.").image(null).time(Instant.parse("2025-10-18T12:00:00Z")).vote(Vote.REAL).news(news3).build();
        Comment c3_2 = Comment.builder().username("reader").text("They say this every year to scare vendors. It will never pass. It's fake.").image(null).time(Instant.parse("2025-10-18T13:00:00Z")).vote(Vote.FAKE).news(news3).build();

        news3.getComments().add(c3_1);
        news3.getComments().add(c3_2);
        newsDao.save(news3);

        // --- News 4: (Real News) Space Exploration ---
        News news4 = News.builder()
                .topic("NASA's Artemis Mission Successfully Launches Orion Crew Capsule")
                .shortDetail("NASA has successfully launched the Orion crew capsule as part of the Artemis I mission, marking a critical step towards returning humans to the Moon.")
                .fullDetail("NASA's new heavy-lift rocket, the Space Launch System (SLS), successfully lifted off from Kennedy Space Center, carrying the uncrewed Orion capsule. The capsule will now spend several weeks in lunar orbit, testing critical systems before returning to Earth. This mission is the first major test of the hardware intended to send astronauts, including the first woman and first person of color, to the lunar surface later this decade. 'This is a historic day, not just for America, but for all of humanity,' said the NASA administrator.")
                .image("https://example.com/images/artemis_launch.jpg")
                .reporter("Tech Insider")
                .dateTime(Instant.parse("2025-10-17T05:00:00Z"))
                .realVotes(310) // 310 + 2 = 312
                .fakeVotes(5)   // 5 + 0 = 5
                .build();

        Comment c4_1 = Comment.builder().username("reader").text("Watched this live! Amazing!").image(null).time(Instant.parse("2025-10-17T05:30:00Z")).vote(Vote.REAL).news(news4).build();
        Comment c4_2 = Comment.builder().username("member").text("Go Artemis! This is real, confirmed on NASA's official feed.").image("https://example.com/images/nasa_feed.jpg").time(Instant.parse("2025-10-17T06:00:00Z")).vote(Vote.REAL).news(news4).build();

        news4.getComments().add(c4_1);
        news4.getComments().add(c4_2);
        newsDao.save(news4);

        // --- News 5: (Fake News) Celebrity Hoax ---
        News news5 = News.builder()
                .topic("Famous Actor 'David Chen' Announces Retirement to Become a Monk")
                .shortDetail("A viral blog post claims beloved actor David Chen is quitting Hollywood to join a monastery in Tibet, selling all his possessions.")
                .fullDetail("A story originating from an obscure entertainment blog has gone viral, claiming that actor David Chen told an 'inside source' he was 'tired of the shallow life' and would be retiring immediately. The article alleges he sold his $50 million mansion and boarded a one-way flight to Lhasa. However, the actor's official publicist released a statement this morning calling the story 'a complete fabrication.' The publicist confirmed Chen is currently in London filming his next movie and has no plans to retire.")
                .image("https://example.com/images/david_chen.jpg")
                .reporter("Gossip Today")
                .dateTime(Instant.parse("2025-10-16T20:00:00Z"))
                .realVotes(10) // 10 + 0 = 10
                .fakeVotes(250) // 250 + 2 = 252
                .build();

        Comment c5_1 = Comment.builder().username("admin").text("Confirmed with the actor's agent. This is 100% false.").image(null).time(Instant.parse("2025-10-17T09:00:00Z")).vote(Vote.FAKE).news(news5).build();
        Comment c5_2 = Comment.builder().username("reader").text("I knew it! He was just posting from the film set yesterday. FAKE.").image(null).time(Instant.parse("2025-10-17T09:15:00Z")).vote(Vote.FAKE).news(news5).build();

        news5.getComments().add(c5_1);
        news5.getComments().add(c5_2);
        newsDao.save(news5);

        // --- News 6: (Real News) Environment ---
        News news6 = News.builder()
                .topic("Rare Rhino Species Shows Signs of Population Recovery")
                .shortDetail("Conservationists report the birth of 12 Javan rhino calves in Ujung Kulon National Park, a significant boost for the critically endangered species.")
                .fullDetail("The Javan rhino, one of the most endangered large mammals on Earth, has seen a glimmer of hope. Officials at Ujung Kulon National Park, the rhino's last remaining habitat, announced that camera traps and field monitoring have confirmed the birth of 12 new calves over the past 18 months. This brings the total estimated population to 85, a slow but steady increase from an all-time low of just 40 individuals in the 1990s. Conservation efforts, including habitat protection and anti-poaching patrols, are being credited for the success.")
                .image("https://example.com/images/javan_rhino.jpg")
                .reporter("Eco Watch")
                .dateTime(Instant.parse("2025-10-15T10:00:00Z"))
                .realVotes(150) // 150 + 2 = 152
                .fakeVotes(1)  // 1 + 0 = 1
                .build();

        Comment c6_1 = Comment.builder().username("member").text("This is fantastic news! We need to protect them.").image(null).time(Instant.parse("2025-10-15T11:00:00Z")).vote(Vote.REAL).news(news6).build();
        Comment c6_2 = Comment.builder().username("reader").text("So happy to hear this, I donated to this park last year.").image(null).time(Instant.parse("2025-10-15T12:00:00Z")).vote(Vote.REAL).news(news6).build();

        news6.getComments().add(c6_1);
        news6.getComments().add(c6_2);
        newsDao.save(news6);

        // --- News 7: (Fake News) Financial Scam ---
        News news7 = News.builder()
                .topic("Government to Deposit 'Digital Stimulus' of $5,000 via New App")
                .shortDetail("A message circulating on WhatsApp claims the government is giving $5,000 to all citizens who download a new 'GovPay' app and link their bank account.")
                .fullDetail("Authorities have issued an alert regarding a sophisticated phishing scam. The message claims a new 'Digital Stimulus' program is being launched and urges users to download an unofficial app. This app, which is not on the official App Store or Play Store, then asks for full bank account details and personal identification numbers. The Treasury Department has confirmed that no such program exists and that this is an attempt to steal banking information. 'The government will never ask for your PIN or full bank details via an app,' a spokesperson warned.")
                .image("https://example.com/images/govpay_scam.jpg")
                .reporter("Cyber Security Today")
                .dateTime(Instant.parse("2025-10-14T13:00:00Z"))
                .realVotes(0)
                .fakeVotes(120) // 120 + 2 = 122
                .build();

        Comment c7_1 = Comment.builder().username("admin").text("This is a dangerous phishing scam. Do not download this app.").image("https://example.com/images/phishing_alert.jpg").time(Instant.parse("2025-10-14T14:00:00Z")).vote(Vote.FAKE).news(news7).build();
        Comment c7_2 = Comment.builder().username("reader").text("My dad almost fell for this. It's fake!").image(null).time(Instant.parse("2025-10-14T15:00:00Z")).vote(Vote.FAKE).news(news7).build();

        news7.getComments().add(c7_1);
        news7.getComments().add(c7_2);
        newsDao.save(news7);

        // --- News 8: (Real News) Tech Gadget ---
        News news8 = News.builder()
                .topic("New Transparent Laptop Screen Unveiled at Tech Expo")
                .shortDetail("A leading tech company showcased a prototype laptop with a fully transparent micro-LED display, allowing users to see through their screen.")
                .fullDetail("At the World Tech Expo in Tokyo, 'TechCorp' stunned audiences by unveiling a laptop prototype featuring a 14-inch transparent micro-LED display. The screen boasts 70% transparency, allowing users to see objects behind it while still displaying vibrant images. The company demonstrated potential uses, such as augmented reality applications for designers and architects. While the prototype is not yet ready for mass production and faces challenges with battery life and contrast in bright light, it provides an exciting glimpse into the future of personal computing.")
                .image("https://example.com/images/transparent_laptop.jpg")
                .reporter("Gadget World")
                .dateTime(Instant.parse("2025-10-13T10:00:00Z"))
                .realVotes(88) // 88 + 2 = 90
                .fakeVotes(9)  // 9 + 1 = 10
                .build();

        Comment c8_1 = Comment.builder().username("member").text("I saw the video from the expo, this is real! Looks amazing.").image(null).time(Instant.parse("2025-10-13T11:00:00Z")).vote(Vote.REAL).news(news8).build();
        Comment c8_2 = Comment.builder().username("reader").text("Looks cool, but seems useless. Why do I need to see through my screen?").image(null).time(Instant.parse("2025-10-13T11:30:00Z")).vote(Vote.REAL).news(news8).build();
        Comment c8_3 = Comment.builder().username("reader").text("Looks photoshopped. I don't believe this is possible yet.").image(null).time(Instant.parse("2025-10-13T12:00:00Z")).vote(Vote.FAKE).news(news8).build();

        news8.getComments().add(c8_1);
        news8.getComments().add(c8_2);
        news8.getComments().add(c8_3);
        newsDao.save(news8);

        // --- News 9: (Fake News) Historical "Discovery" ---
        News news9 = News.builder()
                .topic("Pyramids Found in Antarctica, Government Hides Evidence")
                .shortDetail("Conspiracy website posts grainy satellite images claiming to show three 'perfectly aligned' pyramids poking through the ice in Antarctica.")
                .fullDetail("A website known for paranormal theories claims that NASA satellite images confirm the existence of ancient pyramids in Antarctica, suggesting an advanced civilization lived there before the ice age. The story alleges that world governments are actively suppressing this 'discovery' to prevent a 'paradigm shift.' Geologists, however, have debunked these claims, explaining that the formations are 'nunataks' - natural, pyramid-shaped mountains whose peaks stick out above glaciers. The 'perfect alignment' is a coincidence and a result of natural geological formations and erosion.")
                .image("https://example.com/images/antarctica_pyramid.jpg")
                .reporter("Alien Today")
                .dateTime(Instant.parse("2025-10-12T18:00:00Z"))
                .realVotes(2) // 2 + 0 = 2
                .fakeVotes(98) // 98 + 2 = 100
                .build();

        Comment c9_1 = Comment.builder().username("admin").text("This is a well-known hoax. These are natural mountains (nunataks).").image("https://example.com/images/nunatak_explain.jpg").time(Instant.parse("2025-10-12T19:00:00Z")).vote(Vote.FAKE).news(news9).build();
        Comment c9_2 = Comment.builder().username("member").text("This theory comes up every few years. It's fake, folks.").image(null).time(Instant.parse("2025-10-12T20:00:00Z")).vote(Vote.FAKE).news(news9).build();

        news9.getComments().add(c9_1);
        news9.getComments().add(c9_2);
        newsDao.save(news9);

        // --- News 10: (Real News) Local Event ---
        News news10 = News.builder()
                .topic("Yi Peng Lantern Festival Dates Announced for Chiang Mai")
                .shortDetail("Chiang Mai officials have confirmed the dates for the 2025 Yi Peng Lantern Festival, with mass release events scheduled for November 14-15.")
                .fullDetail("The Chiang Mai provincial government has officially announced the schedule for the highly anticipated 2025 Yi Peng Lantern Festival. The main events, including the traditional mass lantern release, will take place on the evenings of November 14th and 15th, coinciding with the nationwide Loy Krathong festival. This year, officials stated that two major release events will be held: one at the traditional site near Mae Jo and another smaller, city-organized event near the Nawarat Bridge. Tourists are advised to book accommodation and any event tickets early, as high demand is expected.")
                .image("https://example.com/images/yi_peng.jpg")
                .reporter("Chiang Mai News")
                .dateTime(Instant.parse("2025-10-11T09:00:00Z"))
                .realVotes(118) // 118 + 2 = 120
                .fakeVotes(0)
                .build();

        Comment c10_1 = Comment.builder().username("reader").text("Can't wait! Already booked my flight.").image(null).time(Instant.parse("2025-10-11T10:00:00Z")).vote(Vote.REAL).news(news10).build();
        Comment c10_2 = Comment.builder().username("member").text("This is real. The official tourism authority site just posted it.").image(null).time(Instant.parse("2025-10-11T11:00:00Z")).vote(Vote.REAL).news(news10).build();

        news10.getComments().add(c10_1);
        news10.getComments().add(c10_2);
        newsDao.save(news10);

        // --- News 11: (Equal News) Tech Rumor ---
        News news11 = News.builder()
                .topic("Rumor: Next-Gen 'Apple Car' to be Unveiled in 2026")
                .shortDetail("An unconfirmed report from a supply chain analyst claims Apple has finalized its 'Apple Car' design and is targeting a 2026 launch.")
                .fullDetail("A new report from a prominent supply chain analyst suggests Apple's secretive 'Project Titan' (the Apple Car) is back on track. The report alleges that Apple has secured a partnership with a major automotive manufacturer and has finalized a sleek, autonomous EV design. The analyst claims a launch event is being planned for late 2026. However, Apple has made no official announcements, and other tech insiders have dismissed the report, stating the project is still in the R&D phase and has no firm timeline. Apple's stock rose 2% on the rumor before settling back down.")
                .image("https://example.com/images/apple_car_rumor.jpg")
                .reporter("Tech Leaks")
                .dateTime(Instant.parse("2025-10-10T15:00:00Z"))
                .realVotes(24) // 24 + 1 = 25
                .fakeVotes(24) // 24 + 1 = 25
                .build();

        Comment c11_1 = Comment.builder().username("reader").text("This is just a rumor, not real news. Marking it fake.").image(null).time(Instant.parse("2025-10-10T16:00:00Z")).vote(Vote.FAKE).news(news11).build();
        Comment c11_2 = Comment.builder().username("member").text("The news is that the RUMOR exists, which is true. The rumor itself might be fake, but the news report about the rumor is real.").image(null).time(Instant.parse("2025-10-10T17:00:00Z")).vote(Vote.REAL).news(news11).build();

        news11.getComments().add(c11_1);
        news11.getComments().add(c11_2);
        newsDao.save(news11);

        // --- News 12: (Real News) Business ---
        News news12 = News.builder()
                .topic("Local Coffee Chain 'Akha Ama' Opens First International Store in Tokyo")
                .shortDetail("Chiang Mai's famous social enterprise coffee chain, Akha Ama, has officially opened its first international branch in Tokyo, Japan.")
                .fullDetail("Akha Ama Coffee, which started as a small café in Chiang Mai supporting the Akha hill tribe community, has achieved a major milestone. The company opened its first international store in the competitive coffee market of Tokyo. The opening event was attended by many coffee enthusiasts and highlighted the brand's 'bean-to-cup' story, which focuses on fair trade and sustainable farming practices in Northern Thailand. This move is seen as a major success for Thai social enterprises on the global stage.")
                .image("https://example.com/images/akha_ama_tokyo.jpg")
                .reporter("Lanna Business")
                .dateTime(Instant.parse("2025-10-09T10:00:00Z"))
                .realVotes(98) // 98 + 2 = 100
                .fakeVotes(0)
                .build();

        Comment c12_1 = Comment.builder().username("member").text("I'm so proud of them! This is real, I follow their official page.").image("https://example.com/images/akha_ama_post.jpg").time(Instant.parse("2025-10-09T11:00:00Z")).vote(Vote.REAL).news(news12).build();
        Comment c12_2 = Comment.builder().username("reader").text("This is real! My friend in Tokyo just went there.").image(null).time(Instant.parse("2025-10-09T12:00:00Z")).vote(Vote.REAL).news(news12).build();

        news12.getComments().add(c12_1);
        news12.getComments().add(c12_2);
        newsDao.save(news12);

        // --- News 13: (Fake News) Satire ---
        News news13 = News.builder()
                .topic("Study Finds 90% of Office Workers 'Pretending to Type' During Zoom Calls")
                .shortDetail("A satirical report from 'The Onion' claims a new study found the majority of remote workers just 'move their hands randomly' over the keyboard.")
                .fullDetail("A humorous article from satirical website 'The Onion' is being shared seriously on some social media platforms. The article claims the 'Institute for Work Studies' found that 9 out of 10 workers on Zoom calls are not actually typing, but 'just tapping keys rhythmically to simulate productivity.' The article includes fake quotes like, 'I just tap spacebar and the delete key over and over.' While intended as a joke, some users appear to believe the study is real.")
                .image("https://example.com/images/fake_typing.jpg")
                .reporter("The Onion")
                .dateTime(Instant.parse("2025-10-08T14:00:00Z"))
                .realVotes(1)  // 1 + 0 = 1
                .fakeVotes(78) // 78 + 2 = 80
                .build();

        Comment c13_1 = Comment.builder().username("admin").text("This is a satirical article from The Onion, it is not real news.").image(null).time(Instant.parse("2025-10-08T15:00:00Z")).vote(Vote.FAKE).news(news13).build();
        Comment c13_2 = Comment.builder().username("member").text("The Onion is satire... marking as fake.").image(null).time(Instant.parse("2025-10-08T16:00:00Z")).vote(Vote.FAKE).news(news13).build();

        news13.getComments().add(c13_1);
        news13.getComments().add(c13_2);
        newsDao.save(news13);

        // --- News 14: (Real News) Science/Discovery ---
        News news14 = News.builder()
                .topic("Ancient Roman 'Fast Food' Counter Unearthed in Pompeii")
                .shortDetail("Archaeologists in Pompeii have excavated a remarkably well-preserved 'thermopolium,' or Roman-era fast-food counter, complete with painted decorations.")
                .fullDetail("Archaeologists at the Pompeii site have unveiled an exceptional discovery: a 'thermopolium' (a hot-food and drink counter) that was buried by the eruption of Mount Vesuvius in 79 AD. The counter is richly decorated with frescoes of animals, including chickens and ducks, which were likely on the menu. Researchers also found containers with food remnants, providing new insights into the dietary habits of the city's residents. This discovery offers a rare look at the daily life of ordinary Pompeiians.")
                .image("https://example.com/images/pompeii_fast_food.jpg")
                .reporter("History Channel")
                .dateTime(Instant.parse("2025-10-07T07:00:00Z"))
                .realVotes(130) // 130 + 1 = 131
                .fakeVotes(0)
                .build();

        Comment c14_1 = Comment.builder().username("member").text("This is real. I read about this in the Smithsonian magazine. Absolutely fascinating discovery.").image(null).time(Instant.parse("2025-10-07T09:00:00Z")).vote(Vote.REAL).news(news14).build();

        news14.getComments().add(c14_1);
        newsDao.save(news14);

        // --- News 15: (Fake News) "Removed" by Admin ---
        News news15 = News.builder()
                .topic("This News Contained Harmful Information and was Removed")
                .shortDetail("This content was flagged by users and removed by an administrator for violating community guidelines. (This is a test case for 'Removed' status)")
                .fullDetail("This news article originally contained severe misinformation that posed a public safety risk. After review by the moderation team, it has been removed from public view. Only administrators can see this content. Regular users will not see this news item in their feed, unless they specifically filter for 'removed' content (which only admins can do).")
                .image(null)
                .reporter("System")
                .dateTime(Instant.parse("2025-10-01T10:00:00Z"))
                .removed(true) // <-- This news is soft-deleted
                .realVotes(1)  // 1 + 1 = 2
                .fakeVotes(4)  // 4 + 1 = 5
                .build();

        Comment c15_1 = Comment.builder().username("admin").text("Removing this post. It's dangerous and fake.").image(null).time(Instant.parse("2025-10-01T10:05:00Z")).vote(Vote.FAKE).news(news15).build();
        Comment c15_2 = Comment.builder().username("reader").text("What did it say?").image(null).time(Instant.parse("2025-10-01T10:10:00Z")).vote(Vote.REAL).news(news15).build();

        news15.getComments().add(c15_1);
        news15.getComments().add(c15_2);
        newsDao.save(news15);
    }
}
