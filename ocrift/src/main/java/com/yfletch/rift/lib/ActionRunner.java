package com.yfletch.rift.lib;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.ui.overlay.components.LineComponent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionRunner<T extends ActionContext>
{
	private final T ctx;
	private final IMenuEntryProvider menuEntryProvider;
	private final List<Action<T>> actions = new ArrayList<>();

	@Getter
	private Action<T> current = null;

	public ActionRunner(T ctx, IMenuEntryProvider menuEntryProvider)
	{
		this.ctx = ctx;
		this.menuEntryProvider = menuEntryProvider;
	}

	/**
	 * Add an action to the runner. Order here is important - FIFO
	 */
	public void add(Action<T> action)
	{
		this.actions.add(action);
	}

	/**
	 * Attempt to move to the next action.
	 * Will move on if current action is done (or no current action).
	 * Next action will be the first action that is reporting as ready.
	 */
	public void tick()
	{
		if (current != null)
		{
			if (current.isDone(ctx))
			{
				// allow next action to run
				current.done(ctx);
			}
		}

		for (Action<T> action : actions)
		{
			if (action.isReady(ctx) && !action.isDone(ctx))
			{
				current = action;
				return;
			}
		}
	}

	/**
	 * Run the current action. If the current action is in
	 * the working state, the event will be consumed.
	 * <p>
	 * Will also determine the currently used/selected item
	 * and save it into the context.
	 */
	public void run(MenuOptionClicked event)
	{
		ctx.setUsingItemName(null);

		// "use"
		if (event.getMenuAction() == MenuAction.WIDGET_TARGET)
		{
			String target = event.getMenuTarget();
			if (!target.contains("->"))
			{
				target = target.replaceAll("</?col(?:=\\w{6})?>", "");
				ctx.setUsingItemName(target);
			}
		}

		if (current == null)
		{
			return;
		}

		if (current.isWorking(ctx))
		{
			event.consume();
			return;
		}

		current.run(ctx, new WrappedEvent(event, menuEntryProvider));
	}

	public boolean isWorking()
	{
		return current != null && current.isWorking(ctx);
	}

	public LineComponent getDisplayLine()
	{
		if (current == null)
		{
			return null;
		}

		return current.getDisplayLine(ctx);
	}
}
