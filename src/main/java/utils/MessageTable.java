package utils;

class MessageTable {

    static void fill() {
        fill1();
        fill2();
        fill3();
        fill4();
        fill5();
        fill6();
    }

    private static void fill6() {
        Messages.put("room.create.throttled_retry",
                "No FlatCreated (attempt %d of %d) - the server sometimes just does not answer. Waiting %d s and trying again",
                "Kein FlatCreated (Versuch %d von %d) - der Server antwortet manchmal einfach nicht. Warte %d s und versuche es erneut");
        Messages.put("inventory.requested",
                "Requesting the inventory from the server - with a large inventory this takes a while",
                "Frage das Inventar beim Server ab - bei gro\u00dfem Inventar dauert das eine Weile");
        Messages.put("inventory.block_released",
                "A stale inventory request was still hiding FurniList packets from the client - released",
                "Eine h\u00e4ngende Inventar-Anfrage hat noch FurniList-Pakete vor dem Client versteckt - freigegeben");
        Messages.put("inventory.kept_cached",
                "The server did not resend the inventory - it only sends the full list once per session. Keeping the %d items already read",
                "Der Server hat das Inventar nicht erneut geschickt - er sendet die volle Liste nur einmal pro Session. Behalte die %d bereits gelesenen Objekte");
        Messages.put("inventory.invalidated",
                "The server marked the inventory as outdated - press \"Load inventory\" to read it again",
                "Der Server hat das Inventar als veraltet markiert - dr\u00fccke \"Inventar laden\", um es neu zu lesen");
        Messages.put("ui.button.reloadroom",
                "Reload room",
                "Raum neu laden");
        Messages.put("room.reload.start",
                "Reloading room %d so the extension can read it",
                "Lade Raum %d neu, damit die Extension ihn auslesen kann");
        Messages.put("room.reload.done",
                "Room read",
                "Raum ausgelesen");
        Messages.put("room.reload.unknown",
                "No room id known yet - walk into a room first",
                "Noch keine Raum-ID bekannt - geh erst in einen Raum");
        Messages.put("room.create.outgoing_bytes",
                "CreateFlat bytes: %s",
                "CreateFlat-Bytes: %s");
        Messages.put("room.create.safe_name",
                "Creating the room as \"%s\" first - the real name \"%s\" is applied afterwards with the room settings",
                "Lege den Raum zuerst als \"%s\" an - der echte Name \"%s\" kommt danach mit den Raum-Settings");
        Messages.put("preset.newroom.roomname",
                "The new room is called \"%s\"",
                "Der neue Raum hei\u00dft \"%s\"");
        Messages.put("preset.newroom.saved_plan",
                "Using the saved floor plan of the original room (%sx%s, %s tiles, door %s,%s)",
                "Benutze den gespeicherten Floorplan des Originalraums (%sx%s, %s Kacheln, T\u00fcr %s,%s)");
        Messages.put("preset.newroom.generated_plan",
                "No room snapshot saved for this preset - using a generated %sx%s rectangle",
                "Kein Raum-Snapshot zu diesem Preset gespeichert - benutze ein generiertes %sx%s-Rechteck");
        Messages.put("preset.newroom.plan_too_small",
                "The preset needs %sx%s but the saved plan is only %sx%s - furni outside it will not be placed",
                "Das Preset braucht %sx%s, der gespeicherte Plan ist aber nur %sx%s - M\u00f6bel au\u00dferhalb werden nicht gesetzt");
        Messages.put("preset.newroom.snapshot_unreadable",
                "Room snapshot \"%s\" could not be read (%s) - using a generated rectangle",
                "Raum-Snapshot \"%s\" nicht lesbar (%s) - benutze ein generiertes Rechteck");
        Messages.put("ui.dialog.save",
                "Save",
                "Speichern");
        Messages.put("ui.dialog.cancel",
                "Cancel",
                "Abbrechen");
        Messages.put("categories.loaded",
                "%s room categories read from the server",
                "%s Raum-Kategorien vom Server gelesen");
        Messages.put("preset.rename.unchanged",
                "Enter a name that differs from the current one",
                "Gib einen Namen ein, der sich vom aktuellen unterscheidet");
        Messages.put("preset.editor.title",
                "Room settings of the preset",
                "Raum-Settings des Presets");
        Messages.put("preset.editor.header",
                "Preset: %s",
                "Preset: %s");
        Messages.put("preset.editor.unreadable",
                "\"%s\" could not be read (%s)",
                "\"%s\" konnte nicht gelesen werden (%s)");
        Messages.put("preset.editor.saved",
                "Room settings of \"%s\" saved",
                "Raum-Settings von \"%s\" gespeichert");
        Messages.put("preset.editor.save_failed",
                "Could not save the room settings (%s)",
                "Raum-Settings konnten nicht gespeichert werden (%s)");
        Messages.put("preset.editor.tab.basic",
                "Basic",
                "Allgemein");
        Messages.put("preset.editor.tab.access",
                "Access",
                "Zugang");
        Messages.put("preset.editor.tab.hc",
                "HC",
                "HC");
        Messages.put("preset.editor.tab.mod",
                "ModTools",
                "ModTools");
        Messages.put("preset.editor.name",
                "Room name",
                "Raumname");
        Messages.put("preset.editor.description",
                "Description",
                "Beschreibung");
        Messages.put("preset.editor.category",
                "Category",
                "Kategorie");
        Messages.put("preset.editor.category_id",
                "Category (numeric id)",
                "Kategorie (Zahl)");
        Messages.put("preset.editor.visitors",
                "Maximum amount of visitors",
                "Maximale Besucherzahl");
        Messages.put("preset.editor.visitors_n.10",
                "10",
                "10");
        Messages.put("preset.editor.visitors_n.25",
                "25",
                "25");
        Messages.put("preset.editor.visitors_n.50",
                "50",
                "50");
        Messages.put("preset.editor.visitors_n.75",
                "75",
                "75");
        Messages.put("preset.editor.visitors_n.100",
                "100",
                "100");
        Messages.put("preset.editor.trade",
                "Trade settings",
                "Handel");
        Messages.put("preset.editor.trade.0",
                "Trading not allowed (0)",
                "Handel nicht erlaubt (0)");
        Messages.put("preset.editor.trade.1",
                "Trading with rights (1)",
                "Handel mit Rechten (1)");
        Messages.put("preset.editor.trade.2",
                "Trading for everyone (2)",
                "Handel f\u00fcr alle (2)");
        Messages.put("preset.editor.tag1",
                "Tag 1",
                "Tag 1");
        Messages.put("preset.editor.tag2",
                "Tag 2",
                "Tag 2");
        Messages.put("preset.editor.walkthrough",
                "Allow walking through users",
                "Durch Nutzer hindurchlaufen erlauben");
        Messages.put("preset.editor.unavailable",
                "Tags are carried by the packet, but the clone always sent zero of them - set here they are transferred. Deleting a room is a separate packet and is not part of this editor.",
                "Tags tr\u00e4gt das Paket mit, der Klon hat aber immer null gesendet - hier gesetzt werden sie \u00fcbertragen. Raum l\u00f6schen ist ein eigenes Paket und geh\u00f6rt nicht in diesen Editor.");
        Messages.put("preset.editor.access",
                "Access to this room",
                "Zugang zu diesem Raum");
        Messages.put("preset.editor.door.0",
                "Open - anyone can enter",
                "Offen - jeder kann rein");
        Messages.put("preset.editor.door.1",
                "Visitors have to ring the doorbell",
                "Besucher m\u00fcssen klingeln");
        Messages.put("preset.editor.door.2",
                "Password is required",
                "Passwort erforderlich");
        Messages.put("preset.editor.door.3",
                "Invisible in navigator",
                "Unsichtbar im Navigator");
        Messages.put("preset.editor.password",
                "Password",
                "Passwort");
        Messages.put("preset.editor.password_note",
                "The room password is not part of any packet the extension can read, so a clone can never carry it over. Set it here and it is stored in the preset file as plain text and sent on the next build.",
                "Das Raum-Passwort steht in keinem Paket, das die Extension lesen kann - ein Klon kann es also nie mitnehmen. Hier gesetzt landet es im Klartext in der Preset-Datei und wird beim n\u00e4chsten Aufbau mitgesendet.");
        Messages.put("preset.editor.error.password",
                "Password mode needs a password - without one the room would be unreachable",
                "Der Passwortmodus braucht ein Passwort - ohne w\u00e4re der Raum nicht betretbar");
        Messages.put("preset.editor.pet_settings",
                "Pet settings",
                "Haustiere");
        Messages.put("preset.editor.pets",
                "Allow pets",
                "Haustiere erlauben");
        Messages.put("preset.editor.pets_eat",
                "Allow other pets to eat food",
                "Fremde Haustiere d\u00fcrfen fressen");
        Messages.put("preset.editor.pets_mute",
                "Mute all pets",
                "Alle Haustiere stummschalten");
        Messages.put("preset.editor.hidewalls",
                "Hide room walls (HC only)",
                "Raumw\u00e4nde ausblenden (nur HC)");
        Messages.put("preset.editor.wallthickness",
                "Wall thickness",
                "Wanddicke");
        Messages.put("preset.editor.floorthickness",
                "Floor thickness",
                "Bodendicke");
        Messages.put("preset.editor.thickness_n.-2",
                "Thinnest (-2)",
                "D\u00fcnnste (-2)");
        Messages.put("preset.editor.thickness_n.-1",
                "Thin (-1)",
                "D\u00fcnn (-1)");
        Messages.put("preset.editor.thickness_n.0",
                "Normal (0)",
                "Normal (0)");
        Messages.put("preset.editor.thickness_n.1",
                "Thick (1)",
                "Dick (1)");
        Messages.put("preset.editor.leaveondoor",
                "Leave room when walking on the door tile",
                "Raum verlassen beim Betreten der T\u00fcrkachel");
        Messages.put("preset.editor.sleep",
                "Sleep after timeout",
                "Nach Zeit einschlafen");
        Messages.put("preset.editor.sleep_seconds",
                "Sleep after (seconds)",
                "Einschlafen nach (Sekunden)");
        Messages.put("preset.editor.autokick",
                "Auto-kick after timeout",
                "Nach Zeit auto-kicken");
        Messages.put("preset.editor.autokick_seconds",
                "Auto-kick after (seconds)",
                "Auto-Kick nach (Sekunden)");
        Messages.put("preset.editor.flood",
                "Flood sensitivity",
                "Flood-Empfindlichkeit");
        Messages.put("preset.editor.flood.0",
                "Strict (0)",
                "Streng (0)");
        Messages.put("preset.editor.flood.1",
                "Standard anti-flood protection (1)",
                "Standard-Flood-Schutz (1)");
        Messages.put("preset.editor.flood.2",
                "Loose (2)",
                "Locker (2)");
        Messages.put("preset.editor.who_mute",
                "Who can mute",
                "Wer darf stummschalten");
        Messages.put("preset.editor.who_kick",
                "Who can kick",
                "Wer darf kicken");
        Messages.put("preset.editor.who_ban",
                "Who can ban",
                "Wer darf bannen");
        Messages.put("preset.editor.rights.0",
                "None (0)",
                "Niemand (0)");
        Messages.put("preset.editor.rights.1",
                "Rights holders (1)",
                "Rechteinhaber (1)");
        Messages.put("preset.editor.rights.2",
                "Everyone (2)",
                "Alle (2)");
        Messages.put("preset.editor.mod_unavailable",
                "The banned-user list and Unban are separate packets and are not part of this editor. Numbers in brackets are the raw protocol values.",
                "Die Bannliste und Unban sind eigene Pakete und nicht Teil dieses Editors. Zahlen in Klammern sind die rohen Protokollwerte.");
        Messages.put("preset.editor.error.name",
                "The room name must not be empty",
                "Der Raumname darf nicht leer sein");
        Messages.put("preset.editor.error.seconds",
                "The timeouts must be whole, non-negative numbers",
                "Die Zeiten m\u00fcssen ganze, nicht negative Zahlen sein");
        Messages.put("preset.editor.error.category",
                "The category must be a non-negative number",
                "Die Kategorie muss eine nicht negative Zahl sein");
        Messages.put("ui.presets.edit", "Edit", "Bearbeiten");
        Messages.put("ui.presets.rename", "Rename", "Umbenennen");
        Messages.put("ui.presets.delete", "Delete", "L\u00f6schen");
        Messages.put("preset.rename.title", "Rename preset", "Preset umbenennen");
        Messages.put("preset.rename.header", "Current name: %s", "Aktueller Name: %s");
        Messages.put("preset.rename.label", "New name:", "Neuer Name:");
        Messages.put("preset.rename.invalid", "\"%s\" is not a usable file name", "\"%s\" ist kein brauchbarer Dateiname");
        Messages.put("preset.rename.exists", "A preset named \"%s\" already exists", "Ein Preset namens \"%s\" existiert schon");
        Messages.put("preset.rename.done", "Preset renamed: \"%s\" -> \"%s\"", "Preset umbenannt: \"%s\" -> \"%s\"");
        Messages.put("preset.rename.failed", "Could not rename \"%s\"", "\"%s\" lie\u00df sich nicht umbenennen");
        Messages.put("preset.delete.title", "Delete preset", "Preset l\u00f6schen");
        Messages.put("preset.delete.header", "Delete \"%s\"?", "\"%s\" l\u00f6schen?");
        Messages.put("preset.delete.body", "The preset file and its room snapshot are deleted from disk. This cannot be undone.",
                "Die Preset-Datei und ihr Raum-Snapshot werden von der Platte gel\u00f6scht. Das l\u00e4sst sich nicht widerrufen.");
        Messages.put("preset.delete.done", "Preset \"%s\" deleted", "Preset \"%s\" gel\u00f6scht");
        Messages.put("preset.delete.failed", "Could not delete \"%s\"", "\"%s\" lie\u00df sich nicht l\u00f6schen");
        Messages.put("settings.full_read",
                "Full room settings read (flood %s, idle %s/%s, rights %s/%s/%s)",
                "Vollst\u00e4ndige Raum-Settings gelesen (Flood %s, Idle %s/%s, Rechte %s/%s/%s)");
        Messages.put("settings.name_fallback",
                "The name \"%s\" did not take. Trying the plain room name \"%s\" without the suffix",
                "Der Name \"%s\" ist nicht angekommen. Versuche den reinen Raumnamen \"%s\" ohne Zusatz");
        Messages.put("settings.name_fallback_ok",
                "The room is called \"%s\" now - without the suffix, but named",
                "Der Raum hei\u00dft jetzt \"%s\" - ohne Zusatz, aber benannt");
        Messages.put("settings.name_fallback_failed",
                "Even the plain room name did not take - the room keeps its creation name",
                "Auch der reine Raumname ist nicht angekommen - der Raum beh\u00e4lt seinen Erstell-Namen");
        Messages.put("preset.name.numbered",
                "A preset called \"%s\" already exists, saving as \"%s\"",
                "Ein Preset namens \"%s\" gibt es schon, speichere als \"%s\"");
        Messages.put("settings.verify.all_ok",
                "Verified by readback: \"%s\" - every transferred field matches the source",
                "Per R\u00fcckleseprobe best\u00e4tigt: \"%s\" - alle \u00fcbertragenen Felder stimmen mit der Quelle");
        Messages.put("settings.verify.diff_count",
                "READBACK: %s field(s) did not survive - the server holds different values:",
                "R\u00dcCKLESEPROBE: %s Feld(er) haben nicht \u00fcberlebt - der Server h\u00e4lt andere Werte:");
        Messages.put("settings.verify.field", "%s: expected %s, server reports %s",
                "%s: erwartet %s, Server meldet %s");
        Messages.put("settings.verify.ok", "Verified by readback: the room is now called \"%s\"",
                "Per R\u00fcckleseprobe best\u00e4tigt: der Raum hei\u00dft jetzt \"%s\"");
        Messages.put("settings.verify.mismatch",
                "READBACK MISMATCH: the server reports \"%s\", expected was \"%s\" - the settings really did not apply",
                "R\u00dcCKLESEPROBE ABWEICHEND: der Server meldet \"%s\", erwartet war \"%s\" - die Settings sind wirklich nicht angekommen");
        Messages.put("settings.verify.unreadable", "Could not read the settings of room %s back",
                "Settings von Raum %s lie\u00dfen sich nicht zur\u00fccklesen");
        Messages.put("settings.debug.sent", "SaveRoomSettings sent: %s", "SaveRoomSettings gesendet: %s");
        Messages.put("settings.debug.await", "Waiting for %s / %s", "Warte auf %s / %s");
        Messages.put("settings.retry_raw",
                "No confirmation - retrying with raw (Latin-1) strings",
                "Keine Best\u00e4tigung - Wiederholung mit rohen (Latin-1) Strings");
        Messages.put("settings.applied_raw",
                "Room settings applied - the raw (Latin-1) variant was the one that worked",
                "Raum-Settings \u00fcbernommen - die rohe (Latin-1) Variante hat gegriffen");
        Messages.put("settings.retry",
                "No confirmation for the room settings - the room was probably still reloading. Trying once more",
                "Keine Best\u00e4tigung f\u00fcr die Raum-Settings - der Raum lud wohl noch neu. Versuche es nochmal");
        Messages.put("settings.rename",
                "Renaming the room to \"%s\"",
                "Benenne den Raum in \"%s\" um");
        Messages.put("room.create.header_info",
                "Header: %s",
                "Header: %s");
        Messages.put("room.create.outgoing_packet",
                "CreateFlat packet going out: %s",
                "Rausgehendes CreateFlat-Paket: %s");
        Messages.put("room.create.detected_via_entry",
                "No FlatCreated, but the client was moved into room %d - using that as the new room",
                "Kein FlatCreated, aber der Client wurde in Raum %d bewegt - nehme den als neuen Raum");
        Messages.put("inventory.progress",
                "Inventory: chunk %d of %d, %d items so far",
                "Inventar: Teil %d von %d, bisher %d Objekte");
        Messages.put("inventory.no_answer",
                "No inventory data after %d s - the server ignored the request. Press \"Load inventory\" again",
                "Nach %d s keine Inventardaten - der Server hat die Anfrage ignoriert. Dr\u00fccke nochmal \"Inventar laden\"");
        Messages.put("ui.language.en", "English", "Englisch");
        Messages.put("ui.language.de", "German", "Deutsch");
        Messages.put("ui.label.language",
                "Language:",
                "Sprache:");
        Messages.put("ui.button.presettonewroom",
                "Create room + build",
                "Raum anlegen + aufbauen");
        Messages.put("ui.label.presettonewroomhint",
                "Creates a room large enough for the preset, enters it and builds the preset there.",
                "Legt einen passend gro\u00dfen Raum an, springt rein und baut das Preset dort auf.");
        Messages.put("annex.describe.full",
                "Annex %dx%d at %d,%d | door %d,%d | stack tile %dx%d at %d,%d | free space %d,%d | plan %dx%d",
                "Anbau %dx%d ab %d,%d | T\u00fcr %d,%d | Stacktile %dx%d ab %d,%d | Freifl\u00e4che %d,%d | Plan %dx%d");
        Messages.put("stacktile.in_room",
                "Stack tile is in the room (ID %s)",
                "Stacktile liegt im Raum (ID %s)");
        Messages.put("stacktile.none_available",
                "No stack tile \"%s\" available - put one in your inventory or pick a different stack tile size",
                "Kein Stacktile \"%s\" verf\u00fcgbar - leg eins ins Inventar oder w\u00e4hle eine andere Stacktile-Gr\u00f6\u00dfe");
        Messages.put("stacktile.source.bc",
                "Builders Club",
                "Builders Club");
        Messages.put("stacktile.spot.preferred_unusable",
                "The intended stack tile spot %d,%d is not usable - searching inside the room",
                "Der vorgesehene Stacktile-Platz %d,%d ist nicht nutzbar - suche im Raum");
        Messages.put("preset.newroom.title",
                "=== Creating a room and building preset \"%s\" ===",
                "=== Raum anlegen und Preset \"%s\" aufbauen ===");
        Messages.put("preset.newroom.load_failed",
                "Preset \"%s\" could not be loaded or is empty",
                "Preset \"%s\" konnte nicht geladen werden oder ist leer");
        Messages.put("preset.newroom.too_large",
                "The preset needs %dx%d tiles - that does not fit into a Habbo room",
                "Das Preset braucht %dx%d Felder - das passt nicht in einen Habbo-Raum");
        Messages.put("preset.newroom.summary",
                "Preset \"%s\": %d furni, needs %dx%d tiles",
                "Preset \"%s\": %d M\u00f6bel, braucht %dx%d Felder");
        Messages.put("stacktile.no_space_found",
                "No space found for stack tile and free area",
                "Kein Platz f\u00fcr Stacktile und Freifl\u00e4che gefunden");
    }

