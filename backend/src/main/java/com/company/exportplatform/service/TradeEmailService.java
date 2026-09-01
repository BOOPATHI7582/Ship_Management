package com.company.exportplatform.service;

import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.NegotiationMessage;
import com.company.exportplatform.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Trade-workflow emails driven by client acceptance events.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeEmailService {

    private final MailService mailService;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Sent when the client accepts an offer on a negotiation: a quotation /
     * agreed-terms summary so the client has a record of what was accepted.
     */
    public void sendAcceptedTermsEmail(Enquiry enquiry, NegotiationMessage message) {
        User user = enquiry.getClient() != null ? enquiry.getClient().getUser() : null;
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        String currency = enquiry.getCurrency();
        BigDecimal price = message.getOfferPrice();
        String html = buildAcceptedTermsHtml(enquiry, user, currency, price, message.getMessage());
        mailService.sendHtml(user.getEmail(),
                "Quotation accepted - " + enquiry.getReferenceNo(), html);
        log.info("Emailed accepted-terms quotation summary for enquiry {} to {}",
                enquiry.getReferenceNo(), user.getEmail());
    }

    private String buildAcceptedTermsHtml(Enquiry enquiry, User user,
                                          String currency, BigDecimal price, String messageText) {
        String priceBlock = price != null
                ? "<p style=\"color:#334155\">You accepted the quoted price of:" 
                        + "</p><div style=\"font-size:26px;font-weight:bold;color:#c9a227;background:#f8fafc;border:1px solid #e2e8f0;padding:14px 20px;border-radius:10px;text-align:center\">"
                        + currency + " " + String.format(Locale.ENGLISH, "%,.2f", price) + "</div>"
                : "";
        String messageBlock = messageText != null && !messageText.isBlank()
                ? "<p style=\"color:#334155\">\"<i>" + escape(messageText) + "</i>\"</p>" : "";
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f8fafc;padding:24px;">
                  <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;padding:32px;border:1px solid #e2e8f0;">
                    <h2 style="color:#0f172a;margin-top:0">Deal agreed, %s!</h2>
                    <p style="color:#334155">Your negotiation for enquiry <b>%s</b> has been <b>accepted</b>. The below terms now stand:</p>
                    <p style="color:#334155;margin-bottom:2px"><b>Cargo:</b> %s</p>
                    %s
                    %s
                    <p style="color:#64748b;font-size:13px">Our team will finalise the formal quotation and next steps. Track everything in your dashboard:</p>
                    <p><a href="%s" style="display:inline-block;background:#0a2540;color:#ffffff;padding:12px 22px;border-radius:8px;text-decoration:none;">Open your dashboard</a></p>
                    <p style="color:#94a3b8;font-size:12px">ExportPlatform • trade support</p>
                  </div>
                </body></html>
                """.formatted(escape(user.getFullName()), enquiry.getReferenceNo(),
                escape(enquiry.getCargoType()), priceBlock, messageBlock,
                baseUrl + "/client/enquiries/" + enquiry.getId());
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}