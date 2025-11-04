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
//commit push 3
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

        // --- ข่าวที่ 16: Giant Sinkhole (FAKE) ---
        News news16 = News.builder()
                .topic("Giant Sinkhole Appears in Downtown Area")
                .shortDetail("Mysterious 500-foot deep sinkhole opens in city center, revealing ancient underground civilization with glowing crystals and hieroglyphic writings that archaeologists claim could rewrite human history.")
                .fullDetail("A massive sinkhole measuring 300 feet in diameter and an estimated 500 feet deep suddenly opened in the heart of downtown yesterday morning, swallowing three city blocks and exposing what city officials are calling an impossible archaeological discovery - the perfectly preserved remains of an advanced underground civilization complete with crystal-powered structures and walls covered in unidentified hieroglyphic symbols. Emergency response coordinator Captain James Mitchell, speaking from behind mysterious government-erected barriers that now surround the entire area, claimed that initial drone surveys revealed architectural elements that shouldn't exist according to our understanding of ancient technology, including what appears to be a functioning power grid made entirely of luminescent crystals that continue to emit a bright blue light. Dr. Elena Vasquez, a self-described xenoarchaeologist who was allegedly the first scientist lowered into the sinkhole, reported discovering tablets written in an unknown language that she believes may contain star maps pointing to Earth's original extraterrestrial visitors, though her findings have been immediately classified by federal authorities who arrived within hours of the discovery. Local geology professor Dr. Robert Kim expressed bewilderment, stating that geological surveys conducted just last month showed no underground cavities in this area, making this sinkhole's appearance scientifically impossible, while residents report strange electromagnetic interference affecting all electronic devices within a six-block radius since the hole appeared. Curiously, city officials have banned all media access to the site and are reportedly evacuating surrounding buildings under the pretense of structural safety concerns, leading conspiracy theorists to speculate that the government is covering up evidence of ancient alien contact rather than addressing what they claim is simply a routine geological phenomenon.")
                .image("https://media.wired.com/photos/59269d64cfe0d93c47430d6b/191:100/w_1280,c_limit/Before-623639136.jpg")
                .reporter("Sarah Lee")
                .dateTime(Instant.parse("2025-08-06T12:00:00Z"))
                .realVotes(11) // 13 - 2
                .fakeVotes(12) // 14 - 2
                .build();

        Comment c16_1 = Comment.builder()
                .username("Anna Suda")
                .text("This is absolutely mind-blowing! If the reports about glowing crystals and hieroglyphics are true, this could be the most important archaeological discovery in human history.")
                .image(null)
                .time(Instant.parse("2025-08-06T15:00:00Z"))
                .vote(Vote.REAL)
                .news(news16)
                .build();

        Comment c16_2 = Comment.builder()
                .username("Michael Prasit")
                .text("No geological evidence, no credible sources, and only mysterious 'classified findings.' Until there's hard proof, this is just another conspiracy theory.")
                .image("https://i.pinimg.com/1200x/86/bd/cc/86bdccac55661b840ac2050a3fe4c359.jpg")
                .time(Instant.parse("2025-08-06T17:00:00Z"))
                .vote(Vote.FAKE)
                .news(news16)
                .build();

        Comment c16_3 = Comment.builder()
                .username("Benjamin Miller")
                .text("I believe this. Governments always try to hide things like this, and the sudden media ban just makes it more convincing that they found something extraordinary underground.")
                .image("https://i.pinimg.com/736x/8e/47/ab/8e47abac1e9ac500a4ba17d9772408fd.jpg")
                .time(Instant.parse("2025-08-06T14:00:00Z"))
                .vote(Vote.REAL)
                .news(news16)
                .build();

        Comment c16_4 = Comment.builder()
                .username("Mia Anderson")
                .text("A 500-foot sinkhole revealing an 'ancient alien civilization' in the middle of a city? That sounds more like a sci-fi movie than real life.")
                .image(null)
                .time(Instant.parse("2025-08-06T16:00:00Z"))
                .vote(Vote.FAKE)
                .news(news16)
                .build();

        news16.getComments().add(c16_1);
        news16.getComments().add(c16_2);
        news16.getComments().add(c16_3);
        news16.getComments().add(c16_4);
        newsDao.save(news16);

        // --- ข่าวที่ 17: Tax Cut ---
        News news17 = News.builder()
                .topic("Government Announces Tax Cut for Small Businesses")
                .shortDetail("Government unveils comprehensive small business tax relief package reducing corporate rates from 25% to 18%, while critics question funding sources and long-term economic impact.")
                .fullDetail("The federal government announced a significant tax relief package for small businesses, reducing the corporate tax rate from 25% to 18% for companies with annual revenues under $2 million, a move that officials estimate will save qualifying businesses an average of $12,000 annually while potentially stimulating job creation and economic growth. Treasury Secretary Patricia Williams explained that the tax cuts, effective January 1st, are designed to help small enterprises recover from recent economic challenges and compete more effectively with larger corporations, though the initiative has sparked intense debate among economists and lawmakers about its fiscal implications. The proposal includes additional incentives such as expanded equipment depreciation allowances and simplified tax filing procedures, with the Small Business Administration projecting that approximately 850,000 businesses nationwide could benefit from the reduced rates. However, opposition leaders have raised concerns about the estimated $8.5 billion annual revenue loss to the federal budget, questioning how the government plans to offset the reduced income without cutting essential services or increasing the national deficit. Economic analyst Dr. Robert Chen from the Brookings Institution offered cautious optimism, noting that targeted tax relief can stimulate business investment and hiring, but the effectiveness depends largely on broader economic conditions and whether businesses actually use the savings for expansion rather than simply increasing profits. Meanwhile, small business owner Maria Rodriguez welcomed the news but expressed skepticism about the timing, stating that while tax cuts are always appreciated, many businesses are more concerned about supply chain disruptions and labor shortages that tax relief alone cannot address. The legislation still requires congressional approval, with heated debates expected over the coming weeks as lawmakers from both parties weigh the potential economic benefits against concerns about fiscal responsibility and equitable tax policy.")
                .image("https://assets.bwbx.io/images/users/iqjWHBFdfxIU/iNyEyqdV3pYY/v0/-1x-1.webp")
                .reporter("John Miller")
                .dateTime(Instant.parse("2025-08-07T16:00:00Z"))
                .realVotes(21) // 23 - 2
                .fakeVotes(21) // 23 - 2
                .build();

        Comment c17_1 = Comment.builder()
                .username("Golf")
                .text("This could be a real boost for small businesses. Cutting corporate taxes from 25% to 18% would definitely help struggling enterprises and stimulate the economy.")
                .image(null)
                .time(Instant.parse("2025-08-07T20:00:00Z"))
                .vote(Vote.REAL)
                .news(news17)
                .build();

        Comment c17_2 = Comment.builder()
                .username("Mila Sanchez")
                .text("I believe this is true. Governments often implement targeted tax relief programs, and the projected benefits for thousands of small businesses make sense.")
                .image(null)
                .time(Instant.parse("2025-08-07T21:00:00Z"))
                .vote(Vote.REAL)
                .news(news17)
                .build();

        Comment c17_3 = Comment.builder()
                .username("Stella Rogers")
                .text("An $8.5 billion budget hole? Sounds suspicious. Until Congress actually passes it and official documents are released, I'm skeptical this will happen.")
                .image("https://i.pinimg.com/736x/2c/5c/21/2c5c212e52ff6cd3d8b853758d54ba28.jpg")
                .time(Instant.parse("2025-08-07T20:00:00Z"))
                .vote(Vote.FAKE)
                .news(news17)
                .build();

        Comment c17_4 = Comment.builder()
                .username("Aum")
                .text("$8.5B lost? Seems fishy.")
                .image("https://i.pinimg.com/736x/29/e8/05/29e80518f21a781c1ddaf1479c298bf9.jpg")
                .time(Instant.parse("2025-08-07T19:00:00Z"))
                .vote(Vote.FAKE)
                .news(news17)
                .build();

        news17.getComments().add(c17_1);
        news17.getComments().add(c17_2);
        news17.getComments().add(c17_3);
        news17.getComments().add(c17_4);
        newsDao.save(news17);

        // --- ข่าวที่ 18: Holographic Phone ---
        News news18 = News.builder()
                .topic("New Smartphone Released with Holographic Display")
                .shortDetail("Samsung unveils Galaxy Holo featuring advanced light field technology that projects 3D holographic images up to 6 inches above the screen, priced at $2,800 for early adopters.")
                .fullDetail("Samsung has officially launched the Galaxy Holo, marking a significant milestone in mobile technology with the world's first commercially available smartphone featuring a functional holographic display system that uses advanced light field projection technology to create floating 3D images up to 6 inches above the device's surface. The breakthrough technology, developed in partnership with Israeli startup Looking Glass Factory, employs a proprietary array of micro-LEDs and precision optics to generate viewable holograms without requiring special glasses, though the effect is currently limited to a 45-degree viewing angle and works best in controlled lighting conditions. Samsung's head of mobile innovation, Dr. Lisa Park, demonstrated the device at Mobile World Congress, showing holographic video calls, 3D gaming experiences, and interactive product visualizations that respond to hand gestures detected by the phone's advanced depth sensors. The Galaxy Holo, priced at $2,800 for the 256GB model, represents a premium offering targeted at early technology adopters, content creators, and professionals in fields like architecture and medical imaging who could benefit from true 3D visualization capabilities. Technology analyst Ming-Chi Kuo noted that while the holographic display is genuinely impressive, current limitations including battery drain, viewing angle restrictions, and high production costs will likely keep this as a niche product until the technology matures. Early reviews from tech journalists who tested preview units report that the holographic effect works remarkably well for certain applications like 3D modeling and augmented reality experiences, though conventional smartphone tasks like reading text or browsing social media show little practical advantage over traditional displays. Samsung plans to release software development tools later this year to encourage app creators to explore holographic interfaces, while the company projects that refined versions of the technology could appear in mainstream devices within 3-5 years as manufacturing costs decrease and technical limitations are addressed.")
                .image("https://thedebrief.b-cdn.net/wp-content/uploads/2024/04/hologram.jpg")
                .reporter("Laura White")
                .dateTime(Instant.parse("2025-08-08T11:00:00Z"))
                .realVotes(26) // 29 - 3
                .fakeVotes(11) // 11 - 0
                .build();

        Comment c18_1 = Comment.builder()
                .username("Hannah")
                .text("Wow, a holographic phone? Looks legit.")
                .image("https://i.pinimg.com/736x/bb/71/23/bb7123aea24b79177c5a1602796fe70e.jpg")
                .time(Instant.parse("2025-08-08T12:00:00Z"))
                .vote(Vote.REAL)
                .news(news18)
                .build();

        Comment c18_2 = Comment.builder()
                .username("Aurora Collins")
                .text("This could actually be real tech, super cool!")
                .image(null)
                .time(Instant.parse("2025-08-08T13:00:00Z"))
                .vote(Vote.REAL)
                .news(news18)
                .build();

        Comment c18_3 = Comment.builder()
                .username("Kevin Young")
                .text("Exciting innovation! The price is steep, but if this tech is legit, it could set a new standard for mobile devices.")
                .image(null)
                .time(Instant.parse("2025-08-08T15:00:00Z"))
                .vote(Vote.REAL)
                .news(news18)
                .build();

        news18.getComments().add(c18_1);
        news18.getComments().add(c18_2);
        news18.getComments().add(c18_3);
        newsDao.save(news18);

        // --- ข่าวที่ 19: Renewable Energy Plan ---
        News news19 = News.builder()
                .topic("Government Launches New Renewable Energy Plan")
                .shortDetail("The government launches a $50 billion renewable energy plan aiming for 50% clean electricity by 2030, including major solar and wind projects, consumer incentives up to $15,000, and creating 250,000 green jobs to establish the nation as a global clean energy leader.")
                .fullDetail("The government has launched an ambitious $50 billion renewable energy initiative aimed at transforming the nation's power infrastructure, with the goal of generating 50% of electricity from renewable sources by 2030. This comprehensive plan includes the construction of 15 major solar farms and 20 offshore wind installations, alongside advanced energy storage systems and a $12 billion smart grid modernization program to enhance efficiency and reliability. Citizens will receive substantial benefits including rebates up to $15,000 for solar panel installations, zero-interest loans for energy upgrades, and $8,000 credits for electric vehicle purchases, while businesses can access enhanced tax incentives for clean energy investments. The initiative is projected to create 250,000 new jobs across engineering, manufacturing, and installation sectors, reduce household energy costs by 30%, and decrease national carbon emissions by 40% within five years. Energy Minister Sarah Chen emphasized that the plan represents more than climate action, stating it will secure energy independence and position the country as a global leader in clean energy technology, with quarterly progress reports ensuring full transparency and accountability throughout the implementation process.")
                .image("https://media.nationthailand.com/uploads/images/md/2024/06/wfnb7E2ihaF0IGs0Vk53.webp")
                .reporter("Michael Lee")
                .dateTime(Instant.parse("2025-08-14T16:27:45.406629Z"))
                .realVotes(148) // 150 - 2
                .fakeVotes(149) // 150 - 1
                .build();

        Comment c19_1 = Comment.builder()
                .username("John Doe")
                .text("Great step forward! 🌱 Very promising for the economy and environment.")
                .image("https://images.unsplash.com/photo-1654083198752-56ff209c8129?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80")
                .time(Instant.parse("2025-08-14T10:30:00Z"))
                .vote(Vote.REAL)
                .news(news19)
                .build();

        Comment c19_2 = Comment.builder()
                .username("Jane Smith")
                .text("This could be a game changer for the environment. If they follow through, this plan would not only help with clean energy but also create thousands of jobs.")
                .image(null)
                .time(Instant.parse("2025-08-14T11:45:00Z"))
                .vote(Vote.REAL)
                .news(news19)
                .build();

        Comment c19_3 = Comment.builder()
                .username("Fake News Watcher")
                .text("I'm not buying it. A 50% renewable target by 2030 sounds too ambitious, especially with how slow the implementation has been in the past.")
                .image(null)
                .time(Instant.parse("2025-08-14T12:00:00Z"))
                .vote(Vote.FAKE)
                .news(news19)
                .build();

        news19.getComments().add(c19_1);
        news19.getComments().add(c19_2);
        news19.getComments().add(c19_3);
        newsDao.save(news19);

        // --- ข่าวที่ 20: Mystery Illness ---
        News news20 = News.builder()
                .topic("Mystery Illness Spreads in Rural Town")
                .shortDetail("Over 200 residents in Millbrook have contracted an unknown respiratory illness, prompting CDC investigation into the mysterious pathogen causing fever and persistent cough.")
                .fullDetail("The quiet farming community of Millbrook, population 3,500, has become the center of an intensive medical investigation after more than 200 residents developed a mysterious respiratory illness characterized by severe cough, high fever, and prolonged fatigue that doesn't match any known disease patterns. The outbreak began three weeks ago when several patients visited the local clinic with similar symptoms that failed to respond to standard treatments for flu, COVID-19, or bacterial infections. Dr. Elizabeth Warren, the town's chief medical officer, immediately contacted state health authorities when preliminary tests came back negative for all common respiratory pathogens. The Centers for Disease Control and Prevention has since deployed a rapid response team led by epidemiologist Dr. Michael Rodriguez, who established a temporary command center at Millbrook High School to coordinate the investigation and provide additional medical support. Environmental health specialists are examining potential sources including the town's water supply, a nearby agricultural processing plant, and recent changes in local farming practices, while virologists at the CDC's Atlanta laboratory are analyzing blood and tissue samples using advanced genetic sequencing techniques. Mayor Sarah Thompson has implemented voluntary isolation protocols and canceled all public gatherings as a precautionary measure, while assuring residents that food and medical supplies continue to arrive daily. The illness appears to have a recovery rate of 95% with most patients showing improvement after 10-14 days of supportive care, though health officials emphasize the importance of identifying the cause to prevent further spread to neighboring communities.")
                .image("https://i.guim.co.uk/img/media/ac19dc69e4510d4f2306f1450a097c5d95ea7a26/0_109_2500_1499/master/2500.jpg?width=1200&quality=85&auto=format&fit=max&s=3a5cdfed87e118dcdf0cd2ae924f99fd")
                .reporter("John Miller")
                .dateTime(Instant.parse("2025-08-10T16:00:00Z"))
                .realVotes(7) // 8 - 1
                .fakeVotes(10) // 14 - 4
                .build();

        Comment c20_1 = Comment.builder()
                .username("Penelope Gonzalez")
                .text("Hope the CDC finds the cause soon—this sounds serious.")
                .image(null)
                .time(Instant.parse("2025-08-10T21:00:00Z"))
                .vote(Vote.REAL)
                .news(news20)
                .build();

        Comment c20_2 = Comment.builder()
                .username("Nok")
                .text("200 people sick? Sounds exaggerated.")
                .image("https://i.pinimg.com/564x/03/9f/98/039f988ed60a27700294098950ff457f.jpg")
                .time(Instant.parse("2025-08-10T21:00:00Z"))
                .vote(Vote.FAKE)
                .news(news20)
                .build();

        Comment c20_3 = Comment.builder()
                .username("Plaa")
                .text("Could be panic or misinformation, I'm skeptical.")
                .image(null)
                .time(Instant.parse("2025-08-10T19:00:00Z"))
                .vote(Vote.FAKE)
                .news(news20)
                .build();

        Comment c20_4 = Comment.builder()
                .username("Liu Qiang")
                .text("Until CDC releases official statement, I won't believe it😡")
                .image(null)
                .time(Instant.parse("2025-08-10T21:00:00Z"))
                .vote(Vote.FAKE)
                .news(news20)
                .build();

        Comment c20_5 = Comment.builder()
                .username("Jay")
                .text("Could just be flu misreported, not really unknown.")
                .image(null)
                .time(Instant.parse("2025-08-10T20:00:00Z"))
                .vote(Vote.FAKE)
                .news(news20)
                .build();

        news20.getComments().add(c20_1);
        news20.getComments().add(c20_2);
        news20.getComments().add(c20_3);
        news20.getComments().add(c20_4);
        news20.getComments().add(c20_5);
        newsDao.save(news20);

        // --- ข่าวที่ 21: Solar Farm ---
        News news21 = News.builder()
                .topic("World's Largest Solar Farm Opens")
                .shortDetail("The Al Dhafra Solar Farm in Abu Dhabi started operations, generating 2 gigawatts of clean energy to power 200,000 homes, marking a key milestone in renewable energy.")
                .fullDetail("The Al Dhafra Solar Farm in Abu Dhabi has officially commenced operations as the world's largest single-site solar installation, spanning 20 square kilometers and generating 2 gigawatts of clean electricity capable of powering approximately 200,000 homes while reducing carbon emissions by 2.4 million tons annually. The $871 million project, developed through a partnership between Emirates Water and Electricity Company, Masdar, EDF Renewables, and Jinko Power, features over 4 million high-efficiency bifacial solar panels that utilize both direct sunlight and reflected light from the ground to maximize energy production. Crown Prince of Abu Dhabi Sheikh Khaled bin Mohamed bin Zayed Al Nahyan inaugurated the facility yesterday, emphasizing the UAE's commitment to achieving net-zero emissions by 2050 and positioning the nation as a global leader in renewable energy technology. The project employed over 4,000 workers during its three-year construction phase and represents a significant step toward the UAE's goal of generating 50% of its electricity from clean sources by 2050. International energy analysts predict that the farm's success will accelerate similar mega-projects worldwide, with the International Renewable Energy Agency noting that such large-scale installations are crucial for achieving global climate targets. The facility incorporates advanced tracking systems that follow the sun's movement throughout the day, increasing energy output by up to 20% compared to fixed installations, while also serving as a testing ground for next-generation photovoltaic technologies that could further revolutionize the solar industry.")
                .image("https://agreenerlifeagreenerworld.net/wp-content/uploads/2024/07/tarim-basin-solar-farm.-photo-credit-cfp.jpeg")
                .reporter("Alice Johnson")
                .dateTime(Instant.parse("2025-08-11T15:00:00Z"))
                .realVotes(12) // 13 - 1
                .fakeVotes(2) // 3 - 1
                .build();

        Comment c21_1 = Comment.builder()
                .username("Nokky")
                .text("Makes sense with all the solar tech advances.")
                .image(null)
                .time(Instant.parse("2025-08-11T16:00:00Z"))
                .vote(Vote.REAL)
                .news(news21)
                .build();

        Comment c21_2 = Comment.builder()
                .username("Joy")
                .text("Too perfect to be true, need proof.")
                .image("https://i.pinimg.com/736x/34/64/ad/3464ad1c33c983b87d66f14b092f11ee.jpg")
                .time(Instant.parse("2025-08-11T18:00:00Z"))
                .vote(Vote.FAKE)
                .news(news21)
                .build();

        news21.getComments().add(c21_1);
        news21.getComments().add(c21_2);
        newsDao.save(news21);

        // --- ข่าวที่ 22: Lab-Grown Meat ---
        News news22 = News.builder()
                .topic("Researchers Develop Lab-Grown Meat at Scale")
                .shortDetail("Scientists at Netherlands-based Mosa Meat have achieved commercial-scale production of lab-grown beef, reducing costs by 90% and paving the way for widespread availability of cultivated meat in supermarkets by 2026.")
                .fullDetail("Breakthrough research by Mosa Meat, the Netherlands-based biotechnology company founded by Dr. Mark Post, has successfully scaled up lab-grown beef production to industrial levels, achieving a dramatic 90% cost reduction that brings cultivated meat closer to price parity with conventional beef for the first time in the industry's history. The company's new 50,000-square-foot facility in Maastricht can produce 10,000 pounds of cultivated beef monthly using advanced bioreactor technology that grows animal cells in nutrient-rich media without the need for animal slaughter. Dr. Post, who created the world's first lab-grown burger in 2013, announced that production costs have dropped from $330,000 per pound to under $50 per pound through optimized cell lines, improved growth media formulations, and automated manufacturing processes. The European Food Safety Authority granted preliminary approval for the product last month, with full commercial authorization expected by early 2025, while the company has secured partnerships with major European retailers including Albert Heijn and Carrefour for distribution across 15 countries. Environmental scientists praise the development, noting that cultivated meat production requires 96% less land, 82% less water, and generates 78% fewer greenhouse gas emissions compared to conventional beef farming. Industry analysts project that the global cultivated meat market could reach $25 billion by 2030, with competing companies like Upside Foods and Good Meat racing to achieve similar production milestones as consumer acceptance grows and regulatory frameworks evolve worldwide.")
                .image("https://cdn.mos.cms.futurecdn.net/ZTP8d2zAcm5Yjz2VjNZJBM-1200-80.jpg")
                .reporter("John Miller")
                .dateTime(Instant.parse("2025-08-12T20:00:00Z"))
                .realVotes(17) // 19 - 2
                .fakeVotes(16) // 19 - 3
                .build();

        Comment c22_1 = Comment.builder()
                .username("Oil")
                .text("I'd like to see real supermarket availability first.")
                .image("https://i.pinimg.com/1200x/43/76/5a/43765acb57f02cb60b92d22c53ee1401.jpg")
                .time(Instant.parse("2025-08-13T00:00:00Z"))
                .vote(Vote.FAKE)
                .news(news22)
                .build();

        Comment c22_2 = Comment.builder()
                .username("Jaruwan Boonmak")
                .text("Could be marketing spin, not reality yet.")
                .image("https://i.pinimg.com/736x/3a/5c/91/3a5c9122e645b7c6b7f7f335779ee89e.jpg")
                .time(Instant.parse("2025-08-12T21:00:00Z"))
                .vote(Vote.FAKE)
                .news(news22)
                .build();

        Comment c22_3 = Comment.builder()
                .username("Ethan White")
                .text("Too perfect to be true, need verification.")
                .image("https://i.pinimg.com/736x/1b/96/c5/1b96c54cbd0339b11a7da86d65f06a6e.jpg")
                .time(Instant.parse("2025-08-13T00:00:00Z"))
                .vote(Vote.FAKE)
                .news(news22)
                .build();

        Comment c22_4 = Comment.builder()
                .username("Worawit Promraksa")
                .text("If experts say it's real, I trust this.")
                .image(null)
                .time(Instant.parse("2025-08-12T23:00:00Z"))
                .vote(Vote.REAL)
                .news(news22)
                .build();

        Comment c22_5 = Comment.builder()
                .username("Kim Ji-hyun")
                .text("This could really change the way we eat.")
                .image(null)
                .time(Instant.parse("2025-08-12T21:00:00Z"))
                .vote(Vote.REAL)
                .news(news22)
                .build();

        news22.getComments().add(c22_1);
        news22.getComments().add(c22_2);
        news22.getComments().add(c22_3);
        news22.getComments().add(c22_4);
        news22.getComments().add(c22_5);
        newsDao.save(news22);

        // --- ข่าวที่ 23: Data Breach ---
        News news23 = News.builder()
                .topic("Major Data Breach Affects Millions of Users")
                .shortDetail("ConnectWorld revealed that hackers breached the personal data of 45 million users, including passwords and financial info, leading to security updates and a government investigation.")
                .fullDetail("ConnectWorld, the popular social networking platform with over 200 million active users, announced yesterday that cybercriminals successfully breached their security systems and accessed sensitive personal information of approximately 45 million users between March 15-22, including usernames, email addresses, encrypted passwords, phone numbers, and stored payment card details for premium subscribers. The company's Chief Security Officer, Dr. Jennifer Martinez, revealed during an emergency press conference that the attack was discovered by their automated threat detection system on March 23, when unusual data transfer patterns triggered security alerts, leading to the immediate involvement of cybersecurity firm CyberShield and federal law enforcement agencies. The sophisticated breach appears to have exploited a previously unknown vulnerability in the platform's third-party authentication system, which hackers used to gain elevated access privileges and extract user data over a seven-day period before detection. ConnectWorld CEO Michael Thompson issued a formal apology and announced that all affected users have been automatically logged out and must reset their passwords, while the company has implemented additional multi-factor authentication requirements and upgraded their encryption protocols. The FBI's Cyber Crime Division, led by Special Agent Sarah Collins, has launched a full investigation and believes the attack may be linked to the international hacking group known as 'DataVault,' which has previously targeted major technology companies for financial gain. Cybersecurity experts warn that affected users should immediately change passwords on other accounts that may share similar credentials, monitor their financial statements for unauthorized transactions, and remain vigilant for phishing attempts as stolen data often appears on dark web marketplaces within weeks of major breaches.")
                .image("https://www.secpod.com/blog/wp-content/uploads/2020/06/data-breach.jpg")
                .reporter("Sophia Martinez")
                .dateTime(Instant.parse("2025-08-13T14:00:00Z"))
                .realVotes(19) // 19 - 0
                .fakeVotes(29) // 29 - 0
                .build();

        newsDao.save(news23); // ข่าวนี้ไม่มี comment เริ่มต้น

        // --- ข่าวที่ 24: Drone Delivery (FAKE) ---
        News news24 = News.builder()
                .topic("Drone Delivery Service Expands Nationwide")
                .shortDetail("SkyDelivery announced plans to launch drone delivery services in 50 major cities by year-end, sparking debate among aviation experts and local officials about airspace safety and regulatory oversight.")
                .fullDetail("SkyDelivery, the innovative logistics company backed by venture capital firm TechVentures, unveiled ambitious plans yesterday to expand their autonomous drone delivery network to 50 major metropolitan areas across the United States by December 2025, representing the largest civilian drone operation in aviation history and igniting fierce debate among industry experts, federal regulators, and municipal authorities. CEO David Park announced that the company's fleet of 10,000 advanced hexacopter drones will provide 30-minute delivery of packages weighing up to 5 pounds within a 15-mile radius of designated distribution centers, utilizing artificial intelligence navigation systems and redundant safety protocols developed in partnership with aerospace manufacturer AeroDyne Technologies. The Federal Aviation Administration has granted conditional approval for the expansion under their new Commercial Drone Integration Program, though aviation safety expert Dr. Linda Harrison from the National Transportation Safety Board expressed concerns about potential mid-air collisions and the strain on already congested urban airspace. Local government officials remain divided on the initiative, with Seattle Mayor James Rodriguez praising the environmental benefits and job creation potential, while Phoenix City Council member Maria Santos cited noise pollution and privacy invasion as primary concerns requiring additional public hearings. The service promises to revolutionize e-commerce delivery for major retailers including QuickMart and GlobalShop, though critics argue that insufficient testing of the AI guidance systems in adverse weather conditions could pose significant risks to public safety. Transportation policy analyst Professor Robert Chen from Stanford University predicts that successful implementation could trigger a $50 billion transformation of the logistics industry within five years, while also necessitating comprehensive updates to federal aviation regulations and local zoning ordinances to accommodate the new aerial delivery infrastructure.")
                .image("https://s24806.pcdn.co/wp-content/uploads/2025/06/doordash-flytrex-delivery-970.jpg")
                .reporter("Alice Johnson")
                .dateTime(Instant.parse("2025-08-14T13:00:00Z"))
                .realVotes(0) // 3 - 3
                .fakeVotes(19) // 21 - 2
                .build();

        Comment c24_1 = Comment.builder()
                .username("Maxie")
                .text("Too ambitious, I'm skeptical.")
                .image("https://i.pinimg.com/1200x/24/17/24/241724215dde007280e57ec42e43793e.jpg")
                .time(Instant.parse("2025-08-14T18:00:00Z"))
                .vote(Vote.FAKE)
                .news(news24)
                .build();

        Comment c24_2 = Comment.builder()
                .username("Jiraporn Boonying")
                .text("Urban airspace is too crowded for this.")
                .image(null)
                .time(Instant.parse("2025-08-14T15:00:00Z"))
                .vote(Vote.FAKE)
                .news(news24)
                .build();

        Comment c24_3 = Comment.builder()
                .username("Aronong Prasertsuk")
                .text("Officials provide updates on the situation.")
                .image("https://i.pinimg.com/736x/f5/13/c1/f513c1879d645f57174e88034f5692a7.jpg")
                .time(Instant.parse("2025-08-14T17:00:00Z"))
                .vote(Vote.REAL)
                .news(news24)
                .build();

        Comment c24_4 = Comment.builder()
                .username("Toon")
                .text("Makes sense with current drone technology.")
                .image("https://i.pinimg.com/1200x/11/10/6c/11106ccb869afcab5bba203a23a4d896.jpg")
                .time(Instant.parse("2025-08-14T16:00:00Z"))
                .vote(Vote.REAL)
                .news(news24)
                .build();

        Comment c24_5 = Comment.builder()
                .username("Joshua Nelson")
                .text("If this works as promised, it could revolutionize the delivery industry. The potential for fast and efficient deliveries is huge, but airspace safety will be key.")
                .image(null)
                .time(Instant.parse("2025-08-14T17:00:00Z"))
                .vote(Vote.REAL)
                .news(news24)
                .build();

        news24.getComments().add(c24_1);
        news24.getComments().add(c24_2);
        news24.getComments().add(c24_3);
        news24.getComments().add(c24_4);
        news24.getComments().add(c24_5);
        newsDao.save(news24);

        // --- ข่าวที่ 25: Heatwave ---
        News news25 = News.builder()
                .topic("Unprecedented Heatwave Hits Northern Europe")
                .shortDetail("Northern Europe faces record temperatures of 45°C, causing power outages and emergency cooling centers as residents struggle with the extreme heat.")
                .fullDetail("An extraordinary heatwave has engulfed Northern Europe for the past week, with temperatures soaring to unprecedented levels of 47°C in Stockholm, 45°C in Helsinki, and 44°C in Copenhagen, shattering century-old weather records and forcing governments across the region to declare national heat emergencies. The extreme weather event, attributed by meteorologists to a persistent high-pressure system combined with climate change effects, has overwhelmed electrical grids as air conditioning usage peaked, causing rolling blackouts in major cities and prompting utility companies to implement emergency power rationing measures. Swedish Prime Minister Erik Lundberg announced the opening of 200 emergency cooling centers in schools and community buildings, while Norwegian authorities have distributed over 500,000 free fans to elderly residents most vulnerable to heat-related illness. Local resident Maria Johansson from Stockholm expressed shock at the conditions, stating that her apartment reached 38°C despite closed blinds and fans, while Helsinki resident Pekka Virtanen reported that his usual morning jog became impossible due to the oppressive heat. Climate scientist Dr. Anna Bergström from the University of Copenhagen warned that this heatwave represents a dangerous preview of future summer conditions in the region, with computer models suggesting similar events could become annual occurrences by 2035. Emergency medical services have reported a 300% increase in heat-related hospitalizations, particularly among the elderly and those with pre-existing conditions, while agricultural experts estimate that the extreme temperatures have damaged 40% of regional crop yields, potentially leading to food shortages. The European Centre for Medium-Range Weather Forecasts predicts the heatwave will persist for at least another ten days, prompting calls from environmental activists for immediate action on carbon emissions and infrastructure adaptation to handle increasingly severe weather patterns.")
                .image("https://www.ecowatch.com/wp-content/uploads/2021/10/1706207636-origin.jpg")
                .reporter("David Brown")
                .dateTime(Instant.parse("2025-08-15T16:00:00Z"))
                .realVotes(18) // 19 - 1
                .fakeVotes(21) // 23 - 2
                .build();

        Comment c25_1 = Comment.builder()
                .username("Joseph Moore")
                .text("This sounds like media fear-mongering. Sure, it's hot, but I'm not buying the idea that it's the worst heatwave ever. They always say that.")
                .image(null)
                .time(Instant.parse("2025-08-15T19:00:00Z"))
                .vote(Vote.FAKE)
                .news(news25)
                .build();

        Comment c25_2 = Comment.builder()
                .username("Felix K.J.")
                .text("This is terrifying. With temperatures hitting these levels, it's clear that climate change is having a serious impact. We need to take action before it's too late.")
                .image("https://i.pinimg.com/736x/56/15/20/561520db0c3626e181b5768426efe263.jpg")
                .time(Instant.parse("2025-08-15T19:00:00Z"))
                .vote(Vote.REAL)
                .news(news25)
                .build();

        Comment c25_3 = Comment.builder()
                .username("Stella Carpenter")
                .text("A heatwave this extreme in Northern Europe? Seems like another exaggeration. How could they not have prepared for something like this?")
                .image(null)
                .time(Instant.parse("2025-08-15T18:00:00Z"))
                .vote(Vote.FAKE)
                .news(news25)
                .build();

        news25.getComments().add(c25_1);
        news25.getComments().add(c25_2);
        news25.getComments().add(c25_3);
        newsDao.save(news25);

        // --- ข่าวที่ 26: Battery Technology ---
        News news26 = News.builder()
                .topic("Breakthrough Battery Technology Promises 5-Minute Charge")
                .shortDetail("TechPower Labs unveiled solid-state batteries that charge electric vehicles in 5 minutes and last 1 million miles, potentially transforming the automotive industry.")
                .fullDetail("TechPower Labs announced a revolutionary breakthrough in solid-state battery technology that enables electric vehicles to achieve full charge in just 5 minutes while maintaining battery life for over 1 million miles of driving. The new PowerCell-X batteries utilize proprietary silicon nanowire anodes and ceramic electrolytes developed by Dr. Rachel Kim's team, achieving energy density 300% higher than current lithium-ion batteries while eliminating fire risks. CEO Mark Stevens demonstrated the technology by charging a Tesla Model S equivalent battery pack from 0% to 100% in 4 minutes and 47 seconds using their prototype ultra-fast charging station. Major automakers including Ford and General Motors have expressed significant interest, with Ford signing a preliminary $12 billion supply agreement for integration into their 2027 vehicle lineup. However, industry analyst Dr. Jennifer Martinez cautioned that mass production challenges and charging infrastructure costs could delay widespread implementation by 3-5 years. Energy Secretary Amanda Roberts praised the innovation as crucial for achieving 50% EV market share by 2030, while TechPower Labs plans to begin pilot production next year with initial capacity of 100,000 battery packs annually.")
                .image("https://carnewschina.com/wp-content/uploads/2025/04/768d4066b04f4dc7bc58c3e504f05f4d.png")
                .reporter("Zhang Wei")
                .dateTime(Instant.parse("2025-08-16T08:00:00Z"))
                .realVotes(1) // 2 - 1
                .fakeVotes(4) // 5 - 1
                .build();

        Comment c26_1 = Comment.builder()
                .username("Harper Turner")
                .text("I'm skeptical. This is probably just another overhyped announcement. Even if it's true, it will take years before it's available for everyday use.")
                .image("https://i.pinimg.com/736x/ec/ec/d1/ececd1a7e07d5dce6eeadd66d3b6faeb.jpg")
                .time(Instant.parse("2025-08-16T13:00:00Z"))
                .vote(Vote.FAKE)
                .news(news26)
                .build();

        Comment c26_2 = Comment.builder()
                .username("Oliver Harris")
                .text("This technology sounds promising. If it lives up to its claims, it could really transform how we think about electric cars and energy storage.")
                .image("https://image.made-in-china.com/251f0j00UYfGuGRPdEVh/made-in-china.jpg")
                .time(Instant.parse("2025-08-16T11:00:00Z"))
                .vote(Vote.REAL)
                .news(news26)
                .build();

        news26.getComments().add(c26_1);
        news26.getComments().add(c26_2);
        newsDao.save(news26);

        // --- ข่าวที่ 27: Landmark Restoration ---
        News news27 = News.builder()
                .topic("Famous Landmark to Undergo Restoration")
                .shortDetail("The Statue of Liberty faces a controversial $500 million restoration using modern materials, sparking debate about preserving authenticity versus structural integrity.")
                .fullDetail("The National Park Service announced a comprehensive $500 million restoration of the Statue of Liberty, involving replacement of deteriorating sections with advanced composite materials to combat climate change effects, sparking debate among preservation experts about historical authenticity versus structural stability. The five-year project, led by restoration architect Dr. Maria Gonzalez, will replace the statue's corroding iron framework with lightweight titanium alloy supports while maintaining the copper exterior through advanced patina-matching techniques. Park Service Director James Mitchell defended the approach, citing engineering reports showing the 139-year-old landmark faces structural failure within a decade due to rising sea levels, storms, and pollution. Art historian Professor Catherine Wells from Columbia University criticized the plan, arguing that replacing original materials fundamentally alters historical integrity and sets dangerous precedents for cultural artifacts. The French government has requested involvement through their Ministry of Culture, expressing concerns about maintaining symbolic authenticity while acknowledging preservation necessity. Tourism representatives estimate extended closure could cost New York City over $2 billion in lost revenue, while environmental groups praise climate adaptation features. Public polls show Americans evenly divided, with 48% supporting modern preservation techniques and 47% preferring traditional methods despite potentially reduced effectiveness against environmental threats.")
                .image("https://www.telegraph.co.uk/content/dam/Travel/2018/June/sphinx-GettyImages-643614006.jpg?imwidth=640")
                .reporter("Emily Davis")
                .dateTime(Instant.parse("2025-08-17T14:00:00Z"))
                .realVotes(5) // 5 - 0
                .fakeVotes(11) // 15 - 4
                .build();

        Comment c27_1 = Comment.builder()
                .username("Li Ting")
                .text("How convenient! Replacing the statue's original materials with modern ones? Feels like they're ruining history in the name of 'preservation.")
                .image(null)
                .time(Instant.parse("2025-08-17T15:00:00Z"))
                .vote(Vote.FAKE)
                .news(news27)
                .build();

        Comment c27_2 = Comment.builder()
                .username("Paulo Santos")
                .text("I find it hard to believe that a statue that's lasted for 139 years suddenly needs such drastic changes. This sounds like an excuse for corporate interests.")
                .image(null)
                .time(Instant.parse("2025-08-17T19:00:00Z"))
                .vote(Vote.FAKE)
                .news(news27)
                .build();

        Comment c27_3 = Comment.builder()
                .username("Suwanna n.")
                .text("I'm all for preserving history, but replacing original parts with new materials feels like changing what made the statue iconic. What's next, a complete overhaul?")
                .image(null)
                .time(Instant.parse("2025-08-17T15:00:00Z"))
                .vote(Vote.FAKE)
                .news(news27)
                .build();

        Comment c27_4 = Comment.builder()
                .username("João Silva")
                .text("This project sounds like a disaster. If it's not broken, why fix it? The Statue of Liberty has stood the test of time without all these modern fixes.")
                .image("https://i.pinimg.com/736x/18/f4/70/18f470dcba6c8eff8326060bc6215c50.jpg")
                .time(Instant.parse("2025-08-17T15:00:00Z"))
                .vote(Vote.FAKE)
                .news(news27)
                .build();

        news27.getComments().add(c27_1);
        news27.getComments().add(c27_2);
        news27.getComments().add(c27_3);
        news27.getComments().add(c27_4);
        newsDao.save(news27);

        // --- ข่าวที่ 28: Ancient City ---
        News news28 = News.builder()
                .topic("Archaeologists Unearth Ancient City")
                .shortDetail("Archaeologists discovered a 4,000-year-old Mesopotamian city in Iraq with advanced infrastructure and cuneiform tablets that could rewrite ancient history.")
                .fullDetail("An international team led by Dr. Sarah Johnson from Oxford University has uncovered a remarkably preserved 4,000-year-old Mesopotamian city near Baghdad, featuring sophisticated urban infrastructure including paved roads, drainage systems, and multi-story buildings that challenge assumptions about Bronze Age capabilities. The 300-acre site contains over 15,000 cuneiform tablets providing unprecedented insights into ancient trade networks, legal systems, and daily life during the Babylonian Empire. Initial translations by Professor Michael Chen reveal previously unknown trade routes connecting Mesopotamia to the Indus Valley, suggesting more extensive ancient global commerce than historians believed. Extraordinary artifacts include intricately carved cylinder seals, advanced bronze weapons, and ceramics with astronomical observations demonstrating sophisticated mathematical knowledge. Iraqi Minister of Culture Dr. Layla Al-Rashid called it potentially the most significant regional archaeological find since the 1920s, while UNESCO announced emergency funding for site protection. The research team, collaborating with Iraqi antiquities authorities, plans a permanent research station with digital preservation technology to create 3D models of structures and artifacts. Preliminary analysis suggests the city was suddenly abandoned around 1800 BCE, possibly due to climate change or invasion, with residents leaving behind valuable possessions and documents, making this a unique time capsule of ancient Mesopotamian civilization.")
                .image("https://idsb.tmgrup.com.tr/ly/uploads/images/2021/04/10/106838.jpg")
                .reporter("Yusuf Ibrahim")
                .dateTime(Instant.parse("2025-08-18T17:00:00Z"))
                .realVotes(26) // 27 - 1
                .fakeVotes(9) // 11 - 2
                .build();

        Comment c28_1 = Comment.builder()
                .username("Grace Williams")
                .text("Incredible! I've always thought there's so much more to ancient civilizations than what we've been taught. This find proves it.")
                .image(null)
                .time(Instant.parse("2025-08-18T18:00:00Z"))
                .vote(Vote.REAL)
                .news(news28)
                .build();

        Comment c28_2 = Comment.builder()
                .username("Daniel T.")
                .text("This sounds like a publicity stunt. How could they have missed such an important site for so long?")
                .image(null)
                .time(Instant.parse("2025-08-18T20:00:00Z"))
                .vote(Vote.FAKE)
                .news(news28)
                .build();

        Comment c28_3 = Comment.builder()
                .username("Mason Lewis")
                .text("I'm not buying this. Every time they 'unearth' something amazing, it ends up being exaggerated or debunked later.")
                .image(null)
                .time(Instant.parse("2025-08-18T18:00:00Z"))
                .vote(Vote.FAKE)
                .news(news28)
                .build();

        news28.getComments().add(c28_1);
        news28.getComments().add(c28_2);
        news28.getComments().add(c28_3);
        newsDao.save(news28);

        // --- ข่าวที่ 29: Self-Driving Taxis ---
        News news29 = News.builder()
                .topic("Self-Driving Taxis Begin Service in Capital")
                .shortDetail("Autonomous taxi service RoboRide launched in Washington D.C. with 200 driverless vehicles, marking the first fully automated taxi fleet in a major U.S. capital city.")
                .fullDetail("RoboRide, the San Francisco-based autonomous vehicle company, officially launched the first fully driverless taxi service in Washington D.C. yesterday with a fleet of 200 electric vehicles operating 24/7 across downtown and surrounding neighborhoods, representing a historic milestone in urban transportation and sparking both excitement and safety concerns among residents and officials. The sleek white vehicles, equipped with advanced LiDAR sensors, cameras, and AI navigation systems developed over eight years of testing, can be summoned through a smartphone app and operate without human safety drivers for the first time in the nation's capital. D.C. Mayor Patricia Williams celebrated the launch at a ribbon-cutting ceremony, emphasizing the potential for reduced traffic congestion and improved mobility for disabled residents, while acknowledging ongoing discussions with labor unions representing traditional taxi drivers who fear job displacement. The service initially covers a 25-square-mile area including Capitol Hill, downtown, and Georgetown, with plans to expand citywide by 2026 pending regulatory approval and public acceptance. However, transportation safety advocate Dr. Robert Martinez from the Highway Safety Institute expressed concerns about the vehicles' ability to handle unpredictable situations like construction zones and emergency vehicles, citing recent incidents in other test cities. Federal regulators from the National Highway Traffic Safety Administration are closely monitoring the deployment, with Administrator Jennifer Park stating that comprehensive data collection will inform future autonomous vehicle policies nationwide. Early user reviews have been mixed, with commuters praising the smooth rides and competitive pricing at $0.85 per mile, while others report confusion about pickup locations and concerns about riding in vehicles without human operators during late-night hours.")
                .image("https://eu-images.contentstack.com/v3/assets/blt31d6b0704ba96e9d/blt771dc5c2e03d8c80/667df77fe96baf6a6960ff58/pic_5.jpg?width=1280&auto=webp&quality=80&format=jpg&disable=upscale")
                .reporter("Isabelle Robert")
                .dateTime(Instant.parse("2025-08-19T20:00:00Z"))
                .realVotes(1) // 2 - 1
                .fakeVotes(17) // 19 - 2
                .build();

        Comment c29_1 = Comment.builder()
                .username("Hubleaw no five")
                .text("I'm skeptical about this. Driverless taxis have been announced many times before and failed to take off. Let's see how long this lasts.")
                .image("https://i.pinimg.com/736x/68/00/48/68004889c41f09fa8fc06bdfc37b03ef.jpg")
                .time(Instant.parse("2025-08-20T00:00:00Z"))
                .vote(Vote.FAKE)
                .news(news29)
                .build();

        Comment c29_2 = Comment.builder()
                .username("Maria Bianchi")
                .text("I've been waiting for this for years! It's great to see driverless taxis finally becoming a reality in a major city.")
                .image(null)
                .time(Instant.parse("2025-08-19T22:00:00Z"))
                .vote(Vote.REAL)
                .news(news29)
                .build();

        Comment c29_3 = Comment.builder()
                .username("Marta Ruiz")
                .text("Driverless cars in a major city? Feels like they're jumping the gun. How can we trust this technology when it's still so new?")
                .image(null)
                .time(Instant.parse("2025-08-19T22:00:00Z"))
                .vote(Vote.FAKE)
                .news(news29)
                .build();

        news29.getComments().add(c29_1);
        news29.getComments().add(c29_2);
        news29.getComments().add(c29_3);
        newsDao.save(news29);

        // --- ข่าวที่ 30: Rare Animal ---
        News news30 = News.builder()
                .topic("Rare Animal Species Spotted After Decades")
                .shortDetail("The Javan elephant, believed extinct for 40 years, was photographed by camera traps in Indonesia's remote forests, offering new hope for conservation efforts.")
                .fullDetail("Wildlife researchers from the Indonesian Institute of Sciences have confirmed the existence of a small population of Javan elephants in the remote forests of West Java, marking the first verified sighting of the species in over four decades after it was presumed extinct due to deforestation and human encroachment. The breakthrough discovery came through motion-activated camera traps installed by Dr. Sari Indrawati's conservation team, which captured clear footage of at least six individuals including a mother with her calf foraging near a hidden water source in Gunung Halimun-Salak National Park. The Javan elephant, a subspecies distinct from Sumatran and Asian elephants, was last officially documented in 1982 before habitat destruction eliminated their known populations, making this rediscovery one of the most significant conservation stories of the decade. Indonesian Environment Minister Dr. Ahmad Sutrisno announced immediate emergency protection measures for the estimated 12-15 remaining individuals, including expanded patrol units and strict access restrictions to their habitat area. International elephant expert Dr. Lisa Thompson from the World Wildlife Fund called the discovery miraculous but emphasized the species remains critically endangered, requiring urgent genetic diversity studies and potential breeding programs to ensure long-term survival. Local communities in nearby villages reported occasional signs of large animals over the years but dismissed them as Sumatran elephants migrating from other regions, until DNA analysis of dung samples confirmed the unique genetic markers of the Javan subspecies. The Indonesian government has pledged $5 million in emergency conservation funding while collaborating with international organizations to develop a comprehensive species recovery plan, though experts warn that habitat protection and human-wildlife conflict mitigation will be crucial for preventing another near-extinction event.")
                .image("https://assets.globalwildlife.org/m/7550306bdae0c311/webimage-Rediscovered-Fernandina-Giant-Tortoise.jpg")
                .reporter("Michael Smith")
                .dateTime(Instant.parse("2025-08-20T20:00:00Z"))
                .realVotes(6) // 6 - 0
                .fakeVotes(2) // 4 - 2
                .build();

        Comment c30_1 = Comment.builder()
                .username("Peter Weber")
                .text("This sounds too good to be true. The Javan elephant has been gone for decades, and now it's suddenly 'discovered'? Doubtful.")
                .image("https://i.pinimg.com/736x/34/64/ad/3464ad1c33c983b87d66f14b092f11ee.jpg")
                .time(Instant.parse("2025-08-20T21:00:00Z"))
                .vote(Vote.FAKE)
                .news(news30)
                .build();

        Comment c30_2 = Comment.builder()
                .username("Sato Shota")
                .text("This sounds too convenient. How did they miss these elephants all this time? I doubt this will last.")
                .image(null)
                .time(Instant.parse("2025-08-21T00:00:00Z"))
                .vote(Vote.FAKE)
                .news(news30)
                .build();

        news30.getComments().add(c30_1);
        news30.getComments().add(c30_2);
        newsDao.save(news30);

        // --- ข่าวที่ 31: Moo Deng ---
        News news31 = News.builder()
                .topic("Here We Go! 'Moo Deng' Pygmy Hippo Meme Valued at Nearly 150 Million USD")
                .shortDetail("Moo Deng, a viral pygmy hippo from Khao Kiew Zoo, boosted zoo visits and appeared in ads. No royalties have been paid to the zoo. @hippo_cto recently donated 5 million THB, raising the coin's market cap to 143 million USD.")
                .fullDetail("Moo Deng, a 2-month-old pygmy hippo from Khao Kiew Zoo, became a viral sensation worldwide after its cute pictures and videos were shared on the Khamoo & The Gang Facebook page. The page originally posted adorable moments of animals at the zoo, such as capybaras and older hippos, which led to Moo Deng gaining widespread attention and helping the zoo attract over 100,000 visitors per month. This popularity turned Moo Deng into a social media superstar, beloved by fans of all ages. Although Moo Deng’s fame led to brands using its image in various ads and merchandise, such as t-shirts, bags, and billboards, there has been no news about any brand paying royalties to Khao Kiew Zoo. This raises questions about how the profits from this fame are shared. Moo Deng is not only a cute animal but has also become a valuable asset with significant economic impact. Recently, @hippo_cto, a cryptocurrency organization, donated 5 million THB to Khao Kiew Zoo, which boosted the market cap of the associated coin to 143 million USD. This highlights the financial worth of Moo Deng as not just a viral sensation but also a key player in the digital economy. However, questions about how copyrights and benefits from this fame are managed remain to be addressed, ensuring that Moo Deng's caregivers and Khao Kiew Zoo receive fair compensation for the viral success they’ve helped create.")
                .image("https://files-world.thaipbs.or.th/16_9_d662e87653.png")
                .reporter("Nina Wong")
                .dateTime(Instant.parse("2025-08-09T11:00:00Z"))
                .realVotes(44) // 46 - 2
                .fakeVotes(44) // 46 - 2
                .build();

        Comment c31_1 = Comment.builder()
                .username("Kaem W.")
                .text("It's so cute.")
                .image("https://s.isanook.com/jo/0/ud/494/2474469/m11.jpg?ip/resize/w728/q80/jpg")
                .time(Instant.parse("2025-08-09T13:00:00Z"))
                .vote(Vote.REAL)
                .news(news31)
                .build();

        Comment c31_2 = Comment.builder()
                .username("Mia Choi")
                .text("Seems like Moo Deng's fame is being used for profit without fair compensation to the zoo. Kinda sketchy.")
                .image(null)
                .time(Instant.parse("2025-08-09T14:00:00Z"))
                .vote(Vote.FAKE)
                .news(news31)
                .build();

        Comment c31_3 = Comment.builder()
                .username("Liam Phan")
                .text("Moo Deng is a star! But the zoo should definitely get paid for the brand deals.")
                .image("https://www.mp-uni.com/th/wp-content/uploads/sites/7/2024/09/hippo-moo-deng-01.jpg")
                .time(Instant.parse("2025-08-09T15:00:00Z"))
                .vote(Vote.REAL)
                .news(news31)
                .build();

        Comment c31_4 = Comment.builder()
                .username("Sophie Li")
                .text("It's just a hippo, though.")
                .image(null)
                .time(Instant.parse("2025-08-09T16:00:00Z"))
                .vote(Vote.FAKE)
                .news(news31)
                .build();

        news31.getComments().add(c31_1);
        news31.getComments().add(c31_2);
        news31.getComments().add(c31_3);
        news31.getComments().add(c31_4);
        newsDao.save(news31);
    }
}
