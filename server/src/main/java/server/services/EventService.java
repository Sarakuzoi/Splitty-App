package server.services;
import commons.EventLongPollingWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import server.database.EventRepository;
import commons.Event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.List;
@Service
public class EventService {

    private final EventRepository eventRepository;

    /**
     * This is a hashmap that stores all the listeners for new events.
     * The key is a randomly created object to identify the listener.
     * The value is a consumer that accepts an event, it is the deferredresult that will be accepted with a new event.
     */
    private Map<Object, Consumer<EventLongPollingWrapper>> listeners = new ConcurrentHashMap<>();

    @Autowired
    public EventService(EventRepository eventRepository){
        this.eventRepository=eventRepository;
    }

    public void newEventLastActivity(UUID eventID) {
        Event event = eventRepository.findById(eventID).orElse(null);
        if (event != null) {
            event.setLastActivityTime(Instant.now());
            eventRepository.save(event);
            EventLongPollingWrapper wrapper=new EventLongPollingWrapper("PUT", event);
            System.out.println(wrapper);
            accept(wrapper);
        }
    }

    public List<Event> getAll(){
        return eventRepository.findAll();
    }

    public Event get(UUID id){
        return eventRepository.findById(id).orElse(null);
    }
    public Event joinEvent(String inviteCode){
        return eventRepository.getEventForInviteCode(inviteCode);
    }

    public Event add(Event e){
        Event saved = eventRepository.save(e);
        EventLongPollingWrapper wrapper=new EventLongPollingWrapper("POST", saved);
        this.accept(wrapper);
        return saved;
    }

    public boolean delete(UUID id){
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            EventLongPollingWrapper wrapper=new EventLongPollingWrapper("DELETE", new Event(id));
            this.accept(wrapper);
            return true;
        }
        return false;
    }

    public Event update(UUID id, Event event){
        if (eventRepository.existsById(id)) {
            event.setId(id);
            event.setLastActivityTime(Instant.now()); //update last activity time
            Event saved = eventRepository.save(event);
            EventLongPollingWrapper wrapper = new EventLongPollingWrapper("PUT", event);
            this.accept(wrapper);
            return saved;
        }
        return null;
    }

    public void accept(EventLongPollingWrapper wrapper){
        System.out.println("accepting!!!");
        System.out.println(wrapper);
        System.out.println("!!!!!");
        listeners.values().forEach(listener -> listener.accept(wrapper)); // notify all listeners of a new event
        System.out.println("Accepted");
    }

    public void subscribe(DeferredResult<ResponseEntity<EventLongPollingWrapper>> deferredResult){
        Object key = new Object();
        listeners.put(key, wrapper -> {
            System.out.println(wrapper);
            System.out.println(wrapper.getAction());
            deferredResult.setResult(ResponseEntity.ok(wrapper));
        });
        deferredResult.onCompletion(() -> {
            listeners.remove(key);
        });
        deferredResult.onError((Throwable t) -> {
            System.out.println("Error deferredResult : " + t.getMessage());
            listeners.remove(key);
        });
        deferredResult.onTimeout(() -> {
            deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
        });
    }

    public boolean existsById(UUID id){
        return eventRepository.existsById(id);
    }
}
