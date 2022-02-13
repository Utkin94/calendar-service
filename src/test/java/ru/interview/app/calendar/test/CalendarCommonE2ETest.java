package ru.interview.app.calendar.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.interview.app.calendar.entity.Meeting;
import ru.interview.app.calendar.entity.MeetingMember;
import ru.interview.app.calendar.entity.MeetingStatus;
import ru.interview.app.calendar.entity.User;
import ru.interview.app.calendar.repository.MeetingRepository;
import ru.interview.app.calendar.repository.UserRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext
public class CalendarCommonE2ETest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MeetingRepository meetingRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    protected <T> T executeWithTransaction(TransactionCallback<T> objectTransactionCallback) {
        return transactionTemplate.execute(objectTransactionCallback);
    }

    protected void executeWithTransaction(Runnable task) {
        executeWithTransaction(t -> {
            task.run();
            return t;
        });
    }

    protected Meeting createMeeting(User creator, List<User> memberList, Consumer<Meeting> consumer) {
        var meeting = new Meeting();
        meeting.setCreator(creator);
        meeting.setTitle("title");
        meeting.setStartTime(ZonedDateTime.now().plusHours(1));
        meeting.setEndTime(ZonedDateTime.now().plusHours(2));
        meeting.setStatus(MeetingStatus.OPEN);

        var members = memberList.stream()
                .map(memberUser -> new MeetingMember()
                        .setUser(memberUser)
                        .setMeeting(meeting)
                )
                .collect(Collectors.toList());
        meeting.setMembers(members);

        if (consumer != null) {
            consumer.accept(meeting);
        }

        return meetingRepository.save(meeting);
    }

    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("calendar-test");

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDB::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDB::getUsername);
        registry.add("spring.datasource.password", postgresDB::getPassword);
    }
}
