package com.yfletch.rift.action.cycle.common;

import com.yfletch.rift.RiftContext;
import com.yfletch.rift.enums.Cell;
import com.yfletch.rift.helper.CellTileDecider;
import com.yfletch.rift.lib.ObjectAction;
import com.yfletch.rift.lib.WrappedEvent;
import net.runelite.api.MenuAction;
import net.runelite.api.TileObject;

public class PlaceCell extends ObjectAction<RiftContext>
{
	public PlaceCell()
	{
		super("Place-cell", "Cell tile");
	}

	@Override
	public boolean isReady(RiftContext ctx)
	{
		return !ctx.isPregame()
			&& ctx.getGameTime() > 110
			&& !ctx.hasGuardianStones()
			&& ctx.hasCell();
	}

	@Override
	public boolean isWorking(RiftContext ctx)
	{
		// ensures spam clicking because target choice
		// can change on the fly
		return false;
	}

	@Override
	public boolean isDone(RiftContext ctx)
	{
		return !ctx.hasItem(Cell.UNCHARGED.getItemId())
			&& !ctx.hasCell();
	}

	@Override
	public void run(RiftContext ctx, WrappedEvent event)
	{
		TileObject tile = new CellTileDecider(ctx).pick();
		event.overrideObjectAction("Place-cell", MenuAction.GAME_OBJECT_FIRST_OPTION, tile);
		ctx.flag("crafting", false);
	}
}