    private static void fill1() {
        Messages.put("annex.blockers.count",
                "Still in the annex: %d furni",
                "Noch im Anbau: %d M\u00f6bel");
        Messages.put("annex.blockers.state_empty",
                "The room state shows the annex as empty - the furni in there slipped past the state. Check the annex yourself",
                "Im Raum-State ist der Anbau leer - das M\u00f6bel darin ist dem State entgangen. Schau selbst im Anbau nach");
        Messages.put("annex.describe",
                "Annex %dx%d from %d,%d - door %d,%d, stack tile %d,%d, free space %d,%d (plan %dx%d)",
                "Anbau %dx%d ab %d,%d - T\u00fcr %d,%d, Stacktile %d,%d, Freifl\u00e4che %d,%d (Plan %dx%d)");
        Messages.put("annex.door.restore_failed",
                "The door could not be reset - do that in the floor plan editor",
                "Die T\u00fcr konnte nicht zur\u00fcckgesetzt werden - mach das im Floorplan-Editor");
        Messages.put("annex.door.restore_only",
                "Resetting at least the door to %d,%d dir %d",
                "Setze wenigstens die T\u00fcr zur\u00fcck auf %d,%d dir %d");
        Messages.put("annex.door.restored",
                "Door is back like in the original, only the annex remains",
                "T\u00fcr steht wieder wie im Original, nur der Anbau bleibt");
        Messages.put("annex.not_fitting",
                "The work area does not fit - Habbo caps a room at 55x55 plus the door tile. Stack tile and free space are placed inside the room instead",
                "Der Arbeitsbereich passt nicht - Habbo begrenzt einen Raum auf 55x55 plus T\u00fcrkachel. Stapelfeld und Freifl\u00e4che werden stattdessen im Raum gesetzt");
        Messages.put("annex.purpose",
                "The annex keeps the stack tile, the free area and the door out of the real room and is removed again after the build",
                "Der Anbau h\u00e4lt Stacktile, Freifl\u00e4che und T\u00fcr aus dem echten Raum heraus und wird nach dem Aufbau wieder entfernt");
        Messages.put("annex.remove.done",
                "Annex removed, door back at %d,%d dir %d - the room now matches the original",
                "Anbau entfernt, T\u00fcr zur\u00fcck auf %d,%d dir %d - der Raum entspricht jetzt dem Original");
        Messages.put("annex.remove.manual_hint",
                "Pick up the furni in the annex, then remove the annex in the floor plan editor",
                "Nimm die M\u00f6bel im Anbau auf und entferne den Anbau dann im Floorplan-Editor");
        Messages.put("annex.remove.no_response",
                "The server did not answer the annex teardown",
                "Der Server hat auf den Abriss nicht geantwortet");
        Messages.put("annex.remove.rejected",
                "The server discarded the annex teardown - there is still something in the annex",
                "Der Server hat den Abriss verworfen - im Anbau liegt noch etwas");
        Messages.put("annex.remove.send_failed",
                "The annex could not be removed (sending failed)",
                "Der Anbau konnte nicht entfernt werden (Senden fehlgeschlagen)");
        Messages.put("annex.remove.start",
                "Removing the work annex and resetting the door",
                "Entferne den Arbeits-Anbau und setze die T\u00fcr zur\u00fcck");
        Messages.put("capture.flooritems.parsed",
                "Parsed floor items",
                "Bodenm\u00f6bel eingelesen");
        Messages.put("capture.floorplan.dimensions",
                "%dx%d, %d tiles",
                "%dx%d, %d Felder");
        Messages.put("capture.floorplan.missing",
                "missing",
                "fehlt");
        Messages.put("capture.floorplan.parsed",
                "Parsed floor plan",
                "Floorplan eingelesen");
        Messages.put("capture.floorplan.read_failed",
                "Floor plan could not be read: %s",
                "Floorplan konnte nicht gelesen werden: %s");
        Messages.put("capture.heightmap.parsed",
                "Parsed heightmap",
                "H\u00f6henkarte eingelesen");
        Messages.put("capture.no_refresh_request",
                "No resolvable room refresh request in this client, using the recorded packets",
                "Kein aufl\u00f6sbarer Raum-Refresh-Request in diesem Client, nutze die mitgeschnittenen Pakete");
        Messages.put("capture.not_recorded",
                "Not recorded: %s",
                "Nicht mitgeschnitten: %s");
        Messages.put("capture.roomdata.missing",
                "Room data missing (GetGuestRoomResult) - walk out of the room once and back in",
                "Raumdaten fehlen (GetGuestRoomResult) - lauf einmal aus dem Raum raus und wieder rein");
        Messages.put("capture.roomdata.save_failed",
                "Room data could not be saved: %s",
                "Raumdaten konnten nicht gesichert werden: %s");
        Messages.put("capture.roomdata.saved",
                "Room data saved: %s",
                "Raumdaten gesichert: %s");
        Messages.put("capture.settings_fields_missing",
                "Room settings: %d field(s) missing in this client's packets, defaults used",
                "Raum-Settings: %d Feld(er) fehlen in den Paketen dieses Clients, Defaults benutzt");
        Messages.put("capture.summary",
                "Room captured: \"%s\" (ID %d), floor plan %s, %d wall furni",
                "Raum erfasst: \"%s\" (ID %d), Floorplan %s, %d Wandm\u00f6bel");
        Messages.put("capture.wallitems.read_failed",
                "Wall furni could not be read: %s",
                "Wandm\u00f6bel konnten nicht gelesen werden: %s");
        Messages.put("clone.aborted.exception",
                "Cloning aborted: %s",
                "Klonen abgebrochen: %s");
        Messages.put("clone.already_running",
                "A clone run is already in progress",
                "Es l\u00e4uft schon ein Klon-Vorgang");
        Messages.put("clone.build.cancelled",
                "Build cancelled",
                "Aufbau abgebrochen");
        Messages.put("clone.build.done",
                "Furni and wired are built",
                "M\u00f6bel und Wired sind aufgebaut");
        Messages.put("clone.build.failed",
                "The build did not complete",
                "Der Aufbau ist nicht durchgelaufen");
        Messages.put("clone.build.phase",
                "Build phase: %s",
                "Aufbau-Phase: %s");
        Messages.put("clone.build.start",
                "Starting the build",
                "Starte den Aufbau");
        Messages.put("clone.build.stuck",
                "Build has been stuck in %s for 10 minutes - aborted",
                "Aufbau h\u00e4ngt seit 10 Minuten in %s - abgebrochen");
        Messages.put("clone.cancel.requested",
                "Cancel requested - the running step will still finish",
                "Abbruch angefordert - laufender Schritt wird noch beendet");
        Messages.put("clone.cleanup_incomplete",
                "Cleanup was not complete - see the orange lines above",
                "Aufr\u00e4umen war nicht komplett - siehe die orangenen Zeilen oben");
        Messages.put("clone.done",
                "=== Done: room \"%s\" (ID %d) ===",
                "=== Fertig: Raum \"%s\" (ID %d) ===");
        Messages.put("clone.finish_pending_first",
                "Finish or cancel the running export/build first",
                "Erst den laufenden Export/Aufbau beenden oder abbrechen");
        Messages.put("clone.furnidata_not_ready",
                "Furnidata is not loaded yet",
                "Furnidata ist noch nicht geladen");
        Messages.put("clone.height_offset_mismatch",
                "Height offset in the target room is %d, in the source room it was %d - using the source value so the heights match",
                "H\u00f6hen-Offset im Zielraum ist %d, im Quellraum war er %d - benutze den Quellwert, damit die H\u00f6hen stimmen");
        Messages.put("clone.hint.start",
                "Stand in the room you want to copy and press \"Copy room\".",
                "Stell dich in den Raum, den du kopieren willst, und dr\u00fccke \"Raum kopieren\".");
        Messages.put("clone.no_floorplan",
                "Without a floor plan the room cannot be cloned 1:1",
                "Ohne Floorplan kann der Raum nicht 1:1 geklont werden");
        Messages.put("clone.no_free_space",
                "No free area found for the build - the room is completely built up",
                "Keine freie Fl\u00e4che f\u00fcr den Aufbau gefunden - der Raum ist komplett zugebaut");
        Messages.put("clone.no_furni_rights",
                "You have no rights to move furni in this room",
                "Du hast in diesem Raum keine Rechte, M\u00f6bel zu bewegen");
        Messages.put("clone.no_room",
                "No room detected - walk out once and back in",
                "Kein Raum erkannt - lauf einmal raus und wieder rein");
        Messages.put("clone.reserved_space.fallback",
                "All free tiles get built on - using %d,%d as scratch space, a furni can end up in the wrong place there",
                "Alle freien Felder werden bebaut - nutze %d,%d als Zwischenablage, dort kann ein M\u00f6bel sp\u00e4ter falsch liegen");
        Messages.put("clone.source_height_offset",
                "Height offset of the source room: %d",
                "H\u00f6hen-Offset des Quellraums: %d");
        Messages.put("clone.start.header",
                "=== Room cloning started ===",
                "=== Raum klonen gestartet ===");
        Messages.put("clone.status.aborted",
                "Cancelled - see log",
                "Abgebrochen - siehe Log");
        Messages.put("clone.status.cancel_requested",
                "Cancel requested",
                "Abbruch angefordert");
        Messages.put("clone.status.done",
                "Done",
                "Fertig");
        Messages.put("clone.status.ready",
                "Ready",
                "Bereit");
        Messages.put("clone.status.running",
                "Running...",
                "L\u00e4uft...");
        Messages.put("floorplan.verified",
                "Floor plan verified: the room really is %sx%s with %s walkable tiles",
                "Floorplan gepr\u00fcft: der Raum ist wirklich %sx%s mit %s begehbaren Kacheln");
        Messages.put("floorplan.mismatch",
                "The server kept a different floor plan - sent %sx%s with %s tiles, room has %sx%s with %s. Retrying (%s/%s)",
                "Der Server behielt einen anderen Floorplan - gesendet %sx%s mit %s Kacheln, Raum hat %sx%s mit %s. Neuer Versuch (%s/%s)");
        Messages.put("floorplan.never_applied",
                "The floor plan was not applied. Without it the room keeps its creation layout and furni cannot be placed on tiles that do not exist",
                "Der Floorplan wurde nicht \u00fcbernommen. Ohne ihn beh\u00e4lt der Raum sein Erstell-Layout, und M\u00f6bel k\u00f6nnen nicht auf Kacheln gesetzt werden, die es nicht gibt");
        Messages.put("floorplan.applied",
                "Floor plan applied",
                "Floorplan \u00fcbernommen");
        Messages.put("floorplan.not_confirmed",
                "The server did not confirm the floor plan - it may be too large for the chosen room model (%s), or the floor plan editor is locked for this account",
                "Der Server hat den Floorplan nicht best\u00e4tigt - eventuell ist er zu gro\u00df f\u00fcr das gew\u00e4hlte Raum-Modell (%s) oder der Floorplan-Editor ist f\u00fcr diesen Account gesperrt");
        Messages.put("floorplan.send_failed",
                "UpdateFloorProperties could not be sent",
                "UpdateFloorProperties konnte nicht gesendet werden");
        Messages.put("floorplan.state_incomplete",
                "Room state incomplete after the floor plan update - waiting a moment",
                "Raum-State nach dem Floorplan-Update unvollst\u00e4ndig - kurz warten");
        Messages.put("floorplan.transfer",
                "Transferring the floor plan (%dx%d, %d tiles, door %d,%d dir %d, wall height %d, thickness %d/%d)",
                "\u00dcbertrage den Floorplan (%dx%d, %d Felder, T\u00fcr %d,%d dir %d, Wandh\u00f6he %d, Dicken %d/%d)");
    }

