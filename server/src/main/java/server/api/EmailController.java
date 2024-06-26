package server.api;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.EmailSenderService;

@RestController
@RequestMapping("/api/mail")
public class EmailController {
    private final EmailSenderService emailSender;

    @Autowired
    public EmailController(EmailSenderService emailSender) {
        this.emailSender = emailSender;
    }

    @PostMapping(path = "/custom")
    public ResponseEntity<Void> sendEmail(@RequestBody String contents) {
        JsonObject body = JsonParser.parseString(contents).getAsJsonObject();

        emailSender.sendEmail(body.get("senderEmail").toString().replaceAll("\"", ""),
                body.get("toEmail").toString(),
                body.get("password").toString().replaceAll("\"", ""),
                body.get("host").toString().replaceAll("\"", ""),
                Integer.parseInt(body.get("port").toString().replaceAll("\"", "")),
                Boolean.parseBoolean(body.get("smtpAuth").toString().replaceAll("\"", "")),
                Boolean.parseBoolean(body.get("startTls").toString().replaceAll("\"", "")),
                body.get("body").toString().replaceAll("\"", ""),
                body.get("subject").toString().replaceAll("\"", ""));


        return ResponseEntity.ok().build();

    }
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Void> sendEmailInvitation(@RequestBody String contents) {
        JsonObject body = JsonParser.parseString(contents).getAsJsonObject();

        emailSender.sendInvitationEmail(body.get("senderEmail").toString().replaceAll("\"", ""),
                body.get("toEmail").toString(),
                body.get("inviteCode").toString(),
                body.get("creator").toString(),
        body.get("password").toString().replaceAll("\"", ""),
                body.get("host").toString().replaceAll("\"", ""),
                Integer.parseInt(body.get("port").toString().replaceAll("\"", "")),
                Boolean.parseBoolean(body.get("smtpAuth").toString().replaceAll("\"", "")),
                Boolean.parseBoolean(body.get("startTls").toString().replaceAll("\"", "")));

        return ResponseEntity.ok().build();
    }


}
