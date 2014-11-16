package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.tile.misc.TileTeleporter;
import appeng.util.Platform;

public class BlockTeleporter extends AEBaseBlock
{

	public BlockTeleporter( )
	{
		super( BlockTeleporter.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.Core ) );
		setTileEntity( TileTeleporter.class );
	}

	@Override
	public void onEntityWalking( World w, int x, int y, int z, Entity p )
	{
		super.onEntityWalking( w, x, y, z, p );
		if ( p instanceof EntityPlayer )
		{
			TileTeleporter tg = getTileEntity( w, x, y, z );
			if ( tg != null )
			{
				if ( Platform.isClient( ) )
					return;

				try
				{
					tg.teleport( ( EntityPlayer ) p );
				} catch ( GridAccessException e )
				{
					e.printStackTrace( );
				}
			}
		}
	}

	@Override
	public boolean onActivated( World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ )
	{
		TileTeleporter tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isClient( ) )
				return true;

			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_TELEPORTER );
			return true;
		}
		return false;
	}

}
