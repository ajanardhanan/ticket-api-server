package com.ticketmanagement.service;

import com.ticketmanagement.dto.*;
import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Rating;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.repository.AgentRepository;
import com.ticketmanagement.repository.RatingRepository;
import com.ticketmanagement.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private TicketService ticketService;

    private Agent testAgent;
    private Ticket testTicket;
    private Rating testRating;

    @BeforeEach
    public void setUp() {
        testAgent = new Agent();
        testAgent.setId(1L);
        testAgent.setName("John Doe");
        testAgent.setEmail("john@example.com");

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("Test Description");
        testTicket.setPriority(Ticket.TicketPriority.HIGH);
        testTicket.setStatus(Ticket.TicketStatus.OPEN);

        testRating = new Rating();
        testRating.setId(1L);
        testRating.setScore(5);
        testRating.setFeedback("Excellent");
    }

    @Test
    public void testCreateTicket_Success() {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("New Ticket");
        request.setDescription("New Description");
        request.setPriority(Ticket.TicketPriority.MEDIUM);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            return ticket;
        });

        // Act
        Ticket result = ticketService.createTicket(request);

        // Assert
        assertNotNull(result);
        assertEquals("New Ticket", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(Ticket.TicketPriority.MEDIUM, result.getPriority());
        assertEquals(Ticket.TicketStatus.OPEN, result.getStatus());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    public void testAssignTicket_Success() {
        // Arrange
        AssignTicketRequest request = new AssignTicketRequest();
        request.setAgentId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // Act
        Ticket result = ticketService.assignTicket(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(testAgent, result.getAssignedAgent());
        assertEquals(Ticket.TicketStatus.IN_PROGRESS, result.getStatus());
        verify(ticketRepository, times(1)).findById(1L);
        verify(agentRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    public void testAssignTicket_TicketNotFound() {
        // Arrange
        AssignTicketRequest request = new AssignTicketRequest();
        request.setAgentId(1L);

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> ticketService.assignTicket(999L, request)
        );
        assertTrue(exception.getMessage().contains("Ticket not found"));
    }

    @Test
    public void testAssignTicket_AgentNotFound() {
        // Arrange
        AssignTicketRequest request = new AssignTicketRequest();
        request.setAgentId(999L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(agentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> ticketService.assignTicket(1L, request)
        );
        assertTrue(exception.getMessage().contains("Agent not found"));
    }

    @Test
    public void testUpdateTicket_Success() {
        // Arrange
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTitle("Updated Title");
        request.setStatus(Ticket.TicketStatus.PENDING);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // Act
        Ticket result = ticketService.updateTicket(1L, request);

        // Assert
        assertNotNull(result);
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    public void testCloseTicket_Success() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // Act
        Ticket result = ticketService.closeTicket(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Ticket.TicketStatus.CLOSED, result.getStatus());
        assertNotNull(result.getClosedAt());
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    public void testRateTicket_Success() {
        // Arrange
        testTicket.setAssignedAgent(testAgent);
        testTicket.setStatus(Ticket.TicketStatus.CLOSED);

        RateTicketRequest request = new RateTicketRequest();
        request.setScore(5);
        request.setFeedback("Excellent service");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ratingRepository.findByTicket(testTicket)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(1L);
            return rating;
        });

        // Act
        Rating result = ticketService.rateTicket(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getScore());
        assertEquals("Excellent service", result.getFeedback());
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    public void testRateTicket_NoAssignedAgent() {
        // Arrange
        testTicket.setAssignedAgent(null);
        testTicket.setStatus(Ticket.TicketStatus.CLOSED);

        RateTicketRequest request = new RateTicketRequest();
        request.setScore(5);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.rateTicket(1L, request)
        );
        assertTrue(exception.getMessage().contains("no assigned agent"));
    }

    @Test
    public void testRateTicket_TicketNotClosed() {
        // Arrange
        testTicket.setAssignedAgent(testAgent);
        testTicket.setStatus(Ticket.TicketStatus.IN_PROGRESS);

        RateTicketRequest request = new RateTicketRequest();
        request.setScore(5);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.rateTicket(1L, request)
        );
        assertTrue(exception.getMessage().contains("closed tickets"));
    }

    @Test
    public void testRateTicket_AlreadyRated() {
        // Arrange
        testTicket.setAssignedAgent(testAgent);
        testTicket.setStatus(Ticket.TicketStatus.CLOSED);

        RateTicketRequest request = new RateTicketRequest();
        request.setScore(5);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ratingRepository.findByTicket(testTicket)).thenReturn(Optional.of(testRating));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.rateTicket(1L, request)
        );
        assertTrue(exception.getMessage().contains("already been rated"));
    }

    @Test
    public void testGetAgentRatings_Success() {
        // Arrange
        RatingRepository.AgentRatingProjection projection1 = mock(RatingRepository.AgentRatingProjection.class);
        when(projection1.getAgentId()).thenReturn(1L);
        when(projection1.getAgentName()).thenReturn("Agent 1");
        when(projection1.getAverageScore()).thenReturn(4.5);
        when(projection1.getTotalRatings()).thenReturn(10L);

        RatingRepository.AgentRatingProjection projection2 = mock(RatingRepository.AgentRatingProjection.class);
        when(projection2.getAgentId()).thenReturn(2L);
        when(projection2.getAgentName()).thenReturn("Agent 2");
        when(projection2.getAverageScore()).thenReturn(3.8);
        when(projection2.getTotalRatings()).thenReturn(5L);

        when(ratingRepository.findAgentAverageRatings()).thenReturn(Arrays.asList(projection1, projection2));

        // Act
        List<AgentRatingResponse> result = ticketService.getAgentRatings();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getAgentId());
        assertEquals("Agent 1", result.get(0).getAgentName());
        assertEquals(4.5, result.get(0).getAverageScore());
        assertEquals(10L, result.get(0).getTotalRatings());
    }

    @Test
    public void testGetTicket_Success() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // Act
        Ticket result = ticketService.getTicket(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Ticket", result.getTitle());
    }

    @Test
    public void testGetTicket_NotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> ticketService.getTicket(999L)
        );
        assertTrue(exception.getMessage().contains("Ticket not found"));
    }

    @Test
    public void testGetAllTickets_Success() {
        // Arrange
        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setTitle("Second Ticket");

        when(ticketRepository.findAll()).thenReturn(Arrays.asList(testTicket, ticket2));

        // Act
        List<Ticket> result = ticketService.getAllTickets();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ticketRepository, times(1)).findAll();
    }
}
