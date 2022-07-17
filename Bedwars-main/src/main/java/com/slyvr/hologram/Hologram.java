package com.slyvr.hologram;

import java.util.List;

import org.bukkit.Location;

public interface Hologram {

	String getText(int line);

	void setText(int line, String text);

	void addLine(String text);

	void removeLine(int line);

	List<String> getLines();

	void setLines(List<String> lines);

	void setVisible(int line, boolean visible);

	boolean isVisible(int line);

	void teleport(Location loc);

	void remove();

	int size();

}