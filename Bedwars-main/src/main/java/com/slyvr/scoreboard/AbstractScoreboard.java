package com.slyvr.scoreboard;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.scoreboard.DisplaySlot;

import com.google.common.base.Preconditions;
import com.slyvr.api.scoreboard.Scoreboard;
import com.slyvr.util.ChatUtils;

public abstract class AbstractScoreboard implements Scoreboard {

	protected Map<Integer, String> lines = new HashMap<>();

	protected AnimatedTitle title;
	protected String display;
	protected DisplaySlot slot;

	public AbstractScoreboard(AnimatedTitle title) {
		Preconditions.checkNotNull(title, "Scoreboard title cannot be null!");

		this.title = title;
		this.display = title.next();
		this.slot = DisplaySlot.SIDEBAR;
	}

	@Override
	public AnimatedTitle getTitle() {
		return this.title;
	}

	@Override
	public void setTitle(AnimatedTitle title) {
		if (title != null)
			this.title = title;
	}

	@Override
	public String getDisplayTitle() {
		return this.display;
	}

	@Override
	public void setDisplayTitle(String title) {
		if (title != null)
			this.display = title;
	}

	@Override
	public Map<Integer, String> getLines() {
		return new HashMap<>(this.lines);
	}

	@Override
	public String getText(int line) {
		return this.lines.get(line);
	}

	@Override
	public void setText(int line, String text) {
		if (text != null && line >= 1 && line <= 15)
			this.lines.put(line, ChatUtils.format(text));
	}

	@Override
	public String removeText(int line) {
		return this.lines.remove(line);
	}

	@Override
	public DisplaySlot getDisplaySlot() {
		return this.slot;
	}

	@Override
	public void setDisplaySlot(DisplaySlot slot) {
		if (slot != null)
			this.slot = slot;
	}

}