    private static void fill2() {
        Messages.put("floorplan.wallheight_default",
                "Source room reports wall height -1 (default); 0 is written instead because the server silently discards -1",
                "Quellraum meldet Wandh\u00f6he -1 (Standard); geschrieben wird 0, weil der Server -1 stumm verwirft");
        Messages.put("inventory.already_loaded",
                "Inventory is loaded, no reload needed",
                "Inventar ist geladen, kein Neuladen n\u00f6tig");
        Messages.put("inventory.availability.failed",
                "Availability check failed, check if everything is loaded",
                "Verf\u00fcgbarkeitspr\u00fcfung fehlgeschlagen, pr\u00fcfe ob alles geladen ist");
        Messages.put("inventory.availability.header",
                "Required furni: ",
                "Ben\u00f6tigte M\u00f6bel: ");
        Messages.put("inventory.availability.item.count",
                "(%d/%d)",
                "(%d/%d)");
        Messages.put("inventory.availability.item.name",
                "* %s ",
                "* %s ");
        Messages.put("inventory.donate.aborted",
                "Aborted auto donate",
                "Auto-Donate abgebrochen");
        Messages.put("inventory.donate.all_started",
                "Donating all, wait for it to finish",
                "Es wird alles gespendet, bitte warten");
        Messages.put("inventory.donate.already_running",
                "Already donating, wait for it to finish, or say :abort in game!",
                "Es wird schon gespendet - warte bis es fertig ist oder sag :abort im Spiel!");
        Messages.put("inventory.donate.finished",
                "Donated all (donatable) floor items",
                "Alle (spendbaren) Bodenm\u00f6bel gespendet");
        Messages.put("inventory.donate.inventory_not_loaded",
                "Failed to start auto donate, inventory is not loaded",
                "Auto-Donate konnte nicht gestartet werden, das Inventar ist nicht geladen");
        Messages.put("inventory.donate.missing_started",
                "Donating missing furni, wait for it to finish",
                "Fehlende M\u00f6bel werden gespendet, bitte warten");
        Messages.put("inventory.donate.no_preset",
                "Failed to start auto donate, no preset selected",
                "Auto-Donate konnte nicht gestartet werden, kein Preset ausgew\u00e4hlt");
        Messages.put("inventory.donate.progress",
                "Donated %d/%d floor items",
                "%d/%d Bodenm\u00f6bel gespendet");
        Messages.put("inventory.donate.wait_before_manual",
                "Please wait for the auto donate to finish before donating yourself furni!",
                "Bitte warte, bis Auto-Donate fertig ist, bevor du dir selbst M\u00f6bel schenkst!");
        Messages.put("inventory.loaded",
                "Inventory loaded: found %d items.",
                "Inventar geladen: %d Items gefunden.");
        Messages.put("inventory.loaded.duration",
                "Inventory loaded (%d s)",
                "Inventar geladen (%d s)");
        Messages.put("inventory.loading",
                "Loading inventory...",
                "Inventar wird geladen...");
        Messages.put("inventory.loading.long_wait",
                "Loading the inventory - with a large inventory this takes a few minutes",
                "Lade das Inventar - bei einem gro\u00dfen Inventar dauert das ein paar Minuten");
        Messages.put("inventory.timeout",
                "The inventory did not finish within 5 minutes - press \"Load inventory\", wait until \"Inventory read\" is green, then start the clone again",
                "Das Inventar wurde in 5 Minuten nicht fertig - dr\u00fccke \"Inventar laden\", warte bis \"Inventar gelesen\" gr\u00fcn ist, und starte den Klon erneut");
        Messages.put("inventory.updating",
                "Updating inventory...",
                "Inventar wird aktualisiert...");
        Messages.put("inventory.waiting",
                "... still waiting for the inventory (%d s, status %s)",
                "... warte weiter auf das Inventar (%d s, Status %s)");
        Messages.put("preset.availability.not_ready",
                "No preset chosen or furnidata not ready",
                "Kein Preset gew\u00e4hlt oder Furnidata noch nicht geladen");
        Messages.put("preset.dimensions",
                "Preset dimensions: %dx%d",
                "Preset-Gr\u00f6\u00dfe: %dx%d");
        Messages.put("preset.donate.busy.export",
                "Can't autodonate while export is in progress",
                "Autodonate ist w\u00e4hrend eines laufenden Exports nicht m\u00f6glich");
        Messages.put("preset.donate.busy.import",
                "Can't autodonate while import is in progress",
                "Autodonate ist w\u00e4hrend eines laufenden Aufbaus nicht m\u00f6glich");
        Messages.put("preset.export.aborted",
                "Aborted preset export",
                "Preset-Export abgebrochen");
        Messages.put("preset.export.already_running",
                "Already exporting a preset.. finish up or abort first",
                "Es wird schon ein Preset exportiert.. erst fertigstellen oder abbrechen");
        Messages.put("preset.export.already_running.log",
                "An export is already running",
                "Es l\u00e4uft schon ein Export");
        Messages.put("preset.export.cancelled",
                "Export cancelled",
                "Export abgebrochen");
        Messages.put("preset.export.enter_name",
                "Enter the name of the preset",
                "Gib den Namen des Presets ein");
        Messages.put("preset.export.error.invalid_characters",
                "Invalid characters in name, don't use the following characters: &lt; &gt; : / \\ | ? *",
                "Ung\u00fcltige Zeichen im Namen, verwende diese Zeichen nicht: &lt; &gt; : / \\ | ? *");
        Messages.put("preset.export.error.insufficient_resources",
                "ERROR - Couldn't export due to insufficient resources",
                "FEHLER - Export nicht m\u00f6glich, es fehlen Ressourcen");
        Messages.put("preset.export.error.insufficient_resources.log",
                "Couldn't export due to insufficient resources",
                "Export nicht m\u00f6glich, es fehlen Ressourcen");
        Messages.put("preset.export.error.invalid_name",
                "Invalid characters in name, don't use the following characters: &lt; &gt; : / \\ | ? *",
                "Ung\u00fcltige Zeichen im Namen, benutze diese Zeichen nicht: &lt; &gt; : / \\ | ? *");
        Messages.put("preset.export.error.no_floorstate",
                "ERROR - Couldn't export due to missing floor state or furnidata",
                "FEHLER - Export nicht m\u00f6glich, Floorstate oder Furnidata fehlt");
        Messages.put("preset.export.error.no_floorstate.log",
                "Couldn't export due to missing floor state or furnidata",
                "Export nicht m\u00f6glich, Floorstate oder Furnidata fehlt");
        Messages.put("preset.export.error.not_ready",
                "Error: no room detected or furnidata not available",
                "Fehler: Kein Raum erkannt oder Furnidata nicht verf\u00fcgbar");
        Messages.put("preset.export.error.not_ready.log",
                "No room detected or furnidata not loaded",
                "Kein Raum erkannt oder Furnidata nicht geladen");
        Messages.put("preset.export.failed",
                "Export failed - the clone will not be started",
                "Export fehlgeschlagen - der Klon wird nicht gestartet");
        Messages.put("preset.export.select_rect_end",
                "Select the end of the rectangle",
                "W\u00e4hle das Ende des Rechtecks");
        Messages.put("preset.export.select_rect_start",
                "Select the start of the rectangle",
                "W\u00e4hle den Anfang des Rechtecks");
        Messages.put("preset.export.start",
                "Exporting the whole room including wired as preset \"%s\"",
                "Exportiere den ganzen Raum inkl. Wired als Preset \"%s\"");
        Messages.put("preset.export.success",
                "Exported \"%s\" successfully",
                "\"%s\" erfolgreich exportiert");
        Messages.put("preset.export.success.log",
                "Exported preset \"%s\" successfully",
                "Preset \"%s\" erfolgreich exportiert");
        Messages.put("preset.export.timeout",
                "Export has been running for 15 minutes without a result - aborted",
                "Export l\u00e4uft seit 15 Minuten ohne Ergebnis - abgebrochen");
        Messages.put("preset.exported.summary",
                "Preset \"%s\": %d furni",
                "Preset \"%s\": %d M\u00f6bel");
        Messages.put("preset.furni.unknown_typeid",
                "typeId %d",
                "typeId %d");
        Messages.put("preset.hint.build_command",
                "Say :ip in-game to start the build at a clicked spot, or :ip x,y for a fixed position",
                "Sag im Spiel :ip um den Aufbau an einer geklickten Stelle zu starten, oder :ip x,y f\u00fcr eine feste Position");
        Messages.put("preset.import.aborted",
                "Successfully aborted importing",
                "Import erfolgreich abgebrochen");
        Messages.put("preset.import.adding_furni",
                "Adding furni...",
                "M\u00f6bel werden gesetzt...");
        Messages.put("preset.import.ads.setup",
                "Setting up ads backgrounds..",
                "Ads-Hintergr\u00fcnde werden eingerichtet..");
        Messages.put("preset.import.already_running",
                "Already importing a preset.. finish up or abort first",
                "Es wird schon ein Preset importiert.. erst fertigstellen oder abbrechen");
        Messages.put("preset.import.already_running.log",
                "A build is already running",
                "Es l\u00e4uft schon ein Aufbau");
        Messages.put("preset.import.bc_item_missing_continue",
                "Couldn't find the item '%s' in the Builders Club warehouse.. continuing",
                "Item '%s' nicht im Builders Club Lager gefunden.. es geht weiter");
        Messages.put("preset.import.error.bc_item_missing_abort",
                "ERROR: Couldn't find the item '%s' in the Builders Club warehouse.. aborting",
                "FEHLER: Item '%s' nicht im Builders Club Lager gefunden.. Abbruch");
        Messages.put("preset.import.error.inventory_item_missing_abort",
                "ERROR: Couldn't find '%s' in the inventory.. aborting",
                "FEHLER: '%s' nicht im Inventar gefunden.. Abbruch");
        Messages.put("preset.import.error.missing_items",
                "ERROR: Some items were not available, check the availability first!",
                "FEHLER: Einige Items waren nicht verf\u00fcgbar, pr\u00fcfe zuerst die Verf\u00fcgbarkeit!");
        Messages.put("preset.import.error.no_preset",
                "ERROR: No preset selected!",
                "FEHLER: Kein Preset ausgew\u00e4hlt!");
        Messages.put("preset.import.error.not_all_placed",
                "ERROR: not all furni were placed",
                "FEHLER: es wurden nicht alle M\u00f6bel platziert");
        Messages.put("preset.import.error.not_initialized",
                "ERROR: extension not fully initialized yet",
                "FEHLER: Extension ist noch nicht vollst\u00e4ndig initialisiert");
    }

