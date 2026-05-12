package dhbw.heilbronn.pawsitters.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Not;

import java.math.BigDecimal;

/**
 * Angebot eines Hosts auf eine konkrete Betreuuungsanfrage.
 * Status startet immer auf PENDING, wechsel zu ACCEPTED/REJECTED,
 * passiert workflowgetrieben durch Owner-Action.
 */
@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Pflichtfelder ===
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private HostProfile host;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "care_request_id", nullable = false)
    private CareRequest careRequest;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "Wochenpreis muss höher als 0€ sein")
    @Digits(integer = 6, fraction = 8)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal weeklyPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OfferStatus status;

    public Offer(HostProfile host, CareRequest careRequest, BigDecimal weeklyPrice) {
        this.host = host;
        this.careRequest = careRequest;
        this.weeklyPrice = weeklyPrice;
        // Neue Offers starten immer als Pending.
        this.status = OfferStatus.PENDING;
    }

}
