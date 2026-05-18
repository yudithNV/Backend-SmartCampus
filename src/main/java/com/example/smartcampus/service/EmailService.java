package com.example.smartcampus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@ucb.edu.bo}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

   
    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("UCB SmartCampus — Restablecer tu contraseña");

            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            String html = buildResetEmailHtml(fullName, resetUrl);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("[EmailService] Reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("[EmailService] Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildResetEmailHtml(String fullName, String resetUrl) {

        String firstName = (fullName != null && !fullName.isBlank())
                ? fullName.split(" ")[0]
                : "Estimado usuario";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Restablecer Contraseña — UCB SmartCampus</title>
            </head>

            <body style="margin:0;padding:0;background:#f0f2f5;
                         font-family:'Segoe UI',Arial,sans-serif;">

              <table width="100%%" cellpadding="0" cellspacing="0"
                     style="background:#f0f2f5;padding:40px 0;">

                <tr>
                  <td align="center">

                    <table width="560" cellpadding="0" cellspacing="0"
                           style="background:#ffffff;
                                  border-radius:16px;
                                  overflow:hidden;
                                  box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                      <!-- HEADER -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#1a3a52,#1e4d6b);
                                   padding:36px 40px;
                                   text-align:center;">

                          <!-- ICON -->
                          <div style="display:inline-block;
                                      background:#FFD200;
                                      border-radius:14px;
                                      width:56px;
                                      height:56px;
                                      line-height:56px;
                                      text-align:center;
                                      margin-bottom:16px;">

                            <svg width="28" height="28"
                                 viewBox="0 0 24 24"
                                 fill="none"
                                 xmlns="http://www.w3.org/2000/svg"
                                 style="vertical-align:middle;margin-top:14px;">

                              <path d="M12 3L1 9L12 15L21 10.09V17H23V9L12 3Z"
                                    fill="#1a3a52"/>

                              <path d="M5 12V16C5 17.1 8.13 19 12 19C15.87 19 19 17.1 19 16V12L12 16L5 12Z"
                                    fill="#1a3a52"/>

                            </svg>
                          </div>

                          <h1 style="margin:0;
                                     color:#ffffff;
                                     font-size:22px;
                                     font-weight:800;
                                     letter-spacing:-0.5px;">

                            UCB SmartCampus
                          </h1>

                          <p style="margin:6px 0 0;
                                    color:rgba(255,255,255,0.7);
                                    font-size:13px;">

                            Universidad Católica Boliviana · La Paz
                          </p>

                        </td>
                      </tr>

                      <!-- BODY -->
                      <tr>
                        <td style="padding:40px;">

                          <h2 style="margin:0 0 12px;
                                     color:#1a3a52;
                                     font-size:20px;
                                     font-weight:700;">

                            Hola, %s
                          </h2>

                          <p style="margin:0 0 20px;
                                    color:#475569;
                                    font-size:15px;
                                    line-height:1.7;">

                            Recibimos una solicitud para
                            <strong>restablecer tu contraseña</strong>
                            en UCB SmartCampus.

                            Si fuiste tú, haz clic en el botón de abajo.
                          </p>

                          <!-- BUTTON -->
                          <div style="text-align:center;margin:32px 0;">

                            <a href="%s"
                               style="display:inline-block;
                                      background:#FFD200;
                                      color:#0c1a27;
                                      text-decoration:none;
                                      font-weight:800;
                                      font-size:15px;
                                      padding:14px 36px;
                                      border-radius:10px;
                                      box-shadow:0 4px 16px rgba(255,210,0,0.4);">

                              <span style="vertical-align:middle;
                                           margin-right:8px;">

                                <svg width="16" height="16"
                                     viewBox="0 0 24 24"
                                     fill="none"
                                     xmlns="http://www.w3.org/2000/svg"
                                     style="vertical-align:middle;">

                                  <path d="M14 3C10.69 3 8 5.69 8 9C8 9.72 8.13 10.41 8.36 11.05L2 17.41V21H5.59L7 19.59V18H8.59L10 16.59V15H11.59L12.95 13.64C13.59 13.87 14.28 14 15 14C18.31 14 21 11.31 21 8C21 4.69 18.31 3 15 3H14Z"
                                        fill="#0c1a27"/>

                                </svg>
                              </span>

                              Restablecer mi contraseña
                            </a>
                          </div>

                          <!-- WARNING -->
                          <div style="background:#fffbeb;
                                      border:1px solid #fde68a;
                                      border-left:4px solid #f59e0b;
                                      border-radius:10px;
                                      padding:16px 20px;
                                      margin:24px 0;">

                            <p style="margin:0;
                                      color:#92400e;
                                      font-size:13px;
                                      line-height:1.6;">

                              <span style="vertical-align:middle;
                                           margin-right:6px;">

                                <svg width="14" height="14"
                                     viewBox="0 0 24 24"
                                     fill="none"
                                     xmlns="http://www.w3.org/2000/svg"
                                     style="vertical-align:middle;">

                                  <circle cx="12"
                                          cy="12"
                                          r="10"
                                          stroke="#92400e"
                                          stroke-width="2"/>

                                  <path d="M12 7V12L15 15"
                                        stroke="#92400e"
                                        stroke-width="2"
                                        stroke-linecap="round"/>

                                </svg>
                              </span>

                              <strong>Este enlace expira en 30 minutos.</strong>
                              <br/>

                              Si no solicitaste este cambio,
                              puedes ignorar este correo.
                              Tu contraseña actual permanecerá igual.
                            </p>
                          </div>

                          <!-- MANUAL URL -->
                          <p style="margin:20px 0 0;
                                    color:#94a3b8;
                                    font-size:12px;
                                    text-align:center;
                                    word-break:break-all;">

                            O copia este enlace en tu navegador:
                            <br/>

                            <span style="color:#1a3a52;">
                              %s
                            </span>
                          </p>

                        </td>
                      </tr>

                      <!-- FOOTER -->
                      <tr>
                        <td style="background:#f8fafc;
                                   padding:20px 40px;
                                   border-top:1px solid #e2e8f0;
                                   text-align:center;">

                          <p style="margin:0;
                                    color:#94a3b8;
                                    font-size:12px;">

                            © 2026 Universidad Católica Boliviana — UCB SmartCampus
                            <br/>
                            Este correo fue enviado automáticamente,
                            por favor no respondas.
                          </p>

                        </td>
                      </tr>

                    </table>

                  </td>
                </tr>

              </table>

            </body>
            </html>
            """.formatted(firstName, resetUrl, resetUrl);
    }
}