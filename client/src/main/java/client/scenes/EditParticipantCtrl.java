package client.scenes;

import client.services.I18NService;
import client.services.NotificationService;
import client.utils.ServerUtils;
import commons.Debt;
import commons.Event;
import commons.Expense;
import commons.Participant;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditParticipantCtrl implements Initializable {
    private final MainCtrl mainCtrl;

    private final ServerUtils server;

    private final NotificationService notificationHelper;


    @FXML
    private TextField email;

    @FXML
    private TextField name;

    @FXML
    private TextField iban;

    @FXML
    private TextField bic;

    @FXML
    public Button cancelButton2;
    @FXML
    public Button remind;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Label nameLabel2;
    @FXML
    public Label editParticipant;
    private Event event;
    private Participant p;

    private final I18NService i18n;

    public Event getEvent() {
        return event;
    }

    private boolean validEmail;

    @Inject
    public EditParticipantCtrl(MainCtrl mainCtrl, Event event, Participant p, ServerUtils server, NotificationService notificationService, I18NService i18n) {
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.p = p;
        this.server = server;
        this.notificationHelper = notificationService;
        this.i18n = i18n;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ImageView bin=new ImageView(new Image("client/icons/bin-red.png"));
        bin.setPreserveRatio(true);
        bin.setFitHeight(15);

        deleteButton.setGraphic(bin);
        i18n.update(cancelButton2);
        i18n.update(editButton);
        i18n.update(deleteButton);
        i18n.update(nameLabel2);
        i18n.update(editParticipant);
        i18n.update(remind);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setParticipant(Participant p) {
        this.p = p;
    }

    public void editParticipantButton() {
        if (name.getText() != null && name.getText().isEmpty()) {
            String warningMessage = i18n.get("participant.add.error");
            if (name.getText().isEmpty()){
                warningMessage += i18n.get("participant.add.error.name") + " ";
            }
            warningMessage += ")";
            notificationHelper.showError(i18n.get("general.warning"), warningMessage);
            return;
        }
        if (email.getText() != null && !email.getText().isBlank()&&!email.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            String warningMessage = i18n.get("participant.add.error.message.email");
            notificationHelper.showError(i18n.get("general.warning"), warningMessage);
            return;
        }
        if (iban.getText() != null && !iban.getText().isBlank()&&iban.getText().length() != 34){
            String warningMessage = i18n.get("participant.add.error.message.iban");
            notificationHelper.showError(i18n.get("general.warning"), warningMessage);
            return;
        }
        p.setName(name.getText());
        p.setEmail(email.getText());
        p.setIban(iban.getText());
        p.setBic(bic.getText());
        server.updateParticipant(
                event,
                p
        );
        this.event.addParticipant(p);
        server.send("/app/events", this.event);
        returnToOverview();
    }

    public void remind() {
        if (!validEmail) {
            return;
        }
        double invoice = calculateOutgoing(p);
        String subject = "Hello "
                + p.getName()
                + "! You have an oustanding invoice of "
                + invoice
                + " "
                + mainCtrl.getUser().getPrefferedCurrency().toString()
                + " for the event "
                + event.getName()
                + ". Please make sure to settle your debts within the app with the other participants. Best wishes, Splitty (OOPP Team 35)";
        server.sendEmail(p.getEmail(), "Payment Reminder", subject);

        notificationHelper.informUser(i18n.get("reminder.title"), i18n.get("reminder.message"), i18n.get("reminder.header"));
    }

    public double calculateOutgoing(Participant p){
        double outgoing = 0;
        for (Expense e : event.getExpenses()){
            if (e.getPaidBy().getId().equals(p.getId())){
                continue;
            }
            List<Debt> debts = e.getDebts();
            for (Debt d : debts) {
                if (d.isPaid()) {
                    continue;
                }
                if (d.getParticipant().getId().equals(p.getId())){
                    outgoing+=server.convert(d.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
                }
            }
        }
        return outgoing;
    }

    public void returnToOverview() {
        mainCtrl.showEventOverviewScene(event);
    }

    public void removeParticipant() {
        if (!notificationHelper.showConfirmation(i18n.get("participant.remove.notification_title"), i18n.get("participant.remove.notification"))) {
            return;
        }
        try {
            server.removeParticipant(event, p);
            server.send("/app/events", this.event);
        } catch (WebApplicationException e) {
            String warningMessage = i18n.get("participant.remove.error");
            notificationHelper.showError(i18n.get("general.warning"), warningMessage);

        } finally {
            returnToOverview();
        }
    }

    public void refresh() {
        this.email.setText(p.getEmail());
        this.name.setText(p.getName());
        this.iban.setText(p.getIban());
        this.bic.setText(p.getBic());
        this.validEmail = true;
        remind.setStyle("");
        if (server.getMailConfig() == null || p.getEmail() == null || p.getEmail().isEmpty()) {
            this.validEmail = false;
            remind.setStyle("-fx-background-color: #808080");
        }
    }
}
