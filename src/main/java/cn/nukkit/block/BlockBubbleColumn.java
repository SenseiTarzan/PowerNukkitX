package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.block.BlockFadeEvent;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.level.particle.SplashParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;

import java.util.concurrent.ThreadLocalRandom;

public class BlockBubbleColumn extends BlockTransparentMeta {

    public BlockBubbleColumn() {
        this(0);
    }

    public BlockBubbleColumn(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BUBBLE_COLUMN;
    }

    @Override
    public String getName() {
        return "Bubble Column";
    }

    @Override
    public int getWaterloggingLevel() {
        return 2;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public boolean canBeFlowedInto() {
        return true;
    }
    
    @Override
    public Item[] getDrops(Item item) {
        return new Item[0];
    }
    
    @Override
    public Item toItem() {
        return new ItemBlock(new BlockAir());
    }

    @Override
    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return null;
    }
    
    @Override
    public boolean isBreakable(Item item) {
        return false;
    }

    @Override
    public boolean canBePlaced() {
        return false;
    }

    @Override
    public boolean canBeReplaced() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        return this;
    }
    
    @Override
    public void addVelocityToEntity(Entity entity, Vector3 vector) {
        if (entity.canBeMovedByCurrents()) {
            if (up().getId() == AIR) {
                if (getDamage() == 0) {
                    vector.y = Math.max(-0.9, vector.y - 0.03);
                } else {
                    vector.y = Math.min(1.8, vector.y + 0.1);
                }
                
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for(int i = 0; i < 2; ++i) {
                    level.addParticle(new SplashParticle(add(random.nextFloat(), random.nextFloat() + 1, random.nextFloat())));
                    level.addParticle(new BubbleParticle(add(random.nextFloat(), random.nextFloat() + 1, random.nextFloat())));
                }
                
            } else {
                if (getDamage() == 0) {
                    vector.y = Math.max(-0.3, vector.y - 0.03);
                } else {
                    vector.y = Math.min(0.7, vector.y + 0.06);
                }
            }
        }
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (down().getId() == MAGMA) {
            setDamage(1);
        }
        this.getLevel().setBlock(this, 1, new BlockWater(), true, false);
        this.getLevel().setBlock(this, this, true, true);
        return true;
    }
    
    @Override
    public double getHardness() {
        return 100;
    }

    @Override
    public double getResistance() {
        return 500;
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    public void onEntityCollide(Entity entity) {
        entity.resetFallDistance();
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            Block water = getLevelBlockAtLayer(1);
            if (!(water instanceof BlockWater) || water.getDamage() != 0 && water.getDamage() != 8) {
                fadeOut(water);
                return type;
            }

            if (water.getDamage() == 8) {
                water.setDamage(0);
                this.getLevel().setBlock(this, 1, water, true, false);
            }

            Block down = down();
            if (down.getId() == BUBBLE_COLUMN) {
                if (down.getDamage() != this.getDamage()) {
                    this.getLevel().setBlock(this, down, true, true);
                }
            } else if (down.getId() == MAGMA) {
                if (this.getDamage() != 1) {
                    setDamage(1);
                    this.getLevel().setBlock(this, this, true, true);
                }
            } else if (down.getId() == SOUL_SAND) {
                if (this.getDamage() != 0) {
                    setDamage(0);
                    this.getLevel().setBlock(this, this, true, true);
                }
            } else {
                fadeOut(water);
                return type;
            }

            Block up = up();
            if (up instanceof BlockWater && (up.getDamage() == 0 || up.getDamage() == 8)) {
                BlockFromToEvent event = new BlockFromToEvent(this, up);
                if (!event.isCancelled()) {
                    this.getLevel().setBlock(up, 1, new BlockWater(), true, false);
                    this.getLevel().setBlock(up, 0, new BlockBubbleColumn(this.getDamage()), true, true);
                }
            }

            return type;
        }

        return 0;
    }

    private void fadeOut(Block water) {
        BlockFadeEvent event = new BlockFadeEvent(this, water.clone());
        if (!event.isCancelled()) {
            this.getLevel().setBlock(this, 1, new BlockAir(), true, false);
            this.getLevel().setBlock(this, 0, event.getNewState(), true, true);
        }
    }

}