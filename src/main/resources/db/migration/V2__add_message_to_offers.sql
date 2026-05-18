-- Issue #88: Optionales Nachrichten-Feld am Offer.
-- Hosts können beim Angebot eine kurze Nachricht an den Owner mitschicken;
-- nullable, weil "Angebot ohne Nachricht" weiter erlaubt sein muss.
-- Limit 500 Zeichen synchron mit @Size(max=500) am Entity-Feld (siehe Offer.java).
ALTER TABLE offers ADD COLUMN message VARCHAR(500);
