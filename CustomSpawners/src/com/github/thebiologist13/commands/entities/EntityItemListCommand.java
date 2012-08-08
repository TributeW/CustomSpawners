package com.github.thebiologist13.commands.entities;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.thebiologist13.CustomSpawners;
import com.github.thebiologist13.SpawnableEntity;
import com.github.thebiologist13.commands.SpawnerCommand;

public class EntityItemListCommand extends SpawnerCommand {

	private CustomSpawners plugin = null;
	
	private Logger log = null;
	
	public EntityItemListCommand(CustomSpawners plugin) {
		this.plugin = plugin;
		this.log = plugin.log;
	}
	
	@Override
	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		//Player
		Player p = null;
		//SpawnableEntity
		SpawnableEntity s = null;
		//Item ID
		int itemId = 0;
		//Perms
		String itemlistPerm = "customspawners.entitites.itemlist";

		if(!(arg0 instanceof Player)) {
			log.info(NO_CONSOLE);
			return;
		}

		p = (Player) arg0;

		if(p.hasPermission(itemlistPerm) && arg3[0].equalsIgnoreCase("additem")) {

			if(CustomSpawners.entitySelection.containsKey(p) && arg3.length == 2) {

				s = plugin.getEntity(CustomSpawners.entitySelection.get(p).toString());
				
				String value = arg3[1];
				
				if(!plugin.isInteger(value)) {
					p.sendMessage(SPECIFY_NUMBER);
					return;
				}
				
				itemId = Integer.parseInt(value);
				
			} else if(arg3.length == 2) {
				p.sendMessage(NEEDS_SELECTION);
				return;
			} else if(arg3.length == 3) {

				s = plugin.getEntity(arg3[1]);

				if(s == null) {
					p.sendMessage(NO_ID);
					return;
				}
				
				String value = arg3[2];
				
				if(!plugin.isInteger(value)) {
					p.sendMessage(SPECIFY_NUMBER);
					return;
				}
				
				itemId = Integer.parseInt(value);
				
			} else {
				p.sendMessage(GENERAL_ERROR);
				return;
			}
			
			//Set
			s.addItemDamage(itemId);
			
			//Success
			p.sendMessage(ChatColor.GREEN + "Added item " + ChatColor.GOLD + String.valueOf(itemId) + ChatColor.GREEN + 
					" to itemlist for entity " + ChatColor.GOLD + plugin.getFriendlyName(s) + ChatColor.GREEN + "!");
			
		} else if(p.hasPermission(itemlistPerm) && arg3[0].equalsIgnoreCase("clearitems")) {

			if(CustomSpawners.entitySelection.containsKey(p) && arg3.length == 1) {

				s = plugin.getEntity(CustomSpawners.entitySelection.get(p).toString());
				
			} else if(arg3.length == 1) {
				p.sendMessage(NEEDS_SELECTION);
				return;
			} else if(arg3.length == 2) {

				s = plugin.getEntity(arg3[1]);

				if(s == null) {
					p.sendMessage(NO_ID);
					return;
				}
				
			} else {
				p.sendMessage(GENERAL_ERROR);
				return;
			}
			
			//Clear
			s.setItemDamageList(new ArrayList<Integer>());
			
			//Success
			p.sendMessage(ChatColor.GREEN + "Cleared itemlist for entity " +
					ChatColor.GOLD + plugin.getFriendlyName(s) + ChatColor.GREEN + "!");
			
		} else {
			p.sendMessage(NO_PERMISSION);
		}

	}

}