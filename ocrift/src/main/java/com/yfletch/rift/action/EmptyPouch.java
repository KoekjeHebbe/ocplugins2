package com.yfletch.rift.action;

import com.yfletch.rift.RiftContext;
import com.yfletch.rift.enums.Pouch;
import com.yfletch.rift.helper.PouchSolver;
import com.yfletch.rift.lib.ItemAction;
import com.yfletch.rift.lib.WrappedEvent;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;

public class EmptyPouch extends ItemAction<RiftContext>
{
	private final Pouch pouch;

	public EmptyPouch(Pouch pouch)
	{
		super("Empty", pouch.getItemName());
		this.pouch = pouch;
	}

	@Override
	public boolean isReady(RiftContext ctx)
	{
		Pouch nextPouch = new PouchSolver(ctx).getNextFilledPouch();
		if (nextPouch == null || pouch != nextPouch)
		{
			return false;
		}

		return ctx.isOutsideRift()
			&& !ctx.isEmpty(pouch)
			&& (ctx.hasItem(pouch.getItemId()) || ctx.hasItem(pouch.getDegradedItemId()))
			&& (!ctx.hasItem(ItemID.GUARDIAN_ESSENCE)
			|| ctx.flag("emptying-pouches"));
	}

	@Override
	public boolean isDone(RiftContext ctx)
	{
		return ctx.flag("p-empty-" + pouch.getItemId())
			|| ctx.hasItem(ItemID.GUARDIAN_ESSENCE);
	}

	@Override
	public void run(RiftContext ctx, WrappedEvent event)
	{
		int itemId = ctx.hasItem(pouch.getItemId()) ? pouch.getItemId() : pouch.getDegradedItemId();
		event.overrideItemAction(3, MenuAction.CC_OP, itemId);

		// @see fill pouch logic
		if (pouch != Pouch.COLOSSAL)
		{
			ctx.flag("p-empty-" + pouch.getItemId(), true);
		}
	}

	@Override
	public void done(RiftContext ctx)
	{
		ctx.flag("emptying-pouches", new PouchSolver(ctx).getNextFilledPouch() != null);

		if (!ctx.flag("emptying-pouches"))
		{
			// reset all clicked statuses
			ctx.flag("p-empty-" + Pouch.SMALL.getItemId(), false);
			ctx.flag("p-empty-" + Pouch.MEDIUM.getItemId(), false);
			ctx.flag("p-empty-" + Pouch.LARGE.getItemId(), false);
			ctx.flag("p-empty-" + Pouch.GIANT.getItemId(), false);
			ctx.flag("p-empty-" + Pouch.COLOSSAL.getItemId(), false);
		}
	}
}
