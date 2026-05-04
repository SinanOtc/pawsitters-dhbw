package dhbw.heilbronn.pawsitters.domain;

/**
 * Tierarten, die aktuell unterstützt werden.
 * Enum statt String, damit garantiert nur gültige Werte übernommen werden
 * um Fehler im Filtersystem zu vermeiden
 */

public enum PetSpecies {
    DOG,
    CAT,
    BIRD,
    RABBIT,
    RODENT,
    REPTILE,
    FISH,
    OTHER
}
