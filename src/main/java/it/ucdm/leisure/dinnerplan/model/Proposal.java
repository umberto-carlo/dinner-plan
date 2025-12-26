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
@Table(name = "proposals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dinner_event_id", nullable = false)
    private DinnerEvent dinnerEvent;

    private LocalDateTime dateOption;

    private String location;

    private String address;

    private String description;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();
}