    private static void fill3() {
        Messages.put("preset.import.error.resources_unavailable",
                "ERROR: Inventory, catalog or furnidata is unavailable",
                "FEHLER: Inventar, Katalog oder Furnidata ist nicht verf\u00fcgbar");
        Messages.put("preset.import.error.select_preset_first",
                "ERROR: select the preset first",
                "FEHLER: erst das Preset ausw\u00e4hlen");
        Messages.put("preset.import.finished",
                "Finished importing the preset!",
                "Import des Presets abgeschlossen!");
        Messages.put("preset.import.inventory_item_missing_continue",
                "Couldn't find '%s' in the inventory.. continuing",
                "'%s' nicht im Inventar gefunden.. es geht weiter");
        Messages.put("preset.import.unavailable.header",
                "%d furni could not be placed because they are not available:",
                "%d M\u00f6bel konnten nicht platziert werden, weil sie nicht verf\u00fcgbar sind:");
        Messages.put("preset.import.unavailable.hint",
                "Nothing was sent for these - they have no Builders Club offer and none are in your inventory. Put them in your inventory or switch the item source.",
                "F\u00fcr diese wurde nichts gesendet - sie haben kein Builders-Club-Angebot und liegen nicht im Inventar. Leg sie ins Inventar oder \u00e4ndere die M\u00f6bel-Quelle.");
        Messages.put("preset.import.rejected.header",
                "%d furni were rejected by the server while being placed:",
                "%d M\u00f6bel hat der Server beim Platzieren abgelehnt:");
        Messages.put("preset.import.rejected.item",
                "%s at %d,%d",
                "%s auf %d,%d");
        Messages.put("preset.import.rejected.hint",
                "In game this shows as \"Sorry, you cannot place this item here\" - the tile is blocked by something that carries nothing on top.",
                "Im Spiel erscheint das als \"Sorry, you cannot place this item here\" - auf der Kachel liegt etwas, das nichts auf sich tragen kann.");
        Messages.put("preset.import.missing.header",
                "%d furni missing:",
                "Es fehlen %d M\u00f6bel:");
        Messages.put("preset.import.missing.hint",
                "If the furni are missing from inventory/BC: set Item source to \"Prefer BC\". If they are there, the server swallowed individual drops - raise the Ratelimit or tick \"Allow building without all furni\"",
                "Wenn die M\u00f6bel im Inventar/BC fehlen: M\u00f6bel-Quelle auf \"BC bevorzugen\" stellen. Wenn sie da sind, hat der Server einzelne Drops verschluckt - dann Ratelimit hochziehen oder \"Aufbau auch ohne alle M\u00f6bel erlauben\" anhaken");
        Messages.put("preset.import.missing.item",
                "   %s x%d",
                "   %s x%d");
        Messages.put("preset.import.missing.unknown_class",
                "typeId ?",
                "typeId ?");
        Messages.put("preset.import.missing_items_continue",
                "Some items were not available, building anyways..",
                "Einige Items waren nicht verf\u00fcgbar, es wird trotzdem gebaut..");
        Messages.put("preset.import.moving_furni",
                "Setting furni in their correct position..",
                "M\u00f6bel werden an ihre richtige Position gesetzt..");
        Messages.put("preset.import.no_drop",
                "Can't drop furni while the importer is active, please await the procedure or abort",
                "W\u00e4hrend der Importer l\u00e4uft, k\u00f6nnen keine M\u00f6bel platziert werden - bitte warte ab oder brich ab");
        Messages.put("preset.import.no_furni_adjust",
                "Don't adjust furni while the importer is active!",
                "Keine M\u00f6bel ver\u00e4ndern, w\u00e4hrend der Importer l\u00e4uft!");
        Messages.put("preset.import.not_ready.generic",
                "Extension not ready yet",
                "Extension noch nicht bereit");
        Messages.put("preset.import.not_ready.no_furni_rights",
                "No rights to move furni",
                "Keine Rechte zum M\u00f6belbewegen");
        Messages.put("preset.import.not_ready.no_furnidata",
                "Furnidata not loaded yet",
                "Furnidata noch nicht geladen");
        Messages.put("preset.import.not_ready.no_inventory",
                "Inventory not loaded yet",
                "Inventar noch nicht geladen");
        Messages.put("preset.import.not_ready.no_room",
                "No room detected",
                "Kein Raum erkannt");
        Messages.put("preset.import.not_ready.no_stacktile",
                "No stack tile in the room",
                "Kein Stacktile im Raum");
        Messages.put("preset.import.not_ready.no_wired_rights",
                "No rights for wired",
                "Keine Rechte f\u00fcr Wired");
        Messages.put("preset.import.progress.ingame",
                "%s (%s/%s) - %s%% done",
                "%s (%s/%s) - %s%% fertig");
        Messages.put("preset.import.flat_conflict",
                "Tile %d,%d: nothing can be placed on %s, so %s will be rejected there",
                "Kachel %d,%d: auf %s kann nichts liegen, %s wird dort abgelehnt");
        Messages.put("preset.import.progress.format",
                "%s (%d/%d)",
                "%s (%d/%d)");
        Messages.put("preset.import.progress.move_furni",
                "Moving furni into place",
                "R\u00fccke M\u00f6bel an ihren Platz");
        Messages.put("preset.import.progress.place_furni",
                "Placing furni",
                "Setze M\u00f6bel");
        Messages.put("preset.import.progress.place_multitile",
                "Placing multi-tile floor furni",
                "Setze Multi-Tile-B\u00f6den");
        Messages.put("preset.import.select_free_space",
                "Select unoccupied space in the room",
                "W\u00e4hle eine freie Fl\u00e4che im Raum");
        Messages.put("preset.import.select_root_location",
                "Select where the preset should be imported",
                "W\u00e4hle, wo das Preset aufgebaut werden soll");
        Messages.put("preset.import.started",
                "Build started at %d,%d (free space %d,%d, height offset %d)",
                "Aufbau gestartet bei %d,%d (Freifl\u00e4che %d,%d, H\u00f6hen-Offset %d)");
        Messages.put("preset.import.success",
                "Imported the preset successfully",
                "Preset erfolgreich importiert");
        Messages.put("preset.load_failed.after_export",
                "The preset that was just exported could not be loaded",
                "Das gerade exportierte Preset konnte nicht geladen werden");
        Messages.put("preset.load_failed.named",
                "Preset \"%s\" could not be loaded",
                "Preset \"%s\" konnte nicht geladen werden");
        Messages.put("preset.name.fallback",
                "Room",
                "Raum");
        Messages.put("preset.file_missing",
                "The preset file \"%s\" no longer exists",
                "Die Preset-Datei \"%s\" gibt es nicht mehr");
        Messages.put("preset.open.unsupported",
                "This system cannot open files from here - the folder is %s",
                "Dieses System kann von hier keine Dateien \u00f6ffnen - der Ordner ist %s");
        Messages.put("preset.open.folder_instead",
                "No editor is registered for \"%s\" - opened the folder instead",
                "F\u00fcr \"%s\" ist kein Editor hinterlegt - stattdessen den Ordner ge\u00f6ffnet");
        Messages.put("preset.open.failed",
                "Could not open %s",
                "%s konnte nicht ge\u00f6ffnet werden");
        Messages.put("wired.cache.cleared",
                "Wired cache cleared (%d entries)",
                "Wired-Cache geleert (%d Eintr\u00e4ge)");
        Messages.put("wired.cache.busy",
                "Wired configurations are being fetched right now - the cache stays as it is",
                "Wired-Konfigurationen werden gerade geholt - der Cache bleibt wie er ist");
        Messages.put("ui.button.importpreset",
                "Import preset...",
                "Preset importieren...");
        Messages.put("preset.import.floorplan.load",
                "Load from file...",
                "Aus Datei laden...");
        Messages.put("preset.import.floorplan.prompt",
                "Paste the floor plan here - one row per line, x is blocked, 0 is walkable",
                "Floorplan hier einf\u00fcgen - eine Zeile pro Reihe, x ist gesperrt, 0 begehbar");
        Messages.put("preset.import.status.empty",
                "No floor plan - the preset is saved without one",
                "Kein Floorplan - das Preset wird ohne gespeichert");
        Messages.put("preset.import.status.invalid",
                "This is not a floor plan yet - expected lines of x and 0",
                "Das ist noch kein Floorplan - erwartet werden Zeilen aus x und 0");
        Messages.put("preset.import.status.ok",
                "%sx%s, %s walkable tiles, door %s,%s (%s)",
                "%sx%s, %s begehbare Kacheln, T\u00fcr %s,%s (%s)");
        Messages.put("preset.import.status.door_rule",
                "%sx%s, %s walkable tiles - but the first row has %s and the first column %s. Habbo allows at most one, the server will refuse this plan",
                "%sx%s, %s begehbare Kacheln - aber die erste Zeile hat %s und die erste Spalte %s. Habbo erlaubt h\u00f6chstens eine, der Server wird den Plan ablehnen");
        Messages.put("preset.import.status.snapshot",
                "Room snapshot \"%s\" is used as it is",
                "Raum-Snapshot \"%s\" wird unver\u00e4ndert benutzt");
        Messages.put("preset.import.status.unreadable",
                "\"%s\" could not be read",
                "\"%s\" konnte nicht gelesen werden");
        Messages.put("preset.import.error.plan_invalid",
                "The floor plan cannot be read - remove it or correct it",
                "Der Floorplan ist nicht lesbar - entferne ihn oder korrigiere ihn");
        Messages.put("preset.import.title",
                "Import a preset",
                "Preset importieren");
        Messages.put("preset.import.choose",
                "Choose the preset file",
                "Preset-Datei w\u00e4hlen");
        Messages.put("preset.import.filter.preset",
                "Preset file (*.json)",
                "Preset-Datei (*.json)");
        Messages.put("preset.import.filter.floorplan",
                "Floor plan (*.txt, *.roomJson)",
                "Floorplan (*.txt, *.roomJson)");
        Messages.put("preset.import.filter.all",
                "All files",
                "Alle Dateien");
        Messages.put("preset.import.source",
                "\"%s\" contains %s furni",
                "\"%s\" enth\u00e4lt %s M\u00f6bel");
        Messages.put("preset.import.name",
                "Save as",
                "Speichern als");
        Messages.put("preset.import.floorplan",
                "Floor plan (optional)",
                "Floorplan (optional)");
        Messages.put("preset.import.floorplan.none",
                "none chosen",
                "keiner gew\u00e4hlt");
        Messages.put("preset.import.floorplan.choose",
                "Choose...",
                "W\u00e4hlen...");
        Messages.put("preset.import.floorplan.clear",
                "Remove",
                "Entfernen");
        Messages.put("preset.import.floorplan.title",
                "Choose the floor plan file",
                "Floorplan-Datei w\u00e4hlen");
        Messages.put("preset.import.save",
                "Import",
                "Importieren");
        Messages.put("preset.import.note",
                "Load the plan from a file or paste it straight in. Without one the preset can still be built into a room you are standing in; creating a room for it falls back to a plain rectangle.",
                "Plan aus einer Datei laden oder direkt hineinkopieren. Ohne einen l\u00e4sst sich das Preset weiterhin in einen Raum bauen, in dem du stehst; \"Raum anlegen + aufbauen\" f\u00e4llt dann auf ein schlichtes Rechteck zur\u00fcck.");
        Messages.put("preset.import.error.name_empty",
                "Enter a name for the preset",
                "Gib dem Preset einen Namen");
        Messages.put("preset.import.error.unreadable",
                "\"%s\" could not be read as a preset (%s)",
                "\"%s\" konnte nicht als Preset gelesen werden (%s)");
        Messages.put("preset.import.error.no_furni",
                "\"%s\" contains no furni",
                "\"%s\" enth\u00e4lt keine M\u00f6bel");
        Messages.put("preset.import.error.write",
                "Could not write the preset (%s)",
                "Preset konnte nicht geschrieben werden (%s)");
        Messages.put("preset.import.done",
                "Preset \"%s\" imported with %s furni",
                "Preset \"%s\" importiert, %s M\u00f6bel");
        Messages.put("preset.import.floorplan.parsed",
                "Floor plan read: %sx%s with %s walkable tiles, door %s,%s (%s)",
                "Floorplan gelesen: %sx%s mit %s begehbaren Kacheln, T\u00fcr %s,%s (%s)");
        Messages.put("preset.import.floorplan.snapshot",
                "Floor plan taken from the room snapshot \"%s\"",
                "Floorplan aus dem Raum-Snapshot \"%s\" " + "" + "genommen");
        Messages.put("preset.import.floorplan.unreadable",
                "\"%s\" could not be read (%s)",
                "\"%s\" konnte nicht gelesen werden (%s)");
        Messages.put("preset.import.floorplan.unparsable",
                "\"%s\" is not a floor plan - expected rows of x and 0",
                "\"%s\" ist kein Floorplan - erwartet werden Zeilen aus x und 0");
        Messages.put("preset.import.floorplan.no_plan",
                "\"%s\" contains no floor plan",
                "\"%s\" enth\u00e4lt keinen Floorplan");
        Messages.put("preset.import.floorplan.skipped",
                "The preset was saved, only the floor plan was left out",
                "Das Preset wurde gespeichert, nur der Floorplan blieb weg");
        Messages.put("preset.import.floorplan.door_rule",
                "Careful: the first row has %s and the first column %s walkable tiles. Habbo allows at most one - the server will refuse this plan",
                "Achtung: die erste Zeile hat %s und die erste Spalte %s begehbare Kacheln. Habbo erlaubt h\u00f6chstens eine - der Server wird diesen Plan ablehnen");
        Messages.put("preset.import.door.ISOLATED_TILE",
                "single tile with no neighbours",
                "einzelne Kachel ohne Nachbarn");
        Messages.put("preset.import.door.DEAD_END",
                "only dead end in the plan",
                "einzige Sackgasse im Plan");
        Messages.put("preset.import.door.FIRST_WALKABLE",
                "no clear door found, first walkable tile used",
                "keine eindeutige T\u00fcr gefunden, erste begehbare Kachel benutzt");
        Messages.put("preset.list.entry",
                "%s  -  %s furni",
                "%s  -  %s M\u00f6bel");
        Messages.put("preset.list.entry_wired",
                "%s  -  %s furni, %s wired",
                "%s  -  %s M\u00f6bel, %s Wired");
        Messages.put("preset.estimate.contents",
                "This preset holds %s furni and %s wired configurations",
                "Dieses Preset umfasst %s M\u00f6bel und %s Wired-Konfigurationen");
        Messages.put("preset.estimate.placing",
                "Placing them takes roughly %s",
                "Das Platzieren dauert ungef\u00e4hr %s");
        Messages.put("preset.estimate.total",
                "Whole build roughly %s at a rate limit of %s ms, %s. Waiting for the server is not included",
                "Gesamter Aufbau ungef\u00e4hr %s bei Ratelimit %s ms, %s. Wartezeiten des Servers sind nicht eingerechnet");
        Messages.put("preset.estimate.source.bc",
                "from Builders Club",
                "aus dem Builders Club");
        Messages.put("preset.estimate.source.inventory",
                "from the inventory",
                "aus dem Inventar");
        Messages.put("preset.none_selected",
                "No preset selected",
                "Kein Preset ausgew\u00e4hlt");
        Messages.put("preset.selected",
                "Selected \"%s\" preset",
                "Preset \"%s\" ausgew\u00e4hlt");
        Messages.put("room.create.confirmation_packet",
                "FlatCreated: %s",
                "FlatCreated: %s");
        Messages.put("room.create.header_unresolvable",
                "CreateFlat cannot be resolved in this client - the room cannot be created",
                "CreateFlat ist in diesem Client nicht aufl\u00f6sbar - Raum kann nicht erstellt werden");
        Messages.put("room.create.implausible_id",
                "FlatCreated contained no plausible room ID (%d)",
                "FlatCreated enthielt keine plausible Raum-ID (%d)");
        Messages.put("room.create.name_mismatch",
                "Note: FlatCreated reports the name \"%s\", expected was \"%s\" - the field layout may differ, ID %d is used anyway",
                "Hinweis: FlatCreated meldet den Namen \"%s\", erwartet war \"%s\" - das Feldlayout kann abweichen, ID %d wird trotzdem benutzt");
        Messages.put("room.create.new_id",
                "New room has the ID %d",
                "Neuer Raum hat die ID %d");
        Messages.put("room.create.no_confirmation",
                "The server sent no FlatCreated after several tries - the room was not created. Most likely Habbo is throttling room creation right now; wait a minute and try again",
                "Der Server hat auch nach mehreren Versuchen kein FlatCreated geschickt - der Raum wurde nicht erstellt. Sehr wahrscheinlich drosselt Habbo gerade das Erstellen von R\u00e4umen; warte eine Minute und versuch es nochmal");
        Messages.put("room.create.read_failed",
                "FlatCreated could not be read: %s",
                "FlatCreated konnte nicht gelesen werden: %s");
        Messages.put("room.create.send_failed",
                "CreateFlat could not be sent",
                "CreateFlat konnte nicht gesendet werden");
        Messages.put("room.create.started",
                "Creating room \"%s\" (model %s, category %d, max %d, trade %d)",
                "Erstelle Raum \"%s\" (Modell %s, Kategorie %d, max %d, Trade %d)");
        Messages.put("room.enter.failed",
                "The new room (ID %d) exists but could not be entered automatically - go in manually",
                "Der neue Raum (ID %d) existiert, konnte aber nicht automatisch betreten werden - geh manuell rein");
        Messages.put("room.enter.header_unresolvable",
                "OpenFlatConnection cannot be resolved in this client - enter the room manually and start the build again",
                "OpenFlatConnection ist in diesem Client nicht aufl\u00f6sbar - gehe manuell in den Raum und starte den Aufbau erneut");
        Messages.put("room.enter.no_room_ready",
                "No RoomReady - the room switch did not work",
                "Kein RoomReady - der Raumwechsel hat nicht funktioniert");
        Messages.put("room.enter.packets_missing",
                "Room entered, but floor plan/furni packets are missing - wait a moment and try again",
                "Raum betreten, aber Floorplan-/M\u00f6bel-Pakete fehlen - kurz warten und erneut versuchen");
        Messages.put("room.enter.send_failed",
                "OpenFlatConnection could not be sent",
                "OpenFlatConnection konnte nicht gesendet werden");
        Messages.put("room.enter.success",
                "Entered the new room",
                "Neuer Raum betreten");
        Messages.put("room.leaving",
                "Leaving room..",
                "Raum wird verlassen..");
        Messages.put("room.quota.header_unresolvable",
                "CanCreateRoom cannot be resolved in this client, room limit unknown",
                "CanCreateRoom ist in diesem Client nicht aufl\u00f6sbar, Raumlimit unbekannt");
        Messages.put("room.quota.no_response",
                "No answer to CanCreateRoom, room limit unknown",
                "Keine Antwort auf CanCreateRoom, Raumlimit unbekannt");
        Messages.put("room.quota.response",
                "Room limit answer: %s",
                "Raumlimit-Antwort: %s");
        Messages.put("room.rights.missing",
                "Still missing in the new room: %s%s%s - the server did not send the rights packets. Walk out once and back in, then build the preset by hand in the Presets tab",
                "Im neuen Raum fehlen noch: %s%s%s - der Server hat die Rechte-Pakete nicht geschickt. Lauf einmal raus und wieder rein, dann Preset im Presets-Tab von Hand aufbauen");
        Messages.put("room.rights.missing.furni",
                "furni rights ",
                "M\u00f6bel-Rechte ");
        Messages.put("room.rights.missing.room_state",
                "room state ",
                "Raum-State ");
        Messages.put("room.rights.missing.wired",
                "wired rights",
                "Wired-Rechte");
        Messages.put("room.rights.waiting",
                "Waiting for room state and rights in the new room",
                "Warte auf Raum-State und Rechte im neuen Raum");
        Messages.put("settings.applied",
                "Room settings applied",
                "Raum-Settings \u00fcbernommen");
        Messages.put("settings.door_password_warning",
                "The source room has a door password - it cannot be read out. The new room is set to \"open\", please add the password yourself",
                "Der Quellraum hat ein T\u00fcr-Passwort - das l\u00e4sst sich nicht mitlesen. Der neue Raum wird auf \"offen\" gesetzt, Passwort bitte selbst nachtragen");
        Messages.put("settings.error.implausible_tag_count",
                "implausible tag count %d",
                "unplausible Tag-Anzahl %d");
        Messages.put("settings.get.send_failed",
                "GetRoomSettings could not be sent",
                "GetRoomSettings konnte nicht gesendet werden");
    }

