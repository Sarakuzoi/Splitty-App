package server.api;

import commons.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import commons.Participant;

public class ParticipantControllerTest {
    private TestParticipantRepository repo;
    private ParticipantController participantController;
    private Participant testParticipant1;
    private Participant testParticipant2;
    private Event testEvent;

    @BeforeEach
    public void setup() {
        repo = new TestParticipantRepository();
        participantController = new ParticipantController(repo);
        testEvent = new Event("eventA");
        testParticipant1 = new Participant();
        testParticipant2 = new Participant("Jason", testEvent, "BE123456789", "Jason@smith.com", "12472014");
    }

    @Test
    public void testAddNull(){
        var requestResult1 = participantController.add(testParticipant1);
        var requestResult2 = participantController.add(testParticipant2);

        assertEquals(BAD_REQUEST, requestResult1.getStatusCode());
        assertEquals(BAD_REQUEST, requestResult2.getStatusCode());
    }

}
