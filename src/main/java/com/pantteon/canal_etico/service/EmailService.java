package com.pantteon.canal_etico.service;

import com.pantteon.canal_etico.model.Denuncia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenders;

    @Value("${app.notifications.denuncias-recipient}")
    private String destinatarioDenuncias;

    public void enviarNuevaDenuncia(Denuncia denuncia) {

        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (mailSender == null) {
            log.warn("Email notifications are unavailable because no mail provider is configured");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(destinatarioDenuncias);

        message.setSubject(
                "Nueva denuncia recibida - " +
                        denuncia.getCodigoUnico()
        );

        String tipo = switch (denuncia.getTipoDenuncia()) {
            case GENERAL -> "Denuncia general";
            case LEY_KARIN -> "Ley Karin";
        };

        message.setText(
                """
                Se ha recibido una nueva denuncia en el Canal Ético.

                Empresa: %s
                Código: %s
                Tipo: %s
                Delito: %s
                Estado: %s
                Fecha de registro: %s

                Ingrese al panel administrativo para revisar los antecedentes.

                Este es un mensaje automático.
                """
                        .formatted(
                                denuncia.getEmpresa().getNombre(),
                                denuncia.getCodigoUnico(),
                                tipo,
                                denuncia.getDelitoTipificacion(),
                                denuncia.getEstado(),
                                denuncia.getCreatedAt()
                        )
        );

        mailSender.send(message);

        log.info(
                "Correo de denuncia {} enviado correctamente a {}",
                denuncia.getCodigoUnico(),
                destinatarioDenuncias
        );
    }
}



/*
@Service
@RequiredArgsConstructor
public class EmailService {

    //private final JavaMailSender mailSender;

    //@Value("${app.notifications.denuncias-recipient}")
    private String destinatarioDenuncias;

    public void enviarNuevaDenuncia(Denuncia denuncia) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatarioDenuncias);
        message.setSubject("Nueva denuncia recibida");
        message.setText("Empresa: " + denuncia.getEmpresa().getNombre()
                + "\nCódigo denuncia: " + denuncia.getCodigoUnico()
                + "\nTipo: " + denuncia.getTipo()
                + "\nFecha: " + denuncia.getCreatedAt()
                + "\nDescripción: " + denuncia.getDescripcion());
        mailSender.send(message);


        System.out.println("Simulando correo");
    }
}
*/
