package eu.ha3.presencefootsteps.sound;

import java.io.IOException;
import java.util.Map;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsFile;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.player.ImmediateSoundPlayer;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import eu.ha3.presencefootsteps.util.ResourceUtils;
import eu.ha3.presencefootsteps.util.BlockReport.Reportable;
import eu.ha3.presencefootsteps.world.GolemLookup;
import eu.ha3.presencefootsteps.world.Index;
import eu.ha3.presencefootsteps.world.LocomotionLookup;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.StateLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public record Isolator (
        Variator variator,
        Index<Entity, Locomotion> locomotions,
        Lookup<EntityType<?>> golems,
        Lookup<BlockState> blocks,
        Lookup<BlockSoundGroup> primitives,
        SoundPlayer soundPlayer,
        StepSoundPlayer stepPlayer,
        AcousticLibrary acoustics
    ) implements Reportable {
    private static final Identifier BLOCK_MAP = new Identifier("presencefootsteps", "config/blockmap.json");
    private static final Identifier GOLEM_MAP = new Identifier("presencefootsteps", "config/golemmap.json");
    private static final Identifier LOCOMOTION_MAP = new Identifier("presencefootsteps", "config/locomotionmap.json");
    private static final Identifier PRIMITIVE_MAP = new Identifier("presencefootsteps", "config/primitivemap.json");
    private static final Identifier ACOUSTICS = new Identifier("presencefootsteps", "config/acoustics.json");
    private static final Identifier VARIATOR = new Identifier("presencefootsteps", "config/variator.json");

    public Isolator(SoundEngine engine) {
        this(engine, new ImmediateSoundPlayer(engine));
    }

    public Isolator(SoundEngine engine, SoundPlayer player) {
        this(engine, player, (StepSoundPlayer)player);
    }

    public Isolator(SoundEngine engine, SoundPlayer player, StepSoundPlayer stepPlayer) {
        this(new Variator(), new LocomotionLookup(engine), new GolemLookup(), new StateLookup(), new PrimitiveLookup(), player, stepPlayer, new AcousticsPlayer(player));
    }

    public boolean load(ResourceManager manager) {
        boolean hasConfigurations = false;
        hasConfigurations |= ResourceUtils.forEachReverse(BLOCK_MAP, manager, blocks()::load);
        hasConfigurations |= ResourceUtils.forEach(GOLEM_MAP, manager, golems()::load);
        hasConfigurations |= ResourceUtils.forEach(PRIMITIVE_MAP, manager, primitives()::load);
        hasConfigurations |= ResourceUtils.forEach(LOCOMOTION_MAP, manager, locomotions()::load);
        hasConfigurations |= ResourceUtils.forEach(ACOUSTICS, manager, reader -> AcousticsFile.read(reader, acoustics()::addAcoustic));
        hasConfigurations |= ResourceUtils.forEach(VARIATOR, manager, variator()::load);
        return hasConfigurations;
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, BlockSoundGroup> groups) throws IOException {
        writer.object(() -> {
            writer.object("blocks", () -> blocks().writeToReport(full, writer, groups));
            writer.object("entities", () -> locomotions().writeToReport(full, writer, groups));
            writer.object("primitives", () -> primitives().writeToReport(full, writer, groups));
        });
    }
}
