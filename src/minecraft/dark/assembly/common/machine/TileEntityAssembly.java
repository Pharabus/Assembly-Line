package dark.assembly.common.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import dark.api.INetworkEnergyPart;
import dark.assembly.common.AssemblyLine;
import dark.core.blocks.TileEntityMachine;
import dark.core.tile.network.NetworkSharedPower;
import dark.core.tile.network.NetworkTileEntities;

/** A class to be inherited by all machines on the assembly line. This class acts as a single peace
 * in a network of similar tiles allowing all to share power from one or more sources
 *
 * @author DarkGuardsman */
public abstract class TileEntityAssembly extends TileEntityMachine implements IPacketReceiver, INetworkEnergyPart
{
    public static int refresh_min_rate = 20;
    public static int refresh_diff = 9;
    /** Network used to link assembly machines together */
    private NetworkAssembly assemblyNetwork;
    /** Tiles that are connected to this */
    public List<TileEntity> connectedTiles = new ArrayList<TileEntity>();
    /** Random instance */
    public Random random = new Random();
    /** Random rate by which this tile updates its connections */
    private int updateTick = 1;

    public TileEntityAssembly(float wattsPerTick)
    {
        super(wattsPerTick);
    }

    public TileEntityAssembly(float wattsPerTick, float maxEnergy)
    {
        super(wattsPerTick, maxEnergy);
    }

    @Override
    public void invalidate()
    {
        NetworkAssembly.invalidate(this);
        if (this.getTileNetwork() != null)
        {
            this.getTileNetwork().splitNetwork(this.worldObj, this);
        }
        super.invalidate();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!this.worldObj.isRemote)
        {
            this.prevRunning = this.running;

            if (ticks % updateTick == 0)
            {
                this.updateTick = ((int) random.nextInt(1 + refresh_diff) + refresh_min_rate);
                this.refresh();
            }
            this.running = this.canRun();
            if (running != prevRunning)
            {
                this.sendPowerUpdate();
            }
        }

        this.onUpdate();
    }

    @Override
    public String getChannel()
    {
        return AssemblyLine.CHANNEL;
    }

    @Override
    public boolean canRun()
    {
        //TODO add check for network power
        return super.canRun() || AssemblyLine.REQUIRE_NO_POWER;
    }

    /** Same as updateEntity */
    @Deprecated
    public void onUpdate()
    {

    }

    /** Checks to see if this assembly tile can run using several methods */
    public boolean isRunning()
    {
        return this.running;
    }


    @Override
    public boolean canTileConnect(TileEntity entity, ForgeDirection dir)
    {
        return entity != null && entity instanceof TileEntityAssembly;
    }

    @Override
    public void refresh()
    {
        if (this.worldObj != null && !this.worldObj.isRemote)
        {
            this.connectedTiles.clear();

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj);
                if (tileEntity instanceof TileEntityAssembly && ((TileEntityAssembly) tileEntity).canTileConnect(this, dir.getOpposite()))
                {
                    this.getTileNetwork().merge(((TileEntityAssembly) tileEntity).getTileNetwork(), this);
                    connectedTiles.add(tileEntity);
                }
            }
        }
    }

    @Override
    public List<TileEntity> getNetworkConnections()
    {
        return this.connectedTiles;
    }

    @Override
    public NetworkAssembly getTileNetwork()
    {
        if (this.assemblyNetwork == null)
        {
            this.assemblyNetwork = new NetworkAssembly(this);
        }
        return this.assemblyNetwork;
    }

    @Override
    public void setTileNetwork(NetworkTileEntities network)
    {
        if (network instanceof NetworkAssembly)
        {
            this.assemblyNetwork = (NetworkAssembly) network;
        }

    }

    @Override
    public boolean consumePower(float watts, boolean doDrain)
    {
        return ((NetworkSharedPower) this.getTileNetwork()).drainPower(this, watts, doDrain);
    }

    /** Amount of energy this tile runs on per tick */
    public double getWattLoad()
    {
        return 1;
    }

    @Override
    public float getRequest(ForgeDirection direction)
    {
        return this.getTileNetwork().getEnergySpace();
    }

    @Override
    public void togglePowerMode()
    {
        ((NetworkSharedPower) this.getTileNetwork()).setPowerLess(this.runPowerLess());
    }

    @Override
    public float getEnergyStored()
    {
        return ((NetworkSharedPower) this.getTileNetwork()).getEnergyStored();
    }

    @Override
    public float getPartEnergy()
    {
        return this.energyStored;
    }

    @Override
    public float getPartMaxEnergy()
    {
        return this.MAX_WATTS;
    }

    @Override
    public void setPartEnergy(float energy)
    {
        this.energyStored = energy;
    }

    @Override
    public boolean mergeDamage(String effect)
    {
        this.onDisable(20);
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    public String toString()
    {
        return "[AssemblyTile]@" + (new Vector3(this).toString());
    }
}
