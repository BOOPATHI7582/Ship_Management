package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ClientReplyRequest;
import com.company.exportplatform.dto.request.OfferRequest;
import com.company.exportplatform.dto.response.NegotiationResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.Negotiation;
import com.company.exportplatform.entity.NegotiationMessage;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.EnquiryStatus;
import com.company.exportplatform.entity.enums.NegotiationMessageStatus;
import com.company.exportplatform.entity.enums.NegotiationThreadStatus;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.entity.enums.SenderType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.NegotiationMessageRepository;
import com.company.exportplatform.repository.NegotiationRepository;
import com.company.exportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final NegotiationMessageRepository messageRepository;
    private final EnquiryRepository enquiryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ---------- Manager side ----------

    @Transactional(readOnly = true)
    public NegotiationResponse forManager(Long enquiryId) {
        Enquiry enquiry = findEnquiry(enquiryId);
        Negotiation thread = openThreadOf(enquiry);
        return thread == null ? emptyResponse() : toResponse(thread);
    }

    @Transactional
    public NegotiationResponse sendOffer(Long enquiryId, String staffEmail, OfferRequest request) {
        if (request.getOfferPrice() == null && (request.getMessage() == null || request.getMessage().isBlank())) {
            throw new BadRequestException("An offer price or a message is required");
        }
        Enquiry enquiry = findEnquiry(enquiryId);
        if (enquiry.getStatus() == EnquiryStatus.REJECTED || enquiry.getStatus() == EnquiryStatus.CLOSED) {
            throw new BadRequestException("Cannot negotiate on a " + enquiry.getStatus() + " enquiry");
        }
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));

        Negotiation thread = negotiationRepository
                .findFirstByEnquiryIdAndStatusOrderByCreatedAtDesc(enquiryId, NegotiationThreadStatus.OPEN)
                .orElseGet(() -> {
                    Negotiation fresh = new Negotiation();
                    fresh.setEnquiry(enquiry);
                    fresh.setOpenedBy(staff);
                    return fresh;
                });

        withdrawOpenProposals(thread.getId(), SenderType.ADMIN);

        NegotiationMessage message = new NegotiationMessage();
        message.setNegotiation(thread);
        message.setSender(staff);
        message.setSenderType(SenderType.ADMIN);
        message.setOfferPrice(request.getOfferPrice());
        message.setMessage(request.getMessage());
        message.setStatus(NegotiationMessageStatus.PROPOSED);

        Negotiation saved = negotiationRepository.save(thread);
        message.setNegotiation(saved);
        messageRepository.save(message);

        if (enquiry.getStatus() != EnquiryStatus.APPROVED && enquiry.getStatus() != EnquiryStatus.CONVERTED) {
            enquiry.setStatus(EnquiryStatus.NEGOTIATING);
        }

        String priceText = request.getOfferPrice() != null
                ? "Offer: " + enquiry.getCurrency() + " " + request.getOfferPrice()
                : "New update from our team.";
        notifyClient(enquiry, "New offer on " + enquiry.getReferenceNo(),
                priceText + (request.getMessage() != null && !request.getMessage().isBlank()
                        ? " — " + trimTo(request.getMessage(), 200) : ""),
                "/client/enquiries/" + enquiry.getId());

        return toResponse(negotiationRepository.findById(saved.getId()).orElse(saved));
    }

    // ---------- Client side ----------

    @Transactional(readOnly = true)
    public NegotiationResponse forClient(String email, Long enquiryId) {
        Enquiry enquiry = ownedEnquiry(email, enquiryId);
        Negotiation thread = openThreadOf(enquiry);
        return thread == null ? emptyResponse() : toResponse(thread);
    }

    @Transactional
    public NegotiationReplyResult replyFromClient(String email, Long enquiryId, ClientReplyRequest request) {
        if ((request.getMessage() == null || request.getMessage().isBlank()) && request.getCounterPrice() == null) {
            throw new BadRequestException("Write a message or provide a counter price");
        }
        Enquiry enquiry = ownedEnquiry(email, enquiryId);
        Negotiation thread = openThreadOf(enquiry);
        if (thread == null || thread.getStatus() != NegotiationThreadStatus.OPEN) {
            throw new BadRequestException("There is no active negotiation on this enquiry yet");
        }

        withdrawOpenProposals(thread.getId(), SenderType.CLIENT);

        User user = enquiry.getClient().getUser();
        NegotiationMessage message = new NegotiationMessage();
        message.setNegotiation(thread);
        message.setSender(user);
        message.setSenderType(SenderType.CLIENT);
        message.setOfferPrice(request.getCounterPrice());
        message.setMessage(request.getMessage());
        message.setStatus(request.getCounterPrice() != null
                ? NegotiationMessageStatus.COUNTERED
                : NegotiationMessageStatus.PROPOSED);
        messageRepository.save(message);

        if (request.getCounterPrice() != null) {
            notifyClient(enquiry, "Counter offer sent",
                    "Your counter offer of " + enquiry.getCurrency() + " " + request.getCounterPrice()
                            + " was sent to our team.",
                    "/client/enquiries/" + enquiry.getId());
        }

        return new NegotiationReplyResult(toResponse(thread), message);
    }

    @Transactional
    public NegotiationResponse accept(String email, Long messageId) {
        NegotiationMessage message = ownedMessage(email, messageId);
        if (message.getSenderType() != SenderType.ADMIN) {
            throw new BadRequestException("Only offers from our team can be accepted");
        }
        if (message.getStatus() != NegotiationMessageStatus.PROPOSED
                && message.getStatus() != NegotiationMessageStatus.COUNTERED) {
            throw new BadRequestException("This offer is no longer open");
        }
        Negotiation thread = message.getNegotiation();
        if (thread.getStatus() != NegotiationThreadStatus.OPEN) {
            throw new BadRequestException("This negotiation is already closed");
        }

        message.setStatus(NegotiationMessageStatus.ACCEPTED);
        thread.setStatus(NegotiationThreadStatus.ACCEPTED);
        thread.setAgreedPrice(message.getOfferPrice());
        thread.setClosedAt(LocalDateTime.now());

        Enquiry enquiry = thread.getEnquiry();
        enquiry.setStatus(EnquiryStatus.APPROVED);

        notifyClient(enquiry, "Deal accepted on " + enquiry.getReferenceNo(),
                "You accepted the offer" + (message.getOfferPrice() != null
                        ? " of " + enquiry.getCurrency() + " " + message.getOfferPrice() : "")
                        + ". Quotation paperwork follows next.",
                "/client/enquiries/" + enquiry.getId());

        return toResponse(thread);
    }

    @Transactional
    public NegotiationResponse reject(String email, Long messageId) {
        NegotiationMessage message = ownedMessage(email, messageId);
        if (message.getSenderType() != SenderType.ADMIN) {
            throw new BadRequestException("Only offers from our team can be declined");
        }
        if (message.getStatus() != NegotiationMessageStatus.PROPOSED
                && message.getStatus() != NegotiationMessageStatus.COUNTERED) {
            throw new BadRequestException("This offer is no longer open");
        }
        Negotiation thread = message.getNegotiation();
        if (thread.getStatus() != NegotiationThreadStatus.OPEN) {
            throw new BadRequestException("This negotiation is already closed");
        }

        message.setStatus(NegotiationMessageStatus.REJECTED);

        Enquiry enquiry = thread.getEnquiry();
        notifyClient(enquiry, "Offer declined",
                "You declined the latest offer on " + enquiry.getReferenceNo()
                        + ". You can send a counter offer or continue the conversation.",
                "/client/enquiries/" + enquiry.getId());

        return toResponse(thread);
    }

    // ---------- helpers ----------

    public record NegotiationReplyResult(NegotiationResponse thread, NegotiationMessage savedMessage) {
    }

    private void withdrawOpenProposals(Long threadId, SenderType senderType) {
        if (threadId == null) {
            return;
        }
        messageRepository.findByNegotiationIdOrderByCreatedAtAsc(threadId).stream()
                .filter(m -> m.getSenderType() == senderType)
                .filter(m -> m.getStatus() == NegotiationMessageStatus.PROPOSED
                        || m.getStatus() == NegotiationMessageStatus.COUNTERED)
                .forEach(m -> m.setStatus(NegotiationMessageStatus.WITHDRAWN));
    }

    private Negotiation openThreadOf(Enquiry enquiry) {
        return negotiationRepository
                .findFirstByEnquiryIdAndStatusOrderByCreatedAtDesc(enquiry.getId(), NegotiationThreadStatus.OPEN)
                .orElseGet(() -> negotiationRepository.findByEnquiryIdOrderByCreatedAtDesc(enquiry.getId()).stream()
                        .max(Comparator.comparing(Negotiation::getCreatedAt))
                        .orElse(null));
    }

    private Enquiry findEnquiry(Long id) {
        return enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
    }

    private Enquiry ownedEnquiry(String email, Long enquiryId) {
        Enquiry enquiry = findEnquiry(enquiryId);
        Client client = enquiry.getClient();
        User user = client != null ? client.getUser() : null;
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            throw new ResourceNotFoundException("Enquiry not found");
        }
        return enquiry;
    }

    private NegotiationMessage ownedMessage(String email, Long messageId) {
        NegotiationMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation message not found"));
        Enquiry enquiry = message.getNegotiation().getEnquiry();
        Client client = enquiry.getClient();
        User user = client != null ? client.getUser() : null;
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            throw new ResourceNotFoundException("Negotiation message not found");
        }
        return message;
    }

    private void notifyClient(Enquiry enquiry, String title, String message, String link) {
        Client client = enquiry.getClient();
        User user = client != null ? client.getUser() : null;
        if (user != null) {
            notificationService.notify(user, NotificationType.NEGOTIATION, title,
                    trimTo(message, 990), link, "ENQUIRY", enquiry.getId());
        }
    }

    private String trimTo(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }

    private NegotiationResponse emptyResponse() {
        return new NegotiationResponse(null, null, null, null, List.of());
    }

    private NegotiationResponse toResponse(Negotiation thread) {
        List<NegotiationResponse.NegotiationMessageResponse> messages =
                messageRepository.findByNegotiationIdOrderByCreatedAtAsc(thread.getId()).stream()
                        .map(this::toMessageResponse)
                        .toList();
        return new NegotiationResponse(
                thread.getId(),
                thread.getStatus() != null ? thread.getStatus().name() : null,
                thread.getAgreedPrice(),
                thread.getClosedAt(),
                messages
        );
    }

    private NegotiationResponse.NegotiationMessageResponse toMessageResponse(NegotiationMessage message) {
        return new NegotiationResponse.NegotiationMessageResponse(
                message.getId(),
                message.getSenderType() != null ? message.getSenderType().name() : null,
                message.getSender() != null ? message.getSender().getFullName() : "Operations Team",
                message.getOfferPrice(),
                message.getMessage(),
                message.getStatus() != null ? message.getStatus().name() : null,
                message.getCreatedAt()
        );
    }
}
