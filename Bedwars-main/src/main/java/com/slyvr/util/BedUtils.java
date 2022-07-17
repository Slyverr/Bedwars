package com.slyvr.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import com.slyvr.api.arena.Arena;
import com.slyvr.api.arena.BedwarsBed;
import com.slyvr.api.team.Team;
import com.slyvr.api.util.Version;

public class BedUtils {

	public static Block getOtherBedPart(Block block) {
		if (Version.getVersion().isNewAPI()) {
			BlockData data = block.getBlockData();
			if (!(data instanceof Bed))
				return null;

			Bed bed = (Bed) data;
			return block.getRelative(bed.getPart() == Part.HEAD ? bed.getFacing().getOppositeFace() : bed.getFacing());
		}

		BlockState state = block.getState();

		MaterialData data = state.getData();
		if (!(data instanceof org.bukkit.material.Bed))
			return null;

		org.bukkit.material.Bed bed = (org.bukkit.material.Bed) data;
		return block.getRelative(bed.isHeadOfBed() ? bed.getFacing().getOppositeFace() : bed.getFacing());

	}

	public static BedwarsBed getArenaBed(Arena arena, Block block) {
		Location loc = block.getLocation();

		for (Team team : arena.getTeams()) {
			BedwarsBed bed = arena.getTeamBed(team);
			if (bed == null)
				continue;

			if (loc.equals(bed.getHead().getLocation()) || loc.equals(bed.getFoot().getLocation()))
				return bed;
		}

		return null;
	}

	public static void breakBed(BedwarsBed bed) {
		BedUtils.breakBed(bed.getFoot());
	}

	public static void breakBed(Block block) {
		if (Version.getVersion().isNewAPI()) {
			if (!(block.getBlockData() instanceof Bed))
				return;

			Bed bed = (Bed) block.getBlockData();
			BedUtils.breakBed(block, bed.getFacing(), bed.getPart() == Part.HEAD);

		} else {
			BlockState state = block.getState();

			MaterialData data = state.getData();
			if (!(data instanceof org.bukkit.material.Bed))
				return;

			org.bukkit.material.Bed bed = (org.bukkit.material.Bed) data;
			BedUtils.breakBed(block, bed.getFacing(), bed.isHeadOfBed());
		}

	}

	private static void breakBed(Block block, BlockFace facing, boolean head) {
		if (!head)
			block.setType(Material.AIR);
		else
			block.getRelative(facing.getOppositeFace()).setType(Material.AIR);
	}

	public static boolean isBedHead(Block block) {
		if (Version.getVersion().isNewAPI()) {
			if (!(block.getBlockData() instanceof Bed))
				return false;

			Bed bed = (Bed) block.getBlockData();
			return bed.getPart() == Part.HEAD;
		}

		BlockState state = block.getState();

		MaterialData data = state.getData();
		if (!(data instanceof org.bukkit.material.Bed))
			return false;

		org.bukkit.material.Bed bed = (org.bukkit.material.Bed) data;
		return bed.isHeadOfBed();

	}

	public static boolean isBedFoot(Block block) {
		return !isBedHead(block);
	}

	private static void setBedData(Block block, Material material, BlockFace face, Part part) {
		block.setBlockData(Bukkit.createBlockData(material, data -> {
			((Bed) data).setFacing(face);
			((Bed) data).setPart(part);
		}));
	}

	public static void placeBed(Block foot, BlockFace facing, Material material) {
		if (Version.getVersion().isNewAPI()) {
			setBedData(foot.getRelative(facing), material, facing, Part.HEAD);
			setBedData(foot, material, facing, Part.FOOT);
			return;
		}

		BlockState bedFoot = foot.getState();
		BlockState bedHead = foot.getRelative(facing).getState();

		bedFoot.setType(material);
		bedHead.setType(material);
		bedFoot.setRawData((byte) 0x0);
		bedHead.setRawData((byte) 0x8);

		setDirection(bedHead, facing);
		setDirection(bedFoot, facing);

		bedFoot.update(true, false);
		bedHead.update(true, false);

	}

	private static void setDirection(BlockState state, BlockFace facing) {
		Directional directional = (Directional) state.getData();
		directional.setFacingDirection(facing);
	}

	public static boolean isBed(Block part1, Block part2) {
		return isBed(part1) && isBed(part2);
	}

	public static boolean isBed(Block block) {
		return isBed(block.getType());
	}

	public static boolean isBed(BedwarsBed bed) {
		return isBed(bed.getHead().getType()) && isBed(bed.getFoot().getType());
	}

	public static boolean isBed(Material type) {
		if (!Version.getVersion().isNewAPI())
			return type == Material.getMaterial("BED_BLOCK");

		switch (type) {
			case RED_BED:
			case BLUE_BED:
			case GREEN_BED:
			case YELLOW_BED:
			case CYAN_BED:
			case WHITE_BED:
			case PINK_BED:
			case GRAY_BED:
			case BLACK_BED:
			case BROWN_BED:
			case LIGHT_BLUE_BED:
			case LIGHT_GRAY_BED:
			case LIME_BED:
			case MAGENTA_BED:
			case ORANGE_BED:
			case PURPLE_BED:
				return true;
			default:
				return false;
		}

	}

	// private static BlockFace getCardinalDirection(Player player) {
	// double rotation = (player.getEyeLocation().getYaw() - 180) % 360;
	//
	// if (rotation < 0)
	// rotation += 360.0;
	//
	// if (0 <= rotation && rotation < 22.5)
	// return BlockFace.NORTH;
	//
	// else if (22.5 <= rotation && rotation < 67.5)
	// return BlockFace.NORTH_EAST;
	//
	// else if (67.5 <= rotation && rotation < 112.5)
	// return BlockFace.EAST;
	//
	// else if (112.5 <= rotation && rotation < 157.5)
	// return BlockFace.SOUTH_EAST;
	//
	// else if (157.5 <= rotation && rotation < 202.5)
	// return BlockFace.SOUTH;
	//
	// else if (202.5 <= rotation && rotation < 247.5)
	// return BlockFace.SOUTH_WEST;
	//
	// else if (247.5 <= rotation && rotation < 292.5)
	// return BlockFace.WEST;
	//
	// else if (292.5 <= rotation && rotation < 337.5)
	// return BlockFace.NORTH_WEST;
	//
	// else if (337.5 <= rotation && rotation < 360.0)
	// return BlockFace.NORTH;
	//
	// return null;
	// }

}