    private static void fill4() {
        Messages.put("settings.header_unresolvable",
                "GetRoomSettings cannot be resolved in this client",
                "GetRoomSettings ist in diesem Client nicht aufl\u00f6sbar");
        Messages.put("settings.loaded",
                "Full room settings read (flood %d, idle %d/%d, rights %d/%d/%d)",
                "Vollst\u00e4ndige Raum-Settings gelesen (Flood %d, Idle %d/%d, Rechte %d/%d/%d)");
        Messages.put("settings.namesuffix.default",
                " (Copy)",
                " (Kopie)");
        Messages.put("settings.no_confirmation",
                "No confirmation for the room settings - continuing anyway",
                "Keine Best\u00e4tigung f\u00fcr die Raum-Settings - mache trotzdem weiter");
        Messages.put("settings.no_response",
                "No RoomSettingsData answer - only the basic settings are transferred (GetRoomSettings only works for your own rooms)",
                "Keine RoomSettingsData-Antwort - nur die Basis-Settings werden \u00fcbertragen (GetRoomSettings geht nur f\u00fcr eigene R\u00e4ume)");
        Messages.put("settings.read_failed",
                "RoomSettingsData could not be read (%s) - only basic settings are transferred",
                "RoomSettingsData konnte nicht gelesen werden (%s) - nur Basis-Settings werden \u00fcbertragen");
        Messages.put("settings.rejected",
                "The server rejected the room settings: %s",
                "Der Server hat die Raum-Settings abgelehnt: %s");
        Messages.put("settings.save.send_failed",
                "SaveRoomSettings could not be sent",
                "SaveRoomSettings konnte nicht gesendet werden");
        Messages.put("settings.transfer.basic",
                "Transferring the basic room settings (idle / door tile values at Habbo default)",
                "\u00dcbertrage die Basis-Raum-Settings (Idle-/Doortile-Werte auf Habbo-Standard)");
        Messages.put("settings.transfer.full",
                "Transferring the full room settings",
                "\u00dcbertrage die vollst\u00e4ndigen Raum-Settings");
        Messages.put("stacktile.already_present",
                "Stack tile is already in the room",
                "Stacktile ist schon im Raum");
        Messages.put("stacktile.detected_types",
                "Detected %d available types of stack tiles",
                "%d verf\u00fcgbare Stacktile-Typen erkannt");
        Messages.put("stacktile.missing_in_state",
                "Stack tile was placed but does not show up in the room state",
                "Stacktile wurde gesetzt, taucht aber nicht im Raum-State auf");
        Messages.put("stacktile.no_spot",
                "No suitable spot for the stack tile found in the new room",
                "Kein passender Platz f\u00fcr das Stacktile im neuen Raum gefunden");
        Messages.put("stacktile.not_confirmed",
                "The server did not confirm the stack tile",
                "Der Server hat das Stacktile nicht best\u00e4tigt");
        Messages.put("stacktile.not_in_furnidata",
                "Stack tile \"%s\" is not in the furnidata",
                "Stacktile \"%s\" ist nicht in der Furnidata");
        Messages.put("preset.selected.none",
                "No preset selected",
                "Kein Preset ausgew\u00e4hlt");
        Messages.put("preset.selected.log",
                "Selected preset \"%s\"",
                "Preset \"%s\" ausgew\u00e4hlt");
        Messages.put("ui.checkbox.autostacktile",
                "Temporary stack tile in existing rooms",
                "Tempor\u00e4res Stacktile in bestehenden R\u00e4umen");
        Messages.put("ui.hint.workannexhere",
                "For \"Build preset here\": hangs walkable tiles next to the room, keeps stack tile and work tile there, and removes everything afterwards. Your floor plan is restored unchanged.",
                "F\u00fcr \"Preset hier aufbauen\": h\u00e4ngt begehbare Kacheln neben den Raum, h\u00e4lt Stacktile und Arbeitskachel dort und r\u00e4umt danach alles weg. Dein Grundriss wird unver\u00e4ndert wiederhergestellt.");
        Messages.put("ui.hint.presetplanhere",
                "Builds the preset exactly as designed - but replaces the floor plan of the room you are in. Without this option your floor plan stays untouched.",
                "Baut das Preset genau wie entworfen - ersetzt daf\u00fcr aber den Grundriss des Raums, in dem du stehst. Ohne diese Option bleibt dein Grundriss unangetastet.");
        Messages.put("ui.hint.autostacktile",
                "Fallback when the work area is switched off: places the stack tile inside the room and picks it up afterwards.",
                "Ersatz, wenn der Arbeitsbereich aus ist: legt das Stacktile im Raum selbst hin und sammelt es danach wieder ein.");
        Messages.put("ui.label.buildhereposition",
                "Position",
                "Position");
        Messages.put("buildhere.no_area",
                "No free %dx%d area in this room - enter a position to build anyway",
                "Keine freie Fl\u00e4che %dx%d in diesem Raum - gib eine Position an, um trotzdem zu bauen");
        Messages.put("buildhere.position_outside",
                "Position %d,%d is not a tile of this room",
                "Position %d,%d ist keine Kachel dieses Raums");
        Messages.put("buildhere.error",
                "Build here failed: %s",
                "Aufbau hier fehlgeschlagen: %s");
        Messages.put("preset.selected.summary",
                "Selected: %s  -  %dx%d, %d furni",
                "Gew\u00e4hlt: %s  -  %dx%d, %d M\u00f6bel");
        Messages.put("buildhere.starting",
                "Building here: root %d,%d - work tile %d,%d",
                "Baue hier: Ursprung %d,%d - Arbeitskachel %d,%d");
        Messages.put("buildhere.loading_inventory",
                "Loading inventory first...",
                "Lade zuerst das Inventar...");
        Messages.put("buildhere.title",
                "Building \"%s\" into the current room",
                "Baue \"%s\" in den aktuellen Raum");
        Messages.put("buildhere.no_room_data",
                "Could not read the current room",
                "Der aktuelle Raum konnte nicht gelesen werden");
        Messages.put("buildhere.annex_unavailable",
                "No temporary work area fits next to this room - switch the option off to build with a stack tile inside the room",
                "Neben diesen Raum passt keine tempor\u00e4re Arbeitsfl\u00e4che - schalte die Option aus, um mit einem Stacktile im Raum zu bauen");
        Messages.put("buildhere.plan_adopted",
                "Adopting the floor plan stored with the preset: %dx%d, %d tiles - this replaces the floor plan of this room",
                "Grundriss des Presets wird \u00fcbernommen: %dx%d, %d Kacheln - das ersetzt den Grundriss dieses Raums");
        Messages.put("buildhere.plan_available",
                "This preset carries its own floor plan. Enable \"Adopt the floor plan stored with the preset\" to build it exactly as designed.",
                "Dieses Preset bringt einen eigenen Grundriss mit. Aktiviere \"Grundriss des Presets \u00fcbernehmen\", um es genau wie entworfen aufzubauen.");
        Messages.put("ui.checkbox.workannexhere",
                "Temporary work area in existing rooms",
                "Tempor\u00e4rer Arbeitsbereich in bestehenden R\u00e4umen");
        Messages.put("ui.checkbox.presetplanhere",
                "Adopt the floor plan stored with the preset",
                "Grundriss des Presets \u00fcbernehmen");
        Messages.put("buildhere.bad_position",
                "Position must look like x,y - leave empty to choose automatically",
                "Position muss wie x,y aussehen - leer lassen f\u00fcr automatische Wahl");
        Messages.put("buildhere.no_stacktile_manual",
                "No stack tile in the room. Enable \"Temporary stack tile in existing rooms\" or place one yourself.",
                "Kein Stacktile im Raum. Aktiviere \"Tempor\u00e4res Stacktile in bestehenden R\u00e4umen\" oder lege selbst eins hin.");
        Messages.put("buildhere.no_space_for_stacktile",
                "No free square found for the stack tile",
                "Kein freies Quadrat f\u00fcr das Stacktile gefunden");
        Messages.put("buildhere.stacktile_failed",
                "Stack tile could not be placed",
                "Stacktile konnte nicht gelegt werden");
        Messages.put("buildhere.no_reserved_space",
                "No free tile found to work from",
                "Keine freie Kachel als Arbeitsplatz gefunden");
        Messages.put("buildhere.timeout",
                "Build stalled - no progress for 2 minutes",
                "Aufbau h\u00e4ngt - seit 2 Minuten kein Fortschritt");
        Messages.put("buildhere.done",
                "Preset built in this room",
                "Preset in diesem Raum aufgebaut");
        Messages.put("buildhere.failed",
                "Preset was not built completely",
                "Preset wurde nicht vollst\u00e4ndig aufgebaut");
        Messages.put("stacktile.pickup.already_gone",
                "Stack tile is no longer in the room",
                "Stacktile ist nicht mehr im Raum");
        Messages.put("stacktile.pickup.done",
                "Stack tile picked up",
                "Stacktile aufgenommen");
        Messages.put("stacktile.pickup.send_failed",
                "PickupObject could not be sent",
                "PickupObject konnte nicht gesendet werden");
        Messages.put("stacktile.pickup.start",
                "Picking the work stack tile back up (ID %d)",
                "Nehme das Arbeits-Stacktile wieder auf (ID %d)");
        Messages.put("stacktile.pickup.still_present",
                "The room state still reports the stack tile as present - the annex teardown decides whether it is really gone",
                "Der Raum-State meldet das Stacktile noch als vorhanden - der Abriss entscheidet, ob es wirklich weg ist");
        Messages.put("stacktile.helpers_placed",
                "%s smaller stack tile(s) placed as well, so furni also fit into tight gaps",
                "%s kleinere Stapelfelder zus\u00e4tzlich gesetzt, damit M\u00f6bel auch in enge L\u00fccken passen");
        Messages.put("stacktile.helper_no_space",
                "No free square for the %s helper stack tile",
                "Kein freies Quadrat f\u00fcr das %s-Hilfs-Stacktile");
        Messages.put("stacktile.helper_unavailable",
                "No %s stack tile available - tight gaps may stay empty",
                "Kein %s Stapelfeld verf\u00fcgbar - enge L\u00fccken bleiben m\u00f6glicherweise leer");
        Messages.put("stacktile.placed",
                "Stack tile is in the room (ID %d)",
                "Stacktile liegt im Raum (ID %d)");
        Messages.put("stacktile.placing",
                "Placing stack tile \"%s\" at %d,%d (%s)",
                "Setze Stacktile \"%s\" auf %d,%d (%s)");
        Messages.put("stacktile.send_failed",
                "Stack tile placement could not be sent",
                "Stacktile-Platzierung konnte nicht gesendet werden");
        Messages.put("stacktile.source.inventory",
                "inventory",
                "Inventar");
        Messages.put("stacktile.spot.multi_tile_conflict",
                "No free spot that is not needed by a multi-tile furni - single drops can fail",
                "Kein Platz frei, der nicht von einem Multi-Tile-M\u00f6bel gebraucht wird - einzelne Drops k\u00f6nnen fehlschlagen");
        Messages.put("stacktile.spot.smaller_area",
                "No %dx%d area free, trying a smaller area",
                "Kein %dx%d-Feld frei, versuche eine kleinere Fl\u00e4che");
        Messages.put("stacktile.spot.will_be_built_on",
                "The stack tile sits on a tile that gets built on later - the furni will be moved then, that is fine",
                "Stacktile liegt auf einem Feld, das sp\u00e4ter bebaut wird - das M\u00f6bel wird dann verschoben, das passt");
        Messages.put("stacktile.unavailable",
                "No stack tile \"%s\" available - put one in your inventory or pick a different stack tile size",
                "Kein Stacktile \"%s\" verf\u00fcgbar - leg eins ins Inventar oder w\u00e4hle eine andere Stacktile-Gr\u00f6\u00dfe");
        Messages.put("ui.button.buildpreset",
                "Build preset here",
                "Preset hier aufbauen");
        Messages.put("ui.button.cancel",
                "Cancel",
                "Abbrechen");
        Messages.put("ui.button.checkavailability",
                "Check availability",
                "Verf\u00fcgbarkeit pr\u00fcfen");
        Messages.put("ui.button.clearwiredcache",
                "Clear wired cache",
                "Wired-Cache leeren");
        Messages.put("ui.button.cloneroom",
                "Copy room + build",
                "Raum kopieren + bauen");
        Messages.put("ui.button.loadinventory",
                "Load inventory",
                "Inventar laden");
        Messages.put("ui.button.openpreset",
                "Open preset in editor",
                "Preset im Editor \u00f6ffnen");
        Messages.put("ui.button.openpresetsfolder",
                "Open presets folder",
                "Presets-Ordner \u00f6ffnen");
        Messages.put("ui.button.reloadpresets",
                "Reload presets",
                "Presets neu laden");
        Messages.put("ui.button.selfdonate",
                "Self-donate furni",
                "Furni selbst spenden");
        Messages.put("ui.button.update",
                "Update",
                "Aktualisieren");
        Messages.put("ui.checkbox.allowincomplete",
                "Allow building without all furni",
                "Aufbau auch ohne alle M\u00f6bel erlauben");
        Messages.put("ui.checkbox.alwaysontop",
                "Always on top",
                "Immer im Vordergrund");
        Messages.put("ui.checkbox.copywallitems",
                "Include wall furni",
                "Wandm\u00f6bel mitnehmen");
        Messages.put("ui.checkbox.noexportwired",
                "Do not export wired",
                "Wired nicht exportieren");
        Messages.put("ui.button.copyroom",
                "Copy room",
                "Raum kopieren");
        Messages.put("clone.export_only.done",
                "Room saved as preset \"%s\" - nothing was built. Use \"Copy room + build\" or the Presets tab for that.",
                "Raum als Preset \"%s\" gespeichert - es wurde nichts gebaut. Daf\u00fcr \"Raum kopieren + bauen\" oder den Presets-Tab benutzen.");
        Messages.put("clone.export_only.announce",
                "Room saved as preset - nothing built",
                "Raum als Preset gespeichert - nichts gebaut");
        Messages.put("ui.checkbox.workannex",
                "Temporary work area in new rooms",
                "Tempor\u00e4rer Arbeitsbereich in neuen R\u00e4umen");
        Messages.put("ui.hint.workannex",
                "Only applies when a room is created.",
                "Gilt nur, wenn ein Raum angelegt wird.");
        Messages.put("ui.contextmenu.remove",
                "Remove",
                "Entfernen");
        Messages.put("ui.extension.description",
                "Clone a room including settings, floor plan, furni and wired into a new room",
                "Raum inkl. Settings, Floorplan, M\u00f6bel und Wired in einen neuen Raum klonen");
        Messages.put("ui.label.buildpresethint",
                "Builds the selected preset in the room you are standing in. Leave the position empty for a free spot, type x,y for a fixed one, or say :ip in game to click the spot yourself.",
                "Baut das gew\u00e4hlte Preset in den Raum, in dem du stehst. Position leer lassen f\u00fcr einen freien Platz, x,y eintippen f\u00fcr eine feste Stelle, oder im Spiel :ip sagen und die Stelle selbst anklicken.");
        Messages.put("ui.label.existingfurni",
                "Existing furni:",
                "Vorhandenes Furni:");
        Messages.put("ui.label.furniname",
                "Furni name:",
                "Furni-Name:");
        Messages.put("ui.label.itemsource",
                "Item source:",
                "M\u00f6bel-Quelle:");
        Messages.put("ui.label.mainstacktile",
                "Main stack tile:",
                "Haupt-Stacktile:");
        Messages.put("ui.label.namesuffix",
                "Name suffix for the copy:",
                "Namens-Zusatz der Kopie:");
        Messages.put("ui.label.ratelimit",
                "Ratelimit:",
                "Ratelimit:");
        Messages.put("ui.label.roommodel",
                "Room model of new rooms:",
                "Raum-Modell neuer R\u00e4ume:");
        Messages.put("ui.label.savedpresets",
                "Saved presets (double-click to select):",
                "Gespeicherte Presets (Doppelklick w\u00e4hlt aus):");
        Messages.put("ui.label.selfdonate",
                "Self-donate:",
                "Selbst spenden:");
        Messages.put("ui.log.chatcommands",
                "Chat commands still work: :ep [all], :ip [x,y], :abort",
                "Chat-Kommandos funktionieren weiterhin: :ep [all], :ip [x,y], :abort");
        Messages.put("ui.log.title",
                "G-PresetsPlus",
                "G-PresetsPlus");
        Messages.put("ui.postconfig.column.existingfurniid",
                "Existing furni ID",
                "Vorhandene Furni-ID");
    }

