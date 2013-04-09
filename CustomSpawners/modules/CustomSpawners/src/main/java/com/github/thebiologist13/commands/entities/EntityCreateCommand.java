package com.github.thebiologist13.commands.entities;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.thebiologist13.CustomSpawners;
import com.github.thebiologist13.SpawnableEntity;

public class EntityCreateCommand extends EntityCommand {

	public EntityCreateCommand(CustomSpawners plugin) {
		super(plugin);
	}

	public EntityCreateCommand(CustomSpawners plugin, String mainPerm) {
		super(plugin, mainPerm);
	}

	@Override
	public void run(SpawnableEntity entity, CommandSender sender, String subCommand, String[] args) {
		
		final String NOT_ALLOWED_ENTITY = ChatColor.RED + "That entity type is disabled for those without permission.";
		final String NONEXISTANT_ENTITY = ChatColor.RED + "That is not a entity type";
		
		String in = getValue(args, 0, "pig");
		
		SpawnableEntity newEntity = null;
		
		boolean hasOverride = true;
		
		if(sender instanceof Player) {
			hasOverride = ((Player) sender).hasPermission("customspawners.limitoverride");
		}
		
		try {
			newEntity = PLUGIN.createEntity(in, hasOverride);
		} catch(IllegalArgumentException e) {
			
			if(e.getMessage() == null) {
				e.printStackTrace();
				return;
			}
			
			if(e.getMessage().equals("Invalid entity type.")) {
				PLUGIN.sendMessage(sender, NONEXISTANT_ENTITY);
				return;
			} else if(e.getMessage().equals("Not allowed entity.")) {
				PLUGIN.sendMessage(sender, NOT_ALLOWED_ENTITY);
				return;
			}
		}
		
		if(newEntity == null) {
			PLUGIN.sendMessage(sender, NONEXISTANT_ENTITY);
			return;
		}
		
		CustomSpawners.entities.put(newEntity.getId(), newEntity);
		
		if(CONFIG.getBoolean("data.autosave") && CONFIG.getBoolean("data.saveOnCreate")) {
			PLUGIN.getFileManager().autosave(newEntity);
		}
		
		PLUGIN.sendMessage(sender, ChatColor.GREEN + "Successfully created a new " + ChatColor.GOLD + 
				in + ChatColor.GREEN + " entity with ID number " + ChatColor.GOLD + newEntity.getId() + ChatColor.GREEN + "!");
		
	}
	
}