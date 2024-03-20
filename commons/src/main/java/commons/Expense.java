package commons;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "expense_id")
    private UUID id;
    private String title;
    private double amount;
    private LocalDateTime date;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    @JsonBackReference ("participant-expenses")
    private Participant paidBy;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonBackReference ("event-expenses")
    private Event event;
    @ManyToMany
    private List<Tag> tags;

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", paidBy=" + paidBy +
                ", event=" + event +
                ", tags=" + tags +
                ", debts=" + debts +
                '}';
    }

    @OneToMany(mappedBy = "expense", orphanRemoval = true)
    @JsonManagedReference ("expense-debts")
    private List<Debt> debts;

    public Expense() {
        // For JPA
    }

    public Expense(String title, double amount, LocalDateTime date,
                   Participant paidBy, Event event, List<Debt> debts, List<Tag> tags) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.paidBy = paidBy;
        this.event = event;
        this.debts = debts;
        this.tags = tags;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Participant getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Participant paidBy) {
        this.paidBy = paidBy;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Debt> getDebts() {
        return debts;
    }

    public void setDebts(List<Debt> debts) {
        this.debts = debts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Expense expense = (Expense) o;
        return Double.compare(amount, expense.amount) == 0
                && Objects.equals(id, expense.id)
                && Objects.equals(title, expense.title)
                && Objects.equals(date, expense.date)
                && Objects.equals(paidBy, expense.paidBy)
                && Objects.equals(event, expense.event)
                && Objects.equals(debts, expense.debts)
                && Objects.equals(tags, expense.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, amount, date, paidBy, event, debts, tags);
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
