package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ReviewRequest;
import com.company.exportplatform.dto.response.ReviewResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Review;
import com.company.exportplatform.entity.Shipment;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.AuditLogRepository;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.ReviewRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import com.company.exportplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final String EMAIL = "reviewer@test.com";

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    private ReviewService service;

    private User user(long id) {
        User user = new User();
        user.setId(id);
        user.setEmail(EMAIL);
        user.setFullName("Client Person");
        return user;
    }

    private Client client(long id) {
        Client c = new Client();
        c.setId(id);
        c.setUser(user(id + 100));
        return c;
    }

    private Shipment shipment(long id, Client owner, ShipmentStatus status) {
        Shipment s = new Shipment();
        s.setId(id);
        s.setShipmentRef("SHP-2026-0000" + id);
        s.setClient(owner);
        s.setStatus(status);
        return s;
    }

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, shipmentRepository,
                clientRepository, userRepository,
                new AuditService(auditLogRepository, new com.fasterxml.jackson.databind.ObjectMapper()));
    }

    private void loginAs(Client profile) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(profile.getUser()));
        when(clientRepository.findByUserId(profile.getUser().getId())).thenReturn(Optional.of(profile));
    }

    private ReviewRequest request(long shipmentId, int rating) {
        return new ReviewRequest(shipmentId, rating, "Title", "Text");
    }

    @Test
    @DisplayName("missing shipment yields 404-style not found")
    void missingShipmentNotFound() {
        loginAs(client(12L));
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(EMAIL, request(999L, 5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("another client's completed shipment is invisible (404)")
    void foreignShipmentNotFound() {
        loginAs(client(12L));
        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment(1L, client(77L), ShipmentStatus.COMPLETED)));

        assertThatThrownBy(() -> service.create(EMAIL, request(1L, 5)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("only COMPLETED shipments can be reviewed")
    void onlyCompletedCanBeReviewed() {
        loginAs(client(12L));
        when(shipmentRepository.findById(2L))
                .thenReturn(Optional.of(shipment(2L, client(12L), ShipmentStatus.IN_TRANSIT)));

        assertThatThrownBy(() -> service.create(EMAIL, request(2L, 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("completed");
    }

    @Test
    @DisplayName("duplicate reviews are rejected")
    void duplicateRejected() {
        loginAs(client(12L));
        when(shipmentRepository.findById(3L))
                .thenReturn(Optional.of(shipment(3L, client(12L), ShipmentStatus.COMPLETED)));
        when(reviewRepository.existsByShipmentId(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(EMAIL, request(3L, 4)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been reviewed");
    }

    @Test
    @DisplayName("valid review is saved pending moderation")
    void validReviewSavedPending() {
        Client me = client(12L);
        loginAs(me);
        when(shipmentRepository.findById(4L))
                .thenReturn(Optional.of(shipment(4L, me, ShipmentStatus.COMPLETED)));
        when(reviewRepository.existsByShipmentId(4L)).thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = service.create(EMAIL, request(4L, 5));

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();

        assertThat(saved.getRating()).isEqualTo(5);
        assertThat(saved.isApproved()).isFalse();
        assertThat(saved.getClient().getId()).isEqualTo(12L);
        assertThat(response.approved()).isFalse();
        assertThat(response.clientName()).isEqualTo("Client Person");
    }
}
