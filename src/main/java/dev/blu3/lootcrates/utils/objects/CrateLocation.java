package dev.blu3.lootcrates.utils.objects;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collections;

import static dev.blu3.lootcrates.LootCrates.*;
import static dev.blu3.lootcrates.utils.Utils.getWorld;

public class CrateLocation {

    public CratePos exact;
    public CratePos around;
    public String worldName;
//    public void generatePos() {
//        Vec3i firstLoc;
//        Vec3i secondLoc;
//        boolean valid = false;
//        ArrayList<SimpleWorld> worlds = dataManager.getGeneral().worlds;
//        Collections.shuffle(worlds);
//        this.worldName = worlds.get(0).registryName;
//        ServerWorld world = getWorld(worldName);
//        do {
//            int centerX = (int) world.getWorldBorder().getCenterX();
//            int centerZ = (int) world.getWorldBorder().getCenterZ();
//            Vector2i randomFirst = getRandom(new Vector2i(centerX, centerZ), 0, (int) (world.getWorldBorder().getDistanceInsideBorder(centerX, centerZ) / 2) - 1000);
//            firstLoc = new Vec3i(randomFirst.x, 62, randomFirst.y);
//            Vector2i randomSecond = getRandom(randomFirst, dataManager.getGeneral().secondMinimumRadius, dataManager.getGeneral().secondMaximumRadius);
//            secondLoc = new Vec3i(randomSecond.x, 62, randomSecond.y);
//
//            BlockPos secondPos = new BlockPos(secondLoc);
//
//            if (world.getBlockState(secondPos.up(1)) == Blocks.AIR.getDefaultState()
//                    && world.getBlockState(secondPos) == Blocks.AIR.getDefaultState()
//                    && world.getBlockState(secondPos.down(2)) != Blocks.AIR.getDefaultState()
//                    && world.getBlockState(secondPos.down(2)) != Blocks.WATER.getDefaultState()) {
//                valid = true;
//                if (FabricLoader.getInstance().isModLoaded("griefdefender")) {
//                    final Claim claim = GriefDefender.getCore().getClaimAt(secondPos);
//                    valid = claim == null || claim.isWilderness();
//                } else if (FabricLoader.getInstance().isModLoaded("flan")) {
//                    io.github.flemmli97.flan.claim.Claim claim = ClaimStorage.get(world).getClaimAt(secondPos);
//                    valid = claim == null;
//                }
//            }
//        } while (!valid);
//
//        for (int y = 255; y > 0; y--) {
//            secondLoc = new Vec3i(secondLoc.getX(), y, secondLoc.getZ());
//            if(world.getBlockState(new BlockPos(secondLoc)) != Blocks.AIR.getDefaultState()
//                    && world.getBlockState(new BlockPos(secondLoc)) != Blocks.WATER.getDefaultState()) break;
//        }
//        this.exact = new CratePos(secondLoc.up(1));
//        this.around = new CratePos(firstLoc);
//    }

    public void generatePos() {
        Vec3i firstLoc;
        Vec3i secondLoc;
        boolean valid = false;
        ArrayList<SimpleWorld> worlds = dataManager.getGeneral().worlds;
        Collections.shuffle(worlds);
        this.worldName = worlds.get(0).registryName;
        ServerWorld world = getWorld(worldName);

        BlockPos surfacePos;
        do {
            int centerX = (int) world.getWorldBorder().getCenterX();
            int centerZ = (int) world.getWorldBorder().getCenterZ();
            Vector2i randomFirst = getRandom(new Vector2i(centerX, centerZ), 0, (int) (world.getWorldBorder().getDistanceInsideBorder(centerX, centerZ) / 2) - 1000);
            firstLoc = new Vec3i(randomFirst.x, 62, randomFirst.y);
            Vector2i randomSecond = getRandom(randomFirst, dataManager.getGeneral().secondMinimumRadius, dataManager.getGeneral().secondMaximumRadius);
            secondLoc = new Vec3i(randomSecond.x, 62, randomSecond.y);

            // Calculate the max high in the surface in that position
            surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(secondLoc.getX(), 0, secondLoc.getZ()));

            if (world.getBlockState(surfacePos.up(1)) == Blocks.AIR.getDefaultState()
                    && world.getBlockState(surfacePos) != Blocks.AIR.getDefaultState()
                    && world.getBlockState(surfacePos) != Blocks.WATER.getDefaultState()) {
                valid = true;
                if (FabricLoader.getInstance().isModLoaded("griefdefender")) {
                    final Claim claim = GriefDefender.getCore().getClaimAt(surfacePos);
                    valid = claim == null || claim.isWilderness();
                } else if (FabricLoader.getInstance().isModLoaded("flan")) {
                    io.github.flemmli97.flan.claim.Claim claim = ClaimStorage.get(world).getClaimAt(surfacePos);
                    valid = claim == null;
                }
            }
        } while (!valid);

        this.exact = new CratePos(surfacePos.up(1));
        this.around = new CratePos(firstLoc);
    }

    private Vector2i getRandom(Vector2i center, int minRadius, int maxRadius) {
        double t = random.nextInt(360 * 100) / 100D;
        double r = random.nextInt(maxRadius - minRadius) + minRadius;

        double x = Math.cos(t) * r + center.x;
        double y = Math.sin(t) * r + center.y;
        return new Vector2i((int) x, (int) y);
    }

    public static class CratePos {

        public CratePos() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        public CratePos(Vec3i vec) {
            this.x = vec.getX();
            this.y = vec.getY();
            this.z = vec.getZ();
        }

        public CratePos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private double x;
        private double y;
        private double z;

        public Vec3d getVec3() {
            return new Vec3d(x, y, z);
        }

        public BlockPos getBlockPos() {
            return new BlockPos((int) x, (int) y, (int) z);
        }
    }
}
