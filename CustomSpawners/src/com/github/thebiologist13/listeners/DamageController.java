package com.github.thebiologist13.listeners;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.github.thebiologist13.CustomSpawners;
import com.github.thebiologist13.SpawnableEntity;
import com.github.thebiologist13.Spawner;

public class DamageController {
	
	//The key is an entity ID. The value is ticks left. If an entity ID is in this list, it has caught fire from somewhere else (other than fire ticks prop.) and should take fire damage.
	public static ConcurrentHashMap<Integer, Integer> negatedFireImmunity = new ConcurrentHashMap<Integer, Integer>();
		
	//Hashmap for extra health
	public static ConcurrentHashMap<Integer, Integer> extraHealthEntities = new ConcurrentHashMap<Integer, Integer>();
		
	//HashMap for extra damage entities
	//TODO any spawned entity with a damage mod MUST be added to this.
	public static ConcurrentHashMap<Integer, Integer> damageModEntities = new ConcurrentHashMap<Integer, Integer>();
	
	//Map of entities that should be damaged by an explosion when it occurs.
	//public static ConcurrentHashMap<Integer, Entity> explodingEntities = new ConcurrentHashMap<Integer, Entity>();
	
	private CustomSpawners plugin = null;
	
	public DamageController(CustomSpawners plugin) {
		this.plugin = plugin;
	}
	
	/*
	 * Before it takes damage, it must be an allowed type of damage. Then
	 * it has to be checked if it has a a damage mod. Finally detract normal
	 * health unless it has extra health.
	 */
	
	public int getModifiedDamage(EntityDamageEvent ev) {
		
		DamageCause cause = ev.getCause();
		Entity entity = ev.getEntity();
		int mobId = entity.getEntityId();
		int damage = ev.getDamage();
		SpawnableEntity e = plugin.getEntityFromSpawner(entity);
		
		//If what was damaged was a SpawnableEntity
		if(e != null) {
			
			//Anger appropriately
			if(ev instanceof EntityDamageByEntityEvent) {
				//Cast to EntityDamageByEntityEvent
				EntityDamageByEntityEvent eve = (EntityDamageByEntityEvent) ev;
				//Damager
				Entity damager = eve.getDamager();
				//Spawner
				Spawner s = plugin.getSpawnerWithEntity(entity);
				
				if(damager instanceof Player) {
					if(e != null) {
						if(s.getPassiveMobs().containsKey(entity)) {
							int id = entity.getEntityId();
							s.removePassiveMob(id);
							s.addMob(id, e);
						}
						
					}
					
				} 
				
			}
			
			//If it doesn't return from these statements, the damage is valid
			if(e.isUsingBlacklist()) {
				ArrayList<String> black = e.getDamageBlacklist();
				
				if(black.contains(cause.name())) {
					
					plugin.printDebugMessage("Damage taken in damage blacklist.");
					ev.setCancelled(true);
					return 0;
					
				} else if(black.contains("SPAWNER_FIRE_TICKS") && cause.equals(DamageCause.FIRE_TICK)) {
					
					plugin.printDebugMessage("Damage taken in damage blacklist.");
					int id = entity.getEntityId();
					
					if(negatedFireImmunity.containsKey(id) && !e.getDamageBlacklist().contains(DamageCause.FIRE_TICK.name())) {
						int newTicks = negatedFireImmunity.get(id) - 1;
						negatedFireImmunity.replace(id, newTicks);
					}
					
					ev.setCancelled(true);
					return 0;
					
				} else if(black.contains("ITEM") && (ev instanceof EntityDamageByEntityEvent)) {
					plugin.printDebugMessage("Damage taken in damage blacklist.");
					EntityDamageByEntityEvent eve = (EntityDamageByEntityEvent) ev;
					
					if(eve.getDamager() instanceof Player) {
						Player p = (Player) eve.getDamager();
						if(e.getItemDamageList().contains(p.getItemInHand())) {
							ev.setCancelled(true);
							return 0;
						}
						
					}
					
				}
				
			} else if(e.isUsingWhitelist()) {
				
				ArrayList<String> white = e.getDamageBlacklist();
				
				if(!white.contains(cause.name())) {
					
					plugin.printDebugMessage("Damage taken in damage whitelist.");
					ev.setCancelled(true);
					return 0;
					
				} else if(!(white.contains("SPAWNER_FIRE_TICKS") && cause.equals(DamageCause.FIRE_TICK))) {
					
					plugin.printDebugMessage("Damage taken in damage whitelist.");
					int id = entity.getEntityId();
					
					if(negatedFireImmunity.containsKey(id) && !e.getDamageBlacklist().contains(DamageCause.FIRE_TICK.name())) {
						int newTicks = negatedFireImmunity.get(id) - 1;
						negatedFireImmunity.replace(id, newTicks);
					}
					
					ev.setCancelled(true);
					return 0;
					
				} else if(!(white.contains("ITEM") && (ev instanceof EntityDamageByEntityEvent))) {
					
					plugin.printDebugMessage("Damage taken in damage whitelist.");
					EntityDamageByEntityEvent eve = (EntityDamageByEntityEvent) ev;
					
					if(eve.getDamager() instanceof Player) {
						Player p = (Player) eve.getDamager();
						if(e.getItemDamageList().contains(p.getItemInHand())) {
							ev.setCancelled(true);
							return 0;
						}
						
					}
					
				}
				
			}
			
			/*
			 * Here we check if it was damaged by an SpawnableEntity. If the entity was from a spawner,
			 * apply the damage mod.
			 */
				
			damage = getModDamage(ev);
			plugin.printDebugMessage("Damage to take: " + damage);

			/*
			 * Finally apply the damage to extra health and health accordingly.
			 */
			if(extraHealthEntities.containsKey(mobId)) {
				plugin.printDebugMessage("Extra health entity.");
				//Get the remaining extra health after "damage"
				int newExtraHealth = extraHealthEntities.get(mobId) - damage;
				plugin.printDebugMessage("New extra health: " + newExtraHealth);
				//If the new extra health <= 0, deal actual damage to entity
				if(newExtraHealth <= 0) {
					plugin.printDebugMessage("Lose actual health: " + Math.abs(newExtraHealth));
					extraHealthEntities.remove(mobId);
					return Math.abs(newExtraHealth);
				} else {
					plugin.printDebugMessage("Lose Extra health: " + newExtraHealth);
					extraHealthEntities.replace(mobId, newExtraHealth);
					return 0;
				}
			} else {
				return damage;
			}
			
		//If what was damaged is not a SpawnableEntity
		} else {
			
			return getModDamage(ev);
			
		}
		
	}
	
	private int getModDamage(EntityDamageEvent ev) {
		
		if(ev instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent eve = (EntityDamageByEntityEvent) ev;
			int damagerId = eve.getDamager().getEntityId();
			SpawnableEntity e = plugin.getEntityFromSpawner(eve.getDamager());

			if(e != null) {

				if(ev.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
					return 0;
				}
				
			}
			
			if(damageModEntities.containsKey(damagerId)) {
				return damageModEntities.get(damagerId);
			}

		} 

		return ev.getDamage();
		
	}
	
}
