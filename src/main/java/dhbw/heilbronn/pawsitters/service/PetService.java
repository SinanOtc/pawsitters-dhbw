package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.repository.PetRepository;
import dhbw.heilbronn.pawsitters.service.exception.PetNotFoundException;
import dhbw.heilbronn.pawsitters.web.form.PetForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logik für Pets.
 * ALLE Methoden die ein Pet referenzieren (find/update/delete) prüfen IMMER,
 * ob das Pet dem jeweiligen Owner gehört. Sonst könnte ein Owner per URL Manipulation an Fremde
 * Pets zugreifen
 */
@Service
public class PetService {

    private final PetRepository petRepository;
    private final OwnerService ownerService;

    public PetService(PetRepository petRepository, OwnerService ownerService) {
        this.petRepository = petRepository;
        this.ownerService = ownerService;
    }

    /**
     * Legt neues Pet für den angegebenen User an
     */
    @Transactional
    public Pet register(Long ownerUserId, PetForm form) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);

        Pet pet = new Pet(owner, form.name(), form.species(), form.gender());
        applyOptionalFields(pet, form);

        return petRepository.save(pet);
    }

    /**
     * Leifert alle Pets für einen Owner
     * readonly = true → keine Schreibrechte nötig für Übersicht
     */
    @Transactional(readOnly = true)
    public List<Pet> findAllByOwner(Long ownerUserId) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        return petRepository.findByOwnerId(owner.getId());
    }

    /**
     *Lädt EIN Pet, aber nur wenn es dem User auch gehört.
     * Wirft PetNotFoundException, wenn Pet nicht existiert oder nicht dem User gehört.
     */
    @Transactional(readOnly = true)
    public Pet findByIdForOwner(Long petId, Long ownerUserId) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        return petRepository.findByIdAndOwnerId(petId, owner.getId())
                .orElseThrow(() -> new PetNotFoundException(petId));
    }

    /**
     * Update aller änderbaren Felder.
     * Lookup direkt im Repository, statt findByIdForOwner,
     * da @Transactional Self Invocaiton den Spring Proxy umgehen würde (Dank IntelliJ Warnungen erkannt :) )
     */
    @Transactional
    public Pet update(Long petId, Long ownerUserId, PetForm form) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        Pet pet = petRepository.findByIdAndOwnerId(petId, owner.getId())
                .orElseThrow(() -> new PetNotFoundException(petId));

        applyAllFields(pet, form);
        return pet;
    }

    /**
     * Löscht ein Pet, aber nur wenn es dem Owner auch gehört
     */
    @Transactional
    public void delete(Long petId, Long ownerUserId) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        Pet pet = petRepository.findByIdAndOwnerId(petId, owner.getId())
                .orElseThrow(() -> new PetNotFoundException(petId));

        petRepository.delete(pet);
    }

    // === Helferfunktionen ===

    // Bei register() sind Pflichtfelder schon im Konstruktor gesestzt.
    // Hier werden optionale Felder implementiert.
    private void applyOptionalFields(Pet pet, PetForm form) {
        pet.setBreed(form.breed());
        pet.setBirthYear(form.birthYear());
        pet.setChipped(form.chipped());
        pet.setChipNumber(form.chipNumber());
        pet.setVaccinated(form.vaccinated());
        pet.setNeutered(form.neutered());
        pet.setDescription(form.description());
    }

    // Bei update() können ALLE Felder geändert werden
    private void applyAllFields(Pet pet, PetForm form) {
        pet.setName(form.name());
        pet.setSpecies(form.species());
        pet.setGender(form.gender());
        applyOptionalFields(pet, form);
    }
}
