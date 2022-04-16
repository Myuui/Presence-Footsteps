package eu.ha3.presencefootsteps;

import java.nio.file.Path;

import eu.ha3.presencefootsteps.config.JsonFile;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;

public class PFConfig extends JsonFile {

    private int volume = 70;

    private String stance = "UNKNOWN";

    private boolean multiplayer = true;

    private boolean global = true;

    private transient final PresenceFootsteps pf;

    public PFConfig(Path file, PresenceFootsteps pf) {
        super(file);

        this.pf = pf;
    }

    public boolean toggleMultiplayer() {
        multiplayer = !multiplayer;
        save();

        return multiplayer;
    }

    public boolean toggleGlobal() {
        global = !global;
        save();

        return global;
    }

    public Locomotion setLocomotion(Locomotion loco) {

        if (loco != getLocomotion()) {
            stance = loco.name();
            save();

            pf.getEngine().reload();
        }

        return loco;
    }

    public Locomotion getLocomotion() {
        return Locomotion.byName(stance);
    }

    public boolean getEnabledGlobal() {
        return global && getEnabled();
    }

    public boolean getEnabledMP() {
        return multiplayer && getEnabled();
    }

    public boolean getEnabled() {
        return getVolume() > 0;
    }

    public int getVolume() {
        return MathHelper.clamp(volume, 0, 100);
    }

    public float setVolume(float volume) {
        volume = volume > 97 ? 100 : volume < 3 ? 0 : (int)volume;

        if (this.volume != volume) {
            boolean wasEnabled = getEnabled();

            this.volume = (int)volume;
            save();

            if (getEnabled() != wasEnabled) {
                pf.getEngine().reload();
            }
        }

        return getVolume();
    }

    public void populateCrashReport(CrashReportSection section) {
        section.add("PF Global Volume", volume);
        section.add("PF User's Selected Stance", stance + " (" + getLocomotion() + ")");
        section.add("Enabled Global", global);
        section.add("Enabled Multiplayer", multiplayer);
    }
}
