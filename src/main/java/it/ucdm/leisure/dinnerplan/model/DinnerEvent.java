package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dinner_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DinnerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "dinnerEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proposal> proposals = new ArrayList<>();

    private LocalDateTime deadline;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_proposal_id")
    private Proposal selectedProposal;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    public enum EventStatus {
        OPEN,
        CLOSED,
        DECIDED
    }
}
