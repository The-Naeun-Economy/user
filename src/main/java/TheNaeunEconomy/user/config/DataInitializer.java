package TheNaeunEconomy.user.config;

import TheNaeunEconomy.user.Repository.UserRepository;
import TheNaeunEconomy.user.domain.User;
import TheNaeunEconomy.user.service.request.AddUserRequest;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final List<String> NAMES = List.of(
            "김민준", "이서연", "박지민", "최준호", "정다은", "조유진", "장하늘", "강지수", "오은영", "임서진"
    );

    private static final List<String> ADJECTIVES = List.of(
            "빠른", "똑똑한", "예쁜", "조용한", "큰", "작은", "친절한", "용감한", "이상한", "귀여운"
    );

    private static final List<String> FISH = List.of(
            "고등어", "참치", "광어", "연어", "농어", "가자미", "상어", "황새치", "메기", "복어"
    );

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Random random = new Random();

        for (int i = 1; i <= 100; i++) {
            AddUserRequest request = new AddUserRequest();
            request.setEmail("example" + i + "@gmail.com");
            request.setPassword("password123");
            request.setConfirmPassword("password123");
            request.setName(NAMES.get(random.nextInt(NAMES.size())));
            request.setNickname(ADJECTIVES.get(random.nextInt(ADJECTIVES.size())) + " " + FISH.get(random.nextInt(FISH.size())));
            request.setGender(i % 2 == 0 ? "M" : "F");

            int year = 1990 + random.nextInt(11);
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28);

            request.setBirthDate(LocalDate.of(year, month, day));

            saveUser(request);
        }

        log.info("100개의 더미 데이터가 성공적으로 삽입되었습니다.");
    }

    private void saveUser(AddUserRequest request) {
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getNickname(),
                request.getGender(),
                request.getBirthDate()
        );

        userRepository.save(user);
    }

    @PreDestroy
    public void cleanUp() {
        userRepository.deleteAll();
        log.info("애플리케이션 종료 시 모든 더미 데이터가 삭제되었습니다.");
    }
}