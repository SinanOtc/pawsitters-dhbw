package dhbw.heilbronn.pawsitters.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "care_requests")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Pflichtfelder ===

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerProfile owner;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @NotNull
    @Future
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Future
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status;

    public CareRequest(OwnerProfile owner, Pet pet, LocalDate startDate, LocalDate endDate) {
        this.owner = owner;
        this.pet = pet;
        this.startDate = startDate;
        this.endDate = endDate;
        // Neue Anfragen erstellen ==> IMMER OPEN; Workflow setzt später MATCHED/CLOSED
        this.status = RequestStatus.OPEN;
    }

    /**
     * Constraint: Enddatum muss nach Startdatum liegen.
     * - @Future auf den Feldern prüft schon dass beide in der Zukunft sind.
     */
    @AssertTrue(message = "Enddatum muss nach Startdatum liegen")
    public boolean isDateRangeValid() {
        if(startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

}
