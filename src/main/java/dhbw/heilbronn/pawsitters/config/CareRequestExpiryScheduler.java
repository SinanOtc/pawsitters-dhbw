package dhbw.heilbronn.pawsitters.config;


import dhbw.heilbronn.pawsitters.service.CareRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Triggert das Schließen abgelaufener CareRequests:
 *  - einmalig beim Appstart (initialDelay = 0)
 *  - danach alle 24 h
 * Architektur: bewusst dünner Wrapper um den Service. Die Businesslogik lebt in CareRequestService.closedExpiredRequestS().
 * Der Scheduler ist nur der Trigger, damit der Service eigenständig testbar bleibt.
 */
@Component
public class CareRequestExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(CareRequestExpiryScheduler.class);

    private final CareRequestService careRequestService;

    public CareRequestExpiryScheduler(CareRequestService careRequestService){
        this.careRequestService =  careRequestService;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 24, timeUnit = TimeUnit.HOURS)
    public void closedExpiredRequests() {
        int updated = careRequestService.closeExpiredRequests();
        if(updated > 0){
            log.info("CareRequest-Expiry-Job: {} Anfrage(n) auf CLOSED gesetzt", updated);
        }
    }
}