    private static void fill5() {
        Messages.put("ui.postconfig.column.furniname",
                "Furni name",
                "Furni-Name");
        Messages.put("ui.postconfig.invalidinput",
                "You entered invalid information!",
                "Deine Eingabe ist ung\u00fcltig!");
        Messages.put("ui.radio.donateall",
                "All items in export",
                "Alle M\u00f6bel im Export");
        Messages.put("ui.radio.donatemissing",
                "Only missing items",
                "Nur fehlende M\u00f6bel");
        Messages.put("ui.radio.onlybc",
                "Only BC",
                "Nur BC");
        Messages.put("ui.radio.onlyinventory",
                "Only Inventory",
                "Nur Inventar");
        Messages.put("ui.radio.preferbc",
                "Prefer BC",
                "BC bevorzugen");
        Messages.put("ui.radio.preferinventory",
                "Prefer Inventory",
                "Inventar bevorzugen");
        Messages.put("ui.status.connected",
                "Connected to Habbo",
                "Mit Habbo verbunden");
        Messages.put("ui.status.furnidata",
                "Furnidata loaded",
                "Furnidata geladen");
        Messages.put("ui.status.inventory",
                "Inventory read",
                "Inventar gelesen");
        Messages.put("ui.status.permissions",
                "Permissions granted",
                "Rechte vorhanden");
        Messages.put("ui.status.room",
                "Room detected",
                "Raum erkannt");
        Messages.put("ui.status.stacktile",
                "Stack tile found",
                "Stacktile gefunden");
        Messages.put("ui.tab.clone",
                "Clone room",
                "Raum klonen");
        Messages.put("ui.tab.presetconfig",
                "Preset configuration",
                "Preset-Konfiguration");
        Messages.put("ui.tab.presets",
                "Presets",
                "Presets");
        Messages.put("ui.tab.settings",
                "Settings",
                "Einstellungen");
        Messages.put("ui.window.title",
                "G-PresetsPlus - Building & Wired Presets - %s",
                "G-PresetsPlus - Building & Wired Presets - %s");
        Messages.put("wallitems.aborted",
                "Wall furni aborted",
                "Wandm\u00f6bel abgebrochen");
        Messages.put("wallitems.not_in_furnidata",
                "Wall furni \"%s\" not in the furnidata, skipped",
                "Wandm\u00f6bel \"%s\" nicht in der Furnidata, \u00fcbersprungen");
        Messages.put("wallitems.not_in_inventory",
                "Wall furni \"%s\" not in the inventory (Builders Club placement for wall furni is not supported), skipped",
                "Wandm\u00f6bel \"%s\" nicht im Inventar (BC-Platzierung f\u00fcr Wandm\u00f6bel ist nicht unterst\u00fctzt), \u00fcbersprungen");
        Messages.put("wallitems.place_failed",
                "Wall furni \"%s\" could not be placed",
                "Wandm\u00f6bel \"%s\" konnte nicht platziert werden");
        Messages.put("wallitems.placing",
                "Placing %d wall furni",
                "Setze %d Wandm\u00f6bel");
        Messages.put("wallitems.skipped_cancelled",
                "Wall furni skipped (cancelled)",
                "Wandm\u00f6bel \u00fcbersprungen (abgebrochen)");
        Messages.put("wallitems.summary",
                "Wall furni: %d placed",
                "Wandm\u00f6bel: %d gesetzt");
        Messages.put("wallitems.summary_with_skipped",
                "Wall furni: %d placed, %d skipped",
                "Wandm\u00f6bel: %d gesetzt, %d \u00fcbersprungen");
        Messages.put("wired.export.error.fetch_failed",
                "ERROR - Something went wrong while fetching configurations..",
                "FEHLER - Beim Auslesen der Konfigurationen ist etwas schiefgelaufen..");
        Messages.put("wired.export.error.fetch_failed.log",
                "Something went wrong while fetching configurations..",
                "Beim Auslesen der Konfigurationen ist etwas schiefgelaufen..");
        Messages.put("wired.export.error.missing_configs",
                "ERROR - Couldn't export due to missing wired configurations",
                "FEHLER - Export nicht m\u00f6glich, es fehlen Wired-Konfigurationen");
        Messages.put("wired.export.error.missing_configs.log",
                "Couldn't export due to missing wired configurations",
                "Export nicht m\u00f6glich, es fehlen Wired-Konfigurationen");
        Messages.put("wired.export.fetching",
                "Fetching additional %s wired configurations before exporting... do not alter the room",
                "Es werden noch %s Wired-Konfigurationen ausgelesen, bevor exportiert wird... ver\u00e4ndere den Raum nicht");
        Messages.put("wired.export.no_open_wired",
                "Do not open wired while the extension is fetching configurations",
                "\u00d6ffne kein Wired, w\u00e4hrend die Extension Konfigurationen ausliest");
        Messages.put("wired.export.remaining",
                "%d wired configurations left to retrieve..",
                "Noch %d Wired-Konfigurationen auszulesen..");
        Messages.put("wired.export.retry_missing",
                "WARNING - Did not retrieve all wired. Retrying %d missing wired..",
                "WARNUNG - Es wurden nicht alle Wired ausgelesen. %d fehlende Wired werden erneut versucht..");
        Messages.put("wired.export.retry_missing.log",
                "Did not retrieve all wired. Retrying %d missing wired..",
                "Es wurden nicht alle Wired ausgelesen. %d fehlende Wired werden erneut versucht..");
        Messages.put("wired.setup.progress",
                "Setting up wired",
                "Richte Wired ein");
        Messages.put("wired.setup.start",
                "Setting up wired..",
                "Wired wird eingerichtet..");
    }
}
